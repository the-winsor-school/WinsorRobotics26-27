# Chapter 13 - Control Ownership

*One owner at a time, and a rule for who.*

---

Billy has four mechanisms in its assembly, and three kinds of code that want
to move them:  the driver's gamepad, rapid fire, and turret targeting.  A
motor or a servo obeys whatever it was told last, so when two of those want
the same part in the same tick, something has to decide.

This chapter is about how Billy decides.  The big idea is short:  a part
needs one owner at a time.  Each special job says which parts it needs, the
driver's everyday controls step aside on those parts, and the robot has a
rule for what happens when two jobs want the same one.  Look here when
you're wondering why the turret ignores the bumpers, what the `Assist` and
`Macro` lines on the telemetry screen mean, or how to add a new job without
breaking the old ones.

Read first:  Chapter 12, for how rapid fire and targeting run one step per
tick.  Chapter 8, for control strategies.  Chapters 9 and 10, for the
assembly and robot layers.

| Entry | The question it answers |
|---|---|
| 13.1 | What's a resource, and why does it need one owner? |
| 13.2 | Where did the claims system come from? |
| 13.3 | What are default, assist, macro, and mode strategies? |
| 13.4 | How does a strategy say which parts it needs? |
| 13.5 | What does `MacroManager` do? |
| 13.6 | Who controls the turret? |
| 13.7 | What do the buttons do, and who wins when two jobs collide? |
| 13.8 | What does stop all stop? |
| 13.9 | What do the strategy lines on the telemetry screen mean? |
| 13.10 | What's built, and what's still a plan? |

---

## 13.1  Resources and owners

*Where it lives:*  `RobotModel/Mechs/Assemblies/BillyMA.java`

Billy's assembly names its four mechanisms in one short list:

<<code RobotModel/Mechs/Assemblies/BillyMA.java 25-30>>

> **Aside:  `enum`**
>
> An `enum` is a type with a fixed list of named values.  `BillyMA.Part` has
> exactly four:  `Part.INTAKE`, `Part.PUSHER`, `Part.FLYWHEEL`, and
> `Part.TURRET`.  A variable of type `Part` holds one of those four, and the
> compiler rejects anything else.

Each of those parts is a **resource**:  a piece of the robot that takes
commands, where two different commands at once would fight.  The turret is
the clearest one.  It's a continuous-rotation servo, and every tick it spins
at whatever power the last `setPower` call gave it.

Picture two pieces of code that both want the turret in the same tick.  The
driver's right bumper says spin at `-1`.  Targeting says spin at `0.3`,
toward the tag.  Both calls happen.  The turret gets whichever one ran later
in the tick, and the other is thrown away.  Which one ran later depends on
the order the lines happen to sit in the code.  Move one line, and the robot
behaves differently.

So a resource needs an **owner**:  the one piece of code allowed to command
it right now.  **Ownership** is that right, and it can pass from one piece
of code to another while the robot runs.  **Arbitration** is deciding who
gets it when two want it at once.

Billy's answer, in one sentence:  each special job declares which parts it
needs, and the driver's everyday controls skip those parts while the job
runs.  The declaration is called a **claim**.  Rapid fire claims the pusher
and the flywheel.  Targeting claims the turret.  Everything nobody has
claimed belongs to the driver.

The rest of this chapter is how that sentence turns into code.  13.4 shows
the claims, 13.5 and 13.7 show who decides, and 13.6 follows the turret
through all of it.

*See also:*  13.3, 13.4, 13.6, Chapter 3, Chapter 9
