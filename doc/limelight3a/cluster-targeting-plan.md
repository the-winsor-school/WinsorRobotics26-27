# TODO plan: aim the Limelight at an AprilTag cluster

**For:** students. Mentors should give hints, not code.
**Goal:** the turret aims at the center of a BIOBUZZ Cell opening, even when only some of that goal's tags are visible.

## Background: what a "cluster" is

FTC SDK v12.0 added AprilTag **clusters**: a group of tags on one flat surface, each placed at a known offset from a shared **origin**. In BIOBUZZ, each goal has 4 tags, and the origin is the center of the Cell opening. That's the point we want to shoot at.

The SDK's `VisionPortal` / `AprilTagProcessor` handles clusters automatically. **The Limelight doesn't.** It only reports each tag separately (`LLResultTypes.FiducialResult`). We have to do the cluster math ourselves.

Read these first:
- **Flint lesson:** [Advanced/AprilTag Clusters](../FlintLessons/Advanced/apriltag-clusters.md). Take it before you start Task 1. It explains *why* each task exists.
- [README.md v12.0 notes](../../README.md#L62-L108): the "About AprilTag clusters" section
- [ConceptAprilTag.java](../../FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/ConceptAprilTag.java): see how it separates `AprilTagSingleDetection` from `AprilTagClusterDetection`
- [java-objects.md](java-objects.md) and [data-shapes.md](data-shapes.md): the "Pose shape and reference frames" section (Limelight units are **meters**)
- [LimelightExtensions.java](../../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Extensions/LimelightExtensions.java) and [LimelightAutoTarget.java](../../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/AutonStrategies/LimelightAutoTarget.java): our current code

## BIOBUZZ cluster geometry (from the SDK source)

This comes from `AprilTagGameDatabase.getBioBuzzTagLibrary()` in the Vision 12.0.0 sources jar. **TeamCode can't read these values from the SDK at runtime** because the fields are package-private, so we copy them into our own code.

| Cluster | Tag IDs (L1, L2, R1, R2) |
|---|---|
| RED SCORING | 30, 31, 32, 33 |
| RED AUDIENCE | 34, 35, 36, 37 |
| BLUE AUDIENCE | 38, 39, 40, 41 |
| BLUE SCORING | 42, 43, 44, 45 |

Each tag's center relative to the cluster origin, in **inches**, with tag size **3.25 in**:

| Member | x | y | z |
|---|---|---|---|
| L1 | −6.50 | 7.1874 | −5.622 |
| L2 | −2.75 | 7.1874 | −5.622 |
| R1 | +2.75 | 7.1874 | −5.622 |
| R2 | +6.50 | 7.1874 | −5.622 |

The SDK's cluster frame has +x to the right as you face the tags and +y up. The tags sit 5.622 in behind the origin in z. **Limelight's target frame may use different signs.** Task 3 is where you find out.

Remember: BIOBUZZ goals **move**. Use clusters for aiming, not for field localization.

---

## Tasks

Do the tasks in order. Each one has a **done when** check. Show a mentor before you move on.

### ☐ Task 1: Fix the null crash first
There's a `TODO` in `LimelightExtensions` about `getLatestResult()` returning `null`.
- Make `tryGetFiducial` and `tryFindOneOf` safe when there's no result yet. Also check `isValid()`.
- **Done when:** a TeleOp that starts with the Limelight unplugged doesn't crash, and reports "no target."
- *Think about:* what should the turret do when there's no data? (See `stopTurret()`.)

### ☐ Task 2: Model the cluster as data
Create a small class, for example `Extensions/TagCluster.java`, that holds:
- a name (`"RED SCORING"`)
- the member tag IDs
- each member's offset from the origin

Then add constants for all four BIOBUZZ clusters.
- Pick one unit (meters or inches) and use it everywhere. Limelight poses are in meters.
- Add a method to answer "is tag 32 in this cluster?"
- **Done when:** a mentor can read the class and check every number against the table above.
- *Think about:* the tag IDs follow a pattern, with the first ID + 0..3. Could one constructor build a cluster from just the first ID and the name?

### ☐ Task 3: Bench test to find the axis signs (no driving!)
Write a test TeleOp, for example `OpModes/LimelightClusterProbe.java`. For every visible tag, it should show on telemetry:
- the ID
- `getTargetXDegrees()`
- the x, y, z from `getTargetPoseCameraSpace()`

Before you start, set the Limelight pipeline's tag size to 3.25 in (82.55 mm) in the web UI.

Hold a printed tag in front of the camera and move it **right**, **up**, and **away**. Write down which axis changes and which sign it goes. Also tilt the tag and write down which angle changes.

Measure the real distance with a tape measure and compare it to the reported z.
- **Done when:** the team has a table like "Limelight camera space: +x = ___, +y = ___, +z = ___". Commit it to this doc under *Results* below.
- *Think about:* why is it dangerous to guess these signs instead of measuring them?

### ☐ Task 4: Find the origin from ONE tag
Write `LimelightExtensions.estimateClusterOrigin(FiducialResult tag, TagCluster cluster)`. It should return where the cluster origin is in camera space.
- Idea: you know where the tag is (its camera-space pose). You also know where the origin is *relative to the tag* (the negative of the member's offset). Rotate that offset by the tag's orientation, then add it to the tag's position.
- Use the signs you measured in Task 3.
- **Done when:** with the probe OpMode pointed at a mock-up, covering different tags gives an origin estimate that stays in about the same place (within ~1 in).
- *Think about:* why must you rotate the offset before you add it? Try it without rotating: what goes wrong when the goal is viewed from an angle?

### ☐ Task 5: Combine all the visible tags
Write `LimelightExtensions.tryGetClusterOrigin(Limelight3A limelight, TagCluster cluster)`. It should:
1. collect every visible fiducial whose ID is in the cluster
2. estimate the origin from each one (Task 4)
3. average them, and return `null` if no member tag is visible

Also report how many tags were used. This is like the SDK's `percentClusterFound`.
- Stretch: weight each tag by `getTargetArea()`, so bigger (closer, clearer) tags count more.
- **Done when:** the origin estimate is steadier with 3–4 tags visible than with 1. Show this on telemetry.
- *Think about:* what happens if tags from *two* clusters are in view? Does your filter keep them apart?

### ☐ Task 6: Turn the origin into an aiming angle
Convert the averaged origin (x, z in camera space) into a horizontal angle, like `tx` but pointing at the origin. Use `Math.atan2`, and watch your units: radians vs. degrees.
- **Done when:** with the camera fixed, the angle reads about 0° when the Cell opening is centered in the image, even though no tag is at the center.

### ☐ Task 7: Aim the turret at the cluster
Update `LimelightAutoTarget`:
- Take a `TagCluster` instead of a single `int tagId`, and aim with the Task 6 angle.
- Update `BillyRobot`, `BillyTeleOpRED`, and `BillyTeleOpBLUE`. The RED/BLUE TeleOps have a `TODO` asking for this season's target, and the answer is now a cluster.
- **Before you run it on the robot:** read the `TODO` in `rotateCW` about the sign bug, and decide whether to fix it first.
- **Done when:** on the robot, the turret centers on the Cell opening and stays centered while you cover tags one at a time.

### ☐ Task 8 (stretch): Try a field map instead
The Limelight can do its own multi-tag solve (MegaTag) if you upload a field map (`uploadFieldmap(LLFieldMap, …)`; see [java-objects.md](java-objects.md)). If the map places one cluster's four tags around (0,0,0), then `getBotpose()` is the robot's pose *relative to that goal*.
- Build it, test it, and compare its steadiness with Task 5.
- Write down the catch: a field map covers every tag it lists. What goes wrong when two goals are visible, and how could pipelines or ID filters help?
- **Done when:** the team writes a short recommendation below: "keep Task 5," "switch to the field map," or "use both, because…".

---

## Results
*(Students: fill in as you go.)*

- **Task 3: measured Limelight camera-space axes:**
- **Task 5: origin steadiness, 1 tag vs. 4 tags:**
- **Task 8: recommendation:**
