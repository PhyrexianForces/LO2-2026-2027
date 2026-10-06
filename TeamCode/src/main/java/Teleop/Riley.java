package Teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.CRServo;

//Limelight
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import java.lang.Math;

@TeleOp
public class Riley extends OpMode{
  private DcMotorEx frontLeft, frontRight, backLeft, backRight, intakeMotor;
  //Slowdown variable
  public double slowDown = 0.5;
  private static final boolean useWebcam = true;
  
  @Override
  public void init() {
    frontLeft = hardwareMap.get(DcMotorEx.class, "fl");
    frontRight = hardwareMap.get(DcMotorEx.class, "fr");
    backLeft = hardwareMap.get(DcMotorEx.class, "bl");
    backRight = hardwareMap.get(DcMotorEx.class, "br");
    intakeMotor = hardwareMap.get(DcMotorEx.class, "im");

    //Determines motor direction
    frontLeft.setDirection(DcMotorEx.Direction.REVERSE);
    backLeft.setDirection(DcMotorEx.Direction.REVERSE);
    frontRight.setDirection(DcMotorEx.Direction.FORWARD);
    backRight.setDirection(DcMotorEx.Direction.FORWARD);
    intakeMotor.setDirection(DcMotorEx.Direction.FORWARD);

    //Encodes for motors
    frontLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    frontRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    backLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    backRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    intakeMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
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

    telemetry.addLine(String.valueOf(slowDown));
    telemetry.addLine(String.valueOf(slowDown * flPower * MTPS));
    telemetry.addLine(String.valueOf(flPower * MTPS));

    frontLeft.setVelocity(flPower * MTPS);
    frontRight.setVelocity(frPower * MTPS);
    backLeft.setVelocity(blPower * MTPS);
    backRight.setVelocity(brPower * MTPS);

  }



  @Override
  public void loop() {
    //Determines the inputs of the gamepad
    double y = -gamepad1.left_stick_y;
    double x = gamepad1.left_stick_x;
    double rx = gamepad1.right_stick_x;
    if (gamepad1.a) {
      intakeMotor.setPower(1);
    } else {
      intakeMotor.setPower(0);
    }
    if (gamepad1.x) {
      slowDown = 0.25;
    } else {
      slowDown = 1;
    }
    driveOmni(y, rx, x, slowDown);
   }
}
