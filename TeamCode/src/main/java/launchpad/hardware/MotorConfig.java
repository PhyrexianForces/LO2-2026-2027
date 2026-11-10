package launchpad.hardware;

public class MotorConfig {
    public final double ticksPerRotation;
    public final double wheelDiameter;

    /**
     * @param ticksPerRotation ticks for a full revolution of the initial gear/wheel/object
     * @param wheelDiameter diameter of the final gear/wheel/object being spun
     * @param gearRatio the gear ratio from the final to initial gear/wheel/object
     */
    public MotorConfig(double ticksPerRotation, double wheelDiameter, double gearRatio) {
        this.ticksPerRotation = ticksPerRotation * gearRatio;
        this.wheelDiameter = wheelDiameter;
    }

    /**
     * @param ticksPerRotation ticks for a full revolution of the initial gear/wheel/object
     * @param wheelDiameter diameter of the final gear/wheel/object being spun
     */
    public MotorConfig(double ticksPerRotation, double wheelDiameter) {
        this(ticksPerRotation, wheelDiameter, 1);
    }
}
