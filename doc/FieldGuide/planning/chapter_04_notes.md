# Chapter 4 notes - Sensors, and Where They Live

Code baseline:  `305fb81`, rechecked 2026-10-27 against `dc4da50`.

**Progress:**  complete.  4.1-4.2 from the first session;  4.3-4.10, the
closing sections, and `book/opening_04.md` written 2026-10-27.

## 1.  Sources

- `doc/FlintLessons/GettingStarted/03-sensors-and-where-they-live.md`
- `AutonStrategies/BillyRapidFire.java`, `LimelightAutoTarget.java`;
  `OpModes/ExampleStateMachineAuton.java`
- `RobotModel/Robots/Robot.java` (the sorting rule, the Limelight and IMU
  bullets), `BillyRobot.java`
- `RobotModel/Mechs/Assemblies/MechAssembly.java` (the assembly-sensor `TODO`)
- `RobotModel/DriveTrain/Mecanum/MecanumDrive.java` (the IMU `TODO`s)
- `RobotModel/Mechs/Components/DoublyLimitedMotor.java`, `Claw.java`,
  `Turret.java`
- SDK sample `SensorREV2mDistance.java` (the checker matched the same line in
  `SensorAndyMarkTOF.java`;  both samples have it)

## 2.  Where the lesson and the code disagreed

- The lesson says the Limelight's `null` crash is a live bug in Lesson 4.  It
  was fixed in `1a6809a`.  4.5 shows the current guard as the plan working.
- Lesson block 1 (command against question) and block 3 (hysteresis) are
  illustrative.  4.1 kept the first in prose;  4.3 shows the second in a plain
  fence, introduced as made up.

## 3.  Guesses and open questions for Jason

- 4.4 says a timed-out 2m sensor reading should be treated as suspect.  That's
  inferred from the method's name in the sample.  The exact out-of-range
  number is left unstated, because neither the repo nor the lesson gives it.
- 4.5's list of good fallbacks (stop, keep the last good value, fall back to a
  slower method, wait) is the drafter's, and is general advice.
- 4.9 describes `MecanumDrive`'s IMU `TODO` in neutral prose, leaving out its
  "works by luck" wording, and quotes only "One owner, one initialize."  The
  full quote wrapped badly in the rendered page.
- Several source comments credit a student by name (`Robot.java`,
  `MecanumDrive.java`).  None of those lines are quoted, per PROCESS.
- 4.6's **Argue it** is the lesson's voltage-sensor question, left open.
- The lesson's "a project worth naming" (field-relative drive, heading hold)
  is two sentences at the end of 4.9, pointing at the `TODO`.

## 4.  Unverified

- 3.6 (another drafter) says a distance sensor pointed at nothing reads as a
  very large number;  4.4 relies on it.  The lesson says so too.

## 5.  Cross-references left at chapter level

- 4.3:  Chapter 5 (`tx`)
- 4.5:  Chapter 5 (`NullPointerException`), Chapter 12
- 4.6:  Chapter 6 (layers), Chapter 7, Chapter 8
- 4.7:  Chapter 9, Chapter 12
- 4.8:  Chapter 5, Chapter 13, Chapter 14
- 4.9:  Chapter 14

## 6.  Asides used that another chapter owns

None.  `tx` (Chapter 5) and `NullPointerException` (Chapter 5) each get one
sentence and a pointer.  Deadzone is Chapter 4's by the table;  2.5 also
explains it, and points here.
