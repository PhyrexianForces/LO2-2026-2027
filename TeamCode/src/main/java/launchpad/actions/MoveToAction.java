package launchpad.actions;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.PIDCoefficients;

import java.util.function.Supplier;

import launchpad.Constants;
import launchpad.controllers.Controller;
import launchpad.controllers.PIDController;
import launchpad.controllers.SigmoidController;
import launchpad.geometry.Angles;
import launchpad.geometry.FieldPosition;
import launchpad.geometry.MovementVector;
import launchpad.movement.mecanum.MecanumDriver;
import launchpad.pathing.Localizer;

public class MoveToAction implements Action {
    private final MecanumDriver driver;
    private final Localizer localizer;

    @ActionParameter
    private final FieldPosition destination;

    /**
     * The speed to move horizontally/vertically or some combination of the two in inches/sec
     */
    @ActionParameter
    private final double movementSpeedMultiplier;

    /**
     * The max rotational speed of the robot in radians/sec
     */
    @ActionParameter
    private final double rotationalSpeedMultiplier;

    @ActionParameter
    private final double maxDistanceError;
    @ActionParameter
    private final double maxRotationalError;

    private final Controller xController;
    private final Controller yController;
    private final Controller directionController;

    @ActionParameter
    private PIDCoefficients movementPidCoefficients;

    @ActionParameter
    private PIDCoefficients rotationPidCoefficients;

    public MoveToAction(@NonNull MecanumDriver driver, @NonNull Localizer localizer, @NonNull FieldPosition destination, double movementSpeedMultiplier, double rotationalSpeedMultiplier, double maxDistanceError, double maxRotationalError) {
        this.driver = driver;
        this.localizer = localizer;
        this.destination = destination;
        this.movementSpeedMultiplier = movementSpeedMultiplier;
        this.rotationalSpeedMultiplier = rotationalSpeedMultiplier;
        this.maxDistanceError = maxDistanceError;
        this.maxRotationalError = maxRotationalError;

        this.xController = new PIDController(movementPidCoefficients, () -> localizer.getCurrentPosition().x, () -> destination.x);
        this.yController = new PIDController(movementPidCoefficients, () -> localizer.getCurrentPosition().y, () -> destination.y);
        this.directionController = new PIDController(
                rotationPidCoefficients,
                () -> Angles.angleDifference(localizer.getCurrentPosition().direction, destination.direction)
        );
    }

    @Override
    public void init() {}

    private boolean complete = false;

    @Override
    public void loop() {
        double distanceError = Math.sqrt(Math.pow(localizer.getCurrentPosition().x - destination.x, 2) + Math.pow(localizer.getCurrentPosition().y - destination.y, 2));
        double rotationalError = Angles.angleDifference(localizer.getCurrentPosition().direction, destination.direction);

        if ((distanceError <= maxDistanceError) && (Math.abs(rotationalError) <= maxRotationalError)) {
            driver.stop();
            complete = true;
            return;
        }

        double velocityX = xController.getPower() * movementSpeedMultiplier;
        double velocityY = yController.getPower() * movementSpeedMultiplier;
        double velocityRotational = -directionController.getPower() * rotationalSpeedMultiplier;

        MovementVector vector = new MovementVector(
                velocityX,
                velocityY,
                velocityRotational
        );

        driver.setAbsoluteVelocity(localizer.getCurrentPosition(), vector);
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    public double getErrorY() {
        return yController.getError();
    }

    public double getErrorX() {
        return xController.getError();
    }

    public double getErrorDirection() {
        return directionController.getError();
    }

    public void setMovementPidCoefficients(PIDCoefficients coefficients) {
        movementPidCoefficients = coefficients;
    }

    public void setRotationPidCoefficients(PIDCoefficients coefficients) {
        rotationPidCoefficients = coefficients;
    }
}
