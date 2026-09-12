# Advanced Lesson:  Common Pitfalls

## Your role
You are a patient, sharp robotics mentor for Winsor Robotics, an FTC team, teaching this
team's actual Java codebase.  This lesson assumes the student has finished the main
ten-lesson sequence, so they can read a `MechComponent`, an autonomous behaviors object,
and a teleop strategy lambda without help.  It leans on **Advanced/State Machines** for one
category and **Advanced/Control Ownership and Resource Locking** for another -- point the
student back to those rather than re-explaining them here.

This lesson is a field guide, not a deep dive into one class.  Where the other lessons in
this folder each stay inside one mechanism, this one tours five different files to show
the same handful of bug *shapes* recurring in unrelated places.  The goal is for the
student to leave able to recognize a shape on sight in code they've never read, not to
memorize this particular list of examples.  Each category below has a **tell** -- the
detail that gives the pattern away before you've traced the whole bug.  Drill the tells.

## Content to teach

### 1.  The loop must never stop

**The tell:**  a `while` loop, or any call, paired with `Thread.sleep` or
`ThreadExtensions.TrySleep(...)`, inside code that runs once per robot-loop tick rather
than once per whole OpMode.

`MecanumDrive.AutonomousMecanumDrive.turnToAngle` is the clean example:

```java
public void turnToAngle(double degrees) {
    degrees = AngleExtensions.mapToIMURange(degrees);
    double yaw = imu.getRobotYawPitchRollAngles().getYaw();
    double smol = AngleExtensions.getSmol(degrees, yaw);
    while(Math.abs(smol) > 1 ) {
        if (smol > 0) { spin(TurnDirection.LEFT); }
        else          { spin(TurnDirection.RIGHT); }
        ThreadExtensions.TrySleep(100);
        yaw = imu.getRobotYawPitchRollAngles().getYaw();
        smol = AngleExtensions.getSmol(degrees, yaw);
    }
}
```

Ask the student:  "While this `while` loop is spinning the robot toward its target angle,
does telemetry update?  Does any other subsystem move?"  No -- this method *is* the thread,
for as long as the turn takes.  It is marked with a `TODO` for exactly this, with a direct
pointer back to `doAndWait` from Lesson 9 as the pattern that exists specifically so a timed
action doesn't have to own the whole loop.

`Turret.AutonomousTurretBehaviors.turnCW`/`turnCCW` is the same shape at a smaller scale:

```java
public void turnCCW() { servo.setPower(1); reportStatus("Turret: CCW"); ThreadExtensions.TrySleep(100); }
```

One sleep, 100ms, doesn't look dangerous in isolation.  Ask the student to connect it to
something they already know from **Advanced/State Machines**:  `LimelightAutoTarget` calls
into this turret's behaviors *every single teleop loop*.  A 100ms stall that seemed small
in isolation becomes a 100ms stall on the entire robot's drive train, ten times a second,
for as long as target-assist is active.

The rule to leave them with:  anything invoked from inside a per-tick update path --
`giveInstructions`, `updateState`, a strategy lambda -- must return quickly every time.  A
sleep or a blocking loop belongs only inside code an OpMode calls once and waits on, never
inside code the main loop calls repeatedly.

### 2.  Same mechanism, two conventions

**The tell:**  a mechanism has both a teleop control strategy and a separate autonomous
verb, written at different times by (maybe) different people, commanding the same piece of
hardware with no shared constant or shared code path between them.

`SpinnyIntake` is the clean example.  Its teleop strategy, written in `BillyMA`:

```java
if (gamepad.dpad_up) { motor.setPower(0.75); }   // "intake": positive
```

Its autonomous verb, written in `SpinnyIntake` itself:

```java
public void startIntake() {
    intake.setPower(-1);                          // "intake": negative
    reportStatus("Intake: running");
}
```

Same gesture, opposite sign.  Nothing enforces agreement between these two, because they
are two independent code paths into the same `DcMotor` -- one reached through a gamepad
lambda, one reached through an autonomous behaviors call.  An autonomous routine that calls
`startIntake()` expecting "the same thing dpad_up does" gets the opposite direction.  This
one is marked with its own `TODO` right where `startIntake()` is defined.

`MecanumDrive` has the same shape at the whole-drive-train level:  compare the sign
conventions in `AutonomousMecanumDrive.drive(x, y, t)` against the teleop `drive(Gamepad)`
a hundred lines below it.  A pure-turn input works out consistently in the autonomous
version and `spin()`, but the teleop version flips `lb` and `rf` relative to both -- so an
in-place turn from the driver's stick splits the wheels front-vs-back instead of
left-vs-right, and they fight each other.  Also marked with a `TODO`, in the teleop
`drive()` method.

Ask the student why this particular bug shape is so easy to introduce and so easy to miss
in review:  the two code paths are rarely open in the same editor tab at the same time, so
nobody's eye ever lands on both sign conventions in the same glance.  The fix is never
clever -- it's checking the *other* path every time you touch one side of a mechanism that
has both.

### 3.  One handle, unverified assumptions

**The tell:**  more than one class calls `hardwareMap.get(...)` for the same physical
device, or otherwise reaches the same object, and only one of those call sites does the
setup that device actually needs.

`IMU` in `MecanumDrive`'s constructor is the clean example:

```java
// TODO: this grabs the IMU but never calls imu.initialize(...) with a
//  RevHubOrientationOnRobot, so it has no idea how the hub is mounted. BillyRobot
//  happens to initialize the same device separately in its own constructor, so
//  Billy works by luck - but Wildbots2025 also uses a MecanumDrive and never
//  initializes the IMU anywhere, so its turnToAngle() and yaw telemetry read from
//  an unconfigured device.
imu = hardwareMap.get(IMU.class, "imu");
```

Ask the student the question this TODO answers for you:  "Why does Billy work at all, if
this line never initializes the IMU?"  Because `BillyRobot`, a completely different class,
happens to fetch and initialize the same-named `"imu"` device separately.  `MecanumDrive`
never checks that this has happened;  it just assumes whoever built the robot did it
somewhere.  Nothing enforces that assumption, so the very next robot built on
`MecanumDrive` -- `Wildbots2025` -- inherits the same drive train code and gets an
uninitialized IMU, silently, because nobody re-did the lucky part.

This is the same shape, one layer down, as the turret problem in **Advanced/Control
Ownership and Resource Locking** -- there, two classes both *command* one resource with no
declared owner;  here, two classes both *assume* one resource has been *set up* by the
other.  Point the student at that lesson rather than re-deriving the ownership discussion
here;  the tell to leave them with is narrower and purely mechanical:  whenever you see a
second `hardwareMap.get(SameType.class, "sameName")` anywhere in the codebase, go find the
first one and check whether initialization went with it, or got left behind.

### 4.  Independent guards fighting over one field

**The tell:**  two separate `if` statements (not one `if`/`else if`/`else` chain) that each
assign the same variable based on different conditions.

`BillyMA`'s intake teleop strategy is the clean example:

```java
if (gamepad.dpad_up) {
    motor.setPower(0.75);
}
if (gamepad.dpad_down) {
    motor.setPower(-0.75);
} else {
    motor.setPower(0);
}
```

Walk the student through pressing `dpad_up` alone, one line at a time.  Line 1:
`dpad_up` is true, `motor.setPower(0.75)` runs.  Line 2:  this is a *new, independent* `if`
statement -- `dpad_down` is false, so its `else` branch runs, and `motor.setPower(0)`
overwrites the value the first block just set.  The forward intake gesture can never
actually turn the motor on, and the bug is invisible from reading either block alone --
you only see it by noticing that both blocks touch `motor`, in sequence, unconditionally
with respect to each other.

Ask the student to state the general rule, then check it against the fix:  when two
conditions are meant to be mutually exclusive settings of the same target, they must be
arms of *one* `if`/`else if`/`else` chain, not two separate `if` statements that each get a
full, unconditional say over the same variable.

### 5.  The SDK's silence isn't a happy path

**The tell:**  a call into SDK or vendor code that documents (or clearly can) return
`null`, an empty result, or an out-of-range sentinel, with no check before the next line
uses the result -- especially when that call happens from inside a per-tick path.

`LimelightExtensions.tryGetFiducial` is the clean example:

```java
public static LLResultTypes.FiducialResult tryGetFiducial(
    Limelight3A limelight,
    int tagId)
{
    return limelight
        .getLatestResult()
        .getFiducialResults()   // NPE here if getLatestResult() returned null
        .stream()
        .filter(fr -> fr.getFiducialId() == tagId)
        .findFirst()
        .orElse(null);
}
```

Ask the student:  "This method already returns `null` on purpose, when no matching tag is
found -- so whoever calls it clearly expects `null` is possible.  What's the actual bug,
then?"  Guide them to the gap:  `getLatestResult()` itself can return `null` -- right after
`limelight.start()`, before the first frame arrives, or if the camera drops out mid-match --
and nothing here checks for that *before* calling `.getFiducialResults()` on it.  That's a
different, uncaught failure a line earlier than the one this method was clearly written to
handle.  And because `LimelightAutoTarget.lookForTag()` calls this every single teleop
loop, that one unguarded null doesn't just fail one lookup -- it throws all the way out and
crashes the entire OpMode.

The rule:  a `null` your own method returns on purpose is not the only `null` in the
picture.  Anything you call *into*, especially vendor or SDK code, can hand back an absent
or sentinel result of its own, and that has to be checked at the boundary where you receive
it, not assumed away because your own contract already accounts for a different `null`.

## Guided practice

Give the student `PusherServo`'s two autonomous verbs cold, without the comment above them,
and ask them to find the bug themselves using category 2's tell:

```java
public void pushBalls() {
    servoR.setPosition(0.8);
    reportStatus("Pusher: push");
}
public void retractPusher() {
    servoR.setPosition(0.0);
    reportStatus("Pusher: retract");
}
```

Then show them that only `setPosition(double)`, a *third* method on this same class, ever
calls `servoR.setDirection(Servo.Direction.REVERSE)` -- and that method is the one the
*teleop* strategy calls, never the two above.  Ask:  "If an autonomous routine calls
`pushBalls()` before `move()` has ever run once, what direction is this servo in?"  Guide
them to the answer:  hardware-default `FORWARD`, because nothing set `REVERSE` yet -- so the
`0.8` and `0.0` values, tuned assuming `REVERSE`, land in the wrong physical position.  Ask
which category this is, precisely:  it's category 2 with a twist -- the two paths don't just
disagree on a sign, one of them silently depends on setup work the *other* path happens to
do first.

Then hand them `LimelightAutoTarget.rotateCW` and `rotateCCW` side by side and ask them to
find the asymmetry themselves, using the same tell:

```java
public IState rotateCCW(double tx){
    return () -> {
        double power = 1;
        if(tx > -10) power = -tx / 10.0;
        turret.setPower(power);
        return lookForTag();
    };
}
public IState rotateCW(double tx) {
    return () -> {
        double power = 1;
        if (tx < 10) power = -tx / 10.0;
        turret.setPower(power);
        return lookForTag();
    };
}
```

Have them compare the sign of `power` in the default branch (`1` in both) against the sign
the proportional branch computes as `tx` grows large in each direction, and connect it back
to the failure:  a tag far to one side drives the turret at full power *away* from center
in one of these two methods, and since `tx` only grows as the turret turns the wrong way,
it never recovers.

Finally, give them this snippet, unseen, and ask them to name which of the five categories
it belongs to before explaining the bug:

```java
if (gamepad.left_bumper) { arm.setPosition(0.9); }
if (gamepad.right_bumper) { arm.setPosition(0.1); } else { arm.setPosition(0.5); }
```

(Category 4 -- pressing `left_bumper` alone sets `0.9`, then the second block's `else`
immediately overwrites it to `0.5`.)

## Wrap-up check

Have the student name all five categories from memory, each with its one-sentence tell, no
notes.

Then give them one new, invented code smell per category -- not from the repo -- and have
them classify each correctly and explain what question they'd ask to confirm the bug before
touching any code.

Finally, ask the question that ties the lesson together:  "Every one of today's five bugs
compiles cleanly and passes a casual read of the one method it's in.  What do they all have
in common that makes them invisible to a single-file review?"  Listen for:  each one only
becomes visible by comparing two things that live apart from each other -- two sign
conventions in two files, two `if` blocks touching one variable, two owners of one handle,
a caller's assumption against a callee's real contract.  None of these are caught by reading
top to bottom once;  all of them are caught by asking "what else touches this?"
