# 2026-27 Migration Notes

This repo starts from a clean FTC SDK **v11.2.1** clone.  The `TeamCode` module was carried
over from `WinsorRobotics25-26` (branch `2027-season-prep`), keeping the reusable robot
object model and a curated set of examples, and dropping last season's game-specific code.

## What came over

**The framework** -- the four abstract layers everything else is built on, plus the
static helpers:

- `RobotModel/Robots/Robot.java`, `RobotModel/DriveTrain/DriveTrain.java`,
  `RobotModel/Mechs/Assemblies/MechAssembly.java`,
  `RobotModel/Mechs/Components/MechComponent.java`
- `Extensions/` -- `GamepadExtensions` (dead zones, Y inversion), `AngleExtensions`
  (IMU angle math), `LimelightExtensions` (AprilTag lookup), `ThreadExtensions`,
  `IState`, `TurnDirection`

**Drive trains** -- all three, so there's a worked example at each level of complexity:

| Class | Why it's here |
|---|---|
| `Tank/StandardTankDrive` | Simplest possible `DriveTrain`.  Two motors, two sticks.  Start here. |
| `Tank/OneStickTank` | Arcade mixing math, plus a non-blocking timed state (`LeroyState`). |
| `Mecanum/MecanumDrive` | Full holonomic drive, IMU-based `turnToAngle`, injected motor-direction config. |

**Components** -- every one is generic and reusable; the robot-specific behavior is
injected as a strategy lambda:

`SpinnyIntake`, `PusherServo`, `Turret`, `DoubleShooter`, `DoublyLimitedMotor`
(limit-switch safety), `Claw`, `BallDetectionComponent` (OpenCV color blobs).

**Assemblies** -- `BillyMA` (four components + an embedded state machine),
`CascadeArm` (two limited motors + claw), `ExampleIntakeAssembly` (the minimal one).

**Robots** -- `BillyRobot` (the full example:  mecanum + Limelight + IMU + auton surface),
`Wildbots2025` (mecanum + cascade arm), `StandardTankRobot` and `OneStickTankRobot`
(minimal, teleop-only).

**Autonomous** -- `StateMachine` and its `doAndWait` helper, `BillyRapidFire` and
`LimelightAutoTarget` as the two worked state machines, `ExampleAutonomousStrategies`
as the simplest possible `IAutonStrategy` lambda.

**Docs** -- `AbstractRobotObjectModel.md`, `NewRobotDesignWorkflow.md`,
`ControlStrategyExpansionPlan.md`, and all ten `FlintLessons/`.  Last season's project
assignment and student work moved to `PastProjects/2025-2026/` -- the `@author Susan Zuo`
Javadoc throughout the object model refers to the bug numbers in
`PastProjects/2025-2026/TelemetryRefactorProjectAssignment.md`.

[`TelemetryContract.md`](TelemetryContract.md) is **new**, written during this migration.
The telemetry rules were previously documented only as last season's *project
assignment*, so the only account of them lived in a historical document.  It covers the
single-flush rule, two-phase initialization, the propagation chain, and a checklist for
new code.  The rest of the docs were also brought back in sync with the code -- before
this pass, every template in `NewRobotDesignWorkflow.md` still used the pre-refactor
`updateTelemetry(Telemetry)` signatures, which the compiler would have rejected.

## What stayed behind

All of it is still in `WinsorRobotics25-26` under `Archive/`, with full git history
(`git log --follow`).  Every file is still there, history intact.

- **Season-specific autonomous** -- `ATagL1Strategy`, `ATagL2Strategy`, and the four
  `L1/L2Auton{RED,BLUE}` OpModes.  Hardcoded to last season's field and AprilTag layout, so they belong to that game alone.  (`Archive/PastSeasons/2025-2026/`)
- **Dead stubs and prototypes** -- `CloseStrategy` (empty), `FarStrategy`,
  `BasicApriltagTracking`, and `lime.java` (an unmodified vendor sample that had been
  copied into `TeamCode`).  (`Archive/Deprecated/`)
- **`AppleRobot`** -- an incomplete practice robot; a drive train and a to-do comment.
  `StandardTankRobot` covers the same "minimal robot" ground and actually works.
- **`BillyExampleAuton`** -- its constructor calls `new BillyRobot(hardwareMap, ...)`
  before `hardwareMap` exists, and a field initializer dereferences `robot` before it's
  assigned.  It would `NullPointerException` on init.  `ExampleStateMachineAuton` (below)
  teaches the same thing correctly.

## Changes made during the migration

- **`MechComponent.update()` and `move()` are now `public`** (they were package-private).
  Assemblies live in a different package than components, so `CascadeArm` and
  `ExampleIntakeAssembly` -- the compiler rejected them outright, which is why they'd been shelved.
- **`TeleOp.java` → `ExampleTeleOp.java`.**  The old class was named `TeleOp`, colliding
  with the `@TeleOp` annotation and forcing a fully-qualified annotation to work around it.
- **`SecondExampleAuton.java` → `ExampleStateMachineAuton.java`.**  "Second" referred to a
  first example that stayed behind.
- **`ExampleTankTeleOp.java` is new** -- it gives the tank drive trains a registered
  OpMode, so the Driver Station finally lists them.
- **Hardcoded AprilTag IDs replaced with `TARGET_TAG_ID = -1` constants** carrying a TODO,
  in the three OpModes that had them.  These need this season's real IDs before use.
- **Unused imports removed** across the ported files.

Everything compiles against SDK 11.2.1 (`./gradlew :TeamCode:compileDebugJavaWithJavac`).

## Known bugs left in place, on purpose

The code carries `TODO` comments marking real defects that are still open -- turret ownership
conflicts, sign errors in the mecanum teleop mix and the turret tracker, an intake
if/else chain that leaves the motor at zero, `BillyMA.updateTelemetry()` standing
empty, `StateMachine.abort()` leaving a stale timer.  They're described in place, with
enough context to work out the fix.  Search the project for `TODO` to find them.
