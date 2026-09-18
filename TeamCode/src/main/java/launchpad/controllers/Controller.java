package launchpad.controllers;

import codebase.telemetry_viewer.websocket.TelemetryData;

public interface Controller {
    double getPower();

    // Not on getPower(): PIDController's implementation is stateful (mutates integral/derivative
    // terms based on elapsed time), so an extra reflective call per telemetry loop would corrupt it.
    @TelemetryData
    double getError();
}
