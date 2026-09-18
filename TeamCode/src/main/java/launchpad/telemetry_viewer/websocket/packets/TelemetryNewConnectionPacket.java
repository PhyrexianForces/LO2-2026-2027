package launchpad.telemetry_viewer.websocket.packets;

import com.google.gson.annotations.SerializedName;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TelemetryNewConnectionPacket extends TelemetryPacket {

    @SerializedName("opModeName")
    private String opModeName;

    @SerializedName("opModeType")
    private TelemetryOpModeType opModeType;

    public enum TelemetryOpModeType {
        @SerializedName("AUTONOMOUS")
        AUTONOMOUS,

        @SerializedName("TELEOP")
        TELE_OP;
    }

    @SerializedName("hardwareDevices")
    public List<TelemetryHardwareDevice> hardwareDevices;

    public static class TelemetryHardwareDevice {
        @SerializedName("name")
        public String name;

        @SerializedName("type")
        public String type;

        @SerializedName("connectionDetails")
        public String connectionDetails;

        @SerializedName("version")
        public int version;
    }

    public TelemetryNewConnectionPacket(OpMode opMode) {
        super();

        Autonomous autonomousAnnotation = opMode.getClass().getAnnotation(Autonomous.class);
        TeleOp teleOpAnnotation = opMode.getClass().getAnnotation(TeleOp.class);

        if (autonomousAnnotation != null) {
            this.opModeName = autonomousAnnotation.name();
            this.opModeType = TelemetryOpModeType.AUTONOMOUS;
        } else if (teleOpAnnotation != null) {
            this.opModeName = teleOpAnnotation.name();
            this.opModeType = TelemetryOpModeType.TELE_OP;
        } else {
            throw new IllegalStateException("Not running a valid op mode for telemetry: neither Auto nor Tele-Op");
        }

        if (this.opModeName.isEmpty()) {
            this.opModeName = opMode.getClass().getSimpleName();
        }

        this.hardwareDevices = this.getHardwareDevices(opMode);
    }

    private List<TelemetryHardwareDevice> getHardwareDevices(OpMode opMode) {
        List<TelemetryHardwareDevice> devices = new ArrayList<>();

        for (HardwareMap.DeviceMapping<? extends HardwareDevice> hardwareDevices : opMode.hardwareMap.allDeviceMappings) {
            for (HardwareDevice hardwareDevice : hardwareDevices) {
                TelemetryHardwareDevice telemetryHardwareDevice = new TelemetryHardwareDevice();

                Set<String> deviceNames = opMode.hardwareMap.getNamesOf(hardwareDevice);

                telemetryHardwareDevice.name = deviceNames.isEmpty() ? "(unknown)" : String.join("/", deviceNames);
                telemetryHardwareDevice.type = hardwareDevice.getClass().getSimpleName() + " (" + hardwareDevice.getDeviceName() + ")";
                telemetryHardwareDevice.connectionDetails = hardwareDevice.getConnectionInfo();
                telemetryHardwareDevice.version = hardwareDevice.getVersion();

                devices.add(telemetryHardwareDevice);
            }
        }

        return devices;
    }
}
