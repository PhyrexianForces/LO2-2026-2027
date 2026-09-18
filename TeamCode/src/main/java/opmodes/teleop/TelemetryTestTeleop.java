package opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;


import launchpad.telemetry_viewer.TelemetryOpMode;
import launchpad.telemetry_viewer.websocket.RobotPosition;
import launchpad.telemetry_viewer.websocket.TelemetryData;
import launchpad.Constants;
import launchpad.gamepad.Gamepad;
import launchpad.geometry.FieldPosition;
import launchpad.hardware.PinpointModule;
import launchpad.pathing.PinpointLocalizer;

@TeleOp(name = "Telemetry Test TeleOp")
public class TelemetryTestTeleop extends TelemetryOpMode {

    @TelemetryData public float lx = 0.0f;
    @TelemetryData public float ly = 0.0f;
    @TelemetryData public float rx = 0.0f;
    @TelemetryData public float ryNew = 0.0f;

    @RobotPosition
    public FieldPosition currentPosition = new FieldPosition(0, 0, 0);

    private Gamepad gamepad;

    private PinpointLocalizer localizer;

    @Override
    public void init() {
        super.init();

        this.gamepad = new Gamepad(gamepad1);

        localizer = new PinpointLocalizer(hardwareMap.get(PinpointModule.class, "pinpoint"), Constants.PINPOINT_X_OFFSET, PinpointModule.EncoderDirection.FORWARD, Constants.PINPOINT_Y_OFFSET, PinpointModule.EncoderDirection.FORWARD, PinpointModule.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        localizer.setCurrentFieldPosition(new FieldPosition(0, 0, 0));
    }

    @Override
    public void loop() {
        super.loop();
        gamepad.loop();

        lx = gamepad.leftJoystick.getX();
        ly = gamepad.leftJoystick.getY();
        rx = gamepad.rightJoystick.getX();
        ryNew = gamepad.rightJoystick.getY();

        localizer.loop();

        currentPosition = localizer.getCurrentPosition();
    }

    @Override
    public void stop() {
        super.stop();
    }
}
