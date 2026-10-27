# Chapter 18 - Power Transmission and Wheels

*The motor fits over here.  The wheel has to go over there.*

---

A motor rarely gets the whole choice of where to sit.  The frame needs room
for a game piece, a wheel needs support on its shaft, and a lift needs its
spool in line with the cord.  The transmission carries power between them.

Look here when you're choosing gears, chain, or belts, laying out shaft
spacing, or checking why a drivetrain fights a sideways command.  The same
choices apply to an intake roller and an arm pivot, with different loads.

Read first:  Chapter 16, for shafts and bearings;  Chapter 17, for reduction,
torque, and speed.

| Entry | The question it answers |
|---|---|
| 18.1 | How far apart should two gears sit? |
| 18.2 | What does an idler change? |
| 18.3 | How does a chain carry power across the robot? |
| 18.4 | How tight should that chain be? |
| 18.5 | When is a belt worth using? |
| 18.6 | Which kind of wheel does the job? |
| 18.7 | Which way do mecanum rollers go? |
| 18.8 | How soft should an intake wheel be? |

---

## 18.1  Gear size and center distance

*Where it lives:*  `doc/rev-duo/power-transmission-and-motion.md`

Two gears have to meet at the right distance.  Put their shafts too close
together and the teeth bind.  Too far apart, and the teeth barely engage.
Calculate the spacing before making a plate to hold them.

The REV DUO gears in the team's reference use **0.5 Mod** teeth.  Mod is short
for **module**, the pitch diameter divided by the number of teeth.  The
**pitch diameter** is the diameter of the imaginary circle where gears roll
together.  It gives you the spacing calculation;  the outside tips of the
teeth reach farther out.

For these gears, with $N$ teeth:

$$PD = N \times 0.5\text{ mm}$$

A 20-tooth gear has a 10mm pitch diameter.  A 40-tooth gear has a 20mm pitch
diameter.  To mesh the pair, add their pitch radii, half of each diameter:

$$CD = \frac{PD_1 + PD_2}{2} = \frac{N_1 + N_2}{4}\text{ mm}$$

**Center distance**, or CD, is the distance between the shaft centers.
For 20 and 40 teeth, it is $(20 + 40)/4 = 15$mm.

| Driver | Driven | Tooth sum | Center distance |
|---|---|---|---|
| 20 teeth | 40 teeth | 60 | 15mm |
| 40 teeth | 80 teeth | 120 | 30mm |
| 20 teeth | 100 teeth | 120 | 30mm |

Tooth sums that are multiples of 60 give distances that are multiples of
15mm, the width of the extrusion in 16.2.  That makes them useful starting
points for a layout.  You still have to check the actual holes in the plates
and brackets.  Channel uses the Extended Motion Pattern from 16.3, with its
own spacings.

For practice, imagine 18-tooth and 35-tooth gears.  These are hypothetical
parts for the arithmetic.  Their center distance is $53/4 = 13.25$mm.
The calculation tells you the spacing you need.  Then you check whether
existing mounts give it to you.  Sums of 60 are the handy case, and other
tooth sums can land on other standard spacings too.

*See also:*  16.2, 16.3, 18.2

## 18.2  Driver, driven gear, and idler

The **driver** receives power from the motor.  The **driven gear** receives
power from the driver.  If a 20-tooth driver turns a 40-tooth driven gear,
the driver makes two turns while the driven gear makes one.  That is 2:1
reduction (17.3).

Each pair of touching external gears reverses direction.  Turn the first
clockwise and the next turns counter-clockwise.  That matters on a roller
that has to pull a game piece in, or an arm that has to rise when the motor
turns forward.

An **idler** is a gear between the driver and the final driven gear.  It
helps bridge the gap and adds another direction reversal.  Its tooth count
cancels out of the overall ratio.

Take a 20-tooth driver, a 40-tooth idler, and an 80-tooth final gear.  The
first pair gives $40/20 = 2$ reduction.  The second gives $80/40 = 2$.
Multiply them:

$$R = \frac{40}{20} \times \frac{80}{40} = \frac{80}{20} = 4$$

The final gear turns once for every four driver turns.  Changing the idler
changes the distances you can bridge, but its tooth count still cancels.
That result applies to one gear on each shaft.  A shaft carrying two different
gears makes a compound train, whose ratios need their own calculation.

> **Predict.**  In that three-gear train, the driver turns clockwise.
> Which way does the 80-tooth gear turn?

Clockwise.  The idler turns counter-clockwise, and the final gear reverses
that direction again.

Mounting matters as much as the ratio.  Support each shaft with bearings
(16.7), and keep the gears aligned.  The team's reference calls for a light
coat of suitable grease.  A High Strength Hex Hub protects a plastic gear's
hex bore under load (16.7);  the bearing layout must also keep the teeth
properly engaged.

> **Try it.**  With power disconnected, turn a three-gear train slowly by
> hand.  Mark the driver and final gear, then count four driver turns for one
> final turn in this 20-40-80 example.  Feel for a tight spot over the whole
> rotation.  A tight spot needs inspection before a powered test.

*See also:*  16.7, 17.3, 18.1, 18.5

## 18.3  Sprockets and chain

*Where it lives:*  `doc/rev-duo/power-transmission-and-motion.md`

A row of gears would need a shaft and bearings at every gap.  Chain lets
two shafts sit farther apart with fewer parts between them.  A drivetrain
can put its motor in a protected place and carry power along a chassis rail
to a wheel.

A **sprocket** is the toothed wheel that engages the chain.  The team's
reference uses **#25 roller chain**, with 0.25-inch, or 6.35mm, pitch.
**Pitch** here is the distance from one chain pin to the next.  It has to
match the sprockets.

The listed sprockets have 10, 15, 20, 30, or 40 teeth and a 5mm hex bore.
The bore fits the shaft from 16.6.  Choose the material for the load:
the reference uses lighter Acetal sprockets for rollers and heavier-duty
metal sprockets where shocks and drivetrain loads demand them.

The ratio calculation works like gears.  A 10-tooth sprocket driving a
30-tooth sprocket gives 3:1 reduction.  In an ordinary uncrossed chain loop,
both sprockets turn the same way.  Include that reduction when converting
motor ticks to wheel turns (17.2).

The **REV Chain Tool** presses a chain pin partly out to open the chain,
then presses that same pin back to close it.  The team's parts reference describes
this method for making a loop to length.

1. Support the link in the tool and align the tool's pressing pin with the
   chain pin.
2. Press the chain pin far enough to release the joint while keeping it
   captured in the outer plate.
3. Bring the ends together at the required length.
4. Use the tool's reassembly position to press the retained pin back flush.

> **Watch out.**  Follow the chain tool's own instructions when opening and
> closing a link.  Keep the original pin captured for this method, and check
> that the rejoined link pivots freely.  A pin pushed completely out needs
> attention before that joint can be trusted under load.

*See also:*  16.7, 17.2, 18.4, 20.6

## 18.4  Tension the chain, then check it again

*Where it lives:*  `doc/rev-duo/power-transmission-and-motion.md`

A chain loop needs room to move freely and enough tension to keep it engaged
with its sprockets.  Both extremes cause trouble.

Pull it too tight, and the motor spends effort fighting the transmission.
The chain also pulls hard across the shafts, so their bearings carry that
load.  Leave it too loose, and the chain can jump teeth or derail.

The team's reference gives **about a quarter inch, roughly 6mm, of vertical
flex under moderate finger pressure** as a starting check.  Treat it as a
workshop check for these chain runs.  The span and the specific mechanism
still matter, and the chain has to run freely around a full revolution.

Build adjustment into the mechanism while laying out its shafts.  You can
slide a bearing bracket along extrusion, adjust an idler, or choose a link
count using a half-link where suitable.  A sliding mount gives you continuous
adjustment;  changing a link gives you a fixed step.  Pick a method you can
reach after the mechanism is on the robot.

New chain lengthens slightly as its joints run in.  A tension setting made
on the first day therefore needs checking again after use.  Leave enough
adjustment for that change.

> **Try it.**  Disconnect power and press gently on the middle of a chain
> span.  Compare its flex with the reference's starting value.  Turn the
> mechanism slowly by hand and repeat the check at several positions.
> The chain should move freely throughout, with no very tight section or
> visibly loose loop.  Record where you would adjust it.

> **Watch out.**  Keep hands clear of sprockets during powered tests.  Make
> tension adjustments with power disconnected, and secure the mount again
> before running.  A sliding bracket has to hold its position under load.

The cost of chain includes this maintenance and the space for a tensioning
method.  It buys a sturdy way to carry torque across a long span.  That is
the trade-off to compare with a belt in 18.5.

*See also:*  16.4, 16.7, 18.3, 18.5

## 18.5  Timing belts and pulleys

*Where it lives:*  `doc/rev-duo/power-transmission-and-motion.md`

A flywheel has to turn smoothly at high speed.  A transmission that rattles
and shakes makes the job harder.  The team's reference uses timing belts
for fast flywheels and intakes, and for mechanisms where lost motion matters.

A **timing belt** has teeth that engage matching **pulleys**.  The teeth
carry the turning motion.  REV's system here is GT2 with **3mm pitch**;
the belt in the team's parts reference is 9mm wide.  Match the pulley profile,
pitch, and width to the belt.  [REV's timing-belt guide](https://docs.revrobotics.com/duo-build/motion/timing-belts-and-pulleys)
uses that 3mm-pitch GT2 system.

A belt costs more room for its width and needs the correct length, alignment,
and tension.  It buys smooth running, quiet operation, and lubrication-free
transmission.  Its flexibility can also soften a sudden impact on an intake.
That's a trade-off to weigh for each mechanism.  Chain still wins plenty of
jobs on the same robot.

The team's parts reference suggests belts for flywheel systems running in the
2,000 to 6,000 RPM range.  A chain at high speed brings moving links,
vibration, and lubricant to manage.  A belt is a useful starting choice for
that job;  the actual belt and pulley still need to suit the load and speed.

**Backlash** is movement at the input before a reversal reaches the output.
Clearance between chain joints and sprockets can contribute to it.  A
properly tensioned timing belt can reduce lost motion, which helps when an
encoder is upstream of the driven part.  Belt stretch, loose mounting, and
slipping connections still affect what the encoder tells you about that part.

The pulley ratio is driven teeth divided by driver teeth, just as for chain.
A 15-tooth pulley driving a 30-tooth pulley gives 2:1 reduction.  If the
mechanism already has a gearbox, multiply both reductions to get the total
(17.3).

> **Argue it.**  A proposed intake has to survive bumps and run fast, but the
> frame leaves very little room beside its roller.  Would you choose a belt,
> chain, or gears?  Draw the space each option needs, including bearings and
> tension adjustment, before defending your choice.

*See also:*  17.2, 17.3, 17.9, 18.2, 18.4

## 18.6  Traction, omni, mecanum, or compliant?

*Where it lives:*  `doc/rev-duo/power-transmission-and-motion.md`

Put a traction wheel on a desk and push it sideways.  Its tread resists the
push.  Put an omni wheel beside it, and the little rollers around its edge
let it move sideways much more freely.  Those two wheels solve different
problems on the floor.

| Wheel family | What it does well | Cost to account for | Typical job |
|---|---|---|---|
| Traction | grips the floor and pushes | resists sideways movement during turns | tank drivetrain |
| Omni | rolls sideways on its small rollers | gives little resistance to a sideways push | outer tank-drive wheels, measuring wheels |
| Mecanum | combines forces to drive sideways and diagonally | gives up some straight-line traction and needs correct layout | drivetrain that strafes |
| Compliant | compresses around an object | changes shape under load | intake or feeder |

**Scrub** is the sideways sliding a wheel does when the robot turns.
A tank drivetrain with several fixed wheels asks some of them to slide
across the floor during a turn.  Traction tread resists that motion.  Omni
wheels at suitable positions let their rollers accommodate it.

The cost is sideways grip.  The same omni rollers that help the robot turn
also make it easier for something else to push the robot sideways.  Choose
where the robot needs grip and where it needs freedom to turn.

Mecanum makes that choice differently.  Its angled rollers let four driven
wheels combine their pushes into motion across the floor (18.7).  A
**holonomic** drivetrain can translate in any direction while controlling
its heading.  That can help a robot line up with a scoring target.

Compliant wheels usually work on game pieces above the floor.  They deform
to grip an object, and their softness is a design choice of its own (18.8).

> **Try it.**  With the wheels loose on a bench, compare traction and omni
> wheels by pushing each sideways while pressing it gently down.  The
> traction tread should drag;  the omni's small rollers should turn.  Then
> point to where that sideways motion would help or hinder your drivetrain.

*See also:*  17.9, 18.7, 18.8, 20.6

## 18.7  The mecanum X

*Where it lives:*  `doc/rev-duo/power-transmission-and-motion.md`

A mecanum drivetrain needs the correct wheel at each corner.  Each side uses
one left wheel and one right wheel, with opposing roller angles.  [REV's
setup guide](https://docs.revrobotics.com/duo-build/mecanum-drivetrain-v2/mecanum-wheel-setup-and-behavior)
tells you to look down from above and follow the diagonal roller lines.
They should form an **X**.

This sketch shows those diagonal lines from above.  The word FRONT marks
the robot's front, so you can turn the page to match the chassis.

```text
                  FRONT
         front left    front right
               \       /
                 \   /
                   X
                 /   \
               /       \
          rear left    rear right
```

Each wheel's angled roller gives its push a sideways component as well as a
forward component.  A **component** here means one direction's share of a
force.  For straight travel, the sideways shares cancel and the forward
shares add.  For strafing, different wheel directions make the sideways
shares add instead.

That explains why wheel placement matters.  The drive code assumes a
particular arrangement of those angles.  A misplaced wheel changes the
forces the code gets for its commands.

> **Watch out.**  Check the roller pattern before tuning drive code.  The
> team's parts reference distinguishes carpet-contact rollers from top rollers in
> a confusing way;  use REV's top-view X and its setup pictures.  Then check
> the configured motor directions.  Both wheel placement and motor direction
> affect the result.

A wrong arrangement can make sideways travel weak or turn a requested move
into some other motion.  Its exact behavior depends on which wheels and
motor directions are wrong.  Inspect that arrangement before predicting a
particular drift or spin.

> **Try it.**  Turn robot power off and sketch the actual roller lines at
> all four corners.  Compare the sketch with REV's X.  After correcting the
> layout, use a clear test area and low power to check forward travel, a
> turn, and a sideways move separately.  Record which commands agree with
> the requested direction before increasing power.

*See also:*  18.6, Chapter 3, Chapter 6

## 18.8  Compliant wheel hardness

*Where it lives:*  `doc/rev-duo/power-transmission-and-motion.md`

A hard roller touching an irregular game piece may touch only a small patch
of it.  A **compliant wheel** changes shape around the piece, giving the
roller more contact to grip.  That makes it useful for an intake or feeder.

The wheel's **durometer** describes its hardness.  The A in 30A names the
hardness scale.  Within this family, a larger number means a firmer wheel.
The team's parts reference compares three choices.

| Durometer | Feel | Where the reference uses it |
|---|---|---|
| 30A | soft, easy to squash | hard or slick game pieces with irregular surfaces |
| 45A | medium | general-purpose intake rollers |
| 60A | firm, keeps its shape better | high-wear feeders and heavier pieces |

Softness costs shape and durability under demanding use.  It buys contact
with a piece that a rigid wheel might barely touch.  Firmness costs some
conformity and buys a roller that better keeps its round shape while loaded
and turning fast.  That's the trade-off to test with the actual game piece.

The gap between rollers matters too.  An intake needs enough compression to
grip a piece and enough room to let it pass.  A very soft wheel in a tight
gap can still jam.  The design guide's intake example lets the roller move
on a pivot with spring or rubber-band pressure, so it can lift over a piece.

> **Predict.**  A slick game piece slips past a firm intake wheel.  Would
> a softer wheel be worth trying, and what else would you check?

A softer wheel may conform better.  Also check the gap, roller direction,
and whether the roller can move enough to admit the piece.  Changing the
rubber addresses one part of the contact.

> **Try it.**  With power disconnected, press the actual game piece gently
> against two wheel hardnesses.  Compare the contact patch and the force
> needed to deform each.  Choose one for a controlled powered test, then
> record slipping, jamming, and whether the wheel recovers its shape.

*See also:*  17.1, 17.9, 18.5, 18.6, Chapter 20

---

## Chapter summary

Gears give compact transmission between nearby shafts.  Chain and belts
carry power over a span, with tension and mounting to account for.  Calculate
the full ratio, then check the physical assembly.  At the end of the
transmission, choose wheels for the movement and contact the robot needs.

| Idea | Where |
|---|---|
| 0.5 Mod gear center distance is the tooth sum divided by four, in mm | 18.1 |
| an idler's tooth count cancels from a simple gear train's ratio | 18.2 |
| #25 chain needs matching sprockets and a sound rejoined link | 18.3 |
| chain needs an accessible way to adjust tension | 18.4 |
| timing belts suit smooth, fast transmission | 18.5 |
| sideways resistance is part of choosing a drivetrain wheel | 18.6 |
| check the mecanum X from above | 18.7 |
| compliant wheel hardness trades conformity against keeping shape | 18.8 |

### Vocabulary

**backlash** -- input movement during a reversal before the output responds.
(18.5)

**belt** -- a flexible loop carrying motion between pulleys.  A timing belt
has teeth that engage matching pulleys.  (18.5)

**center distance** -- distance between two shaft centers.  (18.1)

**chain** -- a loop of connected links engaging sprockets to carry motion
between shafts.  (18.3)

**compliant wheel** -- a flexible wheel that compresses around a game piece.
(18.8)

**driver** -- the gear or sprocket receiving power from the motor.  (18.2)

**driven gear** -- the gear receiving power from the driver.  (18.2)

**durometer** -- a hardness rating;  within this wheel family, a higher A
number means a firmer wheel.  (18.8)

**holonomic** -- able to translate in any direction while controlling heading.
(18.6)

**idler** -- an intermediate gear in a simple gear train, used to bridge
distance and change the number of direction reversals.  (18.2)

**mecanum wheel** -- a wheel with angled rollers that works with three others
to combine their forces for driving and strafing.  (18.6, 18.7)

**module** -- gear pitch diameter divided by tooth count.  (18.1)

**omni wheel** -- a wheel whose small perimeter rollers allow sideways
movement.  (18.6)

**pitch** -- a repeating spacing:  between chain pins or between belt teeth.
(18.3, 18.5)

**pitch diameter** -- the diameter of the imaginary circle where gears roll
together.  (18.1)

**pulley** -- a wheel engaging a belt to carry turning motion.  (18.5)

**scrub** -- sideways sliding of a wheel as the robot turns.  (18.6)

**sprocket** -- a toothed wheel engaging a chain.  (18.3)

**traction wheel** -- a treaded wheel that grips the floor and resists
sideways sliding.  (18.6)

### Check your understanding

1. Calculate pitch diameters and center distance for 20-tooth and 100-tooth
   0.5 Mod gears.  What reduction does the 20-tooth driver give?  (18.1, 18.2)
2. Show why a 40-tooth idler between a 20-tooth driver and an 80-tooth final
   gear leaves the overall reduction at 4:1.  (18.2)
3. Which pin goes back into a link when using the chain tool's retained-pin
   method, and what should that link do after reassembly?  (18.3)
4. What goes wrong at each extreme of chain tension?  Give one way to make
   tension adjustable after the mechanism is built.  (18.4)
5. Choose a transmission for a hypothetical 4,000 RPM flywheel, a long
   drivetrain span, and a compact arm pivot.  Defend each choice with its
   space, load, and maintenance costs.  (18.2-18.5)
6. Why can an omni wheel help a tank drivetrain turn while making it easier
   to push sideways?  (18.6)
7. Draw the top-view mecanum X.  What else would you check if the wheels are
   correctly placed but the robot drives in the wrong direction?  (18.7)
8. When might a firm compliant wheel be more useful than a soft one?
   What would you test with the actual game piece?  (18.8)

## Where to go next

Chapter 19 turns rotary motion into a lift, then connects motors and sensors
to the hubs.  Chapter 20 shows complete kits built from these parts.
For the motor driving the transmission, return to Chapter 17;  for the
shafts and bearings supporting it, return to Chapter 16.
