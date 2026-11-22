# Sequence builder for macros

Student: Hailey Duan
Started: 2026-11-20
Branch: hailey/sequence-builder (made from hailey/billy-robot-builder, because it uses the
`rapidFireShots` setting from there)
Assistant: Claude Code (Opus 5.5)

## Goal
Build a timed macro like `BillyRapidFire` as a list of steps that reads top to bottom,
instead of hand-chaining `IState` methods (`startShooter` → `fire` → `retract` →
`stopShooter`). This is the advanced "grow RobotBuilder" project in `Robot.java`.

## What I asked for
My requests to Claude, copied word for word, in order. Include follow-ups and corrections.

> ok i'll do that later, what other builders on to do list?

> yes i can do that

> yes we can continue

> bruh we can start coding now he approves anything

> yes, i commiteed, make branch and get step 1

## Plan
Numbered steps, written down before any code changed.

1. Create `SequenceBuilder`. `new SequenceBuilder(machine)` takes the `StateMachine` it
   belongs to. `.doAndWait(action, millis)` adds a step to a list, and `build()` chains
   the steps from last to first with the machine's existing `doAndWait`, returning the
   first `IState`: `AutonStrategies/SequenceBuilder.java`
2. Rewrite `BillyRapidFire` to use it. `reset(n)` builds spin up → n × (push, retract) →
   stop, with a `for` loop, and each step reports its status when it starts:
   `AutonStrategies/BillyRapidFire.java`
3. Test on Billy, answer the "In my own words" questions, then open a pull request (after
   hailey/imu-builder and hailey/billy-robot-builder have merged).

## What changed
One line per change: file, what changed, why.

## Decisions
- `SequenceBuilder` goes in `AutonStrategies/`, the same package as `StateMachine`.
  `doAndWait` is `protected`, and Java lets classes in the same package call protected
  methods. So the builder can reuse `doAndWait` (and its timer) without changing the
  `StateMachine` base class, which AGENTS.md says needs a mentor's approval.
- Repeats use a plain `for` loop in the macro instead of a `.repeat(...)` method, which keeps
  the builder small. `.repeat(...)` could be a later stretch.

## Deviations and approvals
- AGENTS.md says to ask a mentor before adding a new design pattern or framework, and to
  record the approval here. Hailey started before asking Mr. Cox. Her reason: "he approves
  anything." Not approved yet; Mr. Cox should confirm on the pull request.

## Testing
For each change: run on hardware / compiled only / not tested, and what I saw.

## In my own words
Written by me, not by Claude.

## Still unknown
- Two quirks in today's `BillyRapidFire` that the rewrite should fix (check them on Billy):
  - `reset(0)` still shoots one ball, because `fire()` runs before `--ballCount > 0` is
    checked.
  - The whole chain of states is built at once inside `reset()`, so every
    `reportStatus(...)` line (startShooter, fire, retract, ...) is added to telemetry at
    that moment, not when each step actually runs.
- This branch includes the untested commits from hailey/imu-builder and
  hailey/billy-robot-builder.

## Commits
A commit can't contain its own hash, so add each hash the next time you update this log.
