package launchpad.movement.mecanum;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class MecanumCoefficientSet {
    public double fl;
    public double fr;
    public double bl;
    public double br;

    public MecanumCoefficientSet(
            double fl,
            double fr,
            double bl,
            double br
    ) {
        this.fl = fl;
        this.fr = fr;
        this.bl = bl;
        this.br = br;
    }

    /**
     * Creates a new MecanumCoefficientSet with every coefficient multiplied by the given factor.
     * <p>
     * Scaling is uniform, so the ratios between the four coefficients are unchanged. Because those
     * ratios are what determine the robot's direction of travel, scaling changes only how fast the
     * robot moves, never which way it goes.
     *
     * @param factor the value to multiply every coefficient by.
     * @return the scaled MecanumCoefficientSet.
     */
    public MecanumCoefficientSet scale(double factor) {
        return new MecanumCoefficientSet(
                this.fl * factor,
                this.fr * factor,
                this.bl * factor,
                this.br * factor
        );
    }

    /**
     * Creates a new MecanumCoefficientSet that has the same ratios between coefficients but is scaled down so that no single coefficient has an absolute value greater than the given max value.
     * @param maxValueAllowed the highest absolute value a single coefficient can have.
     * @return the scaled MecanumCoefficientSet.
     */
    public MecanumCoefficientSet downScale(double maxValueAllowed) {
        double highestAbs = getHighestWheelMagnitude();
        double scaleFactor = (highestAbs > maxValueAllowed) ? (maxValueAllowed / highestAbs) : 1.0;

        return scale(scaleFactor);
    }

    /**
     * Creates a new MecanumCoefficientSet that has the same ratios between coefficients but is scaled up so that the largest coefficient has an absolute value of at least the given min value.
     * <p>
     * The floor is applied to the largest wheel, not to each wheel individually. Wheels that are
     * turning slower than the minimum are left below it on purpose: they have less distance to
     * cover, and forcing every wheel to the same floor would break the ratios that keep all three
     * axes of movement proportional.
     * <p>
     * A set whose coefficients are all zero is returned unchanged. A zero command means "hold
     * still", and must never be scaled up into movement.
     *
     * @param minValueAllowed the lowest absolute value the largest coefficient may have.
     * @return the scaled MecanumCoefficientSet.
     */
    public MecanumCoefficientSet upScale(double minValueAllowed) {
        double highestAbs = getHighestWheelMagnitude();
        double scaleFactor = (highestAbs > 0 && highestAbs < minValueAllowed) ? (minValueAllowed / highestAbs) : 1.0;

        return scale(scaleFactor);
    }

    public double getHighestWheelMagnitude() {
        return Math.max(Math.max(Math.abs(fl), Math.abs(fr)),
                Math.max(Math.abs(bl), Math.abs(br)));
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("(%f, %f, %f, %f)", fl, fr, bl, br);
    }
}
