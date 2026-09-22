package launchpad.actions;

public class SleepAction implements Action {

    @ActionParameter
    private final long ms;

    private long endMs = Long.MAX_VALUE;

    public SleepAction(long ms) {
        this.ms = ms;
    }


    @Override
    public void init() {
        this.endMs = System.currentTimeMillis() + this.ms;
    }

    @Override
    public boolean isComplete() {
        return System.currentTimeMillis() >= this.endMs;
    }

    @Override
    public void loop() {
    }
}
