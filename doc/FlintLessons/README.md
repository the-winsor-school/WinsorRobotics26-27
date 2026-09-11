# Flint Lessons

Each file here is a **tutor prompt**, written to be pasted into Flint (or any AI tutoring
platform) as the configuration for one session.  They are addressed to the tutor rather
than to the student:  "Ask the student...", "Have them explain...".  Paste one file, hand
the student the chat, and the tutor runs that lesson.

All of them teach *this repository's actual code*, including its open bugs.  Where a
lesson walks a student into a defect, that defect is real and is marked with a `TODO` in
the source.  Search the project for `TODO` to see all eighteen.

## Getting Started - for absolute beginners

Take these first.  They assume no Java and no FTC experience.

| | Lesson | Teaches |
|---|---|---|
| 1 | [Reading Java in This Codebase](GettingStarted/01-reading-java-in-this-codebase.md) | classes, fields, types, methods, constructors, the dot, `if` chains, and enough about lambdas to recognize one |
| 2 | [The FTC Hardware Toolbox](GettingStarted/02-ftc-hardware-toolbox.md) | every motor, servo, and sensor type the SDK offers, and which team component wraps each one |
| 3 | [Sensors, and Where They Live](GettingStarted/03-sensors-and-where-they-live.md) | what a sensor gives you, noise and thresholds, and which layer of the model owns it |
| 4 | [AprilTags and the Limelight](GettingStarted/04-apriltags-and-limelight.md) | what an AprilTag is, both ways this robot reads one, `tx`, and proportional control |

Getting Started 4 leans on the object model in its last two sections.  A beginner can
stop at *The team's wrapper* and come back after main-sequence lesson 9.

## The object model - ten lessons

The main sequence.  It builds the layered robot model one layer at a time, bottom up.

| | Lesson | Layer |
|---|---|---|
| 1 | [The Layered Robot Model](01-layered-robot-model.md) | the whole shape |
| 2 | [Modeling a Component](02-modeling-a-component.md) | `MechComponent` |
| 3 | [Component Control Strategies](03-component-control-strategies.md) | `MechComponent` |
| 4 | [Modeling a MechAssembly](04-modeling-a-mechassembly.md) | `MechAssembly` |
| 5 | [Assembly-Level Control Strategies](05-assembly-control-strategies.md) | `MechAssembly` |
| 6 | [Modeling the Robot Layer](06-modeling-the-robot-layer.md) | `Robot` |
| 7 | [Robot-Level Control Strategies](07-robot-control-strategies.md) | `Robot` |
| 8 | [Autonomous Behaviors as Verbs](08-autonomous-behaviors-as-verbs.md) | every layer |
| 9 | [Autonomous Strategies and State Machines](09-autonomous-strategies-state-machines.md) | `AutonStrategies` |
| 10 | [Capstone:  Designing a New Robot](10-capstone-new-robot.md) | all of it |

## Companion documents

These are written for humans to read directly, rather than as tutor prompts.

- [AbstractRobotObjectModel.md](../AbstractRobotObjectModel.md) -- the architecture, stated as a rule set
- [TelemetryContract.md](../TelemetryContract.md) -- the two telemetry rules and why they exist
- [NewRobotDesignWorkflow.md](../NewRobotDesignWorkflow.md) -- the build order and templates for a new robot
- [ControlStrategyExpansionPlan.md](../ControlStrategyExpansionPlan.md) -- a design proposal for the unfinished half of the strategy system
- [Migration-2026-27.md](../Migration-2026-27.md) -- what carried over from last season, and what stayed behind

## Keeping these accurate

These lessons quote real code, so a refactor can leave a lesson describing something that
has changed.  When you change a signature or rename a class, search this folder for it.
Lessons 2, 4, 8, 9, and 10 all contain quoted source.
