# Chapter 9 notes - Assemblies

**Progress:**  chapter, opening, and notes complete.  Next:  mechanical
checks over all three chapters, after Chapters 10 and 11.

Code baseline:  `305fb81`, plus the uncommitted `DoublyLimitedMotor.java` TODO
(not quoted in this chapter).  Every block was copied from the files and
dedented;  nothing inside a block is changed.  One block comes from git and
says so:  `BillyMA.updateTelemetry` as it stood before `1a6809a` (9.7).

Size:  about 27.8 KB, over the 12-25 KB guide.  Software chapters carry long
code blocks;  the prose was cut once already.  Further cuts, if wanted:  the
earlier-placeholder block in 9.7, or the `AutonomousBillyMA` constructor body
in 9.5.

## 1.  Sources

- `doc/FlintLessons/04-modeling-a-mechassembly.md`
- `doc/FieldGuide/planning/code-facts.md`, Chapter 9 and sections A-E
- `doc/TelemetryContract.md`, `doc/AbstractRobotObjectModel.md`
- `TeamCode/.../RobotModel/Mechs/Assemblies/BillyMA.java`, `MechAssembly.java`,
  `CascadeArm.java`, `ExampleIntakeAssembly.java`
- `TeamCode/.../RobotModel/Mechs/Components/*.java`
- `TeamCode/.../RobotModel/Robots/Robot.java`, `BillyRobot.java`,
  `StandardTankRobot.java`
- `git show 1a6809a^:TeamCode/.../BillyMA.java`, and `git show cf6e70f:...`
  to confirm the placeholder was there from the repository's first commit

## 2.  Where the lesson and the code disagreed

- **The code review exercise.**  The lesson shows an empty
  `BillyMA.updateTelemetry()` as "the real, current code".  It was filled in
  by `1a6809a`.  The chapter leads with today's method and the checklist idea
  (9.6), and shows the earlier placeholder once, neutrally, in 9.7, as the
  clearest picture of an empty checklist.  It's described as what it was:  a
  placeholder left open as the Lesson 4 exercise (`doc/TelemetryContract.md`
  says "left open on purpose").  No student is blamed for it.
- **The lesson's `initializeTelemetry` block** leaves off the last line,
  `BRF = new BillyRapidFire(auton, 3);`.  The chapter quotes all of it and
  explains why the rapid-fire object is built there (9.4).
- **The lesson's `MechAssembly` block** omits `IMacroAssemblyStrategy<R>` and
  the `telemetry`/`strategy` fields.  The chapter's block shows the
  `telemetry` field and points to Chapter 13 for the macro interface in one
  sentence.
- **"Two places" to edit for a new component.**  For `BillyMA` it's now the
  constructor, the claims-aware strategy, `initializeTelemetry`,
  `AutonomousBillyMA`, `updateTelemetry`, and possibly a `Part` constant
  (9.8).
- **`Part` and claims** get one pointing sentence in 9.3 and a table row in
  9.8;  Chapter 13 owns them.

## 3.  Guesses and open questions for Jason

1. **Size.**  27.8 KB against a 25 KB ceiling.  Acceptable for a code-heavy
   chapter, or cut further (suggestions above)?
2. **The earlier placeholder in 9.7.**  Kept, short and neutral, because it is
   the clearest picture of an empty checklist.  It came from a TODO written
   as a teaching exercise, so it carries no student's mistake.  Drop it if you
   would rather the book show only today's code.
3. **"Composite."**  The lesson calls an assembly a composite.  The textbook
   pattern needs leaf and group to share one type;  here `MechComponent` and
   `MechAssembly` share a shape under different names.  9.1 says so in two
   sentences.  Keep that, or simply say "composite" and move on?
4. **Shared dpad (9.3).**  Dpad up/down drive the intake and trim the
   flywheel in the same loop.  Mentioned once as an example of reading the
   controls side by side, with no judgment.  It may be intentional.
5. **9.9's Argue it** (wait on a seated-ball sensor alone, or sensor-or-timer)
   is the drafter's design question, built on the `MechAssembly` TODO's
   proposal.
6. **Loop rate.**  The opening first had "fifty times a second";  removed,
   since nothing in the repository states a loop rate.

No source TODO is proposed for this chapter:  nothing in Chapter 9's scope
stops a feature from working.

## 4.  Unverified

- The compiler behaviors in 9.8 are standard Java and were not tested by a
  build here:  an unassigned `private final` field fails to compile;  a
  missing constructor argument fails to compile;  a missing method call
  compiles.
- What the driver station shows in 9.6's Try it is read from the code
  (`BillyMA`, the components' telemetry lambdas, `MecanumDrive.updateTelemetry`,
  `BillyRobot.updateTelemetry`), and agrees with code-facts B.11.  Not watched
  on the robot.  Billy's screen also carries the `Assist`, `Macro`, `Last
  macro`, `Last assist` lines and the targeter's status lines above these.

## 5.  Cross-references left at chapter level

Chapter 1 (`abstract`, `null`, `final`), Chapter 3 (`CRServo`), Chapter 4
(sensor ownership rule), Chapter 5 (`NullPointerException`), Chapter 6
(layers, capabilities flow up), Chapter 7 (component contract, two-phase
construction, telemetry buffer, inner classes, generics, reuse), Chapter 8
(control strategy, which layer a rule belongs to), Chapter 10
(`initializeSubsystems`, the flush, `BillyRobot`'s aggregate), Chapter 11
(autonomous verbs, the dot-chain), Chapter 12 (`BillyRapidFire` as a state
machine, the 2200 ms wait), Chapter 13 (`Part`, claims,
`IMacroAssemblyStrategy`).

## 6.  Asides used that another chapter owns

None.  The chapter uses no Aside boxes;  owned terms are used with pointers.

## Stale documentation, logged and left alone

- `doc/TelemetryContract.md:157-159` still says `BillyMA.updateTelemetry()`
  "currently stands empty" and is marked with a `TODO`.  Both are out of date
  since `1a6809a`.
- `doc/TelemetryContract.md:153-155` says adding a component means editing
  two methods;  for `BillyMA` there are now more (9.8).
- `doc/FlintLessons/04-modeling-a-mechassembly.md` (the code review
  exercise) describes the placeholder as current.
