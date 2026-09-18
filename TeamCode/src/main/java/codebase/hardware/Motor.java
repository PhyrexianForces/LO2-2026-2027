package codebase.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import java.util.Locale;

import codebase.sensors.MotorEncoder;
import codebase.telemetry_viewer.websocket.TelemetryData;

public class Motor {
    private final DcMotorEx motor;
    private final double ticksPerRotation;
    /**
     * Diameter of the wheel measured in inches.
     */
    private final double wheelDiameter;

    private final MotorEncoder encoder;

    private final boolean velocityConfigured;

    public Motor(DcMotorEx motor, double ticksPerRotation, double wheelDiameter, boolean runUsingEncoder) {
        this.motor = motor;
        this.ticksPerRotation = ticksPerRotation;
        this.wheelDiameter = wheelDiameter;
        this.encoder = new MotorEncoder(motor, ticksPerRotation);
        this.velocityConfigured = true;

        if (runUsingEncoder) {
            this.motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    public Motor(DcMotorEx motor, MotorConfig config) {
        this(motor, config.ticksPerRotation, config.wheelDiameter, true);
    }

    public Motor(DcMotorEx motor) {
        this.motor = motor;
        this.ticksPerRotation = 1;
        this.wheelDiameter = 1;
        this.encoder = new MotorEncoder(motor, 1);
        this.velocityConfigured = false;
    }

    public void setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior behavior) {
        this.motor.setZeroPowerBehavior(behavior);
    }

    /**
     * Sets the velocity in relation to the diameter of the wheel and the ticks per rotation of the motor.
     * @param velocity The desired velocity measured in inches per second.
     */
    public void setVelocity(double velocity) {
        if (!velocityConfigured) {
            throw new IllegalStateException("Cannot set velocity on a Motor that was not configured with velocity parameters. Use the MotorConfig constructor or the full 4-parameter constructor.");
        }

        double ticksPerSecond = velocity * (ticksPerRotation / (wheelDiameter * Math.PI));

        motor.setVelocity(ticksPerSecond);
    }

    public MotorEncoder getMotorEncoder() {
        return this.encoder;
    }

    /**
     * Gets the velocity in relation to the diameter of the wheel and the ticks per rotation of the motor.
     * @return The desired velocity measured in inches per second.
     */
    public double getVelocity() {
        if (!velocityConfigured) {
            throw new IllegalStateException("Cannot get velocity on a Motor that was not configured with velocity parameters. Use the MotorConfig constructor or the full 4-parameter constructor.");
        }

        return motor.getVelocity() / (ticksPerRotation / (wheelDiameter * Math.PI));
    }

    @TelemetryData("Velocity")
    public String getVelocityTelemetry() {
        return velocityConfigured ? String.format(Locale.getDefault(), "%.2f in/s", getVelocity()) : "not configured";
    }

    public DcMotorEx getMotor() {
        return motor;
    }

    public void setPower(double power) {
        motor.setPower(power);
    }

    @TelemetryData("Power")
    public double getPower() {
        return motor.getPower();
    }
}
