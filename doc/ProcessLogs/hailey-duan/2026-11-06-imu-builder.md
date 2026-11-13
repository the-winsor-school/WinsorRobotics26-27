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

> no don't need to update wildbots 2025

> how many steos are there total

> can you do4) or no

> ok now step 5 repeat

> idk bro

> no it's the right version

> no bro the wheels are fine

> why did you rmeove the intake!??!?!?

> bro what the natke is in SpinnyIntakeMA

> no i mean the inatke isn't removed, it's spinnyintakema

> so your description is wrong

> what is step 6

> wait what but when we run the robot the teleop uses the spinnyinatkema

> wait help lol

> no bro don't change the wheels

> why you ad dthe spinny intake import statement!??!?

> ?!?!? rlly!??!

> wait then if mr.cox didn't add the intake import statement i dont' need it

> ok yeah just keep this

> ok so done with step 6?

> helpo heh

> helpo

> bruh what i don't need to do this

> jajjajaajajaj i'll do it when he comes back

## Plan
Numbered steps, written down before any code changed.

1. Create `ImuBuilder` (`.logoFacing(...)`, `.usbFacing(...)`, `.build()`), which fetches the
   IMU, initializes it and resets yaw: `RobotModel/Robots/ImuBuilder.java`
2. Use it in Billy, then test on Billy: `RobotModel/Robots/BillyRobot.java`
3. ~~Use it in Wildbots, in its own commit, keeping the orientation TODO:
   `RobotModel/Robots/Wildbots2025.java`~~ Dropped 2026-11-13 (see Decisions).
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
- Step 4 (2026-11-13): `doc/NewRobotDesignWorkflow.md` now shows `ImuBuilder` in the
  BillyRobot construction steps, the NewRobot template and the dependency-injection list,
  so the template matches the code. Claude wrote this doc change at Hailey's request
  ("can you do4) or no").
- Step 5 (2026-11-13): `OpModes/ExampleSimpleMecanumTeleOp.java` uses `ImuBuilder` in place
  of its own IMU setup, and the unused `BillyMA` import is removed. `BillyRobot.java`: the
  indent of `super(telemetry);` is fixed.

## Decisions
- 2026-11-13: Step 3 dropped; Hailey chose not to change Wildbots2025. Its hardware no
  longer exists, so the change couldn't be tested. As a result, Wildbots2025 keeps its own
  `hardwareMap.get(IMU.class, "imu")`, and `"imu"` is fetched in more than one place.
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
- Step 4: doc only, nothing to run.
- Step 5: compiled only (passed 2026-11-13). Not yet run on the new chassis.

## In my own words
Written by me, not by Claude.

Claude's questions (2026-11-13):
1. In `BillyRobot`, the IMU has to be built before `MecanumDriveBuilder`. Why? What would go
   wrong if the two blocks were swapped?
2. `ImuBuilder.build()` calls `resetYaw()`. When does that happen during a match, and what
   does "yaw = 0" mean for the robot at that moment?
3. What happens if someone writes `new ImuBuilder(hardwareMap).logoFacing(RIGHT).build()`
   and forgets `usbFacing`? Why is that better than the IMU quietly starting up wrong?

My answers:

## Still unknown
- 2026-11-13: While doing step 5, `ExampleSimpleMecanumTeleOp` also went back to wheel
  directions lf F, lb F, rf R, rb R (the same as Jason Cox's 0e58124) and lost its
  `SpinnyIntakeMA`. Hailey says this is the right version. The weekend test that worked on
  the new chassis used the OTHER directions (lf R, lb R, rf F, rb F, from 125f308) with the
  intake, so this version has NOT been tested. It needs its own commit and a run on the new
  chassis.
  Later the same day: Hailey decided to keep this version, which matches Jason Cox's
  0e58124: drive only, lf F, lb F, rf R, rb R. `SpinnyIntakeMA` (added in 125f308 on
  fair day) stays in the repo but is no longer used by this TeleOp. The robot is still
  running the weekend install, so these directions and the missing intake are untested
  until the next install on the new chassis.
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
- For a mentor: the Field Guide quotes BillyRobot's old IMU block, which is now out of date:
  `doc/FieldGuide/chapters/chapter_03_hardware_toolbox.md` (~line 367),
  `chapter_14_pose_and_navigation.md` (~line 299) and `doc/FieldGuide/planning/code-facts.md`
  (~line 347). These were left alone because the Field Guide is a mentor's project.

## Commits
A commit can't contain its own hash, so add each hash the next time you update this log.

- cc70f94: Add ImuBuilder and BillyRobot uses ImuBuilder (steps 1 and 2 together)
- f134eb1: Simple Mecanum TeleOp and new-robot doc use ImuBuilder (steps 4 and 5). This commit
  also accidentally reverted the TeleOp's wheel directions and dropped `SpinnyIntakeMA`.
  Hailey later chose to keep this drive-only version.
