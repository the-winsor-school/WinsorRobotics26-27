# Chapter 19 - Linear Motion and Control Electronics

*An elevator, and everything it plugs into.*

---

This chapter puts two topics side by side.  The first is the elevator, the
mechanism this team builds most often to reach a high scoring spot.  The second
is the electronics every mechanism plugs into:  the hubs, the wires, and the
sensors.

A lift is where the two meet.  It needs a motor strong enough to raise it, a
switch at each end to stop it, and wires to those switches that survive every
trip up and down.

Look here when you're choosing how to rig a lift, when a lift needs protecting,
or when a sensor reads garbage and you want to know why.

Read first:  Chapter 17, for torque, gear ratios, and the HD Hex Motor.
Chapter 3, for how the code reaches a motor or a sensor.

| Entry | The question it answers |
|---|---|
| 19.1 | What is a linear slide made of? |
| 19.2 | Should a lift be cascading or continuous? |
| 19.3 | Why is a cascading lift so fast? |
| 19.4 | How does a lift come back down quickly? |
| 19.5 | What does a constant-force spring do for a lift? |
| 19.6 | How does the code keep a lift from wrecking itself? |
| 19.7 | How does the robot know where the bottom is? |
| 19.8 | Which hub does what, and how do they talk? |
| 19.9 | How do you wire a robot so it keeps working? |
| 19.10 | Which sensor answers which question? |

---

## 19.1  What a linear slide is made of

*Where it lives:*  `doc/rev-duo/linear-motion.md`

A **linear slide** is a mechanism that moves in a straight line, one section
sliding along another.  Each sliding section is a **stage**.  Stage 1 rides on
the base, Stage 2 rides on Stage 1, and so on up.  Stack a few stages and you
have an elevator.

REV builds its slides out of the same 15mm extrusion as the rest of the robot
(Chapter 16).  The slot that runs the length of the extrusion becomes the track.
REV's slide system is REV-45-1507, and it's built from four kinds of part.

| Part | What it does |
|---|---|
| Slider block (REV-41-1508) | a low-friction Acetal block that slides inside the extrusion's slot |
| Slider bracket | sheet aluminum that sandwiches the slider blocks and bolts to the next stage |
| Pulley (REV-41-1510) | a Delrin pulley on a ball bearing, mounted at the end of an extrusion to route the cord |
| Cord | braided UHMWPE, sold as Dyneema or Spectra.  It keeps its length under tension. |

A motor turns a **spool**, a small drum that winds the cord onto itself.  The
cord runs from the spool over the pulleys to the stages, so reeling it in pulls
the lift up.  Whatever rides on the top stage is the **carriage**:  a claw, a
bucket, or a scoring arm.

*See also:*  19.2, 20.6, Chapter 16

## 19.2  Cascading or continuous?

*Where it lives:*  `doc/rev-duo/linear-motion.md`

**Rigging** is the way the cord runs between the stages of a lift.  REV slides
use one of two kinds.

In **cascading rigging**, every stage extends at the same time.  Each stage
gets its own loop of cord, and the stages pull each other along.

In **continuous rigging**, one cord runs through the whole lift.  The stages
extend one after another, bottom to top.

Here is how they compare, for a lift with $n$ stages.

| | Cascading | Continuous |
|---|---|---|
| Extension | all stages at once | one stage after another |
| Speed | $n \times$ spool speed | equal to spool speed |
| Motor torque needed | $n \times$ load weight | $1 \times$ load weight |
| Rigging | a separate cord loop for every stage | one cord run |
| Coming down | driven by the motor (19.4) | usually gravity or springs |
| Cord slack risk | low, if the stages are pre-tensioned | high.  A fast drop can jump the cord off a pulley. |

> **Predict.**  A 3-stage cascading lift reaches full height in a third of the
> time a continuous lift takes.  What did that speed cost?

It cost torque.  A 3-stage cascade has to pull three times as hard as a
continuous lift carrying the same carriage, because it moves the whole load
through every stage at once.  19.3 shows where the three comes from.

So cascading costs torque, and a cord loop to rig and tension for every stage.
It buys speed, a motor-driven trip back down, and a cord that keeps its tension.
Continuous costs speed, and its cord can go slack on a fast drop.  It buys a
lift that any motor strong enough to carry the load can raise, with one cord to
route.  That's the trade-off:  speed, paid for in torque.  The team's
reference puts cascading on fast competition lifts and hanging, and continuous
on simpler reach mechanisms and light arms.

Here's the choice in practice.  A 3-stage lift has to reach full height in
under a second, and the only motor can lift the carriage's weight once.  A
cascade would need three times that.  Continuous is the only rig this motor can
raise, and whether it's fast enough is a gearing question for Chapter 17.

*See also:*  19.3, 19.4, 19.5, 20.6

## 19.3  How a cascade multiplies speed

A cascading lift is fast because each stage is pulled by a point that is itself
moving.

Start at the bottom.  The motor's spool is tied to Stage 1, and only Stage 1.
When the spool reels in an inch of cord, Stage 1 rises an inch.

Stage 2 gets a second cord.  One end is tied to the base, which is fixed to the
robot.  The cord runs up to a pulley at the top of Stage 1, over it, and back
down to the bottom of Stage 2.

Now watch Stage 1 rise an inch.  Its pulley rises with it, so the stretch of
cord from the base up to the pulley is an inch longer.  The cord's length is
fixed, so that inch comes out of the other side, the stretch from the pulley
down to Stage 2.  Stage 2 rises an inch compared to Stage 1.  Stage 1 has
already risen an inch, so Stage 2 is two inches higher than where it started.

Stage 3 plays the same trick one level up.  Its cord is tied to Stage 1, runs
over a pulley on top of Stage 2, and anchors to Stage 3.

| The spool reels in | Stage 1 rises | Stage 2 rises | Stage 3 rises |
|---|---|---|---|
| 1 inch | 1 inch | 2 inches | 3 inches |

That's why a 3-stage cascade reaches full height in a third of the time.  The
top stage moves three inches for every inch of cord.

The torque comes from the same table.  Work is force times distance (Chapter
17), and the work to lift the load is the same however it's rigged.  Three
times the distance at the top means three times the force at the spool.

In a continuous lift, the one cord moves one stage at a time, so an inch of cord
is an inch of lift, at the load's own weight.

*See also:*  19.2, Chapter 17

## 19.4  Active retraction

Gravity alone is often too slow to bring a lightweight multi-stage slide back
down during a match.  A light slide has little weight pulling it down, and the
friction in its sliders slows it further.

**Active retraction** means the motor drives the lift down as well as up.  On a
cascading lift, the trick is a **dual spool**, a spool with two wound sections
on the motor's shaft.

1. Wind the lift cord clockwise on section A.
2. Wind a retract cord counter-clockwise on section B.

When the motor turns forward, it reels in the lift cord and pays out the retract
cord, so the lift goes up.  When the motor reverses, it reels in the retract
cord and pays out the lift cord, and the lift comes down under full motor power.

This matters because of **cycle time**, the time it takes to collect a game
piece, score it, and get back for the next one.  A slide that falls under
gravity comes down as fast as friction and its own weight let it.  An actively
retracted slide comes down as fast as the driver commands.

A motor that drives the lift down at full power can also drive it into its
bottom stop, so this lift needs the bottom limit switch in 19.6.

*See also:*  19.2, 19.6, 20.6

## 19.5  Constant-force springs

A **constant-force spring** is a rolled strip of spring steel that pulls with
the same force however far it's unrolled.  REV sells them as REV-41-1644.  On a
lift, they solve a current problem.

Picture the lift paused at the top to score.  The motor isn't turning, and it's
still pushing as hard as it needs to keep the carriage from falling.  A motor
that pushes and doesn't turn is at stall (Chapter 17), and a motor at stall
draws its heaviest current.

> **Watch out.**  A lift parked at the top under motor power keeps its motor at
> stall current the whole time.  The motor heats up, and it can trip a breaker
> in the middle of a match.  A heavy carriage needs a counterbalance.

Springs are the counterbalance.  Two 1.5 lb constant-force springs offset 3 lb
of carriage weight.  With the weight offset, the motor holding that carriage
at the top only has to fight friction.

That frees up the gearing, too.  A motor that no longer fights gravity while it
holds can run a faster gear ratio, with less reduction (Chapter 17), and it
won't stall on the hold.

> **Argue it.**  Two springs make a 3 lb carriage feel nearly weightless to the
> motor.  Go back to the motor in 19.2 that could lift the carriage only once.
> With springs on, is a cascade possible after all?  What changes when the
> carriage picks up a game piece, or lets one go?

*See also:*  19.2, Chapter 17

## 19.6  Limits on a lift:  `DoublyLimitedMotor`

*Where it lives:*  `RobotModel/Mechs/Components/DoublyLimitedMotor.java`

Here's the problem.  The carriage reaches the bottom, and the driver keeps
holding the stick down for two more seconds.  The motor keeps pulling.  The
cord snaps, a bracket bends, or the motor stalls against the aluminum at the
end of travel.  It takes one bad button press in a real match.

> **Watch out.**  Put a limit switch at each end of a slide before it ever runs
> under power.  A driver mistake or a software crash will drive an unprotected
> slide into its stops.

A **limit switch** is a sensor at the end of a mechanism's travel that reports
when the mechanism gets there.  On this team that's a REV Touch Sensor, which
gets pressed, or a REV Magnetic Limit Switch, which feels a magnet (19.10).

The switch only reports.  Code has to listen.  On Wildbots2025, the lift is
built in `RobotModel/Mechs/Assemblies/CascadeArm.java`:

```java
cascade = new DoublyLimitedMotor(
        hardwareMap,
        "cascadeMotor",
        "topLiftLimit",
        "bottomLiftLimit",
        // Control strategy
        (gamepad, dlm) ->
        {
             double power = GamepadExtensions.GetLeftStickY(gamepad);
             dlm.setPower(power);
        },
        ((dlm, telemetry) ->
        {
            telemetry.addData("Can Go Up", dlm.canGoForward());
            telemetry.addData("Can Go Down", dlm.canGoReverse());
        })
        );
```

The constructor takes six things, in this order:  the hardware map (Chapter 3);
the motor's configuration name;  the **forward** sensor's name;  the
**reverse** sensor's name;  a control strategy, the lambda that turns the
gamepad into power (Chapter 8);  and a telemetry strategy, which writes the two
`Can Go` lines.  The control strategy is handed the gamepad first and the
`DoublyLimitedMotor` second.

The file's own comment says what forward means:  *"The forward sensor is the
sensor that is hit when the motor is given positive power."*

`GamepadExtensions.GetLeftStickY` flips the stick so that pushing up gives
positive power (Chapter 2).  Positive power raises this lift, so the top
switch is the forward one.

Every move the strategy makes goes through `setPower`.  Here it is, whole:

```java
    /**
     * this is a decorator method that checks to see if the motor can go forward or reverse before setting motor power
     * if the motor cannot move in that direction, it sets the power to zero
     * @param power assumes that power is between -1 and 1, and that the dead zone is applied
     */
    public void setPower(double power){

        if ((power > 0 && !canGoForward() )|| (power < 0 && !canGoReverse())) {
            power = 0;
        }

        motor.setPower(power);
    }
```

`canGoForward()` is true while the forward switch is unpressed.  So pushing up
with the top switch pressed sends zero, and pulling down with the bottom switch
pressed sends zero.  Driving *away* from a pressed switch always works, so the
lift can leave the end it's sitting at.

The motor itself is a `private` field.  The strategy, and the autonomous verbs
`goForward()`, `goBackward()` and `stop()`, can reach it only through this
method, so every path to the motor passes the check.  Wrapping a method that
way is a decorator (Chapter 8), and the class as a whole is a component
(Chapter 7).

So the switch and the code each do half the job.  The switch knows where the
lift is, and the code decides what to do about it.  Each one is useless alone:
a switch only reports, and the code can only act on what a switch reports.  The
aluminum stop at the end of travel is what the lift hits if both of them fail.

> **Watch out.**  Forward is whichever end the motor reaches on positive
> power.  Pass the two names in the wrong order and the guard protects the
> wrong end:  at the top, pushing up still runs the motor.  Reversing the
> motor's direction swaps the ends too.  Check the order every time you wire a
> new lift.  The lift example in `doc/rev-duo/linear-motion.md` passes the
> bottom switch first, and the compiler rejects it besides, so build from
> `CascadeArm` instead.

> **Try it.**  Run *Example TeleOp (Wildbots2025)* and lower the lift until it
> presses its bottom switch.  The Driver Station shows `Can Go Down` as
> `false` and `Can Go Up` as `true`.  Pull gamepad 2's left stick down:  the
> lift motor stays still.  Push it up gently, and the lift rises off the
> switch.  If the motor runs when you pull down, let go of the stick.  The names
> are in the wrong order, or the switch isn't being pressed.

*See also:*  19.7, 19.10, Chapter 7, Chapter 8

## 19.7  Zeroing the lift

A motor's encoder counts ticks as the shaft turns (Chapter 3).  The count
starts from wherever the motor was when it was last reset.  So a count of zero
means "where the lift was at the last reset," and that might be the bottom, or
halfway up.

**Zeroing** is finding a known physical position and resetting the count there.
After that, 1,000 ticks means the same height every time.

For a lift with a bottom switch, the team's reference gives the sequence.

1. Drive the lift slowly down until the bottom switch is pressed.  The reference
   uses a power of `-0.15`.
2. Call `motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER)`.  The count is
   now zero, at the bottom.
3. Switch to `RUN_TO_POSITION`, so the motor drives itself to a tick count you
   give it.  Set a target position before you switch.  The SDK sample
   `RobotAutoDriveByGyro_Linear.java` says this is required.  Run modes are in
   Chapter 3.

This has to happen every match, at the start.  The count means something only
compared to a known zero.  A robot that gets bumped, repowered, or
re-configured between matches can only learn where zero is by finding the
switch again.

> **Predict.**  Skip step 2 and go straight to `RUN_TO_POSITION`.  What goes
> wrong?

Every target is off by the same amount, because the count started from some
unknown number.  Ask for the scoring height and the lift drives to a spot higher
or lower than you meant.  Ask for something near the top and it can drive past
the real top into its stop.  The top limit switch is what catches that.

**An open gap.**  `DoublyLimitedMotor` can do step 1 today:  `setPower(-0.15)`
drives down, and its own check stops the motor when the bottom switch presses.
Steps 2 and 3 need the motor itself, and the class keeps its `DcMotor` private
with no method for run modes or positions.  Its autonomous verbs run at full
power or stop.  Building a zeroing verb into the component is open work, and
the file has no `TODO` for it yet.

*See also:*  19.6, Chapter 3, Chapter 11

## 19.8  The hubs:  who talks to whom

*Where it lives:*  `doc/rev-duo/control-and-sensors.md`

Three boxes run a REV robot.  Trace this line with a finger, from left to right:

```
 [Driver Hub] <--(5 GHz Wi-Fi)--> [Control Hub] <--(RS485 + XT30)--> [Expansion Hub]
```

The **Control Hub** (REV-31-1595) is the robot's computer, and its main motor and
servo controller.  Your code runs on it, and the Driver Hub talks to it by
Wi-Fi.  The Limelight 3A plugs into it over USB.

The **Expansion Hub** (REV-31-1153) is a second motor and servo controller, for
a robot with more than four motors or six servos.  It connects to the Control
Hub by two cables.  An RS485 cable carries the data, and an XT30 cable shares
the 12V power.

The **Driver Hub** (REV-31-1596) is the handheld driver station:  a 5.5"
touchscreen, two USB-A ports for the gamepads, and 2.4 or 5 GHz Wi-Fi.

| | Control Hub | Expansion Hub |
|---|---|---|
| Motors | 4 | 4 |
| Servos | 6 | 6 |
| I2C buses | 4 | 4 |
| Digital ports | 4 (8 channels) | 4 |
| Analog ports | 4 | 4 |
| Also | internal IMU, USB-A, USB-C, Wi-Fi | |

> **Predict.**  A robot already has six servos on the Control Hub.  Where does
> the seventh plug in, and how does that hub know what to do?

The Expansion Hub.  The Control Hub relays commands to it over the RS485 link.
The relay is invisible to your code.  It sees one hardware map (Chapter 3),
with every device on both hubs in it.

*See also:*  19.9, Chapter 3, Chapter 5

## 19.9  Wiring that survives a match

*Where it lives:*  `doc/rev-duo/control-and-sensors.md`

Loose wires cause the worst kind of failure:  intermittent.  The robot works on
the bench and drops a motor in the middle of a match.  REV uses keyed, locking
connectors to fight that.

| Connector | Carries | Looks like |
|---|---|---|
| XT30 | 12V power:  the battery, the power switch, and the line to the Expansion Hub | yellow, polarized, rated for 30A continuous |
| JST VH | motor power | white, 2-pin, locking.  Push until the latch clicks. |
| JST PH | sensors and encoders | small, 2.0mm pin spacing.  4-pin for I2C and encoders, 3-pin for analog and digital. |

> **Aside:  I2C**
>
> **I2C** is the way the hub talks to small sensors like the 2m Distance Sensor
> and the Color Sensor v3.  The hub sends a short digital message down the
> wire asking for a reading, and the sensor sends one back.  It travels on a
> 4-pin JST PH cable.  Each hub has four I2C buses.

Three rules keep the wiring alive.  Each one has a reason.

> **Watch out.**  Pinch the latch to unplug a connector, with your fingernails
> or small pliers.  Pulling on the wire pulls the crimp pin out of its housing.
> The plug often looks fine afterward, and then the connection fails at random
> in the middle of a match.

> **Watch out.**  Give every moving joint a service loop.  A wire pulled tight
> across a hinge or a slide flexes at the same spot on every cycle, and after
> enough cycles it breaks right at the joint.  The bench test passes.  The break
> comes in a tournament, after the lift has run all day.

A **service loop** is a deliberate loop of extra wire at a joint, so the joint
can move through its whole range and the wire flexes gently along the loop.
Hold the wire down everywhere else with zip-ties or REV Wire Clips
(REV-41-1156), so the slack is only where it's needed.

> **Watch out.**  Keep motor wires away from I2C cables.  Route the motor leads
> on the opposite side of the frame from the sensor wires.

The third rule is about **interference**:  electrical noise that one wire puts
into another.  A motor switching on and off draws pulses of high current, and
those pulses create electromagnetic interference, EMI for short.  An unshielded
I2C cable running beside a motor lead picks it up, and the noise can corrupt or
crash the sensor's messages.

So here's a symptom worth knowing.  A color sensor near the intake reports
garbage, but only while the intake motor is spinning.  With the robot sitting
still, it reads perfectly.  The sensor is probably fine.  Its cable is running
next to the intake motor's leads, and the third rule is the one broken.  A
sensor that reads garbage only some of the time is a strong hint to check the
routing before you replace the sensor.

> **Try it.**  At the robot, find every I2C sensor and follow its cable back to
> the hub.  Write down each place the cable runs alongside a motor lead.  While
> you're there, find every joint that moves and check it has a service loop.

*See also:*  19.8, 19.10, Chapter 4

## 19.10  Which sensor answers which question?

*Where it lives:*  `doc/rev-duo/control-and-sensors.md`

Sort sensors by the question each one answers.  Chapter 3 sorts the FTC
sensor types the same way, and shows the Java for each.

| Sensor | Technology | Answers | Plugs into |
|---|---|---|---|
| REV 2m Distance Sensor (REV-31-1505) | Time-of-Flight laser, 10mm to 2000mm | "How far away is that?" | I2C |
| REV Color Sensor v3 (REV-31-1557) | RGB color plus IR proximity | "What color is it, and is something close?" | I2C |
| REV Magnetic Limit Switch (REV-31-1462) | Hall effect | "Is a magnet here?" | digital |
| REV Touch Sensor (REV-31-1425) | momentary push-button | "Is something pressing on it?" | digital |
| REV Through Bore Encoder (REV-11-1271) | quadrature plus absolute PWM, 8,192 counts per revolution | "Exactly what angle or position, with no drift?" | motor encoder port, and a digital port |
| Control Hub IMU | 6-axis or 9-axis gyro and accelerometer | "Which way is the robot facing?" | built into the Control Hub |

- **Time-of-Flight** means the sensor times how long a pulse of laser light
  takes to bounce back, and works out the distance from that.
- A **Hall effect** sensor feels a magnetic field.  The Magnetic Limit Switch
  lights a blue LED when a magnet comes within 10mm.
- The **Through Bore Encoder** has two outputs.  The quadrature output counts
  ticks, the way a motor's encoder does (Chapter 3).  The absolute output
  reports the shaft's angle directly.  It slides onto a 5mm hex shaft with its
  insert (Chapter 16), and suits an arm's shoulder joint.

For a lift's home position, the magnetic switch has one real advantage over a
touch sensor.  A touch sensor works by getting hit, every single time the lift
comes home.  The magnetic switch only needs the magnet on the moving stage to
pass within 10mm, so a hard stop can't break it.

> **Argue it.**  `DoublyLimitedMotor` reads its limits as `TouchSensor`s
> (19.6), and the code is written around a switch that gets pressed.  The
> magnetic switch is out of the way of every hit.  For the next lift, which
> would you put at the bottom, and what would you check before you trusted the
> other one?

*See also:*  19.6, 19.9, Chapter 3, Chapter 4

---

## Chapter summary

A lift is a stack of stages pulled by cord from a spool.  Cascading rigging is
fast and hungry for torque, and continuous is slow and easy on the motor.  Every
lift needs a switch at each end, and code that listens to them.  Behind it all
are two hubs and a handheld screen, joined by cables routed with care.

| Idea | Where |
|---|---|
| cascading buys speed with torque, $n$ times over | 19.2, 19.3 |
| a dual spool drives the lift down | 19.4 |
| springs take the weight off a motor that's holding | 19.5 |
| `DoublyLimitedMotor` refuses power into a pressed limit | 19.6 |
| zero the encoder at the bottom switch, every match | 19.7 |
| the latch, the service loop, the routing | 19.9 |

### Java in this chapter

| Syntax | Where |
|---|---|
| `new DoublyLimitedMotor(...)`, with a strategy lambda and a telemetry lambda | 19.6.  Lambdas are in Chapter 1, strategies in Chapter 8. |
| `setPower` as a decorator | 19.6.  Decorators are in Chapter 8. |
| `setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER)`, `RUN_TO_POSITION` | 19.7.  Run modes are in Chapter 3. |

### Vocabulary

**active retraction** -- driving a lift down with the motor, on a second cord.
(19.4)

**cascading rigging** -- rigging where every stage has its own cord loop and all
the stages extend at once.  (19.2)

**constant-force spring** -- a rolled strip of spring steel that pulls with the
same force however far it's unrolled.  (19.5)

**continuous rigging** -- rigging where one cord runs through the whole lift and
the stages extend one after another, bottom to top.  (19.2)

**Control Hub** -- the robot's computer and main motor and servo controller.
(19.8)

**Driver Hub** -- the handheld driver station.  The gamepads plug into it.
(19.8)

**Expansion Hub** -- a second motor and servo controller, joined to the Control
Hub by an RS485 data cable and an XT30 power cable.  (19.8)

**I2C** -- the way the hub talks to small sensors, in short digital messages on
a 4-pin cable.  (19.9)

**interference** -- electrical noise one wire puts into another.  (19.9)

**limit switch** -- a sensor at the end of a mechanism's travel that reports
when the mechanism gets there.  (19.6)

**linear slide** -- a mechanism that moves in a straight line, one stage sliding
along another.  (19.1)

**service loop** -- a deliberate loop of extra wire at a moving joint, so the
wire flexes along the loop.  (19.9)

**zeroing** -- finding a known physical position and resetting the encoder count
there.  (19.7)

### Check your understanding

1. Explain why a cascading lift moves its top stage three inches for every inch
   of cord, using the pulleys, without the table.  (19.3)
2. A 3-stage lift has to be fast, and the motor can lift the carriage's weight
   only once.  Which rigging can it raise, and why is the other one off the
   table?  (19.2)
3. Why does a lift paused at the top draw so much current, and what do two
   1.5 lb constant-force springs do about a 3 lb carriage?  (19.5)
4. What does the limit switch contribute to protecting a lift, and what does
   `DoublyLimitedMotor` contribute?  Why is either one useless alone?  (19.6)
5. A new lift's top switch was passed in as the reverse sensor.  What happens
   when the driver pushes up at the top?  (19.6)
6. Put the zeroing steps in order from memory.  What goes wrong if the reset is
   skipped?  (19.7)
7. A distance sensor reads garbage only while the drive motors are running.
   Which wiring rule does that point to?  (19.9)

## Where to go next

The motors and gear ratios behind a lift are Chapter 17, and the extrusion it
slides on is Chapter 16.  Chapter 20 shows REV's own three-stage lift kits,
cascading and continuous, built out in full.  On the software side,
`DoublyLimitedMotor` is a component (Chapter 7) with a decorated `setPower`
(Chapter 8), and the sensors' Java types are in Chapters 3 and 4.
