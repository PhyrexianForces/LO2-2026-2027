package launchpad.actions;

public abstract class RunOnceAction implements Action {

    private boolean hasRun = false;

    @Override
    public void loop() {
        if (!hasRun) {
            run();
            hasRun = true;
        }
    }

    @Override
    public boolean isComplete() {
        return hasRun;
    }

    public void run() {}

    public void init() {}
}
