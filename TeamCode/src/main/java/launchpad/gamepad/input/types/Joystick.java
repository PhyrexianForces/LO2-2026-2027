package launchpad.gamepad.input.types;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import launchpad.gamepad.Gamepad;
import launchpad.gamepad.input.GAnalog;
import launchpad.gamepad.input.GInput;

public class Joystick implements GInput, GAnalog<Joystick> {

    private final Gamepad gamepad;
    private final Supplier<Float> xSupplier;
    private final Supplier<Float> ySupplier;
    private BiConsumer<Float, Float> onMove;
    private float lastX;
    private float lastY;

    public Joystick(Gamepad gamepad, Supplier<Float> xSupplier, Supplier<Float> ySupplier) {
        this.gamepad = gamepad;
        this.xSupplier = xSupplier;
        this.ySupplier = ySupplier;
    }

    @Override
    public void loop() {
        float currentX = this.xSupplier.get();
        float currentY = this.ySupplier.get();

        if (currentX != lastX || currentY != lastY) {
            this.lastX = currentX;
            this.lastY = currentY;

            if (this.onMove != null) {
                this.onMove.accept(currentX, currentY);
            }
        }
    }

    @Override
    public Joystick onMove(BiConsumer<Float, Float> onMove) {
        this.onMove = onMove;
        return this;
    }

    @Override
    public Gamepad getGamepad() {
        return this.gamepad;
    }

    public float getX() {
        return xSupplier.get();
    }

    public float getY() {
        return ySupplier.get();
    }
}
