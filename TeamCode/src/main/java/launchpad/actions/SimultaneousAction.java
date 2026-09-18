package launchpad.actions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimultaneousAction implements Action {

    private final ArrayList<Action> actions;

    public SimultaneousAction(Action first, Action... rest) {
        this.actions = new ArrayList<>();

        this.actions.add(first);

        actions.addAll(Arrays.asList(rest));
    }

    public SimultaneousAction() {
        this.actions = new ArrayList<>();
    }

    @Override
    public void init() {
        for (Action action : actions) {
            action.init();
        }
    }

    @Override
    public boolean isComplete() {
        for (Action action : actions) {
            if (!action.isComplete()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void loop() {
        ArrayList<Action> toDelete = new ArrayList<>();

        for (Action action : actions) {
            if (!action.isComplete()) {
                action.loop();
            } else {
                toDelete.add(action);
            }
        }

        for (Action action : toDelete) {
            actions.remove(action);
        }
    }

    public void removeActionsOfType(Class<? extends Action> type) {
        ArrayList<Action> toDelete = new ArrayList<>();

        for (Action action : actions) {
            if (type.isInstance(action)) {
                toDelete.add(action);
            }
        }

        for (Action action : toDelete) {
            actions.remove(action);
        }
    }

    public void addAndInitialize(@NonNull Action action, boolean removeOld) {
        if (removeOld) {
            removeActionsOfType(action.getClass());
        }

        actions.add(action);

        action.init();
    }

    public void addAndInitialize(@NonNull Action action) {
        this.addAndInitialize(action, false);
    }

    @Nullable
    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }
}