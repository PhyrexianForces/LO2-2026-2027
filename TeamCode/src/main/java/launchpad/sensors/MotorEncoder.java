package launchpad.sensors;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import codebase.telemetry_viewer.websocket.TelemetryData;
import launchpad.geometry.Angles;

public class MotorEncoder implements Encoder {
    private final DcMotorEx encoder;
    private final double ticksPerRotation;

    public MotorEncoder(@NonNull DcMotorEx encoder, double ticksPerRotation) {
        this.encoder = encoder;
        this.ticksPerRotation = ticksPerRotation;
        this.encoder.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    }

    @TelemetryData
    public int getTicks() {
        return this.encoder.getCurrentPosition();
    }

    /**
     * Gets the encoder position in radians.
     * @return The position of the encoder in radians.
     */
    @TelemetryData
    public double getPosition() {
        return Angles.normalizeAngle((getTicks() / ticksPerRotation) * 2.0 * Math.PI);
    }

    /**
     * Converts a position in radians to the equivalent encoder ticks.
     * @param radians the position in radians
     * @return the corresponding encoder tick count
     */
    public int toTicks(double radians) {
        return (int) Math.round((radians / (2.0 * Math.PI)) * ticksPerRotation);
    }

    public void reset() {
        this.encoder.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        this.encoder.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    }
}