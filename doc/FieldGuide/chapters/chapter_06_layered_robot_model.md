# Chapter 6 - The Layered Robot Model

*The same four layers, in the same order, on every robot.*

---

Every robot in this codebase is built the same way.  An OpMode sits on top.
Under it is a `Robot`, which owns a drive train and a group of mechanisms.
Under the group are the single mechanisms, each with its own hardware.  The
class names change from robot to robot, and the shape is the same every time.

This chapter is the map for Part 3.  Look here when you open a class and want
to know where it sits, what it's allowed to call, and which chapter goes deep
on it.  Look here, too, when you wonder why the team writes several classes
where an FTC sample program writes one line.

Read first:  Chapter 1, for classes, `extends`, `abstract`, and lambdas.
Chapter 3, for the hardware map and the devices it hands back.

| Entry | The question it answers |
|---|---|
| 6.1 | What are the layers? |
| 6.2 | What is each layer allowed to touch? |
| 6.3 | What does an OpMode do? |
| 6.4 | How does a button press reach a motor? |
| 6.5 | How does autonomous code reach a mechanism? |
| 6.6 | Why do TeleOp and autonomous get different methods? |
| 6.7 | How can two robots share one shape? |
| 6.8 | Which layer does this part belong in? |

---

## 6.1  The four layers

*Where it lives:*  `RobotModel/`

A **layer** is one level of the robot's code, with one job.  There are four
levels, and they always come in this order:

```
OpMode
  -> Robot
       -> DriveTrain
       -> MechAssembly
            -> MechComponent
            -> MechComponent
            -> MechComponent
```

Read it from the top.  Each arrow means "owns, and gives instructions to."
`DriveTrain` and `MechAssembly` share the third level, side by side, because a
robot owns one of each.

| Layer | Its one job | Goes deep in |
|---|---|---|
| OpMode | runs the program:  set up, wait for the start, loop until the stop | 6.3 |
| `Robot` | the whole robot:  one drive train, one mech assembly, and the sensors that describe the whole robot | Chapter 10 |
| `DriveTrain` | moves the chassis:  sticks in, wheel power out | 6.4 and 6.7 here.  Chapter 18 has the wheels. |
| `MechAssembly` | a group of mechanisms that work together, and the coordinating between them | Chapter 9 |
| `MechComponent` | exactly one mechanism and its hardware | Chapter 7 |

Three more chapters finish the picture.  Chapter 8 covers the button mapping
each component is handed.  Chapter 11 covers the named actions every layer
offers to autonomous code.  Chapter 13 covers who gets to command a mechanism
when two pieces of code both want it.

Here's Billy, drawn the same way.

```
BillyTeleOpBLUE
  -> BillyRobot
       -> MecanumDrive
       -> BillyMA
            -> SpinnyIntake
            -> PusherServo
            -> DoubleShooter
            -> Turret
```

Every name in that picture is a class, and every arrow is a field.  `Robot`
gives each robot two fields, one for each half of the third level:

```java
    protected DriveTrain driveTrain;
    protected MechAssembly mechAssembly;
```

`BillyRobot` fills them in its constructor.  The drive train is a
`MecanumDrive`, and the mech assembly is a `BillyMA`, Billy's own group of
mechanisms:

```java
        driveTrain = new MecanumDrive(hardwareMap, new MecanumDrive.OrientationConfiguration(
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.REVERSE,
                DcMotorSimple.Direction.FORWARD)
        );

        mechAssembly = new BillyMA(hardwareMap);
```

The four directions tell `MecanumDrive` which way each of Billy's wheel motors
faces (Chapter 3).  `BillyMA` gets the hardware map and builds its own four
components from it, so the components are the assembly's business.
`BillyRobot` talks to the assembly, and the assembly talks to them.

The four base classes are `abstract` (Chapter 1).  Each one is a list of what
a class at that layer has to provide, and a concrete robot fills it in.  The
compiler rejects `new Robot(...)`.  A subclass such as `BillyRobot` is what
gets built.

*See also:*  6.7, Chapter 9, Chapter 10

## 6.2  What each layer is allowed to touch

The rule has two halves.  Each layer talks only to the layer directly below
it.  And it talks in behaviors:  it asks for an action, and the layer below
decides which hardware carries it out, and how.

Watch it work on Billy's intake.  The intake motor's configuration name
(Chapter 1) is `"intakeMotor"`, and it appears in exactly one line of the
codebase, in `BillyMA`'s constructor:

```java
        intake = new SpinnyIntake(hardwareMap, "intakeMotor",
```

`BillyRobot` asks `BillyMA` for behavior.  The OpMode asks `BillyRobot`.  The
motor is invisible to both of them.  So when somebody moves the intake to a
different port, or renames it on the Driver Station, one line in one file
changes.

Here's the whole rule, layer by layer.

| Layer | Talks to | Hands off to the layer below |
|---|---|---|
| OpMode | its `Robot`:  `update`, `updateTelemetry`, `getAutonomousRobot` | everything with hardware in it |
| `Robot` | its drive train, its mech assembly, and its own robot-wide sensors | the motors and servos inside them |
| `DriveTrain` | its own wheel motors | it's the bottom of its branch |
| `MechAssembly` | its components | each component's hardware |
| `MechComponent` | its own hardware | it's the bottom of the stack |

Chapter 3 sets an FTC sample program beside this, one where the OpMode sets
a motor's power itself.  Both run a robot.  6.7 says what the team's way costs
and what it buys.

Billy has one place where a layer reaches further down.  The targeter, which
turns the turret toward an AprilTag (Chapter 5), needs two things:  the
Limelight camera, which `BillyRobot` owns, and the turret's named actions,
which live in a component two layers down.  `BillyRobot` builds the targeter
and hands it both:

```java
        targeter = new LimelightAutoTarget(
                limelight,
                ((BillyMA)mechAssembly).getAutonomousBehaviors().autonTurret,
                tagID);
```

The cost is that `BillyRobot` now knows `BillyMA` has a turret, and what kind.
Rename the turret field, or replace the turret with a different component,
and `BillyRobot` has to change too.  What it buys is a short path:  the
targeter talks straight to the turret's actions, and `BillyMA` needs no extra
method whose only job is passing them along.

> **Argue it.**  Should `BillyMA` offer an aiming action of its own, so the
> targeter asks the assembly and `BillyRobot` only ever talks to the layer
> below it?  Say what that adds to `BillyMA`, and what it saves the next
> person who changes the turret.

*See also:*  6.4, 6.5, Chapter 10, Chapter 13

## 6.3  OpMode:  the top of the stack

*Where it lives:*  `OpModes/BillyTeleOpBLUE.java`

An **OpMode** is a program the Driver Station can pick from its menu and run.
The FTC SDK supplies the base class.  The team's OpModes extend
`LinearOpMode`, which runs one method, `runOpMode()`, from top to bottom.

A match has two kinds of OpMode in it, because it has two periods.  The
**autonomous** period comes first:  for 30 seconds the robot runs on its own,
from a program.  Then comes **TeleOp**, two minutes when the drivers control
the robot with gamepads.  The game manual in `doc/game-manual/` has the
timing.

Here's all of Billy's blue-alliance TeleOp:

```java
@TeleOp(name="BillyBLUE")
public class BillyTeleOpBLUE extends LinearOpMode {

    // TODO: this season's BLUE-alliance target AprilTag ID goes here (last season's Limelight tag ID was removed during 2027 season prep).
    private static final int TARGET_TAG_ID = -1;

    Robot robot;
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new BillyRobot(hardwareMap, telemetry, TARGET_TAG_ID);

        telemetry.addLine("Billy Initialized");
        telemetry.update();

        waitForStart();
        while(opModeIsActive()){
            robot.update(gamepad1, gamepad2);
            robot.updateTelemetry();
        }
    }
```

`waitForStart()` splits it in two.

Everything above it runs when a driver presses INIT on the Driver Station.
`new BillyRobot(...)` runs Billy's constructor, and that builds every layer
below it:  the drive train, the assembly, and the assembly's four components,
each fetching its own devices from the hardware map.  Then one line of
telemetry goes to the Driver Station screen, so the drivers can see the robot
is ready.

Everything below it runs after the driver presses start.  The loop goes round
until the stop button, and each time round it makes two calls.
`robot.update(gamepad1, gamepad2)` hands both gamepads down to the robot, and
6.4 follows them the rest of the way.  `robot.updateTelemetry()` gathers what
every layer has to report and sends it to the screen, once a loop
(Chapter 10).

The `@TeleOp(name="BillyBLUE")` line puts the program on the Driver Station's
TeleOp menu under that name.  `BillyTeleOpRED` is the same program for the
other alliance.  Each has a `TODO` where this season's target AprilTag ID
goes, and Chapter 5 says what the targeter does with it.

That's the whole job.  The OpMode for `Wildbots2025` has the same loop, and a
comment above it says what the team is after:

```java
    /**
     * Imagine a world where this is all we need to have in this class~
     * @throws InterruptedException
     */
```

```java
        while (opModeIsActive()) {
            robot.update(gamepad1, gamepad2);
            robot.updateTelemetry();
        }
```

Billy's OpMode already lives in that world:  two calls a loop.

*See also:*  6.4, 6.6, Chapter 10, Chapter 12

## 6.4  How a button press reaches a motor

**Instructions flow down.**  Gamepad input comes in at the top, and each
layer hands it to the layer below, until a component turns it into a command
for one piece of hardware.

Follow one press all the way:  dpad up on `gamepad2`, the mechanism driver's
gamepad, which runs Billy's intake forward.

**OpMode to robot.**  The loop in 6.3 calls `robot.update(gamepad1,
gamepad2)`.

**Robot to its two halves.**  Here's `Robot`'s own version of `update`:

```java
    public void update(Gamepad gamepad1, Gamepad gamepad2)
    {
        if (driveTrain != null) driveTrain.drive(gamepad1);
        if (mechAssembly != null) mechAssembly.giveInstructions(gamepad2);
    }
```

`gamepad1` goes to the drive train and `gamepad2` goes to the mech assembly.
The `!= null` checks let a robot with only a drive train use the same method
(6.7).  `Wildbots2025` and both tank robots run this method exactly as `Robot`
wrote it.

Billy writes its own `update`, because it has more to do each loop.  It
watches the buttons that start and stop its rapid-fire sequence and its
targeter.  Its version still ends with the same two hand-offs:

```java
            driveTrain.drive(gamepad1);
            mech.giveInstructions(gamepad2, assistClaims());
            if (assist != null) {
                assist.update(gamepad1, gamepad2);
            }
```

The extra argument, `assistClaims()`, tells the assembly which mechanisms the
targeter is using this loop, so the assembly leaves those to it.  Chapter 10
covers Billy's `update`, and Chapter 13 covers claims.

**Assembly to components.**  `BillyMA`'s `giveInstructions` steps its
rapid-fire sequence, if one is running (Chapter 12).  Then it gathers up which
mechanisms are claimed this loop and runs its own strategy:

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

Each component gets the gamepad unless something else has claimed it this
loop.  With nothing claimed, all four of them `move`.

**Component to motor.**  `SpinnyIntake` hands its motor and the gamepad to
its control strategy:

```java
    @Override
    public void move(Gamepad gamepad)
    {
        strategy.nomNomNom(intake, gamepad);
    }
```

That strategy is a lambda (Chapter 1) that `BillyMA` wrote when it built the
intake:

```java
        intake = new SpinnyIntake(hardwareMap, "intakeMotor",
                (motor, gamepad) -> {
                    if (gamepad.dpad_up) {
                    motor.setPower(0.75); }
                    else if (gamepad.dpad_down) {
                    motor.setPower(-0.75);
                    } 
                    else {
                        motor.setPower(0);
                    }
                });
```

`dpad_up` is pressed, so the motor gets `0.75`.  The lambda is written in
`BillyMA` and runs inside `SpinnyIntake.move`, and Chapter 8 is about why.

That's four hand-offs for one button, and each one goes exactly one layer
down.

Telemetry takes the same road.  `robot.updateTelemetry()` asks the drive train
and the assembly for their lines, the assembly asks each component, and each
component writes its own.  Then `Robot` sends the lot to the screen, once.
Chapter 7 has the component's end of that, Chapter 9 the assembly's, and
Chapter 10 the send.

> **Watch out.**  Billy starts every OpMode with the targeter switched on,
> and the targeter claims the turret.  So the bumpers on `gamepad2` leave the
> turret alone until the driver presses `y` on `gamepad1`, which switches the
> targeter off.  Chapter 13 has the rest of Billy's buttons.

> **Try it.**  Open `BillyTeleOpBLUE.java` in Android Studio.  Put the cursor
> on `update` in `robot.update(gamepad1, gamepad2)` and use Go to
> Declaration (Ctrl+B).  You land in `Robot.update`, because the field is
> declared as a `Robot`.  Go back and use Go to Implementation (Ctrl+Alt+B)
> instead.  Android Studio lists `BillyRobot.update`, the one that runs when
> the robot is a Billy.  Keep going, a call at a time, until you reach the
> intake's lambda.

*See also:*  6.3, 6.5, Chapter 8, Chapter 9, Chapter 10

## 6.5  How autonomous code reaches a mechanism

Autonomous code has no gamepad to hand down.  It needs a different road, and
every layer builds a piece of it.  Each base class has a second class inside
it, an autonomous twin, and the twins line up the same way the layers do:

```
Robot.AutonomousRobot
  -> DriveTrain.AutonomousDriving
  -> MechAssembly.AutonomousMechBehaviors
       -> MechComponent.AutonomousComponentBehaviors
```

A twin is a small object with named actions on it, such as `startIntake()`
and `pushBalls()`.  Chapter 11 calls those actions autonomous verbs.

**Capabilities flow up.**  Each component builds its twin.  The assembly
gathers its components' twins into one object of its own.  The robot gathers
the drive train's twin and the assembly's twin into one more.  Here's
`BillyMA`'s, with a field for each of its four components' twins:

```java
    public class AutonomousBillyMA extends AutonomousMechBehaviors {
        public final SpinnyIntake.AutonomousIntakeBehaviors autonIntake;
        public final PusherServo.AutonomousBallPusherBehaviors autonBallPusher;
        public final DoubleShooter.AutonomousShooterBehavior autonFlywheel;
        public final Turret.AutonomousTurretBehaviors autonTurret;
```

And `BillyRobot`'s, with one for the drive train and one for the assembly:

```java
        public final MecanumDrive.AutonomousMecanumDrive driveTrain;
        public final BillyMA.AutonomousBillyMA mechAssembly;
```

So autonomous code can walk down from the top, one field at a time.  This is
from Billy's example autonomous program:

```java
                () -> robot.getAutonomousRobot()
                        .mechAssembly
                        .autonFlywheel
                        .setPower(0.6), 2200, pushBall());
```

Read the chain from the left.  `robot.getAutonomousRobot()` is Billy's
top-level twin.  `.mechAssembly` is `BillyMA`'s twin inside it.
`.autonFlywheel` is the flywheel's twin inside that.  `.setPower(0.6)` is one
of the flywheel's verbs.  Each step in that chain was built by a layer
contributing its piece.  Chapter 11 reads the chain more closely, and
Chapter 12 explains the `doAndWait` it sits in.

The twins get built from the bottom up, while the robot is being
constructed.  Each one needs the telemetry object, so it can report what it's
doing, and Chapter 7 covers the order that makes that work.

*See also:*  6.6, Chapter 7, Chapter 9, Chapter 11

## 6.6  Why TeleOp and autonomous get different methods

*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java`

Billy's intake has two ways in.  TeleOp code calls this one, every loop:

```java
    public void move(Gamepad gamepad)
```

Autonomous code calls verbs like this one, once, when it wants the intake
running:

```java
        public void startIntake()
        {
            intake.setPower(1);
            reportStatus("Intake: running");
        }
```

They're separate because they answer different questions.  TeleOp code asks
what the driver is doing right now.  Autonomous code asks what should happen
next.  So `move` takes the gamepad and reads it, and `startIntake` takes
nothing and says what it means.

> **Predict.**  Suppose the intake had only `move(Gamepad)`.  What would an
> autonomous program have to do to start it?  And what would happen to that
> program the day the team moved the intake from the dpad to the triggers?

It would have to build a pretend gamepad and press `dpad_up` on it.  That
ties the autonomous program to the button mapping, so moving the intake to
the triggers would quietly break autonomous too, in a program nobody had
touched.  With two methods, the mapping lives in one lambda (6.4), and
autonomous code calls `startIntake()` whatever the buttons are.

The names do work as well.  `startIntake()` says what you want.  A program
built from verbs like it reads like a list of steps a person could follow, and
Chapter 11 is about writing them that way.

Every layer keeps the two apart.  `MechComponent` has `move(Gamepad)` and a
component twin.  `MechAssembly` has `giveInstructions(Gamepad)` and an
assembly twin.  `DriveTrain` has `drive(Gamepad)` and a driving twin.  `Robot`
has `update(Gamepad, Gamepad)` and a robot twin.

*See also:*  6.5, Chapter 7, Chapter 11

## 6.7  Composition:  two robots, one shape

*Where it lives:*  `RobotModel/Robots/`

The team has four robot classes, and each one is a different set of parts
plugged into the same four layers.

| Robot | Drive train | Mech assembly |
|---|---|---|
| `BillyRobot` | `MecanumDrive` | `BillyMA` |
| `Wildbots2025` | `MecanumDrive` | `CascadeArm` |
| `OneStickTankRobot` | `OneStickTank` | `ExampleIntakeAssembly` |
| `StandardTankRobot` | `StandardTankDrive` | none |

Here's `Wildbots2025`'s constructor filling the same two fields Billy fills in
6.1:

```java
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
```

That's the drive train class Billy uses, built with this robot's own four
motor directions, beside a completely different set of mechanisms.

Building an object out of other objects, held in its fields, is called
**composition**.  `BillyRobot` is composed of a `MecanumDrive` and a
`BillyMA`.  `BillyMA` is composed of four components.  The team's design
document puts it in two lines:  inheritance defines the allowed shapes, and
composition builds the real machine (`doc/AbstractRobotObjectModel.md`).

`extends` (Chapter 1) is what says `BillyRobot` is a kind of `Robot`, and so
has to provide what every robot provides.  The fields are where the actual
drive train and mechanisms go.  A new robot next season is a new set of
parts, and the loop in 6.3 and the hand-offs in 6.4 work the same way for it.

This design costs something up front.  One button on Billy
passes through four classes before a motor moves.  A newcomer has to open
four files to follow it, where an FTC sample would show one.  What it buys is
reuse.  `MecanumDrive` runs two robots.  `SpinnyIntake` runs in two
assemblies, and so does `Claw`.  `CascadeArm` builds two `DoublyLimitedMotor`s
from one class.  Each of those classes is reused exactly as written (Chapter 7).
That's the trade-off:  complexity, for reusability.

*See also:*  6.1, 6.8, Chapter 7, Chapter 9, Chapter 21

## 6.8  Which layer does this part belong in?

Sort a part by what it touches and what it coordinates.  Its name can
mislead you, so ask these in order.

1. Does it decide when the program starts and stops?  **OpMode.**
2. Does it describe the whole robot, or does more than one subsystem want
   it?  **Robot.**
3. Does it move the chassis?  **Drive train.**
4. Does it coordinate two or more mechanisms?  **Mech assembly.**
5. Is it one mechanism and its own hardware?  **Component.**

Here are six parts, sorted that way.

| Part | Layer | Why, and where it is now |
|---|---|---|
| the code that waits for the start button | OpMode | `waitForStart()` in every OpMode (6.3) |
| a camera that sees AprilTags | Robot | it describes the whole robot's view of the field, and more than one subsystem could use it.  `BillyRobot` owns the Limelight. |
| the four drive motors | drive train | they move the chassis together.  `MecanumDrive` owns all four. |
| an arm and a claw that work together | mech assembly | that's two mechanisms, coordinated.  `CascadeArm` is one. |
| a claw that opens and closes | component | one mechanism, one servo.  `Claw` is one. |
| a lift with a switch at each end | component | one motor, guided by its own two switches.  `DoublyLimitedMotor` is one (Chapter 7). |

The last row is worth a second look.  The lift has three devices, and it's
still one mechanism, because the switches exist to guide that one motor.
Chapter 7 calls that the second kind of component.  Chapter 4 sorts sensors
by the same questions.

The IMU, the sensor inside the Control Hub that knows which way the robot
faces (Chapter 3), sorts to the robot layer by question 2.  On Billy,
`BillyRobot` sets it up.  `MecanumDrive` also fetches it for its own turning,
and a `TODO` in `MecanumDrive`'s constructor says how to give the IMU one
owner at the robot layer.  Chapter 4 covers it.

Chapter 8 asks the same questions about rules instead of parts:  which layer
should enforce a rule that protects one mechanism, and which one a rule that
coordinates several?

> **Try it.**  Open the `RobotModel` folder in Android Studio.  For each file,
> find the class it `extends`.  Each folder has one base class that extends
> nothing:  `MechComponent`, `MechAssembly`, `DriveTrain`, and `Robot`.  The
> other files in that folder extend it.  One more file in `Assemblies/`
> extends nothing.  Find it, and read Chapter 13 to see what it's for.

*See also:*  6.2, Chapter 4, Chapter 8 (8.7)

---

## Chapter summary

Every robot in this codebase has four layers, in one order:  an OpMode, a
`Robot`, a drive train and a mech assembly side by side, and the components
at the bottom.  Each layer has one job and talks only to the layer below it,
in behaviors.  Gamepad instructions flow down through every layer to the
hardware.  Autonomous capabilities flow up, as each layer gathers the verbs
of the layer below into a twin of its own.  A new robot is a new set of parts
plugged into the same shape.

| On Billy | It's an example of |
|---|---|
| `BillyTeleOpBLUE`, two calls a loop | an **OpMode** that talks only to its robot |
| `robot.update` to `BillyMA` to `SpinnyIntake.move` to the lambda | **instructions flowing down** |
| `robot.getAutonomousRobot().mechAssembly.autonFlywheel` | **capabilities flowing up** |
| `move(Gamepad)` beside `startIntake()` | separate **TeleOp** and **autonomous** methods |
| `BillyRobot` and `Wildbots2025`, both on `MecanumDrive` | **composition** |
| the targeter handed `autonTurret` | a layer reaching two layers down, on purpose |

### Vocabulary

**autonomous** -- the opening period of a match, when the robot runs on its own,
from a program.  Also the code written for it.  (6.3)

**capabilities flow up** -- each layer gathers the autonomous verbs of the
layer below into an object of its own, so autonomous code can reach any
mechanism from the top.  (6.5)

**composition** -- building an object out of other objects held in its
fields.  `BillyRobot` is composed of a `MecanumDrive` and a `BillyMA`.  (6.7)

**DriveTrain** -- the layer that moves the chassis, turning sticks into wheel
power.  `MecanumDrive` is one.  (6.1)

**instructions flow down** -- gamepad input enters at the OpMode, and each
layer hands it to the layer below until a component commands hardware.  (6.4)

**layer** -- one level of the robot's code, with one job.  There are four.
(6.1)

**MechAssembly** -- the layer that owns a group of mechanisms and coordinates
them.  `BillyMA` is one.  (6.1)

**MechComponent** -- the layer that owns exactly one mechanism and its
hardware.  `SpinnyIntake` is one.  (6.1)

**OpMode** -- a program the Driver Station can pick and run.  It builds the
robot and calls it every loop.  (6.3)

**Robot** -- the layer that owns the whole robot:  one drive train, one mech
assembly, and robot-wide sensors.  (6.1)

**TeleOp** -- the period of a match when drivers control the robot with
gamepads.  Also the code written for it.  (6.3)

### Check your understanding

1. Draw the four layers from memory, then the autonomous twins beside them.
   Write one sentence for each layer saying what it does.  (6.1, 6.5)
2. The string `"intakeMotor"` appears in one file.  Which one, and why is
   that the right file?  (6.2)
3. Billy writes its own `update`, and `Wildbots2025` doesn't.  What do both
   versions do with the two gamepads?  (6.4)
4. An autonomous program calls `startIntake()`.  Why is that better than
   handing `move` a pretend gamepad?  (6.6)
5. Sort these into layers:  a beam-break sensor that sees a ball pass from
   the intake to the pusher;  a wheel motor;  a servo that flips a gate open.
   Say which question in 6.8 decided each one.  (6.8)
6. Name one class that runs in two different robots or assemblies, unchanged.
   What did each one hand it to make it theirs?  (6.7)

## Where to go next

Each layer has a chapter of its own.  Chapter 7 is the component, the bottom
of the stack.  Chapter 8 is the control strategy each component is handed.
Chapter 9 is the assembly, Chapter 10 is the robot, and Chapter 11 is the
autonomous verbs that flow up through all of them.

Behavior over time comes after that.  Chapter 12 is the state machines that
run sequences like rapid fire, and Chapter 13 is how the robot decides who
controls a mechanism when two pieces of code both want it.
