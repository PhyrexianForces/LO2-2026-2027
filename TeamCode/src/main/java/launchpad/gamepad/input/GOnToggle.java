package launchpad.gamepad.input;

import java.util.function.Consumer;

public interface GOnToggle<T> {
    T onToggleOn(Runnable runnable);

    T onToggleOff(Runnable runnable);

    T initialToggleState(boolean toggled);

    T onToggle(Consumer<Boolean> stateNew);


}
