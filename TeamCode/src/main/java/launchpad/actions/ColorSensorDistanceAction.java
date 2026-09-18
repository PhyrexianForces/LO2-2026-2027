package launchpad.actions;

import androidx.annotation.NonNull;

import launchpad.sensors.ColorSensor;

public class ColorSensorDistanceAction implements Action {

    private final ColorSensor colorSensor;
    private final double triggerDistance;
    private final DistanceMode mode;

    public ColorSensorDistanceAction(@NonNull ColorSensor colorSensor, double triggerDistance, @NonNull DistanceMode mode) {
        this.colorSensor = colorSensor;
        this.triggerDistance = triggerDistance;
        this.mode = mode;
    }

    public enum DistanceMode {
        LESS_THAN_EQUAL_TO,
        GREATER_THAN_EQUAL_TO
    }

    @Override
    public void init() {}

    @Override
    public boolean isComplete() {
        double currentDistance = colorSensor.getDistance();
        return mode == DistanceMode.LESS_THAN_EQUAL_TO ? (currentDistance <= triggerDistance) : (currentDistance >= triggerDistance);
    }

    @Override
    public void loop() {}
}
