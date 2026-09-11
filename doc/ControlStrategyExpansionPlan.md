# Plan for Expanding Control Strategies to the Assembly and Robot Layers

This document proposes a refactor plan for expanding the current control-strategy model so that strategy concepts exist consistently at the `MechComponent`, `MechAssembly`, and `Robot` layers.

This is a design document only.

- It does **not** propose concrete code changes.
- It does **not** introduce final interface signatures.
- It does **not** attempt to rewrite the current implementation in place.

Instead, it defines the reasoning, desired architecture, and migration plan for a future refactor.

Reference classes:

- [MechComponent](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotModel/Mechs/Components/MechComponent.java)
- [MechAssembly](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotModel/Mechs/Assemblies/MechAssembly.java)
- [Robot](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotModel/Robots/Robot.java)
- [BillyMA](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotModel/Mechs/Assemblies/BillyMA.java)
- [BillyRapidFire](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/AutonStrategies/BillyRapidFire.java)
- [LimelightAutoTarget](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/AutonStrategies/LimelightAutoTarget.java)
- [StateMachine](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/AutonStrategies/StateMachine.java)

Related architecture guides:

- [NewRobotDesignWorkflow.md](./NewRobotDesignWorkflow.md)
- [AbstractRobotObjectModel.md](./AbstractRobotObjectModel.md)

## Executive Summary

The current codebase already has a clear control-strategy concept at the component layer:

- a component defines a control-strategy interface
- the assembly injects a concrete strategy, often as a lambda
- the component uses that strategy to translate input into hardware behavior

That pattern works well for local mechanism control.

However, the same idea is not yet formalized at the higher layers:

- assembly-level orchestration exists, but only as special-case logic
- robot-level orchestration exists, but only as special-case logic
- runtime selection of competing strategies is manual and scattered

`BillyRapidFire` is the best current example of an assembly-level orchestrated strategy.
`LimelightAutoTarget` is the best current example of a robot-level orchestrated strategy.

Both prove that the need is real.
Neither is yet part of a standard architectural pattern.

This refactor plan proposes:

1. Keep component strategies as the local, mechanism-level control layer.
2. Add a formal `MechAssembly`-level strategy layer for coordinated subsystem behavior.
3. Add a formal `Robot`-level strategy layer for whole-robot orchestration.
4. Standardize lifecycle, ownership, arbitration, and runtime selection across those layers.
5. Treat state machines as one implementation technique for strategies, not as a separate architectural category.

## Why This Refactor Is Needed

The current design has the right instincts, but the orchestration model is incomplete.

### What works well today

- Components encapsulate hardware and accept strategy injection.
- Assemblies compose components and pass down input.
- Robots compose drive trains, assemblies, and sensors.
- State machines already exist for non-trivial multi-step behavior.

### What is missing

- no standard assembly-level control-strategy abstraction
- no standard robot-level control-strategy abstraction
- no common lifecycle for higher-level strategies
- no consistent model for priority or resource ownership
- no standard way to select strategies at runtime
- no standard way to express "manual strategy," "assist strategy," and "exclusive macro strategy"

### Current symptoms in the Billy implementation

`BillyMA` currently coordinates `BillyRapidFire` by:

- storing a specific state machine instance
- checking `BRF.isComplete()` inside multiple component lambdas
- starting the sequence manually from `giveInstructions()`
- letting the sequence update itself from `giveInstructions()`

That works, but the coordination policy is distributed across several places.

Similarly, `BillyRobot` currently coordinates `LimelightAutoTarget` by:

- storing a specific targeter instance at the robot level
- updating it from `Robot.update(...)`
- informally deciding when it should or should not run

Again, this works, but it is not yet a standardized robot-level strategy mechanism.

## Desired End State

The goal is a layered strategy architecture that mirrors the layered robot object model.

### Strategy layers

| Layer | Purpose | Existing example | Proposed role |
|---|---|---|---|
| Component strategy | translate local operator intent into one mechanism's behavior | intake, pusher, turret lambdas | keep as-is conceptually |
| Assembly strategy | coordinate multiple related components within one subsystem | `BillyRapidFire` | formalize |
| Robot strategy | coordinate drive train, assemblies, and robot-wide sensors/helpers | `LimelightAutoTarget` | formalize |

### Key principle

Each layer should own the kind of orchestration appropriate to that layer:

- component strategy: one mechanism
- assembly strategy: several mechanisms in one subsystem
- robot strategy: several subsystems plus robot-wide sensing

That preserves the same abstraction boundaries already established elsewhere in the codebase.

## Design Principles for the Refactor

### 1.  Do not discard the current component strategy pattern

The component layer already has the right basic shape.

The refactor should extend the pattern upward, not replace it.

### 2.  Make orchestration explicit

If a behavior coordinates multiple mechanisms, it should be represented as a first-class strategy object, not hidden inside:

- scattered boolean guards
- ad hoc state fields
- special-case logic embedded in `giveInstructions()` or `update()`

### 3.  Separate strategy implementation from strategy selection

A strategy is one thing.
A selector or manager that chooses between strategies is another.

That distinction is essential if runtime strategy switching is a real goal.

### 4.  Preserve thin OpModes

OpModes should still choose a robot and start the lifecycle.
They should not become the place where control-mode arbitration happens.

### 5.  Treat state machines as an implementation detail

Some strategies will naturally use `StateMachine`.
Others may not.

The architecture should not require every strategy to be a state machine, but it should allow state machines to plug cleanly into the strategy model.

### 6.  Prefer resource ownership over scattered guard conditions

Today, `BillyRapidFire` blocks manual behavior through repeated `if (!BRF.isComplete()) return;` checks.

That is really an ownership rule:

- while rapid fire is active, it owns shooter-related resources

The refactor should express this rule directly instead of encoding it repeatedly inside unrelated component lambdas.

## Proposed Architectural Additions

### 1.  Add a formal assembly-level strategy concept

`MechAssembly` should gain a conceptual counterpart to component control strategies.

This assembly strategy layer should be responsible for:

- coordinated control of multiple components in the same assembly
- temporary overrides of normal manual behavior
- macros and multi-step sequences
- arbitration between default manual behavior and orchestrated behavior

### Intended strategy types at the assembly layer

- default manual strategy
- assist strategy
- macro strategy
- exclusive sequence strategy

Examples in Billy terms:

- default manual strategy: normal intake, pusher, flywheel, and turret control
- assist strategy: possible future "spin up shooter while still allowing turret aim"
- exclusive sequence strategy:  `BillyRapidFire`

### What this would standardize

Every assembly strategy should conceptually answer the same questions:

- What assembly resources do I control?
- When am I eligible to run?
- Am I exclusive or cooperative?
- How do I start?
- How do I update each loop?
- When am I complete?
- How do I stop or yield?
- What status or telemetry should I report?

Those questions are currently answered informally in Billy code.
The refactor would make them explicit.

### 2.  Add a formal robot-level strategy concept

`Robot` should gain a strategy counterpart above `MechAssembly`.

This robot strategy layer should be responsible for:

- coordinating drive train and mechanism behavior together
- integrating robot-wide sensors or vision systems
- implementing assist modes that span multiple subsystems
- selecting between full-robot control modes at runtime

Examples in Billy terms:

- default driver-controlled strategy
- target-assist strategy
- possible future drive-and-shoot assist
- possible future auto-align-and-fire routine

`LimelightAutoTarget` is already acting like the beginning of this category.
The refactor would make that role first-class.

### What this would standardize

Every robot strategy should conceptually answer:

- Which top-level resources do I control?
- Am I background assistance or exclusive control?
- What triggers my activation?
- What happens to the default drive/mech flow while I am active?
- Can I be interrupted by operator input or higher-priority modes?

### 3.  Introduce strategy managers at the assembly and robot layers

Formal strategies alone are not enough.
If the system is going to support runtime strategy selection, it also needs a consistent place to manage that selection.

### Assembly strategy manager

Each `MechAssembly` should conceptually own a strategy manager responsible for:

- registering available assembly strategies
- tracking the currently active strategy or strategies
- selecting defaults
- starting temporary overrides
- resolving conflicts between strategies that want the same resources

### Robot strategy manager

Each `Robot` should conceptually own a higher-level manager responsible for:

- selecting robot-wide control modes
- activating or deactivating robot assists
- coordinating robot strategies with drive train and mech behavior
- deciding when robot-level strategies override or compose with lower-level strategies

This separates:

- "what strategies exist"
- "which one is active right now"
- "who wins when strategies overlap"

## Strategy Taxonomy

To keep the refactor understandable, the new system should classify strategies by behavior rather than by implementation detail.

### Default strategies

These are the normal pass-through behaviors that should exist even when nothing special is happening.

Examples:

- normal driver drive behavior
- normal mech manual behavior

These are not special cases.
They are still strategies, just the baseline ones.

### Assist strategies

These modify or enrich control without fully replacing it.

Examples:

- target tracking that only owns turret control
- heading hold that only owns turn correction
- automatic shooter spin-up while leaving other controls manual

These are often cooperative rather than exclusive.

### Macro strategies

These run a multi-step behavior on demand.

Examples:

- rapid fire
- intake-and-index sequence
- stow mechanism sequence

These are often exclusive over a subset of resources.

### Mode strategies

These change the overall control scheme until explicitly changed.

Examples:

- normal driving mode
- assisted targeting mode
- endgame mode
- field-centric vs robot-centric mode

These are especially relevant at the robot layer.

## Resource Ownership and Arbitration

This is the most important design problem in the refactor.

Without a clean ownership model, higher-level strategies will continue to rely on scattered guard logic.

### Resource ownership model

Every strategy should conceptually declare which resources it owns while active.

At the assembly level, resources might be:

- intake
- pusher
- flywheel
- turret

At the robot level, resources might be:

- drive train translation
- drive train rotation
- full drive train
- mech assembly
- specific assembly subresources
- robot-wide sensors/helpers

This allows clear answers to questions like:

- can target assist run while the driver still controls translation?
- can rapid fire take over the shooter while the driver still controls the turret?
- can a robot-level auto-align strategy temporarily own drive rotation but not translation?

### Arbitration rules

The refactor should define a simple first version of arbitration:

1. Explicitly triggered exclusive strategies beat default strategies on owned resources.
2. Higher-priority safety or stop strategies beat everything else.
3. Cooperative assist strategies may coexist only when their owned resources do not conflict.
4. When a strategy loses ownership, it must be cleanly interrupted or suspended.

This is much clearer than embedding "do nothing if another sequence is active" checks inside unrelated control lambdas.

## Strategy Lifecycle

Higher-level strategies need a standard lifecycle.

Without that, each strategy will invent its own activation and teardown rules.

### Proposed lifecycle states

- registered
- available
- active
- suspended
- completed
- cancelled

Not every strategy must use every state, but the architecture should support them.

### Lifecycle events

Every assembly or robot strategy should conceptually support:

- enter
- update
- complete
- interrupt
- cancel
- exit

This is where existing state machines fit naturally.

For example:

- `BillyRapidFire` already has clear start, update, completion, and abort semantics
- `LimelightAutoTarget` already has clear active-update behavior and can conceptually be stopped

The refactor should not erase those patterns.
It should normalize them.

## Runtime Strategy Selection

The user specifically asked for a standard way to select different control strategies at runtime.

That means the design must separate three ideas:

- the available strategies
- the current operator/sensor context
- the strategy-selection policy

### Selection triggers

Strategies should be selectable through:

- explicit button press
- button hold
- toggle mode
- sensor condition
- match phase or timer condition
- autonomous routine request

### Selection policies

The first version of the refactor should keep this simple.

Recommended policy model:

- default strategy always exists
- explicit requests may activate overrides
- overrides either run until complete or until cancelled
- when an override ends, ownership returns to the default strategy automatically

This would turn today's Billy behavior into a standard pattern instead of a special case.

### Billy example under the proposed model

Today:

- `gamepad.a` starts `BillyRapidFire`
- component lambdas check `BRF.isComplete()` to know whether to act

Under the proposed model:

- `gamepad.a` requests an assembly-level macro strategy
- the assembly strategy manager activates rapid fire
- rapid fire claims the shooter-related resources it needs
- the default manual assembly strategy no longer controls those claimed resources while rapid fire is active
- when rapid fire completes, control returns automatically

The important difference is that the ownership rule becomes architectural instead of incidental.

## Relationship to the Existing StateMachine Pattern

The current `StateMachine` abstraction should remain useful, but its role should become clearer.

### Current ambiguity

Today, a state machine is sometimes acting like:

- a reusable utility
- a behavior implementation
- a pseudo-strategy

That ambiguity is manageable now, but it will become harder to reason about as the system grows.

### Proposed interpretation

After the refactor:

- `StateMachine` should be understood as one way to implement a strategy's internal progression
- a strategy may use a state machine internally
- the strategy manager should care about lifecycle and ownership, not about internal state implementation details

This gives cleaner architecture:

- strategy = externally visible control policy
- state machine = internal implementation technique for time-based or phased behavior

## Proposed Refactor Phases

The refactor should be staged.
It is not a good candidate for a single large rewrite.

### Phase 1:  Define the vocabulary

Document the intended strategy categories and their roles:

- component strategy
- assembly strategy
- robot strategy
- default, assist, macro, and mode strategies
- ownership and lifecycle terminology

Goal:

- make future design conversations consistent before changing architecture

### Phase 2:  Formalize assembly-level strategy as a concept

Introduce a standard architectural place for:

- default manual assembly behavior
- orchestrated assembly sequences
- resource ownership inside one assembly

Goal:

- stop embedding assembly-level orchestration policy inside scattered component-control lambdas

Primary migration candidate:

- `BillyRapidFire`

### Phase 3:  Formalize robot-level strategy as a concept

Introduce a standard architectural place for:

- robot-wide assist behaviors
- drive/mech coordination
- sensor-driven full-robot control overlays

Goal:

- stop treating robot-wide assist logic as one-off additions inside `Robot.update(...)`

Primary migration candidate:

- `LimelightAutoTarget`

### Phase 4:  Introduce runtime selection and arbitration

Once the strategy layers exist, add a standard selection model:

- default selection
- explicit override requests
- ownership-based arbitration
- automatic return to default after completion

Goal:

- make control-mode selection predictable and reusable across robots

### Phase 5:  Standardize telemetry and observability

Strategies should report status in a consistent way:

- active strategy name
- owned resources
- current lifecycle state
- completion or interruption reason

Goal:

- make strategy-driven behavior debuggable during driver practice and competition

### Phase 6:  Expand robot-by-robot

Only after the architectural pattern is stable should it be applied more widely:

- Billy first
- then future assemblies and robots
- then optional drive-train-level assist modes if needed

Goal:

- avoid turning the refactor into a framework-first rewrite disconnected from real needs

## Recommended First Use Cases

The refactor should be justified by concrete examples, not just abstract elegance.

The strongest initial use cases are:

### 1.  Billy rapid fire

Why first:

- it already exists
- it clearly owns multiple mech resources
- it clearly needs start, update, complete, and interrupt behavior
- it is the cleanest example of an assembly-level macro strategy

### 2.  Limelight target assist

Why second:

- it already exists
- it clearly spans sensor input and mechanism control
- it fits the robot-level assist category
- it demonstrates robot-wide strategy without requiring a fully autonomous mode

### 3.  Future driver-selectable modes

Potential examples:

- normal mode
- target-assist mode
- endgame mode
- safe-stow mode

Why these matter:

- they exercise runtime selection directly
- they show that the strategy model is not only for macros, but also for persistent control modes

## Risks and Tradeoffs

This refactor is worthwhile, but it has real design risks.

### Risk: over-engineering too early

If the architecture becomes too general before serving real use cases, it will be harder for students to understand and maintain.

Mitigation:

- start from Billy use cases
- keep the first version small
- standardize only the patterns already proven necessary

### Risk: building a scheduler that is too clever

A highly dynamic scheduling system may become harder to debug than the problem it solves.

Mitigation:

- prefer explicit ownership and simple priority rules
- prefer deterministic selection over "smart" automatic blending

### Risk: blurring TeleOp and autonomous concerns

If higher-level strategies become too broad, the codebase may lose the useful distinction between operator-driven and autonomous behavior.

Mitigation:

- keep manual strategies, assist strategies, and autonomous strategies conceptually distinct
- reuse architecture where useful, but do not flatten all behavior categories into one undifferentiated system

### Risk: hiding too much from students

A good abstraction should clarify behavior, not make it mysterious.

Mitigation:

- keep the strategy taxonomy simple
- expose active strategy names in telemetry
- document ownership and lifecycle rules clearly

## Success Criteria

The refactor should be considered successful if it produces these outcomes:

- assembly-level orchestration has a first-class architectural home
- robot-level orchestration has a first-class architectural home
- `BillyRapidFire` no longer needs to be coordinated through scattered manual guards
- robot-wide assist logic no longer feels like a one-off exception
- runtime control-mode selection becomes a standard pattern
- students can explain who owns each resource at any moment
- new orchestrated behaviors can be added without rewriting the main control flow

## Final Recommendation

The control-strategy idea in this codebase should be expanded upward, not sideways.

The existing component-level strategy pattern is already the right seed.
The next step is to give it clear counterparts at the `MechAssembly` and `Robot` levels.

The right architectural interpretation is:

- component strategies control one mechanism
- assembly strategies orchestrate one subsystem
- robot strategies orchestrate the whole machine
- strategy managers choose which strategies are active
- resource ownership determines who is allowed to command what
- state machines remain a powerful implementation tool inside strategies

`BillyRapidFire` should be treated as the prototype for assembly-level orchestrated control.
`LimelightAutoTarget` should be treated as the prototype for robot-level assist control.

If this refactor is done carefully, it will make future control modes easier to add, easier to reason about, and easier to teach without abandoning the layered object model already present in the codebase.
