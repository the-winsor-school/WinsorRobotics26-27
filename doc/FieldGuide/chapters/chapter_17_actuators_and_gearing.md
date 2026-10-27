# Chapter 17 - Actuators, Gearing, and Sizing

*The gearbox gets you torque.  The motor still has to do the work.*

---

A lift has to raise a carriage, and a flywheel has to spin fast enough to
launch a game piece.  Both need a motor.  They ask very different things of it.

Look here when you're choosing a motor or servo, stacking an UltraPlanetary
gearbox, or working out whether a mechanism can move its load in the time you
have.  The arithmetic belongs beside the parts.  A motor that fits the bracket
can still be too weak for the job.

Read first:  Chapter 16, for shafts, bearings, and the structure holding the
motor.  Chapter 3 explains the Java types used to control these devices.

| Entry | The question it answers |
|---|---|
| 17.1 | HD Hex or Core Hex? |
| 17.2 | How many encoder ticks mean one turn? |
| 17.3 | What does a gearbox ratio mean? |
| 17.4 | Which cartridge goes closest to the motor? |
| 17.5 | What do free speed and stall torque tell you? |
| 17.6 | When should a mechanism use a servo? |
| 17.7 | Where does servo power come from? |
| 17.8 | Can the motor lift this load in time? |
| 17.9 | How fast will the wheel or roller move? |

---

## 17.1  HD Hex or Core Hex?

*Where it lives:*  `doc/rev-duo/actuators-and-gearboxes.md`

An **actuator** turns electrical power into movement.  The two DC motors in
this team's parts reference are the HD Hex Motor and the Core Hex Motor.
Both can turn a 5mm hex shaft, the six-sided shaft from 16.6.  The gearing
between the motor and that shaft makes them useful for different jobs.

The **HD Hex Motor** takes an external gearbox.  With an UltraPlanetary, you
choose the reduction by stacking cartridges (17.3).  A flywheel can use a small
reduction for speed;  a lift can use a larger one for torque.  **Torque** is
twisting force, measured here in newton-meters, written N-m.

The **Core Hex Motor** arrives with its gearbox built in.  Its output turns
at a free speed of 125 revolutions per minute, or RPM.  A shaft can pass all
the way through its hex socket and stick out both sides, which helps when you
want to drive two parts on the same axis.

| | HD Hex Motor | Core Hex Motor |
|---|---|---|
| Part | REV-41-1291, bare motor | REV-41-1300 |
| Gearing used here | interchangeable UltraPlanetary cartridges | fixed internal gearbox |
| Output speed | depends on the cartridges | 125 RPM with no load |
| Typical jobs | drivetrain, lift, arm, flywheel | compact intake, feeder, light wrist |

Start with the motion the mechanism needs.  A light claw that opens to an
angle points toward a servo (17.6).  A roller that turns continuously points
toward a motor.  Then check the load and speed.  The Core Hex's compact shape
costs you the freedom to change its internal reduction.  It buys a complete
gearmotor in one small package.  The HD Hex with an UltraPlanetary costs more
space and assembly work, and buys that choice of ratio.  That's the trade-off.

Use the current [REV motor specifications](https://docs.revrobotics.com/duo-build/motion/motors/core-hex-motor)
when a calculation needs internal details.  The Core Hex's internal reduction
is 72:1.  The team's parts reference calls it 20:1, which is wrong;  use 72:1.

*See also:*  17.3, 17.6, 17.8, Chapter 3

## 17.2  Encoder ticks and output turns

An encoder counts shaft movement in small steps called ticks (Chapter 3).
To turn a count into a distance, first ask which shaft a revolution refers to.

An HD Hex Motor produces **28 counts per motor revolution**.  Its motor turns
many times while the gearbox's output turns once.  If the reduction were
exactly 20:1, one output turn would take 20 motor turns, so the count would be:

$$28 \times 20 = 560\text{ ticks per output revolution}$$

That is a made-up exact 20:1 gearbox for the arithmetic.  The nominal 20:1
UltraPlanetary stack actually has about 18.93:1 reduction (17.3), so its count
is about $28 \times 18.93 = 530$ ticks per output revolution.

The Core Hex's published **288 counts per output revolution** already includes
its internal gearbox.  Divide its measured ticks by 288 to get output turns.
Multiplying those 288 counts by the internal reduction would count the same
gearbox twice.

| Motor and gearbox | Calculation for output turns |
|---|---|
| HD Hex with reduction $R$ | measured ticks divided by $(28 \times R)$ |
| Core Hex, at its own output socket | measured ticks divided by 288 |

What matters is which shaft a published count describes.  REV gives the
Core Hex's 288 counts per turn of the output, so you use the number on the
output shaft, wherever the sensor happens to sit inside the housing.

Now follow the whole mechanism.  A wheel attached directly to the gearbox
turns once per output turn.  Add another gear or chain reduction between them
(Chapter 18), and that reduction belongs in the calculation too.  A change of
cartridges changes the distance each tick represents even though the encoder
cable is still in the same port.

> **Try it.**  With power disconnected, write down the cartridges in a
> drivetrain gearbox, in order from the motor.  Calculate ticks per output
> revolution using 17.3's fractions.  Compare that with the ticks-per-turn
> value used by its autonomous code.  They should describe the same complete
> transmission.  Record a mismatch before changing either the gearbox or code.

*See also:*  17.3, 17.9, Chapter 3, Chapter 18

## 17.3  Nominal and actual ratios

*Where it lives:*  `doc/rev-duo/actuators-and-gearboxes.md`

A **gear reduction** makes the output turn more slowly than the motor.  A
5:1 reduction means five input turns for one output turn.  Ideally it also
multiplies torque by five.  Some power goes into friction in a real gearbox.

The **UltraPlanetary** is a gearbox built from cartridges.  The labels are
3:1, 4:1, and 5:1.  Those are **nominal ratios**:  convenient names for
choosing parts.  The actual ratios come from the gears inside.

| Cartridge | Nominal ratio | Actual ratio as a fraction | Approximate decimal |
|---|---|---|---|
| REV-41-1601 | 3:1 | $84/29$ | 2.89655:1 |
| REV-41-1602 | 4:1 | $76/21$ | 3.61905:1 |
| REV-41-1603 | 5:1 | $68/13$ | 5.23077:1 |

These fractions are from [REV's cartridge details](https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/cartridge-details).
Use them for calculations, then round the final answer to the precision you
need.  A decimal printed to three places is an approximation too.

Stack two cartridges and multiply their ratios.  A motor driving the 5:1
cartridge, then the 4:1 cartridge, gives:

$$R = \frac{68}{13} \times \frac{76}{21} \approx 18.93$$

So a nominal 20:1 stack takes about 18.93 motor turns for one output turn.
Adding the 3:1 cartridge gives about 54.83:1.  Multiplying the labels alone
would give 60:1, a useful name and a noticeably different calculation.

The team's parts reference lists 18.88:1 and 54.68:1 in its stack table.  Those
numbers came from rounding too early.  Use the fractions above when converting
encoder ticks or calculating speed.

The ratio's size describes a choice you can feel.  More reduction costs output
speed and buys output torque.  Less reduction costs torque and buys speed.
The motor supplies the power either way.  A gearbox lets you choose how to
use that power;  17.8 checks whether there is enough of it.

*See also:*  17.2, 17.4, 17.5, 17.8

## 17.4  Highest reduction closest to the motor

*Where it lives:*  `doc/rev-duo/actuators-and-gearboxes.md`

The same cartridges give the same multiplied ratio in either order.  Their
order still matters to the life of the gearbox.

> **Watch out.**  Put the cartridge with the highest reduction closest to the
> motor.  For a 5:1 and a 4:1 stack, the order is motor, 5:1, 4:1, output.
> For all three sizes, it is motor, 5:1, 4:1, 3:1, output.

Torque increases through the stack.  A cartridge near the motor works with
less torque than one near the output.  The team's reference explains that the
5:1 cartridge has smaller sun and planet teeth.  Putting it at the motor end
keeps those teeth out of the position carrying the greatest torque.

Picture an arm that catches on the frame.  The output stops while the motor
keeps trying to turn.  That is a **stall**.  With the cartridges in the wrong
order, the high torque from an earlier stage reaches the small teeth at the
output.  Damaged teeth mean taking the gearbox apart, even though the ratio
you calculated was right.

This is the fifth Golden Rule in the team's hardware reference (16.1).
[REV's cartridge guide](https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/cartridge-details)
gives the same order.  Check it while the cartridges are visible, before the
gearbox goes on the robot.

> **Predict.**  An arm needs roughly 15:1 reduction.  You have a 5:1 cartridge
> and a 3:1 cartridge.  Which order works?

Motor, 5:1, 3:1, output.  The actual reduction is about 15.15:1 either way,
but this order puts the greatest reduction at the motor.

> **Try it.**  Lay out a stack beside its motor, in assembly order, with power
> disconnected.  Have another builder name each cartridge starting at the
> motor.  Check the order against the rule before fastening it together.

*See also:*  16.1, 17.3, 17.5

## 17.5  Free speed, stall torque, and working load

Two headline numbers describe opposite ends of a motor's operation.
**Free speed** is how fast it turns with essentially no useful load.
**Stall torque** is how hard it twists when the output has been stopped.

A moving mechanism works between those ends.  As load rises, speed falls.
At stall, output speed is zero.  A table's free speed and stall torque are
therefore two different conditions to compare your design against.

Here are approximate unloaded speeds calculated from a 6,000 RPM bare HD Hex
and the fractions in 17.3.

| Cartridge stack, motor first | Actual reduction, rounded | Calculated free output speed |
|---|---|---|
| 3:1 | 2.897:1 | 2,071 RPM |
| 5:1 then 4:1 | 18.93:1 | 317 RPM |
| 5:1 then 4:1 then 3:1 | 54.83:1 | 109 RPM |

A loaded wheel or lift runs slower.  Use the table for a first estimate,
then measure the built mechanism with its real load.  A test with an empty
carriage tells you little about a carriage carrying game pieces.

> **Watch out.**  [REV's load-rating page](https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/load-ratings)
> describes cartridge failure at 40 N-m.  Treat that as a failure threshold.
> Shock loads and a shaft supported at only one end can cause damage at lower
> torque.  Give a shaft carrying chain tension or a wheel its own external
> bearing, and check REV's rating chart for the actual stack.

The team's reference usually stops at three cartridges.  More cartridges
cost length and add parts carrying multiplied torque.  Before adding a fourth,
check the load chart and the whole mounting arrangement with a mentor.

An arm that struggles may need more reduction.  It may also have a binding
bearing, a cord rubbing a bracket, or more weight than its design assumed.
Turn the unpowered mechanism by hand where it is safe to do so, and inspect
the load path before using gearing to compensate for friction.

*See also:*  16.7, 17.3, 17.8, 18.3, Chapter 19

## 17.6  One servo, two modes

*Where it lives:*  `doc/rev-duo/actuators-and-gearboxes.md`

A claw often needs to open, close, and keep its angle.  A roller needs to keep
turning.  The **Smart Robot Servo**, or SRS, can do either job, depending on
the mode set with its programmer.

In **angular mode**, the servo moves to a requested position and works to keep
it there.  In **continuous rotation mode**, it turns in either direction at a
commanded power.  The hardware mode decides which kind of command makes sense.

| Mode | Request | Java type from Chapter 3 | Example job |
|---|---|---|---|
| Angular | a position in the configured travel range | `Servo` | claw, gate, latch |
| Continuous rotation | turning power and direction | `CRServo` | turret, roller |

The SRS in the team's parts reference has a 25-tooth spline, the toothed shaft
where its horn attaches.  The horn is the small arm or disk that joins the
servo to the mechanism.  Its angular travel can be up to 270 degrees.  Check
the actual servo model and programmed limits before designing around that range.

The **SRS Programmer** changes the physical servo's mode.  It can also set
angular limits, such as restricting movement to the useful travel of a claw.
Chapter 3 explains the SDK commands.  A change of Java type by itself leaves
the servo's programmed mode exactly as it was.

Choose with the load in mind.  An angular servo suits a light claw because
it already controls position.  A heavy shoulder joint can require a geared
motor, limit switches, and a component that manages them (19.6).  The servo's
convenient position control still has to move the real load.

> **Try it.**  With robot power off, identify the servo on a claw or gate.
> Record its model, intended mode, and the mechanism's useful travel.  Find
> the corresponding `Servo` or `CRServo` in its component.  The hardware mode
> and the kind of command should agree before a powered test.

*See also:*  17.7, 19.6, Chapter 3, Chapter 7

## 17.7  Servo load and the power supply

*Where it lives:*  `doc/rev-duo/actuators-and-gearboxes.md`

A servo pushing against a hard stop keeps trying to reach its requested
position.  It draws current while it does that.  Several loaded servos can
ask more of their supply than the supply can deliver.

The Control Hub supplies 5V to its servo ports.  Its limits apply to groups
of ports as well as to the total.  According to [REV's Control Hub specifications](https://docs.revrobotics.com/duo-control/control-system-overview/control-hub-basics),
each pair, 0-1, 2-3, and 4-5, shares a 2A limit.  All servo ports and the
auxiliary 5V ports together share a 5A total limit.  The team's parts reference's
"2A across all six ports" combines two different limits.

A **brownout** is a drop in supply voltage far enough to disrupt operation.
Overloading a supply can cause voltage problems or protective shutdown.
Investigate the power and the load when servos falter under resistance.

> **Watch out.**  Set a servo's useful travel inside the mechanism's hard
> stops.  Start with the reference's suggested 5 to 10 percent margin, then
> check the linkage through its full motion.  A linkage can bind partway
> through a move even when both endpoints look clear.

For greater servo load, the **Servo Power Module** supplies servo power from
the battery bus while the hub supplies control signals.  It adds wiring and
another device to mount.  It buys a separate supply for the servos, which is
the trade-off to consider when the hub's supply is insufficient.

The module takes nominal 12V input and supplies 6V.  [REV's module guide](https://docs.revrobotics.com/rev-crossover-products/servo/spm)
specifies a 15A total maximum and 3A per output channel.  Check both limits,
the servo's allowed voltage, and the connections.  Follow that guide's input
wiring diagram;  this module uses screw terminals for power.

Extra supply capacity leaves the mechanical problem to solve.  If a claw is
forcing its horn into aluminum, correct the travel or linkage before running
it again.  A stronger supply lets the servo push harder on the obstruction.

*See also:*  17.6, 19.8, 19.9

## 17.8  Work, power, and a lift that has to be fast

*Where it lives:*  `doc/rev-duo/design-decision-guide.md`

Start with the lift in the team's design guide:  a 2.0 kg carriage and game
piece, rising 1.2 meters in 1.5 seconds.  Before choosing cartridges, work out
how much energy it takes to raise the load.

**Work** is energy transferred by a force moving something through a distance.
For this vertical lift, gravity supplies the resisting force.  Use:

$$W = m \times g \times d$$

Here $m$ is mass in kilograms, $g$ is gravitational acceleration, about
9.81 meters per second squared, and $d$ is the rise in meters.  The result
is in joules, written J.

$$W = 2.0 \times 9.81 \times 1.2 = 23.544\text{ J}$$

**Power** is work done per second.  Its unit is the watt, written W.  The
letter W also names work in the formula above;  the unit following a number
tells you when it means watts.

$$P = W/t = 23.544/1.5 = 15.696\text{ W}$$

That calculation accounts for raising the specified mass.  The built lift
also has friction, moving slide parts, cable drag, and a battery whose voltage
drops under load.  A **safety factor** gives the design room for loads and
losses beyond the simple calculation.  The team's guide uses 2.0 to 2.5.
Using 2.0 gives:

$$P_{\text{design}} = 15.696 \times 2.0 \approx 31.4\text{ W}$$

Now compare that with the motor.  [REV specifies 15 W maximum output for the
bare HD Hex Motor](https://docs.revrobotics.com/duo-build/motion/motors/hd-hex-motor).
One motor can't meet this 31.4 W design requirement.  The team's design guide
says the HD Hex makes about 40 W and passes this lift.  REV's own number is
15 W, so the guide's conclusion is wrong, and the lift needs a second look:
more time, a lighter carriage, or more motors.

> **Predict.**  Keep the same carriage and height, but give the lift twice
> as long.  What happens to the required power?

It halves.  The work is the same, spread across twice the time.  Reducing the
load or the height also reduces the work.  Adding suitable motors is another
design to calculate, including its losses and load sharing.

More reduction alone trades speed for torque.  To meet a lift requirement,
check power first, then torque at the spool and the speed the spool needs.
Test the finished lift with its load and the battery conditions it will face.
A peak motor rating gives you a ceiling for the calculation;  it still needs
room below it for a mechanism that has to run repeatedly.

*See also:*  17.3, 17.5, 17.9, 19.2, 19.5

## 17.9  From RPM to surface speed

*Where it lives:*  `doc/rev-duo/design-decision-guide.md`

A wheel turning once moves its rim through one circumference.  For a diameter
$D$, that distance is $\pi D$, where $\pi$ is about 3.1416.  Multiply by turns
per second to get **surface speed**, the speed of the rim.

$$V = \pi \times D \times \frac{\text{RPM}}{60}$$

RPM counts turns per minute, so dividing by 60 changes it to turns per second.
Use meters for $D$ and the answer is meters per second.  Use inches and it is
inches per second.  Keep the units together from the start.

The team's drivetrain example uses an HD Hex, a 5:1 then 4:1 UltraPlanetary
stack, and a 90mm wheel.  Begin with 17.3's actual reduction, about 18.93:1.

$$\text{Output RPM} = 6000/18.93 \approx 317$$

Convert the wheel diameter before putting it into the next formula:
90mm is 0.090m.

$$V = \pi \times 0.090 \times \frac{317}{60} \approx 1.49\text{ m/s}$$

This is an unloaded estimate at the wheel's rim.  The actual robot has weight,
friction, and wheel slip.  Mecanum rollers also make direction matter
(18.7).  Use a measured driving test to learn the robot's field speed.

The team's design guide suggests 1.4 to 1.8m/s as a starting target for a
drivetrain.  Treat that as the guide's design choice.  A faster robot asks
more precision of the driver near a scoring target;  a slower one spends
longer crossing the field.  The right choice depends on the jobs it must do.

The same formula applies to a lift spool or an intake roller.  A larger spool
reels in more cord each turn and asks for more torque to pull the same load.
For a cascading lift, the carriage travels farther than the spool's cord
(19.3), so carry the rigging into the calculation too.

> **Try it.**  Calculate the unloaded rim speed of your chosen wheel and
> gearbox.  Write the units on every line.  Compare the estimate with a
> measured run over a known distance, and record the load and battery used.
> The measured speed will usually be lower.

*See also:*  17.2, 17.3, 18.6, 18.7, 19.3

---

## Chapter summary

Choose the actuator from its job, then calculate the transmission and load.
Use actual cartridge ratios for distance and speed, put the highest reduction
at the motor, and compare working requirements with the motor's power.
Servo travel and the supply feeding it are part of the same design.

| Idea | Where |
|---|---|
| choose motion, load, and speed before choosing a motor | 17.1 |
| counts per motor turn and counts per output turn are different quantities | 17.2 |
| multiply actual ratios;  round at the end | 17.3 |
| highest reduction closest to the motor | 17.4 |
| free speed and stall torque describe opposite operating conditions | 17.5 |
| a servo's programmed mode determines its kind of motion | 17.6 |
| check both port-pair and total servo current | 17.7 |
| calculate work, required power, and a safety factor | 17.8 |
| circumference times turns per second gives rim speed | 17.9 |

### Vocabulary

**actuator** -- a device that turns electrical power into movement.  (17.1)

**brownout** -- a voltage drop far enough to disrupt operation.  (17.7)

**Core Hex Motor** -- a compact motor with a fixed gearbox and a through hex
socket, rated at 125 RPM free output speed.  (17.1)

**free speed** -- rotational speed with essentially no useful load.  (17.5)

**gear reduction** -- a ratio that trades output speed for output torque.
(17.3)

**HD Hex Motor** -- the motor used here with an external UltraPlanetary
gearbox whose ratio the builder chooses.  (17.1)

**nominal ratio** -- the convenient ratio used to name a cartridge.  The
actual ratio comes from its internal gears.  (17.3)

**power** -- work done per second, measured in watts.  (17.8)

**safety factor** -- a multiplier allowing for loads and losses beyond a
simple calculation.  (17.8)

**Servo Power Module** -- a device supplying servo power from the battery bus,
with control signals from the hub.  (17.7)

**Smart Robot Servo** -- a programmable servo with angular and continuous
rotation modes.  (17.6)

**stall torque** -- the torque a motor produces with its output stopped.
(17.5)

**surface speed** -- the speed of a wheel or roller's rim.  (17.9)

**torque** -- twisting force, measured here in N-m.  (17.1)

**UltraPlanetary** -- a modular gearbox with interchangeable reduction
cartridges.  (17.3)

**work** -- energy transferred by a force moving something through a distance,
measured in joules.  (17.8)

### Check your understanding

1. What does choosing an HD Hex with an UltraPlanetary let you change that
   a Core Hex's internal gearbox fixes?  (17.1)
2. Why do you multiply an HD Hex's 28 counts by its gearbox ratio, while the
   Core Hex's 288 counts already describe an output turn?  (17.2)
3. Calculate the actual reduction of a 5:1 and 3:1 stack.  Which cartridge
   touches the motor, and why?  (17.3, 17.4)
4. Why would using free speed and stall torque together overestimate what a
   moving mechanism can do?  (17.5)
5. A claw's Java code expects an angular servo, but the servo is programmed
   for continuous rotation.  What needs checking before it runs?  (17.6)
6. Two servos share ports 0 and 1.  Which current limit applies to the pair,
   and what other total also needs checking?  (17.7)
7. Use the lesson's practice load:  1.5 kg, 0.9m rise, 1 second, safety factor
   2.0.  Calculate work and design power.  Can one 15 W motor meet it?  (17.8)
8. Calculate the unloaded rim speed of a 90mm wheel at 317 RPM.  What would
   you measure on the robot before relying on the estimate?  (17.9)

## Where to go next

Chapter 18 carries the motor's output through gears, chain, and belts, then
down to the wheels.  Chapter 19 applies these calculations to a lift and
protects its travel with switches.  Chapter 3 covers the SDK devices, and
Chapter 7 puts the chosen hardware inside a component.
