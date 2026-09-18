package launchpad.telemetry_viewer;

import android.content.Context;

import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.WebHandlerManager;

import org.firstinspires.ftc.ftccommon.external.WebHandlerRegistrar;
import org.firstinspires.ftc.robotcore.internal.webserver.WebHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

/**
 * Serves the built telemetry viewer page from the Robot Controller's own web server (the one
 * that already hosts the RC console on port 8080), so a laptop joined to the robot's WiFi can
 * just open {@code http://192.168.43.1:8080/telemetry}. The page then connects back to
 * {@link launchpad.telemetry_viewer.websocket.TelemetryServer} on the same host.
 * <p>
 * The SDK calls {@link #registerHandlers} once when the RC app starts, via the
 * {@link WebHandlerRegistrar} annotation. The page itself lives in
 * {@code TeamCode/src/main/assets/telemetry_viewer_web/index.html}, produced by
 * {@code telemetry-viewer/web/build-and-copy.sh}.
 */
public final class TelemetryViewerWebHandler {
    public static final String PATH = "/telemetry";
    private static final String ASSET_PATH = "telemetry_viewer_web/index.html";
    private static final String TAG = "TelemetryViewerWebHandler";

    private TelemetryViewerWebHandler() {}

    @WebHandlerRegistrar
    public static void registerHandlers(Context context, WebHandlerManager manager) {
        final byte[] page;
        try {
            page = readAsset(context, ASSET_PATH);
        } catch (IOException e) {
            RobotLog.ee(TAG, e, "Telemetry viewer page missing from assets (%s); run telemetry-viewer/web/build-and-copy.sh", ASSET_PATH);
            return;
        }

        WebHandler handler = session -> NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK, "text/html; charset=utf-8", new ByteArrayInputStream(page), page.length);

        manager.register(PATH, handler);
        manager.register(PATH + "/", handler);
        RobotLog.ii(TAG, "Telemetry viewer available at http://192.168.43.1:8080%s", PATH);
    }

    private static byte[] readAsset(Context context, String path) throws IOException {
        try (InputStream in = context.getAssets().open(path)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }
}
