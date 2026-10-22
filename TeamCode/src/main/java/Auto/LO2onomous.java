package Auto;

import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@Autonomous
public class LO2onomous extends OpMode {
    private DcMotorEx frontLeftWheel, frontRightWheel, backLeftWheel, backRightWheel, intakeMotor;
    private static final boolean useWebcam = true;

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
        limelight3A.start();
    }

    @Override
    public void loop() {
        frontLeftWheel.setVelocity(1);
    }
}
