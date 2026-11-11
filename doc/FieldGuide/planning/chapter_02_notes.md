# Chapter 2 notes - Extension Methods

**Progress:**  complete.  2.1-2.9, closing sections, and `book/opening_02.md`
written 2026-10-27.  Checked and rendered;  see BACKLOG.

Code baseline:  `305fb81`.  None of the `Extensions/` files changed after it.
Rechecked 2026-10-27 against `dc4da50`:  the only TeamCode change since the
baseline is the `TODO` in `DoublyLimitedMotor.java`.

## 1.  Sources

- `doc/FlintLessons/Advanced/extension-methods.md`
- `Extensions/GamepadExtensions.java`, `ThreadExtensions.java`,
  `AngleExtensions.java`, `LimelightExtensions.java`
- Callers:  `MecanumDrive.java`, `CascadeArm.java`,
  `AutonStrategies/LimelightAutoTarget.java` (`lookForTag`),
  `OpModes/ExampleStateMachineAuton.java` (`findTags`)
- `planning/code-facts.md`, Chapter 2 and Chapter 5 sections

## 2.  Where the lesson and the code disagreed

- The lesson calls its three files "the actual, current inventory of
  `Extensions/`."  The folder also has `LimelightExtensions`, `IState`, and
  `TurnDirection`.  2.7 covers `LimelightExtensions`.  `IState` belongs to
  Chapter 12;  `TurnDirection` is left out.
- The lesson implies `TrySleep` times turret rotations.  Those sleeps were
  removed in `1a6809a`;  2.4 names the current callers.

## 3.  Guesses and open questions for Jason

- 2.7:  `tryGetFiducial` and `tryFindOneOf` check for a `null` result and
  leave `isValid()` unchecked.  code-facts flags it, and also that
  `doc/limelight3a/api.md` (written for 11.2.1) says `getLatestResult()`
  synthesizes an empty invalid result instead of returning `null`.  Neither
  is confirmed as a feature-breaking bug in 12.0.0, so the chapter leaves both
  out and proposes no `TODO`.  Worth a check against the 12.0.0 jar.
- 2.7:  the claim that `tryFindOneOf` returns the first match *in the
  camera's list*, whatever order the call names the IDs in, is read from the
  source (`filter` over `getFiducialResults()`, then `findFirst`).  The order
  the Limelight reports tags in is unchecked, so the chapter makes no claim
  about it.
- 2.7's **Argue it** (does cluster search belong in this file?) is a real
  open design question.  The chapter leaves the answer open.
- 2.8's `GetBillyIntakeCommand` and 2.9's three proposals are the lesson's
  invented examples, introduced as invented, in plain fences so the source
  checker skips them.
- Left out:  `BillyRobot.AutonomousMecanumRobot.angleWrap`, which code-facts
  suggests as a "should have been an extension" example.  It would teach by
  pointing at an unused method in student code;  your call.
- The chapter is 33 KB, over the 12-25 KB guide.  The likeliest trims are
  2.7's overlap with Chapter 15, and 2.6's three full method listings.
- Opening:  the joke is the private constructor in 2.2, which exists so it
  can never run, with the class's own comment about it underneath.
- Page 7 of the standalone render holds only 2.4's `See also` line.

## 4.  Unverified

Nothing new in 2.7.  It makes no claims about the world outside the repository.

## 5.  Cross-references left at chapter level

- 2.1:  Chapter 4 (deadzone)
- 2.3:  Chapter 4 (deadzone), Chapter 6 (robot model layers)
- 2.4:  Chapter 12 (blocking, `Thread.sleep`)
- 2.5:  Chapter 4 (deadzone), Chapter 8 (control strategy)
- 2.6:  Chapter 3 (yaw, heading), Chapter 14 (headings and pose)
- 2.7:  Chapter 5 (fiducial, the `stream` chain), Chapter 15 (clusters, the
  cluster plan;  15.5 is the matching entry)

## 6.  Asides used that another chapter owns

- 2.7 gives **fiducial** (Chapter 5's) one sentence and a pointer.
- 2.7 has an Aside on `int...` (varargs).  The ownership table in PROCESS
  gives it to no chapter, so Chapter 2 claims it.  Added to the table.
