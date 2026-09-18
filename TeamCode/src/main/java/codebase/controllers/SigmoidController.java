package codebase.controllers;

import java.util.function.Supplier;

import codebase.telemetry_viewer.websocket.TelemetryData;

public class SigmoidController implements Controller {
    @TelemetryData
    private final double power;
    @TelemetryData
    private final double steepness;
    private final Supplier<Double> errorSupplier;

    public SigmoidController(double power, double steepness, Supplier<Double> errorSupplier) {
        this.power = power;
        this.steepness = steepness;
        this.errorSupplier = errorSupplier;
    }

    public SigmoidController(double power, double steepness, Supplier<Double> currentPosition, Supplier<Double> targetPosition) {
        this(power, steepness, () -> targetPosition.get() - currentPosition.get());
    }

    // Named "output" to avoid colliding with the "power" coefficient field's telemetry name.
    // Safe to annotate directly: unlike PIDController, this is a pure computation with no
    // mutated state, so an extra reflective call per telemetry loop has no side effects.
    @Override
    @TelemetryData("output")
    public double getPower() {
        return power * 2 * ((1 / (1 + Math.pow(Math.E, -errorSupplier.get() * steepness))) - 0.5);
    }

    @Override
    public double getError() {
        return errorSupplier.get();
    }
}
