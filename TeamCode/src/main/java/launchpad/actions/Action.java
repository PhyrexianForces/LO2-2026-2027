package launchpad.actions;

import launchpad.Loop;

public interface Action extends Loop {
    void init();

    boolean isComplete();
}
