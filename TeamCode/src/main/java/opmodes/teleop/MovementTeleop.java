package opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import launchpad.Constants;
import launchpad.TeleopBase;
import launchpad.geometry.MovementVector;
import launchpad.hardware.Motor;
import launchpad.movement.mecanum.MecanumDriver;

@TeleOp(name="Movement Teleop")
public class MovementTeleop extends TeleopBase {

    private MecanumDriver driver;

    @Override
    public void init() {
        super.init();
        driver = new MecanumDriver(
                new Motor(hardwareMap.get(DcMotorEx.class, "fl")),
                new Motor(hardwareMap.get(DcMotorEx.class, "fr")),
                new Motor(hardwareMap.get(DcMotorEx.class, "bl")),
                new Motor(hardwareMap.get(DcMotorEx.class, "br")),
                Constants.MECANUM_COEFFICIENT_MATRIX
        );


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


        driver.setRelativePower(new MovementVector(
                mainGamepad.rightJoystick.getY(),
                mainGamepad.rightJoystick.getX(),
                mainGamepad.leftJoystick.getX()
        ));
    }

    @Override
    public void stop() {
        super.stop();
        if (driver != null) {
            driver.stop();
        }
    }
}