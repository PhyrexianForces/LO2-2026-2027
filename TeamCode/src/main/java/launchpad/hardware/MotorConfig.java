package launchpad.hardware;

public class MotorConfig {
    public final double ticksPerRotation;
    public final double wheelDiameter;

    public MotorConfig(double ticksPerRotation, double wheelDiameter) {
        this.ticksPerRotation = ticksPerRotation;
        this.wheelDiameter = wheelDiameter;
    }
}
