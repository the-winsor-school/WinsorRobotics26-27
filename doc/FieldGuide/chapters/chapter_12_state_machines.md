# Chapter 12 - State Machines

*One small step per loop, and a note saying what comes next.*

---

A robot does plenty of jobs in order, with waiting in between.  Spin up the
flywheel.  Wait for it.  Push the ball.  Wait.  Pull the pusher back.  And
the whole time, the driver is still driving and the screen still has to show
what's going on.

A **state machine** is how Billy does a job like that one small step at a
time, so the waiting leaves the rest of the robot free to keep working.  Look
here when you want to know how rapid fire or turret targeting works inside,
why rapid fire waits by checking a clock, or how to write a timed sequence of
your own.

Read first:  Chapter 6, for the layers and the OpMode.  Chapter 11, for the
autonomous verbs every state in this chapter calls.  Chapter 1, if lambdas
are new to you.

| Entry | The question it answers |
|---|---|
| 12.1 | Why can't the robot just stop and wait? |
| 12.2 | What is a state? |
| 12.3 | What does `StateMachine` do? |
| 12.4 | How does `doAndWait` wait and keep the robot moving? |
| 12.5 | What are the steps of rapid fire? |
| 12.6 | What about a job that runs until something stops it? |
| 12.7 | Why does Billy run two machines side by side? |
| 12.8 | What do `abort` and `reset` do? |
| 12.9 | When is plain waiting fine? |

---

## 12.1  Why the robot can't just stop and wait

*Where it lives:*  `OpModes/BillyTeleOpBLUE.java`

Every TeleOp program on Billy ends with the same few lines.  Here they are in
`BillyTeleOpBLUE`:

```java
        waitForStart();
        while(opModeIsActive()){
            robot.update(gamepad1, gamepad2);
            robot.updateTelemetry();
        }
```

That `while` is the **loop**.  It starts when the driver presses start, and
it runs until the match ends.  Each trip around it is one **tick**.  In one
tick, `robot.update(...)` reads both gamepads and tells every motor and servo
what to do, and `robot.updateTelemetry()` sends one screen of readings to the
Driver Station.  Then the loop goes around again and reads the gamepads fresh.

A tick is short, and the robot gets through a lot of them every second.
That's what makes Billy feel quick to the driver.  A button pressed now gets
noticed in the very next tick.

Now suppose one part of the robot has to wait.  Rapid fire spins up the
flywheel, and it gives the flywheel 2,200 milliseconds to come up to speed
before the first ball goes in.  Java has a ready-made way to wait:
`Thread.sleep(2200)`.  **`Thread.sleep(millis)`** stops the program right
where it is, for that many milliseconds.  It's a **blocking** call, which
means the line after it has to wait until it's done.

> **Predict.**  Say rapid fire waited for the flywheel with
> `Thread.sleep(2200)`.  What happens to the driver's sticks, the other
> buttons, and the telemetry screen during those 2.2 seconds?

Everything freezes.  The sleep sits inside one tick, so the loop stops going
around.  The gamepads go unread, and the screen shows the same frame the
whole time.  The drive motors keep the last power they were given, so a robot
that was driving forward when the sleep started keeps driving forward.

The answer is a **non-blocking** wait.  Each tick, check the clock.  If it
isn't time yet, finish the tick and check again on the next one.  The rest of
the robot gets its turn every time around.  The `TODO` above
`MecanumDrive.turnToAngle()` points at the same answer for turning in
autonomous, and names `doAndWait` (12.4) as the tool.

*See also:*  12.4, 12.9, Chapter 6, Chapter 22

## 12.2  IState:  one step, and the name of the next

*Where it lives:*  `Extensions/IState.java`

```java
public interface IState {
    public IState execute();
}
```

That's the whole file.  An `IState` is an object with one method,
`execute()`, and `execute()` hands back another `IState`.

A **state** is one step of a job.  Calling `execute()` does a small piece of
work, right now, and then answers a question:  what should run next tick?
The answer is the next state.  When the answer is `null`, the job is
finished.  (`null` means "no object here";  the Aside in Chapter 1 has it.)

Here's the smallest state on Billy.  It's from `LimelightAutoTarget`, the
code that aims the turret:

```java
    public IState stopTurret() {
        return () ->
        {
            turret.stop();
            turret.reportStatus("Turret Stopped");
            return lookForTag();
        };
    }
```

`stopTurret()` builds a state and returns it.  The state is the lambda after
`return`.  (A lambda is a chunk of code handed over to run later;  see
Chapter 1.)  `IState` has one method, so a lambda can stand in for a whole
`IState`, the functional interface idea from Chapter 8.

So calling `stopTurret()` only builds the step.  The turret stops later, when
something calls that step's `execute()`.  Then the turret's power goes to
zero, a line goes into the telemetry buffer, and `lookForTag()` comes back as
the step for the next tick.

Every state does a little, returns right away, and names what comes next.
That's how a whole job fits inside the loop from 12.1, one step per tick.  A
chain of states, each naming the next, is a **state machine**.

*See also:*  12.3, 12.6, Chapter 1, Chapter 8

## 12.3  StateMachine

*Where it lives:*  `AutonStrategies/StateMachine.java`

A chain of states needs something to keep track of which step it's on, and to
run that step once per tick.  That's this class.  Here's the top of it:

```java
public abstract class StateMachine {
    /* Every State Machine must be aware of its Current State */
    protected IState currentState;


    /* if this state machine can terminate, it does so by setting currentState to NULL. ...*/
    public boolean isComplete() {
        return currentState == null;
    }


    /* Abort the Current execution - no questions asked! */
    public void abort() {
        currentState = null;
        elapsedTime = null;
    }


    /* Every State Machine must be able to update its state.*/
    public void updateState() {
        if (currentState != null)
            currentState = currentState.execute();
    }
```

Four pieces, and each one is short.

- `currentState` is the step the machine is on.
- `isComplete()` says whether the job is done.  Done means `currentState`
  is `null`.
- `abort()` stops the job on the spot.  Entry 12.8 explains why it clears a
  second field too.
- `updateState()` runs the current step and keeps whatever it hands back as
  the new current step.  Call it once per tick, and the machine moves one
  step per tick.  Once a step hands back `null`, the machine is done.

`StateMachine` is `abstract` (Chapter 1), so you write a subclass that
`extends` it and supplies the states.  Billy has two:  `BillyRapidFire`,
which fires three balls, and `LimelightAutoTarget`, which aims the turret.
Each has an `update` method whose whole body is `updateState();`.  Here's the
path from the OpMode's loop down to those two calls, drawn by hand:

```
robot.update(gamepad1, gamepad2)              once per tick, from the OpMode
    mech.giveInstructions(gamepad2, ...)      BillyRobot hands off to BillyMA
        macros.update(gamepad)                BillyMA's MacroManager
            BRF.update(gamepad)               one step of rapid fire
    assist.update(gamepad1, gamepad2)         BillyRobot
                                              one step of targeting
```

In a single tick, each machine moves one step, the drive train reads the
sticks, and the other mechanisms read their buttons.  Rapid fire gets
stepped only while it's running, and targeting only while it's switched on.
Chapter 13 covers who switches them.

*See also:*  12.2, 12.4, 12.7, Chapter 13

## 12.4  doAndWait

*Where it lives:*  `AutonStrategies/StateMachine.java`

This is the method that waits and lets the loop keep going.  It's the last
method in `StateMachine`, and it uses a private field, `elapsedTime`, that
entry 12.7 looks at closely:

```java
    protected IState doAndWait(Runnable action, int millis, IState nextState) {
        return () ->
        {
            if (elapsedTime == null) // if we are STARTING this state
            {   // this is an initialization step.
                elapsedTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
                // DO THE THING!
                action.run();
            }


            // if we are still waiting... go let other things happen!!
            // this is the non blocking step.
            if(elapsedTime.milliseconds() < millis)
                return doAndWait(action, millis, nextState);
            // if we have waited long enough, clear the clock
            // and go on over to the next state!
            elapsedTime = null;
            return nextState;
        };


    }
```

`doAndWait` takes three things:  an action to do once, how many milliseconds
to wait after it, and the state to move on to when the wait is over.  It
hands back one state that does all of that, a tick at a time.

> **Aside:  `Runnable` and `ElapsedTime`**
>
> `Runnable` is Java's built-in interface for "a chunk of code that takes
> nothing and hands back nothing."  It has one method, `run()`, so a lambda
> can stand in for it.  `action.run()` runs whatever code was handed in.
>
> `ElapsedTime` is a stopwatch from the FTC SDK.  It starts counting the
> moment it's made, and `milliseconds()` reads how long it's been running.

Follow the state through its ticks.  Say it was built as
`doAndWait(spinUp, 2200, fire)`, where `spinUp` turns on the flywheel.

| Tick | `elapsedTime` when the tick starts | What the state does | What it hands back |
|---|---|---|---|
| first | `null` | starts a new stopwatch, runs `spinUp` | a fresh copy of itself |
| the ones after | a running stopwatch | reads it:  under 2,200 | a fresh copy of itself |
| the last | a stopwatch past 2,200 | sets `elapsedTime` back to `null` | `fire` |

**The first tick runs the action, and only the first.**  The test
`if (elapsedTime == null)` is how the state knows it's just starting.  It
starts the clock and runs the action inside that `if`.  From then on the
clock is running, the test is false, and the action is left alone.

**While it's waiting, the state names itself as next.**  The line
`return doAndWait(action, millis, nextState);` builds a new state with the
same action, the same wait, and the same next step, and hands it back.  The
tick ends right there.  Next tick, `updateState()` runs that copy, which
checks the clock again.  Every tick in between, the rest of the robot gets
its full turn.

When the time is up, the state clears the clock and hands back `nextState`.
The next `doAndWait` in the chain looks for `elapsedTime == null` to know
it's starting, so each wait leaves the clock empty for the one after it.

> **Aside:  closure**
>
> The lambda inside `doAndWait` uses `action`, `millis`, and `nextState`,
> the method's parameters.  The method returns right away, and the lambda
> still has all three every time it runs, ticks later.  A lambda that keeps
> the variables from the code around it is a **closure**.  This one also
> reaches the field `elapsedTime`, which belongs to the whole object (12.7).

> **Try it.**  On paper, trace `doAndWait(spinUp, 2200, fire)` through five
> ticks, with each tick 1,000 milliseconds apart (a slow robot, to keep the
> numbers easy).  You should get:  tick 1 runs `spinUp` and returns a copy;
> tick 2 sees 1,000 and returns a copy;  tick 3 sees 2,000 and returns a copy;
> tick 4 sees 3,000, clears the clock, and returns `fire`;  tick 5 runs the
> first tick of `fire`.  `spinUp` ran once.

*See also:*  12.1, 12.5, 12.7, Chapter 1

## 12.5  BillyRapidFire, step by step

*Where it lives:*  `AutonStrategies/BillyRapidFire.java`

Rapid fire spins up the flywheel, then pushes and retracts the pusher three
times.  Here are its four states:

```java
    public IState startShooter() {
        mechAssembly.reportStatus("startShooter");
        return
                doAndWait(
                        () -> mechAssembly
                                .autonFlywheel
                                .setPower(0.6),
                        2200,
                        fire()
                );


    }
    public IState fire()
    {
        mechAssembly.reportStatus("fire");
        return
                doAndWait(
                        mechAssembly
                                .autonBallPusher
                                ::pushBalls,
                        1000,
                        retract()
                );
    }
    public IState retract()
    {
        mechAssembly.reportStatus("retract");
        return
                doAndWait(
                        mechAssembly
                                .autonBallPusher
                                ::retractPusher,
                        1000,
                        --ballCount > 0
                                ? fire()
                                : stopShooter()


                );
    }
    public IState stopShooter()
    {
        mechAssembly.reportStatus("stopShooter");
        return() ->
        {
            mechAssembly
                    .autonFlywheel
                    .StopShoot();
            return null;
        };
    }
```

Three of the four are `doAndWait` calls (12.4), and each one names the next.
Read as a picture, the job looks like this:

```
startShooter --2200 ms--> fire --1000 ms--> retract --1000 ms--> fire ...
                                                 ... retract --1000 ms--> stopShooter --> null
```

Spin the flywheel up to power `0.6` and give it 2,200 milliseconds.  Push the
ball and give it 1,000.  Pull the pusher back and give it 1,000.  Then fire
again, or stop.  `stopShooter` is a plain lambda that stops the flywheel and
hands back `null`.  Every state works through the autonomous verbs
(Chapter 11).

> **Aside:  `::`, `? :`, and `--`**
>
> `mechAssembly.autonBallPusher::pushBalls` is a **method reference**.  It's
> a short way to write the lambda `() -> mechAssembly.autonBallPusher.pushBalls()`,
> and it fits anywhere that lambda would, including a `Runnable`.
>
> `test ? a : b` is the **conditional operator**.  If `test` is true, the
> whole thing is `a`;  otherwise it's `b`.  So `--ballCount > 0 ? fire() :
> stopShooter()` means "fire again if any balls are left, or else stop."
>
> `--ballCount` subtracts one from `ballCount` first, then uses the new
> value.

**When the chain gets built.**  Java works out every argument before it calls
a method.  So `startShooter()` calls `fire()` for its third argument, `fire()`
calls `retract()`, and `retract()` counts `ballCount` down and calls `fire()`
again.  The whole chain gets built at once, in the tick rapid fire starts:
start, fire and retract three times, stop.  Then the machine walks it one
step per tick.  Each method writes its name to telemetry as it's built, so
all eight names show up together in that first tick.

**How it starts.**  A new `BillyRapidFire` is idle, with `currentState` still
`null`.  Its `start()` calls `reset(shotsPerRun)`, and `reset` sets the count
and puts the machine on its first step:

```java
    public void reset(int ballCount)
    {
        this.ballCount = ballCount;
        currentState = startShooter();
    }
```

`shotsPerRun` is the `3` that `BillyMA` passes in.  In TeleOp, the driver
starts rapid fire from `gamepad2.a`, and Chapter 13 follows that press down.

> **Try it.**  At the robot, with hands clear of the flywheel, run a TeleOp
> and press `a` on gamepad 2.  The telemetry line `Macro` changes from
> `None []` to `BillyRapidFire [PUSHER, FLYWHEEL]`.  The flywheel spins up,
> and the pusher goes out and back three times.  After about eight seconds
> (2,200 plus six waits of 1,000), `Macro` goes back to `None []` and
> `Last macro` reads `BillyRapidFire completed`.

*See also:*  12.4, 12.8, Chapter 11, Chapter 13

## 12.6  A machine that runs until it's stopped

*Where it lives:*  `AutonStrategies/LimelightAutoTarget.java`

Rapid fire has an end.  Turret targeting doesn't, and it's built that way on
purpose.  Here's its main state:

```java
    public IState lookForTag() {
        return()->
        {
            LLResultTypes.FiducialResult tag =
                    LimelightExtensions.tryGetFiducial(
                            limelight,
                            targetTagId);

            if(tag == null)
            {
                turret.reportStatus("Tag " + targetTagId + " not found.");
                return stopTurret();
            }
            double tx = tag.getTargetXDegrees();
            if(tx < -2) return rotateCCW(tx);
            else if(tx > 2) return rotateCW(tx);
            else            return stopTurret();
        };
    }
```

Each tick, `lookForTag()` asks the Limelight for one AprilTag, the target the
OpMode chose (Chapter 5 covers tags, tag IDs, and `tx`).  Then it picks the
next step:

- No tag in sight:  report it, and go to `stopTurret()`.
- The tag is more than 2 degrees to one side:  go to `rotateCCW(tx)` or
  `rotateCW(tx)`, which turn the turret toward it, harder when it's farther
  off.
- The tag is within 2 degrees of center:  go to `stopTurret()`.

Now look at where each of those goes.  `stopTurret()` hands back
`lookForTag()` (12.2), and so do `rotateCCW` and `rotateCW`.  Every road
leads back to looking again.  Drawn out, it's a circle:

```
lookForTag --> rotateCCW or rotateCW or stopTurret --> lookForTag
```

So while targeting runs, `isComplete()` is `false` the whole time.  The job
is keeping a turret pointed at a tag that might move, and that job has no
last step.  It ends when something outside calls `stop()`, which calls
`abort()` (12.8) and sets the turret's power to zero.  To run again,
`start()` puts it back on `lookForTag()`.

That's the difference between Billy's two machines.  Rapid fire's chain runs
downhill to `null`.  Targeting's chain circles back to its start.  Chapter 13
names the two kinds, a macro and an assist.

You can watch the circle on the telemetry screen.  Billy's TeleOps start
with targeting on, and the target tag ID is `-1` until this season's tag is
chosen.  No tag has that ID, so the screen alternates, a tick at a time,
between `Tag -1 not found.` and the two stop lines.  Entry 13.6 says what
that means for the driver.

*See also:*  12.2, 12.5, 12.8, Chapter 5, Chapter 13

## 12.7  Why two machines, side by side

*Where it lives:*  `AutonStrategies/StateMachine.java`

Billy does two stepped jobs at once:  rapid fire and targeting.  It runs
them as two separate machines, and the reason is one line in
`StateMachine`:

```java
    private ElapsedTime elapsedTime;
```

That's the stopwatch every `doAndWait` uses (12.4).  There's one of it per
`StateMachine` object.  Every wait inside one machine reads and writes that
same field.

> **Predict.**  Suppose one machine tried to keep two chains going at once,
> each stepped every tick:  a 2,200-millisecond flywheel spin-up in one, and
> a 500-millisecond wait for the turret to settle in the other.  Both are
> `doAndWait` states in the same object.  What happens?

The two waits trip over each other.  Say the flywheel's starts first and
makes the stopwatch.  The turret's wait sees a stopwatch already running, so
it skips its own action and reads the flywheel's time.  At 500 milliseconds
it finishes and sets the field back to `null`.  On the next tick the
flywheel's wait sees `null`, decides it's just starting, and spins up again
on a fresh clock.

Inside one chain, one clock is plenty, and rapid fire shows it.  Its steps
run one after another, so only one wait is ever going, and each wait clears
the clock before handing off.

So when Billy needs two jobs at once, it makes two objects.  `BillyRapidFire`
and `LimelightAutoTarget` each have their own `currentState` and their own
`elapsedTime`, and each runs on its own.  Each is stepped once per tick
by whatever owns it:  rapid fire by `BillyMA`, through its `MacroManager`,
and targeting by `BillyRobot` (12.3).  That's composition (Chapter 6), used
for jobs that run over time.  `ExampleStateMachineAuton` keeps a private copy
of `doAndWait` with its own clock, too (12.9).

Separate machines still share one robot.  Both of Billy's machines command
parts of `BillyMA`, and so does the driver's gamepad.  `StateMachine` has no
idea which part belongs to which job.  Chapter 13 is about deciding that.

> **Argue it.**  The stopwatch could belong to each wait, where it lives
> now on the whole machine.  `doAndWait` could start its own clock and carry
> it along in each copy it hands back.  Then one machine could run two waits
> at once, and `abort` would have one less thing to clean up (12.8).  What
> would it cost?  Think about how hard the method gets to read, and whether
> anybody on the team needs two waits in one machine yet.

*See also:*  12.3, 12.4, 12.8, Chapter 6, Chapter 13

## 12.8  abort and reset

*Where it lives:*  `AutonStrategies/StateMachine.java`, `AutonStrategies/BillyRapidFire.java`

A job sometimes has to end early, and sometimes has to start over.
**`abort()`** stops a machine on the spot and leaves it idle:

```java
    /* Abort the Current execution - no questions asked! */
    public void abort() {
        currentState = null;
        elapsedTime = null;
    }
```

After `abort()`, `isComplete()` is `true`, and the machine sits idle until
something gives it a new first step.

**`reset`** is that something, for rapid fire.  It sets the ball count and
puts the machine back on its first state (12.5).  `start()` calls it, so
every run begins at `startShooter`.  Targeting has no `reset`;  its `start()`
does the same job by setting `currentState` to `lookForTag()`.

`abort()` only empties the machine.  `StateMachine` has no motors to stop, so
each subclass wraps `abort()` in a method that also makes the hardware safe.
Here's rapid fire's:

```java
    @Override
    public void cancel()
    {
        abort();
        mechAssembly.autonFlywheel.StopShoot();
        mechAssembly.autonBallPusher.retractPusher();
    }
```

Abort the machine, stop the flywheel, pull the pusher back.  Targeting's
`stop()` (12.6) does the same with the turret.

**Why `abort()` clears the clock too.**  Here's a sequence a driver can cause
today.  Rapid fire is partway through `fire()`, so the stopwatch from 12.4 is
running.  The driver presses `b` on gamepad 2, which cancels rapid fire and
calls `abort()`.  A second later the driver presses `a`, which starts it
again:  `start()`, then `reset`, then `startShooter`.

On its first tick, `startShooter`'s wait checks `if (elapsedTime == null)`,
learns it's just starting, and spins up the flywheel.  That works because
`abort()` emptied `elapsedTime`.  Picture an `abort()` that cleared only
`currentState`.  The stopwatch left over from `fire()` would still be there,
so `startShooter` would skip the spin-up and move on whenever the old clock
passed 2,200.  The machine would report a spun-up flywheel, and the flywheel
would be sitting still:  a change of state with no physical action behind
it.

The second line of `abort()` arrived in commit `e89bec0`, the same day the
team began building `cancel()` and the other macro methods on top of it.  The
idea carries over to any machine you write.  A machine's state is every field
its states read, here `currentState` and `elapsedTime`.  When you write
`abort` or `reset`, clear all of them.

One more protection lives outside the machine.  Calling `reset` partway
through a run would throw the run away with the flywheel still spinning.  On
Billy, holding `a` counts as one press, and a second press while rapid fire
is running gets turned down by the `MacroManager` that starts it (13.5).

*See also:*  12.4, 12.5, 12.6, 13.5, 13.7

## 12.9  IAutonStrategy:  when plain waiting is fine

*Where it lives:*  `AutonStrategies/IAutonStrategy.java`

The codebase has a second, simpler style for autonomous code:

```java
public interface IAutonStrategy
{
    void execute();
}
```

An **`IAutonStrategy`** is one whole routine in one method.  Call `execute()`,
and it runs from start to finish before it returns.  It has no current step
and no `updateState()`.

`ExampleStateMachineAuton` is an autonomous OpMode that is also an
`IAutonStrategy`.  Its `execute()` is the entire autonomous period:

```java
    @Override
    public void execute()
    {
        currentState = findTags();
        while (opModeIsActive() && currentState != null){
            currentState = currentState.execute();
        }
    }
```

This one still uses `IState`, with its own private `doAndWait`:  find a tag,
drive left, forward, or right depending on which tag it saw, spin, stop, and
fire one ball.  The difference is the outer shape.  Billy's TeleOp machines
get one step per tick from somebody else's loop.  This method owns its loop
and steps its states until the chain hands back `null` or the period ends.

The plainest form is a lambda that calls verbs and sleeps between them.
`MecanumAutonDance`, written for `Wildbots2025`, starts like this:

```java
            robot.driveTrain.drive(0, 1, 0);
            robot.mechAssembly.drawbridge.goForward();
            ThreadExtensions.TrySleep(500);
            robot.driveTrain.drive(0.5, 0, 0.5);
            ThreadExtensions.TrySleep(500);
```

`ThreadExtensions.TrySleep` is the team's wrapper around `Thread.sleep`
(Chapter 2), and every one of those sleeps blocks (12.1).  In TeleOp that
would freeze the robot.  Here the sleeps are harmless, because this routine
is the only thing running.  The drivers have their hands off the controls,
and no other job is waiting for a turn, so a straight list of "do this, wait, do that" is the clearest way to write it.
`MecanumAutonDance` has no caller yet;  it's an example of the style.

So here's a working rule for choosing.

| Use | When | Billy's examples |
|---|---|---|
| `IAutonStrategy` | one routine has the whole OpMode to itself, start to finish | `ExampleStateMachineAuton`, `MecanumAutonDance` |
| a `StateMachine` subclass | a job moves forward over time while other code keeps running | `BillyRapidFire`, during TeleOp;  `LimelightAutoTarget`, beside the driver |

A 30-second parking routine that runs alone fits the first row.
A heading correction that nudges the robot straight while the driver drives
fits the second, because the driver's sticks need reading every tick.

*See also:*  12.1, 12.3, Chapter 2, Chapter 11

---

## Chapter summary

The robot's loop has to keep going around, so a job that takes time gets cut
into small steps.  Each step is a state:  it does a little work and names the
step for next tick.  A `StateMachine` runs one step per tick, and `doAndWait`
waits by checking a clock each tick, so the rest of the robot keeps working
while it waits.  Billy runs two machines side by side, each with its own
clock.  An autonomous routine that has the robot to itself can skip all of
this and wait the plain way.

| On Billy | The idea |
|---|---|
| the `while (opModeIsActive())` loop in every TeleOp | the **loop**, one **tick** per trip around |
| `IState.execute()` handing back the next `IState` | a **state** |
| `BillyRapidFire` and `LimelightAutoTarget` | **state machines**, stepped by `updateState()` |
| `doAndWait`, handing back a copy of itself | a **non-blocking** wait |
| `TrySleep` in `MecanumAutonDance` | a **blocking** wait, fine when one routine has the loop to itself |
| `abort()` clearing `currentState` and `elapsedTime` | **abort**:  empty every field the states read |
| `ExampleStateMachineAuton` | an **`IAutonStrategy`**:  one routine that owns the loop |

### Java in this chapter

| Syntax | Where |
|---|---|
| `Runnable` and `run()` | 12.4 |
| `ElapsedTime` (FTC SDK) | 12.4 |
| a lambda that keeps its surroundings:  a closure | 12.4 |
| method reference:  `object::method` | 12.5 |
| conditional operator:  `test ? a : b` | 12.5 |
| `--ballCount`, subtract one first | 12.5 |

### Vocabulary

**abort** -- stop a state machine on the spot and leave it idle.  On Billy,
`abort()` empties `currentState` and `elapsedTime`.  (12.8)

**blocking** -- a call that makes the next line wait until it's done.
`Thread.sleep` is one.  (12.1)

**closure** -- a lambda that keeps the variables from the code around it and
still has them when it runs later.  (12.4)

**doAndWait** -- `StateMachine`'s helper for a timed step:  do an action once,
wait some milliseconds a tick at a time, then move on.  (12.4)

**IAutonStrategy** -- an interface for one whole autonomous routine in a
single `execute()` method, run start to finish.  (12.9)

**IState** -- the interface for one step.  Its `execute()` does a little work
and hands back the next step, or `null` when the job is done.  (12.2)

**loop** -- the `while (opModeIsActive())` that runs from start to the end
of the match, updating the robot every time around.  (12.1)

**non-blocking** -- a way of waiting that finishes the tick and checks again
on the next one, so the rest of the robot keeps running.  (12.1, 12.4)

**reset** -- put a state machine back on its first step.  `BillyRapidFire`'s
`reset` also sets the ball count.  (12.8)

**state** -- one step of a job.  (12.2)

**state machine** -- a chain of states, each naming the next, run one step
at a time.  (12.2, 12.3)

**StateMachine** -- the team's abstract class that keeps the current step
and runs it once per call to `updateState()`.  (12.3)

**Thread.sleep** -- Java's method for stopping the program for a number of
milliseconds.  It blocks.  (12.1)

**tick** -- one trip around the loop.  (12.1)

### Check your understanding

1. What would happen to the driver's controls if rapid fire waited with
   `Thread.sleep(2200)`?  Why?  (12.1)
2. Calling `stopTurret()` doesn't stop the turret.  What does it do, and
   when does the turret actually stop?  (12.2)
3. `doAndWait` runs its action exactly once, however many ticks it waits.
   Which line makes sure of that?  (12.4)
4. While `doAndWait` is waiting, what does its state hand back, and why does
   that keep the loop going?  (12.4)
5. Write out rapid fire's states in order for a `ballCount` of 2.  (12.5)
6. Why does `LimelightAutoTarget` keep running until something stops it,
   and what stops it?  (12.6)
7. Billy needs rapid fire and targeting at the same time.  Why are they two
   separate objects?  Use the word `elapsedTime` in your answer.  (12.7)
8. Rapid fire is cancelled in the middle of `fire()`, then started again.
   Walk through why the flywheel spins up on the new run.  (12.8)
9. A routine for the whole 30-second autonomous period, and a correction
   that runs while the driver drives:  which gets an `IAutonStrategy`, and
   which gets a `StateMachine`?  Why?  (12.9)

## Where to go next

- **Chapter 13** picks up where 12.7 stops.  Two machines share four
  mechanisms, and something has to decide which job gets which one.
- **Chapter 11** has the autonomous verbs every state here calls.
- **Chapter 22** has the loop that stops, as one of the shapes bugs take.
- **Chapter 5** explains the AprilTag search inside `lookForTag()`.
