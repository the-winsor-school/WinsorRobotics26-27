# Chapter 16 - Structure, Fasteners, and Shafts

*Studs up, ribs in, bevel out.*

---

Every mechanism on the robot hangs off a frame, and every frame this team builds comes from one system:  **REV DUO**.  This chapter covers the aluminum that makes up the frame, and the screws, nuts, and shafts that hold it together.

Look here when you're choosing what to build a frame from, bolting a bracket onto extrusion, putting a shaft through bearings, or cutting a part to length.  Most of it is about doing small things the right way every time.  A loose bracket or a shaft that slides out stops a mechanism with every part in it still whole.

This is the first chapter about building, and it assumes you've never built anything.  Have the real parts in front of you if you can:  a length of extrusion, an M3 screw, a nyloc nut, and a hex shaft.  Point at each one as you read about it.

| Entry | The question it answers |
|---|---|
| 16.1  REV DUO | What is the team's build system, and why use only one? |
| 16.2  Extrusion or channel? | Which kind of aluminum should this part be? |
| 16.3  The Extended Motion Pattern | Why do REV parts always seem to line up? |
| 16.4  Studs Up | How do you bolt a bracket onto extrusion? |
| 16.5  Ribs in, bevel out | Which way round do brackets and nyloc nuts go? |
| 16.6  Why a hex shaft? | Why are the team's shafts six-sided? |
| 16.7  Keeping a shaft in place | What stops a shaft sliding out of its bearings? |
| 16.8  Cutting extrusion and shafts | How do you cut a part to length safely? |

---

## 16.1  REV DUO

REV DUO is the build system made by REV Robotics, and Winsor Robotics builds with it exclusively.  Two numbers define it.

- **A 15mm grid.**  The structural pieces are 15 millimeters across, and their holes and slots are laid out to match.
- **A 5mm hex shaft.**  The shafts are six-sided steel rods 5mm across, and REV's gears, sprockets, pulleys, and wheels have hex holes that fit them.

Because the parts share those two numbers, a shaft or a mount from one mechanism fits the next one.  A few devices need an adapter, so check a part's hole pattern and bore before you plan around it.

**M3.**  Nearly every screw on the robot is an M3.  The M means metric, and the 3 is the screw's diameter:  3 millimeters.  The threads on an M3 screw are 0.5mm apart, so the tool that cuts M3 threads into a hole is called an M3 × 0.5 tap.  REV sells M3 hex cap screws, plain M3 nuts, and M3 nyloc nuts.  16.4 and 16.5 cover how they go together.

The team's parts reference opens with six "Golden Rules" that every builder is expected to know before turning a wrench.  Five are in this chapter, and the gearbox rule is in 17.4.  The first one applies to every screw you'll ever tighten.

> **Watch out.**  Snug, not stuck.  Aluminum threads strip under too much force, and so do threads in Delrin, the tough, slippery plastic REV uses for spacers and some gears.  Tighten an M3 screw until it's firmly seated, and stop there.  Forcing a screw until it refuses to turn is how threads get stripped.  On anything that shakes, use a nyloc nut or Blue Loctite (16.5).

The chapter summary has all six rules in one table, with the entry for each.

*See also:*  16.3, 16.4, Chapter 17

## 16.2  Extrusion or channel?

REV DUO gives you two kinds of structural aluminum, built on two different ideas.

**Extrusion** is a long, square aluminum bar with a slot running down each of its four sides.  The slots are called T-slots, because a cut across one is shaped like a T, and the head of an M3 screw slides along inside it.  The slot runs the whole length of the bar, so you can bolt something on at any spot, and slide it a few millimeters later.

**Channel** is aluminum shaped into a long box with one side open.  Its walls are covered in holes at fixed spacings, the pattern 16.3 describes.  You bolt things to the holes, at the spacings the holes give you.

| | 15mm extrusion | C-channel and U-channel |
|---|---|---|
| Cross-section | 15mm × 15mm, or 15mm × 30mm | 15mm × 45mm (C), or 45mm × 45mm (U) |
| Mounting | a continuous slot, adjustable to any spot | the Extended Motion Pattern, at fixed 8mm and 16mm spacings |
| Bearings | need end-bearing plates or pillow blocks | built-in 9mm bearing seats every 16mm |
| Torsional rigidity | moderate | extreme |
| Best for | prototypes, sensor mounts, custom angles, intake arms | drivetrain chassis rails, elevator towers, enclosed gearboxes |

Two words in that table need explaining.  A **bearing** is a ring that a shaft spins inside, smoothly, while the bearing keeps the shaft centered in its hole.  **Torsional rigidity** is how hard a part is to twist.  Grab a piece by both ends and wring it like a towel:  extrusion gives a little, and channel barely moves.

> **Predict.**  You're building a mount for a sensor, and you expect to redesign it three times this week.  Extrusion or channel?

Extrusion.  The continuous slot lets you slide the sensor along, re-position it, or move it somewhere else entirely, all without drilling a new hole.  While a design is still changing, being able to adjust it is worth more than stiffness.

Now flip it.  You're building the drivetrain rails, and they have to last the whole season.  Channel.  Once a design is settled, the built-in bearing seats and the stiffness matter far more than the freedom to slide things around.  Channel has one more advantage:  motors, sprockets, and chain can sit inside it, where its walls protect them from collisions on the field.

So the choice is a trade-off.  Extrusion costs you stiffness, and it makes you add separate plates to hold every bearing.  What it buys is the freedom to change your mind.  Channel costs you that freedom, and it pays you back in strength and a ready-made seat for every shaft.

Two extrusion parts are worth knowing by name.

- **15mm extrusion** (REV-41-1432) is the everyday one.  It comes in 150mm, 225mm, and 420mm lengths, or you can cut it to any length (16.8).  The round hole down its center is sized for an M3 × 0.5mm tap.  Tap threads into the end of a piece, and you can screw it straight onto a plate, end first, with no bracket.
- **15mm × 30mm extrusion** (REV-41-1433) has twice the cross-section, and it bends much less along its 30mm side.  Use it for arms held at one end only, and for tall, heavy elevator masts.

The channels are C-Channel (REV-41-1376) and U-Channel (REV-41-1377).

> **Argue it.**  The drivetrain is going to be channel by the end of the season.  Should it start out as channel on day one, or as extrusion until the design settles?  Starting in channel means nothing gets rebuilt later.  Starting in extrusion turns every early mistake into a slide and a retighten.

*See also:*  16.3, 16.4, Chapter 19

## 16.3  The Extended Motion Pattern

Channel, flat plates, and REV's motion brackets all share one hole pattern, the **Extended Motion Pattern**.  Learn it once and it explains why REV parts always seem to line up.

```
      ( ) 9mm Bearing Seat (every 16mm)
     /   \
  (o)     (o)  8mm pitch equilateral triangle grid
   |       |
  (o)=====(o)  16mm and 32mm diameter circular bolt circles
```

Three numbers carry the whole pattern.

- **8mm.**  M3 clearance holes run down the center line, exactly 8mm apart.  A clearance hole is a plain hole that a screw slides through, with no threads of its own.
- **16mm and 32mm.**  Groups of four holes sit on circles 16mm and 32mm across.  These are **bolt circles**, and motor faceplates, bearing pillow blocks, and brackets bolt onto them.
- **9mm.**  Every 16mm there's a 9mm hole called a bearing seat.  A REV Flanged Ball Bearing (REV-41-1329), or a bronze bushing, presses straight into it with no adapter.

Now look at what the bolt circles buy you.  Motor faceplates and bearing parts bolt to the same circles.  So a mechanism designed around those circles takes any of the parts that fit them, and you can change the motor or the gearbox later while the holes you already have still line up.

That's also why channel suits a drivetrain so well (16.2).  A shaft can pass through any of its 9mm seats, and the seat for its bearing is already there.

*See also:*  16.2, 16.7, Chapter 17

## 16.4  Studs Up

This is the rule for bolting anything onto 15mm extrusion:  a bracket, a plate, or a sensor.  The screw's head goes in the slot.

1. Slide the **head** of an M3 hex cap screw into the extrusion's slot.
2. The threaded part, called the **stud**, points out of the slot.
3. Set the bracket over the stud.
4. Thread an M3 nut onto the stud, from the outside.

Why does the head go inside?  The head of an M3 hex cap screw is the part shaped for the slot.  It slides along smoothly, and once it's in, it can't turn.  So you can tighten the nut from outside, and the screw underneath has nowhere to spin.  Turn it around, with the screw coming in from outside and the nut in the slot, and the slot has to grip a part shaped for some other job.  The team's reference says the fastener binds, or strips the edges of the slot.

**Load the bracket first.**  Fishing for loose hardware in a slot, with your other hand full of bracket, is how screws end up dropped inside the robot, where they can jam a mechanism later.  So put the bracket's screws in before it goes anywhere near the extrusion.

1. Push the screws through the bracket's holes from the side that will face the extrusion, so their heads are on that side.
2. Thread a nut onto each stud, loosely:  one or two turns.
3. Slide the whole bracket onto the extrusion as one piece, screw heads into the slot.
4. Slide it to where it goes, and tighten the nuts until they're snug.

**When the slot ends are closed.**  Studs Up needs an open end, because the screw head slides in from the end of the slot.  On a finished frame, the ends are usually bolted to something.  That's what a **Drop-In T-Nut** (REV-41-1493) is for.  It drops straight into the slot from above, anywhere along its length, and turns 90° to lock in place as you tighten the screw.  You can add a bracket mid-season and leave the frame in one piece.

> **Try it.**  Take a bracket, two M3 screws, two nuts, and a piece of extrusion.  Say each step out loud before you do it, then load the bracket and slide it on, Studs Up.  Before you tighten, the bracket should slide freely along the slot.  Snug the nuts, and it locks in place, with the screw heads gripped in the slot as you turn.

*See also:*  16.1, 16.5

## 16.5  Ribs in, bevel out

Two small parts have a right way round, and both are easy to get backwards.

**Alignment ribs face the slot.**  REV's plastic motion brackets, and many of its stamped aluminum ones, have small molded ridges on one face.  Those ribs fit into the extrusion's slot, and they push the bracket into a true 90° or 45° angle on their own.  So the ribbed face always goes toward the extrusion.

Put a bracket on backwards, ribs facing out, and you can see it.  The bracket tilts, and it sits at a visibly wrong angle, with a gap where it ought to sit flush.

> **Try it.**  Put two brackets on the same piece of extrusion, one the right way and one backwards, ribs out.  Look at both from the side.  The backwards one sits at a visible angle.  Learn that look, so you can spot it on somebody else's build at a glance.  Then turn the bad one around.

**Nyloc nuts:  bevel out.**  A **nyloc nut** (REV-41-1361) is an M3 nut with a ring of nylon inside one end.  The nylon squeezes around the screw's threads and grips them.  The side with the colored nylon ring is beveled, and it faces out, away from the bracket.  The flat metal side presses against the bracket or plate.

Why bother with the nylon?  A robot shakes.  It drives over the field, it hits walls, and other robots hit it.  Under all that shaking a plain nut slowly turns itself loose, and the nylon's grip on the threads is what stops it.  Use nyloc nuts on anything that vibrates, or Blue Loctite, a liquid that locks threads in place.

*See also:*  16.1, 16.4, 16.7

## 16.6  Why a hex shaft?

The **5mm hex shaft** used throughout this build system is a six-sided rod of stainless steel, 5mm across.

> **Predict.**  A round shaft is simpler to make.  So why would a whole build system standardize on six sides?

The answer is torque.  **Torque** is twisting force:  how hard something is being turned (17.1).  A motor puts torque into a shaft, and the shaft has to pass it on to whatever is mounted on it.

On a round shaft, a gear is often held by a **set screw**, a small screw that threads through the gear's hub and presses on the shaft at a single point.  Now suppose the motor stalls, which means something stops it turning while it's still pushing as hard as it can.  All that torque arrives at one small point, and the set screw slips.  The motor turns, and the gear sits there.

A hex shaft passes torque through its flat faces.  The hole in a matching gear is a hexagon too, so when the shaft turns, the gear turns with it.  The shape does the work that the set screw's grip was doing.  The parts still have limits, and a big enough load can round out a bore or twist a shaft, but a gear slipping on its shaft is off the list.

The second payoff is reuse.  REV's gears, sprockets, pulleys, and wheels share the same 5mm hex bore, so one shaft takes any of them.  A few devices need an adapter:  the Through Bore Encoder in 19.10 uses an insert to fit this shaft.

Hex shafts come in four stock lengths.

| Length | Part |
|---|---|
| 75mm | REV-41-1347 |
| 90mm | REV-41-1348 |
| 135mm | REV-41-1349 |
| 400mm | REV-41-1362, made to be cut down to the length you need (16.8) |

*See also:*  16.7, Chapter 17, Chapter 18

## 16.7  Keeping a shaft in place

Here's the part people miss.  A hex shaft refuses to twist, and it slides as freely as you please.  Push on one end and it slides straight out of its bearings.  The team's reference puts it this way:

> While hex shafts cannot slip in rotation, they will freely slide laterally out of bearings unless constrained.

So every shaft needs something on it that blocks sliding in both directions.  That sliding is called **axial play**, because it runs along the shaft's axis, and blocking it is called constraining the shaft.  It's one of the six Golden Rules.  REV makes these parts for the job.

- **Delrin spacers** (REV-41-1323) are short plastic tubes with a hex hole, 1.5mm, 3mm, or 6mm long.  They fill the short gaps between bearings, sprockets, and gears, so the parts on a shaft can't wander along it.
- **Shaft collars** (REV-41-1327) are metal rings that clamp onto the shaft with an M3 set screw.  A collar grips the shaft itself, so a collar is what keeps the shaft from sliding.
- **The Locking Motion Hub** (REV-41-1719) is a clamping shaft collar with a 16mm bolt circle on its face.  Bolt a metal sprocket or a high-torque wheel to it, and it locks that part to the shaft with no separate collar.
- **The High Strength Hex Hub** (REV-41-1147) clamps onto a plastic gear or sprocket.  Under heavy load a plastic hex hole can round itself out, until the shaft spins inside it.  The hub protects the hole.
- **Flanged bearings** help too.  A flanged bearing has a lip around its outer edge, and the team's rule is to mount it with the flange on the outside.

> **Watch out.**  Tighten a collar's set screw with a 1.5mm Allen wrench, onto one of the hex shaft's **flat faces**.  A corner is the wrong place for it, every time.  Put a drop of Blue Loctite on the set screw first, so it can't back itself out.

**Putting it together.**  Say a shaft runs through a bearing at each end, with a sprocket in the middle.  Here's one way to trap it.

```
   collar  bearing  spacers  sprocket  spacers  bearing  collar
     ||      |#|     [==]      |||      [==]     |#|      ||
  ===||======|#|=====[==]======|||======[==]=====|#|======||===
     ||      |#|     [==]      |||      [==]     |#|      ||
            wall                                wall
```

The spacers fill the gaps between the sprocket and the bearings, so the sprocket can't drift along the shaft.  The collars clamp the shaft just outside each bearing, so the shaft can't slide either way.  The shaft spins freely, and it's locked along its length.

> **Try it.**  Build that stack with real parts:  a shaft through two bearings, a sprocket, spacers, and two collars.  Spin the shaft with your fingers, then push on one end.  It should spin easily and refuse to slide.  If it slides, find the gap and fill it with a spacer, or move a collar up tight against its bearing.

*See also:*  16.3, 16.6, Chapter 18

## 16.8  Cutting extrusion and shafts

Sooner or later you'll need a part in a length REV doesn't sell, and extrusion and hex shaft both get cut down by hand.

1. **Mark it.**  Wrap painter's tape around the part at the length you want, and draw a crisp line on the tape with an ultra-fine Sharpie.
2. **Cut it.**  Use a horizontal bandsaw with the part clamped in its vise, or a hacksaw with a miter box.  Cut along the inside of the line.
3. **Deburr it.**  Run a flat metal file over the fresh cut end.  Cutting leaves **burrs**, sharp little curls of metal along the edge, and filing them off is called deburring.  A burr left in a T-slot jams screw heads, and a burr on an edge slices the insulation off wires.

> **Watch out.**  Cut 15mm extrusion on a bandsaw or with a hacksaw, with the work clamped and a mentor there.  Keep it away from the chop saw and the powered miter saw.  A high-speed wood blade grabs the light aluminum profile and throws it out of the vise.  That wrecks the piece and the blade, and it can badly hurt whoever is standing there.  A bandsaw or a hacksaw cuts slowly enough that a clamped extrusion stays put.

Learn the reason well enough to explain it to the next person who reaches for the chop saw.

*See also:*  16.2, 16.6

---

## Chapter summary

The frame is REV DUO:  a 15mm grid and a 5mm hex shaft, so the parts line up with each other.  Extrusion lets you slide things along and change your mind, and channel is stiff and gives every shaft a seat.  Screws go in Studs Up, brackets go ribs in, and nyloc nuts go bevel out.  A gear on a hex shaft can't slip, so the job left is to trap the shaft from both sides.  Cut on a bandsaw or with a hacksaw, clamped, and deburr every cut end.

The team's six Golden Rules, and where each one is explained:

| Golden Rule | What it means | Entry |
|---|---|---|
| Snug, not stuck | tighten until seated, then stop | 16.1 |
| Studs Up | screw head in the slot, nut on the outside | 16.4 |
| Ribs face the slot | the ribs square the bracket for you | 16.5 |
| Nyloc bevel faces out | nylon ring out, flat side on the bracket | 16.5 |
| Highest reduction closest to the motor | the gearbox rule | 17.4 |
| Constrain every shaft | something clamped on each side | 16.7 |

### Vocabulary

**alignment ribs** -- small molded ridges on one face of a REV bracket.  They fit into the extrusion's slot and square the bracket at 90° or 45°.  (16.5)

**axial play** -- a shaft sliding along its own length.  Constraining a shaft removes it.  (16.7)

**bearing** -- a ring a shaft spins inside, smoothly, which keeps the shaft centered in its hole.  (16.2)

**bolt circle** -- four holes on a circle, 16mm or 32mm across, where motor faceplates and bearing parts bolt on.  (16.3)

**burr** -- a sharp curl of metal left along a fresh cut.  Filing it off is deburring.  (16.8)

**channel** -- aluminum shaped into a long box with one side open, with the Extended Motion Pattern in its walls.  C-channel is 15mm × 45mm, and U-channel is 45mm × 45mm.  (16.2)

**Drop-In T-Nut** -- a nut that drops into an extrusion slot from above and turns 90° to lock, for adding a bracket to a finished frame.  (16.4)

**Extended Motion Pattern** -- REV's hole pattern for channel, plates, and motion brackets:  M3 holes at 8mm, bolt circles of 16mm and 32mm, and a 9mm bearing seat every 16mm.  (16.3)

**extrusion** -- a long, square aluminum bar with a T-slot down each side, for bolting things on at any spot.  (16.2)

**hex shaft** -- the six-sided, 5mm steel shaft REV DUO mechanisms turn on.  (16.6)

**M3** -- the metric screw size used all over the robot:  3mm across, with threads 0.5mm apart.  (16.1)

**nyloc nut** -- an M3 nut with a nylon ring that grips the threads against vibration.  The beveled side, with the ring, faces out.  (16.5)

**REV DUO** -- REV Robotics' build system, built on a 15mm grid and a 5mm hex shaft.  The team builds with it exclusively.  (16.1)

**set screw** -- a small screw that threads through a hub and presses on a shaft to hold the hub in place.  (16.6, 16.7)

**shaft collar** -- a metal ring that clamps onto a hex shaft with a set screw, to keep the shaft from sliding.  (16.7)

**spacer** -- a short Delrin tube with a hex hole, 1.5mm, 3mm, or 6mm long, that fills a gap along a shaft.  (16.7)

**Studs Up** -- the rule for extrusion:  the screw head goes in the slot, the stud points out, and the nut goes on from outside.  (16.4)

**torque** -- twisting force.  Chapter 17 has the rest of it.  (16.6)

**torsional rigidity** -- how hard a part is to twist.  (16.2)

### Check your understanding

1. Why is extrusion the right choice for a prototype sensor mount, and channel the right choice for a finished drivetrain rail?  (16.2)
2. Two motor faceplates both bolt to the same 16mm bolt circle.  What does that let the team do later?  (16.3)
3. State the Studs Up rule.  Why is it the screw head that goes in the slot, and the nut that stays outside?  (16.4)
4. A bracket on the robot sits at a slight angle, with a gap along one edge.  What's the likeliest mistake, and how do you fix it?  (16.5)
5. Why does a gear on a hex shaft need no set screw to keep from slipping, when a gear on a round shaft does?  (16.6)
6. A hex shaft turns its gear with no set screw, and it still needs something to keep it from sliding along its length.  Name two parts the team uses to stop that sliding, and say where they go on a shaft with a sprocket in the middle.  (16.7)
7. Why does extrusion get cut on a bandsaw or with a hacksaw, and never on a chop saw?  And why deburr the cut?  (16.8)

## Where to go next

Chapter 17 is about the motors and gearboxes that turn these shafts, and the arithmetic for choosing one.  Chapter 18 puts gears, sprockets, pulleys, and wheels on the shafts, and uses the 15mm grid to space them.  Chapter 19 builds linear slides from extrusion and channel.  Chapter 20 walks through REV's own kits, where every rule in this chapter shows up in a finished build.
