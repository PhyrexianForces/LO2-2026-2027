package launchpad.movement.mecanum;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import launchpad.telemetry_viewer.websocket.TelemetryData;
import launchpad.telemetry_viewer.websocket.TelemetryObject;
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
    /**
     * Minimum velocity in inches per second for the fastest wheel of a non-zero velocity command,
     * or 0 to disable the floor.
     * <p>
     * Purely proportional control commands a velocity that shrinks with the remaining error, so it
     * approaches the target asymptotically and eventually falls below the velocity needed to
     * overcome static friction, leaving the robot stalled short of its target. Holding the fastest
     * wheel at a floor keeps the robot moving until it is actually there.
     * <p>
     * <b>The caller must stop the robot once it is within tolerance</b>, otherwise the floor will
     * drive it past the target, back, and past again forever. For that stop to be reachable,
     * {@code minWheelVelocity * loopTimeSeconds} must be comfortably smaller than the position
     * tolerance, or the robot can step straight over the tolerance band in a single loop and never
     * register as arrived. At a 20ms loop and 5 in/s that step is 0.1 inches, so tolerances need to
     * be meaningfully larger than that. Raising this value raises the smallest tolerance you can
     * reliably hit.
     */
    private final double minWheelVelocity;

    /** The vector last passed to a set*Power/set*Velocity call, in whichever frame it was given. */
    @TelemetryData
    private MovementVector currentMovementVector = new MovementVector(0, 0, 0);
    @TelemetryData
    private ControlMode currentControlMode = ControlMode.POWER;
    @TelemetryData
    private MovementFrame currentMovementFrame = MovementFrame.RELATIVE;

    /**
     * Constructs a MecanumDriver with the specified motors, coefficient matrix, and wheel velocity limits.
     *
     * @param fl Front-left motor.
     * @param fr Front-right motor.
     * @param bl Back-left motor.
     * @param br Back-right motor.
     * @param mecanumDriveCoefficients Coefficient matrix for drive adjustments.
     * @param maxWheelVelocity Maximum wheel velocity in inches per second.
     * @param minWheelVelocity Minimum velocity in inches per second for the fastest wheel of a non-zero
     *                         command, or 0 to disable. See {@link #minWheelVelocity} for the loop-time
     *                         constraint this places on your position tolerance.
     */
    public MecanumDriver(
            @NonNull Motor fl,
            @NonNull Motor fr,
            @NonNull Motor bl,
            @NonNull Motor br,
            @NonNull MecanumCoefficientMatrix mecanumDriveCoefficients,
            double maxWheelVelocity,
            double minWheelVelocity
    ) {
        this.fl = fl;
        this.fr = fr;
        this.bl = bl;
        this.br = br;
        this.mecanumDriveCoefficients = mecanumDriveCoefficients;
        this.maxWheelVelocity = maxWheelVelocity;
        this.minWheelVelocity = minWheelVelocity;

        fl.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        fr.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        bl.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        br.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
    }

    /**
     * Constructs a MecanumDriver with no minimum wheel velocity.
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
        this(fl, fr, bl, br, mecanumDriveCoefficients, maxWheelVelocity, 0);
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
     * Calculates the per-wheel velocities a movement command works out to, before any limiting.
     *
     * @param velocity MovementVector containing velocity inputs (inches/second or radians/second).
     * @return the unlimited wheel velocities in inches per second.
     */
    private MecanumCoefficientSet coefficientsFor(MovementVector velocity) {
        return this.mecanumDriveCoefficients.calculateCoefficientsWithVelocity(
                velocity.getVerticalVelocity(),
                velocity.getHorizontalVelocity(),
                velocity.getRotationalVelocity()
        );
    }

    /**
     * Calculates how far a movement command can be scaled up before the fastest wheel reaches
     * {@code maxWheelVelocity}.
     * <p>
     * How fast the robot can travel depends on which way it is going, because all three axes of a
     * movement command draw on the same four wheels. Driving straight forwards uses one wheel's
     * worth of velocity per wheel, but driving at 45 degrees stacks the vertical and horizontal
     * components on the same wheels and so tops out at about 71% of that speed. Adding rotation
     * takes a further share.
     * <p>
     * The returned value is a multiplier for the vector that was passed in, not a speed. If that
     * vector is a unit vector, the two are the same and the result is the fastest that direction can
     * be driven in inches per second. Requesting {@code min(desiredSpeed, maxScaleFor(direction))}
     * keeps a command inside the achievable range, so it is followed exactly rather than being
     * silently cut down by {@link #setRelativeVelocity}.
     *
     * @param direction MovementVector giving the direction of travel to measure.
     * @return the largest factor the given vector can be multiplied by, or positive infinity for a zero vector.
     */
    public double maxScaleFor(MovementVector direction) throws IllegalStateException {
        if (maxWheelVelocity == -1) {
            throw new IllegalStateException("Can not find the maximum scale without first setting maxWheelVelocity");
        }

        double highestWheelMagnitude = coefficientsFor(direction).getHighestWheelMagnitude();

        return highestWheelMagnitude == 0 ? Double.POSITIVE_INFINITY : maxWheelVelocity / highestWheelMagnitude;
    }

    /**
     * Sets relative velocities for the drive system.
     * <p>
     * The command is limited to {@code maxWheelVelocity} and, if one is set, lifted to
     * {@code minWheelVelocity}. Both limits scale all four wheels by the same factor, so the
     * vertical, horizontal, and rotational parts of the command keep their ratios and the robot
     * still travels in the requested direction. Only its speed changes, which means each axis
     * always covers the same percentage of its own movement as the other two.
     *
     * @param velocity MovementVector containing velocity inputs (inches/second or radians/second).
     * @return the factor the command was scaled by: 1 if it was followed exactly, below 1 if it was
     *         too fast for the wheels, above 1 if the minimum velocity lifted it.
     */
    public double setRelativeVelocity(MovementVector velocity) throws IllegalStateException {
        if (maxWheelVelocity == -1) {
            throw new IllegalStateException("Can not set velocity without first setting maxWheelVelocity");
        }

        currentMovementVector = velocity;
        currentControlMode = ControlMode.VELOCITY;
        currentMovementFrame = MovementFrame.RELATIVE;

        return applyRelativeVelocity(velocity);
    }

    /**
     * Applies a robot-relative velocity command without recording it as the current movement
     * vector, so callers that transform a command first (e.g. field-relative) can report the
     * original.
     *
     * @return the factor the command was scaled by, as described by {@link #setRelativeVelocity}.
     */
    private double applyRelativeVelocity(MovementVector velocity) {
        MecanumCoefficientSet requested = coefficientsFor(velocity);
        MecanumCoefficientSet commanded = requested
                .upScale(minWheelVelocity)
                .downScale(maxWheelVelocity);

        this.setMotorVelocities(
                commanded.fl,
                commanded.fr,
                commanded.bl,
                commanded.br
        );

        double requestedMagnitude = requested.getHighestWheelMagnitude();

        return requestedMagnitude == 0 ? 1 : commanded.getHighestWheelMagnitude() / requestedMagnitude;
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
     * @return the factor the command was scaled by, as described by {@link #setRelativeVelocity}.
     */
    public double setAbsoluteVelocity(FieldPosition position, MovementVector velocity) throws IllegalStateException {
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

        return applyRelativeVelocity(relativeVelocity);
    }

    /**
     * Get the max speed
     * @param direction
     * @return
     */
    public double maxFeasibleSpeed(MovementVector direction) {
        MecanumCoefficientSet coefficients = mecanumDriveCoefficients.calculateCoefficientsWithVelocity(
                direction.getVerticalVelocity(),
                direction.getHorizontalVelocity(),
                direction.getRotationalVelocity()
        );

        return maxWheelVelocity / coefficients.getHighestWheelMagnitude();
    }

    /**
     * Stops all motors by setting powers to zero.
     */
    public void stop() {
        setMotorPowers(0, 0, 0, 0);
    }
}