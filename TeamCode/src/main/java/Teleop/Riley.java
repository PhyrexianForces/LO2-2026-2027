package Teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.CRServo;

import java.lang.Math;

@TeleOp
public class Riley extends OpMode{
  private DcMotorEx frontLeft, frontRight, backLeft, backRight;
  //Slowdown variable
  public double slowDown = 0.5;
  
  @Override
  public void init() {
    frontLeft = hardwareMap.get(DcMotorEx.class, "fl");
    frontRight = hardwareMap.get(DcMotorEx.class, "fr");
    backLeft = hardwareMap.get(DcMotorEx.class, "bl");
    backRight = hardwareMap.get(DcMotorEx.class, "br");

    //Determines motor direction
    frontLeft.setDirection(DcMotorEx.Direction.REVERSE);
    backLeft.setDirection(DcMotorEx.Direction.REVERSE);
    frontRight.setDirection(DcMotorEx.Direction.FORWARD);
    frontRight.setDirection(DcMotorEx.Direction.FORWARD);

    //Encodes for motors
    frontLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    frontRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    backLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    backRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
  }

  public void driveOmni(double y, double rx, double x, double slowDown) {
    final double MTPS = 4661;
    //Finds the greatest, positive, not float value among x, y, and rx
    double maxValue = Math.max(Math.abs(x) + Math.abs(y) + Math.abs(rx), 1);

    //Math stuff
    double flPower = (y + x + rx) / maxValue;
    double blPower = (y - x + rx) / maxValue;
    double frPower = (y - x - rx) / maxValue;
    double brPower = (y + x - rx) / maxValue;

    frontLeft.setVelocity(slowDown/2 * flPower * MTPS);
    frontRight.setVelocity(slowDown/2 * frPower * MTPS);
    backLeft.setVelocity(slowDown/2 * blPower * MTPS);
    backRight.setVelocity(slowDown/2 * brPower * MTPS);

  }



  @Override
  public void loop() {
    //Determines the inputs of the gamepad
    double y = -gamepad1.left_stick_y;
    double x = gamepad1.left_stick_x;
    double rx = gamepad1.right_stick_x;
    if (gamepad1.x) {
      slowDown = 1;
      telemetry.addLine(String.valueOf(slowDown));
    } else {
      slowDown = 2;
      telemetry.addLine(String.valueOf(slowDown));
    }
    driveOmni(y, rx, x, slowDown);
   }
}
