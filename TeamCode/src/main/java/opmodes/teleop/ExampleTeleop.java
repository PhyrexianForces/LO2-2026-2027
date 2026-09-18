package opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import launchpad.TeleopBase;
import launchpad.actions.SleepAction;
import launchpad.geometry.MovementVector;
import launchpad.movement.mecanum.MecanumDriver;

@TeleOp(name="Example Teleop")
public class ExampleTeleop extends TeleopBase {

    private MecanumDriver driver;

    @Override
    public void init() {
        super.init();
        // Create a mecanum driver
        // driver = new MecanumDriver(fl, fr, bl, br, new MecanumCoefficientMatrix(new MecanumCoefficientSet(1, 1, 1, 1)));

        // Make a button do something
        mainGamepad.aButton.onPress(() -> {
            // add an action to the action thread, causing it to happen immediately
            runningActions.addAndInitialize(new SleepAction(1000));
        });

        // Hold to run something continuously (intake, shooter, etc.)
        mainGamepad.rightBumper.whileDown(() -> { /* ... */ });

        // Toggle on/off with each press — good for a claw or a latch
        mainGamepad.xButton.onToggle(open -> { /* ... */ });

        // Analog trigger value, 0.0 to 1.0 — variable speed control
        // intake.setPower(mainGamepad.rightTrigger.getValue());

        // Second driver's controller
        secondaryGamepad.dpadUp.onPress(() -> { /* ... */ });
    }

    @Override
    protected void registerSubsystems() {
        // Register a subsystem, such as a claw, a turret, an intake, or a storage mechanism
        // robot.register(new SubsystemName());
    }

    @Override
    public void loop() {
        // Automatically loops all registered subclasses
        super.loop();

        // Loop all localization classes, etc, here


        // Drive using controller (Note: in the future, add encoder wires to each drive motor, and change this to driver.setRelativeVelocity())
        // driver.setRelativePower(new MovementVector(mainGamepad.leftJoystick.getX(), mainGamepad.leftJoystick.getY(), mainGamepad.rightJoystick.getX()));
    }
}