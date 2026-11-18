import { Suspense, useMemo } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, Box, Line, useGLTF } from '@react-three/drei';
import { DoubleSide, Shape } from 'three';
import fieldModelUrl from '../assets/field.glb?url';
import type { FieldPosition } from '../types/telemetry';
import { useTheme } from '../theme';

// Nominal field size for the backdrop floor plane.
const FIELD_SIZE = 144; // inches

// Real tile geometry measured from the CAD: interlocked foam tiles repeat every 23.5", not 24",
// so six tiles span 141". The decimated model loses the seam detail, so seams are drawn here.
const TILE_PITCH = 23.5;
const TILES_PER_SIDE = 6;
const TILE_AREA_HALF = (TILE_PITCH * TILES_PER_SIDE) / 2; // 70.5"
const TILE_TOP_Y = 0;      // tile top surface in the model
// Decimation leaves the tile tops slightly wavy (up to ~0.7"), so sit well above them and the tape.
const SEAM_Y = TILE_TOP_Y + 0.75;

// The field model is exported from Onshape in meters, Y-up, centered on the field origin.
// The scene works in inches.
const METERS_TO_INCHES = 39.3701;

// OrbitControls defaults are 1.0; the field is small enough that full-speed input feels twitchy.
const CAMERA_ROTATE_SPEED = 0.6;
const CAMERA_ZOOM_SPEED = 0.6;

interface Props {
  fieldPosition: FieldPosition | null;
}

interface TileSeamsProps {
  color: string;
}

/** Interior seams between tiles, at the real 23.5" pitch. The perimeter wall frames the edges. */
function TileSeams({ color }: TileSeamsProps) {
  const lines = useMemo(() => {
    const result: Array<[[number, number, number], [number, number, number]]> = [];
    for (let i = 1; i < TILES_PER_SIDE; i++) {
      const pos = -TILE_AREA_HALF + i * TILE_PITCH;
      result.push([[pos, SEAM_Y, -TILE_AREA_HALF], [pos, SEAM_Y, TILE_AREA_HALF]]);
      result.push([[-TILE_AREA_HALF, SEAM_Y, pos], [TILE_AREA_HALF, SEAM_Y, pos]]);
    }
    return result;
  }, []);

  return (
    <>
      {lines.map((pts, i) => (
        <Line key={i} points={pts} color={color} lineWidth={1.5} />
      ))}
    </>
  );
}

/**
 * The official season field CAD, pre-shrunk by telemetry-viewer/web/shrink-field-model.mjs
 * (fasteners removed, meshes decimated, meshopt-compressed) so it can be inlined into the
 * single-file page. useGLTF's third argument enables the bundled meshopt decoder, which
 * matters because the laptop is on the robot's wifi with no internet for a CDN decoder.
 */
function FieldModel() {
  const { scene } = useGLTF(fieldModelUrl, false, true);
  return <primitive object={scene} scale={METERS_TO_INCHES} />;
}

// Chassis dimensions in inches. Local +x is "forward" (matches FieldPosition.direction),
// local z is left/right. The chassis plate is 16" (z) x 18" (x), with 4"-diameter wheels
// side-mounted flush against the plate's edges (like bolted-on axles), sticking out fully
// rather than tucking under the body.
const CHASSIS_LENGTH = 18;
const CHASSIS_WIDTH = 16;
const CHASSIS_HEIGHT = 3;
const WHEEL_RADIUS = 2;
const WHEEL_THICKNESS = 1.5;
const WHEEL_X = CHASSIS_LENGTH / 2 - WHEEL_RADIUS - 1; // inset from the front/back edges
// The wheel's inner face (at its axle-direction half-thickness, not its radius) sits flush
// against the chassis side.
const WHEEL_Z = CHASSIS_WIDTH / 2 + WHEEL_THICKNESS / 2;

// A forward-pointing arrow (flat rectangle shaft + flat triangle head, both lying on top of
// the chassis) marks the front, since a plain rectangle looks the same front-to-back.
const ARROW_TIP_X = CHASSIS_LENGTH / 2 - 1;
const ARROW_HEAD_LENGTH = 5;
const ARROW_HEAD_HALF_BASE = 2.5;
const ARROW_SHAFT_LENGTH = 5;
const ARROW_Y = CHASSIS_HEIGHT + 0.1;

// A flat 2D triangle (in the shape's own x/y plane) for the arrowhead; laid flat on top of the
// chassis via rotation in the JSX below, rather than a curved coneGeometry.
const arrowHeadShape = new Shape();
arrowHeadShape.moveTo(0, -ARROW_HEAD_HALF_BASE);
arrowHeadShape.lineTo(ARROW_HEAD_LENGTH, 0);
arrowHeadShape.lineTo(0, ARROW_HEAD_HALF_BASE);
arrowHeadShape.closePath();

interface RobotProps {
  position: FieldPosition;
  chassisColor: string;
  wheelColor: string;
  frontMarkerColor: string;
}

interface WheelProps {
  x: number;
  z: number;
  color: string;
}

function Wheel({ x, z, color }: WheelProps) {
  return (
    <mesh position={[x, 0, z]} rotation={[Math.PI / 2, 0, 0]}>
      <cylinderGeometry args={[WHEEL_RADIUS, WHEEL_RADIUS, WHEEL_THICKNESS, 16]} />
      <meshStandardMaterial color={color} />
    </mesh>
  );
}

function Robot({ position, chassisColor, wheelColor, frontMarkerColor }: RobotProps) {
  // FTC: x=right, y=up-field. Three.js: x=right, z=-forward
  const tx = position.x;
  const tz = -position.y;
  // FTC direction: CCW from +x (right). Three.js rotation around Y: CCW from +x when viewed from above
  const ry = position.direction;

  return (
    <group position={[tx, WHEEL_RADIUS, tz]} rotation={[0, ry, 0]}>
      <Box args={[CHASSIS_LENGTH, CHASSIS_HEIGHT, CHASSIS_WIDTH]} position={[0, CHASSIS_HEIGHT / 2, 0]}>
        <meshStandardMaterial color={chassisColor} />
      </Box>
      {/* Forward-pointing arrow on top of the chassis: shaft... */}
      <Box
        args={[ARROW_SHAFT_LENGTH, 0.5, 1.5]}
        position={[ARROW_TIP_X - ARROW_HEAD_LENGTH - ARROW_SHAFT_LENGTH / 2, ARROW_Y, 0]}
      >
        <meshStandardMaterial color={frontMarkerColor} />
      </Box>
      {/* ...and a flat triangular head, laid flat (its shape-space +z normal becomes world +y) */}
      <mesh
        position={[ARROW_TIP_X - ARROW_HEAD_LENGTH, ARROW_Y, 0]}
        rotation={[-Math.PI / 2, 0, 0]}
      >
        <shapeGeometry args={[arrowHeadShape]} />
        <meshStandardMaterial color={frontMarkerColor} side={DoubleSide} />
      </mesh>
      <Wheel x={WHEEL_X} z={WHEEL_Z} color={wheelColor} />
      <Wheel x={WHEEL_X} z={-WHEEL_Z} color={wheelColor} />
      <Wheel x={-WHEEL_X} z={WHEEL_Z} color={wheelColor} />
      <Wheel x={-WHEEL_X} z={-WHEEL_Z} color={wheelColor} />
    </group>
  );
}

export function FieldViewer3D({ fieldPosition }: Props) {
  const { theme } = useTheme();

  return (
    <div style={{ background: theme.bgField, borderRadius: 4, overflow: 'hidden' }}>
      <Canvas camera={{ position: [0, 220, 80], fov: 45 }}>
        <ambientLight intensity={0.6} />
        <directionalLight position={[100, 200, 100]} intensity={1} />
        {/* Field floor */}
        <mesh rotation={[-Math.PI / 2, 0, 0]} position={[0, -0.5, 0]}>
          <planeGeometry args={[FIELD_SIZE, FIELD_SIZE]} />
          <meshStandardMaterial color={theme.color3dFloor} />
        </mesh>
        <Suspense fallback={null}>
          <FieldModel />
        </Suspense>
        <TileSeams color={theme.color3dSeam} />
        {fieldPosition && (
          <Robot
            position={fieldPosition}
            chassisColor="#757575"
            wheelColor="#fdd835"
            frontMarkerColor={theme.color3dCone}
          />
        )}
        <OrbitControls
          target={[0, 0, 0]}
          rotateSpeed={CAMERA_ROTATE_SPEED}
          zoomSpeed={CAMERA_ZOOM_SPEED}
        />
      </Canvas>
    </div>
  );
}
