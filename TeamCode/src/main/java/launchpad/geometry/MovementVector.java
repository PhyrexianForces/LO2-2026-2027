package launchpad.geometry;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;

public class MovementVector implements Serializable {
    /**
     * Vertical component, representing either linear velocity (forward/backward) in inches per second
     * or normalized power (-1 to 1) for forward/backward motion. Positive values indicate forward motion.
     */
    protected double verticalVelocity;

    /**
     * Horizontal component, representing either linear velocity (left/right) in inches per second
     * or normalized power (-1 to 1) for left/right motion. Positive values indicate rightward motion.
     */
    protected double horizontalVelocity;

    /**
     * Rotational component, representing either angular velocity in radians per second
     * or normalized power (-1 to 1) for rotation. Positive values indicate clockwise motion.
     */
    protected double rotationalVelocity;

    /**
     * Constructs a MovementVector with specified vertical, horizontal, and rotational components.
     * @param verticalVelocity Linear velocity (inches/second) or power (-1 to 1) for forward/backward motion.
     * @param horizontalVelocity Linear velocity (inches/second) or power (-1 to 1) for left/right motion.
     * @param rotationalVelocity Angular velocity (radians/second) or power (-1 to 1) for rotation.
     */
    public MovementVector(double verticalVelocity, double horizontalVelocity, double rotationalVelocity) {
        this.verticalVelocity = verticalVelocity;
        this.horizontalVelocity = horizontalVelocity;
        this.rotationalVelocity = rotationalVelocity;
    }

    /**
     * Gets the vertical component (velocity in inches/second or power from -1 to 1).
     * @return Vertical component.
     */
    public double getVerticalVelocity() {
        return verticalVelocity;
    }

    /**
     * Gets the horizontal component (velocity in inches/second or power from -1 to 1).
     * @return Horizontal component.
     */
    public double getHorizontalVelocity() {
        return horizontalVelocity;
    }

    /**
     * Gets the rotational component (angular velocity in radians/second or power from -1 to 1).
     * @return Rotational component.
     */
    public double getRotationalVelocity() {
        return rotationalVelocity;
    }

    /**
     * Sets the vertical component (velocity in inches/second or power from -1 to 1).
     * @param verticalVelocity Vertical component.
     */
    public void setVerticalVelocity(double verticalVelocity) {
        this.verticalVelocity = verticalVelocity;
    }

    /**
     * Sets the horizontal component (velocity in inches/second or power from -1 to 1).
     * @param horizontalVelocity Horizontal component.
     */
    public void setHorizontalVelocity(double horizontalVelocity) {
        this.horizontalVelocity = horizontalVelocity;
    }

    /**
     * Sets the rotational component (angular velocity in radians/second or power from -1 to 1).
     * @param rotationalVelocity Rotational component.
     */
    public void setRotationalVelocity(double rotationalVelocity) {
        this.rotationalVelocity = rotationalVelocity;
    }

    /**
     * Adds another MovementVector to this one, combining their components.
     * @param movementVector The vector to add (velocities or powers).
     * @return A new MovementVector with summed components.
     */
    public MovementVector add(@NonNull MovementVector movementVector) {
        return new MovementVector(
                this.verticalVelocity + movementVector.verticalVelocity,
                this.horizontalVelocity + movementVector.horizontalVelocity,
                this.rotationalVelocity + movementVector.rotationalVelocity
        );
    }

    /**
     * Subtracts another MovementVector from this one.
     * @param movementVector The vector to subtract (velocities or powers).
     * @return A new MovementVector with subtracted components.
     */
    public MovementVector subtract(@NonNull MovementVector movementVector) {
        return new MovementVector(
                this.verticalVelocity - movementVector.verticalVelocity,
                this.horizontalVelocity - movementVector.horizontalVelocity,
                this.rotationalVelocity - movementVector.rotationalVelocity
        );
    }

    /**
     * Scales this MovementVector by a scalar magnitude.
     * @param magnitude The scaling factor.
     * @return A new MovementVector with scaled components.
     */
    public MovementVector scalarMultiply(double magnitude) {
        return new MovementVector(
                this.verticalVelocity * magnitude,
                this.horizontalVelocity * magnitude,
                this.rotationalVelocity * magnitude
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof MovementVector)) {
            return false;
        }

        MovementVector that = (MovementVector) o;

        double epsilon = 1e-9;
        return Math.abs(this.verticalVelocity - that.verticalVelocity) < epsilon
                && Math.abs(this.horizontalVelocity - that.horizontalVelocity) < epsilon
                && Math.abs(this.rotationalVelocity - that.rotationalVelocity) < epsilon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(verticalVelocity, horizontalVelocity, rotationalVelocity);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%f; %f; %f}", verticalVelocity, horizontalVelocity, rotationalVelocity);
    }
}