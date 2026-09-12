# REV DUO Hardware Design Decision Guide

> **Winsor Robotics FTC Resource Directory**  
> Component: Engineering Calculations, Trade-Off Analyses, and Mechanism Archetypes

Good robot engineering is the art of trade-offs: balancing torque against speed, weight against stiffness, and complexity against reliability. This guide walks Winsor Robotics students through the mathematics and design decisions needed to build high-performance FTC mechanisms.

---

## 1. Actuator Selection Decision Tree

When designing a new mechanism, follow this decision tree to pick the right actuator:

```
                      [What motion do you need?]
                                  |
                +-----------------+-----------------+
                |                                   |
         [Limited Angle]                  [Continuous Rotation]
          (< 270 degrees)                           |
                |                          [Power Requirement]
        [Load Requirement]                          |
                |                      +------------+------------+
        +-------+-------+              |                         |
        |               |          [Low Power]              [High Power]
     [< 1.3 N·m]     [> 1.3 N·m]   (< 10 Watts)             (> 10 Watts)
        |               |              |                         |
   Smart Robot     HD Hex Motor    Core Hex Motor           HD Hex Motor
      Servo      + UltraPlanetary  (Compact 125 RPM)      + UltraPlanetary
  (Angular Mode)  (Doubly Limited)                           (Custom Gearing)
```

| Question to Ask | Answer | Recommended Choice | Rationale |
|---|---|---|---|
| Need to open/close a lightweight claw or drop a gate? | Limited angle, low load | **Smart Robot Servo (Angular)** | Built-in positional control; holds position without motor encoder loops. |
| Need a fast, compact intake roller? | Continuous, low load | **Core Hex Motor** | Integrated 20:1 gearbox, 125 RPM, compact form factor fits in tight spaces. |
| Need a 1,500+ RPM flywheel launcher? | Continuous, high speed | **HD Hex (3:1 or 4:1 UltraPlanetary)** | High power, 550 motor RPM capabilities, smooth belt drive. |
| Need a high-speed cascading lift elevator? | Linear, high torque | **HD Hex (5:1 + 4:1 + 3:1 UltraPlanetary)** | Multi-stage planetary multiplies torque to overcome carriage weight. |
| Need to rotate a heavy arm holding game pieces? | Limited angle, high torque | **HD Hex (5:1 + 5:1 + 4:1 UltraPlanetary)** | Massive reduction prevents motor back-driving under gravitational load. |

---

## 2. Engineering Calculations for Mechanism Design

### A. Power, Work, and Mechanical Speed
- **Work ($W$):** Energy required to move an object of mass $m$ over distance $d$ against gravity $g$:
  $$W = m \cdot g \cdot d \quad \text{[Joules]}$$
- **Power ($P$):** Rate of work over target completion time $t$:
  $$P = \frac{W}{t} \quad \text{[Watts]}$$
- **FTC Safety Factor ($SF$):** Always multiply calculated power by a safety factor of **$2.0$ to $2.5$** to account for friction, cable drag, and battery voltage sag:
  $$P_{\text{design}} = P \times 2.0$$

#### Worked Example: Sizing an Elevator Motor
- Combined carriage + game piece weight: $m = 2.0\text{ kg}$ (~4.4 lbs)
- Maximum lift extension height: $d = 1.2\text{ meters}$
- Desired extension time: $t = 1.5\text{ seconds}$
- Calculate Work:
  $$W = 2.0\text{ kg} \times 9.81\text{ m/s}^2 \times 1.2\text{ m} = 23.54\text{ Joules}$$
- Calculate Base Power:
  $$P = \frac{23.54\text{ J}}{1.5\text{ s}} = 15.7\text{ Watts}$$
- Apply Safety Factor ($SF = 2.0$):
  $$P_{\text{design}} = 15.7\text{ W} \times 2.0 = 31.4\text{ Watts}$$
- **Actuator Selection:** The HD Hex Motor produces ~40 Watts peak output. A single HD Hex motor can comfortably handle this elevator! A Core Hex motor (peak ~10 Watts) would stall and burn out.

---

### B. Rotational Speed to Linear Velocity
To calculate how fast a wheeled drivetrain, spool, or roller will move linearly:
$$V = \pi \times D \times \left(\frac{\text{RPM}}{60}\right)$$
- $V$ = Linear velocity (meters/sec or inches/sec)
- $D$ = Diameter of the wheel or spool (meters or inches)
- $\text{RPM}$ = Shaft rotational speed

---

## 3. Mechanism Design Archetypes with REV DUO

### Archetype 1: The FTC Competition Mecanum Drivetrain
- **Target Robot Speed:** $1.4\text{ m/s}$ to $1.8\text{ m/s}$ (~4.5 to 6.0 ft/s). Faster than 6 ft/s makes precision alignment difficult; slower than 4 ft/s leaves you vulnerable to defense.
- **Recommended Setup:**
  - 4 × HD Hex Motors
  - 4 × UltraPlanetary Gearboxes with **5:1 + 4:1** cartridges (Exact ratio: **18.88 : 1**)
  - Free speed at output: $6000\text{ RPM} / 18.88 \approx 318\text{ RPM}$
  - Wheels: 90mm (0.090m) REV Mecanum Wheels
- **Calculated Speed:**
  $$V = \pi \times 0.090\text{ m} \times \left(\frac{318}{60}\right) = 1.50\text{ m/s} \quad (\approx 4.9\text{ ft/s})$$
  *This is the universally recognized sweet spot for FTC competition driving!*

---

### Archetype 2: Heavy Lifting Arm / Shoulder Joint
- **Design Challenge:** Gravitational torque increases as an arm extends horizontally ($\tau = \text{Force} \times \text{Length}$).
- **Recommended Setup:**
  - 1 or 2 × HD Hex Motors
  - UltraPlanetary with **5:1 + 5:1 + 4:1** cartridges (Exact ratio: **98.74 : 1**)
  - Additional external chain or gear reduction (e.g. 15T sprocket driving a 45T sprocket = 3:1 external ratio)
  - Total reduction: $98.74 \times 3 = 296 : 1$
  - Output speed: $6000 / 296 \approx 20\text{ RPM}$ ($120^\circ\text{ per second}$)
  - **Counterbalancing:** Always attach surgical tubing or constant-force springs to pull against gravity when the arm is extended.

---

### Archetype 3: Compliant Roller Intake
- **Design Challenge:** Must aggressively grab game pieces without jamming or stalling when multiple pieces wedge together.
- **Recommended Setup:**
  - Core Hex Motor direct drive (125 RPM) OR HD Hex Motor with single 5:1 UltraPlanetary (~1,150 RPM)
  - 75mm 30A (Soft Green) Compliant Wheels
  - **Floating Geometry:** Never rigidly lock the intake shaft at a fixed distance from the floor. Mount the intake shaft on an articulated pivot or cantilevered arm supported by rubber bands. This allows the roller to climb over game pieces while maintaining continuous downward normal force.

---

## 4. Ticks-to-Inches Math for Autonomous Code

When programming autonomous paths in TeamCode, convert encoder ticks into physical distance units:

$$\text{Ticks Per Unit} = \frac{\text{Encoder Counts Per Rev} \times \text{Gear Ratio}}{\pi \times \text{Wheel/Spool Diameter}}$$

### HD Hex Motor UltraPlanetary Lookup Table (90mm Wheels)

| Cartridge Stack | Exact Ratio | Output Ticks / Revolution | Ticks Per Millimeter | Ticks Per Inch |
|---|---|---|---|---|
| **3:1 + 4:1** | 10.49 : 1 | ~293.7 ticks | 1.04 ticks/mm | 26.4 ticks/in |
| **5:1 + 3:1** | 15.15 : 1 | ~424.2 ticks | 1.50 ticks/mm | 38.1 ticks/in |
| **5:1 + 4:1** | **18.88 : 1** | **~528.6 ticks** | **1.87 ticks/mm** | **47.5 ticks/in** |
| **5:1 + 5:1** | 27.35 : 1 | ~765.8 ticks | 2.71 ticks/mm | 68.8 ticks/in |

*Formulas assume REV HD Hex Motor base encoder (28 ticks/rev of motor rotor) and standard 90mm (3.543 inch) wheel diameter.*

