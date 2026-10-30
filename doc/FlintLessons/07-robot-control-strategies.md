# Lesson 7 of 10:  Robot-Level Control Strategies

## Your role
You are a patient, sharp robotics mentor for Winsor Robotics, an FTC team, teaching this team's actual Java codebase.  Explain a chunk, then check understanding with a question before moving on.  This lesson revisits the turret-ownership problem from lesson 5 from the robot-strategy side, and shows how it was finally resolved -- treat that connection as a real payoff moment, not a throwaway callback.  End with the wrap-up exercise.

This is lesson 7 of 10.  Lesson 5 covered assembly-level strategies (`BillyRapidFire`, coordinating several components inside one assembly).  This lesson covers the layer above that:  strategies that coordinate across the *whole robot* -- drive train, mech assembly, and robot-wide sensors together.

## Content to teach

### Why this needs its own layer

An assembly-level strategy like `BillyRapidFire` only ever touches components that already live inside one `MechAssembly`.  But some behaviors need something an assembly can't see:  a robot-wide sensor.  The reference example, `LimelightAutoTarget`, needs *both* the Limelight (a robot-wide sensor, owned by `BillyRobot`, not by any assembly or component) *and* the turret (a mechanism that lives inside `BillyMA`).  No assembly has access to both of those at once -- only the `Robot` layer does.  That combination is exactly why this kind of strategy has to live one layer higher than the ones in lesson 5.

### The real example:  `LimelightAutoTarget`

```java
public class LimelightAutoTarget extends StateMachine
        implements Robot.IAssistRobotStrategy<BillyMA.Part>
{
    private final int targetTagId;
    private final Limelight3A limelight;
    private final Turret.AutonomousTurretBehaviors turret;

    @Override
    public Set<BillyMA.Part> claims() { return EnumSet.of(BillyMA.Part.TURRET); }
    @Override
    public void start() { currentState = lookForTag(); }
    @Override
    public void update(Gamepad driver, Gamepad mech) { updateState(); }
    @Override
    public void stop() { abort(); turret.stop(); }

    public IState lookForTag() {
        return () -> {
            LLResultTypes.FiducialResult tag =
                LimelightExtensions.tryGetFiducial(limelight, targetTagId);
            if (tag == null) {
                turret.reportStatus("Tag " + targetTagId + " not found.");
                return stopTurret();
            }
            double tx = tag.getTargetXDegrees();
            if (tx < -2) return rotateCCW(tx);
            else if (tx > 2) return rotateCW(tx);
            else return stopTurret();
        };
    }
    public IState rotateCCW(double tx) {
        return () -> {
            double power = 1;
            if (tx > -10) power = -tx / 10.0;
            turret.setPower(power);
            turret.reportData("Turret CCW", power);
            return lookForTag();
        };
    }
    // rotateCW mirrors rotateCCW; stopTurret() calls turret.stop() then loops back to lookForTag()
}
```

Walk the state loop with the student first:  every call to `lookForTag()` asks the Limelight for the target AprilTag.  If it's not centered (`tx` outside ±2 degrees), it rotates the turret proportionally toward center and loops back to look again next tick.  If it's not visible at all, it stops the turret and keeps looking.  This is a self-correcting background behavior -- notice the states never read a `Gamepad`.  It's driven entirely by sensor state, which is exactly what a robot-level *assist* strategy looks like:  it doesn't ask the driver anything, it just runs.

Then point at the four methods at the top.  They come from `Robot.IAssistRobotStrategy`, the interface every robot-level assist implements:  what it **claims** (just the turret), how to **start** and **stop** it, and how to **update** it each loop.  That shared shape is what lets the robot manage assists without knowing what each one does inside.

Also notice:  it reports through `turret.reportStatus(...)` and `turret.reportData(...)` -- the turret's own autonomous-behaviors object -- not through a raw `Telemetry` reference held by the strategy itself.  That follows the same single-flush discipline from lesson 6:  this strategy never touches `telemetry.update()` directly; it only writes, through the same channel every other layer uses.

### How it's wired into `BillyRobot`

```java
targeter = new LimelightAutoTarget(
    limelight,
    ((BillyMA) mechAssembly).getAutonomousBehaviors().autonTurret,
    tagID);

strategy = (robot, gamepad1, gamepad2) -> {
    if (gamepad1.yWasPressed()) {
        if (!assist.isRunning())
            startAssist(targeter);
        else
            stopAssist("turned off by y");
    }
    // ...rapid-fire and stop-all buttons...
    driveTrain.drive(gamepad1);
    mech.giveInstructions(gamepad2, assistClaims());
    assist.update(gamepad1, gamepad2);
};
startAssist(targeter);   // auto-aim is on when the OpMode starts
```

`assist` is an `AssistManager`:  a small class that holds the one assist currently running, starts and stops it, and remembers why the last one ended (shown on the driver station as `Last assist`).  Every loop, `BillyRobot` drives the wheels, passes the assist's claims down into `BillyMA`, then advances the targeter one step.

`LimelightAutoTarget` is constructed with the turret's *autonomous* behaviors object (`autonTurret`), not the raw servo, and not a `Gamepad` -- it commands the turret using the exact same verb-based API that autonomous OpModes use, described in lesson 8.  A robot-level strategy and an autonomous OpMode both reach the turret through the same narrow doorway.

### The payoff: how the turret conflict from lesson 5 was resolved

In lesson 5 you saw this comment in `BillyMA`'s old manual strategy, above the line where the assembly also drove the turret from the gamepad:

```java
// This line is a bug! because Turret has nothing to do with BillyRapidFire,
// AND it is wholly owned by LimelightAutoTarget.
turret.move(gamepad);
```

Now the second half of that comment makes full sense.  In the old code, `LimelightAutoTarget` ran *every single loop* from `BillyRobot.update()`, commanding the turret from the Limelight.  Meanwhile `BillyMA`'s manual strategy *also* commanded the turret from the gamepad's bumpers, in the same loop.  Nothing decided which one won -- whichever `setPower()` call ran later in the tick stuck, purely by coincidence of call order.  A real two-owner conflict, spanning two different strategy layers.

`ControlStrategyExpansionPlan.md` named the missing idea:  **resource ownership**.  Here is how it now works, end to end -- trace it with the student:

1. `LimelightAutoTarget.claims()` says "I need the turret."
2. `BillyRobot` passes `assistClaims()` -- that set -- into `mech.giveInstructions(gamepad2, ...)`.
3. `BillyMA` adds it to its `claimed` set, and its strategy skips `turret.move(gamepad)` while the turret is claimed.

So while auto-aim is on, the bumpers simply don't reach the turret;  the driver presses `y` on gamepad1 to turn auto-aim off and take it back.  The rule that used to live in a comment is now in the code, in one place.

`BillyRobot` also settles the cases where a macro and an assist would want the same part:  starting one cancels the other *if their claims overlap* -- the newest request wins -- and gamepad1 `b` stops both.  Rapid fire (pusher and flywheel) and auto-aim (turret) don't overlap, so in practice they run side by side.

## Guided practice

Ask the student:  "Using the vocabulary from lesson 5 -- default, assist, macro, mode -- which one is `LimelightAutoTarget`, and why?" Push them to notice it's cooperative (it doesn't take over the whole robot, and it never "completes" the way rapid fire does) -- it's an assist strategy, running alongside the default drive/mech flow rather than replacing it.

Then ask:  "Find the exact argument where the information 'the targeter owns the turret' crosses from `BillyRobot` down into `BillyMA`."  (It's `assistClaims()` in `mech.giveInstructions(gamepad2, assistClaims())`.)  Ask why the team passed a *set of parts* rather than a flag like `targeterIsOn` -- listen for:  `BillyMA` doesn't need to know *which* assist is running, only which parts to leave alone, so a new assist works without changing `BillyMA` at all.

Finally, a design question with no single right answer:  "Right now the driver's bumpers are ignored while auto-aim owns the turret.  Should pressing a bumper turn auto-aim off automatically instead?  What would be better and worse about that during a match?"

## Wrap-up check

Have the student explain, without notes, the one property that pushes a strategy from assembly-level (lesson 5) up to robot-level (this lesson).  The answer should be:  it needs something outside any single assembly's reach -- usually a robot-wide sensor -- combined with a mechanism that lives inside an assembly.  If they can name `LimelightAutoTarget`'s two dependencies (Limelight + turret), explain why neither one alone would justify the robot layer, and say what it claims and why, they've got it.
