# Chapter 6 notes

Drafted 2026-10-26 by Claude (drafter for Chapters 6-8).
Robot code baseline:  `305fb81`, plus the uncommitted `TODO` in
`DoublyLimitedMotor.java` recorded in BACKLOG.  Every Java block is copied
from the working tree by line range, and `reference/check_chapters.ps1`
verifies each one verbatim.

Progress:  chapter, opening, and notes complete;  voice checks run.  Next:
Chapter 7.

Size:  about 27 KB, a little over the 25 KB guide, because 6.4 traces one
button through every layer in real code.  Trim candidates if needed:  the
`Robot` field block in 6.1, or the Try it in 6.4.

## Sources

- `doc/FlintLessons/01-layered-robot-model.md`
- `doc/AbstractRobotObjectModel.md` (the "inheritance defines the allowed
  shapes, composition builds the real machine" line, 6.7)
- `doc/game-manual/` (30-second AUTO, 2-minute TELEOP;  `rendered/index.htm`
  around line 12521)
- `planning/code-facts.md`, Chapter 6, sections A-D
- `RobotModel/Robots/Robot.java`, `BillyRobot.java`, `Wildbots2025.java`,
  `StandardTankRobot.java`, `OneStickTankRobot.java`
- `RobotModel/Mechs/Assemblies/BillyMA.java`
- `RobotModel/Mechs/Components/SpinnyIntake.java`
- `OpModes/BillyTeleOpBLUE.java`, `ExampleTeleOp.java`,
  `ExampleStateMachineAuton.java`

Java paths start at `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.

## Where the lesson and the code disagreed

- **The flow of instructions.**  The lesson (and `AbstractRobotObjectModel.md`
  section 4) says Billy builds around `Robot.update` with `super.update(...)`.
  `BillyRobot` now overrides `update` and runs its own strategy, which makes
  the same two hand-offs (`driveTrain.drive(gamepad1)`,
  `mech.giveInstructions(gamepad2, assistClaims())`) after its button
  handling.  6.4 shows `Robot.update` as the version `Wildbots2025` and both
  tank robots use, and Billy's two hand-off lines, and points to Chapter 10
  for the rest.
- **"Each layer talks only to the layer below."**  True with one standing
  case:  `BillyRobot` hands `LimelightAutoTarget` the turret's autonomous
  object (`BillyRobot.java:127-130`).  6.2 presents it as a design choice with
  a cost and a benefit, plus an Argue it.
- **The assembly's strategy now skips claimed parts.**  Shown in 6.4 as it
  is, with one pointing sentence to Chapter 13 for claims.
- **Composition table.**  The lesson names two robots;  the code has four.
  `StandardTankRobot` has no mech assembly, which is what `Robot.update`'s
  `!= null` checks are for.

Left out as minor, per PROCESS's student-work guidance:  both tank robots
return `null` from `getAutonomousRobot()` (no autonomous program uses them);
the `strategy` field in `BillyRobot` beside `Robot`'s;  `Robot.visionPortal`
is never assigned.

## Guesses and open questions for Jason

1. The lesson calls the model "four layers" while drawing five names.  The
   chapter keeps "four layers" and explains that `DriveTrain` and
   `MechAssembly` share the third level.  Is that the count you use out loud?
2. 6.2's Argue it asks whether `BillyMA` should offer its own aiming action.
   Is that a question you want open in the book, or does Chapter 13's design
   already answer it?
3. The 6.4 Watch out tells readers the turret ignores the bumpers until
   `gamepad1.y` turns the targeter off.  That's the code's behavior with
   `TARGET_TAG_ID = -1`;  please confirm it matches what drivers see.
4. The Try it in 6.4 uses Android Studio's default Windows shortcuts
   (Ctrl+B, Ctrl+Alt+B).  Unverified on the team's machines.

## Unverified

- Android Studio shortcut keys (above).
- Nothing about the world outside the repository beyond the match timing,
  which comes from `doc/game-manual/`.

## Proposed source TODOs

None.  Nothing in this chapter's code stops a feature from working.

## Cross-references left at chapter level

Chapter 1 (configuration name, `abstract`, `extends`, lambda), Chapter 3
(hardware map, motor directions, IMU), Chapter 4 (sensor sorting, IMU
ownership), Chapter 5 (targeter, tag ID), Chapter 9, Chapter 10 (Billy's
`update`, single flush point), Chapter 11 (verbs, the dot chain), Chapter 12
(`doAndWait`, rapid fire), Chapter 13 (claims, Billy's buttons, `MacroManager`
for the 6.8 Try it), Chapter 18 (wheels), Chapter 21.  Chapter 8 is cited as
8.7, which is mine.

## Asides used that another chapter owns

None.  Terms owned elsewhere are used with a chapter pointer.
