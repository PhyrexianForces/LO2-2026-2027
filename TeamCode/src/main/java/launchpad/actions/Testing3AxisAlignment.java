package launchpad.actions;

import androidx.annotation.NonNull;

import launchpad.Constants;
import launchpad.controllers.Controller;
import launchpad.controllers.SigmoidController;
import launchpad.geometry.Angles;
import launchpad.geometry.FieldPosition;
import launchpad.geometry.MovementVector;
import launchpad.movement.mecanum.MecanumDriver;
import launchpad.pathing.Localizer;

public class Testing3AxisAlignment implements Action {
    private final MecanumDriver driver;
    private final Localizer localizer;

    private FieldPosition destination;

    /**
     * The speed to move horizontally/vertically or some combination of the two in inches/sec
     */
    private final double movementSpeedMultiplier;

    /**
     * The max rotational speed of the robot in radians/sec
     */
    private final double rotationalSpeedMultiplier;

    private final double maxDistanceError;
    private final double maxRotationalError;

    private static final double MIN_PER_AXIS_VELOCITY = 5;

    public Testing3AxisAlignment(@NonNull MecanumDriver driver, @NonNull Localizer localizer, @NonNull FieldPosition destination, double movementSpeedMultiplier, double rotationalSpeedMultiplier, double maxDistanceError, double maxRotationalError) {
        this.driver = driver;
        this.localizer = localizer;
        this.destination = destination;
        this.movementSpeedMultiplier = movementSpeedMultiplier;
        this.rotationalSpeedMultiplier = rotationalSpeedMultiplier;
        this.maxDistanceError = maxDistanceError;
        this.maxRotationalError = maxRotationalError;
    }

    @Override
    public void init() {}

    private boolean complete = false;

    @Override
    public void loop() {
//        double distanceError = Math.sqrt(Math.pow(localizer.getCurrentPosition().x - destination.x, 2) + Math.pow(localizer.getCurrentPosition().y - destination.y, 2));

        double xError = localizer.getCurrentPosition().x - destination.x;
        double yError = localizer.getCurrentPosition().y - destination.y;

        double rotationalError = Angles.angleDifference(localizer.getCurrentPosition().direction, destination.direction);

        double velocityX = xError;
        double velocityY = yError;
        double velocityRotational = rotationalError;

        MovementVector vector = new MovementVector(
                velocityX,
                velocityY,
                velocityRotational
        );

        if (Math.sqrt(xError * xError + yError * yError) <= maxDistanceError && Math.abs(rotationalError) <= maxRotationalError) {
            driver.stop();
            complete = true;
        } else {
            driver.setAbsoluteVelocity(localizer.getCurrentPosition(), vector);
        }
    }

    @Override
    public boolean isComplete() {
        return complete;
    }
}
