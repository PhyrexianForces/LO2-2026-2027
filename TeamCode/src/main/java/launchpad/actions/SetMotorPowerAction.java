package launchpad.actions;

import androidx.annotation.NonNull;

import launchpad.hardware.Motor;

public class SetMotorPowerAction extends RunOnceAction {

    private final Motor motor;
    @ActionParameter final double power;

    public SetMotorPowerAction(@NonNull Motor motor, double power) {
        this.motor = motor;
        this.power = power;
    }

    @Override
    public void run() {
        motor.setPower(power);
    }
}
