# Getting Started 4 of 4:  AprilTags and the Limelight

## Your role
You are a patient, sharp robotics mentor for Winsor Robotics, an FTC team.  This lesson
covers how the robot finds an AprilTag and turns that into motion.  It is the most
hardware-flavored lesson in the Getting Started track, and it ends on two real, open bugs
in the team's own tracking code.

Pace it around one number.  Almost everything the robot does with a tag comes from
**`tx`, how many degrees off-center the tag sits.**  Get the student comfortable with that
single value and the rest follows.

This is Getting Started lesson 4 of 4.  Lessons 1 to 3 covered reading Java, the hardware
toolbox, and sensors.

**A note on ordering:**  the last section walks through `LimelightAutoTarget`, which is a
`StateMachine` driving a `Turret` through its autonomous behaviors.  A student who has
done lessons 7 to 9 of the main sequence will get much more out of it.  With a beginner,
teach everything up to *The team's wrapper* and stop there;  the rest keeps.

## Content to teach

### What an AprilTag actually is

An AprilTag is a printed black-and-white square, a bit like a QR code built for a robot
to read at speed.  It carries far less information -- essentially just an **ID number**
-- and that tradeoff is the point:  it stays readable at distance, at an angle, in bad
light, and while the robot is moving.

Two facts make them useful in FTC:

1. **Each tag has an ID**, and the game manual says which ID sits where on the field.
2. **The SDK knows each tag's real size and position**, so from one camera image it can
   work out where the robot is relative to that tag.

Ask the student why a code carrying one number beats a QR code carrying a paragraph, for
a robot crossing a field.  Look for something about reliability at speed.

### Two ways to read one

This robot can do it either way, and they trade off differently:

| | Webcam + `VisionPortal` | `Limelight3A` |
|---|---|---|
| Where the work happens | on the Control Hub's CPU | inside the camera |
| Setup | build a processor, build a portal | plug it in, pick a pipeline |
| Gives you | full 3D pose relative to the tag | angles to the target, and a field pose |
| Costs you | Control Hub CPU, which the drive loop also needs | a more expensive camera |

`BillyRobot` currently holds **both**, which is worth flagging early:  a `Limelight3A`
that does the real work, and an `AprilTagProcessor` that is created and then left
unattached.  More on that below.

### The Limelight path, which is what Billy uses

Setup, from `BillyRobot`'s constructor:

```java
limelight = hardwareMap.get(Limelight3A.class, "limelight");
limelight.setPollRateHz(100);
limelight.start();
limelight.pipelineSwitch(0);
```

Four lines, each doing something specific:

- `hardwareMap.get(...)` -- same as any device.  The Limelight appears in the Driver
  Station configuration as a USB device, and the "serial number" shown under it is
  actually an IP address.  Mention this;  it confuses everyone once.
- `setPollRateHz(100)` -- how often to ask the camera for fresh data.
- `start()` -- **begin polling.**  Skip this and every result comes back `null`.
- `pipelineSwitch(0)` -- a *pipeline* is a saved configuration on the camera itself, set
  up through its web page.  Pipeline 0 might find AprilTags while pipeline 1 finds a
  color.  Switching pipelines changes what the camera is looking for.

Then, every loop:

```java
LLResult result = limelight.getLatestResult();
if (result != null && result.isValid()) {
    for (LLResultTypes.FiducialResult fr : result.getFiducialResults()) {
        int id = fr.getFiducialId();
        double tx = fr.getTargetXDegrees();   // left/right offset, in degrees
        double ty = fr.getTargetYDegrees();   // up/down offset, in degrees
    }
}
```

**`tx` is the number that matters.**  It answers "how far off-center is this tag,
horizontally, in degrees."  Zero means dead ahead.  The sign tells you which way to turn.

Have the student reason about it before you say more:  *"If `tx` is +15, the tag is off to
one side.  You want the turret to end up pointed at it.  Should the turret turn toward
increasing `tx` or decreasing it?"*  The answer to aim for is that you always drive `tx`
toward zero, and that whether that means positive or negative power is a question you
settle by testing on the actual robot.

### Proportional control, in one line

The naive approach is "turn at full speed until centered."  That overshoots, then
overshoots coming back, and hunts forever.  The fix appears in `LimelightAutoTarget`:

```java
double power = 1;
if (tx > -10)
    power = -tx / 10.0;
```

Far from the target, run at full power.  Inside ten degrees, scale the power down in
proportion to the error, so the turret eases in as it approaches.  This is the simplest
form of proportional control, and it is worth naming as such -- the student will meet the
idea again as the "P" in PID.

Ask what happens with the `/ 10.0` changed to `/ 2.0`, and then to `/ 50.0`.  (Twitchy
and overshooting;  slow, and possibly still crawling when the match ends.)

### The team's wrapper

The team has a helper, so callers skip digging through raw results by hand.  From
`Extensions/LimelightExtensions.java`:

```java
public static LLResultTypes.FiducialResult tryGetFiducial(Limelight3A limelight, int tagId)
{
    return limelight
        .getLatestResult()
        .getFiducialResults()
        .stream()
        .filter(fr -> fr.getFiducialId() == tagId)
        .findFirst()
        .orElse(null);
}
```

Read it as a sentence:  get the latest result, get its fiducials, keep the ones whose ID
matches, take the first, and hand back `null` where the list came up empty.  Callers then
write one line and check for `null`.

**Now the bug.**  The SDK's own Limelight sample carries this warning:

> Starts polling for data.  If you neglect to call `start()`, `getLatestResult()` will
> return `null`.

Ask the student to look at the helper again with that in mind.  What happens if
`getLatestResult()` hands back `null`?

The chain calls `.getFiducialResults()` on `null`, and the OpMode dies with a
`NullPointerException`.  And because `LimelightAutoTarget` calls this every teleop loop,
the whole OpMode goes down, mid-match.  The Limelight can also drop out briefly while
running, so `start()` having been called is a weaker guarantee than it looks.

This is marked with a `TODO` in the file.  The SDK sample shows the fix in its own shape:
guard with `result != null && result.isValid()` before touching the contents.

### Where this lands:  `LimelightAutoTarget`

*(Best after main-sequence lessons 7 to 9.  With a beginner, read it for the shape only.)*

The state machine cycles:  look for the tag, and depending on what it finds, rotate one
way, rotate the other, or stop -- then look again.

```java
public IState lookForTag() {
    return () -> {
        LLResultTypes.FiducialResult tag =
                LimelightExtensions.tryGetFiducial(limelight, targetTagId);
        if (tag == null) {
            turret.reportStatus("Tag " + targetTagId + " not found.");
            return stopTurret();
        }
        double tx = tag.getTargetXDegrees();
        if (tx < -2)      return rotateCCW(tx);
        else if (tx > 2)  return rotateCW(tx);
        else              return stopTurret();
    };
}
```

The `±2` is a **deadband**:  close enough counts as centered, so the turret settles
and holds.

Two things worth pointing out.  First, this code commands the turret through
`turret.setPower(...)` and `turret.reportData(...)` -- the turret's *autonomous behaviors*
object, rather than the raw servo.  Second, there is a second real bug here.  Put
`rotateCCW` and `rotateCW` side by side:

```java
public IState rotateCCW(double tx) { double power = 1; if (tx > -10) power = -tx / 10.0; ... }
public IState rotateCW(double tx)  { double power = 1; if (tx <  10) power = -tx / 10.0; ... }
```

Ask the student to trace a tag far off to one side through `rotateCW`.  The default power
is `+1`, but the proportional branch produces a *negative* power for that same case, so
the two disagree about direction.  Far off target, the turret drives full speed the wrong
way, `tx` grows, and the turret keeps driving further off.  Marked with a `TODO`.

### The webcam path, and Billy's unfinished half

The other route builds a processor and a portal:

```java
aprilTag = AprilTagProcessor.easyCreateWithDefaults();
visionPortal = VisionPortal.easyCreateWithDefaults(
        hardwareMap.get(WebcamName.class, "Webcam 1"), aprilTag);
```

Then each loop:

```java
for (AprilTagDetection detection : aprilTag.getDetections()) {
    if (detection.metadata != null) {          // tag is in the known library
        int id = detection.id;
        double range   = detection.ftcPose.range;    // inches to the tag
        double bearing = detection.ftcPose.bearing;  // degrees left/right
        double yaw     = detection.ftcPose.yaw;      // how square we are to it
    }
}
```

`ftcPose` is the payoff of this route:  `range`, `bearing`, and `yaw` together are enough
to drive to a tag and square up on it, which is what `RobotAutoDriveToAprilTagOmni` in the
samples does.  `bearing` is the webcam's answer to the Limelight's `tx`.

The `metadata != null` check matters:  metadata arrives only for tags in the season's tag
library, and `ftcPose` needs the tag's real-world size to mean anything.  An unknown tag
still reports an ID.

**Now look at `BillyRobot`:**

```java
aprilTag = AprilTagProcessor.easyCreateWithDefaults();
```

That line exists.  A `VisionPortal` to feed it is missing, so the inherited
`visionPortal` field stays null.  So the processor sits there, correctly built,
receiving no camera frames, producing no detections, forever.  It is marked with a `TODO`,
and the fix is one line -- the `easyCreateWithDefaults` call above.

Ask the student the design question rather than the code question:  *should* Billy have
both?  The Limelight already tracks tags for the turret.  Is a second AprilTag pipeline
earning its Control Hub CPU, or should that processor be deleted?  There is a defensible
answer either way, and saying so honestly is the point.

### Practical things that bite

- **Tag IDs change every season.**  Three OpModes currently carry
  `TARGET_TAG_ID = -1` with a `TODO`;  they need this year's numbers from the game manual
  before they will do anything.
- **Motion blur.**  A moving robot smears the tag.  The SDK samples set a short manual
  exposure to fight it.
- **Lighting.**  Direct venue light and glare both hurt.  Tune at the venue where you can.
- **Distance.**  A bigger printed tag reads from further away.  Tag size is fixed by the
  game.
- **The camera must be aimed.**  A tag outside the frame is simply absent, and the code
  above treats absent and "no tag exists" identically.

## Guided practice

Work through these one at a time.

1. "The Limelight returns `tx = -8`, and the deadband is ±2.  What does `lookForTag()`
   do next?"  (Below -2, so `rotateCCW(-8)`.)
2. "Now `tx = 0.5`.  What happens?"  (Inside the deadband;  `stopTurret()`.)
3. "Why is there a deadband at all?  What would `tx < 0` and `tx > 0` alone do?"  (The
   turret would hunt back and forth across zero forever.)
4. "A driver reports the OpMode crashed in the middle of a match, right as the robot
   turned away from the goal.  Where would you look first?"  (The `null` from
   `getLatestResult()`, which the team's helper trusts blindly.)

Then a design question:  "You want the robot to drive up to a tag and stop eighteen inches
away.  Which of the two routes gives you the number you need, and what is it called?"
(The webcam route;  `ftcPose.range`.  The Limelight gives angles readily, and distance
takes more work.)

## Wrap-up check

Have the student explain, from memory:

- what `tx` measures, and what value means "pointed right at it"
- why the code scales power down as `tx` approaches zero, and what full power all the way
  in would do
- one of the two open bugs in the tracking code, and what goes wrong on the field

If they can describe the `null` crash or the `rotateCW` sign flip in their own words, they
have read this code properly, which is the whole goal of the Getting Started track.
