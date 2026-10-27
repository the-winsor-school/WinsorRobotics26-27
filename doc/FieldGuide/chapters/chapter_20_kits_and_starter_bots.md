# Chapter 20 - Learning From Kits and Starter Bots

*Read someone else's robot for the pattern, then build your own.*

---

The last four chapters were about parts.  This one is about a skill:  reading
someone else's finished design well enough to learn from it.

REV publishes two kinds of finished design.  Kit guides walk you through
building one mechanism, step by step.  Kickoff Starter Bots are whole robots,
built fresh each season for that season's game.  Both are worth reading
closely, and this chapter says what to read them for.

Look here at the start of a season, when the new starter bot comes out.  Look
here, too, when you're about to build a mechanism you've only ever seen on
someone else's robot.

Read first:  Chapters 16 to 19.  Every kit and starter bot here is built from
the parts they cover.

| Entry | The question it answers |
|---|---|
| 20.1 | What's the difference between a kit guide and a starter bot? |
| 20.2 | What's worth studying in last season's starter bot? |
| 20.3 | Can we just build the starter bot? |
| 20.4 | How do you take a design apart? |
| 20.5 | How does a borrowed mechanism get into the code? |
| 20.6 | What do the drivetrain and lift kits teach? |
| 20.7 | Why build a robot that's only for practice? |
| 20.8 | What do we do when the new starter bot comes out? |

---

## 20.1  Kit guide or Kickoff Starter Bot?

*Where it lives:*  `doc/rev-duo/kits-and-starter-bots.md`

> **Predict.**  REV publishes kit build guides, and it publishes Kickoff
> Starter Bots.  What's different about them?

A **kit guide** is REV's step-by-step assembly guide for one of its kits:  a
drivetrain, a lift, or a Class Bot.  What it builds is a general-purpose
mechanism.  A drivetrain kit drives the same way in any game, so one build can
serve robot after robot.

A **Kickoff Starter Bot** is a complete, competition-legal robot that REV
engineers release at the FTC Season Kickoff every year.  It's built entirely
from REV DUO parts (Chapter 16), and it's built to do that season's game tasks.

So the difference is what each one is for.

| | Kit guide | Kickoff Starter Bot |
|---|---|---|
| What it builds | one general-purpose mechanism | a whole robot for one game |
| How often | whenever REV makes a kit | every season, at Kickoff |
| What it is to you | a mechanism to build and reuse | a design case study |
| How much you keep as it comes | most of it | the ideas that fit |

A kit guide is meant to be built.  Follow it and you get a mechanism that works,
and you can put it on this year's robot and next year's.  A starter bot is a
worked answer to one season's game.  You read it to learn how REV's engineers
solved each problem, and then you solve yours.

The team's reference says these resources "provide proven mechanical patterns
that students can analyze, build, and adapt."  The kit guides lean toward
*build*.  The starter bots lean toward *analyze* and *adapt*.

*See also:*  20.2, 20.6, 20.7

## 20.2  What's worth studying in last season's starter bot

*Where it lives:*  `doc/rev-duo/kits-and-starter-bots.md`

The team's reference tracks five seasons of starter bots, plus a preview bot
for 2026-27.

| Season | The starter bot's mechanisms |
|---|---|
| 2025-26 DECODE | a compact channel drivetrain;  a high-velocity flywheel launcher with UltraPlanetary gearing;  a compliant intake roller feeder |
| 2024-25 INTO THE DEEP | a multi-stage linear slide for high-basket scoring;  an articulated shoulder arm and wrist;  a compliant wheel intake and a servo claw |
| 2023-24 CENTERSTAGE | a low-profile intake and ramp transfer;  a tilting bucket for placing pixels;  rigging for a drone launcher and a hanging hook |
| 2022-23 POWERPLAY | a compact vertical elevator for stacking cones on tall poles;  a passive-assist cone gripper |
| 2020-21 Ultimate Goal | a ring-indexing conveyor with polyurethane belts;  a dual-wheel flywheel launcher;  an active roller intake |

Every one of these was built for a game that's over.  A cone gripper from
POWERPLAY has no cones to grip this year.

So the mechanism itself is the wrong thing to study.  Study the **pattern**:
the way the engineers solved a problem that comes back every season.

- How did they mount a motor so it survives shock loads, the sudden hits a
  robot takes in a match?
- How did they tension a belt?
- How did they support a cantilevered shaft against side-load?  A
  **cantilevered shaft** is held at one end only, like a diving board, and
  **side-load** is a push across the shaft that tries to bend it.

Those questions outlive any single game.  Look at the table again:  a flywheel
launcher shows up in 2020-21 and again in 2025-26, five seasons apart.  The
game pieces changed completely.  The problem of flinging one with a spinning
wheel came back.

*See also:*  20.1, 20.4, Chapter 16, Chapter 18

## 20.3  Rule 1:  a starter bot is a baseline

*Where it lives:*  `doc/rev-duo/kits-and-starter-bots.md`

The team's reference gives three rules for using a reference design.  The
first, in its own words, is "Never copy blindly."

Start with what's good about a starter bot, because it's a lot.  It works.
REV's engineers built it, it plays the real game, and it's legal.  Building it
would put a robot on the field fast.

Here's the other half, also in the reference's words:  "A starter bot is built
to be simple and accessible, not optimized for world-championship
performance."  It's designed so that a team can build it from a guide, and so
that it looks clear in a demonstration video.  Those are real goals, and
they're REV's goals.  Yours are winning matches in this season's game, with
this team's parts and skills.

So treat a starter bot as a **baseline**:  the version you measure your own
ideas against, and the floor you're trying to build above.

Copying blindly means building what's in the guide without knowing why each
part is there.  It feels fast.  Then something breaks in a match, or the game
asks for more reach, and you're fixing a design you only know how to assemble.

Rules 2 and 3 are how you avoid that.  Rule 2 takes the design apart (20.4).
Rule 3 brings what you keep into this team's code (20.5).

*See also:*  20.4, 20.5, 20.8

## 20.4  Rule 2:  decompose into mechanisms

The second rule is "Decompose into mechanisms."

**Decomposing a design** means splitting a finished robot into its mechanisms,
and asking of each one how it solved its one problem.  The question to ask is
"How did they solve *this* problem?" for the drivetrain, the intake, and the
scoring mechanism, one at a time.  "Should we build this whole robot?" is the
question to skip.

The reason is what a real robot looks like at the end of a season.  A team's
robot almost always mixes and matches:  a drivetrain from one idea, an intake
from another, a lift of its own.  Decomposing gives you pieces you can keep or
drop one at a time.

Take the INTO THE DEEP starter bot from 20.2.  It splits into three questions.

1. How did they rig the multi-stage slide, and why that way (19.2)?
2. How did they drive the shoulder and wrist, and with what gearing
   (Chapter 17)?
3. How does the compliant wheel intake grab a piece, and what turns it
   (Chapter 18)?

Each answer stands alone.  You can love their slide and drop their intake.

For each mechanism, ask the same three things:  what structure it depends on
(Chapter 16), what transmission does the work (Chapters 17 and 18), and what
would have to change to make it this team's version.

> **Try it.**  Here's a made-up mechanism from a made-up starter bot:  "a
> spring-loaded ramp that flips a game piece into a collection bin using a
> single Core Hex Motor and a compliant roller."  Decompose it.  Write down the
> structure it depends on, the transmission doing the work, and what would
> change to make it this team's version.  Then check yours against the one
> below.

Here's one decomposition.  The ramp pivots, so it needs a shaft and bearings to
pivot on (Chapter 16).  The Core Hex Motor turns the compliant roller, on its
own shaft or through a gear or a belt (Chapter 18).  The spring does the
flipping.  Then the description runs out:  it never says what holds the spring
back and what lets it go.  Finding a gap like that is part of the job, and it's
the first question to answer before you build.  This team's version would be
sized to this season's game piece, with the motor checked against the load
(Chapter 17), and it would arrive in the code as a component (20.5).

*See also:*  20.2, 20.5, 20.8

## 20.5  Rule 3:  map what you keep onto the team's software

*Where it lives:*  `doc/NewRobotDesignWorkflow.md`

The third rule, in the reference's words:  "When you decide on a mechanism from
a starter bot, write its dedicated `MechComponent` class following our
NewRobotDesignWorkflow.md!"

A **`MechComponent`** is the class that owns one mechanism on the robot
(Chapter 7).  The design workflow gives every component four jobs:  own the
raw hardware, define a TeleOp control strategy, expose a small set of
autonomous verbs, and report telemetry.  Components that have to work together
go into a **`MechAssembly`**, which coordinates them (Chapter 9).

So a borrowed mechanism enters this codebase as a component, or a few
components in an assembly.  It never arrives as "the starter bot's code."

That holds even for a mechanism REV built and proved.  The team's software
model is a fixed contract every mechanism honors, whichever season's starter
bot first inspired it:  the control strategies (Chapter 8), the autonomous
verbs (Chapter 11), and the telemetry rules (Chapter 7).  Several starter bots
come with REV's own programming guides, like DECODE's flywheel tuning and INTO
THE DEEP's arm and wrist control.  Read them for how the mechanism is
controlled.  Then write the code this team's way.

One thing to check before you write a class.  The reference says to write a
*dedicated* component, and the code's own rule comes first:  reuse a component
that already owns the same hardware shape (Chapter 7).  A starter bot's slide
with a limit switch at each end is a motor plus two switches.  That's
`DoublyLimitedMotor`, already written, and Wildbots2025 builds its lift from
one (19.6).

The javadoc at the top of `RobotModel/Mechs/Components/MechComponent.java` says
what a needless copy costs:

```java
 * <p>A class that is neither - a second single-device wrapper named after one
 * robot's job for it - is duplication, and every fix to one is a fix owed to
 * the other.
```

"Neither" means a class outside the two kinds of component that Chapter 7
describes.  It's the same mistake as copying a robot blindly, made in code.

This rule comes last on purpose.  Study the mechanism, then decompose it, then
map what you kept.  Chapter 21 shows the whole design workflow, from the
robot's jobs down to the first file.

*See also:*  19.6, 20.8, Chapter 7, Chapter 9, Chapter 21

## 20.6  Reading a kit guide:  drivetrains and lifts

*Where it lives:*  `doc/rev-duo/kits-and-starter-bots.md`

A kit guide teaches more than its parts list.  Each drivetrain kit shows a
different way to combine structure (Chapter 16) with transmission
(Chapter 18).

| Kit | Built from | What it teaches |
|---|---|---|
| Mecanum Drivetrain V2 | C-Channel, 90mm mecanum wheels | direct-drive UltraPlanetary motors, bearing seats inside the channel, a rigid box frame |
| Channel Drivetrain (Mecanum Upgrade) | 45mm U-Channel, sprockets and chain | chained 4-wheel drive, dual bearing supports, counting chain links |
| Extrusion Chain Drivetrain | 15mm extrusion, #25 chain | tensioning chain with a sliding bearing plate, drop-center traction and omni wheels |
| Extrusion Gear Drivetrain | 15mm extrusion, 0.5 Mod gears | meshing gears on the 15mm grid, idler gears |
| Basic Channel Drivetrain | 45mm U-Channel, traction wheels | heavy-duty steel sprockets, single and double sprocket shafts |

Two facts from Chapter 16 explain most of that table.  Channel has a 9mm
bearing seat every 16mm, punched right into it, and it's extremely stiff
against twisting.  Extrusion needs separate bearing plates or pillow blocks,
and it's only moderately stiff.  Chapter 18 adds a third:  chain on extrusion
needs a way to tension it, so the bearing plate slides along the slot.

> **Predict.**  Which kind of drivetrain kit is easier to keep square over a
> whole season, channel or extrusion?

Channel.  Its bearing seats are punched into the metal, so every shaft sits
exactly where the pattern put it, and the channel resists twisting.  On
extrusion, the bearings sit in plates that slide in a slot.  Anything that
slides to be adjusted can also get knocked out of place.

That's a trade-off, and extrusion's cost comes first:  more parts, more
adjusting, and a frame that can drift out of square.  What it buys is freedom.
A bracket can go anywhere along the slot, which is exactly how the Extrusion
Chain Drivetrain tensions its chain.

The lift kits are 19.2's decision, built out in full.

| Kit guide | What it covers |
|---|---|
| Three Stage Cascading Lift | installing the slider blocks, rigging the string stage by stage, the dual spool, tensioning |
| Three Stage Continuous Lift | routing the single cable, gravity and spring return, intermediate pulley blocks |
| Linear Motion Kit Overview | the bill of materials, slider maintenance, pulley alignment |

If you understand cascading and continuous rigging on paper but have never seen
the string routed, these guides are where to look.

> **Try it.**  Open the Extrusion Chain Drivetrain guide.  The link is in
> `doc/rev-duo/kits-and-starter-bots.md`.  Find the step where the bearing
> plate slides to tension the chain, and compare it with the Mecanum
> Drivetrain V2, where the motors drive the wheels directly.

*See also:*  19.2, 19.3, 19.4, Chapter 16, Chapter 18

## 20.7  Why build a Class Bot?

*Where it lives:*  `doc/rev-duo/kits-and-starter-bots.md`

A **Class Bot** is a REV kit robot built to teach the basics of robot
mechanics.  The team's reference lists two.

- **Class Bot V2** teaches squaring a rectangular frame, inside and out;
  assembling wheel hubs;  a two-motor drive;  and a geared arm that rotates,
  with counterbalancing.
- **Class Bot (EDU Kit)** is a compact two-motor classroom robot.  It shows how
  to pre-load brackets and how a claw linkage works.

Be honest about the cost first.  A Class Bot takes parts and build hours, and
REV lists it with the educational kits, apart from every season's starter bot.
It's built to teach, and building it gets you no closer to this season's game.

Here's the case for it anyway.  **Squaring a frame** means making its corners
true right angles, so the rectangle really is a rectangle.  **Counterbalancing**
an arm means offsetting its weight with a spring or a weight, the way 19.5
offsets a lift's carriage.  Every competition robot needs both.  The Class Bot
is a cheap place to learn them.

Finding a squaring mistake on a practice robot costs an afternoon.  Finding it
on the competition drivetrain two weeks before a tournament costs a great deal
more.  A from-scratch competition robot gets built fast, under pressure, and
skills like squaring are the first ones that pressure skips.

> **Argue it.**  A new team member has a month before the first event.  Should
> they build a Class Bot first, or start on the competition drivetrain on day
> one?  What does each choice cost the team, and what does it buy?

*See also:*  19.5, 20.6, Chapter 16

## 20.8  The season-start habit

This chapter is really about a habit, for this season and every season after
it, including the ones nobody on the team has seen yet.  When a new game comes
out, the habit runs in four steps.

1. Find this season's Kickoff Starter Bot.  For 2026-27, the team's reference
   already lists REV's preview bot.
2. Decompose it, mechanism by mechanism (20.4).
3. Decide which patterns are worth keeping.  Keep ideas, and let the parts go
   (20.2, 20.3).
4. Build whatever survives as this team's own component or assembly, following
   the design workflow (20.5, Chapter 21).

The hard case is a starter bot that solves the main scoring task with a
mechanism this team has never built.  Here's what happens between seeing it
for the first time and writing the first line of `MechComponent` code.

- **Study it.**  Watch it run.  Read its build guide and its bill of materials,
  and its programming guide if it has one.  Study comes before adapting.
- **Decompose it.**  What structure does it depend on, what transmission does
  the work, and what sensors does it need?  Decompose before you decide what to
  keep.
- **Decide what to keep,** and check each part you'll use against Chapters 16
  to 19.
- **Map it last.**  What hardware shape is it?  Does a component already own
  that shape (Chapter 7)?  Which assembly will coordinate it (Chapter 9)?  What
  verbs should autonomous call (Chapter 11)?  Then write the code.

The order matters as much as the steps.  A team that starts with the class
names has decided what the mechanism is before it knows.

> **Argue it.**  Should the team build the full Kickoff Starter Bot as its first
> robot of every season, before designing its own?  What would that teach, what
> would it cost, and when in the season would you stop?

*See also:*  20.4, 20.5, Chapter 21

---

## Chapter summary

A kit guide builds one general-purpose mechanism, and you can keep most of it
as it comes.  A Kickoff Starter Bot is one season's worked answer, and you keep
its ideas.  The team's three rules for a reference design run in order:  treat
it as a baseline, decompose it into mechanisms, and map whatever you keep onto
this team's components last.

| Idea | Where |
|---|---|
| a kit guide is built;  a starter bot is studied | 20.1 |
| study the pattern, which outlives the game | 20.2 |
| rule 1:  a starter bot is a baseline | 20.3 |
| rule 2:  decompose into mechanisms | 20.4 |
| rule 3:  map onto the team's software, reusing first | 20.5 |
| channel against extrusion, in the drivetrain kits | 20.6 |
| a Class Bot is a cheap place to learn squaring and counterbalancing | 20.7 |
| study, decompose, keep, map, every season | 20.8 |

### Vocabulary

**baseline** -- the version you measure your own ideas against.  A starter bot
is one.  (20.3)

**cantilevered shaft** -- a shaft held at one end only.  (20.2)

**Class Bot** -- a REV kit robot built to teach the basics:  frame squaring,
wheel hubs, and arm linkages.  (20.7)

**counterbalancing** -- offsetting an arm's or a carriage's weight with a
spring or a weight.  (20.7)

**decomposing a design** -- splitting a finished robot into its mechanisms and
asking of each one how it solved its one problem.  (20.4)

**Kickoff Starter Bot** -- a complete, competition-legal robot REV releases at
each season's Kickoff, built for that season's game.  (20.1)

**kit guide** -- REV's step-by-step assembly guide for one general-purpose
mechanism, such as a drivetrain or a lift.  (20.1)

**pattern** -- the way a design solves a problem that comes back every season,
such as mounting a motor against shock loads.  (20.2)

**squaring a frame** -- making a frame's corners true right angles.  (20.7)

### Check your understanding

1. What's the difference between a kit guide and a Kickoff Starter Bot?  Why
   does it change how much of each you keep as it comes?  (20.1)
2. Name one pattern worth studying in a starter bot from a game that's over.
   (20.2)
3. State the three rules for using a reference design in your own words, and
   give a reason for each.  (20.3, 20.4, 20.5)
4. A starter bot's lift has a limit switch at each end.  Which existing
   component might already own that shape, and where do you check?  (20.5)
5. Pick one drivetrain kit and explain, from what channel and extrusion each
   do well, why REV built it the way it did.  (20.6)
6. Why is a Class Bot worth building, and what does it teach that a rushed
   competition robot might skip?  (20.7)
7. Put the season-start habit in order, and say why mapping onto the code comes
   last.  (20.8)

## Where to go next

The parts every kit is built from are in Chapters 16 to 19, and the lift kits
in 20.6 are Chapter 19's cascading and continuous rigging.  Once you've decided
what to keep, Chapter 7 says whether it needs a new component, Chapter 9 shows
how components join an assembly, and Chapter 21 walks through designing a whole
robot, from its jobs down to its first file.
