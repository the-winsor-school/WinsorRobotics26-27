# REV DUO Linear Motion & Lifts Guide

> **Winsor Robotics FTC Resource Directory**  
> Component: Linear Slides, Cascading vs. Continuous Lifts, and Rigging

Linear elevators and extending arms are essential for reaching high scoring goals in FTC (such as hanging hooks, high baskets, and submersible rungs). The REV DUO build system uses its 15mm extrusion slots as linear rail tracks, paired with Acetal slider blocks and pulley rigging.

---

## 1. REV 15mm Linear Slide Architecture ([REV-45-1507](https://www.revrobotics.com/rev-45-1507/))

Instead of relying on heavy off-the-shelf industrial drawer slides, REV DUO uses an extrusion-based slide system:

- **Slider Blocks ([REV-41-1508](https://www.revrobotics.com/rev-41-1508/)):** Precision-molded low-friction Acetal blocks that slide inside the continuous T-slot of 15mm extrusion.
- **Slider Brackets:** Sheet aluminum brackets that sandwich the slider blocks and bolt to the next extrusion stage.
- **Pulleys ([REV-41-1510](https://www.revrobotics.com/rev-41-1510/)):** Ball bearing Delrin pulleys that mount to extrusion ends to route lift cord.
- **Cord:** Ultra-High-Molecular-Weight Polyethylene (UHMWPE) braided cord (Dyneema or Spectra). Will not stretch under tension.

---

## 2. Cascading vs. Continuous Lifts: Architectural Comparison

The fundamental design decision when building an FTC elevator is choosing between **Cascading** and **Continuous** rigging.

| Feature | Cascading Lift | Continuous Lift |
|---|---|---|
| **Extension Behavior** | All stages extend **simultaneously** | Stages extend **sequentially** (bottom to top) |
| **Speed** | **Extremely Fast** ($n \times \text{spool speed}$, where $n$ = stages) | Standard (equal to spool line speed) |
| **Motor Torque Needed** | High ($n \times \text{load weight}$) | Low ($1 \times \text{load weight}$) |
| **Rigging Complexity** | High (separate cable loop per stage) | Low (one continuous cable run) |
| **Active Retraction** | Yes (positive dual-cable drive up & down) | Difficult (usually relies on gravity or springs) |
| **Cable Slack Risk** | Low (if stages are pre-tensioned) | High (cable can jump off pulleys during rapid drops) |
| **Best Used For** | Fast competition lifts, high baskets, hanging | Simpler reach mechanisms, light arms |

---

## 3. How the Rigging Works

### Cascading Rigging Logic
In a cascading lift, the motor spool only directly connects to **Stage 1**:
1. Motor pulls cable $\rightarrow$ Stage 1 rises.
2. A cable anchored to the Base passes over a pulley at the top of Stage 1 and anchors to the bottom of Stage 2.
3. As Stage 1 moves up 1 inch, it forces Stage 2 to move up 2 inches relative to the base!
4. Another cable from Stage 1 passes over Stage 2's top pulley to anchor to Stage 3.
5. **Result:** A 3-stage lift extends to full height in $\frac{1}{3}$ the time of a continuous lift.

```
       [Top Pulley]
            ||
   +--------++--------+
   |                  |
   | (Stage 1 moves)  |====> Pulls Stage 2 upward at 2x speed!
   |                  |
   +--------++--------+
            ||
       [Fixed Anchor]
```

### Dual-Spool Active Retraction
Gravity alone is often insufficient to pull lightweight multi-stage slides down rapidly during matches.
- Mount a **dual spool** on the drive motor shaft.
- Wind the **Lift Cable** clockwise on spool section A.
- Wind the **Retract Cable** counter-clockwise on spool section B.
- When the motor spins forward, it reels in the lift cable while paying out the retract cable. When the motor reverses, it actively yanks the lift down with full motor power!

---

## 4. Constant-Force Springs ([REV-41-1644](https://www.revrobotics.com/rev-41-1644/))

A constant-force spring is a rolled strip of spring steel that provides an unchanging pulling force regardless of extension length:

- **Why use them?** Lifting a 2 kg carriage against gravity draws heavy motor current. When the lift pauses at the top to score, the motor must hold stall current, causing it to heat up and trip breakers.
- **Counterbalancing:** Mounting two 1.5 lb constant-force springs will offset 3 lbs of carriage weight. The motor only has to overcome friction, allowing you to use a faster gear ratio without stalling.

---

## 5. Software Protection with `DoublyLimitedMotor`

Never run a linear slide without hardware limit switches! Without limit switches, a driver error or software crash will drive the slide into its physical stops, snapping lift cords or bending extrusion brackets.

In our codebase, wrap your lift motor in **[`DoublyLimitedMotor`](file:///Users/jcox/Documents/GitHub/WinsorRobotics26-27/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotModel/Mechs/Components/DoublyLimitedMotor.java)**:

```java
// Example: Creating a doubly-limited linear lift in your MechAssembly
TouchSensor bottomLimit = hardwareMap.get(TouchSensor.class, "liftBottomLimit");
TouchSensor topLimit    = hardwareMap.get(TouchSensor.class, "liftTopLimit");

lift = new DoublyLimitedMotor(
    hardwareMap,
    "liftMotor",
    "liftBottomLimit",
    "liftTopLimit",
    (motor, gamepad) -> {
        double power = -gamepad.left_stick_y; // Stick up = positive power
        motor.setPower(power);
    }
);
```

### Autonomous Zeroing Procedure:
1. In `autonomousInit()`, drive the lift motor slowly downward (`power = -0.15`) until `bottomLimit.isPressed()`.
2. Call `motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER)`.
3. Switch to `RUN_TO_POSITION`. The robot now has an exact, calibrated millimeter zero point that survives throughout the entire match.

---

## Official Links & References
- [REV DUO Linear Motion Kit Overview](https://docs.revrobotics.com/duo-build/linear-motion-kit.md)
- [Three Stage Cascading Lift Assembly Instructions](https://docs.revrobotics.com/duo-build/linear-motion-kit/three-stage-cascading-lift.md)
- [Three Stage Continuous Lift Assembly Instructions](https://docs.revrobotics.com/duo-build/linear-motion-kit/three-stage-continuous-lift.md)
- [Linear Motion Acetal Sliders](https://www.revrobotics.com/rev-41-1508/)
- [Constant Force Springs Guide](https://www.revrobotics.com/rev-41-1644/)

