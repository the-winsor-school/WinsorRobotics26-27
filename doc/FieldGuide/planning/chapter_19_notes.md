# Chapter 19 notes

Reviewed 2026-10-26 by Codex.  Inherited draft from `8f9a46d`.
Status:  source/prose pass and 15-page chapter preview review complete.
Full-book layout and title-page art verification remain pending.
Robot code baseline:  `305fb81`, unchanged at `8f9a46d`.

## Sources

- `doc/FlintLessons/Mechanical/linear-motion-and-control-electronics.md`
- `doc/rev-duo/linear-motion.md`, `control-and-sensors.md`
- `planning/code-facts.md`, mechanical audit, Chapter 19
- `RobotModel/Mechs/Components/DoublyLimitedMotor.java`
- `RobotModel/Mechs/Assemblies/CascadeArm.java`
- `Extensions/GamepadExtensions.java`
- SDK sample `RobotAutoDriveByGyro_Linear.java` for target-before-run-mode order
- [Control Hub specifications](https://docs.revrobotics.com/duo-control/control-system-overview/control-hub-basics),
  checked 2026-10-26 for JST PH pin counts.

Java paths above start at `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.

## Differences and decisions

Used CascadeArm's existing construction:  six arguments, gamepad first in
its strategy, top/forward limit before bottom/reverse.  Source indentation
was restored and both Java blocks verified verbatim.  Zeroing is a possible
addition to the component's autonomous behaviors, rather than a defect to
call out in the students' current work.

For describing how to use this component:  `setPower` checks limits when invoked.
`goForward()` and `goBackward()` issue a single power request, and `update()`
reports telemetry without enforcing limits again.  A switch reached later
requires a new check and stop request.  The chapter now explains that timing
and gives the operating instruction to keep updating the check.  A neutral
source TODO now records the next step for stopping autonomous movement when
a limit is reached.  Future chapters should explain the useful design and
necessary calling pattern, following PROCESS's student-work guidance.

Changed the lift comparison's dimensionally incorrect "torque = load weight"
row to spool pull.  Qualified the ideal speed/pull ratios:  equal spool radius,
same payload, negligible moving-stage mass/friction.  Real design needs those
loads too.  Retained the reference's rigging comparison as an ideal model.

Corrected "holding always draws full stall current".  Current depends on
torque, applied power and control.  Corrected the spring example:  3 lb of
spring pull balances 3 lb under direct routing, leaving 1.4 lb of a 2kg load.
Removed the promise that spring assistance guarantees a hold without stall.

Corrected the connector table:  sensor/encoder JST PH connectors have four
pins, RS485 three.  Removed drift-free/exact-angle and collision-proof sensor
claims.  The lift and drawbridge share `Can Go` telemetry captions;  the
exercise tells readers to identify the lift readings.  No separate defect
discussion is needed for that minor detail.

## Guesses and questions for Jason

Please verify positive motor power physically raises Wildbots2025's lift.
The constructor names express that intention;  wiring and mounting still
need a test.  Prefer checking switch readings with the mechanism supported
before the supervised, low-power movement exercise.

The source TODO covers continued limit checks during autonomous movement.
This follow-up adds a comment only.  Zeroing can be considered when the team
needs that capability.

## Unverified

The precise lift travel, stage masses, and sensor ranges have not been bench
measured.  The 10mm magnetic detection figure remains local-reference
guidance, with a requirement to check actual mounting and detection range.
The printed ideal rigging model needs a builder's review against the kit.

## Cross-references

The numerical 20.6 and 17.x pointers resolve.  Chapters 2-4, 7-8 and 11 await
drafting and the book-wide cross-reference pass.

## Asides owned elsewhere

I2C belongs here and has an Aside.  Components, decorators, run modes,
encoders and lambdas point to their owning software chapters.

## Next step

Rendered code and tables were inspected, with a detailed view of the guard
method and explanation.  Removed repeated prose to bring an orphaned See also
line back onto its entry's page.  Carry the limit-check timing finding
into the software chapter drafts and any future source-reference corrections.
