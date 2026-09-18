package launchpad.vision;

import androidx.annotation.NonNull;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import codebase.telemetry_viewer.websocket.TelemetryData;

public class LimelightManager {
    private final Limelight3A limelight;

    @TelemetryData
    private int currentPipelineIndex = 0;

    public LimelightManager(@NonNull Limelight3A limelight) {
        this.limelight = limelight;
    }

    public static class AprilTagResult {
        public int id;
        public double horizontalOffsetRadians;
        public double verticalOffsetRadians;
        public double imagePercentage;
        public Pose3D targetPositionRelative;

        public AprilTagResult(int id, double horizontalOffsetRadians, double verticalOffsetRadians, double imagePercentage, Pose3D targetPositionRelative) {
            this.id = id;
            this.horizontalOffsetRadians = horizontalOffsetRadians;
            this.verticalOffsetRadians = verticalOffsetRadians;
            this.imagePercentage = imagePercentage;
            this.targetPositionRelative = targetPositionRelative;
        }
    }

    public List<Integer> getVisibleAprilTagIds() {
        return getVisibleAprilTags()
                .stream()
                .map(tag -> tag.id)
                .collect(Collectors.toList());
    }

    public List<AprilTagResult> getVisibleAprilTags() {
        switchToPipeline(3);

        LLResult result = limelight.getLatestResult();

        if (result != null && result.isValid()) {
            return result.getFiducialResults()
                    .stream()
                    .map(f -> new AprilTagResult(
                            f.getFiducialId(),
                            f.getTargetXDegrees(),
                            f.getTargetYDegrees(),
                            f.getTargetArea(),
                            f.getTargetPoseRobotSpace()
                    ))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private void switchToPipeline(int pipelineIndex) {

        if (pipelineIndex == currentPipelineIndex) {
            return;
        }

        limelight.pipelineSwitch(pipelineIndex);

        LLResult initialResult = limelight.getLatestResult();
        if (initialResult == null) {
            return;
        }

        double lastTimestamp = initialResult.getTimestamp();
        double startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime <= 1000) {
            LLResult current = limelight.getLatestResult();
            if (current != null && current.getTimestamp() != lastTimestamp) {
                break;
            }
            // wait for new frame so that new pipeline has initialized
        }

        currentPipelineIndex = pipelineIndex;
    }

    public Limelight3A getLimelight() {
        return limelight;
    }
}
