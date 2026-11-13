# BillyRobot builder (practice)

Student: Hailey Duan
Started: 2026-11-13
Branch: hailey/billy-robot-builder (made from hailey/imu-builder, because BillyRobot uses
ImuBuilder there)
Assistant: Claude Code (Opus 5.5)

## Goal
Practice the builder pattern on Billy, last season's robot. A `BillyRobotBuilder` names the
settings that are hard-coded or passed by position today (the target tag, auto-aim at start
and how many balls rapid fire shoots), so the OpModes read like a list of choices.

## What I asked for
My requests to Claude, copied word for word, in order. Include follow-ups and corrections.

> builder practice for billyrobot (though that is last season) cuz the simple-meanucm  thing is the one for this year/with the wheels and stuff; also could i do one for billyrapidfire and strategies as advanced to-do project

> start the new branch from hailey/imu-builder

> sure,  "grow RobotBuilder" project TODO is fine for billyrapidfire which i helped right last year in a class

## Plan
Numbered steps, written down before any code changed.

1. Create `BillyRobotBuilder` with `.tagID(int)` and `.build()`, which returns
   `new BillyRobot(...)`. Use it in the three places that build Billy:
   `RobotModel/Robots/BillyRobotBuilder.java`, `OpModes/BillyTeleOpRED.java`,
   `OpModes/BillyTeleOpBLUE.java`, `OpModes/ExampleStateMachineAuton.java`
2. Add `.autoAimOnStart(boolean)`. BillyRobot gets a constructor argument, and its
   `startAssist(targeter)` at start only runs when that argument is true:
   `BillyRobotBuilder.java`, `RobotModel/Robots/BillyRobot.java`
3. Add `.rapidFireShots(int)`, passed through BillyRobot into BillyMA in place of the
   hard-coded `3`: `BillyRobotBuilder.java`, `BillyRobot.java`,
   `RobotModel/Mechs/Assemblies/BillyMA.java`
4. Test on Billy, answer the "In my own words" questions, then open a pull request (only
   after hailey/imu-builder has merged).

## What changed
One line per change: file, what changed, why.

- Step 1a (2026-11-13): `RobotModel/Robots/BillyRobotBuilder.java` is new. `.tagID(int)`
  stores the target tag in an `Integer`, so "never set" is `null`. `build()` throws if it's
  missing and otherwise returns `new BillyRobot(hardwareMap, telemetry, tagID)`. Hailey
  wrote it from a skeleton; Claude reviewed it.
- Step 1b (2026-11-13): `OpModes/BillyTeleOpRED.java`, `BillyTeleOpBLUE.java` and
  `ExampleStateMachineAuton.java` build Billy with
  `new BillyRobotBuilder(hardwareMap, telemetry).tagID(TARGET_TAG_ID).build()`. RED and
  BLUE drop the unused `BillyRobot` import; the Auton keeps it because its field is a
  `BillyRobot`. Behavior is unchanged.
- Step 2 (2026-11-13): `BillyRobot.java` takes a 4th constructor argument,
  `boolean autoAimOnStart`, and calls `startAssist(targeter)` only when it's true.
  `BillyRobotBuilder.java` adds `.autoAimOnStart(boolean)` and passes it to the
  constructor. The targeter itself doesn't need it, because it only knows how to aim.

## Decisions
- 2026-11-13: `.autoAimOnStart(...)` is optional, and the builder's field defaults to
  `true`, so Billy behaves like before unless an OpMode asks otherwise. A plain `boolean`
  with no value would have defaulted to `false` and quietly turned off auto-aim. The default
  belongs in the builder, not BillyRobot, because the builder decides what happens when a
  setting isn't given.
- 2026-11-13: The OpModes use the `telemetry` they inherit from `LinearOpMode`, which is the
  one connected to the Driver Station. Hailey first wrote `Telemetry telemetry = new
  Telemetry();`, which doesn't compile (Telemetry is an interface) and would have hidden
  the real one.
- Billy is last season's robot, so this is practice. The drivers don't need to be asked
  first (this changes the earlier plan, which said to ask them).
- The builder configures `BillyRobot`; it doesn't replace it. The class still owns its
  hardware and strategies.
- Step 1 alone (just a tag ID) doesn't need a builder: a constructor argument does the same
  job. Steps 2 and 3 are what make it worth having.
- Out of scope: the builder for BillyRapidFire and strategies (the "SequenceBuilder" idea)
  is a separate advanced project. It needs a mentor's OK first, because AGENTS.md counts a
  new framework as needing approval.
  Hailey filed it under the existing "grow RobotBuilder" project TODO in `Robot.java`. She
  helped write BillyRapidFire last year in a class.

## Deviations and approvals

## Testing
For each change: run on hardware / compiled only / not tested, and what I saw.

- Step 1a: compiled only (passed 2026-11-13). Nothing calls the builder yet.
- Step 2: compiled only (passed 2026-11-13). Not yet run on Billy. To test: with no
  `.autoAimOnStart(...)` call, auto-aim is on at start like before (the turret tracks, and
  y turns it off). With `.autoAimOnStart(false)`, it's off until y is pressed.
- Step 1b: compiled only (passed 2026-11-13). Not yet run on Billy. To test: BillyRED or
  BLUE initializes and drives, and auto-aim and rapid fire work as before. The Example
  State Machine Auton shows "auton ready" at init.

## In my own words
Written by me, not by Claude.

## Still unknown
- The target tag IDs are still `-1` (an existing TODO in BillyTeleOpRED/BLUE). This task
  doesn't fix that TODO.
- This branch includes hailey/imu-builder's untested commits. Its pull request should wait
  until that branch is tested and merged.

## Commits
A commit can't contain its own hash, so add each hash the next time you update this log.
