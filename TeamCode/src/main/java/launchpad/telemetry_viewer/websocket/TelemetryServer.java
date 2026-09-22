package launchpad.telemetry_viewer.websocket;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.RobotLog;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import launchpad.telemetry_viewer.websocket.packets.TelemetryNewConnectionPacket;
import launchpad.telemetry_viewer.websocket.packets.TelemetryUpdatePacket;
import launchpad.Loop;
import launchpad.actions.Action;
import launchpad.geometry.FieldPosition;

public class TelemetryServer extends WebSocketServer implements Loop {
    public static final int PORT = 51631;
    private static final String TAG = "TelemetryServer";

    /**
     * A single readable piece of telemetry state: either an annotated field or an annotated
     * no-arg method (possibly declared on an interface/superclass rather than the concrete class).
     */
    private interface TelemetryTarget {
        Object getValue() throws ReflectiveOperationException;
        Class<?> getValueType();
        String getTelemetryName();
    }

    private static class FieldTarget implements TelemetryTarget {
        final Object owner;
        final Field field;

        FieldTarget(Object owner, Field field) {
            this.owner = owner;
            this.field = field;
            field.setAccessible(true);
        }

        @Override
        public Object getValue() throws IllegalAccessException {
            return field.get(owner);
        }

        @Override
        public Class<?> getValueType() {
            return field.getType();
        }

        @Override
        public String getTelemetryName() {
            return field.getName();
        }
    }

    private static class MethodTarget implements TelemetryTarget {
        final Object owner;
        final Method method;

        MethodTarget(Object owner, Method method) {
            this.owner = owner;
            this.method = method;
            method.setAccessible(true);
        }

        @Override
        public Object getValue() throws ReflectiveOperationException {
            return method.invoke(owner);
        }

        @Override
        public Class<?> getValueType() {
            return method.getReturnType();
        }

        @Override
        public String getTelemetryName() {
            String name = method.getName();
            if (name.length() > 3 && name.startsWith("get") && Character.isUpperCase(name.charAt(3))) {
                return Character.toLowerCase(name.charAt(3)) + name.substring(4);
            }
            return name;
        }
    }

    /**
     * A target that has been registered for sending, along with where in the object graph it
     * was found. {@code displayName} is the bare name when it's unique across everything
     * registered, and is prefixed with {@code path} only when another target shares the name
     * (e.g. four drive motors each reporting "Velocity").
     */
    private static class RegisteredTarget {
        final TelemetryTarget target;
        /** Dot-separated field path from the OpMode to the owning object; empty for the OpMode itself. */
        final String path;
        final String baseName;
        final boolean isRobotPosition;
        String displayName;
        /** Created on first use for targets whose value is an Action. */
        ActionTelemetryTracker actionTracker;

        RegisteredTarget(TelemetryTarget target, String path, String baseName, boolean isRobotPosition) {
            this.target = target;
            this.path = path;
            this.baseName = baseName;
            this.isRobotPosition = isRobotPosition;
            this.displayName = baseName;
        }
    }

    /** A {@code @TelemetryObject} whose value was still null when scanned; retried each loop. */
    private static class PendingTelemetryObject {
        final TelemetryTarget target;
        final String path;

        PendingTelemetryObject(TelemetryTarget target, String path) {
            this.target = target;
            this.path = path;
        }
    }

    private final OpMode opMode;
    private final List<RegisteredTarget> registeredTargets = new ArrayList<>();
    private final List<PendingTelemetryObject> pendingTelemetryObjects = new ArrayList<>();
    /**
     * Every object that has already been scanned, by identity. Prevents infinite recursion
     * through reference cycles and ensures an object reachable via several fields is only
     * registered (and therefore only sent) once.
     */
    private final Set<Object> scannedObjects = Collections.newSetFromMap(new IdentityHashMap<>());

    public TelemetryServer(OpMode opMode) {
        super(new InetSocketAddress(PORT));
        this.opMode = opMode;

        scanObject(opMode, "");
        rebuildDisplayNames();

        this.setReuseAddr(true);
        this.setTcpNoDelay(true);
    }

    // ---------------------------------------------------------------------------------------
    // Scanning
    // ---------------------------------------------------------------------------------------

    private void scanObject(Object obj, String path) {
        if (obj == null) return;
        if (!scannedObjects.add(obj)) return;

        for (Field field : collectFields(obj.getClass())) {
            registerAnnotatedMember(field, new FieldTarget(obj, field), path);
        }

        for (Method method : findAnnotatedMethods(obj.getClass())) {
            registerAnnotatedMember(method, new MethodTarget(obj, method), path);
        }
    }

    /**
     * All instance fields declared on {@code clazz} or any of its superclasses, so annotations
     * on a base class (e.g. TeleopBase's {@code robot}) are found on concrete subclasses too.
     */
    private static List<Field> collectFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) continue;
                fields.add(field);
            }
        }
        return fields;
    }

    private void registerAnnotatedMember(AccessibleObject member, TelemetryTarget target, String path) {
        // A RobotPosition target is sent as its own packet type, so it must not also be
        // registered as a plain TelemetryData target (that would send it twice).
        if (member.isAnnotationPresent(RobotPosition.class)) {
            String displayName = member.getAnnotation(RobotPosition.class).value();
            registeredTargets.add(new RegisteredTarget(target, path, displayName.isEmpty() ? target.getTelemetryName() : displayName, true));
        } else if (member.isAnnotationPresent(TelemetryData.class)) {
            String displayName = member.getAnnotation(TelemetryData.class).value();
            registeredTargets.add(new RegisteredTarget(target, path, displayName.isEmpty() ? target.getTelemetryName() : displayName, false));
        }
        if (member.isAnnotationPresent(TelemetryObject.class)) {
            resolveTelemetryObjectTarget(target, joinPath(path, target.getTelemetryName()));
        }
    }

    private static String joinPath(String path, String segment) {
        return path.isEmpty() ? segment : path + "." + segment;
    }

    /**
     * Finds no-arg methods carrying a telemetry annotation, including ones declared on an
     * interface or superclass but only ever overridden (without repeating the annotation) by
     * the concrete class. The returned Method objects still dispatch virtually to the concrete
     * override when invoked, so it's safe to invoke them directly on the runtime instance.
     */
    private List<Method> findAnnotatedMethods(Class<?> clazz) {
        List<Method> found = new ArrayList<>();
        Set<String> claimedNames = new HashSet<>();
        collectAnnotatedMethods(clazz, found, claimedNames);
        return found;
    }

    private void collectAnnotatedMethods(Class<?> clazz, List<Method> found, Set<String> claimedNames) {
        if (clazz == null) return;

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getParameterCount() != 0) continue;
            if (claimedNames.contains(method.getName())) continue;

            if (method.isAnnotationPresent(TelemetryData.class)
                    || method.isAnnotationPresent(RobotPosition.class)
                    || method.isAnnotationPresent(TelemetryObject.class)) {
                method.setAccessible(true);
                found.add(method);
                claimedNames.add(method.getName());
            }
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            collectAnnotatedMethods(iface, found, claimedNames);
        }

        collectAnnotatedMethods(clazz.getSuperclass(), found, claimedNames);
    }

    /**
     * Scans a {@code @TelemetryObject} target's current value if it's set, or queues the target
     * to be retried on later loops if it's still null (e.g. hardware wired up after init()).
     */
    private void resolveTelemetryObjectTarget(TelemetryTarget target, String path) {
        try {
            Object nested = target.getValue();
            if (nested != null) {
                scanTelemetryObjectValue(nested, path);
            } else {
                pendingTelemetryObjects.add(new PendingTelemetryObject(target, path));
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A {@code @TelemetryObject} value that's a Collection (e.g. Robot's registered Subsystems)
     * is scanned element-by-element instead of as a single object, since scanning the
     * Collection's own fields (ArrayList internals, etc.) would find nothing useful. Elements
     * are scanned as they exist when this runs; elements added to the collection later aren't
     * picked up retroactively, same as any other already-resolved @TelemetryObject.
     * <p>
     * Each element's path segment is its simple class name (e.g. "robot.subsystems.DriveSubsystem"),
     * with an index appended only when several elements share a class.
     */
    private void scanTelemetryObjectValue(Object value, String path) {
        if (value instanceof Collection<?>) {
            Map<String, Integer> classNameCounts = new HashMap<>();
            for (Object element : (Collection<?>) value) {
                if (element == null) continue;
                String name = element.getClass().getSimpleName();
                classNameCounts.put(name, classNameCounts.getOrDefault(name, 0) + 1);
            }

            Map<String, Integer> seen = new HashMap<>();
            for (Object element : (Collection<?>) value) {
                if (element == null) continue;
                String name = element.getClass().getSimpleName();
                int index = seen.getOrDefault(name, 0);
                seen.put(name, index + 1);
                String segment = classNameCounts.get(name) > 1 ? name + "[" + index + "]" : name;
                scanObject(element, joinPath(path, segment));
            }
        } else {
            scanObject(value, path);
        }
    }

    private void resolvePendingTelemetryObjects() {
        if (pendingTelemetryObjects.isEmpty()) return;

        List<PendingTelemetryObject> stillPending = new ArrayList<>();
        List<PendingTelemetryObject> nowResolved = new ArrayList<>();

        for (PendingTelemetryObject pending : pendingTelemetryObjects) {
            try {
                if (pending.target.getValue() != null) {
                    nowResolved.add(pending);
                } else {
                    stillPending.add(pending);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        pendingTelemetryObjects.clear();
        pendingTelemetryObjects.addAll(stillPending);

        // Scanned after pendingTelemetryObjects is back in a consistent state, since
        // scanning a newly-resolved object can itself queue more pending targets.
        for (PendingTelemetryObject pending : nowResolved) {
            try {
                scanTelemetryObjectValue(pending.target.getValue(), pending.path);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        if (!nowResolved.isEmpty()) {
            rebuildDisplayNames();
        }
    }

    /**
     * Targets display under their bare name unless that name is shared, in which case every
     * target with that name is prefixed with its object path so they can be told apart.
     */
    private void rebuildDisplayNames() {
        Map<String, Integer> baseNameCounts = new HashMap<>();
        for (RegisteredTarget registered : registeredTargets) {
            baseNameCounts.put(registered.baseName, baseNameCounts.getOrDefault(registered.baseName, 0) + 1);
        }

        Map<String, Integer> prefixedCounts = new HashMap<>();
        for (RegisteredTarget registered : registeredTargets) {
            if (baseNameCounts.get(registered.baseName) > 1) {
                registered.displayName = joinPath(registered.path, registered.baseName);
            } else {
                registered.displayName = registered.baseName;
            }
            prefixedCounts.put(registered.displayName, prefixedCounts.getOrDefault(registered.displayName, 0) + 1);
        }

        // Two members on the same object given the same explicit display name would still
        // collide after prefixing; number them so nothing is silently overwritten.
        Map<String, Integer> seen = new HashMap<>();
        for (RegisteredTarget registered : registeredTargets) {
            if (prefixedCounts.get(registered.displayName) > 1) {
                int index = seen.getOrDefault(registered.displayName, 0);
                seen.put(registered.displayName, index + 1);
                registered.displayName = registered.displayName + "[" + index + "]";
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Sending
    // ---------------------------------------------------------------------------------------

    @Override
    public void loop() {
        resolvePendingTelemetryObjects();

        // Reading telemetry means invoking every annotated getter, several of which are live
        // hardware reads; don't pay for that when nobody is watching.
        if (getConnections().isEmpty()) return;

        for (RegisteredTarget registered : registeredTargets) {
            sendPacketForTarget(registered);
        }
    }

    private void sendPacketForTarget(RegisteredTarget registered) {
        TelemetryTarget target = registered.target;

        Object fieldValue;
        try {
            fieldValue = target.getValue();
        } catch (Exception e) {
            // A telemetry getter throwing (e.g. reading an unconfigured sensor) must never take
            // down the OpMode's loop() — report the failure as a value instead of propagating it.
            Throwable cause = (e instanceof InvocationTargetException && e.getCause() != null) ? e.getCause() : e;
            sendErrorPacket(registered, cause);
            return;
        }
        Class<?> fieldType = target.getValueType();

        TelemetryUpdatePacket packet = new TelemetryUpdatePacket();
        packet.telemetryDataName = registered.displayName;

        if (registered.isRobotPosition && fieldType == FieldPosition.class) {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.ROBOT_POSITION;
            if (fieldValue != null) {
                packet.telemetryDataValue = new TelemetryUpdatePacket.TelemetryFieldPosition((FieldPosition) fieldValue);
            }
        } else if (fieldType == Double.class || fieldType == double.class || fieldType == Float.class || fieldType == float.class) {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.DOUBLE;
            packet.telemetryDataValue = fieldValue;
        } else if (fieldType == Integer.class || fieldType == int.class
                || fieldType == Long.class || fieldType == long.class
                || fieldType == Short.class || fieldType == short.class
                || fieldType == Byte.class || fieldType == byte.class) {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.INTEGER;
            packet.telemetryDataValue = fieldValue;
        } else if (fieldType == String.class) {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.STRING;
            packet.telemetryDataValue = fieldValue;
        } else if (fieldType == FieldPosition.class) {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.FIELD_POSITION;
            if (fieldValue != null) {
                packet.telemetryDataValue = new TelemetryUpdatePacket.TelemetryFieldPosition((FieldPosition) fieldValue);
            }
        } else if (Action.class.isAssignableFrom(fieldType)) {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.ACTIONS;
            if (registered.actionTracker == null) {
                registered.actionTracker = new ActionTelemetryTracker();
            }
            packet.telemetryDataValue = registered.actionTracker.snapshot((Action) fieldValue);
        } else {
            packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.STRING;
            if (fieldValue != null) {
                packet.telemetryDataValue = fieldValue.toString();
            }
        }

        this.broadcast(packet.toJson());
    }

    private void sendErrorPacket(RegisteredTarget registered, Throwable cause) {
        TelemetryUpdatePacket packet = new TelemetryUpdatePacket();
        packet.telemetryDataName = registered.displayName;
        packet.telemetryDataType = TelemetryUpdatePacket.TelemetryDataType.STRING;
        packet.telemetryDataValue = "ERROR: " + cause.getClass().getSimpleName()
                + (cause.getMessage() != null ? ": " + cause.getMessage() : "");

        this.broadcast(packet.toJson());
    }

    // ---------------------------------------------------------------------------------------
    // WebSocketServer callbacks
    // ---------------------------------------------------------------------------------------

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        RobotLog.ii(TAG, "Telemetry viewer connected from %s", webSocket.getRemoteSocketAddress());
        try {
            webSocket.send(new TelemetryNewConnectionPacket(this.opMode).toJson());
        } catch (Exception e) {
            // Thrown here, java-websocket would just drop the connection with no trace; log it
            // so a misconfigured OpMode (e.g. missing @TeleOp/@Autonomous) is diagnosable.
            RobotLog.ee(TAG, e, "Failed to build the new-connection packet; closing viewer connection");
            webSocket.close(1011, e.getMessage());
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        RobotLog.ii(TAG, "Telemetry viewer disconnected (code %d, remote=%b): %s", code, remote, reason);
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {}

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        if (webSocket == null) {
            // A null socket means a server-level failure, most commonly failing to bind the port.
            RobotLog.ee(TAG, e, "Telemetry server error (port %d)", PORT);
        } else {
            RobotLog.ee(TAG, e, "Telemetry connection error from %s", webSocket.getRemoteSocketAddress());
        }
    }

    @Override
    public void onStart() {
        RobotLog.ii(TAG, "Telemetry server listening on port %d", PORT);
    }
}
