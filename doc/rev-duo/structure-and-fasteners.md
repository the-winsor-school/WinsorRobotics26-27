# REV DUO Structure & Fasteners Guide

> **Winsor Robotics FTC Resource Directory**  
> Component: Structural Elements, Fasteners, and Shaft Standards

The REV DUO build system is centered on a **15mm metric structural grid** and a **5mm hex shaft standard**. Understanding how these components interconnect and how to properly assemble them is essential to building an FTC chassis and superstructure that remains rigid, square, and vibration-proof throughout an entire competition season.

---

## 1. Extrusion vs. Channel: When to Use Which?

The DUO ecosystem provides two distinct structural philosophies: **Continuous Extrusion** and **Fixed-Pitch Channel**.

| Feature | 15mm Extrusion | C-Channel & U-Channel |
|---|---|---|
| **Cross-Section** | 15mm × 15mm or 15mm × 30mm | 15mm × 45mm (C) or 45mm × 45mm (U) |
| **Mounting Style** | Continuous slot (infinitely adjustable) | Extended Motion Pattern (fixed 8mm/16mm pitch) |
| **Bearing Support** | Requires end-bearing plates or pillow blocks | Built-in 9mm bearing seats every 16mm |
| **Torsional Rigidity** | Moderate (good for uprights, arms, framing) | Extreme (ideal for drivetrains and lift towers) |
| **Best Used For** | Prototyping, sensor mounts, custom angles, intake arms | Drivetrain chassis rails, elevator towers, enclosed gearboxes |

### 15mm Extrusion ([REV-41-1432](https://www.revrobotics.com/rev-41-1432/))
- Features four continuous T-slots compatible with M3 hex cap screw heads.
- **Center Hole Tapping:** The round center hole through the extrusion is sized specifically for tapping with a standard **M3 × 0.5mm tap**. Tapping the end allows direct face-mounting into plates without bulky exterior brackets.
- Available in standard lengths (150mm, 225mm, 420mm) or can be cut down to any custom dimension.

### 15mm × 30mm Extrusion ([REV-41-1433](https://www.revrobotics.com/rev-41-1433/))
- Provides double the cross-sectional area and significantly higher bending resistance along the 30mm axis.
- Perfect for cantilevered mechanism arms and heavy elevator mast uprights.

### C-Channel ([REV-41-1376](https://www.revrobotics.com/rev-41-1376/)) & U-Channel ([REV-41-1377](https://www.revrobotics.com/rev-41-1377/))
- Features the **Extended Motion Pattern** on the web and flanges, with integrated continuous slots on outer walls.
- Channels allow motors, sprockets, and chains to be enclosed securely inside the channel profile, protecting cables and mechanisms from field collisions.

---

## 2. The Extended Motion Pattern

The Extended Motion Pattern is REV's standard geometry for channels, flat plates, and motion brackets:

```
      ( ) 9mm Bearing Seat (every 16mm)
     /   \
  (o)     (o)  8mm pitch equilateral triangle grid
   |       |
  (o)=====(o)  16mm and 32mm diameter circular bolt circles
```

### Key Dimensions
- **8mm Linear Pitch:** M3 clearance holes spaced exactly 8mm apart down the center line.
- **16mm & 32mm Bolt Circles:** Concentric circles of 4 holes for mounting motor faceplates, bearing pillows, and brackets.
- **9mm Bearing Seat:** A 9mm diameter hole centered every 16mm. These seats accept REV Flanged Ball Bearings ([REV-41-1329](https://www.revrobotics.com/rev-41-1329/)) and bronze bushings directly without needing extra adapters.

---

## 3. Fastener Rules & Assembly Techniques

Fastening mistakes are the #1 cause of mechanical failures during FTC matches. Follow these techniques strictly:

### A. The "Studs Up" Rule
When fastening any bracket, plate, or sensor to 15mm extrusion:
1. Slide the **head** of an M3 hex cap screw directly into the extrusion slot.
2. The threaded shaft ("stud") points outward.
3. Seat the bracket over the protruding stud.
4. Thread an M3 nut onto the screw from the outside.
> **Why?** M3 screw heads fit smoothly inside the extrusion slot and cannot rotate when the nut is tightened. Reversing this by sticking screws in from the outside causes fastener heads to bind or strip the slot edges.

### B. Pre-Loading Brackets
Instead of fumbling with loose screws in the extrusion slot:
1. Drop the screws into the holes on the bracket first.
2. Loosely thread the M3 nuts onto the screws (1-2 turns).
3. Align the square nut heads and slide the entire pre-assembled unit into the extrusion slot as a single assembly.
4. Slide to final position and torque down snugly.

### C. Bracket Alignment Ribs
All REV plastic motion brackets and many stamped aluminum brackets have small molded ridges (ribs) on one face:
- **Always face the ribs towards the extrusion slot.**
- The ribs register inside the extrusion gap, forcing the bracket into a true 90-degree square or 45-degree angle automatically.
- Reversing the bracket so the ribs face outward will tilt the bracket and prevent flush mounting!

### D. Nyloc Nut Orientation
- M3 Nyloc Nuts ([REV-41-1361](https://www.revrobotics.com/rev-41-1361/)) contain a nylon ring that deforms around screw threads to resist vibration.
- The **beveled side** containing the colored nylon ring **must face outwards**.
- The flat metal side must seat firmly against the bracket or plate.

### E. Drop-In T-Nuts ([REV-41-1493](https://www.revrobotics.com/rev-41-1493/))
- If a robot chassis is already assembled and you need to add a bracket in the middle of a locked extrusion, do not disassemble the entire frame!
- Use **Drop-In T-Nuts**. These drop straight into any slot from above and rotate 90° to lock in place when the screw is tightened.

---

## 4. The 5mm Hex Shaft Standard

REV DUO uses **5mm Hex SUS303 Stainless Steel Shafts** across all motion components.

### Why 5mm Hex?
- **Zero Set-Screw Slip:** Traditional round shafts with set screws slip under torque when motors stall. Hex shafts transfer pure positive torque through 6 flat faces.
- **Interchangeability:** Gears, sprockets, pulleys, compliant wheels, and encoders all share the identical 5mm hex bore.

### Available Stock Lengths
- **75mm** ([REV-41-1347](https://www.revrobotics.com/rev-41-1347/))
- **90mm** ([REV-41-1348](https://www.revrobotics.com/rev-41-1348/))
- **135mm** ([REV-41-1349](https://www.revrobotics.com/rev-41-1349/))
- **400mm** ([REV-41-1362](https://www.revrobotics.com/rev-41-1362/)) — cut down to custom length as needed.

### Constraining Shafts (Eliminating Axial Play)
While hex shafts cannot slip in rotation, they will freely slide laterally out of bearings unless constrained. Use one of these three methods:

1. **Delrin Spacers ([REV-41-1323](https://www.revrobotics.com/rev-41-1323/)):**
   - Available in 1.5mm, 3mm, and 6mm lengths with 5mm hex ID.
   - Use for short spans between bearings, sprockets, or gears.
2. **Shaft Collars ([REV-41-1327](https://www.revrobotics.com/rev-41-1327/)):**
   - Metal collar with an M3 set screw.
   - Tip: Tighten with a 1.5mm Allen wrench onto a **flat face** of the hex shaft, never onto a corner!
   - Apply Blue Loctite to prevent loosening.
3. **Locking Motion Hub ([REV-41-1719](https://www.revrobotics.com/rev-41-1719/)):**
   - Combines a clamping shaft collar with a 16mm motion bolt circle.
   - Ideal for locking a metal sprocket or high-torque wheel to the shaft without needing a separate collar.
4. **High Strength Hex Hub ([REV-41-1147](https://www.revrobotics.com/rev-41-1147/)):**
   - Clamps onto plastic gears or sprockets to prevent the plastic hex bore from rounding out under extreme loads.

---

## 5. Cutting and Modifying Structure

When building custom mechanisms, you will need to cut extrusion or shafts down to custom lengths.

### Safe Cutting Procedures
- **Tools:** Use a horizontal bandsaw with vise clamps, or a handheld hacksaw paired with a miter box.
- **Marking:** Wrap painter's tape around the extrusion at the target dimension and mark a crisp line with an ultra-fine Sharpie. Cut along the inside of the line.
- **Deburring:** Always deburr freshly cut ends using a flat metal file. Aluminum burrs inside the T-slot will jam screw heads and cut wiring insulation.

> [!CAUTION]
> **NEVER USE A CHOP SAW OR POWERED MITER SAW ON 15MM EXTRUSION!**  
> High-speed wood-blade chop saws will grab the light aluminum extrusion profile, violently kicking it out of the vise. This will destroy the piece, damage the blade, and cause severe injury. Always use a bandsaw or hacksaw.

---

## Official Links & References
- [REV DUO Structure Overview (Docs)](https://docs.revrobotics.com/duo-build/structure/intro.md)
- [15mm Extrusion Specification](https://docs.revrobotics.com/duo-build/structure/intro/15mm-extrusion.md)
- [C-Channel Specification](https://docs.revrobotics.com/duo-build/structure/intro/15mm-x-45mm-c-channel.md)
- [U-Channel Specification](https://docs.revrobotics.com/duo-build/structure/intro/45mm-x-45mm-u-channel.md)
- [M3 Hardware Guide](https://docs.revrobotics.com/duo-build/structure/m3-hardware.md)
- [Shafts and Spacers Guide](https://docs.revrobotics.com/duo-build/motion/intro/shaft.md)

