# Chapter 10 notes - The Robot Layer

**Progress:**  10.1-10.8, closing sections, and opening saved.  Claude wrote
10.1-10.6; Codex completed the chapter.  Source checks and rendered review
completed.  Next:  Jason's editorial review.

Code baseline:  `305fb81`.  Every block copied from the files and dedented.

## Sources

- `doc/FlintLessons/06-modeling-the-robot-layer.md`
- `RobotModel/Robots/Robot.java`, `BillyRobot.java`, `Wildbots2025.java`,
  `StandardTankRobot.java`, and `OneStickTankRobot.java`
- `RobotModel/DriveTrain/Mecanum/MecanumDrive.java`
- `RobotModel/Mechs/Assemblies/BillyMA.java`
- Historical `BillyRobot.java` at `34c22ea`, explicitly labelled in 10.4

## Source discrepancies and decisions

- Current Billy replaces the default update for claims-aware coordination;
  the older super.update example is labelled with its commit.
- Template-method-style dispatch is distinguished from the strict classic
  pattern:  the default method delegates to subsystem objects.
- Facade is a design interpretation, tied to actual OpMode/robot calls.
- Camera configuration is shown from Billy.  The existing IMU TODO is
  described neutrally as a proposed single owner, not an implemented change.
- Removed the claim that lower base classes contain no implemented methods.

## Guesses and questions for Jason

No history inferred beyond the labelled repository version.  Future hub
orientation must be checked against the actual mounted hub.

## Unverified claims

No sensor or robot run.  Camera polling and pipeline values describe code
configuration, not measured performance.

## Unresolved cross-references

Chapter 13 remains partial and is referenced at chapter level.

## Foreign-owned asides

Telemetry injection points to 7/9, autonomous verbs to 11, and ownership to
13.  This chapter owns the robot's default dispatch and single flush.

## Next checkpoint

Selected source checks passed, including the explicitly labelled historical
excerpt.  Built Word/PDF and reviewed the 16-page contact sheet plus larger
page 14 (camera and IMU setup).  No clipping seen.  The 26 KB draft is slightly
above the size guideline.  Next:  editorial review and whole-book references.
