# Chapter 8 - Control Strategies

*The motor doesn't care which button you picked.*

An intake needs a motor and a rule for driving it.  Those two things change
for different reasons.  A new robot might use the same intake motor with a
different driver control.  The team passes the control rule into the
component so the hardware wrapper can be reused.

This chapter follows that rule from its definition to the call that runs
it.  It also explains the choice that comes before writing a rule:  which
layer has enough information to make this decision?

Read first:  1.9 for lambdas, Chapter 6 for the layers, and Chapter 7 for
component construction.  Chapter 9 follows these components into assemblies.

| Entry | The question it answers |
|---|---|
| 8.1 | Why pass a control rule into a component? |
| 8.2 | How can a lambda implement an interface? |
| 8.3 | Who chooses the rule? |
| 8.4 | What happens when `move` runs? |
| 8.5 | Can telemetry have a strategy too? |
| 8.6 | How does a wrapper enforce a travel limit? |
| 8.7 | Which layer should make a decision? |
| 8.8 | When is a strategy worth the extra structure? |

---

## 8.1  Separate the device from its control rule

*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java` and
`RobotModel/Mechs/Assemblies/BillyMA.java`.

Billy's assembly constructs a `SpinnyIntake`.  It supplies a hardware-map
name, `"intakeMotor"`, and a lambda beginning `(motor, gamepad) ->`.  In that
lambda, dpad up requests power `0.75`, dpad down requests `-0.75`, and neither
button requests zero.  These are the controls chosen for this robot.

The component supplies something different:  it obtains the named motor
from the hardware map, stores it, and offers `move`, telemetry, and
autonomous behaviors.  See 7.2 for its constructor.  `SpinnyIntake` itself
contains no choice of dpad buttons.

A **control strategy** is the rule that translates the supplied gamepad
state into commands.  The **Strategy pattern** gives that rule a separate
implementation and passes it to the object that uses it.  Here a lambda is
the implementation.  The component keeps a reference to it and calls it
when the assembly asks the component to move.

This costs a reader an extra step:  to learn Billy's buttons, you must open
`BillyMA`, then follow the component's call into the supplied lambda.  The
benefit is reuse.  Another assembly can construct another `SpinnyIntake`
with another motor name and another mapping, without copying the component
class.  Each instance has its own motor and strategy.

That distinction also helps when tracing a change.  If dpad up should run
Billy's intake more slowly, look in the assembly's lambda.  If every
`SpinnyIntake` should report another motor measurement, look in the
component's `update`.  These changes belong to the choices each object
already makes.

> **Try it.**  Open `BillyMA` and find the intake's constructor call.  Read
> the three branches aloud, including the branch for neither button.  Then
> find the line in `SpinnyIntake` that actually invokes this rule.

See also:  7.6 for reusing a component; 8.4 for the invocation.

## 8.2  The interface gives the lambda its shape

*Where it lives:*  `SpinnyIntake.SpinnyIntakeControlStrategy`.

Inside the intake's strategy interface is this declaration:

```java
        void nomNomNom(DcMotor motor, Gamepad gamepad);
```

The method takes a `DcMotor`, then a `Gamepad`, and returns nothing
(`void`).  A lambda supplied where Java expects this interface must match
that shape.  Its first parameter receives the motor, and its second
receives the gamepad.  The parameter names in the lambda help people read
it; the parameter types and their order are what connect it to this
declaration.

An interface with one abstract method is a **functional interface**.
An abstract method declares a method without supplying its body.  The
lambda supplies that missing body.  The empty parent interface,
`IControlStrategy`, adds no further methods, so this child interface still
has just one to implement.  The annotation `@FunctionalInterface` can ask
Java to check that property, but an interface can qualify without the
annotation.

Read `(motor, gamepad) -> { ... }` as:  when the strategy's method is
called, use these two supplied objects to run the code in braces.  See 1.9
for the punctuation.  Creating the lambda doesn't run its body.  Storing
it in a field doesn't run it either.  A later method call does.

The method name `nomNomNom` belongs to the intake's interface.  The lambda
doesn't repeat that name because its expected interface already identifies
the one method it implements.  Another strategy interface can choose a
different method name and a different parameter list.  For example,
`DoublyLimitedMotorControlStrategy` accepts a gamepad followed by a
`DoublyLimitedMotor`.  Read that interface before interpreting its lambda;
the motor-first order above isn't a project-wide rule.

> **Predict.**  Suppose a lambda for `SpinnyIntakeControlStrategy` has only
> one parameter.  Does the motor receive it or the gamepad?  Neither:  the
> lambda doesn't match the interface's two-parameter method, so Java
> rejects it when the program is compiled.

See also:  1.7 for interfaces; 8.4 for the actual arguments.

## 8.3  The assembly chooses the controls

*Where it lives:*  the `BillyMA` constructor.

The ball pusher is constructed with both a control strategy and a telemetry
strategy.  Here is its constructor call in Billy's assembly:

```java
        ballPusher = new PusherServo(hardwareMap,
                "ballPusherServo",
                (servoR, gamepad) -> {
                    if (gamepad.x) {
                        servoR.setPosition(0.8);
                    } else {
                        servoR.setPosition(0.0);
                    }
                },
                (servo, telemetry) -> {
                    telemetry.addData("pusher position", servo.getPosition());
                });
```

Follow the arguments in order.  `hardwareMap` lets the component obtain
its device.  `"ballPusherServo"` is the configuration name to look up.
The first lambda maps the X button to a requested position.  The second
chooses what to report.  Each lambda is an argument passed to the
constructor, just as the name is an argument.

This mapping requests position `0.8` while X is held and `0.0` otherwise.
It is a held-button control:  each call chooses a position using the
current button state.  Nothing in this lambda remembers whether X was
pressed on the preceding loop.  See Chapter 13 when you need the
distinction between holding a button and starting an action once.

The constructor's numbers are choices for this mechanism.  They don't
measure whether a ball has moved, and they aren't positions in inches.
`Servo.setPosition` takes a normalized position; see Chapter 3 for that
device API.  The component also sets this servo's direction to `REVERSE`
in its own constructor.  The gamepad lambda uses the component's configured
servo, so tracing a physical direction requires reading both places.

Putting the mapping in the assembly gives the person arranging the
mechanisms a place to choose their controls together.  Two separate
component classes can both react to X, but only someone considering the
whole assembly can decide whether that combination is useful.  Chapter 9
explains how the assembly calls the components; Chapter 13 covers
arbitration when several behaviors request the same part.

See also:  8.5 for the second lambda; 9.3 for passing instructions down.

## 8.4  Calling the strategy with real objects

*Where it lives:*  `SpinnyIntake.move` and `DoublyLimitedMotor.move`.

The intake's `move` method connects the stored rule to the hardware:

```java
    @Override
    public void move(Gamepad gamepad)
    {
        strategy.nomNomNom(intake, gamepad);
    }
```

`strategy` is the rule supplied during construction.  `intake` is the
motor the component obtained from the hardware map.  `gamepad` is the
argument passed to this call of `move`.  The strategy's parameter named
`motor` receives that `intake` reference.  It can therefore call
`setPower` on the same motor the component owns.

There are two events to trace.  During setup, the assembly constructs a
component and gives it a strategy.  During a TeleOp loop, the assembly
calls `move`, and `move` calls that strategy using the current gamepad.
The component doesn't construct a new motor each time the driver presses
a button.  It repeatedly supplies the reference it already has.

The object handed to a strategy also determines which operations the
strategy can request.  Compare the bounded motor's call:

```java
    @Override
    public void move(Gamepad gamepad) {
        strategy.move(gamepad, this);

    }
```

Here `this` means the `DoublyLimitedMotor` instance whose method is
running.  Its strategy receives the wrapper, rather than the raw motor.
Calling the wrapper's `setPower` goes through its limit checks.  Section
8.6 follows that check.  The intake has no such limit-switch wrapper, so
its strategy receives the SDK motor directly.

This is why a strategy is more than a list of buttons.  Its interface
defines what information and capabilities its code receives.  To read an
unfamiliar one, find the interface declaration, the supplied lambda, and
the call site.  Together they answer three questions:  what shape must
the rule have, what does this robot's rule do, and which actual objects
will the rule use?

See also:  1.4 for arguments and method calls; 7.7 for sensor-guided components.

## 8.5  Choosing a telemetry strategy

*Where it lives:*  `PusherServo.PusherTelemetryStrategy` and
`PusherServo.update`.

The last argument in 8.3 is another lambda, with a different job.  Its
interface declares this method:

```java
        public void update(Servo servo, Telemetry telemetry);
```

The strategy receives the component's servo and the shared telemetry
object.  Billy's implementation adds a line labelled `"pusher position"`
whose value is `servo.getPosition()`.  The lambda chooses the label and
measurement; `PusherServo` decides when to call it.

```java
    @Override
    public void update() {
        if (telemetryStrategy != null) {
            telemetryStrategy.update(servoR, telemetry);
        } else {
            telemetry.addData("Right Pusher Position", "%.2f", servoR.getPosition());
        }
    }
```

If the constructor received a telemetry strategy, the component uses it.
If it received `null`, the component writes its default line instead.
This is a useful example of an optional strategy:  a caller can accept
the existing report or supply one appropriate to its robot.  The control
strategy is called separately, from `move`.

Both reporting paths add data to the telemetry buffer.  Neither calls
`telemetry.update()`.  The assembly gathers its component reports, then
the robot publishes the complete report once; see 10.6.  Choosing a
different label doesn't change who owns that final publication.

Also check what the reported value means.  `getPosition()` reports the
servo's commanded position.  That number alone doesn't confirm that the
pusher reached a ball or that a ball left the robot.  A separate sensor
would be needed to observe that physical event.  A useful report names
the quantity it can actually show.

> **Try it.**  Follow Billy's telemetry strategy from the constructor call
> in 8.3 to `update` above.  Which line runs when a strategy is present?
> Which line would run if the caller supplied `null`?

See also:  7.5 for component reports; 10.6 for the shared buffer and flush.

## 8.6  A decorator checks the request before forwarding it

*Where it lives:*  `DoublyLimitedMotor.setPower`.

A motor between two travel limits needs a rule that applies whichever
button controls it.  `DoublyLimitedMotor` owns the motor and two touch
sensors.  Its strategy calls this wrapper method:

```java
    public void setPower(double power){

        if ((power > 0 && !canGoForward() )|| (power < 0 && !canGoReverse())) {
            power = 0;
        }

        motor.setPower(power);
    }
```

Read the condition by direction.  A positive request is blocked when
`canGoForward()` is false.  A negative request is blocked when
`canGoReverse()` is false.  A blocked request becomes zero before the
underlying motor receives it.  An allowed request goes through with its
original power.

The wrapper's two permission methods read the corresponding touch
sensors.  The constructor documentation defines the forward sensor as
the one reached under positive power.  That physical setup matters:
software labels must match the installed motor direction and switches.

This is a **decorator**:  a wrapper adds behavior around an operation
and then delegates the operation to another object.  Here the added
behavior is the limit check, and the delegated operation is the motor's
`setPower`.  The wrapper adds a check every caller can use through the
same method.  Chapter 7 explains why these sensors belong with this motor.

The check occurs when `setPower` is called.  During a held-button control,
repeated calls can check the sensors again as the mechanism moves.
A movement started by one autonomous call needs ongoing limit checks
while it is active too.  A call at the start only observes the switches
at that moment.  When planning a sequence, identify which repeated update
will enforce that condition throughout travel; Chapter 12 explains
updates over time.

> **Predict.**  The forward switch is pressed and the reverse switch is
> free.  What happens to a negative request?  It can pass through:  the
> reverse condition permits movement away from the forward limit.

See also:  8.4 for passing the wrapper to a strategy; Chapter 12 for updates.

## 8.7  Put the decision where its information belongs

*Where it lives:*  `DoublyLimitedMotor`, `BillyMA`, and `BillyRobot`.

Compare three decisions on Billy.  A bounded motor checks its own travel
switches.  The assembly decides which mechanisms to ask for movement.
The robot connects the Limelight targeter, driver controls, and the
assembly's turret.  Each decision needs a different view of the robot.

Use that difference to choose a layer:

| Information the decision needs | Place to start looking |
|---|---|
| One device's configuration or a mechanism's own limit sensors | Component |
| A driver's mapping for a component in this assembly | Strategy supplied by the assembly |
| Coordination among several mechanisms | Assembly or a behavior given its assembly capabilities |
| Robot-wide sensors, driving, or cooperation among subsystems | Robot and the behaviors it connects |
| Match setup and the repeated competition loop | OpMode |

Start with the smallest object that has the required information.
`DoublyLimitedMotor` can decide whether positive power is permitted
because it owns the forward switch.  It doesn't need to know whether the
request came from a driver or from an autonomous routine.  Putting that
check in each driver's lambda would repeat a rule that belongs to the
mechanism.

An assembly has a broader view.  Billy's assembly tracks its intake,
pusher, flywheel, and turret.  Its strategy receives the set of claimed
parts and skips manual commands for those parts.  That lets a behavior
use the pusher and flywheel while other controls remain available.  The
part names and ownership rules belong to this arrangement of mechanisms;
they aren't details a generic servo wrapper needs.  Chapter 13 follows
the ownership checks.

At the robot layer, Billy constructs the camera targeter and passes it
the turret's autonomous behaviors.  The robot has the information needed
to connect those objects.  A pusher strategy doesn't need a camera
reference merely because both devices happen to be on Billy.

> **Argue it.**  A proposed control rule needs both a turret and a camera.
> What would each have to know if the rule were put inside the generic
> pusher component?  Name the actual objects the rule needs, then choose
> the layer that can connect them without adding unrelated hardware to
> that component.

See also:  Chapter 6 for the model; 10.8 for robot sensors; Chapter 13 for claims.

## 8.8  Pay for another strategy when a choice varies

*Where it lives:*  the constructor parameters of `SpinnyIntake` and
`PusherServo`.

Follow Billy's intake one last time.  Its hardware name selects a motor.
Its strategy selects how the driver controls that motor.  Its component
class supplies the common implementation.  Those constructor arguments
make reuse possible because a second instance can choose different
values for the choices that vary.

There is a cost.  A reader must connect an interface, a constructor
argument, a stored field, and a later call.  An interface name may also
appear in several files.  That work pays off when the same component is
used with different mappings, or when a report needs a different choice
of measurements.  The table in 8.7 helps decide which choices should be
passed in and which rules should belong to the component itself.

The two strategies in `PusherServo` show that you can vary different
jobs independently.  Changing the telemetry lambda changes what the
component reports.  Changing the control lambda changes the requested
position for each gamepad state.  Neither requires a second copy of
the hardware wrapper.  Keeping those jobs separate also gives a reader
a smaller piece of code to inspect for each question.

Before adding a strategy to a new class, write one sentence naming the
choice:  "This assembly chooses which button runs the motor," for
example.  Then name the information the rule needs.  If it needs a
gamepad and the motor, the intake's interface is a useful example.
If it needs local limit checks, give it the wrapper that owns those
checks, as in 8.4.  If it needs several mechanisms, reconsider the layer
before adding more device references to a component.

When reading an existing strategy, use the same questions.  Find the
choice, its inputs, and its caller.  You can understand a lambda a few
lines at a time without first learning every mechanism on the robot.

See also:  7.6 for reuse; Chapter 9 for building and directing assemblies.

## Chapter summary

A component keeps the hardware it obtained during construction.  A
control strategy supplies a rule for using it.  The assembly chooses
that rule; the component calls it with actual objects during an update.

| To find out... | Read... |
|---|---|
| Which buttons Billy uses | Its assembly's constructor lambdas |
| Which arguments a strategy accepts | The strategy interface |
| Which objects it receives | The strategy's call site |
| Where a travel check runs | The component wrapper method |
| Who publishes telemetry | `Robot.updateTelemetry`, after lower layers add data |

### Java in this chapter

- A functional interface has one abstract method.  A matching lambda can
  provide that method's implementation.
- Lambda parameters receive the arguments supplied at the later call.
- `this` refers to the object whose instance method is running.
- A `null` check can select a default when an optional strategy is absent.

### Vocabulary

**Control strategy.**  A supplied rule that translates inputs into commands.

**Decorator.**  An object that adds behavior around an operation it delegates.

**Functional interface.**  An interface with one abstract method, which a
matching lambda can implement.

**Strategy pattern.**  A design that separates a variable rule from the
object that calls it and lets that object receive an implementation.

### Check your understanding

1. Which file would you open to change Billy's intake buttons?  Which
   would you open to add an intake measurement?  (8.1)
2. What must a lambda match to implement the intake's interface?  (8.2)
3. What does Billy's pusher request when X is released?  (8.3)
4. What actual object receives the name `motor` inside the intake
   strategy?  How does the bounded motor's strategy differ?  (8.4)
5. If a telemetry strategy is supplied, does the default pusher report
   also run?  Who finally publishes the accumulated data?  (8.5)
6. When does the bounded motor check its switches?  What does that imply
   for a movement that continues over several loops?  (8.6)
7. Why does a camera-and-turret behavior need a broader view than a
   generic pusher component?  (8.7)
8. Name a choice that varies between two uses of one component.  What
   would its strategy need to receive?  (8.8)

## Where to go next

Chapter 9 puts the components together in an assembly.  Chapter 10 connects
the assembly to driving and robot sensors.  Chapter 11 follows a different
way to request actions from the same hardware:  autonomous verbs.



