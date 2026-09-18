package launchpad.actions;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.Servo;

public class SetServoRotationAction extends RunOnceAction {

    private final Servo servo;
    private final double position;

    /**
     * @param servo the servo to set the position of
     * @param position the target position in the range [0.0, 1.0]
     */
    public SetServoRotationAction(@NonNull Servo servo, double position) {
        this.servo = servo;
        this.position = position;
    }

    @Override
    public void run() {
        servo.setPosition(position);
    }
}
