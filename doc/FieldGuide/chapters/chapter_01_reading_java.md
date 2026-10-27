# Chapter 1 - Reading Java in This Codebase

*What it knows, what it does, and which branch runs.*

---

Every file in the robot's code is Java, and most of it is built from a short
list of patterns.  Learn to read those patterns and you can open almost any
file in the repository and say what it does, well before you've written a
class of your own.

This chapter is a reading guide.  Every example in it is real code from the
team's repository, the same code that runs on Billy.  It covers the Java you'll
actually meet here and leaves the rest for later.  Writing new classes comes in
Part 3.

Look here when a word in another chapter stops you:  `final`, `extends`,
`@Override`, an arrow `->`, a `null`.  Each one has an entry or an Aside below,
under its own name.

| Entry | The question it answers |
|---|---|
| 1.1 | What is a class, and what does it know and do? |
| 1.2 | What is a type, and what is `null`? |
| 1.3 | What do `public`, `private`, `protected`, and `final` mean? |
| 1.4 | How do you read a method, and what does the dot do? |
| 1.5 | How does an object get built? |
| 1.6 | What do `extends`, `super(...)`, `abstract`, and `@Override` mean? |
| 1.7 | What is an `interface`? |
| 1.8 | How do `if`, `else if`, and `else` decide what runs? |
| 1.9 | What is that arrow, `->`? |
| 1.10 | What are comments and `TODO`s, and how do you read a whole class? |

---

## 1.1  A class:  what it knows and what it does

*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java`

Billy's intake runs on one motor.  The code for it is `SpinnyIntake.java`.
It's ninety-odd lines long, and most of them can wait.  Three pieces tell you
what the class is.

The first line names it:

```java
public class SpinnyIntake extends MechComponent
```

A few lines down is the piece of hardware it remembers:

```java
    private final DcMotor intake;
```

And further down is what it does when the driver presses a button:

```java
    @Override
    public void move(Gamepad gamepad)
    {
        strategy.nomNomNom(intake, gamepad);
    }
```

A **class** describes one kind of thing:  what it knows, and what it can do.
This one describes an intake.  What it knows is a motor, called `intake`.  What
it can do is `move`.

Read every class with those two questions:  what does it know, and what does it
do?  Every class in the repository answers both.  `DoublyLimitedMotor` knows a
motor and two touch sensors, and it can `setPower`, `canGoForward`, and
`canGoReverse`.  `BillyMA` knows Billy's four mechanisms, and it can
`giveInstructions` to all of them at once.

### Class and object

A class is a description.  An **object** is one thing built from that
description.  `SpinnyIntake.java` describes intakes in general.  When Billy
starts up, `BillyMA` builds one `SpinnyIntake` object, and that object is the
one connected to Billy's real intake motor.  Entry 1.5 shows the line that
builds it.

One class can describe many objects.  `CascadeArm`, the arm on `Wildbots2025`,
builds two `DoublyLimitedMotor` objects from the one class:  one called
`cascade` for the lift, and one called `drawbridge`.  Each has its own motor
and its own sensors.

### Fields

A **field** is a name that belongs to an object and lasts as long as the object
does.  `intake` is a field.  Each `SpinnyIntake` object has its own `intake`,
so two intakes would remember two different motors.

You can spot a field by where it's written:  inside the class's outer braces,
and outside every method.  Read the line right to left.  `intake` is the name.
`DcMotor` is its type, the kind of value it has (1.2).  `private final` says
who may touch it and whether it can change (1.3).

The rest of those three pieces gets its own entry.  `extends MechComponent`
says an intake is one kind of mechanism component (1.6).  `@Override` is a note
to the compiler (1.6).  And `strategy.nomNomNom(intake, gamepad)` hands the
motor and the gamepad to a piece of code `BillyMA` supplied, which decides what
the buttons do (1.9).

*See also:*  1.2, 1.5, 1.6, Chapter 7

## 1.2  Types, and `null`

Java makes every name say what kind of value it has.  That kind is the name's
**type**.  In the field from 1.1, `private final DcMotor intake;`, the type is
`DcMotor`, so `intake` can only ever be a motor.  Try to put a
number or a word in it and the compiler rejects the file before it gets
anywhere near the robot.

The list of types you need for this codebase is short.

| Type | What it is | In this code |
|---|---|---|
| `double` | a decimal number | `double power = 0.45;` in `BillyMA`'s flywheel code |
| `int` | a whole number | `private static final int TARGET_TAG_ID = -1;` in `BillyTeleOpBLUE` |
| `boolean` | `true` or `false` | `gamepad.dpad_up`, which is `true` while the button is held |
| `String` | text, in double quotes | `"intakeMotor"`, the motor's name in the robot's configuration (1.5) |
| `float` | a decimal number, stored in less memory | `public static final float DeadZoneRadius = 0.05f;` in `GamepadExtensions` |

Motor power, servo positions, and angles are `double`s.  AprilTag IDs, encoder
counts, and ball counts are `int`s.  The gamepad's sticks report `float`s, and
the `f` on the end of `0.05f` is how Java marks a number as one.

### Types that name objects

One more kind of type is missing from that table, and it's the kind this
codebase uses most.  `DcMotor`, `Servo`, `Gamepad`, and `HardwareMap` are
types too, and a value of
one of those types is a whole object with abilities of its own.  A `DcMotor`
can `setPower`.  A `Gamepad` knows which buttons are down.  They come from the
FTC library, and Chapter 3 is a tour of them.

Every class the team writes becomes a type the same way.  `BillyMA` keeps its
intake in a field whose type is the class from 1.1:

```java
    private final SpinnyIntake intake;
```

> **Aside:  `null`**
>
> A name whose type is an object type can also be `null`, which means *no
> object here*.  `BillyRobot` uses it on purpose, and its comment says what it
> means:
>
> ```java
>     private IAssistRobotStrategy<BillyMA.Part> assist = null; //null = no assist on
> ```
>
> While `assist` is `null`, no driver assist is running.  Chapter 13 is about
> what an assist does.
>
> Code checks for `null` with `==` (*is it the same as*) and `!=` (*is it
> different from*).  Here's the end of `Claw.java`, which writes the claw's
> numbers to the Driver Station:
>
> ```java
>         if (telemetryStrategy != null) {
>             telemetryStrategy.update(servo, telemetry);
>         } else {
>             telemetry.addData("Claw power:", servo.getPower());
>         }
> ```
>
> If somebody handed the claw a way to report itself, it uses that.  If the
> field is `null`, it writes the claw's power itself.  The check comes first
> for a reason:  asking `null` to do something, like `telemetryStrategy.update`
> with no object there, stops the program with a `NullPointerException`
> (Chapter 5).
>
> Only object types can be `null`.  An `int`, a `double`, or a `boolean`
> always has a value.

*See also:*  1.3, 1.4, Chapter 3, Chapter 5

## 1.3  `public`, `private`, `protected`, and `final`

Look again at the field from 1.1, `private final DcMotor intake;`.  The two
words in front of the type answer two questions.  `private` says who
may touch `intake`.  `final` says whether it can ever change.

### Who may use it

`public`, `private`, and `protected` are **access modifiers**.  One of them in
front of a field, a method, or a class says which code is allowed to use it.

| Word | Who may use it | In this code |
|---|---|---|
| `public` | any code, anywhere | `public void move(Gamepad gamepad)` in `SpinnyIntake`, which `BillyMA` calls |
| `private` | code inside this class only | `private final DcMotor intake;` |
| `protected` | this class, its subclasses (1.6), and classes in the same package | `protected SpinnyIntakeControlStrategy strategy;` |

A **package** is a folder of classes that belong together, named on the first
line of each file:  `package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components;`.
A field with none of the three words, like `Robot robot;` in `BillyTeleOpBLUE`,
can be used by any class in its own package.

`private` is how a class decides how its hardware gets used.  The intake's
motor is private, so the rest of the robot reaches it through `move`, and the
intake decides what `move` does with it.

`Claw.java` shows why that matters.  Its servo is a public field:

```java
    public CRServo servo;
```

The `TODO` comment just above that line plans a pair of sensors to stop the
claw at the ends of its travel.  Step 1 of that plan is to make `servo`
private.  A check inside `Claw` can only protect the servo if every other piece
of code has to go through `Claw` to reach it.  Entry 1.10 is about reading a
`TODO` like that one, and Chapter 7 is about the plan itself.

### Whether it can change

`final` means a name gets its value once and keeps it.  The intake's
constructor (1.5) finds the motor and puts it in `intake`, and from then on
nothing can point `intake` at a different motor.  Write a line that tries, and
the compiler rejects the file.

That's the reason to use it.  With `final` in place, pointing the intake at
the wrong motor halfway through a match is a mistake nobody can write.

> **Watch out.**  `final` fixes which object a name points at.  The object
> itself can still change.  A `final` motor can still have its power set over
> and over during a match, and that's exactly what the intake does.

You'll also see `final` in two other places.  On a class,
`public final class GamepadExtensions` means no class may extend it (1.6).
Together with `static`, as in `private static final int TARGET_TAG_ID`, it
makes a constant, a value set once for the whole program (2.2).

*See also:*  1.5, 1.6, 2.2, Chapter 7

## 1.4  Methods, and the dot that calls them

*Where it lives:*  `RobotModel/Mechs/Components/DoublyLimitedMotor.java`

A **method** is a named block of code that an object can run.  Its first line
tells you nearly everything about it, and you'll read lines like this one a
hundred times:

```java
    public void move(Gamepad gamepad)
```

Read it in four parts, left to right.

1. `public` says who may call it (1.3).  Anyone, here.
2. `void` is the **return type**:  what the method hands back when it
   finishes.  `void` means it hands back nothing.  The method does its work,
   and the call ends there.
3. `move` is the name.
4. `(Gamepad gamepad)` is the list of **parameters**:  what you have to hand
   the method so it can do its job.  Each one is a type and a name.  This
   method needs one gamepad, and inside the method it's called `gamepad`.

The name and the parameter list together, `move(Gamepad gamepad)`, are the
method's **signature**.  The signature is what tells two methods apart.

Here's a method that hands something back, from `DoublyLimitedMotor`:

```java
    public boolean canGoForward()
    {
        return ! forwardSensor.isPressed();
    }
```

The return type is `boolean`, so calling `canGoForward()` produces a `true` or
a `false` you can use.  The word `return` is where that value gets handed
back.  The empty parentheses mean it takes no parameters.  It works from what
the object already knows:  its field `forwardSensor`, a touch sensor at the
forward end of the motor's travel.  The `!` flips `true` and `false` (1.8), so
the method answers `true` while that sensor is unpressed and there's still
room to go.

The code between the braces is the method's **body**.  When you only need to
know what a method does, its first line is usually enough.  Read the body when
you need to know how.

When code runs a method, the values it hands over are called **arguments**.
`gamepad` is the parameter, the name in the signature.  The actual gamepad the
driver is holding is the argument.

> **Aside:  the dot, and chaining**
>
> Here's a line from the intake's code in `BillyMA`:
>
> ```java
>                     motor.setPower(0.75); }
> ```
>
> Read `motor.setPower(0.75)` as *take the object called `motor`, and ask it to
> `setPower`, handing it `0.75`*.  The dot reaches into an object for one of
> its methods or fields.
>
> It chains.  From `MecanumDrive`:
>
> ```java
>             double yaw = imu.getRobotYawPitchRollAngles().getYaw();
> ```
>
> Read it left to right:  ask `imu` for its angles, then ask *those angles* for
> the yaw.  Each dot works on whatever the step before it handed back.  The IMU
> is the sensor that knows which way the robot faces, and yaw is its heading
> (Chapter 3).
>
> A dot after a class's name, as in `GamepadExtensions.GetLeftStickX(gamepad)`,
> calls a method that belongs to the class itself (2.2).  Chains like these are
> all over the autonomous code, and a student who can read one aloud can read
> most of the repository.  Chapter 11 has more to say about long ones.

*See also:*  1.2, 1.8, 2.2, Chapter 11

## 1.5  Constructors and `new`

*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java`

An object needs setting up before it's any use.  The intake needs its motor,
and it needs to know what the buttons should do.  That setup is the job of the
constructor.  Here's the intake's:

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

A **constructor** has the same name as its class, and it has no return type
at all, so you'll see `public SpinnyIntake(` where a method would have
`public void` or `public boolean`.  It runs once, at the moment the object is
made, and its job is to get the object ready.

This one takes three parameters, one to a line, and does three things.

1. `super(strategy);` hands the strategy up to `MechComponent`, the class
   `SpinnyIntake` extends.  Entry 1.6 explains `super(...)`.
2. `intake = hardwareMap.get(DcMotor.class, motorName);` looks up a motor by
   its name and puts it in the `intake` field.  The **hardware map** is the
   SDK's list of every device plugged into the robot (Chapter 3).
3. The last line points the intake's own `strategy` field at the same
   strategy.  It has a cast in it, the type in parentheses, and Chapter 7
   explains casts.  You can read past it for now.

`this` means *the object whose code is running*.  So `this.strategy` is this
intake's own field called `strategy`.

### `new` runs it

The word `new` is what runs a constructor.  Here's where
`BillyMA` builds Billy's intake:

```java
        intake = new SpinnyIntake(hardwareMap, "intakeMotor",
```

The call keeps going for ten more lines, because its third argument is a
chunk of code saying what the buttons do (1.9).  `new` makes a fresh
`SpinnyIntake` object, runs the constructor with these arguments, and hands
the finished object back.  The `=` puts it in `BillyMA`'s field `intake`.

Every object in the robot gets made this way, from the top down.  The OpMode
says `new BillyRobot(...)`, `BillyRobot` says `new BillyMA(hardwareMap)`, and
`BillyMA` says `new SpinnyIntake(...)` and three more like it.  Chapter 6 walks
through those layers.

### Configuration names

`"intakeMotor"` is a **configuration name**.  It's the name somebody typed for
that motor in the robot's configuration on the Driver Station (Chapter 3), and
`hardwareMap.get` uses it to find the right device.  Billy's are
`"intakeMotor"`, `"ballPusherServo"`, `"flywheelMotorF"`, `"flywheelMotorB"`,
`"turretServo"`, `"limelight"`, `"imu"`, and `"lf"`, `"lb"`, `"rf"`, `"rb"` for
the four drive motors.

> **Watch out.**  A configuration name is a `String`, and the compiler checks
> nothing inside the quotes.  A typo there builds fine and fails only when the
> robot runs the code.  Check every name in the code against the
> configuration on the Driver Station, letter for letter, capitals included.

*See also:*  1.6, Chapter 3, Chapter 6, Chapter 7

## 1.6  `extends`, `super(...)`, `abstract`, and `@Override`

*Where it lives:*  `RobotModel/Mechs/Components/MechComponent.java`

The intake's first line says more than its name:

```java
public class SpinnyIntake extends MechComponent
```

`extends` says that a `SpinnyIntake` is one kind of `MechComponent`.  The
intake gets everything `MechComponent` has, its fields and its methods, and
adds what's special about intakes.  `SpinnyIntake` is a **subclass** of
`MechComponent`, and `MechComponent` is its **superclass**.

The team's components all share that superclass.  Six classes in the
`Components` folder extend `MechComponent`:  `Claw`, `DoubleShooter`,
`DoublyLimitedMotor`, `PusherServo`, `SpinnyIntake`, and `Turret`.  The
seventh file in the folder is `MechComponent` itself.  What it means to be a
component is Chapter 6's and Chapter 7's subject.

A class can extend a class it didn't write.  Every OpMode does:
`public class BillyTeleOpBLUE extends LinearOpMode {`.  `LinearOpMode` comes
from the FTC SDK, and extending it is what makes a class an OpMode, a
program the Driver Station can run (Chapter 6).

### `abstract`

Here's how `MechComponent` starts:

```java
public abstract class MechComponent
```

An **abstract** class exists to be extended.  The compiler rejects
`new MechComponent(...)`, and every real component is a subclass.  An
abstract class can also list methods with a semicolon where the body would
be:

```java
    public abstract void move(Gamepad gamepad);
```

That's an abstract method, and every subclass has to supply the body.
`MechComponent` has two of them, `move` and `update`, along with one for
autonomous code that Chapter 7 explains.  So every component has to say how
the gamepad moves it and what it reports to the Driver Station.  That promise
is what lets `BillyMA` call `intake.move(gamepad)` and `turret.move(gamepad)`
and know both methods exist.

### `@Override`

Here's where the intake keeps that promise:

```java
    @Override
    public void move(Gamepad gamepad)
```

`@Override` says *this method fills in, or replaces, one from the
superclass*.  The `@` marks an **annotation**, a note attached to the code for
the compiler or the SDK to read.  `@TeleOp(name="BillyBLUE")`, at the top of
`BillyTeleOpBLUE`, is another (Chapter 6).

`@Override` is optional, and the compiler accepts the method either way.
`Claw` and `Turret` write `public void move(Gamepad gamepad)` without it, and
they work just like `SpinnyIntake`.  What the annotation buys is a check.
With `@Override` in place, the compiler confirms the superclass really has a
method with that signature (1.4), so a misspelled `mvoe` gets caught on your
laptop.

### `super(...)`

A subclass's constructor starts by running its superclass's constructor.
That's the first line of the intake's constructor from 1.5:

```java
        super(strategy);
```

`super(...)` calls the superclass's constructor and hands it the arguments in
the parentheses.  It has to be the first line.  Here's the constructor it
runs, in `MechComponent`:

```java
    protected MechComponent(IControlStrategy strategy)
    {
        this.strategy = strategy;
    }
```

So the `MechComponent` part of the intake gets set up first, and then the
intake's own constructor finishes the job.  `super.strategy`, on the intake
constructor's last line, means the `strategy` field that came from the
superclass.

*See also:*  1.5, 1.7, Chapter 6, Chapter 7

## 1.7  `interface`

*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java`

Inside `SpinnyIntake.java`, just above the fields, is this:

```java
    public interface SpinnyIntakeControlStrategy extends IControlStrategy
    {
        /**
         * abstract definition of how a Spinny Intake gets controlled.
         * this Implementation should translate the Gamepad Input into motor movement
         * of the Spinny Intake.
         * The name of this method does not matter at all~ so make it descriptive in the
         * context of the Spinny Intake!
         * @param motor the `intake` motor will be passed here.
         * @param gamepad the gamepad will be passed down from the MechAssembly to this strategy
         */
        void nomNomNom(DcMotor motor, Gamepad gamepad);
    }
```

Strip away the comment and it's three lines.  An **interface** is a type that
lists methods and leaves out their bodies:  each method is a signature and a
semicolon.  It's a promise.  Anything of this type will have a method called
`nomNomNom` that takes a motor and a gamepad.

That promise is all the intake needs.  Its `move` method, from 1.1, is
`strategy.nomNomNom(intake, gamepad);`.  The intake calls `nomNomNom` without
knowing what's inside it.  `BillyMA` supplies the inside, as a chunk of code
it hands to the intake's constructor (1.9).  A different robot could hand the
same intake class different code and get different buttons.  Chapter 8 is
about why the team builds things this way.

The comment in the middle says the method's name doesn't matter, and it
means it.  The code `BillyMA` hands over fills in the interface's one method
without ever writing its name.  Entry 1.9 shows how.

Here's a smaller interface, the whole file `AutonStrategies/IAutonStrategy.java`
apart from its first line:

```java
public interface IAutonStrategy
{
    void execute();
}
```

One method, no parameters, nothing handed back.  An autonomous routine of this
type is anything that can `execute`.  Chapter 12 puts it to work.

### `implements`, and interfaces that extend

A class keeps an interface's promise with `implements`.  The Limelight
targeting code starts
`public class LimelightAutoTarget extends StateMachine implements Robot.IAssistRobotStrategy<BillyMA.Part>`,
and then supplies each method that interface lists, every one marked
`@Override` (1.6).  The angle brackets are generics, which Chapter 7 explains,
and Chapter 13 covers the assist itself.

An interface can extend another interface.  `SpinnyIntakeControlStrategy
extends IControlStrategy`, and so does every other component's strategy
interface.  `IControlStrategy` is an empty interface inside `MechComponent`,
which gives all of them one family name, so `MechComponent` can keep any of
them in its one `strategy` field (1.6).

### An interface and an abstract class

Both are promises a subclass or an implementer has to keep, so it's fair to
ask why the code has two.  An abstract class like `MechComponent` (1.6) can
also have fields and finished methods.  `MechComponent` keeps a `strategy`
and a `telemetry` field, and it has a working `initializeTelemetry` method
that every component inherits.  An interface in this codebase is only the
list.

*See also:*  1.6, 1.9, Chapter 8, Chapter 12, Chapter 13

## 1.8  `if`, `else if`, `else`

*Where it lives:*  `RobotModel/Mechs/Assemblies/BillyMA.java`

Here's how Billy's turret answers the bumpers, from `BillyMA`:

```java
                    if (gamepad.right_bumper)
                    {
                        servo.setPower(-1);
                    }
                    else if (gamepad.left_bumper)
                    {
                        servo.setPower(1);
                    }
                    else {
                        servo.setPower(0);
                    }
```

That's one **`if` chain**:  an `if`, any number of `else if`s, and an optional
`else` at the end.  Java checks the conditions from the top down, runs the
branch under the first one that's `true`, and skips everything after it.  The
`else` runs when none of them is `true`.  So exactly one branch runs, every
time.  Hold the right bumper and the servo gets `-1`.  Hold the left and it
gets `1`.  Hold neither and it gets `0`.

The intake, a few lines up in the same file, has the same shape:

```java
                    if (gamepad.dpad_up) {
                    motor.setPower(0.75); }
                    else if (gamepad.dpad_down) {
                    motor.setPower(-0.75);
                    } 
                    else {
                        motor.setPower(0);
                    }
```

The braces are placed differently, and that changes nothing.  Java follows the
braces and the words `if` and `else`, and it ignores the indenting.  Dpad up
runs the intake forward, dpad down runs it backward, and anything else stops
it.  Hold both, and the first branch wins:  `0.75`.

### Two separate `if`s

The flywheel, a little further down, reads the same two buttons a different
way:

```java
                    double power = 0.45;
                    if (gamepad.dpad_up) {
                        power += 0.05;
                    }
                    if (gamepad.dpad_down) {
                        power -= 0.05;
                    }
```

These are two separate statements, and Java checks both of them, every time.
That's what this code wants.  `power += 0.05` means *add 0.05 to `power`*, so
each button nudges the flywheel speed, up or down.  Hold both and they cancel:
`0.45`.

So a chain picks one branch, and separate `if`s each get their own check.
The difference shows most clearly when an `else` is involved, and an earlier
version of the intake code makes a good reading exercise.  Here is part of
`BillyMA.java` as it was before commit `1a6809a`, when the intake was written
as two statements.

> **Predict.**  The driver holds dpad up, and nothing else.  Go line by line.
> What power does the motor end up with?
>
> ```java
>                     if (gamepad.dpad_up) {
>                         motor.setPower(0.75);
>                     }
>                     if (gamepad.dpad_down) {
>                         motor.setPower(-0.75);
>                     } else {
>                         motor.setPower(0);
>                     }
> ```

The motor ends up at `0`.  The first `if` sets `0.75`.  Then a new statement
starts:  `dpad_down` is `false`, so its `else` runs and sets the power to `0`.
Both lines ran, and the second one came last, so the motor got `0` and the
intake sat still.  Dpad down worked as expected, because its own `if` came
last.

The rule underneath is the one to keep:  an `else` belongs to the nearest `if`
above it, and only to that one.  Here the `else` meant *dpad down isn't held*,
and the intake needed *neither button is held*.  Commit `1a6809a`
joined the two into one chain with `else if`, which is the version you read
above.

When you read an `if`, find where its chain ends.  If the next `if` starts a
new statement, both get checked, and the last one to set a value decides it.

> **Aside:  conditions, and `!`, `&&`, `||`**
>
> The part in parentheses after `if` is the **condition**.  It has to work out
> to a `boolean`.  `DoublyLimitedMotor` builds a big one:
>
> ```java
>         if ((power > 0 && !canGoForward() )|| (power < 0 && !canGoReverse())) {
>             power = 0;
>         }
> ```
>
> | Symbol | Read it as | In this code |
> |---|---|---|
> | `==` | is equal to | `gamepad == null` |
> | `!=` | is different from | `telemetryStrategy != null` |
> | `<`, `>` | is less than, is more than | `power > 0` |
> | `!` | not | `!canGoForward()` |
> | `&&` | and:  `true` when both sides are | `power > 0 && !canGoForward()` |
> | `\|\|` | or:  `true` when either side is | the two halves of the line above |
>
> Read the whole line aloud:  *if the power is forward and the motor can't go
> forward, or the power is backward and it can't go backward, make the power
> zero.*  That's how `DoublyLimitedMotor` refuses to drive into a limit switch
> that's already pressed (Chapter 8).
>
> Keep `==` and `=` apart.  `==` asks whether two things are equal.  `=` puts a
> value in a name, as in `power = 0`.

### `if` without braces

`BillyMA`'s main loop uses `if`s without braces:

```java
            if (!claimed.contains(Part.INTAKE))
                intake.move(gamepad);
```

Without braces, an `if` controls just the one statement after it.  This one
reads *if nothing has claimed the intake, move it with the gamepad*.  Chapter
13 explains claims.

*See also:*  1.2, 1.9, Chapter 8, Chapter 13

## 1.9  Lambdas, enough to recognize one

*Where it lives:*  `RobotModel/Mechs/Assemblies/BillyMA.java`

This codebase hands chunks of code around as values.  When `BillyMA` builds
the intake (1.5), the call goes on past the configuration name:

```java
        intake = new SpinnyIntake(hardwareMap, "intakeMotor",
                (motor, gamepad) -> {
```

That second line starts a **lambda**:  a chunk of code written down so it can
be handed to something else and run later.  The arrow `->` splits it in two.
On the left, in parentheses, is what it receives:  a `motor` and a `gamepad`.
On the right, in braces, is what it does with them, and that's the `if` chain
from 1.8.

`BillyMA` hands this lambda to the intake's constructor, and the intake keeps
it in its `strategy` field.  Then, every time through the robot's loop, the
intake's `move` method runs it:

```java
        strategy.nomNomNom(intake, gamepad);
```

The intake's own motor arrives in the lambda as `motor`, and the driver's
gamepad arrives as `gamepad`.  So the lambda is *how this robot's buttons
drive this intake*, written where the robot is put together and run where the
motor lives.

A lambda fills in the one method of an interface (1.7), which is why its name
never appears.  `nomNomNom` is the name on the interface, and the lambda is
its body.

### Three shapes

Lambdas come in a few shapes, and all of them have the arrow.

| Shape | Example | Where |
|---|---|---|
| several things in, a block of code | `(motor, gamepad) -> { ... }` | every control lambda in `BillyMA` |
| nothing in, a block of code | `() -> { ... }` | the autonomous dance in `ExampleAutonomousStrategies` |
| one thing in, one expression | `fr -> fr.getFiducialId() == tagId` | the tag search in `LimelightExtensions` |

The last one has no parentheses, because there's only one parameter, and no
braces, because the whole body is one expression whose value gets handed
back.  It reads *given a tag result `fr`, is its ID the one we want?*
Chapter 5 explains the search it belongs to.

That's enough to read one.  When you see an arrow, you're looking at code
that's being handed over to run later, and the names on the left are what it
will be given.  Chapter 8 is about why the team writes its robots this way,
and the names for the pieces.

*See also:*  1.5, 1.7, Chapter 5, Chapter 8, Chapter 12

## 1.10  Comments, `TODO`, and reading a whole class

*Where it lives:*  `RobotModel/Mechs/Components/Claw.java`

### Comments

A **comment** is text written for people.  Java skips it.  The code has three
kinds.

| Kind | What it looks like | In this code |
|---|---|---|
| line comment | `//` to the end of the line | `// negative to account for inverted y on gamepads!` in `GamepadExtensions` |
| block comment | `/*` and `*/` around it | `/* Every State Machine must be aware of its Current State */` in `StateMachine` |
| doc comment | `/**` and `*/`, just above a method or class | the comment inside the interface in 1.7 |

A doc comment describes the method under it.  `@param` lines describe the
parameters and an `@return` line describes what comes back.  Android Studio
shows the doc comment when you rest the mouse on the method's name anywhere
it's used, so a good one saves the next person opening the file.

### `TODO`

A comment that starts with `TODO` marks work that's known and still open,
written down right where the work is, with enough explanation to act on it.
The team's `readme.md`, in the `teamcode` folder, puts it this way:  *"Search
the project for `TODO` to find real, known bugs left in place to work on."*

They come in a few kinds.  Some are a value to fill in:
`BillyTeleOpBLUE` has `// TODO: this season's BLUE-alliance target AprilTag ID goes here`
above its tag number.  Some are a project, like the one in `MecanumDrive` that
begins `// TODO (project, and a good one for somebody who wants the drive train to feel`
and lays out field-relative driving.  Some are the next step for a feature,
like the travel limits planned in `Claw` and `Turret`.

When this chapter was written, the Java code had twelve `TODO` comments.
Search the whole project for `TODO` to see today's list.

### Reading a whole class

Everything in this chapter shows up in one short file.

> **Try it.**  Open `RobotModel/Mechs/Components/Claw.java` and answer these
> from the code, one at a time.
>
> 1. What hardware does this class know about, and what's its type?
> 2. Find the constructor.  How can you tell it's the constructor?
> 3. Find `public void open()`.  What does the `void` tell you?
> 4. Read `servo.setPower(1)` aloud, in plain English.
> 5. In `update()`, find the `if` and the `else`.  When does each branch run?
> 6. Read the long `TODO` comment.  What does it want added, and what has to
>    change first?
>
> Reading the file is enough to answer all six.

Here's what the file says.

1. It knows one `CRServo`, a servo that spins continuously (Chapter 3), in the
   field `public CRServo servo;`.  It's `public`, and 1.3 explains why the
   `TODO` above it plans to change that.
2. The constructor is `public Claw(HardwareMap hardwareMap, ...`.  It has the
   class's name and no return type (1.5).  It starts with `super(strategy);`
   (1.6) and then looks up the servo with `hardwareMap.get`.
3. `open()` hands nothing back (1.4).  It does its work, and the call ends.
   It sits inside `AutonomousClawBehaviors`, a class written inside `Claw`
   for autonomous code to use (Chapter 7).
4. *Ask the claw's servo to run at full power, one way.*  `1` is full power
   forward, `-1` is full power in reverse, and `0` is stopped.
5. If somebody handed the claw a `telemetryStrategy`, the first branch runs
   it.  If that field is `null`, the `else` writes the claw's power itself
   (1.2).
6. It wants sensors, so the claw can tell when it's fully closed and whether
   it's holding something.  First it wants three ways of reaching the servo
   routed through `Claw`, starting with making `servo` private.  Chapter 7
   follows that plan.

*See also:*  1.2, 1.3, 1.5, Chapter 3, Chapter 7

---

## Chapter summary

Every class in the repository answers two questions:  what does it know, and
what does it do?  Its fields are what it knows, and its methods are what it
does.  The words around them say who may use each one, whether it can change,
what it extends, and what it promises.  Inside the methods, `if` chains decide
which branch runs, and lambdas carry each robot's own choices into the
classes that run them.

| You read | It's an example of |
|---|---|
| `public class SpinnyIntake extends MechComponent` | a **class** that is a **subclass** |
| `private final DcMotor intake;` | a **field**, with an **access modifier** and `final` |
| `public boolean canGoForward()` | a **method**, with a **return type** and an empty **parameter** list |
| `imu.getRobotYawPitchRollAngles().getYaw()` | the **dot**, **chained** |
| `new SpinnyIntake(hardwareMap, "intakeMotor", ...` | `new` running a **constructor**, with a **configuration name** |
| `public abstract void move(Gamepad gamepad);` | an **abstract** method |
| `SpinnyIntakeControlStrategy` | an **interface** |
| the turret's bumpers | an **`if` chain** |
| `(motor, gamepad) -> {` | a **lambda** |
| `assist = null` | **`null`**, meaning no object |

### Java in this chapter

| Syntax | Where |
|---|---|
| `class`, fields | 1.1 |
| `int`, `double`, `boolean`, `String`, `float`, `null` | 1.2 |
| `public`, `private`, `protected`, `final`, `package` | 1.3 |
| method signatures, `void`, `return`, the dot | 1.4 |
| constructors, `new`, `this` | 1.5 |
| `extends`, `abstract`, `@Override`, `super(...)` | 1.6 |
| `interface`, `implements` | 1.7 |
| `if`, `else if`, `else`, `==`, `!=`, `!`, `&&`, `\|\|`, `+=` | 1.8 |
| lambdas, `->` | 1.9 |
| `//`, `/* */`, `/** */`, `TODO` | 1.10 |

### Vocabulary

**abstract** -- describes a class that exists to be extended, or a method
listed with no body that every subclass has to supply.  `MechComponent` is
abstract.  (1.6)

**access modifier** -- `public`, `private`, or `protected`:  the word in front
of a field, method, or class that says which code may use it.  (1.3)

**annotation** -- a note attached to code with `@`, for the compiler or the
SDK to read.  `@Override` is one.  (1.6)

**argument** -- a value handed to a method or constructor when it runs.  (1.4)

**chaining** -- using a dot on whatever the step before handed back, as in
`imu.getRobotYawPitchRollAngles().getYaw()`.  (1.4)

**class** -- a description of one kind of thing:  what it knows and what it
can do.  (1.1)

**comment** -- text in the code written for people, which Java skips.  (1.10)

**condition** -- the part in parentheses after `if`, which works out to
`true` or `false`.  (1.8)

**configuration name** -- the name typed for a device in the robot's
configuration on the Driver Station, which the code uses to find it.
`"intakeMotor"` is one.  (1.5)

**constructor** -- the code that runs once when an object is made, to get it
ready.  It has the class's name and no return type.  (1.5)

**field** -- a name that belongs to an object and lasts as long as the object
does.  (1.1)

**final** -- describes a name that gets its value once and keeps it.  (1.3)

**`if` chain** -- an `if`, any number of `else if`s, and an optional `else`.
Exactly one branch runs.  (1.8)

**interface** -- a type that lists methods with their bodies left out:  a
promise about what anything of that type can do.  (1.7)

**lambda** -- a chunk of code written down so it can be handed to something
else and run later, written with an arrow `->`.  (1.9)

**method** -- a named block of code an object can run.  (1.4)

**null** -- the value that means no object here.  (1.2)

**object** -- one thing built from a class.  (1.1)

**override** -- to fill in or replace a method from the superclass.  The
`@Override` annotation asks the compiler to check that you have.  (1.6)

**package** -- a folder of classes that belong together, named on the first
line of each file.  (1.3)

**parameter** -- a name in a method's signature for something the method
needs handed to it.  (1.4)

**return type** -- the type of value a method hands back.  `void` means it
hands back nothing.  (1.4)

**signature** -- a method's name and its parameter list.  (1.4)

**subclass** -- a class that extends another, its **superclass**, and gets
everything the superclass has.  (1.6)

**`super(...)`** -- the call, first in a subclass's constructor, that runs
the superclass's constructor.  (1.6)

**TODO** -- a comment marking known, open work, written where the work is.
(1.10)

**type** -- the kind of value a name can have.  (1.2)

**void** -- the return type of a method that hands nothing back.  (1.4)

### Check your understanding

1. Name the four parts of `public boolean canGoForward()`, and say what each
   one tells you.  (1.4)
2. Name the four parts of `public void setPower(double power)` in
   `DoublyLimitedMotor`.  What will calling it hand back?  (1.4)
3. The intake's field is `private final DcMotor intake;`.  What mistake does
   `final` make impossible, and what can still change?  (1.3)
4. `"intakeMotor"` is spelled `"IntakeMotor"` in the robot's configuration.
   When does the problem show up, and why does the compiler miss it?  (1.5)
5. What does `@Override` buy, given that `Claw.move` works without it?  (1.6)
6. The turret uses one `if` chain, and the flywheel uses two separate `if`s.
   Explain why each is the right shape for its job.  (1.8)
7. In the earlier version of the intake code, which `if` did the `else`
   belong to, and what did that do when only dpad up was held?  (1.8)
8. `DoublyLimitedMotor.setPower` zeroes the power before passing it to the
   motor.  Read its condition aloud, and say why a team would want that check
   in one place instead of in every caller.  (1.8)
9. When `BillyMA` builds the intake, what does the lambda receive, and when
   does it run?  (1.9)

## Where to go next

Chapter 2 is the other half of reading the code:  the `Extensions` folder,
`static` methods, and the helpers every robot shares.  Chapter 3 tours the
hardware types you met here as `DcMotor`, `CRServo`, and `IMU`.  Chapter 6
puts the classes in layers, from the OpMode down to the component, and
Chapter 8 explains why every component is handed a lambda.
