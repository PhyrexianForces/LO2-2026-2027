package Teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

//Limelight
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import java.lang.Math;

@TeleOp
public class LO2Leop extends OpMode{
  private DcMotorEx frontLeft, frontRight, backLeft, backRight, intakeMotor;
  private static final boolean useWebcam = true;

  Limelight3A limelight3A;
  /*
  Pipeline 0: returns blue pollen
  Pipeline 1: returns red pollen
  Pipeline 2: returns AprilTag locations
  */
  
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

    limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
  }

  //Triggers on robot startup
  public void start () {
    limelight3A.pipelineSwitch(0);
    limelight3A.start();
  }

  public void driveOmni(double y, double rx, double x) {
    final double MTPS = 4661;
    //Finds the greatest, positive, not float value among x, y, and rx
    double maxValue = Math.max(Math.abs(x) + Math.abs(y) + Math.abs(rx), 1);

    //Math stuff
    double flPower = (y + x + rx) / maxValue;
    double blPower = (y - x + rx) / maxValue;
    double frPower = (y - x - rx) / maxValue;
    double brPower = (y + x - rx) / maxValue;

    frontLeft.setVelocity(flPower * MTPS);
    frontRight.setVelocity(frPower * MTPS);
    backLeft.setVelocity(blPower * MTPS);
    backRight.setVelocity(brPower * MTPS);

  }



  @Override
  public void loop() {
     LLResult llResult = limelight3A.getLatestResult();
      if(llResult != null && llResult.isValid()){
        telemetry.addData("Tx", llResult.getTx());
        telemetry.addData("Ty", llResult.getTy());
        telemetry.addData("Ta", llResult.getTa());
    }
    else{
          telemetry.addLine("noneFound");
    }
    //Determines the inputs of the gamepad
    double y = -gamepad1.left_stick_y;
    double x = gamepad1.left_stick_x;
    double rx = gamepad1.right_stick_x;
    if (gamepad1.a) {
      intakeMotor.setPower(1);
    } else {
      intakeMotor.setPower(0);
    }
    driveOmni(y, rx, x);
   }
}
