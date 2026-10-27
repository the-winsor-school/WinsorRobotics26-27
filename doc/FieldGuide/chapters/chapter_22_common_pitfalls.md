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

