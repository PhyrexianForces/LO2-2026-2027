package launchpad.actions;

import androidx.annotation.NonNull;

public class CustomAction extends RunOnceAction {

    private final Runnable runnable;

    public CustomAction(@NonNull Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
    }

}
