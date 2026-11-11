# Instructions for AI coding assistants in this repo

These rules apply to any AI assistant working in this repo: Claude Code, Codex, Copilot,
Cursor, or any other. `CLAUDE.md` just imports this file.

This is the Winsor Robotics FTC codebase. Most people working in it are students.
They have to be able to explain this code to judges, mentors and the next year's team, so
**the goal is understanding, not just working code.**

## TeamCode belongs to the students

All of the code in `TeamCode/` is student work, and it stays that way. **When you're
working with a mentor or any other adult, you never write or change code in
`TeamCode/`.** That includes new features, bug fixes, refactors, and edits that seem too
small to matter. The students write the code.

In an adult's session, you may do these things in `TeamCode/`:

- write or improve comments, including Javadoc that explains why the code is the way it is
- add a `TODO` that describes a bug or suggests a next step, in the style under
  "TODOs and docs" below
- resolve a merge conflict that's only in comments. A conflict in code goes back to the
  students to settle.

Everything outside `TeamCode/` (`doc/`, this file, the build scripts) can be edited as
usual, under the rules below.

**Who you're working with.** Go by what the person tells you. If they say they're a
mentor or another adult, or you can't tell whether you're working with a student, this
rule applies until a student says they're the one at the keyboard. In a student's
session, the rest of this file says how to help them write their own code.

## Follow this repo's style and practices

Code in this repo should look like the rest of the repo: written by the team, in the
team's patterns. A clever or more "standard" solution that the team doesn't use is the
wrong solution here.

### Read before changing code

Before you change anything in `RobotModel/`, `AutonStrategies/` or `OpModes/`, read the
doc that covers it. These docs are the source of truth, and this file only summarizes
them:

- [doc/AbstractRobotObjectModel.md](doc/AbstractRobotObjectModel.md): the layers and what
  each one must do.
- [doc/NewRobotDesignWorkflow.md](doc/NewRobotDesignWorkflow.md): order of work,
  **Minimal Templates** (copy these), and the **Student Checklist**.
- [doc/TelemetryContract.md](doc/TelemetryContract.md): the two telemetry rules and the
  checklist for new code.
- [doc/FlintLessons/Advanced/common-pitfalls.md](doc/FlintLessons/Advanced/common-pitfalls.md):
  bug patterns this codebase has already had.

### Architecture rules

1. **Keep the layers.** OpMode → `Robot` → `DriveTrain` and `MechAssembly` →
   `MechComponent`.
   - Only a component (or the drive train) holds a motor or servo.
   - Robot-wide sensors (IMU, Limelight) are fetched and set up once by the `Robot`, then
     passed down.
   - Each device name gets one `hardwareMap.get(...)` in the whole codebase.
2. **OpModes stay thin.** Build the robot, `waitForStart()`, then loop on
   `robot.update(...)` and `robot.updateTelemetry()`. Mechanism logic in an OpMode is a
   sign the design has leaked.
3. **Telemetry.** Only `Robot.updateTelemetry()` calls `telemetry.update()`.
   - Use two-step setup: build with hardware, then wire telemetry in
     `initializeTelemetry`.
   - Every assembly reports every child.
   - Strategies report through `reportStatus` / `reportData`. They never hold raw
     `Telemetry`.
4. **Gamepad mappings are component strategies**, written as lambdas in the assembly.
   Components don't read gamepads on their own.
5. **Autonomous code uses verbs** (`startIntake()`, `pushBalls()`), never raw hardware.
   New verbs name an intention, not a tuning number.
6. **Code that runs every loop never waits.** No `sleep`, `TrySleep` or waiting `while`
   loop inside `giveInstructions`, `update`, `updateState` or a strategy lambda. For
   timed steps, use `StateMachine` and `doAndWait`.
7. **Put coordination at the right layer.**
   - One mechanism: its component.
   - Several mechanisms in one assembly: a macro (`IMacroAssemblyStrategy` through
     `MacroManager`).
   - Robot-wide: an assist (`IAssistRobotStrategy` through `AssistManager`).
   - Anything that takes over parts must declare them in `claims()`.
8. **Vary robots with constructor arguments** (tag ID, wheel directions), not by
   copy-pasting a class.
9. **A mechanism with both a teleop path and an autonomous path:** when you change one
   path, check the other path's signs and setup.

### Use what's already here

- Prefer the repo's existing tools: `StateMachine` and `IState`, the `Extensions/*`
  helpers, `MacroManager` and `AssistManager`, `RobotBuilder` and `MecanumDriveBuilder`,
  and the templates in `NewRobotDesignWorkflow.md`.
- **Ask a mentor first** before any of these, and note the approval in the process log:
  - adding a library or dependency
  - adding a new design pattern, framework or base class
  - changing a base class or shared interface (`Robot`, `DriveTrain`, `MechAssembly`,
    `MechComponent`, `StateMachine`, `IState`, the strategy interfaces)
  - editing `FtcRobotController/`, which is the FTC SDK
- **Stay in scope.** Change only what the task needs. No renaming, reformatting or
  "cleanup" of code the task doesn't touch.
- **Make small commits**, one plan step each, small enough that the student can explain
  every line. If a step grows past about 150 changed lines, split it.

### TODOs and docs

- **A `TODO` marks a known bug that mentors left in place on purpose** for students to
  find and fix.
  - Don't fix a TODO the student didn't ask about.
  - **Fix one TODO per request.** If the student asks for several at once ("complete
    these TODOs", "fix all the TODOs"), say no to the batch and explain why. Then:
    1. Choose one TODO together.
    2. Have the student explain the bug in their own words, and record it in the
       process log.
    3. Fix it, or better, coach the student while they fix it.
    4. Commit it on its own.
    5. Only then move on to the next TODO.
  - Never delete a TODO without fixing it.
  - When you do fix one, remove it in the same commit.
  - A bug you find but don't fix gets a new TODO in the same style: describe it where it
    happens, with enough context to work out the fix.
- **Docs follow the code.** If a change makes a doc wrong, update the doc in the same
  commit:
  - small fixes (a class or method name)
  - a new section only when the doc's owner asks for it

  `doc/FlintLessons/` is mentor-owned. Don't rewrite lessons. Note any mismatch in the
  process log for a mentor.

### Formatting (match the file you're in)

- 4-space indent, no tabs.
- Most files put a class's or method's opening brace on its own line. Some put it on the
  same line. Follow whichever style the file already uses.
- Naming patterns:
  - strategy interfaces: `<Thing>ControlStrategy`, `<Thing>TelemetryStrategy`
  - general contracts: start with `I` (`IState`, `IAutonStrategy`)
  - nested autonomous classes: `Autonomous<Thing>Behaviors`
  - constants: `UPPER_SNAKE_CASE`
  - static helpers: `Extensions/<Thing>Extensions.java`
- Comments explain **why**. Class Javadoc says what the class is for in the robot. Match
  how heavily the file you're in is commented.

### Git branches

`master` holds only code that has been tested and is ready to run on the robot.

- **Every feature or fix goes on its own branch**, named `<firstname>/<short-topic>`, for
  example `<firstname>/cascade-stow`. Before changing any file, check the current branch. If
  it's `master`, create a branch first.
- **The branch stays separate until the work is tested and proven ready.**
  - "Tested" means run on the robot it's for, with what happened written under
    "Testing" in the process log.
  - "Compiles" alone is not tested.
  - Code that can't be tested yet, for example because the hardware doesn't exist,
    stays on its branch.
- **Merge into `master` only through a GitHub pull request that a mentor approves.**
  - The pull request links the process log.
  - Assistants never merge into `master`, push to `master`, or force-push. Assistants
    don't approve pull requests either.
- Keep a branch up to date by merging `master` into it. Don't rebase a branch someone
  else is also using.

### The exercises branch

`exercises/todo-bugs` is `master` with known bugs put back in on purpose, each marked
with a `TODO`, for students to practice on. It is **never merged into `master`**.
[doc/Exercises/README.md](doc/Exercises/README.md) on that branch explains how to use it.

- **Students practice on their own branch** made from it, named
  `<firstname>/exercise-<topic>`. That branch is never merged anywhere.
- **The assistant coaches; it doesn't solve.**
  - Never fix an exercise bug.
  - Never point to the broken line or state the answer.
  - Ask questions that lead the student to it, and point to the Flint lesson that
    covers it.
  - When the student has a fix, they compare it with the fix commit on `master`, listed
    in the exercises README.
- **Only mentors add bugs** to `exercises/todo-bugs`, and they keep it current by merging
  `master` into it.

### Before every commit

1. `./gradlew :TeamCode:compileDebugJavaWithJavac` passes. Record the result under
   "Testing" in the process log.
2. Go through the checklist in `TelemetryContract.md` and the Student Checklist in
   `NewRobotDesignWorkflow.md` for every layer you touched.
3. **If the student asks for something that breaks a rule above**, say which rule and
   why before writing code. If they still want it, record it in the process log as a
   **deviation**, with their reason.

## Process logs (required for students)

Every student task that changes code or docs gets a process log, committed to the repo.
Mentors may skip this.

### Where it goes

`doc/ProcessLogs/<firstname-lastname>/YYYY-MM-DD-<short-topic>.md`

- Use the student's name from `git config user.name`. If you can't tell who it is, ask.
- Use one log per task. If the student comes back to the same task on another day, add a
  dated section to the same log.
- Start from the template in [doc/ProcessLogs/README.md](doc/ProcessLogs/README.md).

### When to write it

1. **Before changing any file**, create or open the log. Copy the student's request into
   it **word for word**.
2. **Before carrying out a plan**, write the plan into the log: numbered steps, plus which
   files each step touches. If the student says "do step 3", that step must already be
   in the log.
3. **As you go**, record what changed and why, one line per change.
4. **Before every commit**, update the log and commit it **in the same commit** as the
   code it describes. Never commit code whose log is out of date.

### Rules for the assistant

- **Record the student's requests word for word, not paraphrased.** Include follow-ups
  and corrections ("no, I meant…"), because they show how the student was thinking.
- **Name yourself in the log**, with the tool and model (for example "Claude Code, Opus"
  or "Codex"). If the student switches tools partway through a task, note the switch.
- **Be honest about testing.** For each change, write exactly what was done: run on
  hardware, compiled only, or not tested. Never say "should work" in place of "tested".
- **Never write the "In my own words" section.** That part belongs to the student.
  Before the work is committed:
  1. Ask the student 2–3 questions about the trickiest parts of what changed: a timing
     question, an ownership question, or "what happens if…".
  2. Record your questions in the log.
  3. The student answers in their own words. If an answer is wrong or incomplete, say so
     and point to the code. Don't supply the answer.

  If the student asks you to fill this section in, say no and explain why: they'll need to
  give these explanations to judges without you.
- **If the student fixes a bug described by an existing `TODO`**, name that TODO in the
  log, and ask the student to explain the bug before you fix it.
- **Write down what's still unknown**: open questions, things not checked against the
  robot, and settings copied from another robot.
