package launchpad.sensors;

public interface Encoder {
    int getTicks();

    /**
     * Gets the current position of the encoder in radians.
     * @return position in radians
     */
    double getPosition();

    /**
     * Sets the encoder position to zero.
     */
    void reset();
}