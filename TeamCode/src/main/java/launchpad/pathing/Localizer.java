package launchpad.pathing;

import codebase.telemetry_viewer.websocket.RobotPosition;
import launchpad.Loop;
import launchpad.geometry.FieldPosition;

/**
 * Team 4096 Localization System
 * X-positive: towards audience viewing area
 * Y-positive: towards right of field
 * 0 rotation: facing X-positive direction
 */
public interface Localizer extends Loop {

    @RobotPosition
    FieldPosition getCurrentPosition();

    void init();
}
