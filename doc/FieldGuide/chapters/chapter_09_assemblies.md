# Chapter 9 - Assemblies

*Several mechanisms, one coordinator, and a checklist only you can keep.*

---

Billy's shooter is four mechanisms working together:  an intake, a pusher, a
flywheel, and a turret.  Each one is a component, and Chapter 7 covers how a
component is built.  This chapter is about the class one layer up, the one
that holds all four and makes them act as one subsystem.  That class is an
**assembly**.

An assembly does three jobs.  It passes the gamepad down to every component.
It passes telemetry down to every component, and later collects what each one
reports.  And it gathers the components' autonomous verbs into one object that
autonomous code can use.

Look here when you're adding a component to an assembly, when a mechanism has
gone missing from the driver station, or when you want to know how autonomous
code reaches one component.

Read first:  Chapter 6, for the layers.  Chapter 7, for components, two-phase
construction, and the telemetry buffer.

| Entry | The question it answers |
|---|---|
| 9.1 | What is an assembly? |
| 9.2 | What does every assembly have to do? |
| 9.3 | How does the gamepad reach each component? |
| 9.4 | How does telemetry reach each component? |
| 9.5 | What is `AutonomousBillyMA` for? |
| 9.6 | What does `updateTelemetry` have to do? |
| 9.7 | A mechanism is missing from the driver station.  Where do you look? |
| 9.8 | What changes when an assembly gets a new component? |
| 9.9 | Can an assembly own a sensor? |

---

## 9.1  What is an assembly?

*Where it lives:*  `RobotModel/Mechs/Assemblies/BillyMA.java`

Start with Billy.  Its shooter has four jobs, and each job has its own
mechanism.  The intake pulls a ball in.  The pusher shoves the ball toward the
flywheel.  The flywheel spins up and throws it.  The turret aims the whole
thing.

Each mechanism is a component (Chapter 7):  a class that wraps one piece of
hardware, or one small group of it.  `BillyMA` owns all four.  Here are its
fields:

```java
private final SpinnyIntake intake;
private final PusherServo ballPusher;
private final DoubleShooter flywheel;
private final Turret turret;
```

`BillyMA`'s constructor builds each one with `new`, and hands each one the
control strategy that turns gamepad buttons into motor power (Chapter 8).

An **assembly** is a class that owns several components and coordinates them
as one subsystem.  Its work is passing things down to its components and
collecting what they report.  The hardware itself belongs to the components.
Every assembly extends `MechAssembly`.

The repository has three:

| Assembly | Its components | Used by |
|---|---|---|
| `BillyMA` | intake, pusher, flywheel, turret | `BillyRobot` |
| `CascadeArm` | a cascade lift, a drawbridge, a claw | `Wildbots2025` |
| `ExampleIntakeAssembly` | an intake, a claw | `OneStickTankRobot` |

`SpinnyIntake` and `Claw` each turn up in two different assemblies.  A
component is built to be reused, and the assembly is where it gets put to work
for one robot (Chapter 7).

The robot's loop talks to `BillyMA` as one thing.  It hands over the gamepad,
and it asks for telemetry.  `BillyMA` passes each request on to its four
components.  In design-pattern terms, an assembly is a **composite**:  one
object that stands for a group, so the code above it can treat the group as a
single thing.  The textbook **Composite pattern** gives the group and its
parts one shared type.  Here the assembly and its components share a shape
under different method names, and 9.2 lays the two side by side.

Below an assembly are its components.  Above it is the robot, which owns one
drive train and one assembly (Chapter 10).  A robot with no mechanisms, like
`StandardTankRobot`, leaves its assembly out.

*See also:*  9.2, 9.3, Chapter 6, Chapter 7

## 9.2  What does every assembly have to do?

*Where it lives:*  `RobotModel/Mechs/Assemblies/MechAssembly.java`

`MechAssembly` says what every assembly must be able to do.  Here's the class
with its comments and a few members left out:

```java
public abstract class MechAssembly
{
    protected interface IAssemblyStrategy { }
    ...
    public abstract <T extends AutonomousMechBehaviors> T getAutonomousBehaviors();
    ...
    public abstract void giveInstructions(Gamepad gamepad);

    protected Telemetry telemetry;
    ...
    public abstract void initializeTelemetry(Telemetry telemetry);
    ...
    public abstract void updateTelemetry();
}
```

Four methods are marked `abstract` (Chapter 1), so every assembly writes its
own body for each one, and each method is one job.  9.3 to 9.6 take them in
turn.  The `<T extends ...>` is a generic (Chapter 7), which lets `BillyMA`
promise its own autonomous type, `AutonomousBillyMA`.

The `telemetry` field is declared here, once, for every assembly.  A subclass
assigns it in `initializeTelemetry` and uses the inherited field from then on.

Put this contract beside the component contract from Chapter 7 and the match
is close:

| The job | `MechComponent` | `MechAssembly` |
|---|---|---|
| take the gamepad | `move(Gamepad)` | `giveInstructions(Gamepad)` |
| receive telemetry | `initializeTelemetry(Telemetry)` | `initializeTelemetry(Telemetry)` |
| write telemetry | `update()` | `updateTelemetry()` |
| offer autonomous verbs | `getAutonomousBehaviors()` | `getAutonomousBehaviors()` |

One name changes on purpose.  A component *moves*:  it turns gamepad input
into power for one mechanism.  An assembly *gives instructions*:  it routes
the gamepad to several mechanisms, and decides which of them hear it.

### Why every method is abstract

The base class has no idea how many components an assembly owns, what they're
called, or what order they go in.  `BillyMA` has four and
`ExampleIntakeAssembly` has two.  A default body would need that list, and
only the subclass has it.

Chapter 10 shows the opposite choice, one layer up.  `Robot` writes real
bodies for its `update` and `updateTelemetry`.  A robot has one drive train
and at most one assembly, so the base class does know that list.

The contract also declares `IMacroAssemblyStrategy<R>`, the shape of a timed
sequence an assembly can run, such as Billy's rapid fire.  Chapter 13 covers
it.

*See also:*  9.3, 9.4, 9.6, Chapter 7, Chapter 13

## 9.3  `giveInstructions`:  passing the gamepad down

*Where it lives:*  `RobotModel/Mechs/Assemblies/BillyMA.java`

Every loop, the robot hands the mechanism driver's gamepad to its assembly by
calling `giveInstructions` (Chapter 10).  The assembly passes that gamepad on
to each component, so each one can run its own control strategy.

`CascadeArm` shows the bare idea:

```java
@Override
public void giveInstructions(Gamepad gamepad) {
    cascade.move(gamepad);
    drawbridge.move(gamepad);
    claw.move(gamepad);
}
```

One line per component, every loop.  That's the whole job.

Billy's version does the same thing, with one addition.  It keeps the four
calls in a lambda at the end of its constructor:

```java
strategy = (mechAssembly, gamepad, claimed) -> {

    if (!claimed.contains(Part.INTAKE))
        intake.move(gamepad);
    if (!claimed.contains(Part.PUSHER))
        ballPusher.move(gamepad);
    if (!claimed.contains(Part.FLYWHEEL))
        flywheel.move(gamepad);
    if (!claimed.contains(Part.TURRET))
        turret.move(gamepad);

};
```

Read the four `move` calls first.  Each component gets the same gamepad, and
each component's strategy picks out its own buttons:  the intake reads the
dpad, the pusher reads `x`, the flywheel reads `y`, and the turret reads the
bumpers.

The `if` around each call is the addition.  `Part` is an enum in `BillyMA`
naming its four components, and `claimed` is the set of parts that other code
has taken over for the moment, such as the rapid-fire sequence or the
turret's auto-aim.  A claimed part skips its gamepad turn.  `BillyMA`'s
`giveInstructions` steps any running rapid-fire sequence, works out which
parts are claimed, and then runs this lambda.  Chapter 13 explains claims.
With nothing claimed, every loop visits all four.

### Where to read the controls

The assembly is where every strategy for a subsystem sits side by side, so
it's where to check how the buttons are shared out.  In `BillyMA`, dpad up
and down reach both the intake and the flywheel's power trim.

For the same reason, a rule that spans several components belongs here.
"Skip the turret while something else is aiming it" is a rule about the whole
shooter, and the assembly is the first layer that can see the whole shooter.
Chapter 8 has the general rule for which layer a rule belongs to.

*See also:*  9.2, 9.8, Chapter 8, Chapter 13

## 9.4  `initializeTelemetry`:  passing telemetry down

*Where it lives:*  `RobotModel/Mechs/Assemblies/BillyMA.java`

Every layer writes into one shared telemetry buffer, and one method sends it
to the screen (Chapter 7 has the buffer, Chapter 10 the sending).  Before a
component can write, it needs a reference to the telemetry object, and it gets
that reference in a second step, after it's built.  That's two-phase
construction (Chapter 7).  The assembly makes the second call on every
component it owns:

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

Read it in three steps.

1. **Keep a copy.**  `this.telemetry = telemetry;` fills the field from 9.2.
2. **Pass it to every child.**  One call per component.  Inside each call, the
   component keeps the reference and builds its own autonomous object.
3. **Build what depends on the children.**  Only now do all four autonomous
   objects exist, so only now can `BillyMA` gather them into
   `AutonomousBillyMA` (9.5).  And only then can it build `BillyRapidFire`,
   the rapid-fire sequence, which works through that object (Chapter 12).

Step 3 reads what step 2 made, so the order matters.

This is **passing telemetry down**:  each layer receives the telemetry object
from the layer above, keeps it, and hands it to every layer below before it
builds anything of its own.  The robot starts the relay from its constructor
with `initializeSubsystems()`, which calls `initializeTelemetry` on the drive
train and the assembly (Chapter 10).  `doc/TelemetryContract.md` draws the
whole relay as a tree.

> **Watch out.**  Leave one component out of `initializeTelemetry` and the
> code still compiles.  That component's `telemetry` field is `null` (Chapter
> 1), and its autonomous object was never built.  The first time it's asked to
> report, the OpMode stops with a `NullPointerException` (Chapter 5).  If a
> crash names a component's `update()` or one of its verbs, count the lines in
> its assembly's `initializeTelemetry` first.

Two-phase construction is a trade-off, and `doc/TelemetryContract.md` names
the cost:  for a moment every object exists half-built, and an assembly that
skips a child leaves that child half-built for good.  What it buys is one
obvious place, at every layer, to build the autonomous objects, at a moment
when telemetry is real.  The defense is the method's length.  It's short
enough to read and count.

*See also:*  9.5, 9.6, Chapter 7, Chapter 10

## 9.5  `AutonomousBillyMA`:  the aggregate autonomous object

*Where it lives:*  `RobotModel/Mechs/Assemblies/BillyMA.java`

Autonomous code has no gamepad.  It reaches a mechanism by calling one of that
component's autonomous verbs, like `startIntake()` or `pushBalls()` (Chapter
11).  Each component keeps its verbs in an inner class of its own (Chapter 7).
For autonomous code to reach all four of Billy's mechanisms, it needs those
four objects gathered in one place.  That's this class:

```java
public class AutonomousBillyMA extends AutonomousMechBehaviors {
    public final SpinnyIntake.AutonomousIntakeBehaviors autonIntake;
    public final PusherServo.AutonomousBallPusherBehaviors autonBallPusher;
    public final DoubleShooter.AutonomousShooterBehavior autonFlywheel;
    public final Turret.AutonomousTurretBehaviors autonTurret;

    public AutonomousBillyMA(
            SpinnyIntake.AutonomousIntakeBehaviors autonIntake,
            PusherServo.AutonomousBallPusherBehaviors autonBallPusher,
            DoubleShooter.AutonomousShooterBehavior autonFlywheel,
            Turret.AutonomousTurretBehaviors autonTurret,
            Telemetry telemetry) {
        super(telemetry);
        this.autonIntake = autonIntake;
        this.autonBallPusher = autonBallPusher;
        this.autonFlywheel = autonFlywheel;
        this.autonTurret = autonTurret;
    }
}
```

It has four fields, one per component, and each field's type is that
component's own autonomous class.  The constructor copies them in.  That's
all.  There's no hardware here and no new behavior;  its job is gathering four
existing objects under four names.

An object like this is an **aggregate autonomous object**:  an assembly's
autonomous surface, built out of its components' autonomous surfaces.  The
fields are `public final` (Chapter 1), so autonomous code can read each one
and can't swap it for another.  `BillyMA` builds it in `initializeTelemetry`
(9.4) and hands it out from `getAutonomousBehaviors()`.

It can also report.  Its parent class, `AutonomousMechBehaviors`, gives it
`reportStatus(...)` and `reportData(...)`, which write into the buffer and
leave the sending to the robot.

Two pieces of code use `AutonomousBillyMA` today.  `BillyRapidFire` is built
from it, writes its progress with `mechAssembly.reportStatus("fire")`, and
spins up the flywheel with `mechAssembly.autonFlywheel.setPower(0.6)`.  And
`BillyRobot` gathers it, with the drive train's, into one more aggregate a
layer up (Chapter 10), which is how an autonomous OpMode can write
`robot.getAutonomousRobot().mechAssembly.autonBallPusher.pushBalls()`
(Chapter 11).

This is the idea Chapter 6 calls *capabilities flow up*.  Each component
offers its verbs, the assembly gathers them, the robot gathers the
assembly's, and every layer adds one name to the path.  `CascadeArm` does the
same with three fields named `cascade`, `drawbridge`, and `claw`.

*See also:*  9.4, Chapter 6, Chapter 10, Chapter 11

## 9.6  `updateTelemetry` is a checklist

*Where it lives:*  `RobotModel/Mechs/Assemblies/BillyMA.java`

Once every loop, the robot asks its assembly to write its state into the
telemetry buffer (Chapter 10).  Here's what `BillyMA` does when it's asked:

```java
@Override
public void updateTelemetry() {
    telemetry.addLine("--- BillyMA ---");
    intake.update();
    ballPusher.update();
    flywheel.update();
    turret.update();
}
```

The first line writes a header, so the driver can find Billy's mechanisms on
the screen.  Then it calls `update()` on each component, one line each, and
each component writes its own lines into the buffer (Chapter 7).  All of these
calls write.  The robot sends the whole buffer once, at the end (Chapter 10).

`CascadeArm` has the same shape:  a header, then `claw.update()`,
`cascade.update()`, and `drawbridge.update()`.

An assembly's `updateTelemetry` is a **checklist**.  Its job is to visit every
component the assembly owns, every loop, so each one gets its turn to write.
The component decides *what* it reports.  The assembly makes sure it *gets
asked*.

> **Predict.**  Delete `turret.update();` from `BillyMA` and build.  What does
> the compiler say, and what does the driver see?

The compiler is satisfied, because the method is still legal Java.  The robot
runs.  The driver station shows the header, the intake, the pusher, and the
flywheel, and the turret's line is simply gone.  The screen gives no sign that
a line is missing.

That's why `MechAssembly` makes this method abstract (9.2).  The framework has
no way to count your components for you.  You know there are four, so you are
the check.

A visit calls the component's `update()`, and what that writes is up to the
component.  Most of the team's components take a telemetry strategy, a lambda
that picks the lines.  In `ExampleIntakeAssembly` the claw is visited every
loop, and its telemetry lambda is empty:

```java
(servo, telemetry) -> {});
```

So the claw is on the checklist and writes no lines.  A component that's
visited and still missing from the screen sends you to its telemetry strategy
next.

> **Try it.**  Run *BillyBLUE* on the robot and read the driver station.
> Below the drive train's lines (`yaw:` and four wheel powers) you'll find
> `--- BillyMA ---`, then `intake power:` and `intake position:`, then
> `pusher position`, then `power:` for the flywheel, then `turret position`.
> That last one shows `servo.getPower()`, because a continuous servo has a
> power and no position (Chapter 3).  Press dpad up on the second gamepad and
> watch `intake power:` change.

*See also:*  9.7, 9.8, Chapter 7, Chapter 10

## 9.7  A mechanism is missing from the driver station.  Where do you look?

A driver says they can't tell whether the intake is running.  The intake
works.  Its line just isn't on the screen.  Telemetry passes through four
places on the way there.  Check them in this order.

1. **The assembly's `updateTelemetry`.**  Count the `update()` calls against
   the component fields (9.6).  A missing call here compiles and runs
   silently, so start with it.
2. **The component's telemetry strategy.**  If the component is visited, what
   does it write?  An empty lambda writes nothing (9.6).
3. **The assembly's `initializeTelemetry`.**  A component that was never
   handed telemetry is louder than a missing line:  the OpMode crashes the
   first time it reports (9.4).
4. **The OpMode's loop.**  The buffer reaches the screen only when
   `robot.updateTelemetry()` runs (Chapter 10).  The team's TeleOp OpModes
   call it every loop, right after `robot.update(...)`.

Then read the labels.  A line can be present under a name you didn't expect:
Billy's flywheel reports as `power:`.

Billy's own method shows step 1 in an earlier version.  When this repository
began, `BillyMA.updateTelemetry` was a placeholder, left open as the reading
exercise for Flint Lesson 4.  Here it is, as `BillyMA.java` stood before
commit `1a6809a`:

```java
@Override
public void updateTelemetry() {
    // TODO: this method never visits any component, so none of the four
    // mechanisms (intake, ballPusher, flywheel, turret) ever report telemetry to
    // the driver station. Call each component's own telemetry-reporting behavior
    // here, the same way giveInstructions() above visits every component for
    // gamepad input (see Flint Lesson 4). CascadeArm.updateTelemetry() and
    // ExampleIntakeAssembly.updateTelemetry() both show the shape you want.
}
```

Every loop, this method ran and asked no component to write.  The robot drove
and the intake spun, and the screen showed nothing from Billy's four
mechanisms.  The compiler accepted it, because an empty method body is
perfectly legal.  Commit `1a6809a` filled it in with the five lines in 9.6:  a
header, then one visit per component.

*See also:*  9.4, 9.6, Chapter 10

## 9.8  What changes when an assembly gets a new component?

Say Billy grows a fifth mechanism, a gate that holds balls back until the
flywheel is ready.  Somebody writes the component (Chapter 7).  Then `BillyMA`
needs edits in several places, one for each job in this chapter.

| Where in `BillyMA` | What you add | Entry |
|---|---|---|
| the fields and the constructor | a `private final` field, and the line that builds it | 9.1 |
| the `strategy` lambda | a `move(gamepad)` call, wrapped in a claim check | 9.3 |
| `initializeTelemetry` | an `initializeTelemetry(telemetry)` call, before `AutonomousBillyMA` is built | 9.4 |
| `AutonomousBillyMA` | a field, a constructor parameter, and an argument in `initializeTelemetry` | 9.5 |
| `updateTelemetry` | an `update()` call | 9.6 |
| the `Part` enum | a constant, if other code should be able to claim the gate | Chapter 13 |

The compiler catches some of these.  A `final` field has to be given a value
before the constructor ends, so a gate you declare and forget to build stops
the build.  A new `AutonomousBillyMA` parameter stops the build until
`initializeTelemetry` passes something.

The other three are legal Java when they're missing.  A missing `move` leaves
the gate deaf to the gamepad.  A missing `initializeTelemetry` call crashes
the first time the gate reports (9.4).  A missing `update` leaves the gate off
the screen (9.6).  The last two come as a pair in two different methods, which
is exactly why one of the pair is easy to forget.

> **Try it.**  Open `BillyMA.java` and put your finger on each row of the
> table, for the turret.  You should find the turret in six places:  its
> field, its `new Turret(...)`, its `move` in the strategy, its
> `initializeTelemetry`, its `getAutonomousBehaviors()` in the
> `AutonomousBillyMA` constructor call, and its `update()`.  Then find
> `TURRET` in the `Part` enum.

> **Argue it.**  `MechAssembly` could keep a list of its components and loop
> over it in one `updateTelemetry` of its own.  That costs something.  The
> base class would need a way to hold components of different types, every
> assembly would lose its own header and order, and `giveInstructions`
> couldn't skip a claimed part by name.  What it buys is a checklist the base
> class keeps for you:  add a component to the list once, and every loop
> visits it.  Is that trade-off worth it?  What would you give up first?

*See also:*  9.3, 9.4, 9.6, Chapter 7, Chapter 13

## 9.9  Can an assembly own a sensor?

*Where it lives:*  `RobotModel/Mechs/Assemblies/MechAssembly.java`

Yes.  The comment at the top of `MechAssembly` gives the sorting rule:

```java
 *   <li>guides one mechanism, and protects it from itself -&gt; component</li>
 *   <li>reports on the handoff or interaction between two mechanisms -&gt; assembly</li>
```

(`-&gt;` is how the comment's HTML writes an arrow.)  A limit switch at the top
of a lift guides one mechanism, so it goes in the lift's component.  A sensor
that watches a ball pass from one mechanism to the next sees both of them, so
it goes in the assembly that owns both.  Chapter 4 has the full sensor
ownership rule, and Chapter 10 adds the robot's share.

None of the three assemblies owns a sensor yet, and the same comment names
Billy as the place to start:  a beam-break or color sensor between the intake
and the pusher, answering "is a ball actually seated and ready to fire?"  That
question spans the intake, the pusher, and the flywheel, so it belongs to none
of them alone.

The comment also lists what the first one will need, and each item is a job
from this chapter.  The assembly fetches the sensor in its constructor (9.1).
It writes the reading in `updateTelemetry` (9.6).  `AutonomousBillyMA` offers
what the reading *means*, as a question such as `isBallSeated()` (9.5,
Chapter 11).  And the `strategy` lambda is where the reading changes what the
components are told to do (9.3).

Today `BillyRapidFire` spins up the flywheel and waits a fixed 2200 ms before
it fires (Chapter 12).  A seated-ball sensor would give it a real signal.

> **Argue it.**  With a seated-ball sensor on Billy, should rapid fire wait
> for the sensor alone, or for the sensor or the 2200 ms timer, whichever
> comes first?  Waiting on the sensor alone stalls the sequence if the sensor
> fails, and it fires only when a ball is truly ready.  The timer is a guess,
> and a guess keeps working when a wire comes loose.

*See also:*  9.1, 9.5, Chapter 4, Chapter 10, Chapter 12

---

## Chapter summary

An assembly owns several components and makes them act as one subsystem.  It
passes the gamepad down every loop and telemetry down once at startup,
gathers its components' autonomous objects into one, and visits every
component every loop so each one can report.  `MechAssembly` makes each
assembly write all four jobs itself, because only the assembly knows its own
list of components.  Keeping that list complete, in every method, is your job.

| Idea | Where |
|---|---|
| an assembly owns components, and a robot owns one assembly | 9.1 |
| four abstract jobs:  the component contract, one layer up | 9.2 |
| `giveInstructions` hands the same gamepad to every component | 9.3 |
| `initializeTelemetry` relays telemetry down, then builds what depends on it | 9.4 |
| `AutonomousBillyMA` gathers four autonomous objects under four names | 9.5 |
| `updateTelemetry` visits every component, every loop | 9.6, 9.7 |
| a new component means edits the compiler mostly leaves to you | 9.8 |
| an assembly owns the sensors that watch a handoff | 9.9 |

### Java in this chapter

| Syntax | Where |
|---|---|
| `abstract` methods every subclass must write | 9.2.  `abstract` is in Chapter 1. |
| `<T extends AutonomousMechBehaviors> T getAutonomousBehaviors()` | 9.2.  Generics are in Chapter 7. |
| an inner class with `public final` fields | 9.5.  Inner classes are in Chapter 7. |
| a `final` field the constructor has to assign | 9.8 |

### Vocabulary

**aggregate autonomous object** -- an assembly's autonomous object, built from
its components' autonomous objects.  `AutonomousBillyMA` is one.  (9.5)

**assembly** -- a class that owns several components and coordinates them as
one subsystem.  Every assembly extends `MechAssembly`.  (9.1)

**checklist** -- what an assembly's `updateTelemetry` is:  one visit to every
component it owns, every loop.  (9.6)

**composite** -- one object that stands for a group, so the code above it can
treat the group as a single thing.  The **Composite pattern** is the design
built on it.  (9.1)

**`giveInstructions`** -- the assembly method that takes the mechanism
driver's gamepad and passes it to each component's `move`.  (9.3)

**passing telemetry down** -- each layer receiving the telemetry object from
the layer above, keeping it, and handing it to every layer below before it
builds anything of its own.  (9.4)

### Check your understanding

1. A component has `move(Gamepad)`.  An assembly has
   `giveInstructions(Gamepad)`.  What does the different name say about the
   different job?  (9.2)
2. Why does `MechAssembly` leave all four of its methods abstract, when
   `Robot` writes real bodies for two of its own?  (9.2)
3. In `BillyMA.initializeTelemetry`, why must the four component calls come
   before `new AutonomousBillyMA(...)`?  (9.4)
4. Describe `AutonomousBillyMA` in one sentence.  What new behavior does it
   add?  (9.5)
5. Explain the difference between a component's `update()` and an assembly's
   `updateTelemetry()`.  Use the word *visit*.  (9.6)
6. The intake is running and its line is missing from the screen.  List the
   places you'd look, in order.  (9.7)
7. Billy gets a fifth component.  Which of your edits will the compiler catch,
   and which will it let go by?  (9.8)
8. Where does a sensor go that reports whether a ball has moved from the
   intake to the pusher, and why there?  (9.9)

## Where to go next

The components an assembly owns are Chapter 7, and the control strategies it
hands them are Chapter 8.  One layer up, Chapter 10 is the robot, which owns
the assembly and sends the buffer it fills.  The autonomous verbs an assembly
gathers are Chapter 11.  Claims, `Part`, and the rapid-fire macro are Chapter
13, and the state machine inside rapid fire is Chapter 12.
