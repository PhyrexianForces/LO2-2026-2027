package launchpad.telemetry_viewer.websocket.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public abstract class TelemetryPacket {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    @SerializedName("_packetType")
    public String packetType;

    public TelemetryPacket() {
        this.packetType = this.getClass().getSimpleName();
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
