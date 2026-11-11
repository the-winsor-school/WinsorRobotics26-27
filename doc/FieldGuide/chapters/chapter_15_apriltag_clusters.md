# Chapter 15 - AprilTag Clusters

*Aiming at a point no tag sits on.*

---

The thing Billy wants to hit this season has no tag on it.  The tags sit
nearby, four to a goal, and the opening the robot launches into is blank.
This chapter is about aiming at that blank spot anyway.

The idea is the **cluster**:  a group of tags treated as one object, with a
shared point that no tag sits on.  The FTC SDK added clusters in version 12.0
for exactly this game.  Billy's Limelight doesn't know about them, so the
team's next project is to rebuild the idea in TeamCode, and the plan for it
is already written.

Look here before you start that project, when you're wondering why your
origin estimate jumps around, or when old webcam code stops compiling under
SDK 12.0.

Read first:  Chapter 5, for AprilTags and `tx`.  Chapter 14, for the
difference between a bearing and a pose.

| Entry | The question it answers |
|---|---|
| 15.1 | Why is aiming at one tag a problem? |
| 15.2 | What is a cluster, and where is its origin? |
| 15.3 | How does the SDK find a cluster's pose? |
| 15.4 | Why does SDK 12.0 code check `instanceof`? |
| 15.5 | What does the Limelight know about clusters? |
| 15.6 | How do you get from one tag to the origin? |
| 15.7 | Which way is +x, really? |
| 15.8 | How do you combine several tags, and keep two goals apart? |
| 15.9 | What's built, and what's the project? |

---

## 15.1  The opening has no tag on it

*Where it lives:*  `doc/game-manual/sections/09-9-arena.md`, section 9.6

Start with the field.  The HIVE Structure stands in the middle, holding a red
HIVE and a blue HIVE.  Each HIVE has two CELLS, one at each end, and tips on a
pivot so one CELL faces up at a time.  The manual says *"On the bottom face of
each CELL is a unique AprilTag Cluster containing 4 distinct AprilTags."*
The CELL's opening is about 20 inches wide and 14 inches tall (section 9.6.2).

The robot wants to launch into that opening.  None of the four tags is in the
middle of it.

Billy's turret tracker aims at one tag, chosen by ID.  The ID comes from the
TeleOp, and right now it's a placeholder waiting for this season's answer:

```java
    // TODO: this season's RED-alliance target AprilTag ID goes here (last season's Limelight tag ID was removed during 2027 season prep).
    private static final int TARGET_TAG_ID = -1;
```

That's `OpModes/BillyTeleOpRED.java`, and `BillyTeleOpBLUE.java` has the
same pair of lines for blue.  Suppose you filled in one of this season's
tags.  Two things would go wrong.

**It aims at the wrong point.**  The tracker turns until that tag is centered
(Chapter 5).  The tag sits beside the opening, so the turret points beside the
opening.  The team's cluster plan lists the tags' offsets, and the outer two
sit 6.5 inches to either side of the opening's center
(`doc/limelight3a/cluster-targeting-plan.md`).  Pick an outer tag and the shot
goes 6.5 inches sideways.

**It loses the goal when one tag is covered.**  A defender, or a ball in
flight, blocks the chosen tag.  `lookForTag()` finds nothing, reports
`Tag 30 not found.` (or whichever ID), and stops the turret.  Three other
tags on the same goal are still in plain view, and the tracker ignores them,
because it was asked about one ID.

`LimelightExtensions` has a second helper, `tryFindOneOf`, that takes several
IDs and returns the first match in the camera's list.  Hand it all four and
the tracker keeps a target when one tag is covered.  The aim point moves,
though, every time a different tag comes first, by up to the width of the
group.

*See also:*  15.2, 15.9, Chapter 5, Chapter 13

## 15.2  What is a cluster?

*Where it lives:*  `README.md`, the SDK 12.0 release notes

The SDK's release notes define it:

> *"Clusters are co-planar groups of two or more AprilTags wherein the
> position of each member tag is defined relative to a common origin.  This
> origin may be placed outside the bounds of the tags themselves to provide a
> more suitable "aiming" target."*

So a **cluster** is a group of tags on one flat surface ("co-planar"), each
at a known offset from a shared point.  That shared point is the **cluster origin**, and the
second sentence is the important one.  The origin is a point somebody
*chose*.  Nothing is printed there.  You can find it only because you know
exactly where every tag sits compared with it.

For BIOBUZZ, the release notes put it where you'd want it:  *"the Origin of
each cluster is located in the center of the Cell opening for easy aiming."*

Every member tag comes down to three facts:  its ID, its offset from the
origin, and its size.  See any single member, look up its offset, and you can
work out where the origin is (15.6).

The game manual says which IDs go where (section 9.9), and the team's plan
gives each cluster a name:

| Cluster, as the plan names it | Tag IDs | Where the manual puts it |
|---|---|---|
| RED SCORING | 30, 31, 32, 33 | red CELL, far side from the audience |
| RED AUDIENCE | 34, 35, 36, 37 | red CELL, audience side |
| BLUE AUDIENCE | 38, 39, 40, 41 | blue CELL, audience side |
| BLUE SCORING | 42, 43, 44, 45 | blue CELL, far side from the audience |

Each tag is 3.25 inches square (manual, section 9.9).  The plan's table of
offsets, in inches from the origin, puts the four tags at x = -6.50, -2.75,
+2.75 and +6.50, all at y = 7.1874 and z = -5.622.  In the SDK's cluster
frame +x is to the right as you face the tags and +y is up, so the tags sit
in a row above the origin and a little behind it.

> **Watch out.**  The plan copied those offsets from the SDK's own source
> code, which isn't part of this repository, so nobody can check them here.
> The manual's Figure 9-15 shows the cluster's real dimensions on the printed
> sticker.  Check the plan's numbers against that drawing before you build on
> them.

**A goal that moves.**  The release notes add a warning in bold:
*"Unfortunately, since BIOBUZZ AprilTags move, they are not suitable for
absolute Field Localization."*  The HIVE tips, and the tags tip with it.
That rules out asking the tags where the robot is on the *field* (Chapter
14).  It doesn't hurt aiming at all.  The turret needs to know where the
opening is compared with the *camera*, right now, and a cluster answers
exactly that.

*See also:*  15.3, 15.6, 15.9, Chapter 14

## 15.3  One big solve

*Where it lives:*  `FtcRobotController/.../samples/ConceptAprilTag.java`

The release notes say what a cluster promises:

> *"Clusters are resilient to partial occlusion.  Full 6DOF pose can be
> estimated from a cluster even if only a single member tag is visible.  Of
> course, the more tags that are visible, the better and more stable the pose
> estimate will be"*

"Occlusion" means being blocked from view.  6DOF is the full pose:  three
slides and three turns (Chapter 14).

How does the SDK get there?  Its vision code isn't in this repository, so you
can't open it here.  The lesson this chapter comes from describes the method
like this.  For every tag it can see, the SDK works out where that tag's four
corners *should* be, compared with the cluster origin, using the tag's
offset.  Then it puts every visible corner into **one** pose solve.

It doesn't solve four small squares and average the answers.  It solves one
big shape:  with all four tags in view, sixteen corners spread across about 16
inches.

Why is one big shape better?  Two reasons.

**A wide shape pins down the angle.**  A small tag, far away, covers only a
few pixels.  Turned a little left or a little right, it looks almost the same,
and the solver can flip between two answers that fit nearly equally well.
That's **pose ambiguity**, and small flat targets are known for it.  Corners
spread across sixteen inches can't tilt without their pixel positions
visibly moving, so a flip is much less likely.

**More points means one bad corner matters less.**  Sixteen measurements
agreeing on one answer are steadier than four.  A corner that's off by a pixel
gets outvoted.

The SDK also reports how much of the cluster it saw.  Here's the cluster half
of the SDK's AprilTag sample:

```java
            } else {
                AprilTagClusterDetection clusterDet = (AprilTagClusterDetection) detection;
                telemetry.addLine(String.format("\n==== Tag Cluster (%s)", clusterDet.metadata.name));
                telemetry.addLine(String.format("Percent tags found: %d", clusterDet.percentClusterFound));
                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z));
```

`percentClusterFound` is a whole number, printed with `%d`.  The solve still
works with some tags hidden, and this tells your code how much of the cluster
the answer rests on.  The `ftcPose` here is the pose of the cluster's
*origin*, compared with the camera (Chapter 5).

> **Try it.**  With a webcam, copy `ConceptAprilTag` into TeamCode, remove
> `@Disabled`, and point the camera at a printed BIOBUZZ cluster.  The screen
> shows `==== Tag Cluster` with the cluster's name.  Cover the tags one at a
> time and watch `Percent tags found` drop while the XYZ line keeps
> reporting.

Groups of markers solved as one object are common outside FTC, and the lesson
names three.  OpenCV's ArUco "boards" are sheets of markers at known
positions, solved together.  The `apriltag_ros` package supports "tag
bundles," several tags with one shared origin.  Motion-capture systems track
a "rigid body" marked with several reflective dots, and keep tracking when
some dots are hidden.

*See also:*  15.2, 15.4, 15.8, Chapter 14

## 15.4  `instanceof`:  one detection, two kinds

*Where it lives:*  `FtcRobotController/.../samples/ConceptAprilTag.java`

SDK 12.0 broke older AprilTag code on purpose.  The release notes say
plainly that the cluster feature *"breaks legacy AprilTag OpModes resulting
in compile errors"*, and every sample was rewritten to this shape:

```java
        for (AprilTagDetection detection : currentDetections) {
            if (detection instanceof AprilTagSingleDetection) {
                AprilTagSingleDetection singleDet = (AprilTagSingleDetection) detection;

                if (singleDet.metadata != null) {
                    telemetry.addLine(String.format("\n==== (ID %d) %s", singleDet.id, singleDet.metadata.name));
                    ...
            } else {
                AprilTagClusterDetection clusterDet = (AprilTagClusterDetection) detection;
```

Every detection is an `AprilTagDetection`, and each one is either a single
tag or a whole cluster.  The loop asks which, then treats it as that kind.

> **Aside:  `instanceof`**
>
> `x instanceof SomeType` is a question that answers `true` or `false`:  is
> the object in `x` really a `SomeType`, or a subclass of it (Chapter 1)?
>
> ```java
> if (detection instanceof AprilTagSingleDetection) {
> ```
>
> reads "if this detection is a single-tag detection."  It's usually followed,
> as here, by a cast (Chapter 7), which tells Java to treat the object as that
> more specific type, so you can reach the members only that type has.

Why did the ID move?  Because a cluster doesn't have one ID.  It has four.
`singleDet.id` makes sense for a single tag and none for a cluster, so `id`
lives on `AprilTagSingleDetection`, the class where it's true.

Look at what's left on the parent.  Both halves of the sample read
`detection.ftcPose` straight off the `AprilTagDetection`, with no cast.  Every
detection has a pose, single or cluster, so the pose is on the class every
detection shares.

That's the rule behind the change:  put a thing on the most general class that
can honestly promise it, and no higher.  The team's layered robot model
follows the same rule (Chapter 6).

> **Watch out.**  Webcam AprilTag code written before SDK 12.0 that reads
> `detection.id` fails to compile now.  The
> fix is the shape above:  check `instanceof`, cast, then read `id` from the
> single detection.

*See also:*  15.3, 14.2, Chapter 1, Chapter 6, Chapter 7

## 15.5  The Limelight sees tags one at a time

*Where it lives:*  `Extensions/LimelightExtensions.java`

Billy aims with a Limelight 3A, and the Limelight has never heard of an FTC
cluster.  Every frame, it hands back a list with one
`LLResultTypes.FiducialResult` for each tag it sees.  Clusters live in the
SDK's own vision code, which the Limelight never runs.

So the cluster idea has to be rebuilt in TeamCode, and the team's code already
says where.  At the top of `LimelightExtensions`:

```java
    // TODO (students): BIOBUZZ goals are AprilTag *clusters* (4 tags sharing one origin).
    // Add cluster search + origin estimation here - see doc/limelight3a/cluster-targeting-plan.md
```

Each `FiducialResult` does carry what the rebuild needs.  Besides `tx`, it
has `getTargetPoseCameraSpace()`:  the tag's position *and* orientation,
measured in the camera's frame, in **meters** and degrees
(`doc/limelight3a/data-shapes.md`).  That's a full pose for one tag, and
15.6 turns it into the origin.

The rebuild also needs the offsets from 15.2.  The plan says TeamCode can't
read them out of the SDK while the robot runs, because the SDK keeps them
where outside code can't reach them.  So the team copies them into its own
class.  That's Task 2 of the plan.

Copying has a cost.  If FIRST ever changes the goal's geometry, the SDK gets
an update and the team's copy doesn't, and nothing warns anyone.  Write where
the numbers came from, and when, in a comment beside them.

*See also:*  15.2, 15.6, 15.9, Chapter 2, Chapter 5

## 15.6  From one tag to the origin:  rotating an offset

*Where it lives:*  `doc/limelight3a/cluster-targeting-plan.md`, Task 4

The goal is a **rigid body**:  a thing whose parts are fixed to one another,
so they move together.  The tags are printed on one sticker, stuck on one
CELL, and the opening is part of that same CELL.  Know where one tag is *and
which way it's facing*, and you know where every other point on the goal is.

`getTargetPoseCameraSpace()` gives exactly that for one tag (15.5).  So the
recipe, in words, is:

1. Start at the tag's position.
2. Take the arrow from that tag to the origin.  That's the tag's offset from
   15.2, reversed.
3. **Turn that arrow to match the way the tag is facing.**
4. Add it on.

Turning an arrow to match a new direction, before you add it, is **rotating an
offset**.  Step 3 is the one people skip, because on the bench it seems to
make no difference.

Work it on paper first.  The camera is 30 inches from the goal, looking
straight at it, and it sees only the leftmost tag.  The origin is 6.5 inches
to that tag's right, along the goal's own left-right direction.  Head-on,
that's 6.5 inches to the camera's right.  Easy.

Now turn the goal 30 degrees.

> **Predict.**  The origin is still 6.5 inches along the goal's left-right
> direction, but that direction now points partly away from the camera.  If
> you skip step 3 and just add 6.5 inches sideways, about how far off are you?

Draw it as a right triangle with a long side of 6.5 inches, tipped 30 degrees.
Its two short sides are the parts you need:

| Part | Arithmetic | Inches |
|---|---|---|
| camera-sideways | 6.5 × cos 30° | about 5.63 |
| camera-depth | 6.5 × sin 30° | 3.25 |

Cos 30° is about 0.866 and sin 30° is exactly 0.5;  any calculator's `cos`
and `sin` keys give them.

The unrotated guess said 6.5 sideways and 0 in depth.  The truth is 5.63
sideways and 3.25 in depth.  The two points are about 3.4 inches apart, which
is the square root of 0.87² + 3.25².

That's the trap.  "Just add the offset" works perfectly on the bench, where
you test head-on, and then misses in a match, where the robot is almost never
head-on.

> **Try it.**  Redo the exercise with the goal turned the other way, -30
> degrees.  Before you compute, guess:  does the error change size, direction,
> or both?

*See also:*  15.2, 15.5, 15.7, Chapter 14

## 15.7  Which way is +x, really?

*Where it lives:*  `doc/limelight3a/cluster-targeting-plan.md`, Task 3

Every frame has its own axes, and nothing promises two frames agree.  The
SDK's cluster frame has +x to the right as you face the tags and +y up (15.2).
The Limelight reports in its own camera and target frames, and the team's
reference says to confirm frames *"before combining coordinates"*
(`doc/limelight3a/data-shapes.md`).

Get the sign of x wrong and the turret aims at a point mirrored to the other
side of the tag.  With an outer tag the only one in view, that's 6.5 inches
the wrong way where 6.5 inches the right way was wanted:  13 inches off.  The
code still reads perfectly sensibly, so a code review won't catch it.

Billy's tracker already depends on a sign that only the robot can confirm.
Here's the state that runs when the tag is to the right:

```java
    public IState rotateCW(double tx) {
        return () ->
        {
            double power = -1;
            if (tx < 10)
                power = -tx / 10.0;
            turret.setPower(power);
            ...
```

A tag to the right means positive `tx`, and both branches send negative power.
Whether negative power swings this turret toward the right is a fact about its
servo and gearing, and the way to know is to watch it turn.  The new cluster
math adds three more signs, one per axis, and each one gets the same
treatment.

That's why Task 3 is a bench test, before any math.  Write a test OpMode
(the plan suggests `OpModes/LimelightClusterProbe.java`) that shows, for
every visible tag, its ID, its `getTargetXDegrees()`, and the x, y and z of
its `getTargetPoseCameraSpace()`.

> **Try it.**  Set the Limelight pipeline's tag size to 3.25 inches (82.55 mm)
> in the camera's web page.  Hold a printed tag in front of the camera and
> move it **right**, then **up**, then **away**.  Write down which number
> changes each time and which way it goes.  Tilt the tag and note which angle
> changes.  Measure the real distance with a tape measure and compare it with
> the number that grew when you moved the tag away, remembering the Limelight
> reports meters.  Record the result
> as "+x = ___, +y = ___, +z = ___" in the plan's *Results* section.

*See also:*  15.2, 15.6, 15.9, Chapter 5

## 15.8  Combining tags, and keeping two goals apart

*Where it lives:*  `doc/limelight3a/cluster-targeting-plan.md`, Task 5

With three tags in view, 15.6 gives three guesses at the origin.  They'll
disagree a little, because every measurement has some wobble.  Which one
should you trust?

All of them, but not equally.  A tag that's bigger in the image, because it's
closer or more head-on, has sharper corners and a steadier pose.  Each
`FiducialResult` reports its size in the image as `getTargetArea()`.

A **weighted average** gives each guess a weight, then divides by the total
weight, so heavier guesses pull harder.  Two guesses, 10 inches with weight 3
and 12 inches with weight 1:

| | Arithmetic | Result |
|---|---|---|
| plain average | (10 + 12) ÷ 2 | 11 |
| weighted average | (3 × 10 + 1 × 12) ÷ (3 + 1) | 10.5 |

The weighted answer leans toward the guess you trust more, the same idea as
the standard deviations in 14.6.  The weights only have to be right compared
with one another, which helps, because the Limelight's area scale needs
checking against real camera output (`doc/limelight3a/data-shapes.md`).

**Which goal is which?**  From some spots on the field, two goals are in
view at once.  Average every visible tag and the answer comes out halfway
between the two goals, at a point nobody wants to hit.  Each guess has to
belong to the right cluster first.  The ID settles it:  tags 30 to 33 are
RED SCORING, and no other tag is.  That's data association (14.8), and
because AprilTags print their own IDs, it's a table lookup.

**Averaging against one big solve.**  Averaging per-tag guesses has a real
cost:  it's weaker than the SDK's single solve (15.3), because each guess
still leans on one small tag's shaky orientation.  What it buys is a method
the team can write and check this season, on a camera that knows nothing
about clusters, in code a mentor can read line by line.  That's a trade-off,
and the plan's Task 5 takes it.

There's a stronger route on the table too.  The Limelight can do its own
multi-tag solve if you upload a field map (`uploadFieldmap`, in
`doc/limelight3a/java-objects.md`).  The plan's stretch Task 8 suggests a map
that places one cluster's four tags around the point (0, 0, 0), so that
`getBotpose()` comes back as the robot's pose *compared with that goal*.

> **Argue it.**  A field map covers every tag it lists.  When two goals are in
> view, what does the Limelight's one solve do with tags from both?  Pipelines
> and ID filters might help.  Is the stronger solve worth that problem, or does
> Task 5's average win for this season?  The plan asks for the team's written
> recommendation:  "keep Task 5," "switch to the field map," or "use both,
> because..."

*See also:*  15.3, 15.6, 15.9, 14.6, 14.8

## 15.9  The cluster plan:  what's built, and what's next

*Where it lives:*  `doc/limelight3a/cluster-targeting-plan.md`

The plan is the team's next project:  eight tasks in order, each with a "done
when" check to show a mentor.  Here's where each stands in the code today.

| Task | The job | Status |
|---|---|---|
| 1 | make the tag helpers safe with no result yet | the `null` guard is in;  the `isValid()` check is open |
| 2 | a `TagCluster` class, with all four clusters | open.  `Extensions/TagCluster.java` doesn't exist yet. |
| 3 | bench test for the axis signs (15.7) | open.  No probe OpMode yet, and *Results* is empty. |
| 4 | `estimateClusterOrigin`, origin from one tag (15.6) | open |
| 5 | `tryGetClusterOrigin`, all visible tags combined (15.8) | open |
| 6 | turn the origin into an aiming angle | open |
| 7 | aim the turret at the cluster | open |
| 8 | stretch:  try a Limelight field map | open |

Task 1's first half is already in both helpers.  Each one now checks the
result before using it:

```java
        LLResult result = limelight.getLatestResult();
        if (result == null)
            return null;
```

So a frame that hasn't arrived yet means "no tag," and `lookForTag()` reports
the tag missing and stops the turret.  The `isValid()` check the plan also
asks for is still to do (`doc/limelight3a/java-objects.md` explains it).

Tasks 1 and 7 each point at a `TODO` comment that has since been resolved and
removed.  Read `LimelightExtensions` and `rotateCW` as they are now (15.7).

Task 6 is short and easy to get wrong.  Turn the origin's camera-space x and z
into a horizontal angle with `Math.atan2`, which returns **radians**.  Convert
to degrees before you compare it with anything `tx`-shaped.  The check:  with
the camera fixed and the CELL opening centered in the image, the angle reads
about zero, even though no tag is at the center.

Task 7 has one new requirement the plan predates.  `LimelightAutoTarget` is
now an assist strategy that `BillyRobot` starts and stops, and it declares
that it claims the turret (Chapter 13).  A cluster version has to keep those
methods:  `claims()`, `name()`, `start()`, `update(...)` and `stop()`.  The
RED and BLUE TeleOps' `TODO` asking for this season's target (15.1) gets its
answer there too, and the answer is a cluster.

*See also:*  15.1, 15.5, 15.7, 15.8, Chapter 13

---

## Chapter summary

The opening Billy aims at has no tag on it.  A cluster turns four tags into
one rigid object with an origin at the opening's center, so any one visible
tag can say where the target is.  The SDK solves every visible corner at once.
The Limelight reports tags one at a time, so the team's plan rebuilds the idea
in TeamCode:  rotate each tag's offset, average the guesses with weights, and
keep each goal's tags together by ID.

| Idea | Entry |
|---|---|
| the origin is chosen;  nothing is printed there | 15.2 |
| one big solve beats four small ones | 15.3 |
| rotate the offset before you add it | 15.6 |
| measure every axis sign on the bench | 15.7 |
| weight the guesses, and sort them by ID | 15.8 |

### Java in this chapter

| Syntax | Where |
|---|---|
| `instanceof` | Aside in 15.4 |
| a cast, `(AprilTagSingleDetection) detection` | 15.4.  Casts are in Chapter 7. |
| `Math.atan2`, which returns radians | 15.9 |

### Vocabulary

**cluster** -- a group of AprilTags on one flat surface, each at a known
offset from a shared origin.  (15.2)

**cluster origin** -- the chosen point a cluster's offsets are measured from.
In BIOBUZZ, the center of the CELL opening.  (15.2)

**`instanceof`** -- Java's question "is this object really that type?"
(15.4)

**pose ambiguity** -- when a small, flat target fits two different poses
nearly equally well, so the solver can flip between them.  (15.3)

**rigid body** -- an object whose parts are fixed to one another and move
together.  (15.6)

**rotating an offset** -- turning an arrow to match the direction something
faces, before adding it on.  (15.6)

**weighted average** -- an average where each value counts in proportion to
its weight.  (15.8)

### Check your understanding

1. Why does aiming at a single BIOBUZZ tag aim at the wrong point, and what
   else goes wrong when that tag is blocked?  (15.1)
2. What is a cluster's origin, and how can the SDK find it with nothing
   printed there?  (15.2)
3. Why does the SDK solve every visible corner at once, and what does the team
   give up by averaging on the Limelight instead?  (15.3, 15.8)
4. Why did `id` move to `AprilTagSingleDetection` in SDK 12.0, while
   `ftcPose` stayed on `AprilTagDetection`?  (15.4)
5. Using the 30-degree example, explain why the offset has to be rotated
   before it's added.  Then do it for -30 degrees.  (15.6)
6. Why do the axis signs have to be measured, and what does a wrong x sign do
   to the aim point?  (15.7)
7. Two goals are in view.  Why can't the code average every visible tag?
   (15.8)
8. Why is it fine to aim at a goal that moves, but not to find the robot's
   field position from it?  (15.2)
9. Which task in the plan comes first, and why does the bench test come
   before the math?  (15.7, 15.9)

## Where to go next

Chapter 5 starts the tags, `tx`, and the turret tracker, and Chapter 14 is the
pose idea this chapter points at a goal.  The tracker's place in Billy's
arbitration, and the methods a cluster version has to keep, are Chapter 13.
When the project starts, open `doc/limelight3a/cluster-targeting-plan.md` at
Task 1.
