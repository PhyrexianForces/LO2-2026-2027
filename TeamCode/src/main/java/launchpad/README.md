# launchpad

FTC robot framework for Team 4096. This package provides reusable building blocks for both teleop and autonomous operation. Each sub-package is independent; use only what your op mode needs.

## Core

### `Loop`
Single-method interface (`loop()`) implemented by anything that needs to run every tick — subsystems, gamepads, actions, localizers.

### `Robot`
Manages a list of `Subsystem` instances. Call `register(subsystem)` to initialize and track a subsystem, then call `robot.loop()` each tick and `robot.stop()` on shutdown.

```java
Robot robot = new Robot(hardwareMap);
MyArm arm = robot.register(new MyArm());
// in loop():
robot.loop();
```

### `TeleopBase`
Abstract `OpMode` subclass that creates a `Robot` and two `Gamepad` wrappers. Override `registerSubclasses()` to call `robot.register(...)` for each subsystem.

### `Constants`
Robot-specific tuning values: drive motor config, mecanum coefficient matrix, Pinpoint offsets, movement velocity/steepness, and GoBilda motor tick counts.

---

## Packages

### `actions`
An action is a discrete, reusable task with an `init()`, `loop()`, and `isComplete()`. Actions are composable:

| Class | Description |
|---|---|
| `Action` | Interface: `init()`, `loop()`, `isComplete()` |
| `SequentialAction` | Runs actions one after another |
| `SimultaneousAction` | Runs actions concurrently; completes when all finish. Supports `add(action, init, removeOld)` at runtime |
| `RunOnceAction` | Completes after one `loop()` tick |
| `CustomAction` | Wraps a `Runnable` as a `RunOnceAction` |
| `SleepAction` | Waits for a specified duration |
| `EmptyAction` | No-op; immediately complete |
| `MoveToAction` | Drives the robot to a target `FieldPosition` |
| `DcMotorToPositionAction` | Moves a `DcMotorEx` to an encoder target |
| `SetMotorPowerAction` | Sets motor power for one tick |
| `SetServoRotationAction` | Moves a servo to a target position |
| `ColorSensorDistanceAction` | Waits until a `ColorSensor` reads within a target distance |

### `controllers`
Closed-loop controllers that return a power output given an error signal.

| Class | Description |
|---|---|
| `Controller` | Interface: `getPower()`, `getError()` |
| `PIDController` | Standard PID with smoothed derivative (α = 0.01). Accepts either a current/target supplier pair or a raw error supplier |
| `SigmoidController` | Sigmoid-shaped response: `power × 2 × (sigmoid(error × steepness) − 0.5)`. Good for smooth deceleration |

### `gamepad`
Wraps the FTC `Gamepad` into typed input objects that track state across ticks.

`Gamepad` exposes every input as a typed field. Call `gamepad.loop()` each tick (done automatically by `TeleopBase`).

**Input types:**

| Class | Interfaces | Capabilities |
|---|---|---|
| `Button` | `GOnPress`, `GOnRelease`, `GWhileDown`, `GIsPressed`, `GOnToggle`, `GIsToggled` | Edge detection, toggle state, callbacks |
| `Trigger` | analog, threshold callbacks | Continuous value with pressed/released events |
| `Joystick` | x/y suppliers | Raw axis values with corrected Y polarity |

All input types support a fluent builder API:
```java
gamepad.aButton.onPress(() -> arm.open());
gamepad.bButton.onToggle(on -> claw.setPosition(on ? 1.0 : 0.0));
gamepad.leftTrigger.whileDown(() -> intake.run());
```

### `hardware`
Typed wrappers around FTC hardware that convert raw units to physical units.

| Class | Description |
|---|---|
| `Motor` | Wraps `DcMotorEx`. `setVelocity(in/s)` / `getVelocity()` convert between inches/second and encoder ticks/second using `ticksPerRotation` and `wheelDiameter`. Requires a `MotorConfig` or explicit parameters for velocity control |
| `MotorConfig` | Value class: `ticksPerRotation`, `wheelDiameter` |
| `PinpointModule` | Thin wrapper around the GoBilda Pinpoint odometry computer |

### `geometry`
Coordinate and vector primitives.

| Class | Description |
|---|---|
| `Point` | 2D `(x, y)` in inches |
| `FieldPosition` | `Point` + `direction` (radians CCW from +X). X+ toward audience, Y+ toward right |
| `MovementVector` | `(verticalVelocity, horizontalVelocity, rotationalVelocity)` in in/s and rad/s |
| `Angles` | Angle math utilities |

### `movement/mecanum`
Low-level mecanum drivetrain control.

`MecanumDriver` takes four `Motor` instances and a `MecanumCoefficientMatrix`. All motors are set to `BRAKE` on construction.

| Method | Description |
|---|---|
| `setRelativePower(MovementVector)` | Robot-centric power, normalized to [-1, 1] |
| `setRelativeVelocity(MovementVector)` | Robot-centric velocity in in/s |
| `setAbsolutePower(FieldPosition, MovementVector)` | Field-centric power — rotates the vector by the robot's heading |
| `setAbsoluteVelocity(FieldPosition, MovementVector)` | Field-centric velocity |

`MecanumCoefficientMatrix` computes per-wheel coefficients and handles direction inversion via a `MecanumCoefficientSet`.

### `pathing`
Localization interface and implementations. Separate from movement; localizers do not touch motors.

| Class | Description |
|---|---|
| `Localizer` | Interface: `init()`, `loop()`, `getCurrentPosition() → FieldPosition` |
| `PinpointLocalizer` | Implements `Localizer` using a GoBilda Pinpoint. Configures pod offsets (mm), directions, and encoder resolution on construction. Also exposes `getVelocity()`, `setCurrentFieldPosition()`, and `isDoneInitializing()` |

### `manipulators`
Base class for robot subsystems.

`Subsystem` extends `Loop` and adds `init(HardwareMap)` and an optional `stop()`. Register subsystems with `Robot.register()` — this calls `init` immediately and schedules `loop`/`stop` automatically.

### `sensors`
| Class | Description |
|---|---|
| `Encoder` | Interface for reading encoder position |
| `MotorEncoder` | Implements `Encoder` from a `DcMotorEx`; converts ticks to inches |
| `ColorSensor` | Wraps `RevColorSensorV3`. `getColor()` returns an RGB `Color` normalized to [0, 1]. `getDistance()` returns inches |

### `vision`
| Class | Description |
|---|---|
| `LimelightManager` | Wraps `Limelight3A`. `getVisibleAprilTags()` switches to pipeline 3 and returns a list of `AprilTagResult` (id, angular offsets, area %, robot-space pose). Pipeline switching waits for a new frame before returning |

---

## Adding a subsystem

1. Extend `Subsystem`.
2. Acquire hardware in `init(HardwareMap)`.
3. Run control logic in `loop()`.
4. Register in your op mode's `registerSubclasses()`:

```java
MySlides slides = robot.register(new MySlides());
```

## Coordinate system

- **X+** toward the audience viewing area  
- **Y+** toward the right side of the field  
- **direction = 0** facing X+ (toward audience)  
- Angles increase counterclockwise (radians)
