# Chapter 4 - Sensors, and Where They Live

*What does this sensor let the robot know, and who needs to know it?*

---

Chapter 3 listed the sensors the SDK can read.  This chapter is about what to
do with one once it's plugged in.  A sensor hands back a number or a
`true`/`false`, and the robot almost always wants something else:  a wall
close enough, a ball in place, a turret pointed the right way.  Turning one
into the other is half of this chapter.

The other half is where the sensor goes.  The team's robot model has layers
(Chapter 6), and every sensor belongs to exactly one of them.  There's a rule
for picking the layer, and the code already states it.

Look here when you're adding a sensor, when a reading jumps around or makes no
sense, or when you're deciding which class should own a sensor.  The whole
chapter comes back to one question, the one in the tagline.

Read first:  Chapter 3, for the sensor types themselves.

| Entry | The question it answers |
|---|---|
| 4.1 | What's the difference between a sensor and an actuator? |
| 4.2 | What does a reading mean? |
| 4.3 | Why does a reading flicker, and how do I stop it? |
| 4.4 | What does an out-of-range reading look like? |
| 4.5 | What should the robot do when a sensor has no answer? |
| 4.6 | Which layer owns a sensor? |
| 4.7 | What would a sensor on an assembly look like? |
| 4.8 | Why does the robot own the Limelight? |
| 4.9 | Who sets up the IMU? |
| 4.10 | How does a sensor change a component? |

---

## 4.1  A sensor answers a question

*Where it lives:*  `AutonStrategies/BillyRapidFire.java`

Every motor and servo on the robot is an **actuator**:  it takes a command and
moves.  Every sensor answers a question.  The code reads differently on each
side.  `motor.setPower(power);` is a command, and nothing comes back.
`forwardSensor.isPressed()` is a question, and an answer comes back, `true` or
`false`.

A robot with no sensors can only do what it was told, in the order it was
told, and trust the field to cooperate.  A robot with sensors can check.
Almost everything clever in autonomous comes from that.

Here's the start of Billy's rapid-fire routine.  It spins the flywheel up,
then fires:

```java
    public IState startShooter() {
        mechAssembly.reportStatus("startShooter");
        return
                doAndWait(
                        () -> mechAssembly
                                .autonFlywheel
                                .setPower(0.6),
                        2200,
                        fire()
                );
```

`doAndWait` does the first thing, waits the given number of milliseconds, and
moves on to the next (Chapter 12).  So this sets the flywheel to `0.6` power,
waits 2200 milliseconds, and fires.

> **Predict.**  What does that 2200 assume about the flywheel?  What would
> have to be true for it to be the right number every time?

It assumes the flywheel always reaches shooting speed in the same time.  On a
fresh battery it might get there sooner.  On a tired one, at the end of a long
practice, it might take longer, and the first ball leaves slow.  The routine
has no way to ask the flywheel how fast it's going, so it waits a time that
worked when somebody tested it.

That's a reasonable thing to do with no sensor, and plenty of good robots do
it.  A sensor would let the robot wait on a real answer.  An encoder on the
flywheel could say "up to speed" (3.3).  A sensor between the intake and the
pusher could say "a ball is ready" (4.7).

*See also:*  3.3, 4.7, Chapter 12

## 4.2  Reading against meaning

*Where it lives:*  `RobotModel/Mechs/Components/DoublyLimitedMotor.java`

A sensor hands back a number or a `true`/`false`.  The rest of the robot almost
always wants to know something else.

| The sensor says | What the robot wants to know |
|---|---|
| `touch.isPressed()` is `true` | the arm has reached the bottom of its travel |
| `distance.getDistance(CM)` is `4.2` | the wall is close enough to score against |
| the hue is near purple | there's a purple game piece in front of the sensor |
| the IMU's yaw is `-87.3` | the robot is facing the side wall |

The left column is a **reading**:  what the sensor measured.  The right column
is a **meaning**:  what the reading says about the robot and the field.  The
meaning is what belongs in the rest of the code.  Code that asks "is the wall
close enough?" reads like the plan.  Code that asks "is the distance under
6.0?" makes every reader work out the plan again, and every caller has to
agree on the 6.0.

`DoublyLimitedMotor` shows how it's done.  Its switches answer `isPressed()`.
The component turns that into a question about the mechanism:

```java
    public boolean canGoForward()
    {
        return ! forwardSensor.isPressed();
    }
```

Same bit underneath.  `isPressed()` is about a switch.  `canGoForward()` is
about the motor, and anybody can call it without knowing a switch is
involved.  The `!` flips the answer (Chapter 1):  pressed means it can't go
forward, and not pressed means it can.

`CascadeArm` shows the payoff.  Its telemetry puts the meaning on the Driver
Station screen, in the driver's own words:

```java
                    telemetry.addData("Can Go Up", dlm.canGoForward());
```

The same rule runs through the autonomous verbs in Chapter 11, where a method
says what it does, `pushBalls()`, and hides the number, `0.8`.  A sensor's
version of a verb is a question, named for its meaning.  Two places in the
code already name the questions they'd like answered.  A `TODO` on `Claw`
suggests a grip sensor that "gives `isHolding()` as an autonomous verb worth
having."  A `TODO` on `MechAssembly` suggests `isBallSeated()` (4.7).

> **Try it.**  Name the method for a lift's bottom switch, a magazine's color
> sensor, and a distance sensor on the front bumper.  Name each one for what
> it tells the robot.  Compare yours with `canGoForward()`:  could someone call
> it without knowing what kind of sensor is inside?

*See also:*  3.5, 4.6, 4.7, Chapter 11

## 4.3  Noise, thresholds, and hysteresis

*Where it lives:*  `AutonStrategies/LimelightAutoTarget.java`

Point a distance sensor at a wall that isn't moving, and print the reading
every loop.  It comes back `20.1`, then `19.8`, then `20.3`.  The wall stayed
put.  The reading wobbles anyway, because every real measurement has a little
error in it.  That wobble is **noise**.

So a test like `if (distance == 20.0)` almost never comes true.  Compare
against a **threshold**, a line the reading has to cross:  `distance < 20`.

A threshold has a problem of its own.  Park the robot right at 20 and the
noise carries the reading back and forth across the line, so the answer flips
between `true` and `false` many times a second.  Anything that acts on the
answer flips with it.

The fix is two thresholds with a gap between them.  Here's a made-up example,
the robot deciding whether it's close enough to a wall:

```
if (distance < 18) closeEnough = true;
if (distance > 22) closeEnough = false;   // 18..22 keeps whatever it already decided
```

Under 18, the answer becomes yes.  Over 22, it becomes no.  Between them, the
answer is whatever it was last.  To change its mind, the reading has to move
all the way across the gap, and noise is too small to do that.  The gap is
called **hysteresis**.

Billy's turret tracker uses a close cousin.  Once it sees its tag, it turns
toward it until the tag is near the middle of the picture:

```java
            double tx = tag.getTargetXDegrees();
            if(tx < -2) return rotateCCW(tx);
            else if(tx > 2) return rotateCW(tx);
            else            return stopTurret();
```

`tx` is how many degrees the tag sits left or right of center (Chapter 5).
Anywhere from `-2` to `2` counts as centered, and the turret stops.  Without
that band, it would chase every tiny wobble in `tx`, twitching left and right
around the tag.  A band around a target that counts as "there" is a
**deadband**.  The stick **deadzone** in `GamepadExtensions` is the same idea
for a joystick (2.5):  any reading closer to zero than `0.05` counts as
exactly zero.

Hysteresis and a deadband solve the same problem, sensors and hands that
jitter, in slightly different shapes.  A deadband turns a small error into
"close enough, stop."  Hysteresis keeps a yes-or-no decision from flickering.

> **Try it.**  Run the SDK sample `SensorREV2mDistance` with the sensor
> pointed at a wall, and watch the `range` line on the Driver Station.  Write
> down the smallest and largest readings you see in ten seconds.  That spread
> is the noise, and your hysteresis gap needs to be wider than it.

*See also:*  2.5, 3.6, Chapter 5

## 4.4  Out-of-range readings

A distance sensor with clear air in front of it still answers.  It hands back
a number, and the number is very large (3.6).  On the Driver Station it looks
like any other reading.

That's the trap.  Code that trusts the number concludes the wall is far away,
and drives forward.  The wall was there all along, past what the sensor can
measure, or at an angle the sensor missed.

Every sensor has a range of answers that can be true for your robot.  Before
the code acts on a reading, check that it's in that range.  For a distance
sensor on the field, a reading far larger than the field itself is one to
doubt.

A reading can also look absurd and be fine.  Headings wrap from `180` to
`-180`, so a heading that jumps by almost 360 between two loops is usually a
small turn across that line.  `AngleExtensions` does that arithmetic (2.6).
Know which kind you're looking at before you throw a reading away.

REV's 2m Distance Sensor also has a method that says whether its last reading
timed out.  The SDK sample prints it:

```java
            telemetry.addData("did time out", Boolean.toString(sensorTimeOfFlight.didTimeoutOccur()));
```

`sensorTimeOfFlight` is the same sensor, as a `Rev2mDistanceSensor` (3.6).
When it says `true`, treat that reading as suspect.

What the robot does with a reading it doesn't believe is the question in 4.5.

*See also:*  2.6, 3.6, 4.5

## 4.5  When a sensor has no answer

*Where it lives:*  `AutonStrategies/LimelightAutoTarget.java`

Sometimes the honest answer is "I can't tell."  The camera sees no tag.  The
distance reading is out of range.  The sensor isn't plugged in.  Every one of
these needs a plan, written in the code ahead of time.  If nobody writes one,
the robot gets the default, and the default is a crash.

Billy's turret tracker has a plan.  When `tryGetFiducial` can't find the tag,
it hands back `null` (2.7), and the tracker checks for that first:

```java
            if(tag == null)
            {
                turret.reportStatus("Tag " + targetTagId + " not found.");
                return stopTurret();
            }
```

It says so on the Driver Station, stops the turret, and looks again on the next
loop.  Stopping is a safe choice for a turret:  a stopped turret can't point
anywhere wrong.

The example autonomous in `ExampleStateMachineAuton` has a different plan for
the same situation:

```java
            if(tag == null)
            {
                telemetry.addLine(
                        "Tags 21, 22, 23 not found");
                return findTags();
            }
```

It reports, then keeps looking, and it doesn't move until it sees one of its
three tags.  The robot needs that tag to decide where to drive.

Both plans are right for their job.  The answer to "what if the sensor can't
tell?" depends on the job, so ask it for every sensor.  Good answers are usually one of these:  stop, keep the last
good value, fall back to a slower method that works without the sensor, or
wait.

Leave out the check and use `tag` anyway, and the code crashes when it calls
a method on `null`.  Java calls that a `NullPointerException` (Chapter 5).

A device missing from the configuration fails earlier.  `hardwareMap.get`
stops the OpMode at init and names the device it couldn't find (3.1).

*See also:*  1.2, 2.7, 3.1, Chapter 5, Chapter 12

## 4.6  Which layer owns a sensor?

*Where it lives:*  `RobotModel/Robots/Robot.java`

The team wrote the rule down, at the top of `Robot.java`:

```java
 * <p>The sorting rule runs all the way down the model: a sensor guiding one
 * mechanism belongs in that {@code MechComponent};  one describing the
 * relationship between mechanisms belongs in the {@code MechAssembly};  one
 * describing the whole robot, or wanted by more than one subsystem, belongs
 * here.
```

So a sensor belongs to the layer that can act on what it says.  Three
layers, three questions (Chapter 6 introduces the layers):

| If the sensor... | It belongs to the... | Example |
|---|---|---|
| guides one mechanism, and protects it from itself | `MechComponent` | `DoublyLimitedMotor`'s two switches |
| describes how two mechanisms work together | `MechAssembly` | none yet;  4.7 |
| describes the whole robot, or more than one part wants it | `Robot` | the Limelight (4.8), the IMU (4.9) |

That's the **sensor ownership rule**.  Ask the question in this chapter's
tagline, *what does this sensor let the robot know, and who needs to know
it?*, and the answer names the layer.

In the component row, `DoublyLimitedMotor`'s `setPower` checks its switches
and turns a request into a pressed switch into zero (8.6).  It refuses the
request right there, where it could have reported it upward.  A rule that has
to apply no matter who's calling gets enforced where the hardware is.  The switches exist to stop *this* motor
hurting *itself*, so they live with this motor.

> **Watch out.**  The check runs when `setPower` is called.  In TeleOp that's
> every loop.  The autonomous `goForward()` calls it once, at the start, and a
> `TODO` above the class asks for the limits to be checked for the whole
> move.  Until that's built, an autonomous move can run past a switch it
> started clear of (8.6).

> **Argue it.**  A voltage sensor reads the battery, so it could boost motor
> power as the battery runs down.  It describes the whole robot, so the
> `Robot` owns the reading.  But every component would want to use it.  How
> does a reading owned at the top reach every motor at the bottom, and what
> does that cost each component's design?

*See also:*  3.10, 4.7, 4.8, 4.9, Chapter 6, Chapter 7, Chapter 8

## 4.7  A sensor on an assembly

*Where it lives:*  `RobotModel/Mechs/Assemblies/MechAssembly.java`

No assembly in the code owns a sensor yet.  The team knows it, and says so in
`MechAssembly.java`:

```java
 * <p>TODO: no assembly in this codebase owns a sensor yet, so there is no
 *  reference implementation to copy, and the `doc/FlintLessons` have no example
 *  to teach from. Billy is the obvious candidate. A beam-break or color sensor
 *  sitting between the intake and the pusher would answer "is a ball actually
 *  seated and ready to fire?" - a question that spans intake, pusher and
 *  flywheel, so it fits in none of them. With it, BillyRapidFire could wait on
 *  a real signal instead of the fixed 2200ms guess it currently uses.
```

A **beam-break** sensor is a light shining across a gap at a detector.  A ball
in the gap blocks the light.

Follow the ownership rule (4.6).  Could the intake own the sensor?  The intake
knows when it's spinning, and has no idea whether the pusher is ready.  Could
the pusher?  Same problem, the other way around.  The question is about the
*handoff* between them, so it belongs to the thing that owns both:  `BillyMA`,
the assembly.

The same `TODO` lists what the assembly would need.  The assembly fetches the
sensor in its constructor, with its components.  It reports the reading in
`updateTelemetry()`.  And it exposes what the reading *means*:

```java
 *    <li>the assembly's {@code AutonomousMechBehaviors} exposes what the sensor
 *        <em>means</em> as a verb or a question ({@code isBallSeated()}), rather
 *        than the raw reading</li>
```

That's 4.2's rule again, at the assembly layer.  Code outside the assembly
asks `isBallSeated()`, and the beam-break's raw state is the assembly's
business.

And it would fix 4.1.  `BillyRapidFire` waits 2200 milliseconds, hoping a ball
is ready and the flywheel is up to speed.  With `isBallSeated()`, it could
wait until the answer is yes, however long that takes.

*See also:*  4.1, 4.2, 4.6, Chapter 9, Chapter 12

## 4.8  Why the robot owns the Limelight

*Where it lives:*  `RobotModel/Robots/BillyRobot.java`

`BillyRobot` fetches the Limelight, sets it up, and hands it to the turret
tracker, `LimelightAutoTarget`, along with the turret's autonomous behaviors.
10.8 shows those lines.  `Turret` itself has no camera field and never reads
one.  It turns when it's told to.

The rule in 4.6 says why the camera sits that high.  A camera looking at the
field describes where the robot is and what's around it.  That's a fact
about the whole robot, and the turret is only one of the things that might
want it.  The drive train could use it to line up.  An autonomous routine
could use it to find its starting position (Chapter 14).

So the robot owns it, and that gives three things.  **One owner:**  one class
fetches the device.  **One setup:**  the polling rate, the start, and the
pipeline are set in one place.  **Handed down:**  whoever needs the camera gets
it as a parameter.  `Robot.java` puts it this way:

```java
 *   <li>The <b>Limelight</b> is the pattern done right. {@code BillyRobot} owns
 *       it, and passes it into {@code LimelightAutoTarget} alongside the turret's
 *       autonomous behaviors. Neither the turret nor the assembly knows a camera
 *       exists.</li>
```

The turret gets something from this too.  It knows nothing about cameras, so
it works the same whether a camera, a gamepad, or an autonomous routine is
telling it what to do.

*See also:*  3.8, 4.6, 4.9, 10.8, Chapter 5, Chapter 13

## 4.9  Who sets up the IMU?

*Where it lives:*  `RobotModel/DriveTrain/Mecanum/MecanumDrive.java`

The IMU reports which way the whole chassis faces (3.7).  By the rule in 4.6,
it belongs to the robot, like the Limelight.  Today two classes fetch it.

`BillyRobot` fetches the IMU, tells it how the hub is mounted, and zeroes it
(3.7 shows the code).  `MecanumDrive` fetches the same device again in its own
constructor, and uses it for `turnToAngle()`:

```java
        imu = hardwareMap.get(IMU.class, "imu");
```

On Billy that works, because `BillyRobot` has already set up the device both
classes are reading.  `Wildbots2025` also uses a `MecanumDrive`, and nothing in
it calls `initialize`.  So on that robot, `turnToAngle()` reads heading from an
IMU that was never told how its hub is mounted.

A `TODO` above that line describes the next step, and it follows the
Limelight's pattern.  The robot fetches the IMU, initializes it once with
that robot's own hub orientation, and hands it down.  `MecanumDrive` takes it
as a constructor parameter, the way it already takes its
`OrientationConfiguration`, and its own lookup goes away.  The `TODO` sums it
up as "One owner, one initialize."

The difference between the two sensors is the whole lesson.  The Limelight has
one owner and one setup, and it's handed down to whoever needs it.  The IMU
has two classes fetching it, and only one of them knows it needs setting up.

Two projects wait on this one.  Another `TODO` in `MecanumDrive` describes
**field-relative drive**, where pushing the stick forward always means "away
from the driver," whichever way the robot faces.  It also describes **heading
hold**, which keeps a strafing robot from slowly turning.  Both need the IMU,
and the `TODO` asks for the ownership question to be sorted out first.

*See also:*  3.7, 4.6, 4.8, 10.8, Chapter 14

## 4.10  How a sensor changes a component

*Where it lives:*  `RobotModel/Mechs/Components/Claw.java`, `Turret.java`

Adding a sensor to a component can change what kind of component it is.
`Turret` and `Claw` each wrap one `CRServo` today.  Give the turret two limit
switches, or the claw a grip sensor, and each becomes the second kind of
component:  devices plus the sensors that guide them (7.7).  7.8 tells that
story.

There's one more thing to know before wiring the sensor.  A sensor check only
works if every command goes through it.  The `TODO` in `Claw.java` names the
three ways a command could get past one today:

```java
    //    1. `servo` below is public - make it private.
    //    2. ClawControlStrategy.chomp(CRServo, Gamepad) hands the raw servo to the
    //       teleop lambda. Change it to chomp(Gamepad, Claw) and call
    //       `strategy.chomp(gamepad, this)`, so the lambda goes through this class.
    //    3. AutonomousClawBehaviors.open/close/stop call servo.setPower(...) directly.
    //       Route them through a decorated setPower on Claw.
```

Each one is a path to the servo that skips the class's own methods.  A
`public` field lets any code grab the servo (1.3).  The control strategy gets
handed the servo itself, so the driver's buttons command it directly.  The
autonomous verbs call the servo too.  Put a limit check in a new `setPower`,
and all three paths would walk right past it.

`Turret.java` has the same three, in the same order.  `DoublyLimitedMotor`
already has all three closed:  its strategy gets the component, so every
command goes through its checked `setPower` (8.6).

> **Watch out.**  When you add a sensor to a component, find every line that
> commands the device.  A guard that some code can go around is a guard the
> robot only obeys sometimes.

*See also:*  1.3, 4.6, 7.7, 7.8, 8.6

## Chapter summary

A sensor answers a question, and the robot wants the meaning behind the
answer:  a wall close enough, a ball ready, a turret on target.  Real readings
are noisy, can run out of range, and can be missing, and the code has to
decide ahead of time what to do about each.  Then the sensor goes to the layer
that can act on what it says:  one mechanism, two mechanisms working together,
or the whole robot.

| To find out... | Read... |
|---|---|
| why a robot needs sensors at all | 4.1 |
| how to turn a reading into a meaning | 4.2 |
| why a reading flickers, and how to stop it | 4.3 |
| what a bad reading looks like | 4.4 |
| what to do when the sensor can't tell | 4.5 |
| which layer owns a sensor | 4.6 |
| where Billy's ball-ready sensor would go | 4.7 |
| why the Limelight belongs to `BillyRobot` | 4.8 |
| why the IMU has two owners, and the plan to fix it | 4.9 |
| what to check before adding a sensor to a component | 4.10 |

### Java in this chapter

| Syntax | Where |
|---|---|
| `!`, flipping a `boolean` | 4.2 |
| `==` against `<` and `>` on a `double` | 4.3 |
| a `null` check before using a result | 4.5 |

### Vocabulary

**actuator** -- a device that takes a command and moves:  a motor or a
servo.  (4.1)

**beam-break** -- a sensor that shines light across a gap at a detector, so
anything in the gap blocks it.  (4.7)

**deadband** -- a band around a target that counts as "there," like `-2` to
`2` degrees in the turret tracker.  (4.3)

**deadzone** -- a deadband for a joystick:  readings closer to zero than
`0.05` count as exactly zero.  (4.3, 2.5)

**hysteresis** -- two thresholds with a gap between them, so a yes-or-no
decision changes only when the reading crosses the whole gap.  (4.3)

**meaning** -- what a reading says about the robot and the field, like "the
arm is at the bottom."  (4.2)

**noise** -- the small, random wobble in every real measurement.  (4.3)

**out-of-range value** -- a reading that looks like a number and can't be
true for your robot, like a huge distance with nothing in front of the
sensor.  (4.4)

**reading** -- what a sensor measured, like `true` or `4.2`.  (4.2)

**sensor** -- a device that answers a question about the robot or the
field.  (4.1)

**sensor ownership rule** -- a sensor belongs to the layer that can act on
what it says:  the component, the assembly, or the robot.  (4.6)

**threshold** -- a line a reading has to cross for an answer to change.  (4.3)

### Check your understanding

1. `motor.setPower(power)` and `touch.isPressed()`:  which one is a
   question, and how can you tell from the code?  (4.1)
2. What does `BillyRapidFire`'s 2200 milliseconds assume?  (4.1)
3. Name a method for a magazine's color sensor, named for what it tells the
   robot.  (4.2)
4. Why does `if (distance == 20.0)` almost never come true?  (4.3)
5. In the hysteresis example, the reading goes 17, 20, 23, 20.  What's
   `closeEnough` after each one?  (4.3)
6. A distance sensor reads a huge number.  Name two things that might be in
   front of it.  (4.4)
7. The turret tracker and `findTags` both get `null`.  What does each do, and
   why are they different?  (4.5)
8. A touch sensor at the bottom of a lift stops it crushing itself.  Which
   layer owns it, and what should it expose?  (4.6)
9. A distance sensor on the front bumper is used to stop before a wall and to
   line up the scoring mechanism.  Which layer owns it?  (4.6, 4.8)
10. Why can't the intake own Billy's ball-ready sensor?  (4.7)
11. What does `Wildbots2025`'s `turnToAngle()` read from, and what's the fix
    the `TODO` describes?  (4.9)
12. Name the three ways a command could get past a limit check on `Claw`.
    (4.10)

## Where to go next

Chapter 5 is about the biggest sensor on the robot, the camera, and what its
answers look like.  Chapter 7 explains the two kinds of component a sensor can
turn one into.  Chapter 9 is where the first assembly sensor would go.
Chapter 12 shows how to wait on a sensor's answer without stopping the robot.
