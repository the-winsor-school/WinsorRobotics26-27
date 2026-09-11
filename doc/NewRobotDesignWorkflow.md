# Designing a New Robot in This Codebase

This guide uses the Billy implementation as the reference example:

- [BillyRobot](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotModel/Robots/BillyRobot.java)
- [BillyMA](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotModel/Mechs/Assemblies/BillyMA.java)
- [BillyTeleOpBLUE](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/OpModes/BillyTeleOpBLUE.java)
- [BillyTeleOpRED](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/OpModes/BillyTeleOpRED.java)
- [ExampleStateMachineAuton](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/OpModes/ExampleStateMachineAuton.java)
- [BillyRapidFire](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/AutonStrategies/BillyRapidFire.java)
- [LimelightAutoTarget](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/AutonStrategies/LimelightAutoTarget.java)

Telemetry has its own two rules that every template below follows -- see
[TelemetryContract.md](TelemetryContract.md).

The main idea is simple:

Design the robot **top-down**, but implement it **bottom-up**.

- Top-down design means deciding what the robot must do, which subsystems exist, and which behaviors should belong at each layer.
- Bottom-up implementation means building the smallest hardware abstractions first, then combining them into larger subsystems, then wiring the finished subsystems into a robot and OpModes.

## Mental Model

In this project, a complete robot is built in layers:

| Layer | Responsibility | Billy Example |
|---|---|---|
| `OpMode` | FTC lifecycle, robot selection, start/stop, loop ownership | `BillyTeleOpBLUE`, `ExampleStateMachineAuton` |
| `Robot` | Wires together drive train, mech assembly, sensors, and robot-wide behaviors | `BillyRobot` |
| `DriveTrain` | Chassis movement and drive-specific autonomous functions | `MecanumDrive` |
| `MechAssembly` | Groups related mechanisms and resolves conflicts between them | `BillyMA` |
| `MechComponent` | Owns one mechanism and its hardware abstraction | `SpinnyIntake`, `PusherServo`, `Turret` |
| `Control Strategy` | Translates input or state into commands | component lambdas, `BillyRapidFire`, `LimelightAutoTarget` |
| `Autonomous Strategy` | Chooses the sequence of high-level robot actions | `ExampleStateMachineAuton`, `ExampleAutonomousStrategies` |

That creates a useful rule:

- `OpMode` should be thin.
- `Robot` should coordinate major subsystems.
- `Assembly` should coordinate multiple related components.
- `Component` should hide raw hardware details.
- Autonomous code should call **verbs**, not manipulate hardware directly.

## Order of Operations

### 1.  Define the robot before writing code

Start by answering these questions:

1. What game tasks must the robot perform?
2. What mechanisms are required to perform those tasks?
3. Which mechanisms should be independent components?
4. Which components should be grouped into one assembly?
5. What actions must exist in TeleOp?
6. What actions must exist in Autonomous?
7. Which sensors influence the whole robot, and which belong to a single mechanism?

For Billy, that decomposition looks roughly like this:

- Drive base: mecanum movement
- Mech assembly: intake, pusher, flywheel, turret
- Robot-wide sensors:  Limelight, IMU
- Robot-wide assistance behavior: automatic turret targeting

Do not start by writing the OpMode.  The OpMode is the last wiring layer, not the design center.

### 2.  Lock the hardware map and configuration names

Before coding, decide:

- motor names
- servo names
- sensor names
- drivetrain motor directions
- IMU orientation
- team or side specific constants

Billy shows this in the `BillyRobot` constructor:

- the drive motors are configured through `MecanumDrive.OrientationConfiguration`
- the Limelight and IMU are initialized once at robot construction time
- the alliance-specific tag target is passed in as a constructor parameter instead of duplicating robot code

This is an important pattern:  prefer **configuration through constructor arguments** over copying and pasting whole robot classes.

### 3.  Build one `MechComponent` per mechanism

A `MechComponent` should represent one focused piece of hardware behavior:

- one intake
- one turret
- one shooter
- one arm stage
- one claw

Each component should do four jobs:

1. Own the raw hardware handles.
2. Define a TeleOp control strategy interface.
3. Expose a small autonomous behavior API.
4. Report telemetry relevant to that component.

Billy component examples:

- `SpinnyIntake` wraps one motor and exposes `startIntake()`, `stopIntake()`, `reverseIntake()`
- `PusherServo` wraps the pusher servo and exposes `pushBalls()` and `retractPusher()`
- `Turret` wraps a CR servo and exposes turret movement verbs
- `DoubleShooter` wraps two motors and exposes shooter power control

### 4.  Put the driver input mapping in the component strategy

The key design pattern at the component layer is the **Strategy Pattern**.

Each component defines a control interface such as:

- `SpinnyIntakeControlStrategy`
- `BallPusherControlStrategy`
- `TurretControlStrategy`

Then the assembly supplies the actual strategy, often with a lambda.

That means the component is reusable because:

- the component knows hardware
- the strategy knows operator intent
- the assembly decides which strategy to use for this robot

This separation matters.  If you hard-code all gamepad logic inside the component, that component becomes much harder to reuse on another robot.

### 5.  Add local safeguards inside the component when needed

A component is also the right place for mechanism-specific safety logic.

The best example in this repo is `DoublyLimitedMotor`:

- it owns the motor
- it owns both limit switches
- it decorates `setPower()` so movement is blocked when a limit is pressed

This is the right abstraction boundary.  A caller should not need to remember the limit-switch rules every time it moves the arm.

Rule of thumb:

- If the rule protects one mechanism, put it in the component.
- If the rule coordinates multiple mechanisms, put it in the assembly or robot.

### 6.  Combine components into a `MechAssembly`

The `MechAssembly` layer is a **Composite** of components.

`BillyMA` is the reference pattern:

1. Construct each component, passing hardware alone.
2. Provide each component's TeleOp strategy.
3. In `initializeTelemetry()`, pass telemetry to **every** child, then build the
   autonomous aggregate from their `getAutonomousBehaviors()` results.  This is the
   two-phase pattern:  a child builds its autonomous object the moment it receives
   telemetry, so the aggregate can only be assembled after that.
4. In `giveInstructions()`, pass the gamepad to each child component.
5. In `updateTelemetry()`, visit **every** child.  Miss one and that mechanism silently
   disappears from the driver station.

This is where students should handle interactions between mechanisms.

Billy shows an important assembly-level behavior:

- `BillyRapidFire` is not a single component behavior.
- It coordinates flywheel and pusher actions together.
- `BillyMA` also blocks manual control while rapid fire is running.

That is exactly what assemblies are for:  coordinating multiple components and resolving control conflicts.

### 7.  Expose autonomous behaviors as verbs, not hardware

Every layer in this project has an autonomous-facing API:

- `DriveTrain.AutonomousDriving`
- `MechComponent.AutonomousComponentBehaviors`
- `MechAssembly.AutonomousMechBehaviors`
- `Robot.AutonomousRobot`

This creates a deliberate separation:

- TeleOp code deals with gamepads and continuous operator input.
- Autonomous code deals with high-level actions like `pushBalls()`, `setPower()`, `turnToAngle()`, or `drive(x, y, t)`.

This is one of the most important design ideas in the codebase.

When you create a new component, ask:

- What autonomous verbs should this mechanism expose?
- Which verbs are safe and meaningful?
- Which raw details should remain hidden?

Good autonomous API:

- `openClaw()`
- `closeClaw()`
- `startIntake()`
- `stopIntake()`
- `moveToStow()`
- `moveToScoreHeight()`

Poor autonomous API:

- `setServoTo0_41()`
- `setMotorPowerDirectlyWithoutBounds()`

Expose intention, not tuning noise.

### 8.  Create the robot class after the subsystems exist

The `Robot` layer is a facade over the drive train and mech assembly.

`BillyRobot` shows the expected structure:

1. Call `super(telemetry)` first, so `Robot` holds the reference before anything else runs.
2. Create the drive train.
3. Create the mech assembly.
4. Create robot-wide sensors and helpers.
5. Call `initializeSubsystems()`.  This propagates telemetry into the drive train and the
   mech assembly, which in turn propagate it to every component -- the moment the whole
   tree becomes usable.
6. Build a typed `AutonomousRobot` object from the subsystems' autonomous surfaces.
   This must come *after* step 5;  until then, `getAutonomousDriving()` and
   `getAutonomousBehaviors()` still return `null`.
7. Override `update()` only if the robot has robot-wide background behavior.

Billy uses `update()` for one robot-level behavior:

- manual drive and manual mech control happen in `super.update(gamepad1, gamepad2)`
- automatic turret tracking happens afterward through `targeter.updateState()`

This is the right place for cross-cutting coordination that does not belong to a single component or assembly.

### 9.  Keep TeleOp OpModes thin

The TeleOp OpModes for Billy are intentionally small:

1. Construct the robot in `runOpMode()`
2. Report initialization telemetry
3. Wait for start
4. Loop on `robot.update(...)`
5. Loop on `robot.updateTelemetry()`

That is a strong pattern to preserve.

If an OpMode starts accumulating mechanism logic, it usually means the design has leaked past the proper abstraction boundary.

The `BillyTeleOpRED` and `BillyTeleOpBLUE` difference is also instructive:

- the robot class stays the same
- only the target tag ID changes

That is a clean use of parameterization.

### 10.  Implement autonomous strategies on top of the autonomous APIs

Once the robot exposes autonomous behaviors cleanly, strategy code becomes much easier to write.

The repo currently shows two autonomous styles:

| Style | Examples | Notes |
|---|---|---|
| One-shot strategy implementing `IAutonStrategy` | `ExampleStateMachineAuton`, `ExampleAutonomousStrategies` | good for full autonomous routines |
| Reusable state machine using `StateMachine` + `IState` | `BillyRapidFire`, `LimelightAutoTarget` | better for non-blocking or concurrent behaviors |

Recommended guidance for students:

- Use `IAutonStrategy` when you need one complete autonomous routine.
- Use `StateMachine` when a behavior should update over time and coexist with other logic.
- Prefer the `StateMachine` pattern for anything that must remain responsive while the OpMode loop continues.

This distinction matters because Billy uses state machines in two different places:

- `BillyRapidFire` runs inside TeleOp as an assembly-level sequence.
- `LimelightAutoTarget` runs as a robot-level background targeting behavior.

That shows that "control strategy" exists at multiple layers, not just at the gamepad-to-motor layer.

### 11.  Test from the bottom up

Do not test everything at once.  Test in this order:

1. Individual component hardware direction and limits
2. Component TeleOp control strategy
3. Component autonomous verbs
4. Assembly coordination behavior
5. Robot construction and sensor initialization
6. TeleOp operator flow
7. Autonomous strategy behavior

A fast way to debug is to ask, "Which layer is failing?"

- If the motor spins the wrong way, that is a component or configuration problem.
- If two mechanisms fight each other, that is usually an assembly problem.
- If the robot uses the wrong autonomous routine or tag, that is usually an OpMode or robot-construction problem.

## Control Strategies at Different Abstraction Levels

The phrase "control strategy" means different things at different layers.  Students should use the right layer for the right kind of decision.

| Layer | Typical input | Typical output | Billy example |
|---|---|---|---|
| Component strategy | `Gamepad` buttons/sticks | actuator power or position | intake, pusher, turret lambdas in `BillyMA` |
| Component safety logic | sensor state | filtered actuator command | `DoublyLimitedMotor.setPower()` |
| Assembly strategy | multiple components + operator state | coordinated subsystem behavior | `BillyRapidFire` |
| Robot strategy | multiple subsystems + robot sensors | whole-robot assistance behavior | `LimelightAutoTarget` in `BillyRobot.update()` |
| Autonomous strategy | field objectives + time + sensors | complete routine sequencing | `ExampleStateMachineAuton` |

Use this as a sorting rule:

- One mechanism only: component
- Several mechanisms working together: assembly
- Chassis plus mechanisms plus sensors: robot or autonomous strategy

## Recommended Build Sequence for a New Robot

If students are making a brand-new design, the file creation order should usually be:

1. `RobotModel/Mechs/Components/...`
2. `RobotModel/Mechs/Assemblies/...`
3. `RobotModel/DriveTrain/...` if a new drive style is needed
4. `RobotModel/Robots/...`
5. `AutonStrategies/...`
6. `OpModes/...`

That order matches dependency flow:

- components do not depend on assemblies
- assemblies depend on components
- robots depend on drive trains and assemblies
- strategies depend on robot autonomous APIs
- OpModes depend on finished robots and strategies

## Minimal Templates

### New Component Template

```java
public class NewComponent extends MechComponent {
    public class AutonomousNewComponent extends AutonomousComponentBehaviors {
        public AutonomousNewComponent(Telemetry telemetry) { super(telemetry); }
        public void stow()  { /* command hardware */ reportStatus("New: stowed"); }
        public void score() { /* command hardware */ reportStatus("New: scoring"); }
    }

    public interface NewComponentControlStrategy extends IControlStrategy {
        void move(/* hardware */, Gamepad gamepad);
    }

    private final NewComponentControlStrategy strategy;

    // Built in initializeTelemetry -- see doc/TelemetryContract.md.
    private AutonomousNewComponent auton;

    public NewComponent(HardwareMap hardwareMap, NewComponentControlStrategy strategy) {
        super(strategy);
        this.strategy = strategy;
        // get hardware here -- hardware alone, on purpose
    }

    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousNewComponent(telemetry);
    }

    @Override
    public AutonomousNewComponent getAutonomousBehaviors() {
        return auton;
    }

    @Override
    public void move(Gamepad gamepad) {
        // call strategy here
    }

    @Override
    public void update() {
        // write to the buffer with telemetry.addData(...);  Robot does the flushing
    }
}
```

### New Assembly Template

```java
public class NewRobotMA extends MechAssembly {
    private final NewComponent arm;
    private final NewComponent intake;

    public class AutonomousNewRobotMA extends AutonomousMechBehaviors {
        public final NewComponent.AutonomousNewComponent arm;
        public final NewComponent.AutonomousNewComponent intake;

        public AutonomousNewRobotMA(
                NewComponent.AutonomousNewComponent arm,
                NewComponent.AutonomousNewComponent intake,
                Telemetry telemetry) {
            super(telemetry);
            this.arm = arm;
            this.intake = intake;
        }
    }

    private AutonomousNewRobotMA auton;

    // Components are constructed with hardware alone.
    public NewRobotMA(HardwareMap hardwareMap) {
        arm = new NewComponent(hardwareMap, (hardware, gamepad) -> { });
        intake = new NewComponent(hardwareMap, (hardware, gamepad) -> { });
    }

    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
        // Every child.  Skip one and it stays silently unwired.
        arm.initializeTelemetry(telemetry);
        intake.initializeTelemetry(telemetry);
        // Only now can the children's autonomous objects be gathered.
        auton = new AutonomousNewRobotMA(
                arm.getAutonomousBehaviors(),
                intake.getAutonomousBehaviors(),
                telemetry);
    }

    @Override
    public AutonomousNewRobotMA getAutonomousBehaviors() {
        return auton;
    }

    @Override
    public void giveInstructions(Gamepad gamepad) {
        arm.move(gamepad);
        intake.move(gamepad);
    }

    @Override
    public void updateTelemetry() {
        // Visit every component.  Miss one and it vanishes from the driver station.
        arm.update();
        intake.update();
    }
}
```

### New Robot Template

```java
public class NewRobot extends Robot {
    public class AutonomousNewRobot extends AutonomousRobot {
        public final MecanumDrive.AutonomousMecanumDrive driveTrain;
        public final NewRobotMA.AutonomousNewRobotMA mechAssembly;

        public AutonomousNewRobot(
                MecanumDrive.AutonomousMecanumDrive driveTrain,
                NewRobotMA.AutonomousNewRobotMA mechAssembly,
                Telemetry telemetry) {
            super(driveTrain, mechAssembly, telemetry);
            this.driveTrain = driveTrain;
            this.mechAssembly = mechAssembly;
        }
    }

    private final AutonomousNewRobot auton;

    public NewRobot(HardwareMap hardwareMap, Telemetry telemetry) {
        super(telemetry);                    // Robot stores the reference first

        driveTrain = new MecanumDrive(hardwareMap, /* orientation config */);
        mechAssembly = new NewRobotMA(hardwareMap);
        // ...robot-wide sensors (IMU, Limelight) go here...

        initializeSubsystems();              // propagates telemetry down both branches

        // Only after initializeSubsystems() do the subsystems' autonomous
        // objects exist to be gathered.
        auton = new AutonomousNewRobot(
                driveTrain.getAutonomousDriving(),
                mechAssembly.getAutonomousBehaviors(),
                telemetry);
    }

    @Override
    public AutonomousNewRobot getAutonomousRobot() {
        return auton;
    }
}
```

### Thin TeleOp OpMode Template

```java
@TeleOp(name = "New Robot TeleOp")
public class NewRobotTeleOp extends LinearOpMode {
    private Robot robot;

    @Override
    public void runOpMode() throws InterruptedException {
        robot = new NewRobot(hardwareMap, telemetry);

        telemetry.addLine("Robot initialized");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            robot.update(gamepad1, gamepad2);
            robot.updateTelemetry();   // the only telemetry.update() in the loop
        }
    }
}
```

### Autonomous Strategy Template

```java
public class NewAutonStrategy {
    public static IAutonStrategy run(NewRobot robot, LinearOpMode opMode) {
        return () -> {
            IState state = firstStep(robot);

            while (opMode.opModeIsActive() && state != null) {
                state = state.execute();
                opMode.idle();
            }
        };
    }

    private static IState firstStep(NewRobot robot) {
        return () -> {
            robot.getAutonomousRobot().mechAssembly.arm.stow();
            return secondStep(robot);
        };
    }

    private static IState secondStep(NewRobot robot) {
        return () -> {
            robot.getAutonomousRobot().driveTrain.drive(0, 0.5, 0);
            return null;
        };
    }
}
```

## Design Patterns in Use

These are the recurring software design patterns students should recognize in this codebase:

### Strategy Pattern

Used when a component defines an interface and the assembly supplies the actual behavior.

Examples:

- intake gamepad mapping
- pusher servo mapping
- turret manual control mapping

### Composite Pattern

Used when multiple components are grouped into one higher-level subsystem.

Examples:

- `BillyMA`
- `CascadeArm`
- `ExampleIntakeAssembly`

### Facade Pattern

Used when the `Robot` class presents a simplified surface over drive, mechanisms, and sensors.

Example:

- `BillyRobot`

### Dependency Injection

Used when dependencies are passed through constructors rather than created ad hoc inside methods.

Examples:

- `HardwareMap`
- `Telemetry`
- target tag ID
- drivetrain orientation configuration

### State Machine Pattern

Used when a behavior must advance over time without losing structure.

Examples:

- `BillyRapidFire`
- `LimelightAutoTarget`
- `StateMachine` + `IState`

## Student Checklist

Before calling the robot "done," students should be able to answer yes to all of these:

- Does every physical mechanism have a clear software owner?
- Does each component hide raw hardware details behind meaningful methods?
- Does each component expose autonomous verbs that describe intent?
- Does the assembly coordinate mechanism conflicts in one place?
- Is the robot class responsible only for robot-wide composition and helpers?
- Is the OpMode thin and focused on lifecycle?
- Does autonomous code use subsystem verbs instead of direct hardware calls?
- Can each layer be tested separately?

If the answer is "no" on several of these, the robot design is probably missing an abstraction boundary.

## Final Recommendation

When building a new robot in this project, students should think in two passes:

1. **Design pass:**  decide subsystems, responsibilities, operator actions, and autonomous verbs.
2. **Implementation pass:**  build components first, then assemblies, then robot wiring, then OpModes and strategies.

Billy is useful because it demonstrates all of the key layers:

- thin OpModes
- a composed robot
- a multi-part mech assembly
- reusable component abstractions
- autonomous behavior wrappers
- higher-level state-machine strategies

If students preserve those boundaries, the next robot design will be easier to test, easier to extend, and much easier to reason about during competition.
