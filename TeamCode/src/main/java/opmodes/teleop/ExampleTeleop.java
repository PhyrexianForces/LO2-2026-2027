package opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import launchpad.Constants;
import launchpad.TeleopBase;
import launchpad.actions.SleepAction;
import launchpad.actions.Testing3AxisAlignment;
import launchpad.geometry.FieldPosition;
import launchpad.geometry.MovementVector;
import launchpad.hardware.Motor;
import launchpad.hardware.MotorConfig;
import launchpad.hardware.PinpointModule;
import launchpad.movement.mecanum.MecanumCoefficientMatrix;
import launchpad.movement.mecanum.MecanumCoefficientSet;
import launchpad.movement.mecanum.MecanumDriver;
import launchpad.pathing.Localizer;
import launchpad.pathing.PinpointLocalizer;

@TeleOp(name="Example Teleop")
public class ExampleTeleop extends TeleopBase {

    private MecanumDriver driver;
    private PinpointLocalizer localizer;
    private Motor fl, fr, bl, br;

    @Override
    public void init() {
        super.init();
        // Create a mecanum driver
        fl = new Motor(hardwareMap.get(DcMotorEx.class, "fl"), new MotorConfig(Constants.MotorConstants.GOBILDA_312RPM_5203_2402_0019_TICKS_PER_ROTATION, 4));
        fr = new Motor(hardwareMap.get(DcMotorEx.class, "fr"), new MotorConfig(Constants.MotorConstants.GOBILDA_312RPM_5203_2402_0019_TICKS_PER_ROTATION, 4));
        bl = new Motor(hardwareMap.get(DcMotorEx.class, "bl"), new MotorConfig(Constants.MotorConstants.GOBILDA_312RPM_5203_2402_0019_TICKS_PER_ROTATION, 4));
        br = new Motor(hardwareMap.get(DcMotorEx.class, "br"), new MotorConfig(Constants.MotorConstants.GOBILDA_312RPM_5203_2402_0019_TICKS_PER_ROTATION, 4));

        localizer = new PinpointLocalizer(hardwareMap.get(PinpointModule.class, "pinpoint"), Constants.PINPOINT_X_OFFSET, PinpointModule.EncoderDirection.FORWARD, Constants.PINPOINT_Y_OFFSET, PinpointModule.EncoderDirection.FORWARD, PinpointModule.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        localizer.init();
        driver = new MecanumDriver(fl, fr, bl, br, new MecanumCoefficientMatrix(new MecanumCoefficientSet(-1, 1, -1, 1), Constants.ROTATION_RADIUS_IN), 10);


        // Make a button do something
//        mainGamepad.aButton.onPress(() -> {
//            // add an action to the action thread, causing it to happen immediately
//            runningActions.addAndInitialize(new SleepAction(1000));
//        });

        // Hold to run something continuously (intake, shooter, etc.)
        mainGamepad.rightBumper.whileDown(() -> { /* ... */ });

        // Toggle on/off with each press — good for a claw or a latch
        mainGamepad.xButton.onToggle(open -> { /* ... */ });

        // Analog trigger value, 0.0 to 1.0 — variable speed control
        // intake.setPower(mainGamepad.rightTrigger.getValue());

        // Second driver's controller
        mainGamepad.aButton.onPress(() -> {
            runningActions.addAndInitialize(new Testing3AxisAlignment(driver, localizer, new FieldPosition(0, 5, 0), 1, 1, 3, Math.toRadians(5)));
        });
    }

    @Override
    protected void registerSubsystems() {
        // Register a subsystem, such as a claw, a turret, an intake, or a storage mechanism
        // robot.register(new SubsystemName());
    }

    @Override
    public void loop() {
        // Update localization before actions run so they see this iteration's position, not the previous one's
        localizer.loop();
        // Automatically loops all registered subclasses
        super.loop();

        telemetry.addData("pos", localizer.getCurrentPosition());
        telemetry.update();

        // Drive using controller (Note: in the future, add encoder wires to each drive motor, and change this to driver.setRelativeVelocity())
//         driver.setRelativePower(new MovementVector(mainGamepad.leftJoystick.getY(), mainGamepad.leftJoystick.getX(), mainGamepad.rightJoystick.getX()));
    }
}