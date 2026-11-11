# Chapter 12 notes

Drafted 2026-10-26 by Claude.  Robot code baseline:  `305fb81`.
Status:  drafted, all nine entries, end matter, and opening.  Mechanical
voice checks run and clean.  Next:  review.

Every `java` block is expanded from the source file by line range (CRLF
stripped, nothing else changed), so each one is a verbatim substring of a
current file and passes `check_chapters.ps1`.  Hand-drawn call pictures use a
plain fence with no language and are introduced as drawings.

Size:  about 31 KB, over PROCESS's 12 to 25 KB.  Entries run about 250 to 650
words each, inside PROCESS's 300 to 900 range, and nine entries at that size
can't come in under 25 KB.  Code is about 5.5 KB of it.  If it needs cutting,
the candidates are the 12.4 **Try it** (could become a Check question) and
the summary table, which overlaps the vocabulary.

## Sources

- `doc/FlintLessons/09-autonomous-strategies-state-machines.md`
- `doc/FlintLessons/Advanced/state-machines.md`
- `planning/code-facts.md`, Chapter 12 and section B
- `AutonStrategies/StateMachine.java`, `Extensions/IState.java`,
  `AutonStrategies/IAutonStrategy.java`, `AutonStrategies/BillyRapidFire.java`,
  `AutonStrategies/LimelightAutoTarget.java`,
  `AutonStrategies/ExampleAutonomousStrategies.java`
- `OpModes/BillyTeleOpBLUE.java`, `OpModes/ExampleStateMachineAuton.java`
- `RobotModel/Mechs/Assemblies/BillyMA.java`, `MacroManager.java`,
  `RobotModel/Robots/BillyRobot.java` (who steps each machine)
- `git show e89bec0` (the `abort()` change) and `git show cf6e70f` /
  `34c22ea` (earlier `StateMachine`, `BillyRapidFire`, `BillyMA`;  identical
  in those files)
- SDK sample `FtcRobotController/.../samples/ConceptGamepadEdgeDetection.java`
  (once-per-press buttons)

Java paths start at `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.

## Where the lessons and the code disagreed, and what the chapter says

- **Who calls `updateState()`.**  Lesson 9 says `BillyMA`'s and
  `BillyRobot`'s strategy lambdas call it.  Today rapid fire is stepped by
  `MacroManager.update` -> `BillyRapidFire.update` -> `updateState()`, and
  targeting by `BillyRobot`'s strategy -> `assist.update` -> `updateState()`.
  12.3 draws that path.
- **Construct, abort, then reset.**  The advanced lesson's "build it and
  immediately abort it" pattern is gone.  `BillyRapidFire`'s constructor
  leaves `currentState` null, and `start()` calls `reset(shotsPerRun)`.  12.5
  says so in one sentence;  the old pattern isn't shown.
- **The restart guard.**  `if(gamepad.a && BRF.isComplete())` is gone.  A
  press is edge-triggered (`gamepad2.aWasPressed()` in `BillyRobot`) and
  `MacroManager.request` ignores a request while a macro is active.  12.8
  says this and points at 13.5.
- **The `abort()` stale timer.**  Changed in `e89bec0`.  Per the
  student-work guidance, 12.8 teaches the idea (a machine's state is every
  field its states read) from the current two-line `abort()`, by asking what
  would happen if it cleared only `currentState`, on a sequence a driver can
  cause today (cancel with `gamepad2.b` partway through `fire()`, restart
  with `gamepad2.a`).  It names the commit neutrally, with no old code block.
- **The shared `elapsedTime` field and composing several machines.**  Still
  true:  one private field per `StateMachine`, and Billy runs two separate
  instances.  12.7 keeps it, and adds that the owners now coordinate through
  claims (Chapter 13).
- **The turret two-owner section** of the advanced lesson moved to Chapter
  13, where the claims system leads.
- **`TrySleep` "used for timing turret rotations"** (extension-methods
  lesson) is no longer true;  12.9 uses `MecanumAutonDance` as the blocking
  example.

## Left out on purpose (minor, per the student-work guidance)

- `ExampleStateMachineAuton` builds a `BillyRobot`, whose constructor starts
  the targeter, but the auton loop never calls `robot.update`, so the
  targeter never steps there.  Harmless;  not mentioned.
- `ExampleStateMachineAuton` never flushes telemetry inside its loop, so its
  "Tags 21, 22, 23 not found" line isn't shown.  Not mentioned.
- `MecanumAutonDance` has no caller.  12.9 says so in one neutral clause,
  because a reader looking for where it runs needs to know.

Included, neutrally, because a reader at the robot will see it:  the whole
rapid-fire chain is built at once when `reset` runs (Java evaluates
arguments first), so the eight `reportStatus` names all appear together in
the tick rapid fire starts (12.5).  The sequencing itself is correct.

## Proposed TODO comments

None.  Nothing in this chapter's code stops a feature working.

## Guesses and open questions for Jason

1. **Size.**  31 KB against a 25 KB target (see above).  Cut, or keep?
2. **12.5's "all eight names at once" paragraph.**  It explains telemetry a
   driver will see, and teaches argument evaluation.  It could also read as
   pointing at a quirk.  Keep or drop?
3. **Asides on Java the ownership table doesn't list.**  12.4 explains
   `Runnable` and `ElapsedTime`;  12.5 explains method references (`::`),
   the conditional operator (`? :`), and pre-decrement (`--x`).  If another
   chapter also explains them, the cross-reference pass should pick one owner.
4. **The `turnToAngle` mention in 12.1.**  It points at the source `TODO` in
   one sentence, because that `TODO` names `doAndWait`.  Chapter 22 owns the
   blocking-loop shape.  Fine as a pointer, or drop?
5. No loop rate is stated anywhere, on purpose;  12.1 says only that the
   robot gets through "a lot of" ticks a second.

## Unverified

- No claims about the world outside the repository.
- Once-per-press buttons are described from the SDK sample
  `ConceptGamepadEdgeDetection.java` ("you only detect one button press,
  regardless of how long the button is held").  Not checked against the jar.

## Cross-references left at chapter level

Chapter 1 (lambda, `null`, `abstract`, `extends`), Chapter 2 (`TrySleep`),
Chapter 5 (tags, `tx`), Chapter 6 (OpMode, composition), Chapter 8
(functional interface), Chapter 11 (autonomous verbs), Chapter 13 (macro,
assist), Chapter 22 (blocking loops).  Into Chapter 13 the chapter cites
exact entries:  13.5 (`MacroManager`), 13.6 (the turret and the targeting
**Watch out**), 13.7 (the buttons).

## Asides used that another chapter owns

None.  Lambda, `null`, `abstract`, `extends`, functional interface, and
composition are used with pointers.
