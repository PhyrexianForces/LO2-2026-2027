package launchpad.gamepad.input;


import launchpad.Loop;
import launchpad.gamepad.Gamepad;

public interface GInput extends Loop {

    Gamepad getGamepad();
}
