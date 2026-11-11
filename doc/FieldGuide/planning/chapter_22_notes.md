# Chapter 22 notes - Common Pitfalls

**Progress:**  complete.  22.1-22.3 from the first session;  22.4-22.6, the
closing sections, and `book/opening_22.md` written 2026-10-27.

Code baseline:  `305fb81`, rechecked 2026-10-27 against `dc4da50`.  Real code
in `java` fences;  invented code in plain fences, introduced in prose as made
up.

## 1.  Sources

- `doc/FlintLessons/Advanced/common-pitfalls.md`
- `planning/code-facts.md`, Chapter 22 section (bug status table)
- `RobotModel/DriveTrain/Mecanum/MecanumDrive.java` (`turnToAngle`, the IMU)
- `RobotModel/Mechs/Assemblies/BillyMA.java`, current and at `1a6809a^`
- `Extensions/LimelightExtensions.java`;  SDK sample `SensorLimelight3A.java`

## 2.  Where the lesson and the code disagreed

Six of the lesson's nine repo examples were fixed in `1a6809a`.  Live:
`turnToAngle` (22.2), the `MecanumDrive` TeleOp signs (22.3), the IMU (22.4).

- **22.5** shows the old two-`if` intake from `git show 1a6809a^`, labelled
  with the commit, and points to 2.8 for the current chain.  The old
  version's critical `TODO` comment is left out of the excerpt.
- **22.6** shows the current `null` guard, and describes the unguarded
  version in one sentence.
- The lesson's `PusherServo` and `rotateCW` practice exercises are gone:  both
  bugs are fixed.  22.3 already tells the pusher story as history.
- The lesson's made-up `arm.setPosition` snippet is kept in 22.5 as a
  self-test.

## 3.  Guesses and open questions for Jason

- Added a third historical entry to `reference/check_chapters.ps1`:  chapter
  22, `1a6809a^`, `BillyMA.java`.  It's the same revision Chapter 9 uses.
- 22.4 says `hardwareMap.get` returns the same object for the same name.
  That's how the SDK behaves, and the `MecanumDrive` `TODO` relies on it, but
  no repo file states it outright.
- The opening uses `turnToAngle`'s `while` loop, which is live code.  If that
  `TODO` gets fixed, the opening needs a new moment.

## 4.  Unverified

Nothing new.

## 5.  Cross-references left at chapter level

- 22.4:  Chapter 13 (the turret problem, 13.6)
- 22.5:  Chapter 8
- Where to go next:  Chapters 12, 13, 21

## 6.  Asides used that another chapter owns

None.  **tick** is Chapter 12's;  22.2 defines it in one sentence, and the
vocabulary entry says Chapter 12 owns it.
