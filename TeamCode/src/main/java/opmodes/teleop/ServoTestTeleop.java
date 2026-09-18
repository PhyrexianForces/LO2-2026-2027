package opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;

import launchpad.gamepad.Gamepad;

@TeleOp(name="Servo Test Teleop")
public class ServoTestTeleop extends OpMode {
    private Gamepad gamepad;
    private final List<Servo> servos = new ArrayList<>();


    private Telemetry.Item currentServoTelemetry;
    private Telemetry.Item currentServoPositionTelemetry;

    private int currentServoIndex = 0;

    private double currentServoPosition = 0;

    @Override
    public void init() {
        servos.addAll(hardwareMap.getAll(Servo.class));

        if (servos.isEmpty()) {
            throw new IllegalStateException("No servos found :(");
        }

        currentServoPosition = servos.get(currentServoIndex).getPosition();

        gamepad = new Gamepad(gamepad1);

        gamepad.xButton.onPress(() -> updateServoIndex(true));
        gamepad.bButton.onPress(() -> updateServoIndex(false));

        gamepad.rightTrigger.onPress(() -> updateServoPosition(0.1));

        gamepad.leftTrigger.onPress(() -> updateServoPosition(-0.1));

        gamepad.rightBumper.onPress(() -> updateServoPosition(0.01));

        gamepad.leftBumper.onPress(() -> updateServoPosition(-0.01));

        telemetry.addLine("--- Servo Test Controls ---");
        telemetry.addLine("X/B: prev/next servo");
        telemetry.addLine("Left/Right Trigger: -/+ position by 0.1");
        telemetry.addLine("Left/Right Bumper: -/+ position by 0.01");
        telemetry.addLine("---------------------------");

        currentServoTelemetry = telemetry.addData("Current Servo", getSelectedServoOutput());
        currentServoPositionTelemetry = telemetry.addData("Current Position", currentServoPosition);
    }

    private void updateServoIndex(boolean decrease) {
        if (decrease) {
            currentServoIndex = (currentServoIndex - 1 + servos.size()) % servos.size();
        } else {
            currentServoIndex = (currentServoIndex + 1) % servos.size();
        }
        currentServoPosition = servos.get(currentServoIndex).getPosition();
    }

    private void updateServoPosition(double increaseBy) {
        currentServoPosition = Math.max(0, Math.min(1, currentServoPosition + increaseBy));
        servos.get(currentServoIndex).setPosition(currentServoPosition);
    }

    private String getSelectedServoOutput() {
        return currentServoIndex + ": " + hardwareMap.getNamesOf(servos.get(currentServoIndex)).iterator().next();
    }

    @Override
    public void loop() {
        gamepad.loop();
        currentServoTelemetry.setValue(getSelectedServoOutput());
        currentServoPositionTelemetry.setValue(currentServoPosition);
    }
}
