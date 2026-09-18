package codebase.pathing;

import codebase.Loop;
import codebase.geometry.FieldPosition;
import codebase.telemetry_viewer.websocket.RobotPosition;

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
