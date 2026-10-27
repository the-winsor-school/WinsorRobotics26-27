# Chapter 21 - Designing a Robot, End to End

*Design it top-down.  Build it bottom-up.*

---

This chapter puts every layer of the book to work at once.  It's about a new
robot, or a new mechanism on an old one, and the order you decide things in.

The method fits in one line:  design top-down, build bottom-up.  You decide
what the robot has to do before you write a class, and then you write the
smallest classes first.

Look here when the team starts a new season's robot, when somebody proposes a
new mechanism, or when a design feels finished and you want to check it.  21.9
works one new mechanism all the way through, from the game task to the
checklist.

Read first:  Chapter 6, for the layers.  Chapters 7 to 13 explain each layer
this chapter puts together.

| Entry | The question it answers |
|---|---|
| 21.1 | Where do you start on a new robot? |
| 21.2 | What has to be decided before any code? |
| 21.3 | Which file gets written first, and which gets tested first? |
| 21.4 | What do the templates give you, and what do they leave out? |
| 21.5 | What is dependency injection? |
| 21.6 | Which design patterns does a robot use? |
| 21.7 | How do you know a design is done? |
| 21.8 | What are the seven questions for a new mechanism? |
| 21.9 | What does a full design pass look like? |

---

## 21.1  Top-down design, bottom-up building

*Where it lives:*  `doc/NewRobotDesignWorkflow.md`

Starting with the OpMode feels fastest.  Open a new TeleOp, fetch a motor by
name, read a button, and something on the robot moves inside ten minutes.  For
a quick test, that's fine.

As a way to build a robot, it costs you later.  The gamepad checks, the
hardware names and the timing all pile up in one file, at the top layer, where
no other robot can use them.  When the second mechanism arrives, its buttons go
in the same file and start colliding with the first one's.

So the team designs in one direction and builds in the other.

**Top-down design** means deciding what the robot must do before writing any
code:  which game tasks it handles, which subsystems exist, and which behaviors
belong at each layer.  You start with the whole robot and work down to the
single mechanism.

**Bottom-up building** means writing the smallest pieces first.  Components
come first (Chapter 7), then the assemblies that group them (Chapter 9), then
the robot that wires the subsystems together (Chapter 10), and the OpModes and
autonomous strategies last.

The team's guide calls these the two passes.

1. **The design pass.**  Decide the subsystems, what each one is responsible
   for, what the drivers do, and which autonomous verbs each mechanism offers.
2. **The implementation pass.**  Build the components, then the assemblies,
   then the robot, then the OpModes and strategies.

Here's where the building ends up.  This is the body of Billy's TeleOp OpMode,
from `OpModes/BillyTeleOpBLUE.java`:

```java
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

Read it for what's missing.  It has no mechanism logic, no gamepad checks, and
no hardware names.  It builds a robot, waits for the start, and calls two
methods every loop until the match ends.  An OpMode is the last wiring layer
(Chapter 6), and the design pass finds a lower home for everything else.

> **Watch out.**  When an OpMode starts collecting `if (gamepad2.x)` checks or
> `hardwareMap.get` calls, the design has leaked past a boundary.  Find the
> layer that logic belongs to and move it down.

*See also:*  21.3, Chapter 6, Chapter 10

## 21.2  What to decide before any code

*Where it lives:*  `doc/NewRobotDesignWorkflow.md`

A new robot starts with seven questions, answered in this order.  Each answer
feeds the next one, and each points at the layer that will own it.

| # | The question | It decides | Chapter |
|---|---|---|---|
| 1 | What game tasks must the robot perform? | what the whole design is for | |
| 2 | What mechanisms does each task need? | the physical parts | 20 |
| 3 | Which mechanisms are separate components? | one class per device, or per device and its sensors | 7 |
| 4 | Which components belong in one assembly? | which parts get coordinated together | 9 |
| 5 | Which actions exist in TeleOp, and which in autonomous? | the gamepad strategies, and the autonomous verbs | 8, 11 |
| 6 | Which sensors describe the whole robot, and which guide one mechanism? | where each sensor lives | 4, 10 |
| 7 | Which behaviors span several components, or need a robot-wide sensor plus a mechanism? | the assembly and robot strategies | 13 |

Billy's answers are a good model, because every one of them is visible in the
code.

- **Tasks:**  pick up balls and shoot them at an AprilTag target.
- **Mechanisms:**  a mecanum drive base, and a mechanism assembly with an
  intake, a ball pusher, a flywheel and a turret.
- **Components:**  `SpinnyIntake`, `PusherServo`, `DoubleShooter` and
  `Turret`, grouped in `BillyMA`.
- **Robot-wide sensors:**  the Limelight and the IMU, owned by `BillyRobot`.
- **Behaviors that span parts:**  rapid fire works the pusher and the
  flywheel together, so it belongs to the assembly.  Automatic aiming needs the
  Limelight and the turret, so it belongs to the robot.

Then lock the names.  Every motor, servo and sensor is reached by its
configuration name (Chapter 1), a string that has to match the Driver Station
configuration exactly (Chapter 3).  Decide them before anyone types one.

| Billy's device | Configuration name | Fetched in |
|---|---|---|
| drive motors | `"lf"`, `"lb"`, `"rf"`, `"rb"` | `MecanumDrive` |
| intake motor | `"intakeMotor"` | `BillyMA`, through `SpinnyIntake` |
| pusher servo | `"ballPusherServo"` | `BillyMA`, through `PusherServo` |
| flywheel motors | `"flywheelMotorF"`, `"flywheelMotorB"` | `BillyMA`, through `DoubleShooter` |
| turret servo | `"turretServo"` | `BillyMA`, through `Turret` |
| Limelight | `"limelight"` | `BillyRobot` |
| IMU | `"imu"` | `BillyRobot`, and `MecanumDrive` (22.4) |

The same pass settles the rest of the setup:  which way each drive motor
turns, how the Control Hub is mounted (so the IMU knows which way is up), and
anything that changes with the alliance or the side of the field.  21.5 shows
how Billy passes each of those in, so one robot class serves every match.

> **Try it.**  Open the Driver Station's active configuration for Billy and
> compare it against the table above.  Every name should match, letter for
> letter and in the same case.  A name that differs will stop the OpMode at
> `hardwareMap.get` during initialization.

*See also:*  21.5, 21.8, Chapter 3, Chapter 20

## 21.3  The build order, and testing in the same order

*Where it lives:*  `doc/NewRobotDesignWorkflow.md`

The **build order** is the order the files of a new robot get written in.  It
follows the folders of the codebase from the bottom up:

```
1. RobotModel/Mechs/Components/...
2. RobotModel/Mechs/Assemblies/...
3. RobotModel/DriveTrain/...   (only if a new drive style is needed)
4. RobotModel/Robots/...
5. AutonStrategies/...
6. OpModes/...
```

The order is fixed, because it follows what depends on what.  A component
depends on nothing else in the model.  An assembly builds components in its
constructor.  A robot builds a drive train and an assembly.  An autonomous
strategy calls the robot's autonomous verbs (Chapter 11).  An OpMode builds a
finished robot, and an autonomous OpMode runs a finished strategy too.

> **Predict.**  Suppose you write the robot class first, before its assembly
> exists.  What happens when you build?

The compiler rejects it.  The robot's constructor says `new NewRobotMA(...)`,
and there's no such class yet.  You can write a placeholder to quiet it, but
then you're writing against a shape you've guessed at, and every guess that's
wrong gets rewritten once the real assembly arrives.  Built in order, each
file is written against classes that already exist and already work.

Testing runs in the same direction.  The team's guide gives seven steps:  each
component's hardware (does the motor turn the right way, do the limits stop
it?), then its TeleOp strategy, then its autonomous verbs, then the assembly's
coordination, robot construction and sensor setup, the TeleOp flow with a
driver, and the autonomous strategy last.

A problem found at step 1 is a problem in one class.  The same problem found at
step 6 could be in any of a dozen classes, and the first job is working out
which.

So when something goes wrong, ask which layer is failing.

| The symptom | Usually a problem in |
|---|---|
| a motor spins the wrong way | the component, or the configuration (Chapter 3) |
| two mechanisms fight each other | the assembly (Chapter 9, Chapter 13) |
| the robot runs the wrong routine, or aims at the wrong tag | the OpMode, or the robot's construction |

*See also:*  21.1, 21.4, Chapter 22

## 21.4  Starting from the templates

*Where it lives:*  `doc/NewRobotDesignWorkflow.md`

The team's design guide ends with five templates:  a component, an assembly,
a robot, a thin TeleOp OpMode, and an autonomous strategy.  A **template** is
a starting shape with the blanks marked.  Some of its lines are placeholders,
and the compiler rejects them until you fill them in with real hardware and
real names.

Here is the component template, as the guide has it:

```
public class NewComponent extends MechComponent {
    public class AutonomousNewComponent extends AutonomousComponentBehaviors {
        public AutonomousNewComponent(Telemetry telemetry) { super(telemetry); }
        public void stow()  { /* command hardware */ reportStatus("New: stowed"); }
        public void score() { /* command hardware */ reportStatus("New: scoring"); }
    }

    public interface NewComponentControlStrategy extends IControlStrategy {
        void move(/* hardware */, Gamepad gamepad);
    }

    private final NewComponentControlStrategy strategy;

    // Built in initializeTelemetry -- see doc/TelemetryContract.md.
    private AutonomousNewComponent auton;

    public NewComponent(HardwareMap hardwareMap, NewComponentControlStrategy strategy) {
        super(strategy);
        this.strategy = strategy;
        // get hardware here -- hardware alone, on purpose
    }

    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousNewComponent(telemetry);
    }

    @Override
    public AutonomousNewComponent getAutonomousBehaviors() {
        return auton;
    }

    @Override
    public void move(Gamepad gamepad) {
        // call strategy here
    }

    @Override
    public void update() {
        // write to the buffer with telemetry.addData(...);  Robot does the flushing
    }
}
```

Checked against `MechComponent` as it is today, the template is complete.
The base class has three abstract methods, `getAutonomousBehaviors()`,
`move(Gamepad)` and `update()`, and the template overrides all three.  It
overrides `initializeTelemetry` too, which is where the autonomous object gets
built (two-phase construction, Chapter 7).  Its constructor hands the strategy
to `super(strategy)`, the way every real component does.

The placeholder to fill first is the strategy method's first parameter,
`/* hardware */`.  It becomes whatever the component owns.  `SpinnyIntake`,
which owns one motor, fills it in like this:

```java
        void nomNomNom(DcMotor motor, Gamepad gamepad);
```

The assembly template has the four methods `MechAssembly` requires:
`initializeTelemetry`, `getAutonomousBehaviors`, `giveInstructions` and
`updateTelemetry`.  The robot template builds things in the order Chapter 10
explains:  `super(telemetry)` first, then the drive train, the assembly and
any robot-wide sensors, then `initializeSubsystems()`, and only after that the
autonomous object, gathered from the subsystems' own.  Copy that order exactly.

Two things are worth knowing before you copy them.

The robot template has no `update()` method.  A robot built from it runs
`Robot.update`, the base class's default, which sends the first gamepad to the
drive train and the second to the assembly.  That's the right start.  21.6
says when a robot replaces it.

The templates were written before the team added declared strategies at the
assembly and robot layers.  `MechAssembly` now also carries an interface for a
macro (Chapter 13), a sequence that takes over some of an assembly's parts
for a while:

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

`Robot` has a matching interface, `IAssistRobotStrategy<R>`, for an assist:  a
robot-layer helper, like automatic aiming.  `MacroManager<R>` runs one macro
at a time for any assembly.  A robot with
no macros and no assists can leave all of this alone.  A robot that wants
them copies the pattern from `BillyMA` and `BillyRobot`, which Chapter 13
walks through.

*See also:*  21.3, 21.6, Chapter 7, Chapter 9, Chapter 10, Chapter 13

## 21.5  Dependency injection

*Where it lives:*  `RobotModel/Robots/BillyRobot.java`

Billy plays on both alliances, and each alliance aims at a different
AprilTag.  The team could have written two robot classes, one per alliance,
identical except for one number.  Every fix would then be owed twice.

Billy has one robot class, and the number comes in through its constructor:

```java
    public BillyRobot(HardwareMap hardwareMap, Telemetry telemetry, int tagID) {
```

The OpMode decides the number and hands it over.  This is from
`OpModes/BillyTeleOpBLUE.java`:

```java
    private static final int TARGET_TAG_ID = -1;

    Robot robot;
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new BillyRobot(hardwareMap, telemetry, TARGET_TAG_ID);
```

`BillyTeleOpRED` is the same file with a different name at the top and a
different `TODO` beside the number.  Both numbers are `-1` for now, and the
`TODO` above each one asks for this season's tag.

That move has a name.  **Dependency injection** means handing an object the
things it depends on through its constructor, so the object uses what it's
given and the code that builds it decides what that is.  Billy's constructor
gets three things this way:  the `HardwareMap`, the `Telemetry` and the tag
ID.  It passes each subsystem what that subsystem needs in turn.

The same idea runs all through the model.

- `MecanumDrive` takes an `OrientationConfiguration`, which says which way
  each drive motor turns.  Billy and `Wildbots2025` pass different directions
  to the same drive class.
- `LimelightAutoTarget` is handed the camera, the turret's autonomous verbs,
  and the tag to look for.  It fetches nothing for itself:

```java
        targeter = new LimelightAutoTarget(
                limelight,
                ((BillyMA)mechAssembly).getAutonomousBehaviors().autonTurret,
                tagID);
```

Injection has a cost.  Constructors grow longer, and the robot class has more
wiring to do.  What it buys is reuse:  one class serves both alliances, both
robots, and the next robot too.  That's the trade-off between complexity and
reusability.

It also settles who owns a shared device.  The IMU is the place the codebase
is still working this out.  `MecanumDrive` fetches the IMU for itself, and so
does `BillyRobot`.  Both run this line:

```java
        imu = hardwareMap.get(IMU.class, "imu");
```

The `TODO` in `MecanumDrive`'s constructor names the next step as injection:
*"Take the IMU as a constructor parameter here rather than fetching it - the
same way OrientationConfiguration already passes in what varies per robot."*
The robot would fetch it, set it up once, and pass it down.  22.4 explains why
that matters.

*See also:*  21.2, 22.4, Chapter 3, Chapter 10

## 21.6  The patterns, named

By the end of a design pass you've used five or six named design patterns,
whether or not anybody said their names.  Each one has its own chapter.

| Pattern | What it does here | Where to see it | Chapter |
|---|---|---|---|
| Strategy | the behavior gets handed in from outside | every component's gamepad lambda;  `BillyRapidFire` and `LimelightAutoTarget` as declared macro and assist strategies | 8, 13 |
| Composite | a group answers the same calls as its parts | `BillyMA`, `CascadeArm`, `ExampleIntakeAssembly` | 9 |
| Facade | one simple surface over many subsystems | every `Robot` | 10 |
| Template method | the base class fixes the order of a job, and subclasses fill in steps | `Robot.update` and `Robot.updateTelemetry` | 10 |
| Dependency injection | an object is handed what it depends on | the tag ID, the orientation, the camera | 21.5 |
| State machine | a behavior that moves through steps over time without blocking the loop | `StateMachine` and `IState`, used by `BillyRapidFire` and `LimelightAutoTarget` | 12 |

The template method needs a closer look, because the robots use it two
ways.  Here is the default in `Robot`:

```java
    public void update(Gamepad gamepad1, Gamepad gamepad2)
    {
        if (driveTrain != null) driveTrain.drive(gamepad1);
        if (mechAssembly != null) mechAssembly.giveInstructions(gamepad2);
    }
```

`StandardTankRobot`, `OneStickTankRobot` and `Wildbots2025` leave it exactly
as it is.  Billy replaces it:

```java
    @Override
    public void update(Gamepad gamepad1, Gamepad gamepad2) {
       strategy.execute(this, gamepad1, gamepad2);
    }
```

Billy's strategy still calls `driveTrain.drive(gamepad1)` and gives the
assembly `gamepad2`.  Before it does, it handles the buttons that start and
stop rapid fire and automatic aiming, and it tells the assembly which parts an
assist has claimed (Chapter 13).  A robot whose loop has to referee between
behaviors writes its own `update()`.  A robot that only drives and works its
mechanisms keeps the default.

*See also:*  21.4, 21.5, Chapter 8, Chapter 9, Chapter 10, Chapter 12, Chapter 13

## 21.7  The design checklist

The **design checklist** is eight questions to ask before calling a robot
design done.  Each one should get a yes.

| # | Ask | A yes looks like | Chapter |
|---|---|---|---|
| 1 | Does every physical mechanism have one clear owner in the code? | each motor, servo and sensor is fetched in exactly one class | 4, 7 |
| 2 | Does each component hide its hardware behind meaningful methods? | callers use `move`, `update` and verbs, and the hardware field is `private` | 7 |
| 3 | Do the autonomous verbs describe intent? | `startIntake()` and `pushBalls()`, in place of raw power numbers | 11 |
| 4 | Does the assembly settle conflicts between its parts in one place? | one strategy decides which parts take gamepad orders this loop | 9, 13 |
| 5 | Is the robot class only about the whole robot? | it composes subsystems, owns robot-wide sensors, and referees robot-wide behaviors | 10 |
| 6 | Is the OpMode thin? | it builds a robot and calls `update` and `updateTelemetry` | 6 |
| 7 | Does autonomous code use verbs instead of hardware? | it reaches mechanisms through `getAutonomousRobot()` | 11 |
| 8 | Can each layer be tested on its own? | the seven test steps in 21.3 each make sense for this robot | 21.3 |

Question 4 used to be a promise.  Today Billy has the machinery to keep it.
Each of Billy's parts has a name in the `BillyMA.Part` list, every macro and
assist declares which parts it claims, and the assembly's default strategy
skips any part that's claimed:

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

That's what "one place" looks like.  Rapid fire claims the pusher and the
flywheel, automatic aiming claims the turret, and this one lambda is where the
gamepad gets told to keep its hands off.  The four component strategies read
the same whether rapid fire is running or not.  Chapter 13 explains claims,
`MacroManager`, and what happens when two behaviors want the same part.

When several answers come back no, the design is missing a boundary between
layers.  Go back to the chapter for that layer, and move the logic to where it
belongs.

*See also:*  21.3, 21.9, Chapter 13

## 21.8  The seven questions for a new mechanism

21.2 asks seven questions about a whole robot.  A single new mechanism gets
seven of its own, and they run in this order, because each answer needs the
one before it.  Write the answers down.  A full, defensible set of seven is a
finished design, whether or not any Java exists yet.

| # | The question | A good answer covers |
|---|---|---|
| 1 | What game task does this serve? | the task in one sentence, and how you'll know the mechanism is doing it |
| 2 | What components does it break into, and what hardware does each own? | each motor, servo and sensor, with its configuration name;  whether an existing component already wraps that device (reuse first, Chapter 7) |
| 3 | What is each component's strategy interface, and which verbs does it expose? | the strategy method's parameters;  the gamepad buttons, checked against what's taken;  verbs named for intent |
| 4 | Does it join an existing assembly or need a new one, and does it conflict with anything? | the assembly, and why;  any rule that involves two components;  any sequence that takes over parts, and which parts it would claim |
| 5 | What does the robot class have to wire up, and does `update()` change? | new sensors at the robot layer, if any;  whether the robot keeps `Robot.update` or needs its own |
| 6 | What does one autonomous routine using it look like? | the verbs in order;  a one-shot routine or a state machine, and why (Chapter 12) |
| 7 | How does the design do on the checklist? | all eight questions of 21.7, answered honestly, with the noes named |

Three of these trip people up.

Question 2 has a reuse check in it.  Before you write a new component, look in
`RobotModel/Mechs/Components/` for one that already wraps the same kind of
device.  The rule on `MechComponent` says one wrapper per device type is
enough, and a second wrapper named for one robot's job is duplication.

Question 4 is where conflicts get found on paper, where they're cheap.  If the
new mechanism can bump into another one, or a sequence needs several parts at
once, the assembly owns that rule (Chapter 13).

Question 6 is about time.  A routine an OpMode runs once and waits on can call
verbs and pause between them.  A behavior that has to run while the drivers
are still driving needs a state machine, so the loop keeps turning (Chapter
12).

*See also:*  21.2, 21.7, 21.9

## 21.9  Worked once:  a wrist for the cascade arm

*Where it lives:*  `RobotModel/Mechs/Assemblies/CascadeArm.java`

Here is one design pass, start to finish, for a mechanism the team doesn't
have:  a wrist that tilts `Wildbots2025`'s claw.  The wrist is made up for
this entry, and so are its names.  Every class it touches is real.

**1.  The task.**  Suppose the claw has to hold a game piece flat to pick it up
off the floor, and tilted to place it up high.  The wrist does the tilting.
You'll know it works when a placed piece sits where it was aimed.

**2.  Components and hardware.**  A wrist that moves between set angles is one
positional `Servo` (Chapter 3), configured as, say, `"wristServo"`.  Then the
reuse check.  `PusherServo` already wraps one `Servo`, but it's named for its
job on Billy, its verbs are `pushBalls()` and `retractPusher()`, and its
constructor reverses the servo's direction.  Reuse it, and autonomous code
would tilt the wrist by calling `pushBalls()`.  Write a second wrapper, and the
rule on `MechComponent` calls it duplication.  The `TODO` on `MechComponent`
says the naming convention for single-device wrappers is still an open
question.

> **Argue it.**  Should the wrist reuse `PusherServo`, or should the team write
> one servo wrapper named for the device, and move Billy's pusher onto it too?
> The second costs a rename and changes to `BillyMA`.  What does it buy the
> next robot?

**3.  Strategy and verbs.**  The strategy takes the servo and the gamepad, the
same shape as `PusherServo`'s.  For buttons, check what `CascadeArm` already
uses on the mechanism gamepad:  the left stick runs the lift, the right stick
the drawbridge, and `a` and `b` the claw.  `x` and `y` are free.  The verbs are
the template's own, `stow()` and `score()`, which say what the wrist is for.
Here's a made-up sketch of the strategy, for a `WristServo` class that doesn't
exist:

```
wrist = new WristServo(hardwareMap, "wristServo",
        (servo, gamepad) -> {
            if (gamepad.x)
                servo.setPosition(WristServo.SCORE);
            else if (gamepad.y)
                servo.setPosition(WristServo.STOW);
        });
```

The two positions are named constants, and the verbs use the same ones.  22.3
says why.

**4.  The assembly, and conflicts.**  The wrist rides on the lift beside the
claw, so it joins `CascadeArm`.  Now suppose a tilted wrist would hit the frame
on the way down.  That's a rule about two components, the wrist and the lift,
so it belongs to the assembly.  "Stow the wrist, then lower the lift" is a
sequence that takes over both parts for a moment, which makes it a macro that
claims them (Chapter 13).  `CascadeArm` has no parts list and no
`MacroManager` yet, so the design adds them by copying `BillyMA`:  a `Part`
list, a `MacroManager<Part>`, and a default strategy that skips claimed parts.
`MacroManager<R>` works as it is.  This is the largest piece of work in the
design, and it's better found here than at the robot.

**5.  The robot.**  `Wildbots2025` builds `new CascadeArm(hardwareMap)`, and the
wrist arrives inside it.  `AutonomousCascadeArm` gains a `wrist` field.  The
robot keeps `Robot.update`, because the assembly can read its own macro button
in `giveInstructions`.  Billy wrote its own `update()` to referee assists
against macros (21.6), and this robot has no assists to referee.

**6.  One autonomous routine.**  Raise the lift, tilt the wrist to score, open
the claw, stow the wrist, lower the lift.  An OpMode runs this once and waits
on it, so a one-shot routine that calls verbs and pauses between them fits,
like `ExampleAutonomousStrategies.MecanumAutonDance`.  The stow-then-lower
macro is different:  it runs during TeleOp, while the drivers keep driving, so
it's a state machine built with `doAndWait` (Chapter 12), like
`BillyRapidFire`.

> **Watch out.**  Two facts about this robot shape the routine.  The lift's
> limit check runs each time `setPower` is called (Chapter 19), so a timed lift
> move in autonomous should stop the lift itself.  And `Wildbots2025`'s IMU has
> no setup yet (22.4), so leave `turnToAngle` out of its routines until the IMU
> `TODO` in `MecanumDrive` is done.

**7.  The checklist.**  Run 21.7 honestly.  One owner for the wrist servo:
yes.  Hardware hidden, verbs named for intent:  yes.  Conflicts settled in one
place:  yes, once the macro and its claims exist, and no until then.  Robot
class unchanged, OpMode unchanged:  yes.  Testable layer by layer:  servo
direction, strategy, verbs, the macro, then the whole arm.  The open item is
question 2's reuse decision, and the team should settle it before anyone
writes the class.

*See also:*  21.7, 21.8, 22.3, Chapter 13, Chapter 19

---

## Chapter summary

Decide what the robot must do from the top down, and build it from the bottom
up.  The design pass answers seven questions and locks the configuration
names.  The build order follows dependency, components first and OpModes last,
and testing climbs the same ladder.  The templates give you the shapes, and
dependency injection lets one class serve many robots.  The checklist says
when a design is done.

| Idea | Where |
|---|---|
| design top-down, build bottom-up, and the OpMode comes last | 21.1 |
| seven questions and the configuration names, before any code | 21.2 |
| the build order follows dependency;  test in the same order | 21.3 |
| the templates match today's base classes and predate macros and assists | 21.4 |
| hand an object what it depends on | 21.5 |
| Billy replaces `Robot.update`;  the other robots keep it | 21.6 |
| conflicts settled in one place, by claims | 21.7 |
| one mechanism, seven questions, worked through | 21.8, 21.9 |

### Java in this chapter

| Syntax | Where |
|---|---|
| a constructor parameter carrying configuration, `int tagID` | 21.5 |
| `public interface IMacroAssemblyStrategy<R>`, an interface with a type parameter | 21.4.  Generics are in Chapter 7. |
| `@Override public void update(...)` replacing a base-class method | 21.6.  `@Override` is in Chapter 1. |

### Vocabulary

**bottom-up building** -- writing the smallest classes first:  components,
then assemblies, then the robot, then OpModes and autonomous strategies.  (21.1)

**build order** -- the fixed order the files of a new robot are written in,
following what depends on what.  (21.3)

**dependency injection** -- handing an object the things it depends on
through its constructor, so the code that builds it decides what they are.
(21.5)

**design checklist** -- eight questions a finished robot design should answer
yes.  (21.7)

**template** -- a starting shape for a class, with its blanks marked.  The
team's are in `doc/NewRobotDesignWorkflow.md`.  (21.4)

**top-down design** -- deciding what the robot must do, which subsystems
exist, and which behaviors belong at each layer, before writing code.  (21.1)

### Check your understanding

1. Name two things that go wrong when a new robot starts with its OpMode.
   (21.1)
2. Which of the seven design questions decides where a sensor lives?  Where do
   Billy's two robot-wide sensors live?  (21.2)
3. Why can't the robot class be written before its assembly?  (21.3)
4. A motor spins the wrong way in TeleOp.  Which layer do you check first?
   (21.3)
5. The robot template has no `update()` method.  What runs instead, and what
   does it do?  (21.4, 21.6)
6. `BillyTeleOpRED` and `BillyTeleOpBLUE` share one robot class.  What makes
   that possible, and what does it cost?  (21.5)
7. Why does Billy write its own `update()` while `Wildbots2025` keeps the
   default?  (21.6)
8. Pick a mechanism of your own and answer the seven questions in 21.8.  Then
   run the checklist and name every no.  (21.8, 21.9)

## Where to go next

Every layer this chapter puts together has its own chapter:  components in 7,
strategies in 8, assemblies in 9, the robot in 10, autonomous verbs in 11.
State machines are Chapter 12, and claims and macros are Chapter 13.  Chapter
20 shows how to break a kit robot into these same pieces.  Chapter 22 is the
other half of the job:  the bug shapes to look for once the design is built.
