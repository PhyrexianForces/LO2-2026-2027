package Teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

//Limelight
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

//Aiming

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@TeleOp
public class LO2leop extends OpMode{
  private DcMotorEx frontLeftWheel, frontRightWheel, backLeftWheel, backRightWheel, intakeMotor, outakeMotor;
  private static final boolean useWebcam = true;
  Limelight3A limelight3A;
  double theAngle;
  /*
  Pipeline 0: returns red pollen
  Pipeline 1: returns blue pollen
  Pipeline 2: returns AprilTag locations
  */
  
  @Override
  public void init() {
    frontLeftWheel = hardwareMap.get(DcMotorEx.class, "flw");
    frontRightWheel = hardwareMap.get(DcMotorEx.class, "frw");
    backLeftWheel = hardwareMap.get(DcMotorEx.class, "blw");
    backRightWheel = hardwareMap.get(DcMotorEx.class, "brw");
    intakeMotor = hardwareMap.get(DcMotorEx.class, "im");
    outakeMotor = hardwareMap.get(DcMotorEx.class, "om");

    //Determines motor direction
    frontLeftWheel.setDirection(DcMotorEx.Direction.REVERSE);
    backLeftWheel.setDirection(DcMotorEx.Direction.REVERSE);
    frontRightWheel.setDirection(DcMotorEx.Direction.FORWARD);
    backRightWheel.setDirection(DcMotorEx.Direction.FORWARD);
    intakeMotor.setDirection(DcMotorEx.Direction.FORWARD);
    outakeMotor.setDirection(DcMotorEx.Direction.FORWARD);

    //Encodes for motors
    frontLeftWheel.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    frontRightWheel.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    backLeftWheel.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    backRightWheel.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    intakeMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    outakeMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

    limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
  }

  //Triggers on robot startup
  public void start() {
    limelight3A.pipelineSwitch(2);
    limelight3A.start();
  }

  static double distanceToTarget(double targetY) {
    double LMAD; //Amount of degrees limelight is from being perfectly straight
    double limelightHeight = 10; //10 is a placeholder
    double targetHeight;

    LMAD = 10;
    targetHeight = 1.4478;

    return (targetHeight - limelightHeight) / Math.tan(targetY + LMAD);
  }

  double calculateShotVelocity(double distanceToTarget) {
    //Defining the variables for the equation
    double g = -9.8;
    double y = 1.4478; //meters

    List<Double> possibleVelocities = new ArrayList<>();
    List<Double> possibleAngles = new ArrayList<>();

    for (int i = 0; i <= 9; i++) {
      double angle = i * 10;

      double possibility = (Math.sqrt((g * Math.pow(distanceToTarget, 2)) / (2 * Math.pow((Math.cos(angle)), 2) * (y - distanceToTarget * Math.tan(angle)))));

      if (possibility < 29.84513) {
        possibleVelocities.add(possibility);
        possibleAngles.add(angle);
      }
    }

    theAngle = possibleAngles.get(Collections.min(possibleVelocities).intValue());
    return Collections.min(possibleVelocities);
  }

  public void driveOmni(double y, double rx, double x) {
    final double MTPS = 4661; //Max ticks per second
    //Finds the greatest, positive, not float value among x, y, and rx
    double maxValue = Math.max(Math.abs(x) + Math.abs(y) + Math.abs(rx), 1);

    //Math stuff
    double flwPower = (y + x + rx) / maxValue;
    double blwPower = (y - x + rx) / maxValue;
    double frwPower = (y - x - rx) / maxValue;
    double brwPower = (y + x - rx) / maxValue;

    frontLeftWheel.setVelocity(flwPower * MTPS);
    frontRightWheel.setVelocity(frwPower * MTPS);
    backLeftWheel.setVelocity(blwPower * MTPS);
    backRightWheel.setVelocity(brwPower * MTPS);
  }

  @Override
  public void loop() {
    double velocity = 0;
     LLResult llResult = limelight3A.getLatestResult();
     if (llResult != null && llResult.isValid()) {
       double targetX = llResult.getTx();
       double targetY = llResult.getTy();
       double targetA = llResult.getTa();

       velocity = calculateShotVelocity(distanceToTarget(targetY));
     }

    //Determines the inputs of the gamepad
    double y = -gamepad1.left_stick_y;
    double x = gamepad1.left_stick_x;
    double rx = gamepad1.right_stick_x;
    if (gamepad1.a) {
      outakeMotor.setVelocity(velocity);
    }
    driveOmni(y, rx, x);
   }
}
