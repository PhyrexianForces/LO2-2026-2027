package Auto;

import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@Autonomous
public class LO2onomous extends OpMode {
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
    public void start() {
        limelight3A.pipelineSwitch(0);
        limelight3A.start();
    }

    @Override
    public void loop() {

    }
}
