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
