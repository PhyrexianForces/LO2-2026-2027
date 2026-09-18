package launchpad.gamepad;

import androidx.annotation.NonNull;

import launchpad.Loop;
import launchpad.gamepad.input.types.Button;
import launchpad.gamepad.input.types.Joystick;
import launchpad.gamepad.input.types.Trigger;

public class Gamepad implements Loop {

    public final Button xButton;
    public final Button yButton;
    public final Button aButton;
    public final Button bButton;

    public final Button leftBumper;
    public final Button rightBumper;

    public final Button dpadLeft;
    public final Button dpadRight;
    public final Button dpadUp;
    public final Button dpadDown;

    public final Joystick leftJoystick;
    public final Joystick rightJoystick;

    public final Button leftJoystickButton;
    public final Button rightJoystickButton;

    public final Trigger leftTrigger;
    public final Trigger rightTrigger;

    public Gamepad(@NonNull com.qualcomm.robotcore.hardware.Gamepad gamepad) {
        this.xButton = new Button(this, () -> gamepad.x);
        this.yButton = new Button(this, () -> gamepad.y);
        this.aButton = new Button(this, () -> gamepad.a);
        this.bButton = new Button(this, () -> gamepad.b);

        this.leftBumper = new Button(this, () -> gamepad.left_bumper);
        this.rightBumper = new Button(this, () -> gamepad.right_bumper);

        this.dpadLeft = new Button(this, () -> gamepad.dpad_left);
        this.dpadRight = new Button(this, () -> gamepad.dpad_right);
        this.dpadUp = new Button(this, () -> gamepad.dpad_up);
        this.dpadDown = new Button(this, () -> gamepad.dpad_down);

        // y values from the robotcore gamepad class are inverted from what is expected: + is up, - is down. This corrects it to + is up, - is down
        this.leftJoystick = new Joystick(this, () -> gamepad.left_stick_x, () -> -gamepad.left_stick_y);
        this.rightJoystick = new Joystick(this, () -> gamepad.right_stick_x, () -> -gamepad.right_stick_y);

        this.leftJoystickButton = new Button(this, () -> gamepad.left_stick_button);
        this.rightJoystickButton = new Button(this, () -> gamepad.right_stick_button);

        this.leftTrigger = new Trigger(this, () -> gamepad.left_trigger);
        this.rightTrigger = new Trigger(this, () -> gamepad.right_trigger);
    }

    @Override
    public void loop() {
        this.xButton.loop();
        this.yButton.loop();
        this.aButton.loop();
        this.bButton.loop();

        this.leftBumper.loop();
        this.rightBumper.loop();

        this.dpadUp.loop();
        this.dpadDown.loop();
        this.dpadLeft.loop();
        this.dpadRight.loop();

        this.leftJoystickButton.loop();
        this.rightJoystickButton.loop();

        this.leftJoystick.loop();
        this.rightJoystick.loop();

        this.leftTrigger.loop();
        this.rightTrigger.loop();
    }
}
