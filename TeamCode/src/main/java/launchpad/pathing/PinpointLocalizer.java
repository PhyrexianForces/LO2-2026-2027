package launchpad.pathing;

import androidx.annotation.NonNull;

import com.qualcomm.hardware.lynx.LynxNackException;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

import launchpad.geometry.FieldPosition;
import launchpad.geometry.MovementVector;
import launchpad.hardware.PinpointModule;

public class PinpointLocalizer implements Localizer {

    private final PinpointModule pinpointModule;

    /**
     *
     * The center of rotation of the robot is the point it rotates around when spinning.
     * It can be found by finding the intersection of the two lines made by diagonal wheel pairs.
     *
     * @param pinpointModule The PinPoint device
     * @param xPodOffsetFromCenter How far sideways (in mm) from the robot center the X (forward) odometry pod is. Left of the center is a positive number, right of center is a negative number
     * @param xDirection The direction the X-pod (forward) is oriented
     * @param yPodOffsetFromCenter How far forwards (in mm) from the tracking point the Y (strafe) odometry pod is. forward of center is a positive number, backwards is a negative number
     * @param yDirection The direction the y-pod (strafe) is oriented
     * @param encoderResolution The number of ticks per mm of the encoders attached to the PinPoint
     */
    public PinpointLocalizer(@NonNull PinpointModule pinpointModule, double xPodOffsetFromCenter, @NonNull PinpointModule.EncoderDirection xDirection, double yPodOffsetFromCenter, @NonNull PinpointModule.EncoderDirection yDirection, double encoderResolution) {
        this.pinpointModule = pinpointModule;
        this.pinpointModule.setEncoderResolution(encoderResolution);
        this.pinpointModule.setOffsets(xPodOffsetFromCenter,yPodOffsetFromCenter);
        this.pinpointModule.setEncoderDirections(xDirection, yDirection);
    }

    /**
     *
     * The center of rotation of the robot is the point it rotates around when spinning.
     * It can be found by finding the intersection of the two lines made by diagonal wheel pairs.
     *
     * @param pinpointModule The PinPoint device
     * @param xPodOffsetFromCenter How far left (in mm) from the robot center the X (forward) odometry pod is. Left of the center is a positive number, right of center is a negative number
     * @param xDirection The direction the X-pod (forward) is oriented
     * @param yPodOffsetFromCenter How far forwards (in mm) from the tracking point the Y (strafe) odometry pod is. forward of center is a positive number, backwards is a negative number
     * @param yDirection The direction the y-pod (strafe) is oriented
     * @param pods The type of pods you are using
     */
    public PinpointLocalizer(@NonNull PinpointModule pinpointModule, double xPodOffsetFromCenter, @NonNull PinpointModule.EncoderDirection xDirection, double yPodOffsetFromCenter, @NonNull PinpointModule.EncoderDirection yDirection, @NonNull PinpointModule.GoBildaOdometryPods pods) {
        this.pinpointModule = pinpointModule;
        this.pinpointModule.setEncoderResolution(pods);
        this.pinpointModule.setOffsets(xPodOffsetFromCenter,yPodOffsetFromCenter);
        this.pinpointModule.setEncoderDirections(xDirection,yDirection);
    }

    public void setCurrentFieldPosition(FieldPosition position) {
        pinpointModule.setPosition(new Pose2D(DistanceUnit.INCH, position.x, position.y, AngleUnit.RADIANS, position.direction));
    }

    @Override
    public FieldPosition getCurrentPosition() {
        Pose2D pinpointPosition = pinpointModule.getPosition();
        return new FieldPosition(pinpointPosition.getX(DistanceUnit.INCH), pinpointPosition.getY(DistanceUnit.INCH), pinpointPosition.getHeading(AngleUnit.RADIANS));
    }

    @Override
    public void init() {
        this.pinpointModule.resetPosAndIMU();
    }

    public MovementVector getVelocity() {
        Pose2D pinpointVelocity = pinpointModule.getVelocity();
        return new MovementVector(pinpointVelocity.getX(DistanceUnit.INCH), pinpointVelocity.getY(DistanceUnit.INCH), pinpointVelocity.getHeading(AngleUnit.RADIANS));
    }

    public String status() {
        return pinpointModule.getDeviceStatus().toString();
    }

    public double getFrequency() {
        return pinpointModule.getFrequency();
    }

    @Override
    public void loop() {
        try {
            pinpointModule.update();
        } catch (LynxNackException e) {
            throw new RuntimeException("PinpointModule update failed", e);
        }
    }

    public boolean isDoneInitializing() {
        return pinpointModule.getDeviceStatus() == PinpointModule.DeviceStatus.READY;
    }
}