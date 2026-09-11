# The Telemetry Contract

Telemetry is the driver station display -- the text that tells a driver what the robot
thinks is happening.  Getting it wrong leaves the robot running fine and the driver
blind in the middle of a match, which is arguably worse than a crash.

This codebase has **two hard rules** about telemetry.  They are short, and every class
in `RobotModel` obeys them.

> **Rule 1 -- Single flush point.**  Only `Robot.updateTelemetry()` may call
> `telemetry.update()`.  One method, one call site, once per loop.
>
> **Rule 2 -- Two-phase initialization.**  A subsystem is constructed with hardware,
> then wired with `Telemetry` afterward, through `initializeTelemetry(Telemetry)`.

Everything below explains why.

## Rule 1:  only `Robot` flushes

FTC's `Telemetry` object works like a buffer with a send button:

- `telemetry.addLine(...)` and `telemetry.addData(...)` **write into the buffer**
- `telemetry.update()` **sends the buffer to the driver station and clears it**

So every layer of the robot writes freely, and exactly one place presses send:

```java
// Robot.java -- the only telemetry.update() in the entire codebase
public void updateTelemetry() {
    if (driveTrain != null)   driveTrain.updateTelemetry();     // writes
    if (mechAssembly != null) mechAssembly.updateTelemetry();   // writes
    telemetry.update();                                         // sends, once
}
```

**Why it matters.**  A component that flushes on its own sends a half-filled buffer and
clears it, so whatever wrote *after* it that loop lands in the next frame.  The driver
gets a display that flickers between partial snapshots, with values one loop stale and
an ordering that looks arbitrary.  That was a real bug on this team's robot, and it is
genuinely nasty to diagnose, because every individual class looks correct in isolation.

The rule buys predictable ordering too:  drive train first, then assembly, then whatever
the OpMode added, all in one atomic frame.

**The one legitimate exception** is an OpMode before the main loop starts:

```java
robot = new BillyRobot(hardwareMap, telemetry, TARGET_TAG_ID);
telemetry.addLine("Billy Initialized");
telemetry.update();          // fine -- the loop has yet to begin
waitForStart();
while (opModeIsActive()) {
    robot.update(gamepad1, gamepad2);
    robot.updateTelemetry();  // from here on, this is the only flush
}
```

## Rule 2:  two-phase initialization

Subsystems are **constructed first, wired with telemetry second**:

```java
// Phase 1 -- construction.  Hardware only.
intake = new SpinnyIntake(hardwareMap, "intakeMotor", (motor, gamepad) -> { ... });

// Phase 2 -- initializeTelemetry, called later by the layer above.
@Override
public void initializeTelemetry(Telemetry telemetry) {
    super.initializeTelemetry(telemetry);            // store the reference
    auton = new AutonomousIntakeBehaviors(telemetry); // NOW build the auton object
}
```

**Why keep it out of the constructor?**  Because of what has to be built *from* it.
Each layer's `Autonomous*Behaviors` object holds its own `Telemetry` reference, which is
how autonomous code calls `reportStatus(...)` and `reportData(...)` while handling only
the object model.  Those behavior objects can come into being only once telemetry
exists.  Write them as field initializers and they run ahead of every constructor body
and capture `null`.  That was the original bug:  `AutonomousMecanumDrive` was a `final`
field initializer, so its telemetry reference stayed null and every report it made was
thrown away.

Splitting construction from wiring gives every layer one obvious place to build its
autonomous object, with a reference guaranteed to be real.

### The propagation chain

`initializeTelemetry` is a relay.  Each layer stores its reference, passes it to every
child, and only then builds its own aggregate:

```
Robot constructor
  └─ initializeSubsystems()
       ├─ driveTrain.initializeTelemetry(telemetry)
       │    └─ builds AutonomousMecanumDrive
       └─ mechAssembly.initializeTelemetry(telemetry)
            ├─ intake.initializeTelemetry(telemetry)      ─┐
            ├─ ballPusher.initializeTelemetry(telemetry)   │ each builds its own
            ├─ flywheel.initializeTelemetry(telemetry)     │ Autonomous*Behaviors
            ├─ turret.initializeTelemetry(telemetry)      ─┘
            └─ builds AutonomousBillyMA from all four
```

**The cost of this design, stated plainly:**  two-phase initialization lets an object
exist in a half-built state.  Where an assembly's `initializeTelemetry` skips one child
component, that component's `telemetry` stays null and its autonomous object goes
missing.  The code compiles, the robot boots, and the first sign of trouble is an NPE or
a driver asking why one mechanism has gone quiet.  That is the trade the team accepted;
the defense is that `initializeTelemetry` is short enough to read and count.

## Who calls what

| Layer | Role in the frame | Receives telemetry via |
|---|---|---|
| `OpMode` | writes before the loop, and flushes there | it owns the original |
| `Robot` | gathers from both subsystems, then flushes -- the single flush point | constructor |
| `DriveTrain` | writes in `updateTelemetry()` | `initializeTelemetry()` |
| `MechAssembly` | writes in `updateTelemetry()`, visiting every child | `initializeTelemetry()` |
| `MechComponent` | writes in `update()` | `initializeTelemetry()` |
| `Autonomous*Behaviors` | writes via `reportStatus` / `reportData` | its constructor |
| State machines | write through the behaviors object they were handed | they hold none of their own |

That last row is worth dwelling on.  `BillyRapidFire` and `LimelightAutoTarget` report
*through* the autonomous behaviors object they were given:

```java
mechAssembly.reportStatus("startShooter");   // BillyRapidFire
turret.reportData("Turret CCW", power);      // LimelightAutoTarget
```

A strategy holding raw telemetry can flush, and sooner or later one will.  Routing every
report through the object model makes the rule structural:  the architecture enforces it,
so you can forget it and stay correct.

## Assemblies must visit every child

`MechAssembly.updateTelemetry()` is a checklist:

```java
@Override
public void updateTelemetry() {
    telemetry.addLine("--- Cascade Arm ---");
    claw.update();
    cascade.update();
    drawbridge.update();
}
```

Miss one line and that mechanism vanishes from the driver station while the build stays
green and the logs stay quiet.  You are the only one who knows how many components the
assembly owns, so the framework leaves this check to you.  **You are the check.**

This is also why adding a component to an assembly means editing *two* methods:
`initializeTelemetry` to wire it, and `updateTelemetry` to report it.  Forgetting the
second is the single most common mistake in this codebase.

> `BillyMA.updateTelemetry()` currently stands empty, so all four of Billy's mechanisms
> report silence.  It is marked with a `TODO` and left open on purpose;  see Flint
> Lesson 4.

## Checklist for new code

When you add a component, assembly, or robot, verify:

- [ ] The constructor takes hardware only.
- [ ] `initializeTelemetry` calls `super.initializeTelemetry(telemetry)` (components) or
      assigns `this.telemetry` (assemblies and drive trains).
- [ ] `initializeTelemetry` passes telemetry to **every** child, then builds the
      autonomous object.
- [ ] `updateTelemetry()` / `update()` visits **every** child and writes something useful.
- [ ] Every telemetry call in the file is `addLine` or `addData`.
- [ ] The `telemetry` field comes from the base class alone.  Redeclaring it shadows the
      inherited one.
- [ ] Autonomous verbs report through `reportStatus` / `reportData`.

## History

This architecture came out of a 2025-26 student refactor project by Susan Zuo, which
identified seven distinct telemetry bugs and the two rules that resolve them.  Much of
the `@author Susan Zuo` Javadoc across `RobotModel` refers to those numbered bugs, and
the original writeup is preserved at
[PastProjects/2025-2026/TelemetryRefactorProjectAssignment.md](PastProjects/2025-2026/TelemetryRefactorProjectAssignment.md).

Read this document for what the rules *are*.  Read that one for the case study of what
the code looked like before they existed.
