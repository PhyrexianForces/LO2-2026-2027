package launchpad.telemetry_viewer.websocket.packets;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import launchpad.geometry.FieldPosition;

public class TelemetryUpdatePacket extends TelemetryPacket {
    @SerializedName("telemetryDataName")
    public String telemetryDataName;

    @SerializedName("telemetryDataType")
    public TelemetryDataType telemetryDataType;

    @SerializedName("telemetryDataValue")
    public Object telemetryDataValue;

    public enum TelemetryDataType {
        @SerializedName("integer")
        INTEGER(Integer.class),

        @SerializedName("double")
        DOUBLE(Double.class),

        @SerializedName("string")
        STRING(String.class),

        @SerializedName("fieldPosition")
        FIELD_POSITION(TelemetryFieldPosition.class),

        @SerializedName("robotPosition")
        ROBOT_POSITION(TelemetryFieldPosition.class),

        @SerializedName("actions")
        ACTIONS(TelemetryActions.class);

        public final Class<?> metricTypeClass;

        TelemetryDataType(Class<?> metricTypeClass) {
            this.metricTypeClass = metricTypeClass;
        }
    }

    public static class TelemetryFieldPosition {
        @SerializedName("x")
        public double x;
        @SerializedName("y")
        public double y;

        @SerializedName("direction")
        public double direction;

        public TelemetryFieldPosition(FieldPosition fieldPosition) {
            this.x = fieldPosition.x;
            this.y = fieldPosition.y;
            this.direction = fieldPosition.direction;
        }
    }

    public enum ActionStatus {
        @SerializedName("queued")
        QUEUED,

        @SerializedName("running")
        RUNNING,

        @SerializedName("completed")
        COMPLETED,

        /** Removed from a SimultaneousAction before it reported itself complete. */
        @SerializedName("cancelled")
        CANCELLED
    }

    /**
     * The children of an action container (a SequentialAction or SimultaneousAction), each
     * carrying its status. Built by
     * {@link launchpad.telemetry_viewer.websocket.ActionTelemetryTracker}.
     */
    public static class TelemetryActions {
        @SerializedName("actions")
        public List<TelemetryAction> actions;
    }

    public static class TelemetryAction {
        @SerializedName("actionName")
        public String actionName;

        @SerializedName("actionParameters")
        public String actionParameters;

        @SerializedName("status")
        public ActionStatus status;

        @SerializedName("subActions")
        public List<TelemetryAction> subActions;
    }
}
