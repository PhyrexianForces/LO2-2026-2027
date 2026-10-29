package Auto;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Autonomous
public class LO2onomous extends OpMode {
    private DcMotorEx frontLeftWheel, frontRightWheel, backLeftWheel, backRightWheel, intakeMotor;
    private static final boolean useWebcam = true;

    int pipeline = 0;

    Limelight3A limelight3A;
    /*
    Pipeline 0: returns red pollen
    Pipeline 1: returns blue pollen
    Pipeline 2: returns AprilTag locations
    */

    //Field Dimensions: 3.556m Square

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

    double calculateShot(double distanceToTarget) {
        //Defining the variables for the equation
        double g = -9.8;
        double y = 1.4478; //meters

        List<Double> possibleVelocities = new ArrayList<>();

        for (int i = 0; i <= 9; i++) {
            double angle = i * 10;

            double possibility = (Math.sqrt((g * Math.pow(distanceToTarget, 2)) / (2 * Math.pow((Math.cos(angle)), 2) * (y - distanceToTarget * Math.tan(angle)))));

            if (possibility < 29.84513) {
                possibleVelocities.add(possibility);
            }
        }
        return Collections.min(possibleVelocities);
    }

    static double distanceToTarget(double targetY, boolean isBalls) {
        double LMAD; //Amount of degrees limelight is from being perfectly straight
        double limelightHeight = 10;
        double targetHeight;

        if (isBalls) {
            LMAD = -10; //Amount of degrees limelight is from being perfectly straight
            targetHeight = 0;
        } else {
            LMAD = 10;
            targetHeight = 1.4478;
        }
        return (targetHeight - limelightHeight) / Math.tan(targetY + LMAD);
    }

    double[] getNectar() {
        double[] targetPos = new double[3];
        limelight3A.pipelineSwitch(1);
        pipeline = 1;

        for(int i = 0; i < 4; i++) {
            LLResult llResult = limelight3A.getLatestResult();
            if (llResult != null && llResult.isValid()) {

                targetPos[0] = llResult.getTx();
                targetPos[1] = llResult.getTy();
                targetPos[2] = llResult.getTa();

                telemetry.addData("Target X", targetPos[0]);
                telemetry.addData("Target Y", targetPos[1]);
                telemetry.addData("Target A", targetPos[2]);

                break;
            } else {
                //Turn 90 degrees
                frontRightWheel.setVelocity(1);
            }
        }
        return targetPos;
    }

        //Triggers on robot startup
    public void start () {
        limelight3A.pipelineSwitch(0);
        pipeline = 0;
        limelight3A.start();
    }

    double robotVelocity = 2;

    @Override
    public void loop () {
        double[] nectarPos = getNectar();
        double nectarDist = distanceToTarget(nectarPos[1], true);

        frontRightWheel.setVelocity(1);
        frontLeftWheel.setVelocity(1);
        //Sleep for dist / velocity
        frontLeftWheel.setVelocity(0);
        frontRightWheel.setVelocity(0);
    }
}