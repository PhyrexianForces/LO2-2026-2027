package launchpad;

import launchpad.hardware.MotorConfig;
import launchpad.movement.mecanum.MecanumCoefficientMatrix;
import launchpad.movement.mecanum.MecanumCoefficientSet;

public class Constants {
    public static final double MOVEMENT_VELOCITY = 30;
    public static final double MOVEMENT_STEEPNESS = 0.8;
    public static final double ROTATION_VELOCITY = 17.5;
    public static final double ROTATION_STEEPNESS = 0.6;

    public static final double WHEEL_DIAMETER_INCHES = 4.0;
    public static final double ROTATION_RADIUS_IN = 9.9851;
    public static final double MAX_WHEEL_VELOCITY = 100;
    public static final MecanumCoefficientMatrix MECANUM_COEFFICIENT_MATRIX = new MecanumCoefficientMatrix(new MecanumCoefficientSet(-1, -1, -1, 1), ROTATION_RADIUS_IN);

    public static final double PINPOINT_X_OFFSET = -101;
    public static final double PINPOINT_Y_OFFSET = -169;

    public static final double LIMELIGHT_LENS_HEIGHT = 0;

    public static class MotorConstants {
        public static final double GOBILDA_312RPM_5203_2402_0019_TICKS_PER_ROTATION = ((((1+(46.0/17.0))) * (1+(46.0/11.0))) * 28);
        public static final double GOBILDA_1620RPM_5203_2402_0003_TICKS_PER_ROTATION = ((1+(46.0/17.0)) * 28);
        public static final double GOBILDA_6000RPM_5203_2402_0001_TICKS_PER_ROTATION = 28;
    }

    public static final MotorConfig DRIVE_MOTOR_CONFIG = new MotorConfig(
            MotorConstants.GOBILDA_312RPM_5203_2402_0019_TICKS_PER_ROTATION,
            WHEEL_DIAMETER_INCHES
    );
}
