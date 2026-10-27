# Chapter 14 - Pose and Navigation

*From "which way is the tag" to "where am I on the field."*

---

Billy's turret tracker asks a tag one question:  which way is it?  The answer
is an angle, and an angle is enough to aim a turret.  The same camera can
answer a much bigger question, though.  Where is the robot, and which way is
it facing?

This chapter is about that bigger question.  Billy's code asks only the first
one today, and the hardware for the second is already on the robot.

Look here when you want the robot to know where it is, when a pose reading
looks too good to be true, or when you're wondering what any of this has to do
with robots outside FTC.

Read first:  Chapter 5, for AprilTags, `tx`, and `ftcPose`.

| Entry | The question it answers |
|---|---|
| 14.1 | What can `tx` tell you, and what can't it? |
| 14.2 | How does one tag tell the robot where it is on the field? |
| 14.3 | Why does the code have to say where the camera sits? |
| 14.4 | What is a botpose? |
| 14.5 | Why would a camera want the robot's IMU? |
| 14.6 | How do you know a pose is real? |
| 14.7 | Why is one tag enough? |
| 14.8 | Why does the tag ID matter so much? |
| 14.9 | Is this just a game trick? |

---

## 14.1  Bearing or pose?

*Where it lives:*  `AutonStrategies/LimelightAutoTarget.java`

Here is the heart of the state in Billy's turret tracker that looks at the
camera:

```java
            LLResultTypes.FiducialResult tag =
                    LimelightExtensions.tryGetFiducial(
                            limelight,
                            targetTagId);
            ...
            double tx = tag.getTargetXDegrees();
            if(tx < -2) return rotateCCW(tx);
            else if(tx > 2) return rotateCW(tx);
            else            return stopTurret();
```

It finds the tag with the right ID, and then it reads one number from it.
That's `tx`:  how many degrees the tag sits left or right of the camera's
crosshair (Chapter 5).  The code turns the turret until `tx` is close to zero.
That's all the tracker needs, and it works.

Now list what `tx` leaves out.  It says nothing about how far away the tag
is.  It says nothing about which way the robot is facing, compared with the
tag.  And it can't say where the robot is on the field.  Every answer `tx`
gives has the shape "turn left" or "turn right."

That kind of answer is a **bearing**:  the direction from you to something.
A bearing is enough to aim.

The bigger answer is a **pose**:  where something is *and* which way it's
facing, both at once.  A pose for a robot says "three feet from the wall,
two feet from the corner, pointed toward the goal."

A full pose takes six numbers, because a thing can move in six independent
ways.  It can slide three ways:  left and right, forward and back, up and
down.  It can turn three ways:  **yaw** (turning like a door), **pitch**
(tipping nose up or down), and **roll** (tipping side to side).  Yaw is
explained with the IMU in Chapter 3.  Those six ways to move are the **six
degrees of freedom**, often written 6DOF.

A robot on flat tiles mostly cares about three of them:  its x, its y, and its
yaw.  The camera solves all six anyway.

The webcam's `ftcPose` already gives the tag's pose compared with the camera
(Chapter 5).  This chapter goes one step further, to "where am I, on the
field."

*See also:*  14.2, Chapter 5, Chapter 12

## 14.2  From the tag to the field:  `robotPose`

*Where it lives:*  `FtcRobotController/.../samples/ConceptAprilTagLocalization.java`

Start with the field.  A BIOBUZZ field is about 144 inches on each side
(`doc/game-manual/sections/09-9-arena.md`, section 9.2).  FTC lays a fixed
set of axes over it, so every spot on the field has an x, a y and a z, and
every direction has an angle.  Those axes are the **field coordinates**.  The
sample's own comment points to the FTC page that defines them.

A season's **tag library** is a table the SDK carries, listing each tag's ID,
its printed size, and where it's fixed on the field, in field coordinates.

Now the trick.  From the camera image, the SDK can solve for where the tag is
compared with the camera (14.7 says how).  From the library, it already knows
where the tag is on the field.  Put the two together and you get where the
*camera* is on the field.  Correct for where the camera sits on the robot
(14.3), and you get where the *robot* is on the field.  The chain has two
links:  camera to tag, solved from the image, and tag to field, looked up in
the library by ID.

The webcam route hands back the result as `robotPose`, a field on each
detection.  Here's the SDK's localization sample reading it:

```java
        for (AprilTagDetection detection : currentDetections) {
            if (detection instanceof AprilTagSingleDetection) {
                AprilTagSingleDetection singleDet = (AprilTagSingleDetection) detection;

                if (singleDet.metadata != null) {
                    telemetry.addLine(String.format("\n==== (ID %d) %s", singleDet.id, singleDet.metadata.name));
                    telemetry.addLine(String.format("Robot XYZ %6.1f %6.1f %6.1f  (inch)",
                            detection.robotPose.getPosition().x,
                            detection.robotPose.getPosition().y,
                            detection.robotPose.getPosition().z));
                    telemetry.addLine(String.format("Robot PRY %6.1f %6.1f %6.1f  (deg)",
                            detection.robotPose.getOrientation().getPitch(AngleUnit.DEGREES),
                            detection.robotPose.getOrientation().getRoll(AngleUnit.DEGREES),
                            detection.robotPose.getOrientation().getYaw(AngleUnit.DEGREES)));
```

Read it from the inside out.  `detection.robotPose.getPosition().x` walks
three dots (Chapter 1):  the detection's robot pose, that pose's position, and
the position's x.  The orientation half works the same way and reports pitch,
roll and yaw in degrees.  `String.format` builds a line of text from a pattern,
and each `%6.1f` in the pattern is a slot for one number, six characters wide
with one digit after the decimal point.

The `instanceof` line at the top is new in SDK 12.0, and 15.4 explains it.
For now, read it as "if this detection is a single tag."

`metadata` is the detection's entry in the tag library.  A `null` there means
the second link of the chain is missing, so the sample prints only where the
tag sits in the image.

> **Watch out.**  This season's tags move.  BIOBUZZ puts its AprilTags on the
> CELLS of the HIVE, and each HIVE tips on a pivot during the match
> (`doc/game-manual/sections/09-9-arena.md`, section 9.6).  The SDK's release
> notes, at the top of the repository's `README.md`, say it in bold:
> *"Unfortunately, since BIOBUZZ AprilTags move, they are not suitable for
> absolute Field Localization."*  A field pose needs a tag that's bolted to a
> known spot.  This season the tags are good for aiming at a goal (Chapter
> 15), and the field-pose idea is for seasons, and robots, with fixed tags.

*See also:*  14.3, 14.7, 15.4, Chapter 5

## 14.3  Where is the camera?  Camera pose

*Where it lives:*  `FtcRobotController/.../samples/ConceptAprilTagLocalization.java`

The tag can tell the SDK where the *camera* is.  Only the team knows where
that camera is bolted onto *this* robot.  So the localization sample makes you
say it, in two variables near the top:

```java
    private Position cameraPosition = new Position(DistanceUnit.INCH,
            0, 0, 0, 0);
    private YawPitchRollAngles cameraOrientation = new YawPitchRollAngles(AngleUnit.DEGREES,
            0, -90, 0, 0);
```

and then hands both to the processor as it builds it:

```java
                .setCameraPose(cameraPosition, cameraOrientation)
```

Those two together are the **camera pose**:  where the camera sits on the
robot, measured from the robot's center, and which way it points.

The sample's comment spells out the axes.  The robot's center is the origin,
at field height, with *"+x right, +y forward, +z upward."*  The position is in
inches, so a camera 5 inches left, 7 inches forward and 12 inches up is, in
the comment's own example, `(-5, 7, 12)`.  The last `0` in each constructor is
a timestamp, and the sample leaves it at zero.

The orientation reads yaw, pitch, roll, in that order.  All zeros would mean
the camera points straight up.  The comment explains why the sample uses a
pitch of `-90`:  *"In most cases, you'll need to set the pitch to -90 degrees
(rotation about the x-axis), meaning the camera is horizontal."*  Yaw turns it
left (+90) or right (-90).

> **Watch out.**  Leave the camera pose at the sample's zeros and every
> `robotPose` comes out shifted by however far the real camera is from the
> robot's center, and turned by however far it's tilted.  The numbers still
> look reasonable, so nothing warns you.  Measure the camera's position with a
> tape measure, from the center of the robot, before you trust a single pose.

The Limelight needs the same measurement, entered in its own setup
(`doc/limelight3a/api.md`).

*See also:*  14.2, 14.4, Chapter 3

## 14.4  Botpose:  the Limelight's version

*Where it lives:*  `FtcRobotController/.../samples/SensorLimelight3A.java`

Billy sees with a Limelight 3A, and `BillyRobot` sets it up in its
constructor:

```java
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.start();
        limelight.pipelineSwitch(0);
```

The Limelight can run the same chain as 14.2, on its own computer inside the
camera.  It calls the result a **botpose**:  the robot's pose on the field,
worked out by the camera.  The SDK's Limelight sample reads it like this:

```java
            LLResult result = limelight.getLatestResult();
            if (result.isValid()) {
                // Access general information
                Pose3D botpose = result.getBotpose();
```

A `Pose3D` is the SDK's pose type, with a `getPosition()` and a
`getOrientation()`.  Limelight poses come in **meters** and degrees, where the
webcam sample used inches (`doc/limelight3a/data-shapes.md`).

`getBotpose()` works from vision alone.  The Limelight's name for that is
**MegaTag1**.  It uses every tag in view that's on the camera's field map,
which is the Limelight's version of the tag library, loaded into the camera
along with its mounting pose (14.3).

There's also a single-tag version.  Each tag's result has a
`getRobotPoseFieldSpace()`, which is the robot's field pose worked out from
that one tag alone (`doc/limelight3a/java-objects.md`).  So the Limelight
offers two answers to "where am I?":  one fused from every visible tag, and
one per tag.

Billy calls neither.  Search `TeamCode` for `getBotpose` and you find nothing.
The tracker reads `tx` (14.1), and that's the job it was built for.  The pose
is a capability sitting in hardware the team already has.

> **Try it.**  Copy `SensorLimelight3A` into TeamCode, remove its `@Disabled`
> line, and run *Sensor: Limelight3A* with no tag in view.  The screen shows
> `Limelight` and `No data available`, because the sample checks `isValid()`
> first.  Now hold a tag in front of the camera.  A `Botpose` line appears.
> Whether its numbers mean anything depends on the camera's field map, which
> is the question 14.6 takes up.

A method on `BillyRobot` that returns the botpose would claim nothing, the
way `LimelightAutoTarget` claims the turret (Chapter 13).  It reads a sensor
and commands no hardware, so it has nothing to own.

> **Argue it.**  Where would a `getFieldPosition()` method belong:  on
> `BillyRobot`, which owns the Limelight;  on the base `Robot` class, so every
> robot gets one;  or on a small class of its own that the robot hands the
> camera to?  Chapter 10 has the rule for robot-wide sensors.  Think about
> Wildbots2025 too, which has no Limelight.

*See also:*  14.2, 14.5, 14.6, Chapter 10, Chapter 13

## 14.5  MegaTag2, and why a camera wants your IMU

*Where it lives:*  `doc/limelight3a/api.md`

Right beside `getBotpose()` sits a second method, `getBotpose_MT2()`.  That's
**MegaTag2**, and it works differently.  The team's reference puts it this way:

> *"MegaTag2 additionally requires ongoing `updateRobotOrientation(yawDegrees)`
> calls using an IMU heading aligned to the loaded field map, followed by
> `result.getBotpose_MT2()`."*

So every loop, the robot tells the camera its heading, and the camera uses it
in the solve.  The camera can already solve a heading from a tag (14.4).  It
asks anyway because each sensor is good at a different thing.

A heading solved from vision gets shaky when only one or two tags are in view,
and worse when they're far away.  A far tag covers only a few pixels, and an
error of one pixel in a corner turns into a big error in the angle.

The IMU is the sensor in the Control Hub that measures how the robot is turned
(Chapter 3).  It's very good at heading from one moment to the next.  Over a
whole match it drifts, slowly, a little at a time.

MegaTag2 splits the job.  The IMU supplies the heading, and vision solves only
for position, given that heading.  Each sensor covers the other's weak spot.

That's **sensor fusion**:  combining readings from different sensors so the
answer is better than any one of them could give alone.  MegaTag2 is a small,
concrete case of it.  The lesson this chapter comes from points to a much
bigger one:  a self-driving car blends GPS, an IMU and its wheel sensors in
the same spirit, trusting none of them alone.

Billy has both halves of the hardware.  `BillyRobot` sets up the IMU right
after the Limelight:

```java
        imu = hardwareMap.get(IMU.class, "imu");

        IMU.Parameters parameters = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP
                )
        );

        imu.initialize(parameters);
        imu.resetYaw();
```

`resetYaw()` makes "zero" mean whichever way the robot faced when the code
started.  MegaTag2 wants a heading lined up with the field map, so feeding it
Billy's yaw would take one more step:  lining up the robot's zero with the
map's.  That step, and the `updateRobotOrientation` call every loop, are a
project waiting for someone to take it on.

The same BIOBUZZ caution as 14.2 applies to both MegaTags.  A field pose needs
tags fixed to the field, and this season's tags ride on the HIVE.

*See also:*  14.4, 14.6, Chapter 3, Chapter 4

## 14.6  Is this pose real?

*Where it lives:*  `doc/limelight3a/java-objects.md`

> **Predict.**  The Limelight can't see a single tag.  You call
> `result.getBotpose()`.  Does it throw an exception, return `null`, or hand
> you something else?

It hands you a `Pose3D`, filled in with defaults:  position zero, orientation
zero.  It looks exactly like a robot parked at the field's origin, facing
angle zero.  The team's reference says so plainly:

> *"The SDK supplies zero acquisition times for these poses and substitutes
> default/zero poses for missing values.  Non-null pose and all-zero deviations
> do not imply a measured pose."*

Here's a made-up line that looks careful:

```java
if (result.getBotpose() != null) { /* drive using the botpose */ }
```

The check always passes, because `getBotpose()` always returns an object.
With no tags in view, the code drives toward a robot position that was never
measured.  Nothing crashes, which is what makes it dangerous.  Chapter 22
collects this kind of trap, where a library's silence looks like a happy
answer.

So a pose takes a quality check, and the Limelight gives you several numbers
to check it with (`doc/limelight3a/java-objects.md`):

| Method | What it tells you |
|---|---|
| `getBotposeTagCount()` | how many tags went into the solve.  Zero means no solve. |
| `getBotposeSpan()` | how far apart those tags were, in meters |
| `getBotposeAvgDist()` | how far away they were, on average, in meters |
| `getStddevMt1()`, `getStddevMt2()` | how uncertain each of the six numbers is |

The first line of defense is `getBotposeTagCount() > 0`.  That replaces the
made-up `!= null` check above.

Then think about how *much* to trust a pose.  A solve from four tags spread
across the view beats a solve from one distant tag, the same way a surveyor
trusts a position fixed from four landmarks more than one.  More tags are
supporting evidence.  The reference is careful to add that they're no
guarantee.

A **standard deviation** is a measure of how much a reading tends to wobble
around the true value.  Small means steady, and large means shaky.  The
Limelight reports one for each of the six pose numbers, in the order x, y, z,
roll, pitch, yaw, in meters and degrees.  A real localization system carries
numbers like these with every pose, so the next stage can lean on a steady
reading and discount a shaky one.  15.8 weights tags the same way.

> **Watch out.**  All zeros in the standard deviations look like perfect
> certainty.  They can mean the opposite:  a missing array, which the SDK
> fills with zeros.  Check the tag count first, every time.

*See also:*  14.4, 14.5, 15.8, Chapter 22

## 14.7  Why one tag is enough

*Where it lives:*  `doc/game-manual/sections/09-9-arena.md`, section 9.9

> **Predict.**  A GPS receiver needs signals from several satellites at once
> to find where it is.  A robot can find its full pose from one AprilTag.
> What does a tag give the camera that one satellite can't?

A GPS satellite gives one measurement:  a distance.  Knowing you're a certain
distance from one point puts you somewhere on a huge sphere around it.  It
takes more satellites, and more spheres, to narrow that down to one spot.

A tag gives four points at once.  It's a flat square, and the camera finds all
four of its corners in a single image.  The SDK knows exactly where those
corners are compared with one another, because it knows the square's size.
Four known points, seen together, are enough to solve for where the camera is
and which way it's turned:  all six numbers from 14.1.

That's also why the tag's real printed size has to be right.  One flat image
can't tell a big tag far away from a small tag close up.  The picture is the
same.  Only the known size decides which one it is, so a wrong size gives a
wrong distance, every time.

The game manual fixes the size for this season:  *"AprilTags for BIOBUZZ are
3.25 in. (8.25 cm) square targets from the 36h11 tag family."*  The SDK's tag
library carries that size for the webcam route.  The Limelight needs it typed
into its own setup:  the team's cluster plan says to set the pipeline's tag
size to 3.25 inches (82.55 mm) in the camera's web page
(`doc/limelight3a/cluster-targeting-plan.md`).

> **Watch out.**  The Limelight's field map gives tag sizes in
> **millimeters**, while its poses come back in **meters**
> (`doc/limelight3a/java-objects.md`).  Write the unit next to every number
> you copy.

One tag is enough to solve.  It isn't always enough to solve *well*.  A tag
far away covers only a few pixels, and its corners can't pin down the angle
precisely.  15.3 shows the catch, and how a group of tags gets around it.

*See also:*  14.1, 14.2, 15.3, Chapter 5

## 14.8  Why the ID matters:  data association and SLAM

Two tags are in view.  For the tag-to-field link in 14.2, the code needs to
know which entry in the library goes with which tag in the picture.  It reads
the ID, and the ID picks the row.

A robot that finds its way by ordinary landmarks, like doorways, posts or
corners of furniture, has to answer a hard question every frame:  is this the
same landmark I saw a moment ago, or a new one, and which one is it?  Matching
what you see to what you know is called **data association**, and with
ordinary landmarks it's genuinely hard.

A **fiducial** is a marker made to be recognized (Chapter 5), and an AprilTag
prints its own name.  Data association turns into a table lookup.  The
feature that looks least interesting, "it's a really sturdy QR code," is the
one that makes tag localization practical.

Now take away the library.  Suppose nobody knew where the tags were.  The
robot would have to build the map of landmark positions *while* using that
same map to work out where it is.  That's **SLAM**, Simultaneous Localization
and Mapping, and building the map is its harder half.  A competition field
with fixed tags is the easy case, because the map arrives for free.

BIOBUZZ sits in between.  The game manual lists exactly which tag IDs go on
which CELL, 30 to 33 on one red CELL, 34 to 37 on the other, and so on
(section 9.9), but each HIVE tips during the match, so the tags' field
positions change.  The IDs still say *which* goal you're looking at.  Chapter
15 builds on exactly that.

*See also:*  14.2, 15.8, Chapter 5

## 14.9  Is this just a game trick?

AprilTags came from robotics research, and FTC borrowed them.  The lesson
this chapter comes from says they came out of the APRIL Robotics Lab at the
University of Michigan, around 2011, as a general tool for research.

The chain in 14.2 has a name outside FTC:  **fiducial-based localization**.
Find a marker's corners, solve for the camera's pose against it, and combine
that with where the marker is known to be.  The lesson lists places it shows
up:

- NASA's Astrobee robots, free-flying helpers aboard the International Space
  Station, use markers like these where there's no GPS.
- University robotics labs often mount AprilTags as a cheap stand-in for an
  expensive motion-capture room.
- Warehouse robots and indoor drones read markers on floors, ceilings or
  shelves.  Amazon's warehouse drive units read markers on the floor.
- ROS, the Robot Operating System that much of robotics research builds on,
  has an `apriltag_ros` package that runs this camera-to-tag-to-world chain.
- ArUco, a related marker system in the OpenCV vision library, solves the
  same problem with a different marker design.

The SDK samples in this repository use the same technique those systems use.
FTC is using the real thing.

*See also:*  14.2, 14.8, 15.3

---

## Chapter summary

A bearing says which way to turn.  A pose says where you are and which way
you face.  One AprilTag gives a camera enough to solve a full pose, and a tag
library turns that into a pose on the field.  Billy reads the bearing today,
and the pose is a capability on hardware it already has.  This season's tags
move with the HIVE, so the field pose waits for fixed tags, and the same
tags serve for aiming instead (Chapter 15).

| Idea | Where it shows up | Entry |
|---|---|---|
| bearing | `tx` in `LimelightAutoTarget` | 14.1 |
| tag plus library gives a field pose | `robotPose` | 14.2 |
| say where the camera sits | `setCameraPose(...)` | 14.3 |
| the camera runs the chain | `getBotpose()`, MegaTag1 | 14.4 |
| sensor fusion | MegaTag2 and the IMU | 14.5 |
| check before you trust | `getBotposeTagCount()` | 14.6 |

### Java in this chapter

| Syntax | Where |
|---|---|
| a chain of dots, `detection.robotPose.getPosition().x` | 14.2.  The dot is in Chapter 1. |
| `String.format` with `%6.1f` slots | 14.2 |
| `Pose3D`, `getPosition()`, `getOrientation()` | 14.4 |
| `instanceof` | 14.2, explained in 15.4 |

### Vocabulary

**bearing** -- the direction from you to something.  `tx` is one.  (14.1)

**botpose** -- the robot's field pose, worked out by the Limelight from the
tags it sees.  (14.4)

**camera pose** -- where the camera sits on the robot, measured from the
robot's center, and which way it points.  (14.3)

**data association** -- matching what a sensor sees to the landmark it
belongs to.  A tag's ID makes it a table lookup.  (14.8)

**fiducial-based localization** -- finding your own pose from markers whose
positions are known.  (14.9)

**field coordinates** -- the fixed axes FTC lays over the field, so every spot
has an x, a y and a z.  (14.2)

**MegaTag1** -- the Limelight's field pose from vision alone,
`getBotpose()`.  (14.4)

**MegaTag2** -- the Limelight's field pose using a heading the robot supplies
from its IMU, `getBotpose_MT2()`.  (14.5)

**pose** -- where something is and which way it's facing, together.  (14.1)

**`robotPose`** -- the webcam route's field pose for the robot, on each
detection.  (14.2)

**sensor fusion** -- combining different sensors so each covers the others'
weak spots.  (14.5)

**six degrees of freedom** -- the six independent ways a thing can move:
three slides and three turns.  (14.1)

**SLAM** -- Simultaneous Localization and Mapping:  building a map of
landmarks while finding your place on it.  (14.8)

**standard deviation** -- a measure of how much a reading wobbles around the
true value.  Small means steady.  (14.6)

**tag library** -- the SDK's table of each tag's ID, size, and field position.
(14.2)

### Check your understanding

1. In one sentence each, say what `tx` tells you and what `robotPose` tells
   you.  (14.1, 14.2)
2. Why does the SDK need the camera pose before `robotPose` means anything?
   (14.3)
3. Why does MegaTag2 ask the robot for its IMU heading, when the camera can
   already solve a heading from a tag?  Name the general technique.  (14.5)
4. What does `getBotpose()` return when no tag is in view, and which call
   tells you whether the pose means anything?  (14.6)
5. Why is one AprilTag enough to find a full pose, where one GPS satellite is
   not?  Why does the tag's printed size matter?  (14.7)
6. What would change if nobody knew where the tags were on the field?  (14.8)
7. Why can't this season's tags give Billy a field pose?  (14.2)
8. Name one use of fiducial-based localization outside FTC, and say why a
   marker that prints its own ID is easier to build on than an ordinary
   landmark.  (14.8, 14.9)

## Where to go next

Chapter 5 is where the tags, `tx`, and `ftcPose` start.  Chapter 15 takes the
pose idea and points it at a goal:  four tags treated as one object, aimed at
a spot no tag sits on.  The IMU is in Chapter 3, and whether a new capability
has to claim anything is Chapter 13.
