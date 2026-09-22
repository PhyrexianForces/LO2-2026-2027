package launchpad.telemetry_viewer.websocket;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import launchpad.actions.Action;
import launchpad.actions.ActionParameter;
import launchpad.actions.SequentialAction;
import launchpad.actions.SimultaneousAction;
import launchpad.telemetry_viewer.websocket.packets.TelemetryUpdatePacket.ActionStatus;
import launchpad.telemetry_viewer.websocket.packets.TelemetryUpdatePacket.TelemetryAction;
import launchpad.telemetry_viewer.websocket.packets.TelemetryUpdatePacket.TelemetryActions;

/**
 * Turns an {@link Action} tree into its telemetry representation, giving every node a
 * {@link ActionStatus}.
 * <p>
 * A {@link SequentialAction} keeps all of its children, so their status follows from its
 * current index. A {@link SimultaneousAction} drops children as soon as they finish, so this
 * tracker remembers what each one looked like while it was running and, once it disappears,
 * reports it as completed (or cancelled, if it was removed while still incomplete) for a
 * short while afterwards. One tracker is kept per telemetry target.
 */
public class ActionTelemetryTracker {
    /** How long a finished action stays in the list after leaving its SimultaneousAction. */
    private static final long FINISHED_RETENTION_MS = 10_000;
    /** Upper bound on remembered finished actions per SimultaneousAction. */
    private static final int MAX_FINISHED_PER_CONTAINER = 25;

    private static class FinishedRecord {
        final TelemetryAction snapshot;
        final long finishedAtMs;

        FinishedRecord(TelemetryAction snapshot, long finishedAtMs) {
            this.snapshot = snapshot;
            this.finishedAtMs = finishedAtMs;
        }
    }

    private static class ContainerState {
        /** Children present on the previous snapshot, with how they looked then. Insertion-ordered. */
        Map<Action, TelemetryAction> lastLive = new IdentityHashMap<>();
        /** Most recently finished first. */
        final List<FinishedRecord> finished = new ArrayList<>();
    }

    private final Map<SimultaneousAction, ContainerState> containers = new IdentityHashMap<>();
    /** Containers visited on the snapshot in progress, so stale ones can be dropped afterwards. */
    private final Set<SimultaneousAction> visitedThisSnapshot = Collections.newSetFromMap(new IdentityHashMap<>());

    public TelemetryActions snapshot(Action root) {
        long now = System.currentTimeMillis();
        visitedThisSnapshot.clear();

        TelemetryActions result = new TelemetryActions();
        if (root == null) {
            result.actions = new ArrayList<>();
        } else if (root instanceof SequentialAction || root instanceof SimultaneousAction) {
            // The root container is the "list" itself; show its children at the top level.
            result.actions = describeChildren(root, ActionStatus.RUNNING, now);
        } else {
            result.actions = new ArrayList<>();
            result.actions.add(describe(root, root.isComplete() ? ActionStatus.COMPLETED : ActionStatus.RUNNING, now));
        }

        containers.keySet().retainAll(visitedThisSnapshot);
        return result;
    }

    private TelemetryAction describe(Action action, ActionStatus status, long now) {
        TelemetryAction telemetryAction = new TelemetryAction();
        telemetryAction.actionName = action.getClass().getSimpleName();
        telemetryAction.actionParameters = formatParameters(action);
        telemetryAction.status = status;
        telemetryAction.subActions = describeChildren(action, status, now);
        return telemetryAction;
    }

    /**
     * Children of a container. When the container itself isn't running (queued behind
     * something, or already done) its children simply inherit that status rather than
     * reporting their own internal index, which would otherwise make a queued
     * SequentialAction look like it had already started its first child.
     */
    private List<TelemetryAction> describeChildren(Action parent, ActionStatus parentStatus, long now) {
        if (parent instanceof SequentialAction) {
            return describeSequentialChildren((SequentialAction) parent, parentStatus, now);
        } else if (parent instanceof SimultaneousAction) {
            return describeSimultaneousChildren((SimultaneousAction) parent, parentStatus, now);
        }
        return new ArrayList<>();
    }

    private List<TelemetryAction> describeSequentialChildren(SequentialAction parent, ActionStatus parentStatus, long now) {
        List<TelemetryAction> children = new ArrayList<>();
        List<Action> actions = parent.getAllActions();
        int currentIndex = parent.getCurrentIndex();

        for (int i = 0; i < actions.size(); i++) {
            ActionStatus status;
            if (parentStatus != ActionStatus.RUNNING) {
                status = parentStatus;
            } else if (i < currentIndex) {
                status = ActionStatus.COMPLETED;
            } else if (i == currentIndex) {
                status = ActionStatus.RUNNING;
            } else {
                status = ActionStatus.QUEUED;
            }
            children.add(describe(actions.get(i), status, now));
        }
        return children;
    }

    private List<TelemetryAction> describeSimultaneousChildren(SimultaneousAction parent, ActionStatus parentStatus, long now) {
        List<TelemetryAction> children = new ArrayList<>();
        List<Action> live = parent.getActions();
        if (live == null) live = Collections.emptyList();

        if (parentStatus != ActionStatus.RUNNING) {
            for (Action action : live) {
                children.add(describe(action, parentStatus, now));
            }
            return children;
        }

        visitedThisSnapshot.add(parent);
        ContainerState state = containers.get(parent);
        if (state == null) {
            state = new ContainerState();
            containers.put(parent, state);
        }

        Map<Action, TelemetryAction> nowLive = new LinkedHashMap<>();
        for (Action action : live) {
            TelemetryAction described = describe(action, ActionStatus.RUNNING, now);
            nowLive.put(action, described);
            children.add(described);
        }

        // Anything that was live last time and is gone now has either finished (the
        // SimultaneousAction removes completed children in loop()) or been removed early.
        for (Map.Entry<Action, TelemetryAction> previous : state.lastLive.entrySet()) {
            if (nowLive.containsKey(previous.getKey())) continue;
            TelemetryAction snapshot = previous.getValue();
            markFinished(snapshot, previous.getKey().isComplete() ? ActionStatus.COMPLETED : ActionStatus.CANCELLED);
            state.finished.add(0, new FinishedRecord(snapshot, now));
        }

        Map<Action, TelemetryAction> identityLive = new IdentityHashMap<>();
        identityLive.putAll(nowLive);
        state.lastLive = identityLive;

        pruneFinished(state, now);
        for (FinishedRecord record : state.finished) {
            children.add(record.snapshot);
        }
        return children;
    }

    private static void markFinished(TelemetryAction action, ActionStatus status) {
        action.status = status;
        if (action.subActions == null) return;
        for (TelemetryAction sub : action.subActions) {
            // A cancelled parent's already-completed children stay completed.
            if (sub.status != ActionStatus.COMPLETED) {
                markFinished(sub, status);
            }
        }
    }

    private static void pruneFinished(ContainerState state, long now) {
        Iterator<FinishedRecord> it = state.finished.iterator();
        int index = 0;
        while (it.hasNext()) {
            FinishedRecord record = it.next();
            if (index >= MAX_FINISHED_PER_CONTAINER || now - record.finishedAtMs > FINISHED_RETENTION_MS) {
                it.remove();
            }
            index++;
        }
    }

    /** "(value, value)" from the action's {@link ActionParameter} fields, including inherited ones. */
    private static String formatParameters(Action action) {
        List<String> values = new ArrayList<>();
        for (Class<?> clazz = action.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(ActionParameter.class)) continue;
                field.setAccessible(true);
                Object value;
                try {
                    value = field.get(action);
                } catch (IllegalAccessException e) {
                    value = "<inaccessible>";
                }
                String text = value == null ? "null" : value.toString();
                values.add(text.isEmpty() ? "(null)" : text);
            }
        }
        return "(" + String.join(", ", values) + ")";
    }
}
