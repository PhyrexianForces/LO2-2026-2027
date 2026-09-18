package launchpad.gamepad.input.types;

import java.util.function.Consumer;
import java.util.function.Supplier;

import launchpad.gamepad.Gamepad;
import launchpad.gamepad.input.GInput;
import launchpad.gamepad.input.GIsPressed;
import launchpad.gamepad.input.GIsToggled;
import launchpad.gamepad.input.GOnPress;
import launchpad.gamepad.input.GOnRelease;
import launchpad.gamepad.input.GOnToggle;
import launchpad.gamepad.input.GWhileDown;


public class Trigger implements GInput, GIsPressed, GIsToggled, GWhileDown<Trigger>, GOnToggle<Trigger>, GOnPress<Trigger>, GOnRelease<Trigger> {
    /**
     * The minimum value for the trigger to be considered down
     */
    private static final float PRESS_THRESHOLD = 0.5f;
    private final Supplier<Float> valueSupplier;
    private final Gamepad gamepad;
    private Runnable onPress, onRelease, whileDown, onToggleOn, onToggleOff;
    private Consumer<Boolean> onToggle;
    private boolean wasDownLast;
    private boolean toggleState;

    public Trigger(Gamepad gamepad, Supplier<Float> valueSupplier) {
        this.valueSupplier = valueSupplier;
        this.gamepad = gamepad;
    }

    @Override
    public void loop() {
        boolean isPressed = isPressed();

        if (isPressed && !this.wasDownLast) {
            if (this.onPress != null) {
                onPress.run();
            }

            // Toggle
            this.toggleState = !this.toggleState;

            if (this.onToggle != null) {
                this.onToggle.accept(this.toggleState);
            }

            if (this.onToggleOff != null && !this.toggleState) {
                this.onToggleOff.run();
            }

            if (this.onToggleOn != null && this.toggleState) {
                this.onToggleOn.run();
            }
        }
        if (this.onRelease != null && !isPressed && this.wasDownLast) {
            onRelease.run();
        }
        if (this.whileDown != null && isPressed) {
            whileDown.run();
        }

        this.wasDownLast = isPressed;
    }

    public double getValue() {
        return this.valueSupplier.get();
    }

    @Override
    public Gamepad getGamepad() {
        return this.gamepad;
    }

    @Override
    public boolean isPressed() {
        return this.valueSupplier.get() >= PRESS_THRESHOLD;
    }

    @Override
    public boolean isToggled() {
        return this.toggleState;
    }

    @Override
    public Trigger onPress(Runnable onPress) {
        this.onPress = onPress;
        return this;
    }

    @Override
    public Trigger onRelease(Runnable onRelease) {
        this.onRelease = onRelease;
        return this;
    }

    @Override
    public Trigger whileDown(Runnable whileDown) {
        this.whileDown = whileDown;
        return this;
    }

    @Override
    public Trigger onToggleOn(Runnable onToggleOn) {
        this.onToggleOn = onToggleOn;
        return this;
    }

    @Override
    public Trigger onToggleOff(Runnable onToggleOff) {
        this.onToggleOff = onToggleOff;
        return this;
    }

    @Override
    public Trigger initialToggleState(boolean toggled) {
        this.toggleState = toggled;
        return this;
    }

    @Override
    public Trigger onToggle(Consumer<Boolean> onToggle) {
        this.onToggle = onToggle;
        return this;
    }
}
