# Chapter 22 - Common Pitfalls

*Five bug shapes, and the detail that gives each one away.*

---

Most bugs come in a handful of shapes.  A lift and a camera have nothing in
common, and they can still have the same bug, written the same way.  Learn the
shape once and you'll spot it in code you've never read.

This chapter is a field guide to five of those shapes.  Each entry gives the
shape's tell, a small example, and a way to catch it.  The examples are mostly
made up, so the shape is easy to see.  Where this codebase has met a shape, the
entry says how the code handles it now.

Look here when something works on the bench and misbehaves in a match, when a
review turns up something odd, or before you write code that touches a
mechanism from two places.

Read first:  Chapter 12, for the robot loop and blocking.  Chapter 13, for who
owns a part.

| Entry | The question it answers |
|---|---|
| 22.1 | What is a bug shape, and why are these so hard to see? |
| 22.2 | What happens when code in the loop waits? |
| 22.3 | Why does autonomous do the opposite of TeleOp? |
| 22.4 | What goes wrong when two classes fetch the same device? |
| 22.5 | Why does a button seem to do nothing? |
| 22.6 | What does the SDK hand back when it has nothing to report? |

---

## 22.1  Bug shapes, and the tell

A **bug shape** is a pattern of mistake that turns up again and again, in code
that's otherwise unrelated.  The **tell** is the one detail that gives a shape
away before you've traced the whole bug:  a `sleep` in the wrong place, two
`if` statements where you expected one.

These five are the **five categories** this chapter covers.

| # | Shape | The tell | Entry |
|---|---|---|---|
| 1 | The loop must never stop | a `while` loop or a sleep inside code that runs every tick | 22.2 |
| 2 | Same mechanism, two conventions | a TeleOp strategy and an autonomous verb driving the same hardware, with nothing shared between them | 22.3 |
| 3 | One handle, unverified assumptions | two classes fetching the same device, and only one of them setting it up | 22.4 |
| 4 | Independent guards fighting over one field | two separate `if` statements that each set the same thing | 22.5 |
| 5 | The SDK's silence isn't a happy path | a value from the SDK used on the next line, unchecked | 22.6 |

Every programmer falls into these, beginners and professionals both, and
there's a reason.  Each one compiles.  Each one reads fine inside the one
method it lives in.  The evidence is always split between two places:  two
code paths, two `if` statements, two classes, or your code and the code it
calls.  Read a method top to bottom and it looks right, because the half that's
wrong is somewhere else.

A tell is a place to look, and it proves nothing by itself.  A `while` loop is
the tell for category 1, and most `while` loops are fine.  Every OpMode's main
loop is one.  When you see a tell, you ask one more question to confirm it, and
each entry says which question that is.

*See also:*  22.2 to 22.6, 21.3

## 22.2  The loop must never stop

**The tell:**  a `while` loop, `Thread.sleep`, or `ThreadExtensions.TrySleep`
inside code that runs once every tick.

A **tick** is one pass through the OpMode's loop (Chapter 12).  Every tick,
the OpMode calls `robot.update(...)`, which works its way down through the
drive train, the assembly and every component strategy, and then
`robot.updateTelemetry()`.  The next tick can't start until all of that
returns.

Here's a made-up lift verb that waits for the lift to reach the top:

```
public void raiseToTop() {
    while (canGoForward()) {
        setPower(1);
        ThreadExtensions.TrySleep(50);
    }
    setPower(0);
}
```

Read alone, it's sensible.  It runs the lift until the top switch presses, then
stops it.  Now suppose a strategy lambda calls it when the driver presses a
button.  The lambda waits inside `raiseToTop()`, so `giveInstructions` waits,
so `robot.update` waits, and the OpMode's loop stops turning for as long as
the lift takes to climb.  The drive motors keep whatever power they had the
moment the button was pressed.  The driver's sticks do nothing, and the Driver
Station shows the same numbers the whole time.

> **Watch out.**  A robot whose loop has stopped is still moving.  If it was
> driving when the loop stalled, it keeps driving, and the driver can't steer
> or stop it until the loop comes back.

The rule:  anything called from inside a tick has to return quickly, every
time.  That covers `giveInstructions`, every strategy lambda, `updateState`,
and a macro's or an assist's `update`.  Waiting belongs only in code an OpMode
calls once and waits on.  A whole autonomous routine can pause between its
steps, which is why `ExampleAutonomousStrategies.MecanumAutonDance` does:

```java
            robot.driveTrain.drive(0, 1, 0);
            robot.mechAssembly.drawbridge.goForward();
            ThreadExtensions.TrySleep(500);
            robot.driveTrain.drive(0.5, 0, 0.5);
            ThreadExtensions.TrySleep(500);
```

When something in the loop has to wait, it waits by returning, and checks the
clock again next tick.  That's what `doAndWait` in `StateMachine` does
(Chapter 12).  These two lines are the whole trick:

```java
            if(elapsedTime.milliseconds() < millis)
                return doAndWait(action, millis, nextState);
```

Until the time is up, the state hands back another copy of itself and lets the
loop carry on.  `BillyRapidFire` waits 2200 milliseconds for the flywheel this
way, and Billy keeps driving the whole time.

In this codebase, `MecanumDrive`'s `turnToAngle` turns the robot inside a
`while` loop, and the `TODO` above it names this shape and points at
`doAndWait`.  No code calls it yet.  Read that `TODO` before you use it.

> **Try it.**  From the repository root, run
> `grep -rnE 'while ?\(|TrySleep|Thread\.sleep' TeamCode/src/main/java`.  For
> each hit, find who calls that code, and ask whether it runs once or every
> tick.  You should find the OpModes' own main loops, `ThreadExtensions`
> itself, a few angle helpers that finish in an instant, `MecanumAutonDance`,
> and `turnToAngle`, and nothing inside a strategy lambda or a
> `giveInstructions`.

*See also:*  Chapter 12, Chapter 13, 21.8

## 22.3  Same mechanism, two conventions

**The tell:**  a mechanism with a TeleOp strategy and an autonomous verb that
both command the same hardware, with no shared constant or shared code between
them.

Every component has two ways in.  The TeleOp strategy turns the gamepad into
commands, and it usually lives in the assembly's constructor (Chapter 8).  The
autonomous verbs live in the component itself (Chapter 11).  They're written
in different files, often at different times, and both of them talk straight
to the same motor or servo.

Take the made-up wrist from 21.9.  Its TeleOp strategy, in the assembly:

```
(servo, gamepad) -> {
    if (gamepad.x)
        servo.setPosition(0.7);   // tilt to score
    else if (gamepad.y)
        servo.setPosition(0.2);   // stow
}
```

And its autonomous verb, in the component:

```
public void score() {
    servo.setPosition(0.3);
    reportStatus("Wrist: scoring");
}
```

The driver's `x` tilts the wrist to `0.7`.  Autonomous `score()` tilts it to
`0.3`.  Each file looks right on its own, and the two of them disagree about
what "score" means.  Nobody sees it, because the two are almost never open
side by side.  The same thing happens with a motor's sign:  intake is positive
in one path and negative in the other, and autonomous runs the intake
backwards.

The fix is never clever.  Give the meaning one home, and point both paths at
it.  In 21.9's sketch the positions are named constants on the component, and
both the strategy and the verbs use them:

```
public static final double SCORE = 0.7;
public static final double STOW  = 0.2;
```

There's a second form of this shape, where the two paths agree on numbers and
disagree on setup.  A servo's direction is a good example.  `PusherServo` sets
its direction once, in its constructor, so the TeleOp strategy and the
autonomous verbs both get it:

```java
    public PusherServo(HardwareMap hardwareMap,
                       String servoNameR,
                       BallPusherControlStrategy strategy,
                       PusherTelemetryStrategy telemetryStrategy) {
        super(strategy);
        this.servoR = hardwareMap.get(Servo.class, servoNameR);
        this.servoR.setDirection(Servo.Direction.REVERSE);
        this.strategy = strategy;
        this.telemetryStrategy = telemetryStrategy;
    }
```

An earlier version set the direction inside the TeleOp path, so an autonomous
run that started before TeleOp had ever run used the servo's default
direction.  Moving that one line into the constructor gave both paths the same
setup.  Setup that every path needs goes where every path passes through.

**How to catch it:**  when you change one path into a mechanism, open the other
one in a second tab before you finish.  Compare the signs, the positions and
any setup.  If the same number appears in both, make it one constant.

> **Try it.**  Open `RobotModel/DriveTrain/Mecanum/MecanumDrive.java` and put
> its two drive methods side by side:  `drive(double x, double y, double t)`
> in `AutonomousMecanumDrive`, and `drive(Gamepad gamepad)` near the bottom.
> Find the four lines in each that add or subtract the turn.  The `TODO` above
> the TeleOp lines describes what to compare.  Which version matches a given
> robot's wiring is a question for the robot itself, with its wheels off the
> ground.

*See also:*  21.9, Chapter 8, Chapter 11

## 22.4  One handle, unverified assumptions

**The tell:**  two classes that call `hardwareMap.get(...)` for the same
device, and only one of them doing the setup the device needs.

`hardwareMap.get` hands back the same object every time you ask for the same
name.  So two classes that both fetch `"imu"` have one device between them.  If one
of them sets it up, the other one gets that setup too, without knowing it.
That's the trap.  The second class works because of something a different
class did, and nothing in its own code says so.

The IMU is this codebase's live example, and 4.9 tells the whole story.
`MecanumDrive` fetches the IMU and never initializes it.  `BillyRobot` fetches
the same device and initializes it, so Billy's turns work.  `Wildbots2025`
also builds a `MecanumDrive` and never initializes the IMU anywhere.  Same
drive train code, and on that robot the heading comes from a device that was
never told how its hub is mounted.

It's the same shape as the turret problem in Chapter 13, one layer down.
There, two pieces of code both *command* one part with no declared owner.
Here, two classes both *assume* one device has been set up, and only one of
them does it.

**How to catch it:**  whenever you see `hardwareMap.get(SomeType.class,
"someName")`, search the code for `"someName"`.  For every other hit, find out
which one sets the device up.  If more than one class needs it, the fix is
one owner, one setup, handed down (4.8).

> **Try it.**  Run `grep -rn 'hardwareMap.get' TeamCode/src/main/java` from
> the repository root, and sort the hits by configuration name.  You should
> find `"imu"` twice, once in `BillyRobot` and once in `MecanumDrive`.  For
> each other name, check that exactly one class fetches it.

*See also:*  4.8, 4.9, Chapter 13

## 22.5  Independent guards fighting over one field

**The tell:**  two separate `if` statements, one after the other, that each
set the same thing.

Here's Billy's intake control as it was before commit `1a6809a`, from
`BillyMA`:

```java
                    if (gamepad.dpad_up) {
                        motor.setPower(0.75);
                    }
                    if (gamepad.dpad_down) {
                        motor.setPower(-0.75);
                    } else {
                        motor.setPower(0);
                    }
```

> **Predict.**  The driver presses up on the d-pad, and nothing else.  What
> power does the motor end up with?

Zero.  Step through it.  The first `if` sees `dpad_up`, and sets `0.75`.  The
second `if` is a new statement with its own say.  `dpad_down` is `false`, so
its `else` runs, and sets `0`.  The second call wins, every loop, so the
intake never runs forward.  Down works, because the second `if` comes last.

Read either block alone and it's correct.  The bug only shows when you notice
that both blocks set `motor`, one after the other, with nothing linking them.

The fix makes the conditions one chain, so exactly one branch runs (1.8).
Commit `1a6809a` did that, and 2.8 shows the intake as it is now:  `if`,
`else if`, `else`.

The rule:  when two conditions are meant to be different settings of the same
thing, make them arms of one `if` / `else if` / `else` chain.

Here's a made-up example to test yourself on.  Which category is it, and what
does pressing `left_bumper` alone do?

```
if (gamepad.left_bumper) { arm.setPosition(0.9); }
if (gamepad.right_bumper) { arm.setPosition(0.1); } else { arm.setPosition(0.5); }
```

Category 4.  The second block's `else` sets `0.5` straight after the first
sets `0.9`, so the left bumper does nothing you can see.

**How to catch it:**  when two `if` statements sit next to each other, ask
whether they both assign the same thing.  If they do, ask whether both are
allowed to run in the same loop.  Usually the answer is no, and they should
be one chain.

*See also:*  1.8, 2.8, Chapter 8

## 22.6  The SDK's silence isn't a happy path

**The tell:**  a value from the SDK or a vendor library, used on the next
line with no check.

Code you call can hand back nothing:  `null`, an empty list, or a number that
means "no reading."  An SDK method documents some of those, and some you find
out about the hard way.  Each one has to be checked where your code receives
it.

`LimelightExtensions.tryGetFiducial` is the example, and it shows both halves
of the rule.  The method hands back `null` on purpose when it can't find the
tag, and every caller checks for that (4.5).  Its own call into the SDK can
come back empty too.  The SDK's Limelight sample says `getLatestResult()`
returns `null` if `start()` was never called, and an earlier version of the
helper went straight from that call into `getFiducialResults()`.  Commit
`1a6809a` added the check:

```java
        LLResult result = limelight.getLatestResult();
        if (result == null)
            return null;
```

Without it, a missing result crashed the OpMode with a `NullPointerException`
(5.8).  And because the turret tracker calls this every loop, one empty result
would have stopped the whole robot, mid-match.

So there are two `null`s in this one method:  the one it hands back on purpose,
and the one the SDK can hand to it.  Each one needs its own check.

The same shape turns up with numbers.  A distance sensor with nothing in front
of it hands back a very large number, and code that trusts it drives into a
wall (4.4).  And it turns up with objects that look real:  the Limelight's
`getBotpose()` always hands back a pose, even with no tags in view, so a
`null` check passes every time (14.6).

**How to catch it:**  for every call into the SDK, ask what it hands back when
it has nothing to report.  Read the sample, the method's documentation, or
`doc/limelight3a/` for the camera.  Then check for that answer on the very next
line.

*See also:*  2.7, 4.4, 4.5, 5.8, 14.6

## Chapter summary

Five bug shapes cover a great deal of what goes wrong on this robot.  Each one
compiles, and each one reads fine inside the one method it lives in, because
the evidence is split between two places:  two paths, two `if` statements, two
classes, or your code and the code it calls.  Every one of them gets caught by
the same question:  *what else touches this?*

| Category | The tell | The question that confirms it |
|---|---|---|
| The loop must never stop (22.2) | a `while` or a sleep in code that runs every tick | does anything call this from inside the loop? |
| Same mechanism, two conventions (22.3) | a TeleOp strategy and an autonomous verb on the same hardware | do they agree on signs, positions, and setup? |
| One handle, unverified assumptions (22.4) | two `hardwareMap.get` calls for one name | which one sets the device up? |
| Independent guards (22.5) | two `if` statements setting the same thing | can both run in the same loop? |
| The SDK's silence (22.6) | an SDK result used unchecked | what does it hand back when it has nothing? |

### Java in this chapter

| Syntax | Where |
|---|---|
| `while` inside a method called every tick | 22.2 |
| two `if` statements against one `if` / `else if` / `else` chain | 22.5 |
| a `null` check on an SDK result | 22.6 |

### Vocabulary

**bug shape** -- a pattern of mistake that turns up again and again, in code
that's otherwise unrelated.  (22.1)

**the five categories** -- the five bug shapes in this chapter:  the stopped
loop, two conventions, one handle, independent guards, and the SDK's
silence.  (22.1)

**tell** -- the one detail that gives a bug shape away before you've traced
the whole bug.  (22.1)

**tick** -- one pass through the OpMode's loop.  Chapter 12 owns it.  (22.2)

### Check your understanding

1. Name the five categories, each with its tell.  (22.1)
2. Why does each of these bugs survive a careful read of the method it's in?
   (22.1)
3. A strategy lambda calls a method with `TrySleep(50)` inside a `while` loop.
   What does the driver see while it runs?  (22.2)
4. Where is waiting allowed, and how does `doAndWait` wait without stopping
   the loop?  (22.2)
5. A wrist's TeleOp strategy scores at `0.7`, and its autonomous verb scores
   at `0.3`.  Which category, and what's the fix?  (22.3)
6. Why does Billy's `turnToAngle()` read a correct heading, when
   `MecanumDrive` never initializes the IMU?  (22.4)
7. In the old intake code, what did pressing down on the d-pad do, and why
   did it work when up didn't?  (22.5)
8. Name the two `null`s in `tryGetFiducial`.  Which one did commit `1a6809a`
   add a check for?  (22.6)
9. Invent a bug for any one category, not from this codebase.  Then say what
   question you'd ask to confirm it.  (22.1)

## Where to go next

Chapter 12 explains the loop and how to wait inside it.  Chapter 13 explains
who owns a part when two pieces of code want it.  Chapter 21 has the design
checklist and the questions to ask about a new mechanism (21.7, 21.8).

