# Chapter 21 notes - Designing a Robot, End to End

**Progress:**  Chapter 21, its opening and these notes are drafted.  Next:
Chapter 22.

Code baseline:  `305fb81`, as BACKLOG records.  Every `java` block was copied
from its file with `sed`, CRLF stripped, so `check_chapters.ps1` can verify it.
Template code and the invented wrist sketch sit in plain fences, so the checker
leaves them alone, and the prose introduces each as a template or as made up.

Size:  about 31 KB, over the 12 to 25 KB guide.  The component template alone
is 1.4 KB, and 21.9 (the worked design pass) is about 4.5 KB.  The obvious cuts,
if Jason wants it shorter:  the configuration-name table in 21.2, the Try it in
21.2, or the template-method code pair in 21.6.

## 1.  Sources

- `doc/FlintLessons/10-capstone-new-robot.md`
- `doc/NewRobotDesignWorkflow.md` (method, questions, build order, test order,
  the "which layer is failing" table, templates, patterns, checklist)
- `doc/AbstractRobotObjectModel.md` (template-method passage, checked and
  found stale for Billy;  not quoted)
- `planning/code-facts.md`, sections 10, 13, 21, B, C
- Code:  `OpModes/BillyTeleOpBLUE.java`, `RobotModel/Robots/BillyRobot.java`,
  `Robot.java`, `Wildbots2025.java`, `StandardTankRobot.java`,
  `OneStickTankRobot.java`, `RobotModel/Mechs/Assemblies/MechAssembly.java`,
  `BillyMA.java`, `CascadeArm.java`, `MacroManager.java`,
  `RobotModel/Mechs/Components/MechComponent.java`, `SpinnyIntake.java`,
  `PusherServo.java`, `Claw.java`, `RobotModel/DriveTrain/Mecanum/MecanumDrive.java`,
  `AutonStrategies/ExampleAutonomousStrategies.java`

## 2.  Where the lesson and the code disagreed

| Lesson or doc says | Code says | What the chapter does |
|---|---|---|
| Lesson 10's assembly skeleton (lines 64-77) | It has no constructor, no `getAutonomousBehaviors()` and no `this.telemetry = telemetry;`, so it would fail to compile against `MechAssembly` | Quotes only the Workflow doc's templates, which are complete.  The lesson's shorter skeletons go unmentioned. |
| Templates (Workflow 337-546) | Consistent with today's `MechComponent`, `MechAssembly`, `Robot`.  None shows `IMacroAssemblyStrategy<R>`, `IAssistRobotStrategy<R>` or `MacroManager<R>` | 21.4 says the templates match, and that they predate the macro and assist pieces, and points to Chapter 13. |
| Patterns list:  `Robot` as a template method;  Billy "builds around" `super.update` (Workflow 228-235, AbstractRobotObjectModel 305-323) | `BillyRobot` overrides `update()` and runs its strategy;  `StandardTankRobot`, `OneStickTankRobot` and `Wildbots2025` keep `Robot.update` | 21.6 says exactly that, neutrally:  a robot that referees behaviors writes its own `update()`. |
| Lesson 10:  Strategy at assembly and robot layers is "informal" | Now typed:  `IMacroAssemblyStrategy<R>`, `IAssistRobotStrategy<R>` | 21.4, 21.6 present them as declared strategies. |
| Checklist:  "coordinate conflicts in one place, rather than scattering guard checks" | Real machinery now:  `BillyMA.Part`, `claims()`, the claims-aware default strategy, `MacroManager` | 21.7 quotes `BillyMA.java:117-128` and points to Chapter 13. |
| Workflow 168-172:  `BillyMA` blocks manual control while rapid fire runs | Blocks only claimed parts (`PUSHER`, `FLYWHEEL`) | 21.7 says rapid fire claims the pusher and the flywheel. |
| Workflow 309:  `LimelightAutoTarget` in `BillyRobot.update()` | Runs as the active assist, stepped inside Billy's strategy | 21.6 describes the strategy handling assists. |
| Lesson capstone Q5:  "does it change `update()` beyond the default template?" | Fair question;  Billy replaces it, the others keep it | 21.8 and 21.9 ask it in those terms. |

Code-facts' writer note holds:  all robot-layer arbitration lives in
`BillyRobot`, and the base `Robot` has none.  21.9 says a new robot copies the
pattern;  it never claims a reusable robot-level manager exists.

## 3.  Guesses and open questions for Jason

1. **The worked example is a wrist for `Wildbots2025`'s `CascadeArm`.**  It's
   invented:  `"wristServo"`, `WristServo`, `SCORE`, `STOW`, the frame
   collision, and the game task.  The lesson suggested "a wrist-and-claw end
   effector".  The game task is deliberately generic, and `doc/game-manual/`
   was not consulted.  Is a wrist on Wildbots the right example, or would he
   rather it sat on this season's robot?
2. **The reuse question in 21.9 is real and open.**  `PusherServo` is a
   single-`Servo` wrapper named for a job, and the `TODO` on `MechComponent`
   leaves the naming convention open.  21.9 makes it the Argue it.  If he has
   since decided the convention, 21.9 should say so.
3. **21.9 has the wrist's assembly read its own macro button in
   `giveInstructions`.**  Billy moved its macro trigger up to `BillyRobot`.
   Both are defensible;  for a robot with no assists the assembly seemed the
   simpler home.  Worth his eye.
4. **Billy's "tasks" in 21.2** ("pick up balls and shoot them at an AprilTag
   target") are inferred from the code, not from a game manual.
5. Mentioned in 21.9's Watch out, as operating facts only:  the
   `DoublyLimitedMotor` limit check runs on each `setPower` call (code-facts
   E.1, and the uncommitted TODO Codex proposed), and `Wildbots2025`'s IMU has
   no setup (the existing `MecanumDrive` TODO).  No blame, no history.

**Proposed source TODOs:**  none.  Everything feature-affecting this chapter
touches already has a `TODO` in the source (the IMU, the tag IDs).

## 4.  Unverified

- 21.2's Try it says a mismatched configuration name "will stop the OpMode at
  `hardwareMap.get` during initialization."  That is standard FTC SDK
  behavior (the lookup throws when no device has the name), stated from
  general knowledge, not checked against the 12.0.0 samples.

## 5.  Cross-references left at chapter level

Chapter 1 (configuration name, `@Override`), 3 (Driver Station configuration,
`Servo`), 4 (sensor ownership), 6 (layers, OpMode), 7 (two-phase construction,
reuse first, generics), 8 (Strategy), 9 (Composite, assemblies), 10 (template
method, Facade, robot construction order), 11 (autonomous verbs), 12 (state
machines, `doAndWait`), 13 (macro, assist, claims, `MacroManager`), 19 (the
limit check in `DoublyLimitedMotor`), 20 (decomposing a design).

Within Part 7, exact entries:  22.3 (shared constants), 22.4 (the IMU).

## 6.  Asides used that another chapter owns

None.  Macro, assist and claim are Chapter 13's;  21.4 and 21.7 use them in a
sentence with a pointer and leave them out of the Vocabulary.  Template method
is Chapter 10's;  21.6 points there.
