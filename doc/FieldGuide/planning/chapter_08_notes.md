# Chapter 8 notes - Control Strategies

Continuation by Codex, 2026-10-26.  Code baseline:  `305fb81`, checked against
the working tree.  Student-work guidance in PROCESS applies.

## Sources

- `doc/FlintLessons/03-component-control-strategies.md`
- `RobotModel/Mechs/Components/SpinnyIntake.java`, `PusherServo.java`,
  `DoublyLimitedMotor.java`, and `MechComponent.java`
- `RobotModel/Mechs/Assemblies/BillyMA.java`

## Progress

8.1-8.8, closing sections, and opening saved.  Source checks and rendered
review completed.  Next:  Jason's editorial review.

## Source discrepancies and decisions

- Used current Billy mappings, not older lesson variants.  No bug catalogue.
- Described the bounded motor as checking at each `setPower` call.  Ongoing
  checks during autonomous travel are already covered by the existing TODO;
  no additional source-code change or proposed TODO here.
- Called it a decorator in the wrapper/delegation sense used by the lesson;
  no claim that it implements the SDK motor's full interface.
- Servo telemetry describes a command value, not confirmation of ball motion.

## Guesses and questions for Jason

No new personal history or hardware measurements inferred.  Control choices
are described as the current Billy configuration, not a prescribed mapping
for a future robot.

## Unverified claims

No physical test was run.  Switch orientation and actual servo travel need
checking on the installed mechanism; the chapter distinguishes those from
the code's requested values.

## Unresolved cross-references

Chapter 13 is still partial.  Its ownership discussion is referenced by
chapter rather than by an unwritten entry number.

## Foreign-owned asides

Only short pointers to telemetry (7/10), construction (7), and ownership
(13).  Full treatments remain in those chapters.

## Next checkpoint

Selected source checks passed.  Built Word/PDF and reviewed the 10-page
contact sheet plus larger pages 4 and 7 (constructor call and decorator).
No clipping seen.  Next:  Jason's editorial review and whole-book references.
