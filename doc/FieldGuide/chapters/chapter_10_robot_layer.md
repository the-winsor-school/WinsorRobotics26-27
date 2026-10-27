# Chapter 10 - The Robot Layer

*The top of the chain:  one loop, one flush, and the sensors everybody shares.*

---

`Robot` is the top of the robot model.  It owns one drive train and one
assembly, and the OpMode talks to it with two calls every loop:  `update`,
which passes the gamepads down, and `updateTelemetry`, which sends the
driver station its screen.

`Robot` supplies default methods for dispatching controls and collecting
telemetry.  Its `update` and `updateTelemetry` have working bodies, so a new
robot can inherit those steps after connecting its subsystems.  Most of this chapter is about that
choice:  what the default loop does, how a robot adds to it, and why Billy
writes its own.

Look here when you want to know what `robot.update(...)` does, where the
telemetry gets sent, why `BillyRobot` looks different from `Wildbots2025`, or
where a sensor like the IMU belongs.

Read first:  Chapter 6, for the layers.  Chapter 9, for the assembly the
robot owns.

| Entry | The question it answers |
|---|---|
| 10.1 | What does a `Robot` own? |
| 10.2 | Which parts of `Robot` are written, and which are left to you? |
| 10.3 | What does `Robot.update` do? |
| 10.4 | How does a robot add to the default? |
| 10.5 | Why does Billy write its own `update`? |
| 10.6 | Where does telemetry get sent? |
| 10.7 | What does the OpMode see? |
| 10.8 | Where do robot-wide sensors live? |

---

## 10.1  What does a `Robot` own?

*Where it lives:*  `RobotModel/Robots/Robot.java`

Every robot in the code extends `Robot`, and `Robot` gives each one three
fields and a constructor:

```java
protected DriveTrain driveTrain;
protected MechAssembly mechAssembly;
protected Telemetry telemetry;

protected Robot(Telemetry telemetry)
{
    this.telemetry = telemetry;
}
```

A drive train moves the chassis.  An assembly runs the mechanisms (Chapter 9).
The telemetry object is the one the OpMode was given, and `Robot` is the only
layer that receives it in its constructor.  Everything below gets it later,
passed down (9.4).

`Wildbots2025` is the plainest real robot.  Here's its whole constructor:

```java
public Wildbots2025(HardwareMap hardwareMap, Telemetry telemetry)
{
    super(telemetry);
    driveTrain = new MecanumDrive(
            hardwareMap,
            new MecanumDrive.OrientationConfiguration(
                    DcMotorSimple.Direction.REVERSE,
                    DcMotorSimple.Direction.FORWARD,
                    DcMotorSimple.Direction.FORWARD,
                    DcMotorSimple.Direction.REVERSE
                )
            );
    mechAssembly = new CascadeArm(hardwareMap);

    initializeSubsystems();

    auton = new AutonomousMecanumRobot(
            driveTrain.getAutonomousDriving(),
            mechAssembly.getAutonomousBehaviors(),
            telemetry);
}
```

It does four things, in order.  It hands telemetry to `Robot` with
`super(...)` (Chapter 1).  It builds a drive train and an assembly.  It calls
`initializeSubsystems()`, which passes telemetry down to both (10.2).  Then it
builds its autonomous object from theirs (10.2, Chapter 11).

The repository has four robots:

| Robot | Drive train | Assembly |
|---|---|---|
| `BillyRobot` | `MecanumDrive` | `BillyMA` |
| `Wildbots2025` | `MecanumDrive` | `CascadeArm` |
| `StandardTankRobot` | `StandardTankDrive` | none |
| `OneStickTankRobot` | `OneStickTank` | `ExampleIntakeAssembly` |

Billy and `Wildbots2025` share a drive train class and differ in their
assemblies.  A robot is mostly a choice of parts.  That's composition, the
idea Chapter 6 builds the whole model on.

And here's how an OpMode uses a robot, from *Example TeleOp (Wildbots2025)*:

```java
robot = new Wildbots2025(hardwareMap, telemetry);
telemetry.addLine("initialized");
telemetry.update();
waitForStart();

// run until the end of the match (driver presses STOP)
while (opModeIsActive()) {
    robot.update(gamepad1, gamepad2);
    robot.updateTelemetry();
}
```

Build the robot, wait for the start, then call two methods every loop until
the match ends.  10.3 and 10.6 take those two methods apart.

*See also:*  10.2, 10.7, Chapter 6, Chapter 9

## 10.2  Which parts of `Robot` are written, and which are left to you?

*Where it lives:*  `RobotModel/Robots/Robot.java`

Chapter 9 showed `MechAssembly` leaving every method abstract, because it
can't know how many components an assembly has.  `Robot` makes the opposite
choice for three of its methods, because it does know what a robot has:  one
drive train, and at most one assembly.

Here's the first of the three:

```java
protected void initializeSubsystems()
{
    if (driveTrain != null) driveTrain.initializeTelemetry(telemetry);
    if (mechAssembly != null) mechAssembly.initializeTelemetry(telemetry);
}
```

It starts the relay that passes telemetry down (9.4).  Every robot's
constructor calls it once, after building its parts.  The other two written
methods are `update` (10.3) and `updateTelemetry` (10.6).

One method is left abstract:

```java
public abstract <T extends AutonomousRobot> T getAutonomousRobot();
```

That's the robot's autonomous object, the one autonomous code starts from
(Chapter 11).  The base declaration uses a generic type parameter bounded
by `AutonomousRobot` (7.3).  The concrete overrides name the autonomous
type each robot returns.  `Wildbots2025` hands back a
`Wildbots2025.AutonomousMecanumRobot`, with fields for a `MecanumDrive`'s
verbs and a `CascadeArm`'s.  Billy hands back a `BillyRobot`
version, with Billy's own fields.

The split follows one rule.  What's the same for every robot is written in
the base class.  What differs from robot to robot is left to the subclass.

| Method | Same for every robot? | So |
|---|---|---|
| `initializeSubsystems` | yes:  pass telemetry to the drive train, then the assembly | written in `Robot` |
| `update` | yes, by default:  driver to the drive train, mechanism driver to the assembly | written in `Robot` |
| `updateTelemetry` | yes:  gather from both, then send once | written in `Robot` |
| `getAutonomousRobot` | no:  each robot is built from different parts, with different verbs | abstract |

The two tank robots show the left-open method doing its job.
`StandardTankRobot` and `OneStickTankRobot` have no autonomous object yet, and
their `getAutonomousRobot()` returns `null` (Chapter 1).  They drive in
TeleOp, and autonomous code has nothing to call on them until somebody writes
one.

The written methods all check for `null` before they call a part:
`if (mechAssembly != null)`.  That check is what lets `StandardTankRobot` leave
out an assembly and still use every written method.

*See also:*  10.3, 10.6, Chapter 7, Chapter 9, Chapter 11

## 10.3  `Robot.update` is a template method

*Where it lives:*  `RobotModel/Robots/Robot.java`

Every loop, the OpMode calls `robot.update(gamepad1, gamepad2)`.  Unless a
robot writes its own, this is the method that runs:

```java
public void update(Gamepad gamepad1, Gamepad gamepad2)
{
    if (driveTrain != null) driveTrain.drive(gamepad1);
    if (mechAssembly != null) mechAssembly.giveInstructions(gamepad2);
}
```

Two steps, always in this order.  The first gamepad, the driver's, goes to the
drive train.  The second gamepad, the mechanism driver's, goes to the assembly.

`Wildbots2025` has no `update` of its own, so this exact method is
`Wildbots2025`'s main loop.  Follow one loop through it.  `driveTrain` is a
`MecanumDrive`, so `drive(gamepad1)` turns the sticks into four wheel powers.
`mechAssembly` is a `CascadeArm`, so `giveInstructions(gamepad2)` hands the
gamepad to the lift, the drawbridge, and the claw (9.3).  `Robot` wrote the
two steps and their order.  The drive train and the assembly each decided
what their step means.

`StandardTankRobot` and `OneStickTankRobot` have no `update` of their own
either.  Three of the four robots run this method as written.

> **Predict.**  `StandardTankRobot` has a drive train and no assembly.  What
> does this method do for it each loop?

It drives.  The first line runs, because `driveTrain` was built.  The second
line checks `mechAssembly != null`, finds `null`, and skips the call, so the
second gamepad goes nowhere.  One method serves a robot with an assembly and
a robot without one.

### The name for it

A method in a base class that fixes the steps of a job and their order, while
other code fills in what each step does, is a **template method**.  The base
class writes the template once.  Every robot that uses it gets the same main
loop, and differs only in the parts it plugged in.

The textbook version has the base class call methods that its own subclasses
override.  `Robot.update` is a close cousin:  it calls methods on the parts the
subclass chose.  Either way, the order is written in one place.
`doc/AbstractRobotObjectModel.md` calls it "a template-method style design."

What a template method buys is consistency.  Every robot that leaves `update`
alone sends the driver's gamepad to the drive train and the mechanism driver's
gamepad to the assembly, in that order.

*See also:*  10.2, 10.4, 10.5, Chapter 9

## 10.4  How does a robot add to the default?

*Where it lives:*  `RobotModel/Robots/BillyRobot.java`

A robot that wants more than the default can override a written method, do
its own extra work, and still run the base class's version by calling it with
`super`.  Billy does exactly that with `updateTelemetry`:

```java
@Override
public void updateTelemetry() {
    BillyMA mech = (BillyMA) mechAssembly;
    if (assist == null)
        telemetry.addData("Assist", "None");
    else
        telemetry.addData("Assist", assist.name() + " " + assist.claims());
    telemetry.addData("Macro", mech.macroName() + " " + mech.macroClaims());
    telemetry.addData("Last macro", mech.lastMacroEnd());
    telemetry.addData("Last assist", lastAssistEnd);
    super.updateTelemetry();
}
```

The first line is a cast (Chapter 7):  `mechAssembly` is declared as a plain
`MechAssembly`, and Billy tells Java it's really a `BillyMA`, so it can ask
for the macro's name.  Then Billy writes four lines of its own, about which
strategies are running and how the last ones ended (Chapter 13).  Then it
calls `super.updateTelemetry()`, and `Robot`'s written method does everything
it always does:  gather from the drive train, gather from the assembly, send
once (10.6).

> **Aside:  `super.method()`**
>
> Inside a subclass, `super.updateTelemetry()` calls the parent class's
> version of `updateTelemetry`, the one this class just overrode.  Chapter 1
> covers `super(...)`, which calls the parent's constructor.  This is the same
> word, used to reach a parent's method.  Leave off the `super.` and the call
> inside `updateTelemetry()` runs Billy's own version again, and again, until
> the program crashes.

So Billy's screen is the default screen with four lines on top.  The base
class's algorithm runs untouched, and the subclass adds to it around the
edges.  That's the usual way to extend a template method (10.3):  do your
part, then hand the rest to `super`.

### Billy's `update` used to have the same shape

When the lessons were written, Billy built around `Robot.update` the same way.
Here's its strategy as `BillyRobot.java` stood at commit `34c22ea`:

```java
strategy = (robot, gamepad1, gamepad2)  -> {
    super.update(gamepad1, gamepad2);
    if(!targeter.isComplete())
        targeter.updateState();
};
```

The first line runs `Robot`'s two steps, drive and then mechanisms.  The
second adds one thing afterward:  a step of the turret's auto-aim (Chapter
12).  The lesson pointed at `super.update(gamepad1, gamepad2)` as the line
that showed Billy using the template.

Billy's `update` has since changed shape, and 10.5 says why.  Its
`updateTelemetry` above is today's example of building around the default.

*See also:*  10.3, 10.5, 10.6, Chapter 7, Chapter 13

## 10.5  Why does Billy write its own `update`?

*Where it lives:*  `RobotModel/Robots/BillyRobot.java`

Billy overrides `update` and hands the work to a strategy, a lambda built in
its constructor (Chapter 8):

```java
@Override
public void update(Gamepad gamepad1, Gamepad gamepad2) {
   strategy.execute(this, gamepad1, gamepad2);
}
```

Here's the start and the end of that strategy:

```java
strategy = (robot, gamepad1, gamepad2)  -> {
    if (gamepad1.yWasPressed()) {
    ...
    driveTrain.drive(gamepad1);
    mech.giveInstructions(gamepad2, assistClaims());
    if (assist != null) {
        assist.update(gamepad1, gamepad2);
    }
};
```

The part left out is button handling:  `y` on the driver's gamepad turns the
turret's auto-aim on and off, `a` and `b` on the mechanism gamepad start and
cancel rapid fire, and `b` on the driver's gamepad stops both.  Chapter 13
covers all four.

Then come the same two steps as `Robot.update`, in the same order:  driver's
gamepad to the drive train, mechanism gamepad to the assembly.  Look closely
at the second one, though.  It calls `giveInstructions` with two arguments.

### What the default couldn't carry

Billy's turret can be driven two ways:  by the bumpers, through `BillyMA`'s
strategy (9.3), or by the auto-aim, an *assist* that steers it from the
Limelight (Chapter 13).  Only one of them should command the turret at a
time.  So when the assist is running, `BillyMA` needs to know that the turret
is taken, and skip the bumpers.

`assistClaims()` is how it finds out.  It returns the set of parts the assist
has claimed, which for the auto-aim is the turret.  Billy passes that set
into the two-argument `giveInstructions`.

`Robot.update` calls the one-argument version, `giveInstructions(gamepad2)`.
`Robot` is written for every robot, and most robots have no claims to pass, so
its call has no room for them.  `BillyMA`'s one-argument version passes an
empty set:  nothing claimed, so every component hears the gamepad.  With
that call, the bumpers and the auto-aim would both command the turret in the
same loop.

So Billy writes the two steps itself, with the claims in the call.  The
commit that made the change, `b9b2a25`, says it in its message:  "assist
claims reach BillyMA, turret has one owner."

### The trade-off

Replacing the default costs something.  Billy now has its own copy of the
drive-then-mechanisms order, so a change to `Robot.update` reaches the other three robots and passes Billy by.  Billy's
copy also drops the `null` checks, which is safe only because Billy always
builds both parts.

What it buys is one owner for the turret, and one place to read Billy's whole
loop:  buttons first, then the drive train, then the assembly with its claims,
then the assist.  Billy needed something the template had no slot for, and
paid for it with a copy of the order.

Billy still uses the other written methods.  Its `updateTelemetry` adds four
lines and calls `super` (10.4), and its constructor calls
`initializeSubsystems()` like everyone else's.

> **Argue it.**  `Robot` could make room for robots like Billy.  One way is a
> method `Robot.update` calls between its two steps, which returns the claims
> and returns an empty set unless a robot overrides it.  Then Billy could keep
> the template and supply only the claims.  The cost is a base class that
> knows about claims, which only one robot uses today.  Is that worth it now,
> or after a second robot needs it?

*See also:*  10.3, 10.4, Chapter 8, Chapter 9, Chapter 13

## 10.6  The single flush point

*Where it lives:*  `RobotModel/Robots/Robot.java`

Every layer of the robot writes into the telemetry buffer:  components,
assemblies, drive trains, and the autonomous verbs.  Writing is `addLine` and
`addData`.  Sending the buffer to the driver station is `telemetry.update()`,
which Chapter 7 calls the flush.  Here's the one place in the robot model that
flushes:

```java
public void updateTelemetry() {
    if (driveTrain != null) driveTrain.updateTelemetry();
    if (mechAssembly != null) mechAssembly.updateTelemetry();
    telemetry.update();
}
```

Three steps.  The drive train writes its lines.  The assembly writes its
header and visits every component (9.6).  Then, once both are done, the buffer
goes to the screen in one piece.

This is the **single flush point**:  exactly one method, `Robot.updateTelemetry`,
calls `telemetry.update()`, once per loop.  Search the robot model for
`telemetry.update()` and this is the only call you'll find.  The OpModes make
one more call each, before the loop starts, to show a line like
`Billy Initialized` while the robot waits for the start.  From the first loop
on, the robot does all the sending.

### Why only one

Picture a component that flushed on its own, halfway through a loop.  The
buffer would go to the screen half full, and then clear.  Every line written
after it would wait for the next send, one loop late.  The driver would see
the screen flicker between partial pictures, and every class would look
correct when you read it alone.

The comment at the top of `Robot.java` records that this happened here.  An
earlier design passed telemetry into every loop, "causing multiple competing
flush points and mid-cycle updates."  The 2025-26 telemetry refactor
(`doc/TelemetryContract.md` tells it) moved the flush here, and the rule has
held since.  The strategies that report from inside a loop, like
`BillyRapidFire`, write through an autonomous object's `reportStatus` and
have no telemetry object of their own to flush with (9.5).

### Overriding it safely

Billy overrides `updateTelemetry` (10.4), and it still has one flush, because
it ends by calling `super.updateTelemetry()`.  The order inside the override
matters.

> **Predict.**  Suppose Billy wrote its four lines *after*
> `super.updateTelemetry()`.  What would the driver see?

The four lines would miss this loop's send.  They'd sit in the buffer until
the next loop's flush, so they'd show up a loop late, at the top of the next
screen.  Write your lines first, then call `super`.

> **Try it.**  Search the `RobotModel` folder for `telemetry.update()` and
> read every hit.  You should find one call, in `Robot.updateTelemetry`, and
> the rest of the hits in comments that state the rule.

*See also:*  10.4, 9.6, Chapter 7

## 10.7  The robot is a Facade

*Where it lives:*  `RobotModel/Robots/BillyRobot.java`

The OpMode that runs Billy doesn't need a list of the four components in
`BillyMA`.  It builds a `BillyRobot`, then calls `update` and
`updateTelemetry`.  The robot decides how to reach everything underneath.
10.1 shows that same arrangement on `Wildbots2025`.

That arrangement is a **Facade**:  one object presents a small set of useful
operations over several objects working together.  A reader of the OpMode can
see the match's main loop before learning how a turret, a drive train, or an
assembly works.  Their details are in the classes that own them.

Billy's constructor does the connecting.  It builds the drive train and
assembly, configures the camera and IMU, passes telemetry down, and gathers
the autonomous capabilities.  Here's the gathering step:

```java
        auton = new BillyRobot.AutonomousMecanumRobot(
                driveTrain.getAutonomousDriving(),
                mechAssembly.getAutonomousBehaviors(),
                telemetry);
```

Those three arguments become one robot-level autonomous object.  Its fields
let a caller choose driving or the mechanism assembly, then a particular
component's verbs (Chapter 11).  The same parts can therefore be reached
through a small TeleOp interface and a more detailed autonomous interface.
Each serves the work its caller needs to do.

The cost is that a new connection has to be made in the robot's constructor.
Adding another subsystem also means deciding how the loop updates it and
how it reports.  What that buys is an OpMode that can run the robot through
a few calls.  That's the trade-off:  the robot class does the connecting so
every caller can use what it connected.

There is a useful test when an OpMode starts growing.  Read each new line
and ask whether it manages the match, builds the robot, or decides how a
mechanism behaves.  Waiting for start belongs in the OpMode.  Configuring
the robot's subsystems belongs in the robot.  A mechanism's response belongs
farther down the model.  Chapter 21 uses that test while designing a robot.

*See also:*  10.1, 10.2, Chapter 6, Chapter 11, Chapter 21

## 10.8  Robot-wide sensors

*Where it lives:*  `RobotModel/Robots/BillyRobot.java`

Billy builds its camera at the robot layer:

```java
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.start();
        limelight.pipelineSwitch(0);
```

The constructor fetches the configured device, requests a polling rate,
starts it, and selects a pipeline.  Chapter 5 explains those camera
operations.  The point here is ownership:  the robot does the setup.

Later in the same constructor, Billy gives the camera to the targeting
strategy alongside the turret's autonomous behaviors:

```java
        targeter = new LimelightAutoTarget(
                limelight,
                ((BillyMA)mechAssembly).getAutonomousBehaviors().autonTurret,
                tagID);
```

The targeter receives what it needs to read a tag and turn the turret.
The turret itself deals in turning power and direction.  Its component
class can be used with another source of commands, and the robot can also
make camera information available to another behavior.

An IMU is another robot-wide sensor.  It reports the chassis's orientation,
which driving and autonomous turning can both use (Chapter 3).  Billy's
constructor describes the actual hub mounting when it initializes the IMU:

```java
        IMU.Parameters parameters = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP
                )
        );

        imu.initialize(parameters);
        imu.resetYaw();
```

Those directions describe Billy's mounting.  A different robot needs the
directions that describe its own hub.  The current `MecanumDrive` constructor
also fetches the IMU.  Its source TODO proposes having the robot initialize
it once and pass it down, following the camera's ownership pattern.

Use the sensor's meaning to choose its layer.  A switch that limits one
lift belongs with the lift.  A reading about the transfer between two
mechanisms belongs with their assembly.  A heading for the chassis, or a
sensor shared across subsystems, belongs with the robot.  Chapter 4 owns
that sorting rule;  this constructor is where it becomes a real connection.

> **Try it.**  Read Billy's constructor and find the camera lookup, camera
> setup, and targeter construction.  Then find the hub directions supplied
> to the IMU.  Compare those directions with the actual mounting before
> using heading measurements on a different robot.

*See also:*  10.1, 10.7, Chapter 3, Chapter 4, Chapter 5, Chapter 13

---

## Chapter summary

The robot layer connects the drive train, assembly, sensors, and telemetry.
`Robot` supplies a default dispatch loop and the gather-then-send telemetry
method.  A concrete robot chooses its parts and can extend or replace those
defaults when its coordination needs change.

| Idea | Where |
|---|---|
| the constructor chooses and connects the parts | 10.1, 10.2 |
| the default update sends each gamepad to its subsystem | 10.3 |
| an override can add work and call the parent's method | 10.4 |
| Billy passes ownership claims in its own dispatch loop | 10.5 |
| collect telemetry before sending it once | 10.6 |
| a Facade gives callers a small interface over the connected parts | 10.7 |
| robot-wide sensor setup belongs with the robot | 10.8 |

### Java in this chapter

| Syntax | Where |
|---|---|
| concrete and abstract methods on one base class | 10.2.  Classes and methods are in Chapter 1. |
| `super.updateTelemetry()` | 10.4.  Calls the parent implementation. |
| a cast to `BillyMA` | 10.4, 10.8.  Casts are in 7.2. |

### Vocabulary

**Facade** -- an object that presents a small interface over several objects
working together.  The robot presents `update`, `updateTelemetry`, and its
autonomous object to the OpMode.  (10.7)

**robot-wide sensor** -- a sensor describing the whole robot or supplying
information to more than one subsystem.  (10.8)

**single flush point** -- the method that sends the telemetry buffer after
the subsystems have written their readings.  Here it is
`Robot.updateTelemetry()`.  (10.6)

**template method** -- a base-class method that fixes a job's steps and order
while other code supplies what each step does.  `Robot.update` is a
template-method-style design using the robot's chosen parts.  (10.3)

### Check your understanding

1. What does `Wildbots2025` build before it calls `initializeSubsystems`,
   and what can it build afterward?  (10.1, 10.2)
2. Trace one call to the inherited `Robot.update` on `Wildbots2025`.
   What happens for a robot whose assembly is `null`?  (10.3)
3. Why does Billy write its telemetry lines before calling
   `super.updateTelemetry()`?  (10.4, 10.6)
4. What information does Billy's two-argument `giveInstructions` carry
   that the default loop's one-argument call leaves out?  (10.5)
5. What does the OpMode gain by letting the robot connect its subsystems?
   Where is the extra connecting work done?  (10.7)
6. Trace the camera from its hardware-map name to the targeter.  Which
   object configures it, and which object reads it to aim?  (10.8)
7. Why must a new robot check its own hub mounting before copying Billy's
   IMU parameters?  (10.8)

## Where to go next

Chapter 11 follows the autonomous object through the layers to a mechanism's
verbs.  Chapter 12 puts those verbs in a sequence that takes time.  Chapter
13 explains how Billy's assist and macro share control with the drivers.
For the parts this robot connects, return to Chapters 7 and 9.
