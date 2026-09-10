package launchpad.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import launchpad.Loop;

public abstract class Subsystem implements Loop {
    public abstract void init(HardwareMap hardwareMap);
    public abstract void loop();
    public void stop() {}
}
