# Chapter 2 - Extension Methods

*Methods for classes you don't own, and a test for a good one.*

---

Some code belongs to no robot.  Cleaning up a joystick reading, waiting half a
second, working out the shortest way to turn from one heading to another:
every robot the team builds needs these, and none of them is about Billy.
That code lives in the `Extensions` folder.

This chapter is about that folder.  It covers what the team means by an
extension method, the Java keyword that makes one possible, and how to decide
whether a new helper belongs there.  The hard part is the last one.  A helper
can be too narrow for a shared file, or a shared file can collect too much, and
the chapter ends with three questions that catch both.

Look here when you meet a call like `GamepadExtensions.GetLeftStickX(gamepad)`,
when you see `static`, or when you've written a small method and wonder where
it should go.

Read first:  1.4, for how to read a method's first line.

| Entry | The question it answers |
|---|---|
| 2.1 | What is an extension method, in this codebase? |
| 2.2 | What does `static` mean, and what's a utility class? |
| 2.3 | Why does the code reach for one? |
| 2.4 | What makes `ThreadExtensions` a good one? |
| 2.5 | Where does `GamepadExtensions` stop? |
| 2.6 | What does `AngleExtensions` do, and how could its names say so? |
| 2.7 | What does `LimelightExtensions` do? |
| 2.8 | How can a helper be too narrow, or a file too broad? |
| 2.9 | Does this helper belong in `Extensions`?  The three-question test |

---

## 2.1  What an extension method is

*Where it lives:*  `Extensions/GamepadExtensions.java`

Here's how Billy's drive train reads the left stick, in `MecanumDrive`:

```java
        double horizontal = GamepadExtensions.GetLeftStickX(gamepad);
```

And here's the method it calls:

```java
    public static float GetLeftStickX(Gamepad gamepad)
    {
        if(gamepad == null)
            return 0f;
        return ApplyDeadZone(gamepad.left_stick_x);
    }
```

It takes a gamepad and hands back the stick's left-right position, cleaned
up.  If there's no gamepad at all, it hands back `0` (1.2 covers `null`).
Otherwise it passes the raw reading through `ApplyDeadZone`, which turns tiny
readings near the middle into exactly zero (2.5).

Notice what it's doing.  `Gamepad` is a type from the FTC SDK, and the team
can't add methods to it.  `GetLeftStickX` acts like the method `Gamepad` is
missing.  It takes the gamepad as its first parameter, does one job with it,
and lives in a class named for the type it helps:  `GamepadExtensions`.

That's what the team calls an **extension method**:  a `static` method (2.2),
in a class named `SomethingExtensions`, that takes the type it extends as its
first parameter.

The name comes from other languages.  In C# or Kotlin you can attach a method
to a type somebody else wrote and call it with a dot, as if it had been there
all along:  `gamepad.GetLeftStickX()`.  Java has no way to do that, so this
pattern is the Java version.  The cost shows up at the call:  you write
`GamepadExtensions.GetLeftStickX(gamepad)`, which is longer.  What you get is
the same job done once, in one shared place, and a call that still reads
almost like a method the gamepad has.

*See also:*  2.2, 2.5, Chapter 4

## 2.2  `static`, and the utility class

*Where it lives:*  `Extensions/GamepadExtensions.java`

Most methods belong to an object.  To call `setPower`, you need a motor:
`motor.setPower(0.75)` asks *that* motor (1.4).  Two intakes have two motors,
and each `setPower` works on its own.

A **`static`** method belongs to the class itself.  You call it on the class's
name, with no object anywhere:

```java
                ThreadExtensions.TrySleep(100);
```

That line, from `MecanumDrive`'s turning code, waits a tenth of a second
(2.4).  There's no `ThreadExtensions` object to ask.  The method gets
everything it needs from its parameter, `100`, and that's the mark of a good
`static` method:  everything goes in through the parameters, and the answer
comes back through `return`.

A field can be `static` too, and then there's one copy for the whole program.
Paired with `final` (1.3), that makes a **constant**:

```java
    public static final float DeadZoneRadius = 0.05f;
```

Every gamepad reading in the code uses that one `DeadZoneRadius`.  Change it
here and every robot's sticks change with it.  `BillyTeleOpBLUE` has another
constant, `private static final int TARGET_TAG_ID = -1;`, the AprilTag the
robot aims at.  A `TODO` above it says this season's number goes there.

### The utility class

`GamepadExtensions` has only `static` methods and a constant.  Nobody ever
needs a `GamepadExtensions` object, because there's nothing for one to
remember.  A class like that is a **utility class**:  a named home for a set
of related `static` helpers.

Here's how `GamepadExtensions` begins:

```java
public final class GamepadExtensions
{
    /**
     * Java is so gross.... why can't I just have an explicitly static class ;_;
     */
    private GamepadExtensions() { }
```

Two choices there make it a strict utility class.  `final` means no class can
extend it (1.6).  The constructor is `private` and empty, so code outside the
class can never say `new GamepadExtensions()` (1.5).

The comment explains the feeling.  C# lets you mark a whole class `static`,
and the compiler then refuses to let anybody make one.  Java has no such
keyword, so the private constructor does the same job by hand.

The other three extension classes, `ThreadExtensions`, `AngleExtensions`, and
`LimelightExtensions`, are plain `public class`es with neither guard.  They're
used the same way, always through the class's name.  Making a
`new ThreadExtensions()` would be legal, and the object would have nothing to
do.

> **Watch out.**  A `static` method has no object, so it can't use an
> object's fields.  Write `intake` inside a `static` method and the compiler
> rejects it, because there's no particular intake to mean.  When a helper
> needs a robot's motor or settings, hand them in as parameters, or the
> helper belongs on the class that has them (2.9).

*See also:*  1.3, 1.5, 2.1, 2.9

## 2.3  Why reach for an extension method?

The `Extensions` folder exists for three reasons, and each file in it is there
for at least one of them.

### 1.  You don't own the type

`Gamepad` comes from the FTC SDK, and `Thread` comes from Java itself.  The team
can't edit either one.  You might think of writing a subclass of `Gamepad`
with the extra methods in it (1.6), but the SDK makes the gamepad objects and
hands them to every OpMode as `gamepad1` and `gamepad2`.  A subclass of your
own would never be the gamepad you're actually holding.  A `static` method
that takes the SDK's gamepad as a parameter works on the real one.

### 2.  A convention would otherwise be copied everywhere

Push a gamepad stick forward and the SDK reports a *negative* number.  So
every piece of code that reads a stick's up-down position has to flip the
sign.  `GamepadExtensions` does it once:

```java
        // negative to account for inverted y on gamepads!
        return -ApplyDeadZone(gamepad.left_stick_y);
```

Four files call it:  `MecanumDrive`, both tank drives, and `CascadeArm`.
Without the shared method, each would carry its own deadzone check and its own
minus sign, and the first one to leave the minus sign out would hand a driver
a stick that runs backward.  With the shared method, the flip is written once,
and every robot gets it the same way.

### 3.  Some logic is plain math

`AngleExtensions` never touches a `HardwareMap`, a `Telemetry`, or any piece of
the robot model.  It's degrees in, degrees out (2.6).  A calculation like that
belongs outside the robot model entirely.  Put it inside a `MechComponent` or
a `DriveTrain`, and every caller would need one of those on hand just to do
arithmetic.

*See also:*  2.5, 2.6, Chapter 4, Chapter 6

## 2.4  `ThreadExtensions`

*Where it lives:*  `Extensions/ThreadExtensions.java`

Here's the whole class:

```java
public class ThreadExtensions
{
    /**
     * Encapsulate Thread.sleep(...) in a try catch block.
     * @param millis try to sleep this long
     * @return returns false if sleep was interrupted.
     */
    public static boolean TrySleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            return false;
        }
        return true;
    }
}
```

`TrySleep` pauses for some number of milliseconds and hands back `true` when it
slept the whole time.  `long` is Java's type for a whole number that can be
very large.

`Thread.sleep` is Java's own pause.  Something else in the program can cut a
sleep short, and when that happens `Thread.sleep` throws an
`InterruptedException`, a signal that something went differently than planned.
Java insists that any code calling `Thread.sleep` say what to do then.  `try`
marks the code that might be interrupted, and `catch` says what happens if it
is.  Here the answer is *hand back `false`*.  Every caller gets that handled
for free, in one line.

Today it's used in two places:  `MecanumDrive`'s `turnToAngle`, which checks
the heading every tenth of a second, and the autonomous dance routine in
`ExampleAutonomousStrategies`, which drives, waits, and drives again.

### Why it's a good one

It does one job, and nothing about robots gets into it:  no gamepad, no
motor, no Billy.  Here's a quick test for an extension method.  Would this
exact method be useful in a project that has nothing to do with robots?  For
`TrySleep` the answer is yes.  A game, a web server, or a homework program
could use it unchanged.  A helper that passes that test belongs in a shared
file.

> **Watch out.**  While `TrySleep` waits, the code that called it waits too.
> In `turnToAngle`, nothing else on the robot updates until the turn is done,
> and a `TODO` above that method says so.  Waiting without stopping everything
> else is the subject of Chapter 12.

*See also:*  2.2, 2.9, Chapter 12

## 2.5  `GamepadExtensions`:  where it stops

*Where it lives:*  `Extensions/GamepadExtensions.java`

`GamepadExtensions` has one helper that the other four call:

```java
    /**
     * Helper Method for Applying a standard Dead-Zone calculation to a Gamepad Axis reading.
     * @param input pass any gamepad axis here.
     * @return 0 if abs(input) < DeadZoneRadius, otherwise, input is unchanged.
     */
    public static float ApplyDeadZone(float input)
    {
        if(Math.abs(input) < DeadZoneRadius)
            return 0.0f;
        return input;
    }
```

A stick at rest rarely reads exactly zero.  It reads something tiny, like
`0.02`, even with nobody touching it.  The **deadzone** is the band around
zero that counts as zero (Chapter 4).  `Math.abs` gives a number's size with
its sign dropped, so this reads *if the reading is closer to zero than
`DeadZoneRadius`, hand back zero;  otherwise hand back the reading*.

Then there are four readers, one for each stick direction the team uses:
`GetLeftStickX`, `GetLeftStickY`, `GetRightStickX`, and `GetRightStickY`.  Each
handles a missing gamepad, applies the deadzone, and flips the sign on the
up-down ones (2.3).

Every robot the team builds has a gamepad, a stick that needs a deadzone, and
an up-down axis that reads backward.  So the whole file clears the same bar as
`ThreadExtensions` (2.4):  any robot would want it, unchanged.

### Where it stops

Look at what the file leaves to everyone else.  It answers one question:
*what is this stick reporting, cleaned up?*  It never decides what that
number should *do*.  Spinning an intake, driving a wheel, raising a lift:
those decisions happen somewhere else.

Here's one of them.  `CascadeArm`, the arm on `Wildbots2025`, hands its lift a
piece of code (1.9) that starts:

```java
                     double power = GamepadExtensions.GetLeftStickY(gamepad);
```

and then gives that `power` to the lift motor.  *The left stick drives the
lift* is a choice about one robot, so it lives with that robot, in the code
`CascadeArm` hands its component.  Chapter 8 calls that code a control
strategy.  `GamepadExtensions` supplies the clean number and stops there.

*See also:*  2.3, 2.8, Chapter 4, Chapter 8

## 2.6  `AngleExtensions`

*Where it lives:*  `Extensions/AngleExtensions.java`

Headings wrap around.  Turn far enough left and `179` degrees becomes `-179`,
two degrees away.  Any code that compares or subtracts headings has to handle
that, and `AngleExtensions` does it in three methods.  `MecanumDrive` uses two
of them to turn the robot to a heading in autonomous.

Here's the first:

```java
    /**
     * used when u give bad data bc ur bad
     * converts number outside of (-180, 180) range to equivalent angle within that range
     * @param degrees
     * @return angle in range (-180, 180)
     */
    public static double mapToIMURange (double degrees) {
        while(Math.abs(degrees)>180) {
            if (degrees<0)
                degrees+=360;
            else {
                degrees-=360;
            }
        }
        return degrees;
    }
```

The first line of the comment is a joke.  The second is the real
documentation, and it's accurate:  while the angle is more than 180 away from
zero, add or take away 360 until it fits.  `370` becomes `10`.  `-190` becomes
`170`.  A `while` loop repeats its body as long as its condition is `true`
(1.8 covers conditions).

The second method:

```java
    /**
     * used to find the other "name" of the same angle; helps in getSmol Calculatioins
     * @param degrees
     * @return angle in range (-360, 360)
     */
    public static double getDoppleganger(double degrees) {
        if (degrees < 0)
            return degrees+360;

        return degrees-360;
    }
```

Every direction has two names.  `-90` and `270` point the same way.  This
method hands back the other name:  it adds 360 to a negative angle and takes
360 away from anything else.

And the third:

```java
    /**
     * looks at all the possible ways to get from angle A to Angle B and finds the shortest
     * possible path, aka angle with smallest magnitutde
     * @param degrees
     * @param yaw
     * @return angle in range (-180, 180);
     */

    public static double getSmol(double degrees, double yaw) {
        double yeeyaw = getDoppleganger(yaw);
        double dees = getDoppleganger(degrees);
        double[] possibleValues = new double[]{
                degrees-yaw,
                degrees-yeeyaw,
                dees-yaw,
                dees-yeeyaw
        };
        double smol=possibleValues[0];
        for (int i =1; i<4; i++) {
            if (Math.abs(possibleValues[i])<Math.abs(smol)) {
                smol = possibleValues[i];
            }
        }
        return smol;
    }
```

`getSmol` finds the shortest turn from the heading the robot has, `yaw`, to
the heading it wants, `degrees`.  It writes both angles under both of their
names, works out all four differences, and keeps the one closest to zero.  The
sign says which way to turn.  `double[]` is an **array**, a numbered list of
values, and the `for` loop walks through it one item at a time.

> **Predict.**  The robot faces `-170` and wants to face `170`.  What does
> `getSmol(170, -170)` hand back?

It hands back `-20`.  The four differences are `340`, `-20`, `-20`, and
`-380`, and `-20` is the one closest to zero.  Those two headings are only 20
degrees apart, across the point where `180` meets `-180`.  So `turnToAngle`,
which spins left for a positive answer and right for a negative one, turns
right 20 degrees and skips the long way around.

### The right place, and names that could say more

Each of these is pure math, general, and free of hardware.  That's exactly
what belongs in `Extensions` (2.3), so the file's place in the code is right.

The names are the part that could do more work.  Wrapping an angle into the
range from -180 to 180 is useful for any angle on a circle, from a gyro or
not, and `mapToIMURange` names it after one place it's used.  `getDoppleganger`
and `getSmol` are playful names for real ideas:  the other name for an angle,
and the shortest turn between two headings.  That second idea is the most
useful one in the file, and a student searching the code for "shortest turn"
or "wrap angle" would find neither by name.

The doc comments make up much of the difference.  `getSmol`'s says exactly what
it does.  But a method gets reused most when its name and the first line of its
comment are enough to use it correctly, without reading the body.  Choosing
where code lives and choosing what to call it are separate decisions, and this
file gets the first one right.

> **Try it.**  Cover the names and read the three method bodies.  For
> `getDoppleganger` and `getSmol`, write a name that says what each hands back,
> and one plain sentence of documentation.  Then rewrite the comment on
> `mapToIMURange`, so a student reading only the comment would reach for it
> the next time *any* angle needs wrapping.  You should end up with names a
> stranger could guess the purpose of, something like `otherNameForAngle` and
> `shortestTurn`.  Keep it on paper for now.  `MecanumDrive` calls two of
> these, so a real rename is a pull request that updates the callers too.

*See also:*  2.3, 2.9, Chapter 3, Chapter 14
