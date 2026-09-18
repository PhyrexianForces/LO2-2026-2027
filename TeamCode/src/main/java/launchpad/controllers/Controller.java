package launchpad.controllers;

import launchpad.telemetry_viewer.websocket.TelemetryData;

public interface Controller {
    double getPower();

    @TelemetryData
    double getError();
}
