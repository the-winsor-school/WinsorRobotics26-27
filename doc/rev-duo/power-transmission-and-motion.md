# REV DUO Power Transmission & Motion Guide

> **Winsor Robotics FTC Resource Directory**  
> Component: Gears, Sprockets & Chain, Timing Belts, and Wheel Systems

Power transmission components move mechanical energy from motors to where it is needed on the robot. Because REV DUO is built on a 15mm structural grid, its gears, sprockets, and pulleys are mathematically designed to mesh at integer multiples of 15mm.

---

## 1. 0.5 Module Gears & The 15mm Grid

REV DUO spur gears use the **Metric Module 0.5 (0.5 Mod)** tooth profile with a standard 5mm hex bore.

### Gear Mathematics
- **Pitch Diameter ($PD$):**
  $$PD = \text{Teeth} \times \text{Module} = N \times 0.5\text{ mm} = \frac{N}{2}\text{ mm}$$
- **Center-to-Center Distance ($CD$):**
  $$CD = \frac{PD_1 + PD_2}{2} = \frac{N_1 + N_2}{4}\text{ mm}$$

### How REV Gears Align with 15mm Extrusion & Channels
Notice the symmetry: any gear pair whose tooth counts sum to a multiple of 60 meshes at a multiple of 15mm!

| Driver Gear | Driven Gear | Sum of Teeth | Center Distance ($CD$) | Extrusion / Channel Alignment |
|---|---|---|---|---|
| **20T** | **40T** | 60 | **15.0 mm** | **Exactly 1 standard 15mm extrusion width!** |
| **40T** | **80T** | 120 | **30.0 mm** | **Exactly 2 standard extrusion widths (30mm)!** |
| **20T** | **100T** | 120 | **30.0 mm** | **Exactly 2 standard extrusion widths (5:1 reduction)!** |
| **60T** | **60T** | 120 | **30.0 mm** | 1:1 idler spacing |
| **30T** | **90T** | 120 | **30.0 mm** | 3:1 reduction |

### Gear Assembly Best Practices
1. **Never run gears dry without grease:** Apply a light coat of white lithium grease or teflon lube to reduce friction and eliminate gear whine.
2. **Prevent gear climbing:** Under high torque, plastic gear teeth can deflect and skip over each other. For high-torque joints (such as arm pivots), clamp a **High Strength Hex Hub ([REV-41-1147](https://www.revrobotics.com/rev-41-1147/))** onto the gear face using M3 screws and nyloc nuts.
3. **Idler Gears:** To bridge a long distance between shafts, insert an idler gear. Idler gears change the direction of rotation but **do not alter the gear ratio**.

---

## 2. Sprockets and #25 Roller Chain

Chain drives are the gold standard for transferring torque across long spans where gears are too bulky or heavy (such as drivetrains and lift winches).

### Chain Specifications
- **Chain Size:** Standard ANSI **#25 Roller Chain** (0.25" / 6.35mm pitch).
- **Sprocket Sizes:** 10T, 15T, 20T, 30T, and 40T with 5mm hex bore.
- **Materials:** Acetal (lightweight, intake/roller use) and Sintered Steel/Aluminum (drivetrains and high-shock loads).

### REV Chain Tool ([REV-41-1442](https://www.revrobotics.com/rev-41-1442/))
The REV chain tool allows builders to push pins out of #25 chain and press them back together without needing weak master links.
- **Breaking Chain:** Place the chain link in the slot, align the drive pin with the chain pin, and turn the handle until the pin pushes out.
- **Assembling Chain (Master-less):** Leave the pin held partially in the outer plate! Slip the new link into place and use the reverse press on the tool to push the pin back flush.

### Chain Tensioning Rules
1. **Chain stretches over time:** Brand-new #25 chain will elongate slightly during the first few matches ("run-in").
2. **Always incorporate tension adjustment:** Never fix both shaft bearings at completely stationary points without a mechanism to tension the chain. Use:
   - Sliding bearing brackets along 15mm extrusion slots.
   - Half-links ([REV-41-1366](https://www.revrobotics.com/rev-41-1366/)) to achieve precise link counts.
   - Floating Delrin chain idler blocks or floating sprockets.
3. **Target Sag:** A properly tensioned FTC chain run should have approximately **¼ inch (6mm)** of vertical flex under moderate finger pressure. Overtightened chain causes excessive motor friction; loose chain will jump teeth and derail.

---

## 3. GT2 Timing Belts & Pulleys

Timing belts offer zero backlash, require zero lubrication, and run near-silently at high RPM.

### Specifications
- **Belt Standard:** **GT2 (3mm pitch)**, 9mm width neoprene with fiberglass reinforcement.
- **Pulleys:** 15T, 24T, 30T, and 36T with 5mm hex bore and side retention flanges.

### When to Choose Belts over Chain:
- **Flywheel Shooters:** At 2,000–6,000 RPM, roller chain will oscillate violently and throw lubricant. Belts run smoothly and stably at high speeds.
- **High-Speed Intakes:** Belts absorb impact when colliding with game elements without snapping.
- **Precision Autonomous Encoders:** Belts have virtually zero mechanical backlash compared to chain slop.

---

## 4. Wheel Selection & Ground Interaction

REV DUO offers four wheel families: **Traction/Grip**, **Omni-Directional**, **Mecanum**, and **Compliant**.

```
  [Traction]             [Omni]                 [Mecanum]             [Compliant]
 (Solid Tread)      (Perpendicular Rollers)  (45° Angled Rollers)    (Flexible Urethane)
  High Grip            Zero Scrub Turns       Holonomic / Strafe      Intake Compression
```

### Wheel Comparison Matrix

| Wheel Type | Available Sizes | Main Benefit | Main Trade-off | Ideal Use Case |
|---|---|---|---|---|
| **Traction / Grip** ([REV-41-1354](https://www.revrobotics.com/rev-41-1354/)) | 75mm, 90mm | Maximum pushing force, zero slip | Cannot slide sideways | Center-drop tank drive, defense bots |
| **Omni-Directional** ([REV-41-1190](https://www.revrobotics.com/rev-41-1190/)) | 75mm, 90mm | Eliminates turning scrub | No lateral resistance (pushed easily) | Outer wheels on tank drive, odometry pods |
| **Mecanum** ([REV-45-1655](https://www.revrobotics.com/rev-45-1655/)) | 75mm, 90mm | Full 360° translation (strafing) | Reduced forward tractive force | General FTC competition holonomic drive |
| **Compliant** | 35mm, 50mm, 75mm, 100mm | Deforms around game objects | Not suitable for driving on the floor | Roller intakes, ball collectors, ramps |

---

### The Mecanum Wheel "O-Pattern" Rule

> [!IMPORTANT]
> **MECANUM ROLLER ORIENTATION:**  
> Mecanum wheels come in distinct **Left** and **Right** configurations based on the 45-degree angle of the perimeter rollers.
> 
> When viewed **from above looking down on the robot**:
> - The contact rollers touching the carpet must form an **"X"** shape.
> - The top-facing rollers must form an **"O"** or diamond pointing outwards toward the corners.
> 
> ```
>   Front Left  [/]     [\]  Front Right
>                \       /
>                 \     /
>                  \   /
>                   \ /
>                    X
>                   / \
>                  /   \
>                 /     \
>                /       \
>   Back Left   [\]     [/]  Back Right
> ```
> 
> If two wheels on the same side are swapped, the robot will drive diagonally when commanded forward or spin uncontrollably when trying to strafe!

---

### Compliant Wheel Durometer Guide
Compliant wheels are made of flexible thermoplastic polyurethane (TPU) and deform around game pieces to create high friction:

- **30A (Green — Soft):** Extremely squishy. Best for hard, slick plastic spheres or irregularly shaped game elements. Conforms immediately to game piece geometry.
- **45A (Grey/Black — Medium):** General-purpose intake roller. Good balance of grip and mechanical durability.
- **60A (Orange — Firm):** Stiff. Best for high-wear feeder wheels and heavy game pieces where the wheel must maintain its circular geometry under speed.

---

## Official Links & References
- [Introduction to Motion (Docs)](https://docs.revrobotics.com/duo-build/motion/intro.md)
- [Gears and Gear Math](https://docs.revrobotics.com/duo-build/motion/gears.md)
- [Advanced Gears & Addendum Shifting](https://docs.revrobotics.com/duo-build/motion/gears/gears-advanced.md)
- [Sprockets and Chain Guide](https://docs.revrobotics.com/duo-build/motion/sprockets-and-chain.md)
- [Chain Tool Instructions](https://docs.revrobotics.com/duo-build/motion/sprockets-and-chain/chain-tool.md)
- [Timing Belts and Pulleys](https://docs.revrobotics.com/duo-build/motion/timing-belts-and-pulleys.md)
- [REV Wheel System Overview](https://docs.revrobotics.com/duo-build/motion/wheels.md)
- [Mecanum Wheel Behavior & Setup Guide](https://docs.revrobotics.com/duo-build/mecanum-drivetrain-v2/mecanum-wheel-setup-and-behavior.md)

