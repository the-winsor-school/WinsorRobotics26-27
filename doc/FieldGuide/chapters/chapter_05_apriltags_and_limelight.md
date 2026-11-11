# Chapter 5 - AprilTags and the Limelight

*Almost everything the robot does with a tag comes from one number.*

---

A camera is the biggest sensor on the robot, and the fussiest.  On an FTC
field, most of what a camera looks for is an **AprilTag**:  a printed square
the robot can find and identify from across the field.  Billy uses one to aim
its turret.

This chapter covers what a tag is, the two ways an FTC robot can read one, and
how Billy turns one number from the camera into turret motion.  That number is
`tx`, how far the tag sits from the middle of the picture, and most of the
chapter is about it.

Look here when you're setting up a Limelight, when the turret won't aim, or
when you're deciding between a Limelight and a webcam.

Read first:  4.3, for the deadband, and 2.7, for `tryGetFiducial`.

| Entry | The question it answers |
|---|---|
| 5.1 | What is an AprilTag? |
| 5.2 | Webcam or Limelight? |
| 5.3 | How does `BillyRobot` set up the Limelight? |
| 5.4 | What does the Limelight hand back? |
| 5.5 | What is `tx`? |
| 5.6 | How does the turret ease in on a tag? |
| 5.7 | How does `tryGetFiducial` search the results? |
| 5.8 | What happens when there's no result? |
| 5.9 | Why won't the turret aim?  The tag ID, and other things that bite |
| 5.10 | How does the webcam route work? |

---

## 5.1  What an AprilTag is

*Where it lives:*  `doc/game-manual/`

An AprilTag is a printed black-and-white square, a bit like a QR code built
for a robot to read at speed.  A QR code carries a whole web address.  An
AprilTag carries much less, essentially one number, its **tag ID**.

That's the trade-off.  Carrying less costs you everything except the number.
What it buys is a pattern simple enough to read from far away, at an angle,
in bad light, and while the robot is moving.  For a robot crossing a field,
reading one number every time beats reading a paragraph some of the time.

An AprilTag is one kind of **fiducial**:  a marker placed on purpose so a
camera can find it and know which one it's looking at.  The Limelight's code
calls every tag result a `FiducialResult`, and that's why.

Two facts make tags useful in FTC:

1. **Every tag has an ID**, and the game manual says which ID sits where on
   the field.  Find the tag, read the ID, and you know which part of the field
   you're looking at.
2. **The SDK knows each tag's real size and position.**  A tag of known size
   looks smaller the farther away it is, so one picture is enough to work out
   where the camera is compared with the tag.  Chapter 14 does that.

The IDs change every season.  5.9 says where the team's code needs this
season's numbers.

*See also:*  5.9, Chapter 14, Chapter 15

## 5.2  Two ways to read a tag

An FTC robot can read AprilTags two ways, and they trade off differently.

A **webcam and a `VisionPortal`** (3.8).  The webcam takes pictures, and the
Control Hub does the work of finding tags in them.  A Limelight 3A is a camera
with its own small computer inside.  It finds the tags itself and hands back
answers.

| | Webcam and `VisionPortal` | `Limelight3A` |
|---|---|---|
| Where the work happens | on the Control Hub | inside the camera |
| Setup | build a processor, build a portal | plug it in, pick a pipeline |
| Gives you | the tag's full position compared with the camera | angles to the tag, and a pose on the field |
| Costs you | Control Hub time, which the drive loop also needs | a more expensive camera |

The webcam route costs Control Hub time.  Every loop the hub spends looking
for tags is time it spends away from driving.  What the webcam route buys is
detail:  how far away the tag is, and how squarely you face it, with no extra
math (5.10).

The Limelight costs money, and its angles take more work to turn into a
distance.  What it buys is a Control Hub left free for everything else, and a
setup of four lines.

Billy uses the Limelight.  The `Robot` base class has a `VisionPortal` field
ready for a robot that takes the webcam route, and Billy leaves it unused.

*See also:*  3.8, 5.3, 5.10, Chapter 14

## 5.3  Setting up the Limelight

*Where it lives:*  `RobotModel/Robots/BillyRobot.java`

Here's the whole setup, from `BillyRobot`'s constructor:

```java
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.start();
        limelight.pipelineSwitch(0);
```

Four lines, each with one job.

**`hardwareMap.get(...)`** fetches the camera, the same as any device (3.1).
The Limelight shows up in the Driver Station configuration as a USB device.
The "serial number" shown under it is an IP address, which confuses everyone
once.

**`setPollRateHz(100)`** says how often to ask the camera for fresh results:
100 times a second.  **Polling** means asking again and again on a schedule,
the way you might check a pot of water.  The poll rate is how often the SDK
asks.  It's separate from how many pictures a second the camera takes.

**`start()`** begins the polling.  The SDK's own Limelight sample,
`SensorLimelight3A.java`, warns about leaving it out:

```java
        /*
         * Starts polling for data.  If you neglect to call start(), getLatestResult() will return null.
         */
        limelight.start();
```

**`pipelineSwitch(0)`** picks a pipeline.  A **pipeline** is a saved setup on
the camera itself, made through the camera's own web page.  Pipeline 0 might
look for AprilTags, and pipeline 1 for a color.  Switching pipelines changes
what the camera is looking for.  Billy uses pipeline 0, so the camera's slot 0
has to be set up to find AprilTags.

The `Limelight3A` object belongs to `BillyRobot`, and 4.8 says why.

> **Try it.**  Open the Limelight's web page from a laptop on the robot's
> network, and look at pipeline 0.  Check that its type finds AprilTags.  Then
> run `SensorLimelight3A` from the SDK samples with a tag in front of the
> camera.  The Driver Station should show a `Fiducial` line with the tag's ID.

*See also:*  3.1, 3.8, 4.8, 10.8

## 5.4  What the Limelight hands back

*Where it lives:*  `SensorLimelight3A.java`, in the SDK samples

Every loop, the code asks for the newest result:

```java
            LLResult result = limelight.getLatestResult();
```

An `LLResult` is one batch of answers from the camera.  It has a list in it,
one `LLResultTypes.FiducialResult` for every tag the camera saw.  The SDK
sample walks through that list:

```java
                List<LLResultTypes.FiducialResult> fiducialResults = result.getFiducialResults();
                for (LLResultTypes.FiducialResult fr : fiducialResults) {
                    telemetry.addData("Fiducial", "ID: %d, Family: %s, X: %.2f, Y: %.2f", fr.getFiducialId(), fr.getFamily(), fr.getTargetXDegrees(), fr.getTargetYDegrees());
                }
```

That `for` loop runs its body once for each item in the list, and calls the
current item `fr` (2.6 covers `for`).  Each `fr` answers questions about one
tag:

| Method | Answers |
|---|---|
| `getFiducialId()` | which tag this is |
| `getTargetXDegrees()` | how far left or right of center, in degrees:  `tx` |
| `getTargetYDegrees()` | how far up or down of center, in degrees:  `ty` |

`getLatestResult()` hands back what the SDK last heard from the camera, and
returns right away.  Two calls close together usually get the same answer.

*See also:*  2.7, 5.5, 5.8, Chapter 14

## 5.5  `tx`, the number that matters

**`tx`** answers one question:  how far off-center is this tag, side to side,
in degrees?  Zero means dead ahead.  The farther the tag sits from the middle
of the picture, the bigger `tx` gets, and its sign says which side.  **`ty`**
is the same thing up and down.

Billy's turret only needs `tx`.  Aiming is turning until the tag sits in the
middle, and that's the same as turning until `tx` is zero.

> **Predict.**  The camera reports `tx = 15`.  Should the turret turn so that
> `tx` grows, or so that it shrinks?  And does that mean positive power or
> negative?

Always drive `tx` toward zero, so it should shrink.  Which power does that is
a different question, and the code can't answer it.  It depends on which way
the servo is mounted and which way the camera faces.  You settle it by testing
on the robot:  command a small positive power, and watch which way `tx` goes.

Billy's tracker settled it.  For a tag on the positive side, it commands
negative power (5.6).

A tag outside the picture is missing from the list, so there's no `tx` to
read.  5.8 is about what the code does then.

*See also:*  4.3, 5.4, 5.6, 14.1

## 5.6  Easing in:  proportional control

*Where it lives:*  `AutonStrategies/LimelightAutoTarget.java`

The simple plan is "turn at full speed until the tag is centered."  It
overshoots.  At full speed, the turret is still moving fast when `tx` reaches
zero, so it sails past, turns around, sails past again, and hunts back and
forth forever.

Here's what `LimelightAutoTarget` does instead, for a tag on the
negative side:

```java
    public IState rotateCCW(double tx){
        return () ->
        {
            double power = 1;
            if(tx > -10)
                power = -tx / 10.0;
            turret.setPower(power);
            turret.reportData("Turret CCW", power);
            return lookForTag();
        };
    }
```

Far from the tag, at `-10` degrees or more, it runs at full power, `1`.
Inside ten degrees, the power shrinks with the error.  At `tx = -5` the power
is `0.5`.  At `-1` it's `0.1`.  The turret slows as it closes in, and arrives
gently.

That's **proportional control**:  the correction is in proportion to the
error.  Big error, big push.  Small error, small push.  The number that sets
how hard it pushes is the **gain**.  Here dividing by `10.0` is the same as
multiplying by a gain of `0.1`.  It's the simplest form of the idea, and
you'll meet it again as the "P" in PID.

`rotateCW` is the mirror image, for a tag on the positive side:

```java
    public IState rotateCW(double tx) {
        return () ->
        {
            double power = -1;
            if (tx < 10)
                power = -tx / 10.0;
            turret.setPower(power);
            turret.reportData("Turret CW", power);
            return lookForTag();
        };
    }
```

Far off, it runs at `-1`.  Close in, `-tx / 10.0` is negative too, so both
branches turn the same way.

Each state does one step and names the next, `lookForTag()`, which reads the
camera again.  Chapter 12 explains how those steps run one per loop.

> **Watch out.**  Change the gain and you change how the turret behaves.
> Divide by `2.0` and it pushes hard even when close, so it overshoots and
> twitches.  Divide by `50.0` and it pushes gently even when far off, so it
> crawls.  Change it a little at a time, at the robot, and watch.

*See also:*  4.3, 5.5, 5.9, Chapter 12

## 5.7  The tag search, one call at a time

*Where it lives:*  `Extensions/LimelightExtensions.java`

The tracker finds its tag with the team's helper, `tryGetFiducial` (2.7).  Its
last statement is one long chain:

```java
        return result
                .getFiducialResults()
                .stream()
                .filter(fr -> fr.getFiducialId() == tagId)
                .findFirst()
                .orElse(null);
```

Each step hands its answer to the next, so read it from the top.

| Step | What it hands on |
|---|---|
| `.getFiducialResults()` | the list of every tag the camera saw |
| `.stream()` | the same items, lined up to be worked through one at a time |
| `.filter(...)` | only the items that pass the test |
| `.findFirst()` | the first item left, if there is one |
| `.orElse(null)` | that item, or `null` if the line came up empty |

A **stream** is a list's items lined up for a chain of steps like this.
`filter` takes a test, written as a lambda (1.9), and keeps the items that
pass:  here, `fr -> fr.getFiducialId() == tagId` keeps the tag with the ID
you asked for.

`findFirst` hands back a box that holds one item, or holds nothing when no
item passed.  Java calls that box an `Optional`.  `orElse(null)` opens the
box.  It gives you the item if there is one, and `null` if the box is empty.

So the whole chain says:  *of the tags the camera saw, keep the one with this
ID, and hand it back, or `null` if it isn't there.*

*See also:*  1.9, 2.7, 5.4, 5.8

## 5.8  When there's no result

*Where it lives:*  `Extensions/LimelightExtensions.java`

Two things can come back empty.  The whole result can be `null`, before the
camera has sent anything.  Or the result arrives, and the tag you want isn't
in it.

The helper handles the first one before the chain:

```java
        LLResult result = limelight.getLatestResult();
        if (result == null)
            return null;
```

That guard is new.  The version the lesson was written from went straight
from `getLatestResult()` into the chain.  A `null` result there would have
called `getFiducialResults()` on nothing.

Calling a method on `null` is the most common crash in Java, and it has a
name, the **`NullPointerException`**.  The OpMode stops right there, mid-match.
`null` means "no object here" (1.2), and there's no object to ask.

The guard is a **guard clause**:  a check at the top of a method that hands
back early when something's missing, so the rest of the method can assume
it's there.

The second empty case is handled by the chain itself.  When no tag matches,
`orElse(null)` hands back `null`.  So callers always get one of two things:  a
tag, or `null`.  The tracker checks for `null` first, every time (4.5).

The SDK's sample uses a different test:  `result.isValid()`, which is `true`
when the result has a real target in it.  The team's helper tests for `null`,
and its search over an empty list hands back `null` too.  Chapter 14 shows
code that checks `isValid()` before trusting a pose.

*See also:*  1.2, 2.7, 4.5, 5.7, Chapter 13

## 5.9  Why won't the turret aim?

*Where it lives:*  `OpModes/BillyTeleOpBLUE.java`

Start with the tag ID.  Here's the top of `BillyTeleOpBLUE`:

```java
    // TODO: this season's BLUE-alliance target AprilTag ID goes here (last season's Limelight tag ID was removed during 2027 season prep).
    private static final int TARGET_TAG_ID = -1;
```

No AprilTag has the ID `-1`.  So `lookForTag()` never finds its tag.  Every
loop it reports `Tag -1 not found.` and stops the turret.  `BillyTeleOpRED`
and `ExampleStateMachineAuton` have the same `-1`, with the same `TODO`.  Put
this season's IDs in from the game manual, and the tracker has something to
look for.

Billy starts the tracker on its own, at the end of `BillyRobot`'s
constructor.  While it's running, it owns the turret, so the turret's manual
bumpers are skipped.  `gamepad1.y` switches it off and on.  Chapter 13
explains how that ownership works.  So "the turret won't aim" and "the turret
won't respond to the bumpers" can be the same cause:  the tracker is running,
and has no tag to aim at.

When the ID is right and the turret still won't aim, these are the next
things to check.

- **The pipeline.**  Slot 0 on the camera has to find AprilTags (5.3).
- **The aim.**  A tag outside the camera's view isn't in the list at all.  The
  code treats a tag out of view exactly like a tag that doesn't exist.
- **Motion blur.**  A moving robot smears the tag in the picture.  The SDK's
  webcam samples set a short exposure to fight it.
- **Lighting.**  Direct venue light and glare both make tags harder to read.
  Tune at the venue when you can.
- **Distance.**  A bigger tag reads from farther away, and the game sets the
  size.

> **Try it.**  Put this season's ID in a copy of `BillyTeleOpBLUE`, hold a
> printed tag in front of the camera, and watch the turret's telemetry.  You
> should see `Turret CW` or `Turret CCW` with a power that shrinks as the
> tag gets near the middle, then `Turret Stopped`.

*See also:*  4.5, 5.3, 5.6, 12.6, 13.6, Chapter 15

## 5.10  The webcam route

*Where it lives:*  `ConceptAprilTagEasy.java` and `ConceptAprilTag.java`, in the SDK samples

Billy uses the Limelight, so this is the route the team's code leaves open.
The SDK sample `ConceptAprilTagEasy.java` builds it in two calls:

```java
        aprilTag = AprilTagProcessor.easyCreateWithDefaults();
```

and then a portal to feed it pictures from the webcam:

```java
            visionPortal = VisionPortal.easyCreateWithDefaults(
                hardwareMap.get(WebcamName.class, "Webcam 1"), aprilTag);
```

The `AprilTagProcessor` finds tags.  The `VisionPortal` connects the camera
to it (3.8).  The processor only sees the pictures a portal hands it, so a
robot needs both.

Every loop after that, `aprilTag.getDetections()` hands back a list of what it
found.  SDK 12.0 changed what's in that list, because it added tag clusters.
Each item is now either one tag or a group of tags, and the code checks which
before reading it.  14.2 walks through the sample's loop, and 15.4 explains
the check.

The payoff of this route is **`ftcPose`**:  the tag's position compared with
the camera, worked out for you.  Three of its parts are enough to drive up to
a tag and face it squarely.

| Part | Means |
|---|---|
| `range` | how far away the tag is, in inches |
| `bearing` | how far left or right it is, in degrees:  the webcam's version of `tx` |
| `yaw` | how squarely you face it, in degrees |

The SDK sample `RobotAutoDriveToAprilTagOmni` uses them to drive up to a tag
and stop.  That's the job where the webcam route is easier.  The Limelight
gives `tx` right away, and a distance takes more work (Chapter 14).

`ftcPose` only means something for a tag in the season's tag library, because
the SDK needs the tag's real size.  An unknown tag still reports its ID, with
no pose.

> **Argue it.**  Billy already aims with the Limelight.  Would a webcam and an
> `AprilTagProcessor` earn their Control Hub time on the same robot, say for
> driving up to a tag in autonomous?  Or does one camera, doing one job well,
> beat two?

*See also:*  3.8, 5.2, 14.1, 14.2, 15.4

## Chapter summary

An AprilTag is a printed square that carries one number, readable at speed.
Billy reads tags with a Limelight, set up in four lines, and asks it every loop
for the newest result.  The number that matters is `tx`, how far the tag is
off-center.  The turret drives `tx` toward zero, with power in proportion to
the error, and stops inside a small deadband.  The team's helper hands back a
tag or `null`, and every caller checks.

| To find out... | Read... |
|---|---|
| what an AprilTag is | 5.1 |
| how a webcam and a Limelight compare | 5.2 |
| what each line of the Limelight setup does | 5.3 |
| what's in an `LLResult` | 5.4 |
| what `tx` measures | 5.5 |
| why the turret slows down near the tag | 5.6 |
| how the `stream` chain finds a tag | 5.7 |
| what stops a `NullPointerException` | 5.8 |
| why the turret won't aim | 5.9 |
| how the webcam route works | 5.10 |

### Java in this chapter

| Syntax | Where |
|---|---|
| a `for` loop over a `List` | 5.4 |
| `stream()`, `filter`, `findFirst`, `orElse` | 5.7 |
| `Optional` | 5.7 |
| `NullPointerException` | 5.8 |
| a guard clause | 5.8 |

### Vocabulary

**AprilTag** -- a printed black-and-white square that carries one number,
its tag ID, and can be read at speed.  (5.1)

**fiducial** -- a marker placed so a camera can find it and know which one it
is.  An AprilTag is one.  (5.1)

**`ftcPose`** -- the webcam route's answer for a tag:  its position compared
with the camera, including `range`, `bearing`, and `yaw`.  (5.10)

**gain** -- the number that sets how hard a proportional correction pushes.
Dividing by `10.0` is a gain of `0.1`.  (5.6)

**guard clause** -- a check at the top of a method that hands back early when
something's missing.  (5.8)

**`NullPointerException`** -- the crash you get when code calls a method on
`null`.  (5.8)

**pipeline** -- a saved setup on the Limelight that decides what it looks
for.  (5.3)

**polling** -- asking for fresh data again and again, on a schedule.  (5.3)

**proportional control** -- making a correction in proportion to the error:
big error, big push.  (5.6)

**stream** -- a list's items lined up to be worked through by a chain of
steps.  (5.7)

**tag ID** -- the number an AprilTag carries.  The game manual says which ID
is where.  (5.1)

**`tx`** -- how far a tag sits left or right of the middle of the picture, in
degrees.  Zero is dead ahead.  (5.5)

**`ty`** -- the same, up and down.  (5.5)

### Check your understanding

1. Why does a code carrying one number beat a QR code, for a robot crossing a
   field?  (5.1)
2. Name one thing the webcam route costs, and one thing the Limelight costs.
   (5.2)
3. What does `getLatestResult()` hand back if `start()` was never called?
   (5.3)
4. The Limelight reports `tx = -8`.  Which state does `lookForTag()` go to,
   and what power does it command?  (5.6)
5. Now `tx = 0.5`.  What happens, and why?  (4.3, 5.9)
6. What would the turret do if the code tested `tx < 0` and `tx > 0`, with no
   band between them?  (4.3, 5.6)
7. Read the `stream` chain out loud as one sentence.  (5.7)
8. A driver says the OpMode crashed mid-match, just as the robot turned away
   from the goal, on a version of the code with no `null` guard.  Where do you
   look first?  (5.8)
9. The turret ignores its bumpers and never aims.  Name the first two things
   you'd check.  (5.9)
10. You want the robot to stop eighteen inches from a tag.  Which route gives
    you that number most directly, and what's it called?  (5.10)

## Where to go next

Chapter 12 explains the state machine the tracker runs on.  Chapter 13
explains why the tracker owns the turret while it runs.  Chapter 14 turns one
tag into the robot's position on the field.  Chapter 15 aims at a group of
tags, which is how this season's goals are marked.
