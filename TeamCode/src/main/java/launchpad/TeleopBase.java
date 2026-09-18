package launchpad;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import launchpad.actions.SimultaneousAction;
import launchpad.gamepad.Gamepad;

public abstract class TeleopBase extends OpMode {
    protected Robot robot;
    protected Gamepad mainGamepad;
    protected Gamepad secondaryGamepad;

    protected SimultaneousAction runningActions;

    @Override
    public void init() {
        robot = new Robot(hardwareMap);
        mainGamepad = new Gamepad(this.gamepad1);
        secondaryGamepad = new Gamepad(this.gamepad2);
        runningActions = new SimultaneousAction();

        registerSubsystems();
    }

    protected abstract void registerSubsystems();

    @Override
    public void loop() {
        mainGamepad.loop();
        secondaryGamepad.loop();
        runningActions.loop();
        robot.loop();
    }

    @Override
    public void stop() {
        robot.stop();
    }
}
