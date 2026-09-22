package launchpad.sensors;

import androidx.annotation.NonNull;

import com.qualcomm.hardware.rev.RevColorSensorV3;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.Locale;

import launchpad.telemetry_viewer.websocket.TelemetryData;

public class ColorSensor {
    private final RevColorSensorV3 sensor;

    public ColorSensor(@NonNull RevColorSensorV3 sensor) {
        this.sensor = sensor;
    }

    /**
     * Get the color sensed, normalized to 0-1
     * @return the color
     */
    public Color getColor() {
        double magnitude = Math.sqrt(Math.pow(sensor.red(), 2) + Math.pow(sensor.green(), 2) + Math.pow(sensor.blue(), 2));

        if (magnitude == 0) {
            return new Color(0, 0, 0);
        }

        return new Color(sensor.red() / magnitude, sensor.green() / magnitude, sensor.blue() / magnitude);
    }

    @TelemetryData
    public String getColorTelemetry() {
        return getColor().toString();
    }

    /**
     * Get the distance to the object sensed
     * @return the distance in inches
     */
    public double getDistance() {
        return sensor.getDistance(DistanceUnit.INCH);
    }

    public static class Color {
        public double red;
        public double green;
        public double blue;

        /**
         * @param red - 0-1
         * @param green - 0-1
         * @param blue - 0-1
         */
        public Color(double red, double green, double blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "R:%.2f G:%.2f B:%.2f", red, green, blue);
        }
    }
}
