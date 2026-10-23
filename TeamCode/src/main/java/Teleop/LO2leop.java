package Teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

//Limelight
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import java.lang.Math;

@TeleOp
public class LO2leop extends OpMode{
  private DcMotorEx frontLeftWheel, frontRightWheel, backLeftWheel, backRightWheel, intakeMotor;
  private static final boolean useWebcam = true;
  int pipeline = 0;
  Limelight3A limelight3A;
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

    //Determines motor direction
    frontLeftWheel.setDirection(DcMotorEx.Direction.REVERSE);
    backLeftWheel.setDirection(DcMotorEx.Direction.REVERSE);
    frontRightWheel.setDirection(DcMotorEx.Direction.FORWARD);
    backRightWheel.setDirection(DcMotorEx.Direction.FORWARD);
    intakeMotor.setDirection(DcMotorEx.Direction.FORWARD);

    //Encodes for motors
    frontLeftWheel.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    frontRightWheel.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    backLeftWheel.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    backRightWheel.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    intakeMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

    limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
  }

  //Triggers on robot startup
  public void start() {
    limelight3A.pipelineSwitch(0);
    pipeline = 0;
    limelight3A.start();
  }

  public void driveOmni(double y, double rx, double x) {
    final double MTPS = 4661;
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
     LLResult llResult = limelight3A.getLatestResult();
       if (llResult != null && llResult.isValid()) {
         if (pipeline <= 1) {
           double targetX = llResult.getTx();
           double targetY = llResult.getTy();
           double targetA = llResult.getTa();

           telemetry.addData("Target X", targetX);
           telemetry.addData("Target Y", targetY);
           telemetry.addData("Target A", targetA);
         } else {
           Pose3D botpose = llResult.getBotpose();
           if (botpose != null) {
             double x = botpose.getPosition().x;
             double y = botpose.getPosition().y;
             telemetry.addData("Bot Pose", "(" + x + ", " + y + ")");
           }
         }
       } else {
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
