package launchpad.actions;

import java.util.function.Supplier;

public class WaitUntilAction implements Action {

    private final Supplier<Boolean> condition;

    public WaitUntilAction(Supplier<Boolean> condition) {
        this.condition = condition;
    }

    @Override
    public void init() {}

    @Override
    public boolean isComplete() {
        return condition.get();
    }

    @Override
    public void loop() {}
}
