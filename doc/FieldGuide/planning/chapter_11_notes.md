# Chapter 11 notes - Autonomous Verbs

Continuation by Codex, 2026-10-26.  Baseline `305fb81`, with excerpts checked
against the working tree.  No source-code edits planned.

## Sources

- `doc/FlintLessons/08-autonomous-behaviors-as-verbs.md`
- `RobotModel/Mechs/Components/SpinnyIntake.java`, `PusherServo.java`,
  `DoubleShooter.java`, and `MechComponent.java`
- `RobotModel/Mechs/Assemblies/BillyMA.java`
- `RobotModel/Robots/Robot.java` and `BillyRobot.java`
- `OpModes/ExampleStateMachineAuton.java`, `AutonStrategies/BillyRapidFire.java`

## Progress

11.1-11.8, closing sections, and opening saved.  Source checks and rendered
review completed.  Next:  Jason's editorial review.

## Source discrepancies and decisions

- Replaced lesson pseudocode with actual excerpts and the current dot-chain.
- Scoped gamepad independence to the component verbs.  Macro/assist update
  interfaces accept gamepads; BillyRapidFire's update ignores its argument.
- The API does not enforce hardware ownership.  Claims are a separate concern.
- Did not claim all hardware is private or impossible to reach:  the lesson's
  universal access claim is broader than the current source supports.
- `setPower(0.6)` remains a real, parameterized operation.  Discussed the
  trade-off instead of labelling the students' setter an error.
- Invented claw names are explicitly labelled as API-design examples.

## Guesses and questions for Jason

No personal anecdotes inferred.  Future API choices are exercises, not a
request to rename current student methods or change their implementations.

## Unverified claims

No physical mechanism or autonomous routine was run.  Command telemetry is
distinguished from measured physical completion throughout.

## Unresolved cross-references

Chapter 13 is partial; references use its chapter number.  Chapter 12 is
already drafted and owns the timing helper's implementation.

## Foreign-owned asides

Initialization is briefly recapped with pointers to 7/9/10.  Timing belongs
to 12 and ownership to 13; this chapter explains why callers need them.

## Next checkpoint

Selected source checks passed.  Built Word/PDF, reviewed the 10-page contact
sheet and larger page 5 (dot-chain and table).  Replaced a split closing list
with a short table, rebuilt and rendered again, then inspected final page 10.
No clipping seen.  Next:  editorial review and whole-book references.
