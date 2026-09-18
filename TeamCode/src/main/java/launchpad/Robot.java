package launchpad;

import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.ArrayList;
import java.util.List;

import launchpad.subsystems.Subsystem;

public class Robot {
    private final List<Subsystem> subsystems = new ArrayList<>();
    private final HardwareMap hardwareMap;

    public Robot(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    public <T extends Subsystem> T register(T subsystem) {
        subsystem.init(hardwareMap);
        subsystems.add(subsystem);
        return subsystem;
    }

    public void loop() {
        for (Subsystem s : subsystems) s.loop();
    }

    public void stop() {
        for (Subsystem s : subsystems) s.stop();
    }
}
