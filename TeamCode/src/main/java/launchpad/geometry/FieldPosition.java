package launchpad.geometry;


import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class FieldPosition extends Point {

    /**
     * The direction of the field position defined as radians counterclockwise from directly to the right of the field
     */
    public double direction;

    public FieldPosition(double x, double y, double direction) {
        super(x, y);
        this.direction = direction;
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("(%f, %f, %f) (in, in, rad)", x, y, direction);
    }
}