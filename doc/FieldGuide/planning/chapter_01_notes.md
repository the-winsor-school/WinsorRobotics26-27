# Chapter 1 notes - Reading Java in This Codebase

**Progress:**  complete;  mechanical checks run and clean.  `check_chapters.ps1`
passes this chapter (10 entries, every `java` block verbatim).  **Next:  Chapter 2.**

Code baseline:  `305fb81`, plus the uncommitted `TODO` in
`DoublyLimitedMotor.java` (it moves that file's line numbers down by two;  the
quoted lines themselves are unchanged).  Line numbers below are at `305fb81`.

## 1.  Sources

- `doc/FlintLessons/GettingStarted/01-reading-java-in-this-codebase.md`
- `doc/FieldGuide/planning/code-facts.md`, Chapter 1 section and sections A-D
- `RobotModel/Mechs/Components/SpinnyIntake.java` (1.1, 1.5, 1.6, 1.7, 1.9)
- `RobotModel/Mechs/Components/MechComponent.java` (1.6, 1.7)
- `RobotModel/Mechs/Components/DoublyLimitedMotor.java` (1.4, 1.8)
- `RobotModel/Mechs/Components/Claw.java` (1.2, 1.3, 1.10)
- `RobotModel/Mechs/Assemblies/BillyMA.java` (1.2, 1.4, 1.5, 1.8, 1.9)
- `RobotModel/Mechs/Assemblies/CascadeArm.java`, `RobotModel/Robots/Wildbots2025.java` (1.1)
- `RobotModel/Robots/BillyRobot.java` (1.2, the `assist = null` field)
- `RobotModel/DriveTrain/Mecanum/MecanumDrive.java` (1.4 chain, 1.5 drive-motor names, 1.10 project TODO)
- `OpModes/BillyTeleOpBLUE.java` (1.2, 1.3, 1.6, 1.10)
- `AutonStrategies/IAutonStrategy.java`, `ExampleAutonomousStrategies.java`, `StateMachine.java`
- `Extensions/GamepadExtensions.java`, `LimelightExtensions.java`
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/readme.md:28` (quoted in 1.10)
- `git show 1a6809a~1:TeamCode/.../RobotModel/Mechs/Assemblies/BillyMA.java`, lines 73-80
  (the earlier intake lambda in 1.8's Predict box)

## 2.  Where the lesson and the code disagreed, and what was written

- **The intake two-`if` bug is fixed** (`1a6809a`).  The lesson calls it open,
  with a TODO above it.  Following the student-work guidance, 1.8 leads with
  today's code:  the turret chain, the intake chain, and the flywheel's two
  separate `if`s, which are separate on purpose.  The earlier intake lambda
  appears once, in a **Predict** box, introduced as "part of `BillyMA.java` as
  it was before commit `1a6809a`" and used to teach which `if` an `else`
  belongs to.  Its old TODO comment is left out, and the prose frames it as a
  reading exercise.  The title-page opening uses the current flywheel code.
- **"One of eighteen" TODOs.**  The Java code has 12 distinct TODO comments
  (16 grep lines in `.java`, 17 including `readme.md`).  1.10 says "twelve",
  dated to when the chapter was written, and tells the reader to search for
  today's list.  It also describes the three kinds that exist now (a value to
  fill in, a project, the next step for a feature), since "known open defect"
  describes only some.
- **The lesson's skeleton of `SpinnyIntake`** paraphrased inside a code block
  (`String motorName, ...)`).  1.1 shows three contiguous real excerpts.  The
  constructor in 1.5 includes the cast line the lesson silently dropped, with a
  pointer to Chapter 7.
- **"`DcMotor` is a type"**:  kept the lesson's careful wording.  `DcMotor`,
  `TouchSensor` and friends are SDK interfaces, so the chapter never calls them
  classes.
- **Claw practice:**  a **Try it** against today's `Claw.java` (servo field
  line 88, constructor 93-102, `open()` 18-22 inside `AutonomousClawBehaviors`,
  `update()` 125-132), with the answers written out.  Added a sixth question
  about reading the file's TODO.
- **`DoublyLimitedMotor.setPower` practice** became Check your understanding 8,
  and its condition is the worked example in the conditions Aside.
- Dropped the lesson's claim that a configuration-name typo is "the single
  most common first-competition bug";  kept "builds fine and fails only when
  the robot runs".

## 3.  Guesses and open questions for Jason

1. **Size.**  The chapter is about 40 KB against PROCESS's 12-25 KB, at the
   10-entry cap.  Chapter 1 owns about thirty terms, each needing a real
   example.  Places to cut if wanted:  the "An interface and an abstract class"
   and `implements` subsections (1.7), the summary table, and the Vocabulary
   (the glossary gathers it anyway).  Or split into two chapters, "Classes"
   (1.1-1.7) and "Inside a method" (1.8-1.10).
2. **Is the earlier intake lambda wanted at all?**  It's the clearest way to
   show that an `else` belongs to the nearest `if`, which today's code never
   shows.  If history should stay out entirely, the Predict box and the two
   paragraphs after it can go, and 1.8 still teaches chains against separate
   `if`s from current code.  Note:  `check_chapters.ps1` only checks unquoted
   `java` fences, so it skips this block, which sits inside the blockquote.  It
   is verbatim from `1a6809a~1`, lines 73-80.
3. Terms with no owner in PROCESS that Chapter 1 glosses in a sentence:  `this`
   (1.5), annotation (1.6), `implements` (1.7), package and package-private
   access (1.3), argument and body (1.4), `+=` (1.8), `float` (1.2).  `enum`
   has no owner either;  Chapter 2 glosses it for `TurnDirection`.  Worth adding
   to the ownership table under Chapter 1.
4. 1.3 uses the Claw TODO's step 1 (make `servo` private) as the reason
   `private` matters.  That's a design point the TODO already makes, framed as
   the plan.  Fine?
5. The tagline is the drafter's:  *What it knows, what it does, and which
   branch runs.*

## 4.  Unverified

- 1.10:  "Android Studio shows the doc comment when you rest the mouse on the
  method's name."  True of IntelliJ-based IDEs by default;  not checked against
  the team's Android Studio settings.

## 5.  Cross-references left at chapter level

- 1.1:  Chapter 7 (components)
- 1.2:  Chapter 3 (FTC hardware types), Chapter 5 (`NullPointerException`), Chapter 13 (assist)
- 1.3:  Chapter 7 (the Claw TODO plan)
- 1.4:  Chapter 3 (IMU, yaw), Chapter 11 (dot-chains)
- 1.5:  Chapter 3 (hardware map, Driver Station configuration), Chapter 6 (layers), Chapter 7 (cast)
- 1.6:  Chapter 6 (OpMode, `@TeleOp`, `MechComponent`), Chapter 7 (component contract, `getAutonomousBehaviors`)
- 1.7:  Chapter 7 (generics), Chapter 8 (control strategy), Chapter 12 (`IAutonStrategy`), Chapter 13 (assist)
- 1.8:  Chapter 8 (the `DoublyLimitedMotor` decorator), Chapter 13 (claims)
- 1.9:  Chapter 5 (stream search), Chapter 8 (control strategies, functional interface), Chapter 12
- 1.10:  Chapter 3 (`CRServo`), Chapter 7 (inner class `AutonomousClawBehaviors`, the Claw TODO plan)

## 6.  Asides owned by other chapters

None.  `NullPointerException` (Chapter 5), hardware map (Chapter 3), cast and
inner class (Chapter 7) are named with a one-line gloss and a pointer.

## 7.  Proposed source TODOs

None.  Every bug this chapter touches is already fixed or already has a TODO
in the source.
