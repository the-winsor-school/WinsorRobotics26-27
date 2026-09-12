# REV DUO Control System & Sensors Guide

> **Winsor Robotics FTC Resource Directory**  
> Component: Control Hub, Expansion Hub, Wiring Standards, and Sensors

The control system forms the brain and nervous system of the robot. The REV DUO electronics ecosystem consolidates motor driving, servo power, computing, Wi-Fi telemetry, and sensor monitoring into robust, competition-hardened modules.

---

## 1. Hub Ecosystem Overview

| Device | Primary Function | Ports & Interfaces |
|---|---|---|
| **Control Hub** ([REV-31-1595](https://www.revrobotics.com/rev-31-1595/)) | On-robot computer + primary motor/servo controller | 4 DC Motors, 6 Servos, 4 I2C buses, 4 Digital (8 channels), 4 Analog, Internal IMU, USB-A, USB-C, Wi-Fi |
| **Expansion Hub** ([REV-31-1153](https://www.revrobotics.com/rev-31-1153/)) | Secondary motor/servo controller (connects via RS485) | 4 DC Motors, 6 Servos, 4 I2C, 4 Digital, 4 Analog |
| **Driver Hub** ([REV-31-1596](https://www.revrobotics.com/rev-31-1596/)) | Dedicated handheld driver station device | Dual USB-A for gamepads, 5.5" touchscreen, 2.4/5GHz Wi-Fi, USB-C charging |

```
 [Driver Hub] <--(5 GHz Wi-Fi)--> [Control Hub] <--(RS485 + XT30)--> [Expansion Hub]
  (Gamepads)                       |                                  |
                                   +--> Motors 0-3 / Servos 0-5        +--> Motors 4-7 / Servos 6-11
                                   +--> Limelight 3A (USB)             +--> Auxiliary Sensors
```

---

## 2. Cables, Connectors & Wiring Best Practices

Loose wires cause intermittent disconnects during high-impact matches. REV uses keyed, locking connectors:

- **XT30 (Power Bus):** Yellow polarized connectors rated for 30A continuous. Used for the 12V battery, power switch, and daisy-chaining 12V power to the Expansion Hub.
- **JST VH (Motor Power):** 2-pin locking white connectors with thick gauge silicone wire. Ensure the locking latch clicks fully into place.
- **JST PH (Sensors & Encoders):** Compact 4-pin (I2C, quadrature encoders) and 3-pin (analog, digital) connectors with 2.0mm pin pitch.

### Wiring Rules for Winsor Builders:
1. **Never yank on wires to unplug:** Always pinch the connector latch with your fingernails or small pliers. Pulling the wire pulls crimp pins out of the connector housing.
2. **Strain relief everywhere:** Secure wires to 15mm extrusion slots using zip-ties or REV Wire Clips ([REV-41-1156](https://www.revrobotics.com/rev-41-1156/)). Every moving joint must have an intentional service loop to prevent wires fatigue-breaking during mechanism extension.
3. **Keep motor wires away from I2C cables:** High current pulses to drive motors create electromagnetic interference (EMI) that can crash unshielded I2C sensor communication. Route motor leads on the opposite side of the frame from sensor wires.

---

## 3. REV Hardware Client

The **[REV Hardware Client](https://docs.revrobotics.com/rev-hardware-client/home/rev-hardware-client-overview.md)** is a free PC application for managing, testing, and flashing REV devices over USB-C or Wi-Fi:

- **One-Click Updates:** Automatically downloads and updates the Control Hub OS, Hub firmware, and FTC Robot Controller / Driver Station apps.
- **Motor & Servo Testbed (No Code Required):** Allows students to plug in a motor or servo and run it at variable speeds directly from the PC to test hardware before any robot code is compiled.
- **Wi-Fi Configuration:** Reset network names (SSID), change Wi-Fi passwords, or switch between 2.4GHz and 5.0GHz channels to avoid competition venue interference.

---

## 4. Sensor Catalog & Usage Guide

### A. REV 2m Distance Sensor ([REV-31-1505](https://www.revrobotics.com/rev-31-1505/))
- **Technology:** STMicroelectronics FlightSense Time-of-Flight (ToF) laser ranging.
- **Range:** 10 mm to 2000 mm (2 meters).
- **Interface:** I2C bus.
- **Best For:** Wall distance detection during autonomous alignment, detecting game elements inside an intake hopper.
- *SDK Type:* `DistanceSensor` (`getDistance(DistanceUnit.CM)`).

### B. REV Color Sensor v3 ([REV-31-1557](https://www.revrobotics.com/rev-31-1557/))
- **Technology:** Broadcom APDS-9151 RGB color sensor + IR proximity sensor.
- **Interface:** I2C bus.
- **Best For:** Detecting game piece colors (e.g. distinguishing yellow samples vs. blue/red samples), proximity sensing when an object enters a claw.
- *SDK Type:* `NormalizedColorSensor` (use `getNormalizedColors()` and convert to HSV values for robust lighting invariance).

### C. REV Magnetic Limit Switch ([REV-31-1462](https://www.revrobotics.com/rev-31-1462/))
- **Technology:** Omnipolar Hall effect sensor.
- **Interface:** Digital (active-low signal when magnet is present).
- **Features:** Built-in blue LED lights up when magnet is within 10mm range.
- **Best For:** Non-contact home positioning on linear lifts, elevator stage stops, arm angle limits (will never break on physical collision).

### D. REV Touch Sensor ([REV-31-1425](https://www.revrobotics.com/rev-31-1425/))
- **Technology:** Momentary push-button switch in a bracket housing.
- **Interface:** Digital channel.
- *SDK Type:* `TouchSensor` (`isPressed()`).

### E. REV Through Bore Encoder ([REV-11-1271](https://www.revrobotics.com/rev-11-1271/))
- **Resolution:** **8,192 counts per revolution** (quadrature mode).
- **Interface:** Dual output — Incremental Quadrature (plugs into motor encoder port) + Absolute PWM (plugs into digital port).
- **Bore:** 1/2" hex through bore, with included adapter insert for 5mm hex shafts.
- **Best For:** Dead-wheel odometry pods, tracking exact angle on arm shoulder joints without drift.

### F. Control Hub Internal IMU
- **Function:** 6-axis/9-axis gyroscope and accelerometer integrated directly on the Control Hub PCB.
- **Usage:** Provides heading (Yaw angle in degrees) for field-centric driving and autonomous turning routines.
- *Configuration Rule:* In your `Robot` class, always initialize orientation using `RevHubOrientationOnRobot` to specify which direction the Control Hub's logo and USB ports are facing relative to the robot chassis!

---

## 5. Software Mapping in TeamCode

Review how each REV sensor connects to Java in [02-ftc-hardware-toolbox.md](../FlintLessons/GettingStarted/02-ftc-hardware-toolbox.md):

```java
// Sensor initialization examples:
DistanceSensor distance = hardwareMap.get(DistanceSensor.class, "frontDistance");
NormalizedColorSensor color = hardwareMap.get(NormalizedColorSensor.class, "intakeColor");
TouchSensor limit = hardwareMap.get(TouchSensor.class, "slideBottomLimit");
```

---

## Official Links & References
- [REV DUO Control System Overview](https://docs.revrobotics.com/duo-control/menu/master.md)
- [Control Hub Specifications & Pinouts](https://docs.revrobotics.com/duo-control/control-system-overview/control-hub-basics.md)
- [Expansion Hub Specifications](https://docs.revrobotics.com/duo-control/control-system-overview/expansion-hub-basics.md)
- [Driver Hub Specifications](https://docs.revrobotics.com/duo-control/control-system-overview/driver-hub-specifications.md)
- [REV Hardware Client Manual](https://docs.revrobotics.com/rev-hardware-client/home/rev-hardware-client-overview.md)
- [Color Sensor V3 Guide](https://docs.revrobotics.com/rev-crossover-products/sensors/color-sensor.md)
- [2m Distance Sensor Guide](https://docs.revrobotics.com/rev-crossover-products/sensors/2m-distance.md)
- [Magnetic Limit Switch Guide](https://docs.revrobotics.com/rev-crossover-products/sensors/magnetic-limit-switch.md)
- [Through Bore Encoder Guide](https://docs.revrobotics.com/rev-crossover-products/sensors/tbe.md)

