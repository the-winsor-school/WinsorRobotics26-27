# Chapter 3 notes - The FTC Hardware Toolbox

Code baseline:  `305fb81` (master at the audit).  The working copy's uncommitted
`TODO` in `DoublyLimitedMotor.java` shifts that file's line numbers by two;  no
quoted line changed.  SDK samples quoted from the same checkout (SDK 12.0.0).

**Progress:**  chapter, opening, and notes written;  mechanical checks run
(see the end of this file).

## 1. Sources

- `doc/FlintLessons/GettingStarted/02-ftc-hardware-toolbox.md`, from the top
  up to *Reuse first*.  *Reuse first* and *The two kinds of component* belong
  to Chapter 7.
- `doc/FieldGuide/planning/code-facts.md`, Chapter 3 section, A-E.
- `doc/FieldGuide/planning/recovery_2026-10-26.md` for the Core Hex count.
- Team code:  `RobotModel/Mechs/Components/SpinnyIntake.java`,
  `PusherServo.java`, `Turret.java`, `DoublyLimitedMotor.java`,
  `RobotModel/Mechs/Assemblies/BillyMA.java`, `CascadeArm.java`,
  `RobotModel/DriveTrain/Mecanum/MecanumDrive.java`,
  `RobotModel/Robots/BillyRobot.java`, `Wildbots2025.java`, `Robot.java`,
  `OpModes/BillyTeleOpBLUE.java`.
- SDK samples (`FtcRobotController/.../external/samples/`):
  `RobotAutoDriveByEncoder_Linear.java`, `ConceptMotorBulkRead.java`,
  `SensorTouch.java`, `SensorDigitalTouch.java`, `SensorREV2mDistance.java`,
  `SensorColor.java`, `SensorIMUOrthogonal.java`,
  `ConceptVisionColorLocator_Circle.java`, `SensorGoBildaPinpoint.java`,
  `BasicOpMode_Iterative.java`, plus the shelf samples named in 3.9.
- REV:  [Core Hex Motor](https://docs.revrobotics.com/duo-build/motion/motors/core-hex-motor),
  288 counts per output revolution, as recorded by Codex on 2026-10-26.
  Not re-read by this drafter.

## 2. Where the lesson and the code disagreed

- **`BallDetectionComponent`** has never existed in this repository.  Every
  mention dropped.  3.8 describes the webcam route from the SDK sample
  `ConceptVisionColorLocator_Circle.java` (a `ColorBlobLocatorProcessor` with
  `ColorRange.ARTIFACT_PURPLE`), and the mapping table in 3.10 has no webcam
  row.  `MechComponent.java:40-42` still names it in a `TODO`;  that's
  Chapter 7's to handle.
- **The lesson's code blocks are illustrative.**  Every block in the chapter
  is now a real line from team code or an SDK sample.  `intake =
  hardwareMap.get(DcMotor.class, "intakeMotor");` in the lesson merges two
  real lines;  3.1 shows both.
- **`StandardTankDrive` with "four `DcMotor` + `IMU`".**  Both tank drives have
  two motors and no IMU.  3.10's table gives `StandardTankDrive` and
  `OneStickTank` their own row.
- **`VoltageSensor` "documented only in its javadoc."**  `ConceptTelemetry.java`
  reads one (`for (VoltageSensor sensor : hardwareMap.voltageSensor)`).  3.9
  names that sample.
- **`PusherServo` direction.**  Now set in the constructor
  (`setDirection(Servo.Direction.REVERSE)`), which 3.4 quotes as the servo
  version of motor direction.
- **The IMU ownership `TODO`** (lesson lines 143-146).  Still in the code.
  Following the student-work guidance, Chapter 3 mentions only that two
  parts of the robot want the IMU and points to 4.9, where it's framed around
  the idea (one owner sets a device up).
- **Left out as minor:**  `turret position` telemetry printing
  `servo.getPower()`, and the shared D-pad on `gamepad2` that trims the
  flywheel and runs the intake at once.  Neither stops a feature working.

## 3. Guesses and open questions for Jason

- 3.3's **Argue it** (flywheel on `setVelocity`) assumes no encoder is wired
  to a flywheel motor today.  If one is, the cost side shrinks;  the box
  still reads correctly.
- 3.2 says "Put the robot up on a box with its wheels off the ground" for the
  direction test.  That's a safety instruction added by the drafter;  keep or
  replace with the team's own practice.
- 3.9 adds a sentence about using lights to check a sensor at the workbench.
  Drafter's elaboration of the lesson's "genuinely useful for debugging."
- No new `TODO` proposed for Chapter 3.

## 4. Unverified

- 3.1:  a wrong type or a missing name stops the OpMode at init, and the
  missing-name error names the string.  From the lesson;  the exact behavior
  and text were not checked against SDK 12.0.0.
- 3.3:  `RUN_WITHOUT_ENCODER` is the default run mode, and in it "power goes
  straight to the motor."  From the lesson;  no sample states the default.
- 3.3:  `setVelocity(...)` exists on `DcMotorEx`.  No sample in this checkout
  calls it;  the lesson names it.  `getVelocity()` is quoted from a sample.
- 3.3:  "plain power is a share of whatever the battery has," so a tired
  battery spins a flywheel slower.  General knowledge, implied by the lesson's
  `VoltageSensor` row.
- 3.2:  "the encoder only counts when its cable is plugged into the hub."
  From the lesson's "if wired."
- 3.6:  hue is "roughly the same purple in a bright gym and a dim one."  The
  lesson's "much steadier" made concrete;  check the wording.

## 5. Cross-references left at chapter level

- Chapter 1 (configuration name, lambda, method calls)
- Chapter 4 (4.2, 4.4, 4.6, 4.7, 4.8, 4.9 are exact;  same drafter)
- Chapter 5 (5.2, 5.3, 5.10 are exact;  same drafter)
- Chapter 6 (the robot model, OpMode), Chapter 7 (two kinds of component,
  reuse first), Chapter 10 (robot-wide sensors), Chapter 11 (verbs named for
  intentions), Chapter 12 (waiting loops), Chapter 14 (odometry, heading),
  Chapter 17 (motors), Chapter 18 (drive trains), Chapter 19 (Control Hub,
  limit switches, wiring)

## 6. Asides used that another chapter owns

None.  `DcMotor.class` (the class literal) is in no chapter's row of the
ownership table;  Chapter 3 explains it in an Aside in 3.1.  `MechComponent`,
`MechAssembly`, `Robot` and OpMode (Chapter 6), lambda (Chapter 1), limit
switch and Control Hub (Chapter 19) are used with a pointer.  The color
sensor's "gain" in 3.6 is a multiplier on readings, a different sense from
Chapter 5's control gain;  3.6 defines it in passing and leaves it out of the
vocabulary list.

## Code-block note

The shared checker (`reference/check_chapters.ps1`) compares each ```` ```java ````
block against `TeamCode` only, so the SDK sample blocks here will report
"differs from every current source."  They were checked by hand against the
samples folder (see the end of this file).  Suggest adding the samples folder
to `$javaSources`.
