# Chapter 11 - Autonomous Verbs

*The routine asks for a push.  The pusher knows the position.*

An autonomous routine needs to request actions:  start the intake, push a
ball, stop driving.  The component knows how those requests reach its
hardware.  Its autonomous behaviors give the routine named methods for
making the requests.

This chapter follows those methods from one component up to a complete
robot.  Look here when a long chain of dots is hard to read, or when you're
choosing what a new autonomous method should mean.  Chapter 12 explains
how requests become a sequence that runs over time.

Read first:  Chapter 7 for components, 9.5 for the assembly's autonomous
object, and Chapter 10 for the robot layer.  Section 1.4 explains method
calls and the dot that connects them.

| Entry | The question it answers |
|---|---|
| 11.1 | What makes a method an autonomous verb? |
| 11.2 | Does the autonomous object own another motor? |
| 11.3 | When do these objects become available? |
| 11.4 | How do I read the full dot-chain? |
| 11.5 | How does one request operate several devices? |
| 11.6 | Which details should a caller choose? |
| 11.7 | What does a status message prove? |
| 11.8 | Who handles time and competing requests? |

---

## 11.1  Name the action the caller wants

*Where it lives:*  `RobotModel/Mechs/Components/PusherServo.java`, inside
`AutonomousBallPusherBehaviors`.

The ball pusher exposes this method:

```java
        public void pushBalls() {
            servoR.setPosition(0.8);
            reportStatus("Pusher: push");
        }
```

The caller asks to push balls.  The method translates that request into
the servo position chosen for this pusher, then adds a status message.
Elsewhere in the same behaviors class, `retractPusher()` requests position
`0.0`.  A routine can name both actions without copying either position.

An **autonomous verb** is a method that lets a routine request an action
through the mechanism's behavior API.  An API is the set of operations
an object offers its callers.  The useful design question is what the
caller needs to express.  Here it needs a push and a retraction.
`PusherServo` has the information needed to translate those actions into
servo commands.

That puts a tuning choice in one place.  If the linkage later needs a
different push position, changing the implementation of `pushBalls`
changes requests made through that verb.  The routine's sequence of
actions can remain the same.  See 11.6 for cases where callers do need
a parameter, such as a requested shooter power.

The method name describes intention.  It doesn't by itself promise
that a ball was present, that it moved, or that motion has finished.
This method sets a requested position and returns.  Its body has no
sensor check or wait.  Read what a verb actually does before deciding
what a routine may assume after calling it.

> **Try it.**  Find `pushBalls` and `retractPusher` in the source.  Which
> two lines contain the tuning positions?  Which method names would a
> routine use to request those actions?

See also:  11.7 for status messages; 11.8 for waiting after a request.

## 11.2  Two ways to use the same hardware

*Where it lives:*  `SpinnyIntake.AutonomousIntakeBehaviors`.

The intake's autonomous behaviors are an inner class, declared inside
`SpinnyIntake`.  Its start method is short:

```java
        public void startIntake()
        {
            intake.setPower(1);
            reportStatus("Intake: running");
        }
```

`intake` here is the motor field of the enclosing `SpinnyIntake`
instance.  The behaviors object can reach it because it is an inner
object associated with that component.  It doesn't obtain a second
motor from the hardware map.  See 7.3 for inner classes.

The TeleOp path reaches the same hardware by a different route.
`SpinnyIntake.move(Gamepad)` passes `intake` and the gamepad to the
control strategy.  Billy's strategy reads the dpad and chooses a
power.  The autonomous path above requests a known action directly.
`startIntake()` needs no gamepad argument because the request already
specifies what to do.

That gives the component two ways for callers to use its hardware:
gamepad-based control and autonomous verbs.  They share a device, so
their effects can overwrite one another.  If a manual strategy writes
zero after an autonomous verb writes one, the later write determines
the motor's request.  Offering a separate behaviors object doesn't
schedule those writes or assign ownership.  Chapter 13 handles that
question for Billy's macros and assists.

Also notice that the autonomous and manual choices can differ.
Billy's manual intake strategy requests `0.75` for forward movement;
`startIntake` above requests `1`.  Read the actual path you are using
when checking a command.  A name such as "start" doesn't imply that
all callers use the same configured speed.

> **Predict.**  Does constructing an autonomous behaviors object create
> another physical intake?  No.  It creates another Java object that can
> use the enclosing component's existing motor.

See also:  8.4 for the manual path; Chapter 13 for ownership of shared parts.

## 11.3  Build the behaviors from the bottom up

*Where it lives:*  `SpinnyIntake.initializeTelemetry`,
`BillyMA.initializeTelemetry`, and the `BillyRobot` constructor.

The component creates its autonomous object when telemetry is supplied:

```java
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousIntakeBehaviors(telemetry);
    }
```

The first call stores telemetry through the base component.  The next
line gives that same telemetry object to the new behaviors object.
That object can now report through `reportStatus` and `reportData`
without asking each caller for telemetry again.

The assembly repeats the process at a larger scale.  Billy initializes
the telemetry of all four components, obtains their autonomous objects,
and passes them into `AutonomousBillyMA`.  Section 9.5 shows the whole
assembly object.  Its fields are references to those existing component
behaviors, gathered for callers to use.

Billy's robot constructor assigns its drivetrain and assembly, calls
`initializeSubsystems()`, and then builds its robot-level autonomous
object from the drivetrain's and assembly's autonomous objects.
Each layer can gather the lower layer's results because initialization
has already reached that lower layer.

This is the **two-phase construction** introduced in 7.4.  First the
program constructs the hardware-owning objects.  Then it supplies
telemetry and creates the behaviors that need it.  The ordering is part
of using the API:  obtaining a component's `auton` before it has been
initialized won't produce the ready behaviors object shown here.

Trace this lifecycle when adding a component.  Its constructor must
run, its telemetry initializer must be reached, and its behaviors
must be included in the assembly's aggregate.  A declaration of a
field alone doesn't perform any of those steps.  The checklist in
9.8 follows a component through the assembly.

See also:  7.4 for initialization; 9.5 for aggregation; 10.2 for the robot helper.

## 11.4  Read a dot-chain one object at a time

*Where it lives:*  `OpModes/ExampleStateMachineAuton.java`.

The example autonomous OpMode requests a push like this:

```java
    private IState pushBall(){
        return doAndWait(
                () -> robot.getAutonomousRobot()
                        .mechAssembly
                        .autonBallPusher
                        .pushBalls(), 1000, reset());
    }
```

For now, follow the chain inside the lambda.  Chapter 12 explains the
`doAndWait` helper.  Each step selects a smaller part of the robot's
autonomous API:

| Expression or field | What it provides on Billy |
|---|---|
| `robot` | The `BillyRobot` used by this OpMode |
| `getAutonomousRobot()` | Its `AutonomousMecanumRobot` object |
| `.mechAssembly` | The assembly's `AutonomousBillyMA` object |
| `.autonBallPusher` | The pusher's `AutonomousBallPusherBehaviors` object |
| `.pushBalls()` | The request implemented in 11.1 |

The field names are important.  `.mechAssembly` at this point is a
field of the autonomous robot object.  It refers to the assembly's
behaviors, rather than to the hardware-owning `BillyMA` itself.
Likewise, `.autonBallPusher` refers to the pusher's behaviors.  The path
exposes named operations while leaving this caller out of the servo's
configuration and setup.

Java's declared types determine which fields and methods a caller can
use.  Billy's concrete `getAutonomousRobot()` returns its concrete
autonomous robot type.  That type declares the assembly field, and the
assembly's concrete behaviors type declares `autonBallPusher`.  Those
declarations explain why the next name in the chain is available.
Follow each declaration when your editor offers an unfamiliar method.

You can also read the chain backward:  `pushBalls` is available on the
pusher behaviors, which are gathered into Billy's assembly behaviors,
which are gathered into Billy's autonomous robot.  That is the
construction order from 11.3 appearing in a call made from the top.

See also:  1.4 for dots and calls; 9.5 for the assembly fields; Chapter 12 for states.

## 11.5  One verb can command several devices

*Where it lives:*  `DoubleShooter.AutonomousShooterBehavior`.

The shooter uses a pair of motors.  Its parameterized power command is:

```java
        public void setPower(double power){
            shooterF.setPower(power);
            shooterB.setPower(-power);
            reportData("Shooter power", power);
        }
```

The caller chooses one power value.  The component sends that value to
the front motor and its negative to the back motor.  The relationship
between the two commands belongs to this paired mechanism.  A routine
using this method doesn't have to reproduce the pair of assignments
every time it requests a shooter speed.

That is one reason the component layer can represent a purposeful
collection of devices, as described in 7.7.  A pair driven together has
a common operation worth naming.  The component keeps the hardware
relationship close to that operation.  If a physical rebuild changes
the relationship, review this implementation and the motor configuration
instead of assuming the old signs still fit the new mechanism.

At a higher layer, the assembly's behaviors gather several independent
capabilities.  Billy's caller can select `autonFlywheel`,
`autonBallPusher`, `autonIntake`, or `autonTurret`.  A sequence can combine
them, but merely obtaining the assembly object doesn't start any of
them.  The sequence still needs to make the appropriate calls.

Driving is a sibling capability on the autonomous robot.  The example
OpMode selects `.driveTrain` to request chassis motion, or
`.mechAssembly` to request a mechanism action.  These are separate
branches of the same robot object.  The autonomous API gives a routine
access to both without requiring it to construct another drivetrain
or assembly.

> **Try it.**  In `ExampleStateMachineAuton`, find `driveForward` and
> `startShooter`.  Follow both chains from `getAutonomousRobot`.  Where
> do the paths first differ, and which object receives the final call?

See also:  7.7 for coupled devices; 10.7 for the robot's combined interface.

## 11.6  Let the caller choose the quantity it understands

*Where it lives:*  `PusherServo.AutonomousBallPusherBehaviors` and
`DoubleShooter.AutonomousShooterBehavior`.

Billy's rapid-fire routine calls the shooter's `setPower(0.6)`.  The
routine chooses the requested power; the component translates it into
the opposing motor commands shown in 11.5.  By comparison, the routine
calls `pushBalls()` without passing a servo position.  The pusher
component makes that choice.

Useful verbs can therefore take parameters.  Ask what each parameter
means to the caller.  Shooter power is a quantity the rapid-fire routine
currently tunes.  The pusher's exact servo position is a detail the
named push operation can supply.  Both choices are visible in the
current API; neither establishes that all future mechanisms should
offer the same methods.

A **leaky abstraction** makes a caller depend on details that the
interface was meant to contain.  Consider an invented method named
`setClawServoToPointOneFive()`.  To use it as an opening action, every
caller must know what that number means on this linkage.  An invented
`openClaw()` method could keep the corresponding setting inside the
claw's implementation.  These names illustrate an API choice; they
aren't quotations from the current `Claw` class.

There is a trade-off.  A named operation gives callers less direct
choice of hardware settings.  That restriction helps keep tuning
knowledge in one place.  A more general setter exposes more choice
and gives its caller more responsibility.  `PusherServo` offers both
`pushBalls()` and `setPosition(double position)`.  A reader should
choose deliberately:  does this routine need the pusher's defined
push action, or is it responsible for selecting a position itself?

> **Argue it.**  A new linkage changes the servo position needed to
> release a game piece.  Compare an invented `releasePiece()` API with
> a routine that supplies the raw servo position.  Who must know about
> the linkage change in each design?  Which caller, if any, needs the
> extra control of a position parameter?

See also:  11.1 for naming intention; Chapter 21 for designing behavior APIs.

## 11.7  Reporting a request through the shared telemetry

*Where it lives:*  `MechComponent.AutonomousComponentBehaviors`.

The pusher's verb ends with `reportStatus("Pusher: push")`.  That helper
is inherited from the base autonomous component behaviors:

```java
        public void reportStatus(String status) { telemetry.addLine(status); }
```

The behaviors object already has a telemetry reference because it
received one during initialization.  `reportStatus` adds a line to the
shared buffer.  `reportData` similarly adds a named value.  Assembly-
and robot-level autonomous objects offer these reporting helpers too,
so a sequence can report at the level where it has useful information.

That lets the rapid-fire routine report which action it is requesting
without receiving another raw telemetry argument.  The component can
also report its own command.  The robot's telemetry update collects
the regular subsystem measurements and publishes the accumulated
report at the single flush point described in 10.6.

Read a status according to the statement that produced it.  In
`pushBalls`, the message is added immediately after `setPosition`.
It tells you the method reached that line after making the command.
There is no ball detector or arrival check in that method.  In the
intake, `"Intake: running"` similarly follows a power request.  A
command message is useful evidence when tracing a routine, but a
separate measurement is needed to answer a physical question.

When designing a report, name that question first.  "Which step is
active?" can use a state label.  "What power was requested?" can use
a command value.  "Has the mechanism reached its limit?" needs an
appropriate sensor reading.  Keeping those meanings clear helps you
choose what to inspect when a robot behaves differently from the
sequence you expected.

See also:  7.5 for measurements; 10.6 for publishing telemetry.

## 11.8  Verbs need a sequence and an owner

*Where it lives:*  `AutonStrategies/BillyRapidFire.java` and
`OpModes/ExampleStateMachineAuton.java`.

Billy's rapid-fire routine starts the shooter, waits, pushes a ball,
and retracts the pusher.  The component verbs provide the requests.
The routine's state machine provides their order and timing.  In the
example from 11.4, `doAndWait` receives an action, a wait in milliseconds,
and a following state.  Chapter 12 follows how that helper advances.

This division matters for a motor command.  Starting an intake requests
power and returns; it doesn't promise to stop a second later.  A
routine needs an explicit plan for stopping, cancellation, and any
conditions that must be checked throughout movement.  A one-time
request can't observe a switch that changes later.  Section 8.6
discusses that timing for the bounded motor.

An autonomous verb can also be useful during TeleOp.  Billy's rapid-fire
macro uses the same pusher and shooter behaviors while a driver is
present.  "Autonomous" here describes the API for directly requesting
an action.  It doesn't limit the method to the autonomous period of
a match.

Using that API during TeleOp introduces an ownership question.  The
macro and a manual control strategy can both reach the same devices.
Billy's macro declares claims on `PUSHER` and `FLYWHEEL`, and the
assembly skips manual commands for claimed parts.  Chapter 13 owns
the details of that arbitration.  The verb itself supplies no lock.

Some coordination interfaces still accept gamepads.
`BillyRapidFire.update(Gamepad)` accepts one to match its macro
interface, but its body advances `updateState()` without reading that
argument.  The component verbs above need no gamepad.  Trace the
actual method body to learn which inputs a behavior uses, rather
than inferring it from the class's name.

See also:  Chapter 12 for state machines; Chapter 13 for claims and cancellation.

## Chapter summary

The component's autonomous behaviors turn requests into device commands.
Assembly and robot objects gather those behaviors into a path a routine
can follow.  The routine remains responsible for order, timing, and the
conditions under which requests may run.

| Question | Place to look |
|---|---|
| What does a verb command? | Its component method body |
| Which object supplies the next part of a dot-chain? | The current field or method's declared type |
| When is the behaviors object created? | Telemetry initialization, then aggregation upward |
| How long does the action run? | The sequence and its repeated updates |
| Who may command the part now? | The ownership mechanism used by the caller |

### Java in this chapter

| Feature | What to remember |
|---|---|
| Inner class | Its object can use the enclosing component's fields. |
| Concrete return type | It tells Java which members the next caller can use. |
| Method return | A command method can return before physical movement finishes. |

### Vocabulary

**API.**  The operations an object offers its callers.

**Autonomous verb.**  A method through which a routine requests a named action.

**Dot-chain.**  A sequence of member selections and method calls used to
reach an object or operation.

**Intention.**  The action a caller wants the mechanism to perform.

**Leaky abstraction.**  An interface that makes callers depend on details
the interface was meant to contain.

### Check your understanding

1. Which object knows the servo position used by `pushBalls`?  (11.1)
2. Do manual intake control and `startIntake` use separate motors?
   Why might their requests conflict?  (11.2)
3. Why must a component's telemetry initialization happen before the
   assembly gathers its autonomous behaviors?  (11.3)
4. Name each object in the path from Billy's autonomous robot to
   `pushBalls`.  (11.4)
5. Which motor-command relationship does the shooter hide from its
   caller?  Which value does the caller still choose?  (11.5, 11.6)
6. What would a caller have to know to use a raw servo position as a
   release action?  (11.6)
7. What does `"Pusher: push"` tell you?  What extra observation would
   confirm that a ball moved?  (11.7)
8. Which code handles waiting between actions?  Which code prevents
   manual control from competing with Billy's macro?  (11.8)

## Where to go next

Chapter 12 turns verbs into a state machine that advances over time.
Chapter 13 follows how a macro and driver share the robot's parts.
For the objects that make the dot-chain possible, return to 7.3, 9.5,
and Chapter 10.


