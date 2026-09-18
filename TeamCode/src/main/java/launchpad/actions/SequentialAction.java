package launchpad.actions;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SequentialAction implements Action {

    private final List<Action> actions;
    private int currentIndex = 0;

    public SequentialAction(Action first, Action... rest) {
        this.actions = new ArrayList<>();
        this.actions.add(first);
        this.actions.addAll(Arrays.asList(rest));
    }

    public SequentialAction(Action[] actions) {
        this.actions = new ArrayList<>(Arrays.asList(actions));
    }

    @Override
    public void init() {
        if (!actions.isEmpty()) {
            actions.get(0).init();
        }
    }

    @Override
    public boolean isComplete() {
        return currentIndex >= actions.size();
    }

    @Override
    public void loop() {
        if (isComplete()) {
            return;
        }

        if (actions.get(currentIndex).isComplete()) {
            currentIndex++;

            if (!isComplete()) {
                actions.get(currentIndex).init();
            }
        }

        if (isComplete()) {
            return;
        }

        actions.get(currentIndex).loop();
    }

    @Nullable
    public Action getRunningAction() {
        if (isComplete()) {
            return null;
        }
        return actions.get(currentIndex);
    }

    @Nullable
    public List<Action> getActions() {
        if (isComplete()) {
            return null;
        }
        return actions;
    }
}
