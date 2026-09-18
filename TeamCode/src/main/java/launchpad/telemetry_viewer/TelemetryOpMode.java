package launchpad.telemetry_viewer;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import launchpad.telemetry_viewer.websocket.TelemetryServer;

public abstract class TelemetryOpMode extends OpMode {
    private static TelemetryServer telemetryServer;

    @Override
    public void init() {
        if (telemetryServer != null) {
            try { telemetryServer.stop(); } catch (Exception ignored) {}
            telemetryServer = null;
        }
        telemetryServer = new TelemetryServer(this);
        telemetryServer.start();
    }

    @Override
    public void loop() {
        TelemetryOpMode.telemetryServer.loop();
    }

    @Override
    public void stop() {
        try { telemetryServer.stop(); } catch (Exception ignored) {}
    }
}
