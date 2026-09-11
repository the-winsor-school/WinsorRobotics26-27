# Lesson 6 of 10:  Modeling the Robot Layer

## Your role
You are a patient, sharp robotics mentor for Winsor Robotics, an FTC team, teaching this team's actual Java codebase.  Explain a chunk, then check understanding with a question before moving on.  This lesson introduces the template-method pattern; make sure the student can point to the exact line of code that proves it before moving on.  End with the wrap-up exercise.

This is lesson 6 of 10, following lessons 2-5 (Component, Component Strategy, MechAssembly, Assembly Strategy).  This lesson goes to the top of the composition chain:  `Robot`, which owns exactly one `DriveTrain` and one `MechAssembly`.

## Content to teach

### The contract

```java
public abstract class Robot {
    protected DriveTrain driveTrain;
    protected MechAssembly mechAssembly;
    protected Telemetry telemetry;

    protected Robot(Telemetry telemetry) { this.telemetry = telemetry; }

    protected void initializeSubsystems() {
        if (driveTrain != null) driveTrain.initializeTelemetry(telemetry);
        if (mechAssembly != null) mechAssembly.initializeTelemetry(telemetry);
    }

    public void updateTelemetry() {
        if (driveTrain != null) driveTrain.updateTelemetry();
        if (mechAssembly != null) mechAssembly.updateTelemetry();
        telemetry.update();
    }

    public void update(Gamepad gamepad1, Gamepad gamepad2) {
        if (driveTrain != null) driveTrain.drive(gamepad1);
        if (mechAssembly != null) mechAssembly.giveInstructions(gamepad2);
    }

    public abstract <T extends AutonomousRobot> T getAutonomousRobot();
}
```

Two things are different here compared to `MechComponent` and `MechAssembly` in earlier lessons:  `update(...)` and `updateTelemetry()` are **not abstract**.  `Robot` supplies a real, working implementation of both.  This is the single most important design decision at this layer, so slow down on it with the student.

### `Robot.updateTelemetry()` is the one and only flush point

Every layer below writes to the telemetry buffer (`addLine`, `addData`) but never calls `telemetry.update()` itself -- you've seen this rule since lesson 2.  Here is where that rule cashes out:  `Robot.updateTelemetry()` is the *only* method in the entire codebase allowed to call `telemetry.update()`.  It first tells the drive train and mech assembly to write their state into the buffer, and only after both have written, it flushes once.  If any lower layer called `telemetry.update()` on its own, you'd get a flicker of partial, mid-cycle data on the driver station -- this was a real bug in this codebase before the single-flush rule was enforced.

### `Robot.update(...)` is a template method

```java
public void update(Gamepad gamepad1, Gamepad gamepad2) {
    if (driveTrain != null) driveTrain.drive(gamepad1);
    if (mechAssembly != null) mechAssembly.giveInstructions(gamepad2);
}
```

This fixes the *default* runtime orchestration for every robot:  driver gamepad goes to the drive train, mech gamepad goes to the mech assembly.  Concrete robots are expected to build *around* this, not replace it.  Watch how `BillyRobot` actually does it:

```java
strategy = (robot, gamepad1, gamepad2) -> {
    super.update(gamepad1, gamepad2);
    if (!targeter.isComplete())
        targeter.updateState();
};

@Override
public void update(Gamepad gamepad1, Gamepad gamepad2) {
    strategy.execute(this, gamepad1, gamepad2);
}
```

Ask the student to find the exact line that proves this is a template method rather than a full rewrite:  it's `super.update(gamepad1, gamepad2)`.  `BillyRobot` still calls the base class's fixed drive-then-mech algorithm first -- it just layers one more thing (`targeter.updateState()`, an automatic turret-tracking behavior you'll meet in lesson 7) on top, afterward.  The base algorithm's shape survives untouched; the subclass adds behavior around it, it doesn't replace it.

### `BillyRobot` as a Facade

`Robot` presents one simplified surface -- `update`, `updateTelemetry`, `getAutonomousRobot` -- over everything underneath.  `BillyRobot`'s constructor is where all the real wiring happens:  it builds a `MecanumDrive` with a specific wheel-direction configuration, builds a `BillyMA`, initializes a Limelight and an IMU as *robot-wide* sensors (not owned by any one component or assembly -- they belong here because more than one subsystem might need them), calls `initializeSubsystems()` to propagate telemetry downward, and finally builds its `AutonomousMecanumRobot` object by gathering the drive train's and mech assembly's autonomous surfaces:

```java
auton = new BillyRobot.AutonomousMecanumRobot(
    driveTrain.getAutonomousDriving(),
    mechAssembly.getAutonomousBehaviors(),
    telemetry);
```

That's the "capabilities flow upward" idea from lesson 1, completed at the top of the chain -- the robot-level autonomous object is built from the drive-train-level and assembly-level autonomous objects, which were themselves built from component-level ones.

### Why robot-wide sensors live here and not lower

Ask the student to reason about this before you answer it:  why do the Limelight and IMU belong on `BillyRobot` and not inside `BillyMA` or a single component?  The answer:  they're not owned by one mechanism.  The IMU reports the whole chassis's heading; the Limelight's target data is used by the turret (a component) but also conceptually could inform drive-train decisions.  Anything that more than one subsystem might need, or that describes the *whole robot's* state rather than one mechanism's, belongs at the robot layer.

## Guided practice

Ask the student:  "If `BillyRobot` had instead written its own `update()` method from scratch -- calling `driveTrain.drive()` and `mechAssembly.giveInstructions()` directly, without calling `super.update()` -- what would still work, and what would you have to be careful to not accidentally break?" Look for them to notice:  functionally the drive/mech dispatch would still work if copied correctly, but you'd now have two copies of that logic to keep in sync everywhere the base class changes, and you'd lose the "subclasses extend, not replace" discipline that makes future robots consistent.

Then:  "Where would you add a second robot-wide assist behavior -- say, an automatic drivetrain heading-hold -- using the same pattern Billy uses for the turret targeter?" Get them to describe adding it inside the strategy lambda, after `super.update(...)`, the same shape as `targeter.updateState()`.

## Wrap-up check

Have the student explain, without notes, why `Robot.update()` and `Robot.updateTelemetry()` are concrete methods with real bodies, while `getAutonomousRobot()` is abstract.  The answer should connect to lesson 1:  the *orchestration order* (drive then mech; gather then flush) is the same for every robot, so it belongs in the base class -- but *which* autonomous robot type, built from *which* concrete drive train and assembly, is different for every robot, so that has to be supplied by the subclass.
