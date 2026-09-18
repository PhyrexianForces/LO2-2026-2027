package launchpad;

import launchpad.actions.SimultaneousAction;
import launchpad.gamepad.Gamepad;
import launchpad.telemetry_viewer.TelemetryOpMode;
import launchpad.telemetry_viewer.websocket.TelemetryObject;

public abstract class TeleopBase extends TelemetryOpMode {
    @TelemetryObject
    protected Robot robot;
    protected Gamepad mainGamepad;
    protected Gamepad secondaryGamepad;

    protected SimultaneousAction runningActions;

    @Override
    public void init() {
        super.init();

        robot = new Robot(hardwareMap);
        mainGamepad = new Gamepad(this.gamepad1);
        secondaryGamepad = new Gamepad(this.gamepad2);
        runningActions = new SimultaneousAction();

        registerSubsystems();
    }

    protected abstract void registerSubsystems();

    @Override
    public void loop() {
        super.loop();

        mainGamepad.loop();
        secondaryGamepad.loop();
        runningActions.loop();
        robot.loop();
    }

    @Override
    public void stop() {
        super.stop();

        robot.stop();
    }
}
