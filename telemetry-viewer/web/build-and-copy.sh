#!/bin/sh
# Builds the telemetry viewer and copies the single-file bundle into TeamCode's assets,
# where TelemetryViewerWebHandler serves it from the Robot Controller's web server.
set -e

cd "$(dirname "$0")"

ASSET_DIR="../../TeamCode/src/main/assets/telemetry_viewer_web"

npm install
npm run build

mkdir -p "$ASSET_DIR"
cp dist/index.html "$ASSET_DIR/index.html"

echo "Built and copied to $ASSET_DIR/index.html."
echo "With the robot's WiFi joined, open http://192.168.43.1:8080/telemetry (or use dist/index.html locally)."
