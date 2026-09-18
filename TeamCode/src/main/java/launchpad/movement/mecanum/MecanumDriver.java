package launchpad.movement.mecanum;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import codebase.telemetry_viewer.websocket.TelemetryData;
import codebase.telemetry_viewer.websocket.TelemetryObject;
import launchpad.geometry.FieldPosition;
import launchpad.geometry.MovementVector;
import launchpad.hardware.Motor;

/**
 * Driver class for controlling a mecanum drive system
 * Supports both power-based and velocity-based control modes for relative and absolute movements.
 * Absolute movement refers to field-centric movement. For example, a positive vertical means "up" on the field.
 * Relative movement refers to robot-centric movement. For example, a positive vertical means "forwards" for the robot.
 */
public class MecanumDriver {
    public enum ControlMode { POWER, VELOCITY }
    public enum MovementFrame { ABSOLUTE, RELATIVE }

    @TelemetryObject final Motor fl;
    @TelemetryObject final Motor fr;
    @TelemetryObject final Motor bl;
    @TelemetryObject final Motor br;
    /** Coefficient matrix for mecanum drive adjustments. */
    public final MecanumCoefficientMatrix mecanumDriveCoefficients;
    /** Maximum allowable wheel velocity in inches per second. */
    private final double maxWheelVelocity;

    /** The vector last passed to a set*Power/set*Velocity call, in whichever frame it was given. */
    @TelemetryData
    private MovementVector currentMovementVector = new MovementVector(0, 0, 0);
    @TelemetryData
    private ControlMode currentControlMode = ControlMode.POWER;
    @TelemetryData
    private MovementFrame currentMovementFrame = MovementFrame.RELATIVE;

    /**
     * Constructs a MecanumDriver with the specified motors, coefficient matrix, and maximum wheel velocity.
     *
     * @param fl Front-left motor.
     * @param fr Front-right motor.
     * @param bl Back-left motor.
     * @param br Back-right motor.
     * @param mecanumDriveCoefficients Coefficient matrix for drive adjustments.
     * @param maxWheelVelocity Maximum wheel velocity in inches per second.
     */
    public MecanumDriver(
            @NonNull Motor fl,
            @NonNull Motor fr,
            @NonNull Motor bl,
            @NonNull Motor br,
            @NonNull MecanumCoefficientMatrix mecanumDriveCoefficients,
            double maxWheelVelocity
    ) {
        this.fl = fl;
        this.fr = fr;
        this.bl = bl;
        this.br = br;
        this.mecanumDriveCoefficients = mecanumDriveCoefficients;
        this.maxWheelVelocity = maxWheelVelocity;

        fl.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        fr.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        bl.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        br.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
    }

    /**
     * Constructs a MecanumDriver with the specified motors, coefficient matrix, and maximum wheel velocity.
     *
     * @param fl Front-left motor.
     * @param fr Front-right motor.
     * @param bl Back-left motor.
     * @param br Back-right motor.
     * @param mecanumDriveCoefficients Coefficient matrix for drive adjustments.
     */
    public MecanumDriver(
            @NonNull Motor fl,
            @NonNull Motor fr,
            @NonNull Motor bl,
            @NonNull Motor br,
            @NonNull MecanumCoefficientMatrix mecanumDriveCoefficients
    ) {
        this(fl, fr, bl, br, mecanumDriveCoefficients, -1);
    }

    /**
     * Sets the velocities for all motors.
     *
     * @param fl Velocity for front-left motor in inches per second.
     * @param fr Velocity for front-right motor in inches per second.
     * @param bl Velocity for back-left motor in inches per second.
     * @param br Velocity for back-right motor in inches per second.
     */
    public void setMotorVelocities(double fl, double fr, double bl, double br) {
        this.fl.setVelocity(fl);
        this.fr.setVelocity(fr);
        this.bl.setVelocity(bl);
        this.br.setVelocity(br);
    }

    /**
     * Sets the power levels for all motors.
     *
     * @param fl Power for front-left motor (-1 to 1).
     * @param fr Power for front-right motor (-1 to 1).
     * @param bl Power for back-left motor (-1 to 1).
     * @param br Power for back-right motor (-1 to 1).
     */
    public void setMotorPowers(double fl, double fr, double bl, double br) {
        this.fl.setPower(fl);
        this.fr.setPower(fr);
        this.bl.setPower(bl);
        this.br.setPower(br);
    }

    /**
     * Sets relative power inputs for the drive system.
     * Normalizes the powers to ensure they do not exceed 1.0 in absolute value.
     *
     * @param powerInput MovementVector containing normalized power inputs (-1 to 1).
     */
    public void setRelativePower(MovementVector powerInput) {
        currentMovementVector = powerInput;
        currentControlMode = ControlMode.POWER;
        currentMovementFrame = MovementFrame.RELATIVE;

        applyRelativePower(powerInput);
    }

    private void applyRelativePower(MovementVector powerInput) {
        MecanumCoefficientSet coefficientSet = this.mecanumDriveCoefficients.calculateCoefficientsWithPower(
                powerInput.getVerticalVelocity(),
                powerInput.getHorizontalVelocity(),
                powerInput.getRotationalVelocity()
        ).downScale(1);

        this.setMotorPowers(
                coefficientSet.fl,
                coefficientSet.fr,
                coefficientSet.bl,
                coefficientSet.br
        );
    }

    /**
     * Sets relative velocities for the drive system.
     * Normalizes the velocities to ensure they do not exceed the maximum wheel velocity.
     *
     * @param velocity MovementVector containing velocity inputs (inches/second or radians/second).
     */
    public void setRelativeVelocity(MovementVector velocity) throws IllegalStateException {
        if (maxWheelVelocity == -1) {
            throw new IllegalStateException("Can not set velocity without first setting maxWheelVelocity");
        }

        currentMovementVector = velocity;
        currentControlMode = ControlMode.VELOCITY;
        currentMovementFrame = MovementFrame.RELATIVE;

        applyRelativeVelocity(velocity);
    }

    private void applyRelativeVelocity(MovementVector velocity) {
        MecanumCoefficientSet coefficientSet = this.mecanumDriveCoefficients.calculateCoefficientsWithVelocity(
                velocity.getVerticalVelocity(),
                velocity.getHorizontalVelocity(),
                velocity.getRotationalVelocity()
        ).downScale(maxWheelVelocity);

        this.setMotorVelocities(
                coefficientSet.fl,
                coefficientSet.fr,
                coefficientSet.bl,
                coefficientSet.br
        );
    }

    /**
     * Sets absolute power inputs relative to the field, transforming them to robot-relative powers.
     *
     * @param position Current field position including direction.
     * @param powerInput MovementVector containing absolute power inputs (-1 to 1). (vertical - x, horizontal - y)
     */
    public void setAbsolutePower(FieldPosition position, MovementVector powerInput) {
        currentMovementVector = powerInput;
        currentControlMode = ControlMode.POWER;
        currentMovementFrame = MovementFrame.ABSOLUTE;

        double direction = position.direction;

        double relativeVerticalPower = Math.cos(direction) * powerInput.getVerticalVelocity() + Math.sin(direction) * powerInput.getHorizontalVelocity();
        double relativeHorizontalPower = Math.sin(direction) * powerInput.getVerticalVelocity() - Math.cos(direction) * powerInput.getHorizontalVelocity();

        MovementVector relativePower = new MovementVector(
                relativeVerticalPower,
                relativeHorizontalPower,
                powerInput.getRotationalVelocity()
        );

        applyRelativePower(relativePower);
    }

    /**
     * Sets absolute velocities relative to the field, transforming them to robot-relative velocities.
     *
     * @param position Current field position including direction.
     * @param velocity MovementVector containing absolute velocity inputs (inches/second or radians/second). (vertical - x, horizontal - y)
     */
    public void setAbsoluteVelocity(FieldPosition position, MovementVector velocity) throws IllegalStateException {
        if (maxWheelVelocity == -1) {
            throw new IllegalStateException("Can not set velocity without first setting maxWheelVelocity");
        }

        currentMovementVector = velocity;
        currentControlMode = ControlMode.VELOCITY;
        currentMovementFrame = MovementFrame.ABSOLUTE;

        double direction = position.direction;

        double relativeVerticalVelocity = Math.cos(direction) * velocity.getVerticalVelocity() + Math.sin(direction) * velocity.getHorizontalVelocity();
        double relativeHorizontalVelocity = Math.sin(direction) * velocity.getVerticalVelocity() - Math.cos(direction) * velocity.getHorizontalVelocity();

        MovementVector relativeVelocity = new MovementVector(
                relativeVerticalVelocity,
                relativeHorizontalVelocity,
                velocity.getRotationalVelocity()
        );

        applyRelativeVelocity(relativeVelocity);
    }

    /**
     * Stops all motors by setting powers to zero.
     */
    public void stop() {
        setMotorPowers(0, 0, 0, 0);
    }
}