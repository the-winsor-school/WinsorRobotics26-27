# Chapter 7 - Components

*Is it just a device?  Then the wrapper already exists.*

---

A component is the bottom layer of the robot model:  one mechanism, the
hardware it's made of, and the code that drives it.  Every intake, pusher,
turret, and lift on the team's robots is a class that extends
`MechComponent`.

This chapter covers what every component has to do, how one gets built, and
how it reports what it's doing.  Then it takes on the question that matters
most before you write a new one:  does a component for this hardware already
exist?  Usually it does, and you build another one from the same class.

Look here when you're reading a component, writing one, or deciding whether
you need to.

Read first:  Chapter 6, for where components sit.  Chapter 3, for `DcMotor`,
`Servo`, `CRServo`, and `TouchSensor`.  Chapter 1, for classes, interfaces,
and `@Override`.

| Entry | The question it answers |
|---|---|
| 7.1 | What does every component have to do? |
| 7.2 | How is `SpinnyIntake` put together? |
| 7.3 | What's the class inside the class? |
| 7.4 | Why doesn't the constructor take telemetry? |
| 7.5 | How does a component report its state? |
| 7.6 | When should I reuse a component? |
| 7.7 | What are the two kinds of component? |
| 7.8 | When do two classes with one shape become two different things? |
| 7.9 | How do I add a device to the robot? |

---

## 7.1  What every component has to do

*Where it lives:*  `RobotModel/Mechs/Components/MechComponent.java`

A **component** is a class that owns one mechanism's hardware and drives it.
Every component extends `MechComponent`, the abstract base class (Chapter 1)
that says what a component has to provide.  That list is the **component
contract**, and here's the heart of it:

```java
    public abstract class AutonomousComponentBehaviors {
        protected final Telemetry telemetry;
        public AutonomousComponentBehaviors(Telemetry telemetry) {
            this.telemetry = telemetry;
        }
        /** Adds a status line to the telemetry buffer. Does NOT flush. */
        public void reportStatus(String status) { telemetry.addLine(status); }
        /** Adds a key-value pair to the telemetry buffer. Does NOT flush. */
        public void reportData(String key, Object value) { telemetry.addData(key, value); }
    }
    public abstract <T extends AutonomousComponentBehaviors> T getAutonomousBehaviors();
    protected interface IControlStrategy { }

    protected IControlStrategy strategy;
    protected Telemetry telemetry;

    protected MechComponent(IControlStrategy strategy)
    {
        this.strategy = strategy;
    }
```

```java
    public void initializeTelemetry(Telemetry telemetry)
    {
        this.telemetry = telemetry;
    }

    public abstract void move(Gamepad gamepad);
```

```java
    public abstract void update();
```

Four jobs come out of it.

| Job | Where the contract asks for it |
|---|---|
| own the hardware | the subclass's own fields, filled in its constructor (7.2) |
| take a control strategy | the constructor's `IControlStrategy` parameter (Chapter 8) |
| offer autonomous verbs | `getAutonomousBehaviors()`, and the `AutonomousComponentBehaviors` class (7.3) |
| report its own state | `update()` (7.5) |

And one door for TeleOp:  `move(Gamepad)`, which the assembly calls every
loop (6.4).  Each `abstract` method is one a subclass has to write.  The
compiler rejects a component that leaves one out.

`IControlStrategy` is an empty interface.  It works as a label, and each
component declares its own strategy interface that extends it, with the one
method that component needs.  Chapter 8 is about those.

A component does those jobs for its own mechanism, and it leaves everything
else to the layer above.  Code that reaches into another mechanism's
hardware, or decides what another component should do, belongs in the
assembly (Chapter 9).  8.7 has the rule for sorting it.

*See also:*  7.2, 6.1, Chapter 8, Chapter 9

## 7.2  How `SpinnyIntake` is put together

*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java`

`SpinnyIntake` is the simplest component on Billy, so it's the one to read
first.  It runs one motor.  Here are its fields:

```java
    private final DcMotor intake;

    protected SpinnyIntakeControlStrategy strategy;

    private AutonomousIntakeBehaviors auton;
```

`intake` is the hardware:  one `DcMotor`, `private` and `final` (Chapter 1),
so only this class can touch it and it's set exactly once.  `strategy` is the
button mapping it gets handed.  `auton` is its autonomous twin, which gets
built later (7.4).

And its constructor:

```java
    public SpinnyIntake(
            HardwareMap hardwareMap,
            String motorName,
            SpinnyIntakeControlStrategy strategy)
    {
        super(strategy);
        intake = hardwareMap.get(DcMotor.class, motorName);
        this.strategy = (SpinnyIntakeControlStrategy) super.strategy;
    }
```

It takes three things:  the hardware map, the motor's configuration name, and
a strategy.

- `super(strategy);` hands the strategy to `MechComponent`'s constructor,
  which keeps it in the base class's own `strategy` field.
- `intake = hardwareMap.get(DcMotor.class, motorName);` fetches the motor
  (Chapter 3).  The name comes from whoever builds the intake, so the class
  works for any motor, under any name.
- The last line copies the strategy into `SpinnyIntake`'s own field, typed as
  a `SpinnyIntakeControlStrategy`, so `move` can call its method.

> **Aside:  cast**
>
> `super.strategy` is declared as an `IControlStrategy`, and that interface
> has no methods at all.  So Java won't let you call `nomNomNom` through it.
>
> ```java
> (SpinnyIntakeControlStrategy) super.strategy
> ```
>
> A type in parentheses in front of a value is a **cast**.  It tells Java to
> treat the value as that type.  Java checks when the line runs, and if the
> object weren't really a `SpinnyIntakeControlStrategy`, the program would
> stop with a `ClassCastException`.  Here it always is one, because the
> constructor's parameter accepts only that type.  `Claw` and `Turret` do the
> same job with `this.strategy = strategy;`, copying the parameter straight
> across, which needs no cast.

`SpinnyIntakeControlStrategy`, declared a few lines up in the same file, has
one method:  `nomNomNom(DcMotor motor, Gamepad gamepad)`.  Chapter 8 reads it
closely.

Everything about Billy reaches this class through the constructor:  the
motor's name and the button mapping.  The class itself is about a motor that
spins an intake, on any robot.  That's what lets a second robot build one
from the same class (7.6).

*See also:*  7.1, 7.3, 7.6, Chapter 8

## 7.3  The class inside the class

*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java`

The first thing inside `SpinnyIntake` is another class.  It's the intake's
autonomous twin (6.5), and it has the intake's three verbs on it:

```java
    public class AutonomousIntakeBehaviors extends MechComponent.AutonomousComponentBehaviors
    {
        public AutonomousIntakeBehaviors(Telemetry telemetry) {
            super(telemetry);
        }

        public void startIntake()
        {
            intake.setPower(1);
            reportStatus("Intake: running");
        }

        public void stopIntake()
        {
            intake.setPower(0);
            reportStatus("Intake: stopped");
        }

        public void reverseIntake()
        {
            intake.setPower(-1);
            reportStatus("Intake: reversed");
        }
```

Look at `startIntake`.  It calls `intake.setPower(1)`, and `intake` is a
`private` field of `SpinnyIntake`, the class around it.  It can do that
because it's an inner class.

> **Aside:  inner class**
>
> A class written inside another class, without the word `static`, is an
> **inner class**.  Every object of the inner class belongs to one object of
> the outer class, and it can use that object's fields and methods, `private`
> ones included.  So each `AutonomousIntakeBehaviors` belongs to one
> `SpinnyIntake`, and `intake` inside it means that `SpinnyIntake`'s motor.
> The outer object builds it, with an ordinary `new`, from inside one of its
> own methods (7.4).

That's how the twin can drive the motor while the motor field is `private`.
Autonomous code gets the twin, which has three verbs, and the motor is out of
its reach.  The twin also gets `reportStatus` from
`MechComponent.AutonomousComponentBehaviors`, so every verb writes a line to
the Driver Station as it runs (7.5).

The contract asks for the twin with a method that looks a little strange:

```java
    public abstract <T extends AutonomousComponentBehaviors> T getAutonomousBehaviors();
```

And here's `SpinnyIntake`'s version of it:

```java
    @Override
    public AutonomousIntakeBehaviors getAutonomousBehaviors() {
        return auton;
    }
```

> **Aside:  generics**
>
> `<T extends AutonomousComponentBehaviors>` makes the method **generic**.
> `T` stands for a type, picked later, and `extends AutonomousComponentBehaviors`
> limits which types it can be:  `AutonomousComponentBehaviors` or a class
> built on it.  The method hands back a `T`.
>
> The concrete override names the type this component hands back.
> `SpinnyIntake`'s version returns an `AutonomousIntakeBehaviors`, so code
> that asks a `SpinnyIntake` for its twin gets one with `startIntake()` on it.
> With a plain `AutonomousComponentBehaviors` return type, the caller would
> see only `reportStatus` and `reportData`.  Angle brackets around a type are
> how Java writes generics everywhere:  `Set<Part>` is a set of `Part`s.

That exact return type is what makes the dot chain in 6.5 work.
`BillyMA.AutonomousBillyMA` has a field typed `SpinnyIntake.AutonomousIntakeBehaviors`,
filled straight from `intake.getAutonomousBehaviors()`, and Chapter 11 follows
the verbs from there.

*See also:*  6.5, 7.4, Chapter 11

## 7.4  Two-phase construction

*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java`

A component's constructor takes hardware and a strategy, and stops there.
Telemetry, the object that writes to the Driver Station screen, arrives
later, through a second method:

```java
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousIntakeBehaviors(telemetry);
    }
```

So a component gets built in two steps.  First the constructor, with the
hardware.  Then `initializeTelemetry`, which keeps the telemetry object
(`super.initializeTelemetry` stores it in `MechComponent`'s field) and builds
the autonomous twin.  That's **two-phase construction**.

The layer above runs the second step.  Here's `BillyMA` doing it for all four
of its components:

```java
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
        intake.initializeTelemetry(telemetry);
        ballPusher.initializeTelemetry(telemetry);
        flywheel.initializeTelemetry(telemetry);
        turret.initializeTelemetry(telemetry);
        auton = new AutonomousBillyMA(
                intake.getAutonomousBehaviors(),
                ballPusher.getAutonomousBehaviors(),
                flywheel.getAutonomousBehaviors(),
                turret.getAutonomousBehaviors(),
                telemetry);
        BRF = new BillyRapidFire(auton, 3);
    }
```

It hands telemetry to each component, and then, once every component has
built its twin, it gathers the four twins into its own (6.5).  The last line
builds Billy's rapid-fire sequence from that twin (Chapter 12).  `BillyMA` gets
the same treatment from `Robot.initializeSubsystems()`, which `BillyRobot`'s
constructor calls.  So telemetry is handed down the layers, and the twins are
gathered back up them, in one pass.  `doc/TelemetryContract.md` draws the
whole chain.

It's a trade-off, and the cost comes first.  Between the two steps, a
component exists half-built:  it has its motor, and its `telemetry` and
`auton` fields are still `null` (Chapter 1).

> **Predict.**  Suppose `BillyMA`'s `initializeTelemetry` left out the line
> `turret.initializeTelemetry(telemetry);`.  Does it compile?  What happens
> when the robot runs?

It compiles, and the robot boots.  The turret's `telemetry` is still `null`,
so it never built its twin, and `AutonomousBillyMA` gets `null` for
`autonTurret`.  The first time anything asks the turret to report, or asks
for its twin's verbs, the program stops with a `NullPointerException`
(Chapter 5).  `initializeTelemetry` is short on purpose, so you can count its
lines against the list of components.

Here's what the two steps buy.  The twin keeps its own reference to the
telemetry object, so its verbs can report as they run.  That means the twin
can only be built once telemetry exists, and `initializeTelemetry` is the one
place where it's guaranteed to.  The constructor deals in hardware, and the
method that builds the twin is the method that receives what the twin needs.

*See also:*  6.5, 7.3, 7.5, Chapter 9, Chapter 10

## 7.5  `update()` and the telemetry buffer

*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java`

Every loop, each component gets a chance to say how it's doing.  Here's what
the intake says:

```java
    @Override
    public void update() {
        telemetry.addData("intake power:", intake.getPower());
        telemetry.addData("intake position:", intake.getCurrentPosition());
    }
```

Two lines go onto the Driver Station screen:  the power the motor was last
given, and its encoder count.

FTC's telemetry object works like a notepad with a send button.
`telemetry.addData(...)` and `telemetry.addLine(...)` write onto the notepad.
`telemetry.update()` sends the whole page to the Driver Station and starts a
fresh one.  The notepad is the **telemetry buffer**.  Writing to it leaves the
screen as it was.  Sending it puts the new page in front of the drivers.

A component writes, and leaves the sending to `Robot`.  `Robot.updateTelemetry()`
asks every layer to write, and then calls `telemetry.update()` once, at the
end of the loop (Chapter 10).  So one screen holds one loop's worth of lines,
from every mechanism, in the same order every time.  A component that sent
the page itself would send half a screen and clear it, and the rest of that
loop's lines would show up on the next screen, a loop late.

> **Watch out.**  Two methods here are both called `update`, and they do
> opposite jobs.  A component's `update()` writes its lines into the buffer.
> `telemetry.update()` sends the buffer.  Inside a component, write
> `telemetry.addData(...)`, and leave `telemetry.update()` to `Robot`.

The autonomous verbs write to the same buffer.  `startIntake()` calls
`reportStatus("Intake: running")`, which is `telemetry.addLine` underneath
(7.1), so an autonomous program's progress shows up on the same screen.

Some components let the assembly decide what they report, with a second
strategy.  `Claw` takes a telemetry strategy in its constructor, and its
`update()` uses it when there is one:

```java
    @Override
    public void update()
    {
        if (telemetryStrategy != null) {
            telemetryStrategy.update(servo, telemetry);
        } else {
            telemetry.addData("Claw power:", servo.getPower());
        }
    }
```

When the assembly passes a telemetry strategy, the claw reports whatever that
lambda writes.  When it passes `null`, the claw writes its own power.
`Turret`, `PusherServo`, `DoubleShooter`, and `DoublyLimitedMotor` work the
same way, and Chapter 8 reads Billy's telemetry lambdas.

> **Try it.**  Run `BillyBLUE` and hold dpad up on `gamepad2`.  The screen
> shows `intake power:` at `0.75`.  `intake position:` climbs while you hold
> it, if the intake motor's encoder is plugged in.  Let go, and the power
> reads `0`.

*See also:*  7.4, Chapter 8, Chapter 9, Chapter 10

## 7.6  Reuse first

*Where it lives:*  `RobotModel/Mechs/Assemblies/ExampleIntakeAssembly.java`

A component is written to be reused.  For `SpinnyIntake`, the caller
supplies the hardware's configuration name and a strategy saying how this
robot drives it, along with the hardware map.  Those arguments let the
caller choose a device and its controls without copying the wrapper.

The repository already does this.  `BillyMA` builds a `SpinnyIntake` on the
motor named `"intakeMotor"`, run from the dpad (6.4).  `ExampleIntakeAssembly`
builds one on a different motor, run from different buttons:

```java
        intake = new SpinnyIntake(hardwareMap, "spinner",
                (motor, gamepad) -> {
                       if(gamepad.a)
                           motor.setPower(1);
                       else if(gamepad.b)
                           motor.setPower(-1);
                       else
                           motor.setPower(0);
                });
```

Two assemblies, two motors, two button mappings, and one class, used exactly
as it was written.  The same thing happens in other places.

| Class | Built in | Configuration names |
|---|---|---|
| `SpinnyIntake` | `BillyMA`, `ExampleIntakeAssembly` | `"intakeMotor"`, `"spinner"` |
| `Claw` | `CascadeArm`, `ExampleIntakeAssembly` | `"servo"`, `"claw"` |
| `DoublyLimitedMotor` | `CascadeArm`, twice | `"cascadeMotor"` with two switches, `"drawbridge"` with two more |

So before you write a component, look for one that already owns the same
hardware.  That's **reuse first**.  A new class is a second copy of
everything it shares with the old one:  a second constructor, a second
`initializeTelemetry`, a second set of verbs.  When one of them improves, the
other has to be improved by hand.

Here are three new mechanisms, sorted that way.

| New mechanism | Hardware | Reuse |
|---|---|---|
| a linear slide that has to stop at the top and the bottom | one `DcMotor` and two `TouchSensor`s | `DoublyLimitedMotor`, limits included.  Pass three names and a lambda. |
| a second intake on the same robot | one `DcMotor` | another `SpinnyIntake`, with its own name and lambda |
| a gate that flips open and shut | one `Servo` | `PusherServo`, which owns one positional servo |

The gate is the interesting row.  "But it's called *Pusher*" is a fair
objection, and it points at something real:  the class is named for the job
it does on Billy.  Renaming it is a reasonable proposal.  Copying it into a
`GateServo` gives the team two classes to keep in step.  7.7 says how the
team thinks about names.

*See also:*  6.7, 7.7, 7.9, Chapter 8

## 7.7  The two kinds of component

*Where it lives:*  `RobotModel/Mechs/Components/MechComponent.java`

The team wrote its rule for components at the top of `MechComponent.java`:

```java
 * <p><b>What belongs in a component.</b> Every concrete MechComponent should be
 * exactly one of two things:
 *
 * <ol>
 *   <li><b>A wrapper around a single output device</b> - one motor, one servo,
 *       one CR servo. The class adds the strategy seam, the autonomous verbs,
 *       and telemetry, and nothing else. We need exactly <em>one</em> of these
 *       per device type, and it should be named for the <em>device</em>, since
 *       any mechanism built on that device reuses it as-is.</li>
 *   <li><b>A purposeful collection of output devices plus the sensors that
 *       guide them</b> - a motor bounded by two limit switches, two motors
 *       driven as an opposed pair, a camera and the processors reading it.
 *       These are named for what the collection <em>does</em>, because the
 *       coupling between the outputs and the sensors is the whole point.</li>
 * </ol>
 *
 * <p>A class that is neither - a second single-device wrapper named after one
 * robot's job for it - is duplication, and every fix to one is a fix owed to
 * the other.
```

So every component should be one of **the two kinds of component**.

The first kind wraps a single output device:  one motor, one servo, or one
continuous-rotation servo.  It adds the strategy, the verbs, and the
telemetry, and that's all.  The team needs one of these for each type of
device, named for the device, because any mechanism built on that device can
use it.

The second kind is a set of devices that belong together:  outputs, and the
sensors that guide them.  It's named for what the set does, because the way
the parts work together is the reason the class exists.

Here are the six components in `Components/`, sorted by the rule.

| Component | Kind | Owns |
|---|---|---|
| `SpinnyIntake` | 1, one device | one `DcMotor` |
| `PusherServo` | 1, one device | one `Servo` |
| `Claw` | 1, one device | one `CRServo` |
| `Turret` | 1, one device | one `CRServo` |
| `DoubleShooter` | 2, a set | two `DcMotor`s, driven as an opposed pair |
| `DoublyLimitedMotor` | 2, a set | one `DcMotor` and two `TouchSensor`s |

`DoublyLimitedMotor` is the clearest second kind.  Its two switches exist to
stop its one motor, and its `setPower` refuses to drive into a switch that's
pressed (8.6).  `DoubleShooter` is the other.  Its verbs always give the two
flywheel motors equal and opposite power, the way this flywheel needs them
driven.

> **Predict.**  Four of those six are the first kind.  How many types of
> device do those four wrap?

Three:  `DcMotor`, `Servo`, and `CRServo`.  `Claw` and `Turret` both wrap one
`CRServo`.  By the rule, one class named for the `CRServo` could serve both,
and 7.8 takes up what that means for those two.

The rule also says the first kind should be named for the device.  All four
first-kind classes are named for a job one robot gave them:  spinning an
intake, pushing balls, gripping, turning.  A `TODO` just below the rule says
the naming convention for the first kind is still an open question, and asks
the team to settle it before renaming anything, since every assembly that
builds a component moves with its name.

> **Argue it.**  Name a first-kind component for the device or for the job?
> `PusherServo` tells a reader on Billy exactly what it's for.  A device name
> tells the next robot it can use the class too.  Pick a convention, and say
> what it would rename.

*See also:*  7.6, 7.8, 8.6, Chapter 4

## 7.8  When two classes with one shape become two different things

*Where it lives:*  `RobotModel/Mechs/Components/Claw.java`, `Turret.java`

`Claw` and `Turret` are built the same way.  Each wraps one `CRServo`.  Each
has a constructor that takes a name, a control strategy, and a telemetry
strategy, the same two-step setup (7.4), and the same `update()` (7.5).  Even
their strategy interfaces match.  Here's the claw's:

```java
    public interface ClawControlStrategy extends IControlStrategy
    {
        public void chomp(CRServo servo, Gamepad gamepad);
    }
```

And the turret's:

```java
    public interface TurretControlStrategy extends IControlStrategy
    {
        public void move(CRServo servo, Gamepad gamepad);
    }
```

What differs is the vocabulary.  The claw's verbs are `open()`, `close()`,
and `stop()`.  The turret's are `turnCCW()`, `turnCW()`, `setPower(...)`, and
`stop()`.  Underneath, each verb sets the servo's power.

So today they're two first-kind components on one device, and the rule in
7.7 says one class, named for the `CRServo`, could serve both.  One class
would mean one constructor and one `update()` to keep right, where now there
are two of each.

Then the mechanisms ask for more, and they ask for different things.  The
turret needs to know where its travel ends.  The `TODO` in `Turret.java`
opens with it:

```java
    // TODO: the turret needs a pair of travel limits. Right now it will happily keep
    //  rotating in one direction for as long as something commands it to, which wraps the
    //  wiring and eventually damages the mechanism. Add two TouchSensors at the ends of
    //  turret travel and enforce them here, the way DoublyLimitedMotor does for a motor:
    //  take both sensor names in the constructor, add canGoCW()/canGoCCW(), and put a
    //  decorated `public void setPower(double)` on Turret that zeroes any power heading
    //  into a limit that is already pressed.
```

The claw needs something else.  A `CRServo` reports no position, so the claw
can't tell whether it's holding anything.  The `TODO` in `Claw.java` names
the sensor that would tell it:

```java
    //    - A grip sensor. One TouchSensor on the jaw, or a DistanceSensor or color
    //      sensor looking into the grip, answering "is there a game piece in there?"
    //      That turns a blind close() into something autonomous can actually branch on,
    //      and gives isHolding() as an autonomous verb worth having.
```

Once the turret has its two limit switches and the claw has its grip sensor,
each one owns hardware the other has no use for, and enforces a rule the
other doesn't need.  They're both the second kind now, a set of devices
named for what it does, and two classes are earned.

The two `TODO`s and the note under the rule in `MechComponent.java` lay out
the plan in two moves.

1. Make one `CRServo` component, named for the device, that both mechanisms
   can use.
2. Build the limited turret and the sensing claw on top of it, each named for
   what it does.

Both `TODO`s also say what has to come before the sensor:  every way of
commanding the servo has to pass through one method on the class, so that
method can check the sensor.  `DoublyLimitedMotor` already works that way,
and 8.6 shows how.

That's the design idea to take from these two.  The hardware-shape test
from 7.7 predicted the answer before anybody wired a sensor.  Two classes
with the same shape today are often two mechanisms whose shapes are still
being finished, and the sensors each one needs are what tell them apart.

The same sorting works for sensors.  A sensor that guides one mechanism
belongs in that component, like the turret's limit switches.  A sensor that
watches the hand-off between two mechanisms belongs in the assembly, and
`MechAssembly.java` describes one for Billy:  a sensor between the intake and
the pusher that sees whether a ball is ready to fire.  A sensor that
describes the whole robot belongs to the robot.  Chapter 4 works through the
rule.

*See also:*  7.7, 8.6, Chapter 4, Chapter 9

## 7.9  Adding a device to the robot

Once you've decided what the device is for, here's the order.

1. Add it to the robot's configuration on the Driver Station, and write the
   name down exactly (Chapter 3).
2. Look for a component that already owns that hardware shape (7.6, 7.7).
   If one exists, build another one from it, with your name and your lambda,
   and skip to step 6.
3. If it's new, give it a control-strategy interface, so the button mapping
   arrives from outside (Chapter 8).
4. Give it verbs that say what you want:  `openClaw()`.  A number like
   `setPosition(0.15)` belongs inside the verb, where one class keeps it.
5. Write its state into the buffer in `update()` (7.5).
6. Wire it into an assembly in four places:  build it in the constructor,
   hand it telemetry in `initializeTelemetry`, call its `move` in
   `giveInstructions`, and call its `update` in `updateTelemetry`.
   Chapter 9 walks through all four.

`doc/NewRobotDesignWorkflow.md` has templates for a new component and a new
assembly.

> **Watch out.**  Step 6 has four places, and the last one is the easiest to
> leave out.  The code compiles, the mechanism moves, and its lines are
> missing from the Driver Station.  When you add a component, count its name
> in `updateTelemetry` before you test.

*See also:*  7.6, Chapter 9, Chapter 21

---

## Chapter summary

A component owns one mechanism's hardware, takes its button mapping from
outside, offers autonomous verbs through a twin, and writes its own state into
the telemetry buffer.  It's built in two steps, hardware first and telemetry
second, so the twin can be built with a real telemetry object.  Most of the
time the component you need already exists, and you build another one with a
new name and a new lambda.  A new class earns its place when it's a new
device type, or a set of devices and the sensors that guide them.

| In the code | It's an example of |
|---|---|
| `MechComponent`'s abstract methods | the **component contract** |
| `SpinnyIntake`'s constructor, then `initializeTelemetry` | **two-phase construction** |
| `AutonomousIntakeBehaviors` inside `SpinnyIntake` | an **inner class** |
| `<T extends AutonomousComponentBehaviors>` | **generics** |
| `(SpinnyIntakeControlStrategy) super.strategy` | a **cast** |
| `addData` in `update()`, `telemetry.update()` in `Robot` | writing to the **telemetry buffer**, and sending it |
| `SpinnyIntake` in two assemblies | **reuse first** |
| `SpinnyIntake` beside `DoublyLimitedMotor` | **the two kinds of component** |

### Java in this chapter

| Syntax | Where |
|---|---|
| a cast:  `(Type) value` | 7.2 |
| an inner class | 7.3 |
| a generic method:  `<T extends ...> T` | 7.3 |

### Vocabulary

**cast** -- a type in parentheses in front of a value, telling Java to treat
the value as that type.  Java checks it when the line runs.  (7.2)

**component** -- a class that owns one mechanism's hardware and drives it.
Every one extends `MechComponent`.  (7.1)

**component contract** -- what `MechComponent` requires of every component:
take a strategy, `move(Gamepad)`, `update()`, and `getAutonomousBehaviors()`.
(7.1)

**generics** -- writing a class or method with a type left as a placeholder,
such as `T`, to be filled in later.  `<T extends X>` limits the placeholder
to `X` and the classes built on it.  (7.3)

**inner class** -- a class written inside another class without `static`.
Each of its objects belongs to one object of the outer class and can use its
private fields.  (7.3)

**reuse first** -- before writing a component, look for one that already owns
the same hardware, and build another one from it.  (7.6)

**telemetry buffer** -- the page of lines waiting to go to the Driver Station.
`addData` and `addLine` write to it, and `telemetry.update()` sends it.  (7.5)

**the two kinds of component** -- a wrapper around one output device, named
for the device;  or a set of devices and the sensors that guide them, named
for what it does.  (7.7)

**two-phase construction** -- building an object in two steps:  the
constructor with the hardware, then `initializeTelemetry` with telemetry and
the autonomous twin.  (7.4)

### Check your understanding

1. Name the four jobs every component does, and the method or parameter in
   `MechComponent` that asks for each.  (7.1)
2. `startIntake()` sets the power of a `private` field in another class.  How
   is it allowed to?  (7.3)
3. Why does the autonomous twin get built in `initializeTelemetry` and not in
   the constructor?  What can go wrong between the two steps?  (7.4)
4. A component calls `telemetry.update()` at the end of its `update()`.  What
   does the driver see?  (7.5)
5. A new robot needs a gate that flips open and shut on one servo.  Which
   class do you build, and what do you hand it?  (7.6)
6. `Claw` and `Turret` have the same shape today.  What would make them two
   different classes, and what's the first move toward that?  (7.8)

## Where to go next

Chapter 8 is the strategy every component is handed:  where the lambdas come
from, and which rules belong inside the component itself.  Chapter 9 is the
assembly that builds components and coordinates them.  Chapter 11 follows the
verbs from the twin up to an autonomous program, and Chapter 4 sorts sensors
into layers.
