package opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import launchpad.Constants;
import launchpad.TeleopBase;
import launchpad.geometry.MovementVector;
import launchpad.hardware.Motor;
import launchpad.movement.mecanum.MecanumDriver;
import launchpad.telemetry_viewer.websocket.TelemetryObject;

@TeleOp(name="Movement Teleop")
public class MovementTeleop extends TeleopBase {

    @TelemetryObject
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
    }

    @Override
    public void loop() {
        super.loop();

        // Left stick drives (forward/strafe), right stick X turns
        driver.setRelativePower(new MovementVector(
                mainGamepad.leftJoystick.getY(),
                mainGamepad.leftJoystick.getX(),
                mainGamepad.rightJoystick.getX()
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
