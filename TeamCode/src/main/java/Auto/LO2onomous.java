package Auto;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

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

    //Triggers on robot startup
    public void start() {
        limelight3A.pipelineSwitch(0);
        pipeline = 0;
        limelight3A.start();
    }

    @Override
    public void loop() {
        LLResult llResult = limelight3A.getLatestResult();
        if (llResult != null && llResult.isValid()) {
            if (pipeline <= 1) {
                double targetX = llResult.getTx(); //Degrees of target to the left/right
                double targetY = llResult.getTy(); //Degrees of target to the up/down
                double targetA = llResult.getTa(); //Percent of space target takes up in vision

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
    }
}
