package launchpad.gamepad.input;

import java.util.function.BiConsumer;

public interface GAnalog<T> {

    T onMove(BiConsumer<Float, Float> onMove);

}