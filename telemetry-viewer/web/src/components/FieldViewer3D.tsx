import { useMemo } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, Box, Line } from '@react-three/drei';
import { DoubleSide, Shape } from 'three';
import type { FieldPosition } from '../types/telemetry';
import { useTheme } from '../theme';

const FIELD_SIZE = 144; // inches
const TILE_SIZE = 24;   // 24" tiles
const TILES = FIELD_SIZE / TILE_SIZE; // 6

interface Props {
  fieldPosition: FieldPosition | null;
}

interface FieldGridProps {
  gridColor: string;
  borderColor: string;
}

function FieldGrid({ gridColor, borderColor }: FieldGridProps) {
  const lines = useMemo(() => {
    const result: Array<[[number, number, number], [number, number, number]]> = [];
    for (let i = 0; i <= TILES; i++) {
      const pos = -FIELD_SIZE / 2 + i * TILE_SIZE;
      result.push([[pos, 0, -FIELD_SIZE / 2], [pos, 0, FIELD_SIZE / 2]]);
      result.push([[-FIELD_SIZE / 2, 0, pos], [FIELD_SIZE / 2, 0, pos]]);
    }
    return result;
  }, []);

  return (
    <>
      {lines.map((pts, i) => (
        <Line key={i} points={pts} color={gridColor} lineWidth={1} />
      ))}
      {/* Border */}
      <Line
        points={[
          [-FIELD_SIZE / 2, 0.5, -FIELD_SIZE / 2],
          [FIELD_SIZE / 2, 0.5, -FIELD_SIZE / 2],
          [FIELD_SIZE / 2, 0.5, FIELD_SIZE / 2],
          [-FIELD_SIZE / 2, 0.5, FIELD_SIZE / 2],
          [-FIELD_SIZE / 2, 0.5, -FIELD_SIZE / 2],
        ]}
        color={borderColor}
        lineWidth={2}
      />
    </>
  );
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
        <FieldGrid gridColor={theme.color3dGrid} borderColor={theme.color3dBorder} />
        {fieldPosition && (
          <Robot
            position={fieldPosition}
            chassisColor="#757575"
            wheelColor="#fdd835"
            frontMarkerColor={theme.color3dCone}
          />
        )}
        <OrbitControls target={[0, 0, 0]} />
      </Canvas>
    </div>
  );
}
