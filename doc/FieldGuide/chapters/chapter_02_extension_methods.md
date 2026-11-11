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

## 2.7  `LimelightExtensions`

*Where it lives:*  `Extensions/LimelightExtensions.java`

Billy's turret aims itself at an AprilTag.  Every loop, the tracker in
`LimelightAutoTarget` asks the camera one question:  is the tag I want in
view?

```java
            LLResultTypes.FiducialResult tag =
                    LimelightExtensions.tryGetFiducial(
                            limelight,
                            targetTagId);

            if(tag == null)
            {
                turret.reportStatus("Tag " + targetTagId + " not found.");
                return stopTurret();
            }
```

`Limelight3A` is the camera's type, and it comes from the FTC SDK, the same as
`Gamepad`.  A **fiducial** is a marker a camera can find and identify.  On an
FTC field the fiducials are AprilTags (Chapter 5).

Here's the method that call reaches:

```java
    /**
     * *look for a specific AprilTag Id using the limelight camera
     * If the tag is not found, this method returns NULL
     * @param limelight camera configured to read Fiducials
     * @param tagId which tag id are you looking for.
     * @return The desired FiducialResult or NULL if not found.
     */
    public static LLResultTypes.FiducialResult tryGetFiducial(
            Limelight3A limelight,
            int tagId)
    {
        LLResult result = limelight.getLatestResult();
        if (result == null)
            return null;

        return result
                .getFiducialResults()
                .stream()
                .filter(fr -> fr.getFiducialId() == tagId)
                .findFirst()
                .orElse(null);
    }
```

`getLatestResult` hands back the newest batch of what the camera saw, as one
`LLResult`.  If there's no result to read, the method hands back `null` right
away (1.2).  Otherwise it searches the result's list of tags.  Read the chain
from the top:  take the list of tags the camera found, keep the ones whose ID
is `tagId`, take the first of those, and if the list came up empty, hand back
`null`.  `fr -> fr.getFiducialId() == tagId` is a lambda (1.9), and Chapter 5
takes the whole chain apart.

### The name says what to check

The method can come back empty-handed, and it says so twice.  The name starts
with `try`, the same word `TrySleep` uses for a job that might not finish
(2.4).  The doc comment says it outright:  *If the tag is not found, this
method returns NULL.*  So a caller knows to check for `null` before using the
answer, and `lookForTag` does it on the very next line.

That's the third question of 2.9 answered well.  A student can use
`tryGetFiducial` correctly from its name and its comment alone.

### `tryFindOneOf`, and the three dots

The second method looks for any one of several tags:

```java
    public static LLResultTypes.FiducialResult tryFindOneOf(
            Limelight3A limelight,
            int... tags)
    {
        LLResult result = limelight.getLatestResult();
        if (result == null)
            return null;

        return result
                .getFiducialResults()
                .stream()
                .filter(fr ->  Arrays.stream(tags).anyMatch(i -> i == fr.getFiducialId()))
                .findFirst()
                .orElse(null);
    }
```

`ExampleStateMachineAuton` uses it to find out which of three tags is in
front of the robot:

```java
                    LimelightExtensions.tryFindOneOf(
                            robot.limelight,
                            21, 22, 23);
```

> **Aside:  `int...`**
>
> Three dots after a parameter's type mean *any number of these*.  The caller
> lists as many as it likes, separated by commas, and inside the method they
> arrive as one array (2.6).  In the call above, `tags` is an array of three
> numbers:  21, 22, and 23.  Java calls this a **varargs** parameter, short
> for "variable number of arguments."

The search works the same way as before, with a bigger question in the
`filter`:  is this tag's ID any one of `tags`?  `anyMatch` answers `true` as
soon as one number in the array passes the test.

The answer is the first match in the *camera's* list, whatever order the call
names the IDs in.  If tag 23 comes first in what the camera reports, that's
the one you get, even though the call lists 21 first.  `findTags` checks the
ID of whatever comes back, so it handles any of the three.

### A narrower file, in the right place

This file clears the first question of 2.9 by a smaller margin than the
others.  `TrySleep` would help any program at all.  `LimelightExtensions`
helps only code that has a Limelight 3A.

Look at what the methods take, though.  The camera and the tag IDs both come
in as parameters.  Billy passes `targetTagId`, and the example autonomous
passes 21, 22, and 23.  Any robot with a Limelight could call either method
unchanged.  The narrowing comes from the type the file extends, and every
extension class narrows that way:  `GamepadExtensions` matters only to code
with a gamepad.  Ask the first question about robots that carry the type, and
this file passes.

One job is waiting at the top of the class:

```java
    // TODO (students): BIOBUZZ goals are AprilTag *clusters* (4 tags sharing one origin).
    // Add cluster search + origin estimation here - see doc/limelight3a/cluster-targeting-plan.md
```

Chapter 15 explains clusters and the plan for them.

> **Argue it.**  The `TODO` puts cluster search in this file.  Finding a
> cluster's origin takes the offsets of this season's goal tags, and those are
> one game's facts, the kind the first question in 2.9 screens out.  Keeping
> the whole cluster job in one file makes it easy to find.  Where should the
> line go:  all of it here, all of it in a class of its own, or the search
> here with the offsets handed in as a parameter?

> **Try it.**  `tryFindOneOf` is still waiting for a doc comment.  Write one in the style
> of `tryGetFiducial`'s:  what it looks for, what it hands back when it finds
> nothing, and which tag it hands back when the camera sees more than one.  A
> student reading only your comment should know to check for `null`, and
> should know that the camera's order picks the tag that comes back.

*See also:*  1.2, 1.9, 2.4, 2.9, Chapter 5, Chapter 15

## 2.8  Too narrow, and too broad

A shared file can go wrong in two directions.  A helper can be too narrow,
about one robot when the file is for every robot.  Or the file can get too
broad, and collect helpers that have little to do with each other.  The
team's `Extensions` files avoid both today, so the example below is made up.

### Too narrow

Here's how Billy's intake reads the gamepad, in `BillyMA`:

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

Up on the d-pad runs the intake forward at three-quarters power, down runs
it backward, and anything else stops it.  Now here's a made-up method that
copies that rule into `GamepadExtensions`.  This method is invented:

```
public static double GetBillyIntakeCommand(Gamepad gamepad) {
    if (gamepad.dpad_up)   return 0.75;
    if (gamepad.dpad_down) return -0.75;
    return 0.0;
}
```

> **Predict.**  Its only parameter is a `Gamepad`, and it's `static`, in the
> right class.  Why doesn't it belong in `GamepadExtensions`?

Ask the first question from 2.9:  would every robot the team builds want this
method, unchanged?  Only a robot with an intake, run by those two buttons, at
those two power levels.  That's one robot's design choice.  It's a fact about
Billy, and the parameter list hides it, because a `Gamepad` is exactly what a
`GamepadExtensions` method is supposed to take.  The body is where it fails.

So the rule belongs where it is now:  in the lambda `BillyMA` hands its
intake, which Chapter 8 calls a control strategy.  Put it in the shared file
and every other robot gets a method made for Billy, right beside the ones it
uses.

### Too broad:  the junk drawer

The other way to go wrong happens slowly.  Someone adds a one-off helper to a
shared file because it seemed related enough.  Someone else adds another.
Each one has one caller.  After a season the file is a place things get
dropped.

The test is a sentence.  Describe the whole file's job in one, and see if it
still covers every method:

| File | Its job, in one sentence |
|---|---|
| `ThreadExtensions` | pause safely |
| `GamepadExtensions` | read a gamepad's sticks, cleaned up |
| `AngleExtensions` | do arithmetic on headings that wrap around |
| `LimelightExtensions` | find tags in what the Limelight sees |

All four pass.  The day the sentence needs *"and also it has this one thing
for the endgame routine,"* that method belongs somewhere else.

*See also:*  2.5, 2.9, Chapter 8

## 2.9  Does this helper belong in `Extensions`?  The three-question test

You've written a small `static` method, and you're deciding where it goes.
Ask three questions, every time.

1. **Scope, from the top.**  Would every robot the team builds want this
   method, unchanged?  For a helper on someone else's type, read "every
   robot that has the type" (2.7).  This catches the method that's too narrow,
   like `GetBillyIntakeCommand` (2.8).
2. **Scope, from the bottom.**  Does the file's whole list of methods still
   describe one job in a single sentence?  This catches the junk drawer (2.8).
3. **Presentation.**  Could a student use this method correctly from its name
   and the first line of its doc comment, without reading the body?  This
   catches the trouble `AngleExtensions` has with its names (2.6).

The first two ask where code lives.  The third asks what it's called.  Those
are separate decisions, and a file can get one right and the other wrong.
`AngleExtensions` passes the first two and has work left on the third.
`tryGetFiducial` passes all three (2.7).

### Three made-up proposals

Here's the test run on three methods.  All three are invented.

**`inchesToTicks(double inches, double wheelDiameter, int ticksPerRev)`**
turns a distance into encoder ticks.  It belongs in `Extensions`.  Every robot
with encoders on its wheels wants it, and every number it needs comes in
through a parameter, so question 1 passes.

**`inchesToTicks(double inches)`** does the same math with Billy's wheel size
and encoder counts written inside it.  It fails question 1.  A different
robot's wheels would quietly get Billy's numbers.  It belongs wherever that drive train's own numbers already live.

**`shouldFireShooter(int ballCount, double batteryVoltage)`** decides when to
shoot.  That's a decision about how to play the game, and it fails question 1
before the others come up.  It belongs with the rest of that decision:  in
`BillyMA`, or in a macro strategy (Chapter 13).

The Watch out in 2.2 gives the same answer from a different side.  A helper
that needs one robot's motor or settings to do its job is a helper about that
robot.  Hand those things in as parameters, and it might pass question 1.  If
it only makes sense with Billy's values, it belongs on Billy.

> **Try it.**  Pick any `static` method in `TeamCode` outside the
> `Extensions` folder.  Run the three questions on it, and write one line for
> each answer.  If all three pass, you've found a candidate for
> `Extensions`.  If question 1 fails, you've found a method that's in the
> right place already.

*See also:*  2.2, 2.4, 2.6, 2.7, 2.8, Chapter 13

## Chapter summary

An extension method is the team's Java version of a feature other languages
have:  a `static` method in a `SomethingExtensions` class, taking the type it
helps as its first parameter.  The pattern earns its place when you can't edit
the type, when a convention would otherwise be copied into every caller, or
when the logic is plain math.  Deciding what goes in a shared file takes three
questions, and the third, about names, is separate from the first two.

| To find out... | Read... |
|---|---|
| what an extension method is | 2.1 |
| what `static` means, and why there's no `new GamepadExtensions()` | 2.2 |
| why the team writes them at all | 2.3 |
| how to pause, with the interruption handled | 2.4 |
| how a stick reading gets cleaned up, and where that stops | 2.5 |
| how to find the shortest turn between two headings | 2.6 |
| how to ask the Limelight for a tag | 2.7 |
| how a helper can be too narrow, or a file too broad | 2.8 |
| whether your new helper belongs in `Extensions` | 2.9 |

### Java in this chapter

| Syntax | Where |
|---|---|
| `static` methods and fields | 2.2 |
| `static final`, a constant | 2.2 |
| a `private` constructor on a `final` class | 2.2 |
| `try` and `catch` | 2.4 |
| `long` | 2.4 |
| `Math.abs` | 2.5 |
| `while` | 2.6 |
| arrays (`double[]`) and a `for` loop | 2.6 |
| `int...`, varargs | 2.7 |

### Vocabulary

**constant** -- a field that's `static` and `final`, so there's one fixed
copy for the whole program.  `DeadZoneRadius` is one.  (2.2)

**deadzone** -- the band around zero that counts as zero, so a stick at rest
reads exactly `0`.  Chapter 4 owns it.  (2.5)

**extension method** -- in this codebase, a `static` method in a class named
`SomethingExtensions` that takes the type it helps as its first parameter.
`GamepadExtensions.GetLeftStickX(gamepad)` is one.  (2.1)

**fiducial** -- a marker a camera can find and identify.  On an FTC field,
an AprilTag.  Chapter 5 owns it.  (2.7)

**junk drawer** -- a shared file that has collected unrelated one-off
helpers, so its job no longer fits in one sentence.  (2.8)

**`static`** -- belonging to the class, with no object.  You call a `static`
method on the class's name.  (2.2)

**the three-question test** -- scope from the top, scope from the bottom, and
presentation:  the three questions that decide whether a helper belongs in
`Extensions`.  (2.9)

**utility class** -- a class with only `static` members, a named home for a
set of related helpers.  (2.2)

**varargs** -- a parameter written with three dots, like `int... tags`, that
takes any number of values.  Inside the method they arrive as an array.  (2.7)

### Check your understanding

1. Java has no way to add a method to `Gamepad`.  What does the team write
   instead, and how does the call look different from the C# version?  (2.1)
2. Why can't a `static` method use a field like `intake`?  (2.2)
3. Name the three reasons the `Extensions` folder exists, with one file for
   each.  (2.3)
4. What does `TrySleep` hand back if its sleep gets cut short?  (2.4)
5. `GamepadExtensions.GetLeftStickY` returns `-0.6`.  Which way is the stick
   pushed, and what decides what that number does?  (2.3, 2.5)
6. What does `getSmol(10, 350)` hand back, and which way does `turnToAngle`
   turn?  (2.6)
7. `tryFindOneOf(limelight, 21, 22, 23)` comes back with tag 23 while 21 is
   also in view.  How can that happen?  (2.7)
8. Write the one-sentence job of a shared file you know.  Then name a method
   that would break the sentence.  (2.8)
9. A teammate proposes `armToScoringHeight(DcMotor arm)`, with Billy's arm
   height written inside.  Run the three questions on it.  Where does it
   belong?  (2.9)

## Where to go next

Chapter 4 explains the deadzone and the other ways a raw sensor reading gets
cleaned up.  Chapter 5 takes apart the tag search in `LimelightExtensions`.
Chapter 8 is where robot-specific rules like Billy's intake buttons live.
Chapter 12 shows how to wait while the rest of the robot keeps running.
