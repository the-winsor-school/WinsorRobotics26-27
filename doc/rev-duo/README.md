# REV DUO Hardware Guide & Resource Index

> **Winsor Robotics FTC Resource Directory**  
> Focus: REV Robotics DUO Build System (15mm Architecture)

Welcome to the Winsor Robotics **REV DUO Resource Index & Hardware Design Guide**. Winsor Robotics exclusively uses the REV Robotics DUO ecosystem for mechanical fabrication and FTC robot design. 

This guide serves as a comprehensive manual for team members to explore available hardware, make informed engineering design choices, assemble reliable mechanisms, and link physical components directly to our Java software architecture.

---

## Guide Directory

| Document | Description | Key Topics |
|---|---|---|
| [1. Structure & Fasteners](structure-and-fasteners.md) | The physical foundation of the robot | 15mm Extrusion, C/U Channels, Extended Motion Pattern, 5mm Hex Shafts, M3 hardware, "Studs Up", alignment ribs |
| [2. Actuators & Gearboxes](actuators-and-gearboxes.md) | Generating rotational mechanical power | HD Hex Motor, Core Hex Motor, Smart Robot Servo (SRS), UltraPlanetary Gearbox, cartridge math & load rules |
| [3. Power Transmission & Wheels](power-transmission-and-motion.md) | Transferring power across mechanisms | 0.5 Mod Gears (15mm grid math), #25 Chain & Sprockets, GT2 Timing Belts, Mecanum/Omni/Compliant wheels |
| [4. Linear Motion & Lifts](linear-motion.md) | Elevators and extending mechanisms | Linear slide kits, Cascading vs. Continuous rigging, spring returns, string routing |
| [5. Control System & Sensors](control-and-sensors.md) | Electronics, wiring, and feedback | Control Hub, Expansion Hub, Driver Hub, 2m Distance, Color Sensor v3, Magnetic Limits, Through Bore Encoders |
| [6. Hardware Design Decision Guide](design-decision-guide.md) | Engineering calculations & trade-offs | Motor vs. Servo selection, torque/speed calculations, drivetrain sizing, intake geometry |
| [7. Kits & Starter Bots Index](kits-and-starter-bots.md) | Official build manuals & kickoff designs | Drivetrain kits, Class Bot, FTC season starter bots (DECODE, INTO THE DEEP, CENTERSTAGE) |

---

## The 6 "Golden Rules" of REV DUO Building

Before turning a single wrench, every Winsor student builder must know these core physical principles:

1. **"Snug, Not Stuck!"**  
   Aluminum and Delrin threads can strip under excessive force. Tighten M3 screws until they are firmly seated; never force a screw until it refuses to turn. Use nyloc nuts or Blue Loctite on mechanisms subject to vibration.
2. **Build "Studs Up" (Screw Heads in the Extrusion)**  
   When fastening brackets to 15mm extrusion, insert the M3 screw head inside the extrusion slot and face the threaded stud outward through the bracket. The bracket is then secured with an M3 nut from the outside.
3. **Bracket Alignment Ribs Face the Extrusion Slot**  
   REV plastic and stamped metal brackets feature small molded alignment ribs. These ribs **must face into the extrusion slot** to self-align and square the bracket at 90° or 45°.
4. **Nyloc Bevel Faces Outward**  
   The beveled nylon locking collar of an M3 nyloc nut must always face outward away from the bracket. The flat metallic side must press flush against the bracket or plate.
5. **Highest Reduction Closest to the Motor (UltraPlanetary)**  
   When stacking UltraPlanetary gearbox cartridges (e.g., 5:1, 4:1, 3:1), always place the highest ratio cartridge closest to the motor face. The output stages carry much higher torque; placing smaller teeth at the output risks catastrophic gear shearing under stall or shock loads.
6. **Constrain All Shafts Against Axial Slop**  
   The 5mm hex shaft eliminates set-screw slip on rotational axes, but shafts will slide out sideways unless constrained. Always trap shafts using flanged bearings (flange on the outside), shaft collars, or Delrin spacers.

---

## Quick Reference Links

### Official Documentation & Portals
- [REV Robotics Official Documentation](https://docs.revrobotics.com)
- [REV DUO Build System Documentation](https://docs.revrobotics.com/duo-build/home/master.md)
- [REV DUO Control System Documentation](https://docs.revrobotics.com/duo-control/menu/master.md)
- [REV Hardware Client Download & Manual](https://docs.revrobotics.com/rev-hardware-client/home/rev-hardware-client-overview.md)
- [REV DUO Product Store Catalog (FTC)](https://www.revrobotics.com/competition/ftc/)

### CAD & Modeling Resources
- [REV Onshape Parts Library & FeatureScript](https://docs.revrobotics.com/duo-build/building/compatibility.md)
- [REV DUO Onshape CAD Model Portal](https://www.revrobotics.com/competition/ftc/cad/)
- Most product pages on `revrobotics.com` have downloadable STEP and CAD files on their "CAD & Docs" tab.

### Internal Codebase Connections
In the Winsor Robotics software model, hardware is wrapped strictly into object-oriented layers:
- Review [02-ftc-hardware-toolbox.md](../FlintLessons/GettingStarted/02-ftc-hardware-toolbox.md) for how the FTC SDK wraps REV motors, servos, and sensors.
- Review [NewRobotDesignWorkflow.md](../NewRobotDesignWorkflow.md) for how mechanisms are decomposed into `MechComponent` and `MechAssembly` classes.
- Review [TelemetryContract.md](../TelemetryContract.md) for sensor reporting rules.

