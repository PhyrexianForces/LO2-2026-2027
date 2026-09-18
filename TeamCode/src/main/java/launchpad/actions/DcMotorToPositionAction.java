package launchpad.actions;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.DcMotor;

import launchpad.hardware.Motor;

public class DcMotorToPositionAction implements Action {
    private final Motor motor;
    private final int targetTicks;
    private final double power;
    private boolean started = false;

    /**
     * @param motor the motor to move
     * @param targetRotation the target position in radians from encoder zero
     * @param power maximum power [0, 1] applied while moving
     */
    public DcMotorToPositionAction(@NonNull Motor motor, double targetRotation, double power) {
        this.motor = motor;
        this.targetTicks = motor.getMotorEncoder().toTicks(targetRotation);
        this.power = power;
    }

    @Override
    public void init() {
        motor.getMotor().setTargetPosition(targetTicks);
        motor.getMotor().setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motor.getMotor().setPower(power);
        started = false;
    }

    @Override
    public void loop() {
        started = true;
    }

    @Override
    public boolean isComplete() {
        return started && !motor.getMotor().isBusy();
    }
}