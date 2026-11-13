# IMU builder

Student: Hailey Duan
Started: 2026-11-06
Branch: hailey/imu-builder
Assistant: Claude Code (Opus 5.5)

## Goal
Move the IMU setup that is copied into `BillyRobot` and `Wildbots2025` into one `ImuBuilder`,
so the `"imu"` device has one `hardwareMap.get(...)` and each robot only states its hub
orientation.

## What I asked for
My requests to Claude, copied word for word, in order. Include follow-ups and corrections.

> put all other builders in to-do for later: IMU builder as netx thing, then billyrobotbuilder once ask drivers, and then sequencebuilder

> continue doing builders

### 2026-11-13

> let's just do imu builder now, i'll test later, and just not merge branch rn

## Plan
Numbered steps, written down before any code changed.

1. Create `ImuBuilder` (`.logoFacing(...)`, `.usbFacing(...)`, `.build()`), which fetches the
   IMU, initializes it and resets yaw: `RobotModel/Robots/ImuBuilder.java`
2. Use it in Billy, then test on Billy: `RobotModel/Robots/BillyRobot.java`
3. Use it in Wildbots, in its own commit, keeping the orientation TODO:
   `RobotModel/Robots/Wildbots2025.java`
4. Update the new-robot template: `doc/NewRobotDesignWorkflow.md`
5. (Added 2026-11-13) Use it in the Simple Mecanum example, which was merged to master after
   this plan and also sets up the IMU itself:
   `OpModes/ExampleSimpleMecanumTeleOp.java`
6. Answer the "In my own words" questions, then open a pull request for a mentor to review.

## What changed
One line per change: file, what changed, why.

- Step 1 (2026-11-13): `RobotModel/Robots/ImuBuilder.java` is new. `logoFacing(...)` and
  `usbFacing(...)` store the hub orientation. `build()` checks both were set, fetches
  `"imu"`, initializes it and resets yaw, so each robot states only its orientation.
  Hailey wrote it from a skeleton; Claude reviewed it.
- Step 2 (2026-11-13): `RobotModel/Robots/BillyRobot.java` replaces its copied IMU block with
  `new ImuBuilder(hardwareMap).logoFacing(RIGHT).usbFacing(UP).build()`. The orientation is
  unchanged, and the robot still holds `imu` and passes it to `MecanumDriveBuilder`.

## Decisions
- Builder rather than a static helper in `Extensions/`: the IMU has only two settings, so a
  helper would also work. Hailey chose the builder when she queued this work, and it matches
  `RobotBuilder` and `MecanumDriveBuilder`. Mentor can weigh in on the pull request.
- The robot still calls the builder and keeps the IMU, so the robot stays the owner.

## Deviations and approvals
- AGENTS.md says to ask a mentor before adding a new design pattern. The builder pattern is
  already in the repo and listed as an existing tool, so this was not treated as a new
  pattern. No approval recorded yet.

## Testing
For each change: run on hardware / compiled only / not tested, and what I saw.

- Step 1: compiled only (`./gradlew :TeamCode:compileDebugJavaWithJavac` passed 2026-11-13).
  Nothing calls `ImuBuilder` yet, so there is nothing to run on hardware until step 2.
- Step 2: compiled only (passed 2026-11-13). NOT yet tested on Billy. To test: run Billy's
  TeleOp, check that yaw is ~0 at start and rises when turning counter-clockwise, and that
  driving and auto-aim behave as before.

## In my own words
Written by me, not by Claude.

## Still unknown
- 2026-11-13: Hailey is writing the code now and testing on Billy later. The branch stays
  unmerged until it has been tested.
- `ExampleSimpleMecanumTeleOp` was written by a mentor and has its own open wheel-direction
  question. Step 5 only touches its IMU lines.
- 2026-11-13: Hailey ran that TeleOp on the new chassis over the weekend, and drive, strafe,
  turn and the intake all worked. Its IMU orientation (logo RIGHT, USB UP) is copied from
  Billy and hasn't been checked on the new chassis. Check yaw (~0 at start, rising
  counter-clockwise) when testing step 5.
- Wildbots2025 can't be run because its hardware doesn't exist, so step 3 can't be tested.
  AGENTS.md says untested code stays on its branch. Ask a mentor whether step 3 belongs in
  this pull request.
- Wildbots' hub orientation is copied from Billy and has never been checked.
- For a mentor: `doc/FlintLessons/GettingStarted/02-ftc-hardware-toolbox.md` shows raw IMU
  setup. That is still valid SDK teaching, so it was left alone (lessons are mentor-owned).

## Commits
A commit can't contain its own hash, so add each hash the next time you update this log.
