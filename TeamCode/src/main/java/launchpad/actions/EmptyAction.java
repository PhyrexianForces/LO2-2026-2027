package launchpad.actions;

public class EmptyAction implements Action {

    @Override
    public void init() {}

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void loop() {}
}
