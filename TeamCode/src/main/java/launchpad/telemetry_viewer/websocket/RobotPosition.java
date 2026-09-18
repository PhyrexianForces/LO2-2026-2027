package launchpad.telemetry_viewer.websocket;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface RobotPosition {
    /**
     * Overrides the name this value is displayed under. Defaults to the field name, or the
     * method name with a "get" prefix stripped (e.g. getCurrentPosition() -> "currentPosition").
     */
    String value() default "";
}
