# Chapter 13 - Control Ownership

*One owner at a time, and a rule for who.*

---

Billy has four mechanisms in its assembly, and three kinds of code that want
to move them:  the driver's gamepad, rapid fire, and turret targeting.  A
motor or a servo obeys whatever it was told last, so when two of those want
the same part in the same tick, something has to decide.

This chapter is about how Billy decides.  The big idea is short:  a part
needs one owner at a time.  Each special job says which parts it needs, the
driver's everyday controls step aside on those parts, and the robot has a
rule for what happens when two jobs want the same one.  Look here when
you're wondering why the turret ignores the bumpers, what the `Assist` and
`Macro` lines on the telemetry screen mean, or how to add a new job without
breaking the old ones.

Read first:  Chapter 12, for how rapid fire and targeting run one step per
tick.  Chapter 8, for control strategies.  Chapters 9 and 10, for the
assembly and robot layers.

| Entry | The question it answers |
|---|---|
| 13.1 | What's a resource, and why does it need one owner? |
| 13.2 | What separates a behavior from the code that selects it? |
| 13.3 | What are default, assist, macro, and mode strategies? |
| 13.4 | How does a strategy say which parts it needs? |
| 13.5 | What does `MacroManager` do? |
| 13.6 | Who controls the turret? |
| 13.7 | What do the buttons do, and who wins when two jobs collide? |
| 13.8 | What does stop all stop? |
| 13.9 | What do the strategy lines on the telemetry screen mean? |
| 13.10 | What does the current system support? |

---

## 13.1  Resources and owners

*Where it lives:*  `RobotModel/Mechs/Assemblies/BillyMA.java`

Billy's assembly names its four mechanisms in one short list:

```java
    public enum Part {
        INTAKE,
        PUSHER,
        FLYWHEEL,
        TURRET
    }
```

> **Aside:  `enum`**
>
> An `enum` is a type with a fixed list of named values.  `BillyMA.Part` has
> exactly four:  `Part.INTAKE`, `Part.PUSHER`, `Part.FLYWHEEL`, and
> `Part.TURRET`.  Those names are the values this enum defines.  Java checks
> that callers use the right enum type; an intake name from another robot's
> enum would belong to a different type.

Each of those parts is a **resource**:  a piece of the robot that takes
commands, where two different commands at once would fight.  The turret is
the clearest one.  It's a continuous-rotation servo, and every tick it spins
at whatever power the last `setPower` call gave it.

Picture two pieces of code that both want the turret in the same tick.  The
driver's right bumper says spin at `-1`.  Targeting says spin at `0.3`,
toward the tag.  Both calls happen.  The turret gets whichever one ran later
in the tick, and the other is thrown away.  Which one ran later depends on
the order the lines happen to sit in the code.  Move one line, and the robot
behaves differently.

So a resource needs an **owner**:  the one piece of code allowed to command
it right now.  **Ownership** is that right, and it can pass from one piece
of code to another while the robot runs.  **Arbitration** is deciding who
gets it when two want it at once.

Billy's answer, in one sentence:  each special job declares which parts it
needs, and the driver's everyday controls skip those parts while the job
runs.  The declaration is called a **claim**.  Rapid fire claims the pusher
and the flywheel.  Targeting claims the turret.  Everything nobody has
claimed belongs to the driver.

The rest of this chapter is how that sentence turns into code.  13.4 shows
the claims, 13.5 and 13.7 show who decides, and 13.6 follows the turret
through all of it.

*See also:*  13.3, 13.4, 13.6, Chapter 3, Chapter 9

## 13.2  A behavior and its manager have different jobs

*Where it lives:*  `AutonStrategies/BillyRapidFire.java` and
`RobotModel/Mechs/Assemblies/MacroManager.java`.

Billy's rapid-fire routine knows how to sequence the shooter and pusher.
It can start, advance one step, report completion, and cancel.  It receives
the assembly's autonomous behaviors, so it can request actions through
methods such as `pushBalls()` (Chapter 11).

The assembly also has an object that chooses whether this routine may run:
`MacroManager`.  A request reaches it through this assembly method:

```java
    public void requestRapidFire()
    {
        macros.request(BRF);
    }
```

`BRF` is the rapid-fire object.  The manager's `request` method decides
whether to accept it.  Once accepted, the manager calls `start` and later
calls `update` each tick.  The routine decides what those calls do to the
sequence.  Section 13.5 follows the manager's acceptance rule.

This separates **behavior** from **selection**.  Behavior answers "what
steps perform this job?"  Selection answers "which job may run now?"
Keeping the questions separate costs another object and another method
call to trace.  It buys one place to look for the macro's admission and
completion rules.  The routine can concentrate on its sequence.  That is
the trade-off behind this small manager.

The distinction also separates ownership from timing.  A state machine
can sequence a push and a retraction over several ticks, as Chapter 12
explains.  Those timed steps don't automatically prevent a gamepad
strategy from commanding the same servo.  Billy's manager and claims-aware
dispatch supply that other part of the arrangement.

At the robot layer, selection currently lives in `BillyRobot` itself.
It stores an active assist and handles the buttons that start or stop
one.  There is no need to imagine another manager class to understand
that code.  Follow the actual selection methods in 13.6 and 13.7.

*See also:*  13.5, 13.6, Chapter 11, Chapter 12

## 13.3  Default, assist, macro, and mode

*Where it lives:*  `BillyMA`, `BillyRapidFire`, and `LimelightAutoTarget`.

When Billy is running, its ordinary gamepad controls provide the
**default strategy**.  Each tick, unclaimed parts receive manual commands.
A default can therefore run alongside a special job that uses other
parts.  "Default" describes its role in selection; its implementation
in Billy's assembly is a lambda (Chapter 8).

The targeting behavior is an **assist strategy**.  It reads the camera
and commands the turret while the driver can still drive the chassis
and use other mechanisms.  The assist owns its claimed part exclusively:
cooperation across the robot doesn't mean blending two turret power
requests.  Section 13.6 traces that distinction through one loop.

Rapid fire is a **macro strategy**:  a multi-step behavior requested as
one job.  It starts the shooter, pushes and retracts, and eventually
finishes.  Its pusher and flywheel are unavailable to their manual
controls while the macro claims them.  It can be cancelled before it
finishes; see 13.8 for the cleanup calls.

| Kind | Example on Billy | How the special behavior ends |
|---|---|---|
| Default | Manual controls for unclaimed parts | It is skipped wherever an active job claims a part |
| Assist | `LimelightAutoTarget` | An explicit stop or replacement |
| Macro | `BillyRapidFire` | Completion or cancellation |
| Mode | No separate mode strategy implemented here | Depends on the proposed mode's design |

A **mode strategy** would change a control scheme until another selection
changed it again.  An endgame control scheme is a possible example in
`doc/ControlStrategyExpansionPlan.md`.  That example describes a possible
design; Billy currently has no separate mode strategy implementing it.

These names describe the behavior's role.  They don't tell you which
mechanisms it needs or what wins a conflict.  For those questions, read
its `claims()` method and its caller's arbitration rule.  Even two
behaviors that both use a state machine can have different selection
rules.  Billy's macro has a completion test; the assist interface has
an explicit `stop` operation.

*See also:*  13.4, 13.5, 13.7, 13.10, Chapter 12

## 13.4  Claims name the parts a job needs

*Where it lives:*  `BillyRapidFire.claims` and `LimelightAutoTarget.claims`.

Rapid fire declares its resources in one method:

```java
    @Override
    public Set<BillyMA.Part> claims()
    {
        return EnumSet.of(BillyMA.Part.PUSHER, BillyMA.Part.FLYWHEEL);
    }
```

The return value is a **set**:  a collection of distinct values.  Here
the values are `BillyMA.Part` names.  `EnumSet.of` constructs a set from
the listed enum values.  The type `Set<BillyMA.Part>` tells Java what
kind of values belong in this collection; see 7.3 for generics.

The targeter uses the same method shape but names a different resource:

```java
    @Override
    public Set<BillyMA.Part> claims()
    {
        return EnumSet.of(BillyMA.Part.TURRET);
    }
```

A claim is the behavior's declaration of what it needs while active.
The manager and robot collect those declarations.  The assembly's
manual dispatch then asks whether the collected set contains each
part before calling its manual controls (13.6).

The declaration alone doesn't acquire a hardware lock.  Calling
`claims()` returns a set; it doesn't change motor power or prevent
another method from writing to the servo.  Ownership works because
the callers use the returned set when choosing which commands to
issue.  A new caller needs to participate in that same arrangement.

Compare the two declarations above.  Neither claims `INTAKE`.
Rapid fire also leaves `TURRET` out.  The macro and targeter can
therefore run together:  their claimed sets share no part.  Sets
with no shared values are **disjoint**.  If a future behavior requested
the pusher while rapid fire was using it, those sets would overlap,
and the caller would need the rule described in 13.7.

> **Predict.**  Rapid fire and targeting are both active.  Which parts
> can still receive manual assembly commands?

Only the intake is unclaimed in that combination.  Turning targeting
off also makes the turret available to manual control.  Completing
rapid fire releases the pusher and flywheel, independently of the
targeter's claim.

*See also:*  13.1, 13.5, 13.6, 13.7, Chapter 7

## 13.5  MacroManager tracks one active macro

*Where it lives:*  `RobotModel/Mechs/Assemblies/MacroManager.java`.

Billy's assembly stores a `MacroManager<Part>`.  Its type parameter
means this manager deals in the assembly's part names.  The manager
stores one active macro reference; `null` means its slot is empty.
Here is how it accepts a request:

```java
    public void request(MechAssembly.IMacroAssemblyStrategy<R> macro)
    {
        if (active != null) return;   // != here: something's already running, so ignore this request
        active = macro;
        active.start();
    }
```

If a macro is already active, the method returns immediately.  It
doesn't queue the new request or replace the running job.  Otherwise
it stores the requested object and starts it.  This is the manager's
**first-active-wins** rule.

The short early return is a **guard clause**.  It checks a condition
at the entrance to an operation and returns when that operation
shouldn't proceed.  Here the guard prevents a second macro from
occupying the single slot.  Read past it only for the accepted case.

The assembly calls the manager's update each tick:

```java
    public void update(Gamepad gamepad)
    {
        if (active == null)
            return;
        active.update(gamepad);
        if (active.isComplete()) {
            lastEnd = active.name() + " completed";
            active = null;
        }
    }
```

With an active job, the manager advances it and checks completion.
If it has finished, the manager records an end reason and clears
the slot.  Later in that same assembly update, the claim collection
sees the slot as empty.  Manual control can resume on those parts.

Cancellation follows another path.  `cancel(reason)` calls the
active macro's `cancel`, records its name and the supplied reason,
then clears the slot.  The macro supplies the hardware cleanup;
the manager supplies the selection bookkeeping.  Section 13.8
shows rapid fire's cancellation body.

The manager's `claimed()` returns the active job's claims, or an
empty set when idle.  `activeName()` similarly returns its name
or `"None"`.  These let dispatch and telemetry inspect the same
selection.  They don't need to know which rapid-fire state is
currently running.

*See also:*  13.2, 13.4, 13.8, 13.9, Chapter 12

## 13.6  Who controls the turret?

*Where it lives:*  `BillyRobot`'s strategy and `BillyMA.giveInstructions`.

Billy starts its targeter assist at the end of the robot constructor.
The turret is therefore claimed when the normal update loop begins.
To learn why its manual bumpers are skipped, follow the claim down
through the assembly's two-argument update:

```java
    public void giveInstructions(Gamepad gamepad, Set<Part> robotClaims) {

        macros.update(gamepad);     // 1. step the macro (and release it when done)

        Set<Part> claimed = EnumSet.noneOf(Part.class);
        claimed.addAll(macros.claimed());
        claimed.addAll(robotClaims);
        strategy.execute(this, gamepad, claimed);
    }
```

First the macro advances.  Then the assembly makes an empty set and
adds both the macro's claims and the claims supplied by the robot.
Combining sets this way produces their **union**:  every part claimed
by either source appears in the result.  A part that appears in both
still appears just once.

The default assembly strategy receives that set.  Its turret branch is:

```java
            if (!claimed.contains(Part.TURRET))
                turret.move(gamepad);
```

With targeting active, the set contains `TURRET`, so this manual call
is skipped.  The other branches make the same check for their parts.
The robot then advances the active assist after the assembly call:

```java
            driveTrain.drive(gamepad1);
            mech.giveInstructions(gamepad2, assistClaims());
            if (assist != null) {
                assist.update(gamepad1, gamepad2);
            }
```

The targeter can now command the turret through its autonomous
behaviors.  Holding a manual turret bumper doesn't stop the assist
or take ownership back.  The driver uses `gamepad1` Y to turn it off,
or B to cancel the special jobs (13.7, 13.8).

Ownership also continues while the targeter requests zero power.
Being centered on a tag, or looking again after losing a tag, doesn't
remove the assist from Billy's active slot.  The targeter keeps its
claim until stopped or replaced.  A zero motor command and an
unclaimed resource mean different things.

> **Try it.**  On paper, trace the set passed to the assembly when only
> targeting is active, only rapid fire is active, and both are active.
> Check which of the four manual `move` calls runs in each case.

*See also:*  13.4, 13.5, 13.7, 13.8, Chapter 9, Chapter 10

## 13.7  Buttons request changes; arbitration chooses the result

*Where it lives:*  the strategy lambda and `startAssist` in `BillyRobot`.

Billy checks four button events before dispatching normal controls:

| Button event | Request |
|---|---|
| `gamepad1.yWasPressed()` | Start targeting if there is no assist; otherwise stop the current assist |
| `gamepad2.aWasPressed()` | Request rapid fire |
| `gamepad2.bWasPressed()` | Cancel the active macro |
| `gamepad1.bWasPressed()` | Cancel the macro and stop the assist |

These checks use press events.  They differ from the held-button
checks inside component controls, such as the pusher's `gamepad.x`
(8.3).  Holding A doesn't continually request a restart on each tick.

The robot also has a rule for starting an assist:

```java
    public void startAssist(IAssistRobotStrategy<BillyMA.Part> newAssist) {
        BillyMA mech = (BillyMA) mechAssembly;
        if (overlaps(newAssist.claims(), mech.macroClaims()))
            mech.cancelMacro("replaced by " + newAssist.name());

        if (assist != null) {
            lastAssistEnd = assist.name() + " replaced by " + newAssist.name();
            assist.stop();
        }
        assist = newAssist;
        assist.start();
    }
```

An arriving assist cancels an overlapping macro and stops any existing
assist.  Even disjoint assists replace each other:  Billy has one assist slot.

The macro-button branch makes the check in the other direction.
After requesting rapid fire, it compares the active macro's claims
with the assist's claims.  Overlap stops the assist.  Today's rapid
fire and targeter use disjoint sets, so both can run.  The replacement
branches describe what would happen to other overlapping behaviors;
no current pair exercises that conflict.

Keep the macro manager's rule separate.  It ignores a request while
a macro is active (13.5).  The subsequent overlap check uses the
macro actually active at that moment, including one already running.
"Newest request wins" would miss that distinction.

Button checks run in the table's order.  If A requests rapid fire
and `gamepad2` B cancels it in the same tick, cancellation runs later.
Then normal dispatch runs.

*See also:*  13.4, 13.5, 13.6, 13.8, 13.9

## 13.8  What stop all stops

*Where it lives:*  `BillyRobot.stopAll`, `BillyRapidFire.cancel`, and
`LimelightAutoTarget.stop`.

Billy's stop-all method has two calls:

```java
    public void stopAll() {
        ((BillyMA)mechAssembly).cancelMacro("stopped by stop all");
        stopAssist("stopped by stop all");
    }
```

It cancels the active macro and stops the active assist.  Each selected
behavior performs its cleanup before its owner clears the active reference.

Rapid fire's cancellation is:

```java
    @Override
    public void cancel()
    {
        abort();
        mechAssembly.autonFlywheel.StopShoot();
        mechAssembly.autonBallPusher.retractPusher();
    }
```

`abort` clears the state machine's current state (12.8).  The other
calls stop the shooter motors and request pusher retraction.  Stopping
future state updates alone doesn't send a motor a zero-power command.
The behavior supplies both the state change and the hardware requests.

The targeter's `stop` similarly calls `abort()` and `turret.stop()`.
Its owner records why it ended and clears the assist slot.  Once
that slot is empty, `assistClaims()` returns an empty set.

The robot's update continues after `stopAll`.  It still calls the
drivetrain and assembly in that tick.  The now-unclaimed assembly
parts receive manual commands, which can change the cleanup requests
just made.  For example, a held manual shooter button can command
the shooter again through the default strategy.  Driving also
continues from `gamepad1`.

Read this method name with its body:  it stops the selected macro
and assist and returns their parts to manual control.  There is no
persistent stopped mode in this method, and no drivetrain stop call.
If you're planning another kind of stop, define which outputs it
must command and how later updates should behave; that is a separate
control requirement.

> **Predict.**  The driver presses stop all while holding a manual turret
> bumper.  Which path can command the turret later in the same update?

The assembly's default strategy can.  Stopping the assist removes
its claim before the manual dispatch takes place.

*See also:*  13.5, 13.6, 13.7, Chapter 11, 12.8

## 13.9  Read the owner before tracing its commands

*Where it lives:*  `BillyRobot.updateTelemetry`.

Billy adds four selection lines before gathering the ordinary robot
report.  The first lines choose how to describe the active assist:

```java
        if (assist == null)
            telemetry.addData("Assist", "None");
        else
            telemetry.addData("Assist", assist.name() + " " + assist.claims());
        telemetry.addData("Macro", mech.macroName() + " " + mech.macroClaims());
        telemetry.addData("Last macro", mech.lastMacroEnd());
        telemetry.addData("Last assist", lastAssistEnd);
```

The current selection and its history answer different questions:

| Label | What its value describes |
|---|---|
| `Assist` | Current assist name and claims, or `None` |
| `Macro` | Current macro name and claims; idle appears as `None []` |
| `Last macro` | Most recently recorded macro ending, initially `None` |
| `Last assist` | Most recently recorded assist ending, initially `None` |

For example, the assist expression can produce
`LimelightAutoTarget [TURRET]`.  The macro manager can produce the
end reason `BillyRapidFire completed` or combine the macro name
with a cancellation reason such as `cancelled by b`.  These are
examples formed from the code's strings, not a captured robot run.

An end reason is history.  It can still describe a previous run
while a new job is active.  Check `Assist` and `Macro` to learn
what is selected now.  Neither line names the behavior's internal
state; for that question, inspect the behavior's own reporting and
the state-machine code in Chapter 12.

Use these meanings to trace a control that appears unresponsive.
If the turret's manual bumpers are skipped, first check whether
`Assist` names a job claiming `TURRET`.  If a macro remains
selected, its claimed parts are still skipped by manual dispatch.
If the part is unclaimed, follow the normal component strategy
and its device commands instead.  The selection report helps
choose the next code path to inspect.

After adding these lines, Billy calls `super.updateTelemetry()`.
That gathers the drivetrain and assembly measurements and flushes
once (10.6).  Selection reports and device reports can therefore
be read together, with the distinction from 11.7 in mind:  a
command value and a measured physical result are different evidence.

*See also:*  13.5, 13.6, 13.8, 10.6, 11.7, Chapter 12

## 13.10  The current system has a small, explicit scope

*Where it lives:*  `MechAssembly.IMacroAssemblyStrategy`,
`Robot.IAssistRobotStrategy`, `MacroManager`, and `BillyRobot`.

The macro interface describes what a manager can ask a job to do:

```java
    public interface IMacroAssemblyStrategy<R>
    {
        Set<R> claims();
        void start();
        void update(Gamepad gamepad);
        boolean isComplete();
        void cancel();
        String name();
    }
```

Those operations let `MacroManager` select a job without knowing
its internal states.  `BillyRapidFire` supplies their implementation,
using its state machine to advance the sequence.  The manager
only needs the interface to start, update, and finish tracking it.

The assist interface has `claims`, `start`, `update`, `stop`, and
`name`.  Its update accepts both gamepads.  It has no completion
test; Billy's assist selection changes through explicit start and
stop calls.  The targeter's update currently advances its state
machine without reading the gamepad arguments.

| Current facility | Scope to remember |
|---|---|
| `MacroManager<R>` | One active macro, with new requests ignored while occupied |
| Assist field in `BillyRobot` | One active assist; starting another replaces it |
| `BillyMA.Part` | Four assembly mechanisms; no drivetrain translation or rotation values |
| Default assembly strategy | Calls manual controls for unclaimed parts |
| Robot arbitration | Methods and button checks in Billy's concrete class |
| End-reason reporting | Strings recording completion, cancellation, or replacement |

Use this table when considering another behavior.  A second macro
can implement the interface, but the manager still offers one slot.
Even if two macros used disjoint parts, this manager wouldn't run
both at once.  Similarly, the assist field can hold one selected
object; disjoint claims alone don't create more assist slots.

The limited scope keeps the policy short enough to trace directly.
The cost appears when a desired behavior needs a different rule:
several simultaneous macros, a queued request, or separately claimed
drivetrain motion would require additional design.  The trade-off
is between the small policy Billy uses and the extra machinery a
broader scheduler would need.

> **Argue it.**  Imagine adding a second macro that uses only the intake.
> Would one active macro at a time be sufficient for its intended use?
> Describe a reason to keep that rule and a reason to allow concurrent
> disjoint macros.  Treat both as design choices, then name what new
> selection and cleanup rules concurrency would require.

*See also:*  13.2, 13.3, 13.4, 13.5, 13.7, Chapter 21

## Chapter summary

Billy coordinates commands by declaring resources and using those claims
when selecting manual and special behavior.  The macro manager tracks one
macro.  The robot tracks one assist and resolves overlaps between the two.
The assembly gives manual control to the parts neither claims.

| To answer... | Read... |
|---|---|
| Which parts does this job need? | Its `claims()` method (13.4) |
| Will a new macro request be accepted? | `MacroManager.request` (13.5) |
| Why are the turret bumpers skipped? | The combined claims and default dispatch (13.6) |
| What happens when requests arrive together? | Billy's button order and replacement rules (13.7) |
| What happens when a job is cancelled? | Its cleanup and the following manual dispatch (13.8) |
| Which job is active now? | `Assist` and `Macro` telemetry (13.9) |

### Java in this chapter

| Syntax | Where |
|---|---|
| `enum Part { ... }`, named values in a type | 13.1 |
| `Set<BillyMA.Part>`, a collection of distinct part values | 13.4 |
| `EnumSet.of(...)`, a set made from listed enum values | 13.4 |
| `EnumSet.noneOf(Part.class)`, an empty set of that enum type | 13.6 |
| `contains` and `addAll`, membership and combining sets | 13.6 |
| `if (...) return;`, a guard clause | 13.5 |

### Vocabulary

**Arbitration** -- deciding which behavior may control a resource when
requests compete.  (13.1, 13.7)

**Assist strategy** -- a behavior that helps with part of the robot while
other control continues.  (13.3)

**Claim** -- a declaration of the resources an active behavior needs.  (13.4)

**Default strategy** -- the normal behavior available for parts that a
special job hasn't claimed.  (13.3)

**Disjoint sets** -- sets with no shared values.  (13.4)

**Guard clause** -- an early check that returns when an operation should
not proceed.  (13.5)

**Macro strategy** -- a multi-step behavior requested as one job.  (13.3)

**Mode strategy** -- a control scheme that continues until another selection
changes it; no separate implementation is shown on Billy.  (13.3)

**Ownership** -- the right to command a resource at the current moment.  (13.1)

**Resource** -- a part whose competing command requests need coordination.
(13.1)

**Union** -- a set containing all values found in either of the combined
sets.  (13.6)

### Check your understanding

1. Why can the order of two servo commands affect the final request?  How
   does ownership make that choice explicit?  (13.1)
2. Which object knows the rapid-fire sequence, and which object accepts
   or ignores a macro request?  (13.2, 13.5)
3. How does an assist cooperate with manual control while still owning
   its claimed resource exclusively?  (13.3, 13.6)
4. What do rapid fire and targeting claim?  Which parts remain manual
   when both are active?  (13.4)
5. Does the manager queue a second macro request?  When does it release
   the first macro's claims on completion?  (13.5)
6. Why can a targeter that is commanding zero power still own the turret?
   (13.6)
7. How do macro requests differ from assist replacement?  What happens
   when A starts a macro and B cancels it in one tick?  (13.5, 13.7)
8. Why are `abort`, motor cleanup, and releasing ownership separate
   actions?  Which commands follow `stopAll` in the same tick?  (13.8)
9. Can `Last macro` describe an old run while a new macro is active?
   Which lines show current selection?  (13.9)
10. Would disjoint claims let the current manager run two macros at
    once?  What limits that behavior?  (13.10)

## Where to go next

Chapter 12 explains the state machines inside these behaviors.  Chapter 11
follows the autonomous verbs they use, and Chapters 9 and 10 show the
assembly and robot calls that connect them.  Chapter 21 applies these
decisions while designing another robot.  For the targeter's camera
measurements, continue to Chapter 5 and the field-position discussion in
Chapter 14.
