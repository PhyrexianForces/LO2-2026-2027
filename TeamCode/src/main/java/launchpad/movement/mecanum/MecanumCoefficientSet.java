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
     * Creates a new MecanumCoefficientSet that has the same ratios between coefficients but is scaled down so that no single coefficient has an absolute value greater than the given max value.
     * @param maxValueAllowed the highest absolute value a single coefficient can have.
     * @return the scaled MecanumCoefficientSet.
     */
    public MecanumCoefficientSet downScale(double maxValueAllowed) {
        double highestAbs = Math.max(Math.max(Math.abs(this.fl), Math.abs(this.fr)),
                Math.max(Math.abs(this.bl), Math.abs(this.br)));
        double scale = (highestAbs > maxValueAllowed) ? (maxValueAllowed / highestAbs) : 1.0;

        return new MecanumCoefficientSet(
                this.fl * scale,
                this.fr * scale,
                this.bl * scale,
                this.br * scale
        );
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("(%f, %f, %f, %f)", fl, fr, bl, br);
    }
}
