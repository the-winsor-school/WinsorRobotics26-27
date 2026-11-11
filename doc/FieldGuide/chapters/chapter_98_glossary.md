# Glossary

*Every term the book defines, and the entry that explains it.*

---

Each term below is defined where the book first explains it, and the numbers
after it name those entries.  Where one word means two different things, like
*bearing* and *tick*, each meaning gets its own line.

A term in `code font` is a name from the code or the SDK, spelled the way
Java spells it.

## A

**abort** -- stop a state machine on the spot and leave it idle.  On Billy, `abort()` empties `currentState` and `elapsedTime`.  (12.8)

**abstract** -- describes a class that exists to be extended, or a method listed with no body that every subclass has to supply.  `MechComponent` is abstract.  (1.6)

**access modifier** -- `public`, `private`, or `protected`:  the word in front of a field, method, or class that says which code may use it.  (1.3)

**active retraction** -- driving a lift down with the motor, on a second cord.  (19.4)

**actuator** -- a device that takes a command and moves:  a motor or a servo.  (4.1, 17.1)

**aggregate autonomous object** -- an assembly's autonomous object, built from its components' autonomous objects.  `AutonomousBillyMA` is one.  (9.5)

**alignment ribs** -- small molded ridges on one face of a REV bracket.  They fit into the extrusion's slot and square the bracket at 90° or 45°.  (16.5)

**annotation** -- a note attached to code with `@`, for the compiler or the SDK to read.  `@Override` is one.  (1.6)

**API** -- the operations an object offers its callers.  (Chapter 11)

**AprilTag** -- a printed black-and-white square that carries one number, its tag ID, and can be read at speed.  (5.1)

**arbitration** -- deciding which behavior may control a resource when requests compete.  (13.1, 13.7)

**argument** -- a value handed to a method or constructor when it runs.  (1.4)

**assembly** -- a class that owns several components and coordinates them as one subsystem.  Every assembly extends `MechAssembly`.  (9.1)

**assist strategy** -- a behavior that helps with part of the robot while other control continues.  (13.3)

**autonomous** -- the opening period of a match, when the robot runs on its own, from a program.  Also the code written for it.  (6.3)

**autonomous verb** -- a method through which a routine requests a named action.  (Chapter 11)

**axial play** -- a shaft sliding along its own length.  Constraining a shaft removes it.  (16.7)

## B

**backlash** -- input movement during a reversal before the output responds.  (18.5)

**baseline** -- the version you measure your own ideas against.  A starter bot is one.  (20.3)

**beam-break** -- a sensor that shines light across a gap at a detector, so anything in the gap blocks it.  (4.7)

**bearing, a direction** -- the direction from you to something.  `tx` is one.  (14.1)

**bearing, a part** -- a ring a shaft spins inside, smoothly, which keeps the shaft centered in its hole.  (16.2)

**belt** -- a flexible loop carrying motion between pulleys.  A timing belt has teeth that engage matching pulleys.  (18.5)

**blocking** -- a call that makes the next line wait until it's done.  `Thread.sleep` is one.  (12.1)

**bolt circle** -- four holes on a circle, 16mm or 32mm across, where motor faceplates and bearing parts bolt on.  (16.3)

**botpose** -- the robot's field pose, worked out by the Limelight from the tags it sees.  (14.4)

**bottom-up building** -- writing the smallest classes first:  components, then assemblies, then the robot, then OpModes and autonomous strategies.  (21.1)

**brownout** -- a voltage drop far enough to disrupt operation.  (17.7)

**bug shape** -- a pattern of mistake that turns up again and again, in code that's otherwise unrelated.  (22.1)

**build order** -- the fixed order the files of a new robot are written in, following what depends on what.  (21.3)

**builder** -- an object whose only job is to put another object together, a piece at a time, and hand it over at the end.  The `RobotBuilder` in `Robot.java`'s `TODO` would be one.  (21.10)

**burr** -- a sharp curl of metal left along a fresh cut.  Filing it off is deburring.  (16.8)

## C

**camera pose** -- where the camera sits on the robot, measured from the robot's center, and which way it points.  (14.3)

**cantilevered shaft** -- a shaft held at one end only.  (20.2)

**capabilities flow up** -- each layer gathers the autonomous verbs of the layer below into an object of its own, so autonomous code can reach any mechanism from the top.  (6.5)

**cascading rigging** -- rigging where every stage has its own cord loop and all the stages extend at once.  (19.2)

**cast** -- a type in parentheses in front of a value, telling Java to treat the value as that type.  Java checks it when the line runs.  (7.2)

**center distance** -- distance between two shaft centers.  (18.1)

**chain** -- a loop of connected links engaging sprockets to carry motion between shafts.  (18.3)

**chaining** -- using a dot on whatever the step before handed back, as in `imu.getRobotYawPitchRollAngles().getYaw()`.  (1.4)

**channel** -- aluminum shaped into a long box with one side open, with the Extended Motion Pattern in its walls.  C-channel is 15mm × 45mm, and U-channel is 45mm × 45mm.  (16.2)

**checklist** -- what an assembly's `updateTelemetry` is:  one visit to every component it owns, every loop.  (9.6)

**claim** -- a declaration of the resources an active behavior needs.  (13.4)

**class** -- a description of one kind of thing:  what it knows and what it can do.  (1.1)

**Class Bot** -- a REV kit robot built to teach the basics:  frame squaring, wheel hubs, and arm linkages.  (20.7)

**closure** -- a lambda that keeps the variables from the code around it and still has them when it runs later.  (12.4)

**cluster** -- a group of AprilTags on one flat surface, each at a known offset from a shared origin.  (15.2)

**cluster origin** -- the chosen point a cluster's offsets are measured from.  In BIOBUZZ, the center of the CELL opening.  (15.2)

**comment** -- text in the code written for people, which Java skips.  (1.10)

**compliant wheel** -- a flexible wheel that compresses around a game piece.  (18.8)

**component** -- a class that owns one mechanism's hardware and drives it.  Every one extends `MechComponent`.  (7.1)

**component contract** -- what `MechComponent` requires of every component:  take a strategy, `move(Gamepad)`, `update()`, and `getAutonomousBehaviors()`.  (7.1)

**composite** -- one object that stands for a group, so the code above it can treat the group as a single thing.  The **Composite pattern** is the design built on it.  (9.1)

**composition** -- building an object out of other objects held in its fields.  `BillyRobot` is composed of a `MecanumDrive` and a `BillyMA`.  (6.7)

**condition** -- the part in parentheses after `if`, which works out to `true` or `false`.  (1.8)

**configuration name** -- the name typed for a device in the robot's configuration on the Driver Station, which the code uses to find it.  `"intakeMotor"` is one.  (1.5)

**constant** -- a field that's `static` and `final`, so there's one fixed copy for the whole program.  `DeadZoneRadius` is one.  (2.2)

**constant-force spring** -- a rolled strip of spring steel that pulls with the same force however far it's unrolled.  (19.5)

**constructor** -- the code that runs once when an object is made, to get it ready.  It has the class's name and no return type.  (1.5)

**continuous rigging** -- rigging where one cord runs through the whole lift and the stages extend one after another, bottom to top.  (19.2)

**Control Hub** -- the robot's computer and main motor and servo controller.  (19.8)

**control strategy** -- a supplied rule that translates inputs into commands.  (Chapter 8)

**Core Hex Motor** -- a compact motor with a fixed gearbox and a through hex socket, rated at 125 RPM free output speed.  (17.1)

**counterbalancing** -- offsetting an arm's or a carriage's weight with a spring or a weight.  (20.7)

**CRServo** -- a servo rebuilt for continuous rotation.  It takes `setPower` and has no sense of position.  (3.4)

## D

**data association** -- matching what a sensor sees to the landmark it belongs to.  A tag's ID makes it a table lookup.  (14.8)

**DcMotor** -- the SDK's type for an ordinary motor, run with `setPower` from `-1.0` to `1.0`.  (3.2)

**DcMotorEx** -- a `DcMotor` with extra methods, including `getVelocity()` and `setVelocity(...)`.  (3.3)

**deadband** -- a band around a target that counts as "there," like `-2` to `2` degrees in the turret tracker.  (4.3)

**deadzone** -- a deadband for a joystick:  readings closer to zero than `0.05` count as exactly zero.  (2.5, 4.3)

**decomposing a design** -- splitting a finished robot into its mechanisms and asking of each one how it solved its one problem.  (20.4)

**decorator** -- an object that adds behavior around an operation it delegates.  (Chapter 8)

**default strategy** -- the normal behavior available for parts that a special job hasn't claimed.  (13.3)

**dependency injection** -- handing an object the things it depends on through its constructor, so the code that builds it decides what they are.  (21.5)

**design checklist** -- eight questions a finished robot design should answer yes.  (21.7)

**DigitalChannel** -- a raw on-or-off wire on a digital port.  Read as a button, `getState()` is `false` while it's pressed.  (3.5)

**disjoint sets** -- sets with no shared values.  (13.4)

**DistanceSensor** -- the SDK's general type for a sensor that measures distance.  (3.6)

**doAndWait** -- `StateMachine`'s helper for a timed step:  do an action once, wait some milliseconds a tick at a time, then move on.  (12.4)

**dot-chain** -- a sequence of member selections and method calls used to reach an object or operation.  (Chapter 11)

**driven gear** -- the gear receiving power from the driver.  (18.2)

**driver** -- the gear or sprocket receiving power from the motor.  (18.2)

**Driver Hub** -- the handheld driver station.  The gamepads plug into it.  (19.8)

**Driver Station configuration** -- the list, saved on the robot, that names every device on every port.  (3.1)

**DriveTrain** -- the layer that moves the chassis, turning sticks into wheel power.  `MecanumDrive` is one.  (6.1)

**Drop-In T-Nut** -- a nut that drops into an extrusion slot from above and turns 90° to lock, for adding a bracket to a finished frame.  (16.4)

**durometer** -- a hardness rating;  within this wheel family, a higher A number means a firmer wheel.  (18.8)

## E

**encoder** -- a sensor on a motor that counts how far its shaft has turned.  (3.2)

**Expansion Hub** -- a second motor and servo controller, joined to the Control Hub by an RS485 data cable and an XT30 power cable.  (19.8)

**Extended Motion Pattern** -- REV's hole pattern for channel, plates, and motion brackets:  M3 holes at 8mm, bolt circles of 16mm and 32mm, and a 9mm bearing seat every 16mm.  (16.3)

**extension method** -- in this codebase, a `static` method in a class named `SomethingExtensions` that takes the type it helps as its first parameter.  `GamepadExtensions.GetLeftStickX(gamepad)` is one.  (2.1)

**extrusion** -- a long, square aluminum bar with a T-slot down each side, for bolting things on at any spot.  (16.2)

## F

**Facade** -- an object that presents a small interface over several objects working together.  The robot presents `update`, `updateTelemetry`, and its autonomous object to the OpMode.  (10.7)

**fiducial** -- a marker placed so a camera can find it and know which one it is.  An AprilTag is one.  (2.7, 5.1)

**fiducial-based localization** -- finding your own pose from markers whose positions are known.  (14.9)

**field** -- a name that belongs to an object and lasts as long as the object does.  (1.1)

**field coordinates** -- the fixed axes FTC lays over the field, so every spot has an x, a y and a z.  (14.2)

**final** -- describes a name that gets its value once and keeps it.  (1.3)

**five categories** -- the five bug shapes in Chapter 22:  the stopped loop, two conventions, one handle, independent guards, and the SDK's silence.  (22.1)

**fluent interface** -- methods that each hand back the object they were called on, so calls chain with dots.  (21.10)

**free speed** -- rotational speed with essentially no useful load.  (17.5)

**`ftcPose`** -- the webcam route's answer for a tag:  its position compared with the camera, including `range`, `bearing`, and `yaw`.  (5.10)

**functional interface** -- an interface with one abstract method, which a matching lambda can implement.  (Chapter 8)

## G

**gain** -- the number that sets how hard a proportional correction pushes.  Dividing by `10.0` is a gain of `0.1`.  (5.6)

**gear reduction** -- a ratio that trades output speed for output torque.  (17.3)

**generics** -- writing a class or method with a type left as a placeholder, such as `T`, to be filled in later.  `<T extends X>` limits the placeholder to `X` and the classes built on it.  (7.3)

**`giveInstructions`** -- the assembly method that takes the mechanism driver's gamepad and passes it to each component's `move`.  (9.3)

**guard clause** -- an early check that returns when an operation should not proceed.  (5.8, 13.5)

## H

**hardware map** -- the SDK's list of every device plugged into the robot, looked up by type and name.  (3.1)

**HD Hex Motor** -- the motor the team uses with an external UltraPlanetary gearbox whose ratio the builder chooses.  (17.1)

**heading** -- the direction the robot faces, measured as yaw.  (3.7)

**hex shaft** -- the six-sided, 5mm steel shaft REV DUO mechanisms turn on.  (16.6)

**holonomic** -- able to translate in any direction while controlling heading.  (18.6)

**hue** -- a color as an angle around the color wheel, from 0 to 360.  (3.6)

**hysteresis** -- two thresholds with a gap between them, so a yes-or-no decision changes only when the reading crosses the whole gap.  (4.3)

## I

**I2C** -- the way the hub talks to small sensors, in short digital messages on a 4-pin cable.  (19.9)

**IAutonStrategy** -- an interface for one whole autonomous routine in a single `execute()` method, run start to finish.  (12.9)

**idler** -- an intermediate gear in a simple gear train, used to bridge distance and change the number of direction reversals.  (18.2)

**`if` chain** -- an `if`, any number of `else if`s, and an optional `else`.  Exactly one branch runs.  (1.8)

**IMU** -- the sensor inside the Control Hub that reports which way the robot is pointing.  (3.7)

**inner class** -- a class written inside another class without `static`.  Each of its objects belongs to one object of the outer class and can use its private fields.  (7.3)

**`instanceof`** -- Java's question "is this object really that type?" (15.4)

**instructions flow down** -- gamepad input enters at the OpMode, and each layer hands it to the layer below until a component commands hardware.  (6.4)

**intention** -- the action a caller wants the mechanism to perform.  (Chapter 11)

**interface** -- a type that lists methods with their bodies left out:  a promise about what anything of that type can do.  (1.7)

**interference** -- electrical noise one wire puts into another.  (19.9)

**IState** -- the interface for one step.  Its `execute()` does a little work and hands back the next step, or `null` when the job is done.  (12.2)

## J

**junk drawer** -- a shared file that has collected unrelated one-off helpers, so its job no longer fits in one sentence.  (2.8)

## K

**Kickoff Starter Bot** -- a complete reference robot REV releases at each season's Kickoff, built for that season's game.  (20.1)

**kit guide** -- REV's step-by-step assembly guide for one general-purpose mechanism, such as a drivetrain or a lift.  (20.1)

## L

**lambda** -- a chunk of code written down so it can be handed to something else and run later, written with an arrow `->`.  (1.9)

**layer** -- one level of the robot's code, with one job.  There are four.  (6.1)

**leaky abstraction** -- an interface that makes callers depend on details the interface was meant to contain.  (Chapter 11)

**limit switch** -- a sensor at the end of a mechanism's travel that reports when the mechanism gets there.  (19.6)

**linear slide** -- a mechanism that moves in a straight line, one stage sliding along another.  (19.1)

**loop** -- the `while (opModeIsActive())` that runs from start to the end of the match, updating the robot every time around.  (12.1)

## M

**M3** -- the metric screw size used all over the robot:  3mm across, with threads 0.5mm apart.  (16.1)

**macro strategy** -- a multi-step behavior requested as one job.  (13.3)

**meaning** -- what a reading says about the robot and the field, like "the arm is at the bottom."  (4.2)

**mecanum wheel** -- a wheel with angled rollers that works with three others to combine their forces for driving and strafing.  (18.6, 18.7)

**MechAssembly** -- the layer that owns a group of mechanisms and coordinates them.  `BillyMA` is one.  (6.1)

**MechComponent** -- the layer that owns exactly one mechanism and its hardware.  `SpinnyIntake` is one.  (6.1)

**MegaTag1** -- the Limelight's field pose from vision alone, `getBotpose()`.  (14.4)

**MegaTag2** -- the Limelight's field pose using a heading the robot supplies from its IMU, `getBotpose_MT2()`.  (14.5)

**method** -- a named block of code an object can run.  (1.4)

**mode strategy** -- a control scheme that continues until another selection changes it; no separate implementation is shown on Billy.  (13.3)

**module** -- gear pitch diameter divided by tooth count.  (18.1)

## N

**noise** -- the small, random wobble in every real measurement.  (4.3)

**nominal ratio** -- the convenient ratio used to name a cartridge.  The actual ratio comes from its internal gears.  (17.3)

**non-blocking** -- a way of waiting that finishes the tick and checks again on the next one, so the rest of the robot keeps running.  (12.1, 12.4)

**null** -- the value that means no object here.  (1.2)

**`NullPointerException`** -- the crash you get when code calls a method on `null`.  (5.8)

**nyloc nut** -- an M3 nut with a nylon ring that grips the threads against vibration.  The beveled side, with the ring, faces out.  (16.5)

## O

**object** -- one thing built from a class.  (1.1)

**omni wheel** -- a wheel whose small perimeter rollers allow sideways movement.  (18.6)

**OpMode** -- a program the Driver Station can pick and run.  It builds the robot and calls it every loop.  (6.3)

**out-of-range value** -- a reading that looks like a number and can't be true for your robot, like a huge distance with nothing in front of the sensor.  (4.4)

**override** -- to fill in or replace a method from the superclass.  The `@Override` annotation asks the compiler to check that you have.  (1.6)

**ownership** -- the right to command a resource at the current moment.  (13.1)

## P

**package** -- a folder of classes that belong together, named on the first line of each file.  (1.3)

**parameter** -- a name in a method's signature for something the method needs handed to it.  (1.4)

**passing telemetry down** -- each layer receiving the telemetry object from the layer above, keeping it, and handing it to every layer below before it builds anything of its own.  (9.4)

**pattern** -- the way a design solves a problem that comes back every season, such as mounting a motor against shock loads.  (20.2)

**pipeline** -- a saved setup on the Limelight that decides what it looks for.  (5.3)

**pitch** -- a repeating spacing:  between chain pins or between belt teeth.  (18.3, 18.5)

**pitch diameter** -- the diameter of the imaginary circle where gears roll together.  (18.1)

**polling** -- asking for fresh data again and again, on a schedule.  (5.3)

**pose** -- where something is and which way it's facing, together.  (14.1)

**pose ambiguity** -- when a small, flat target fits two different poses nearly equally well, so the solver can flip between them.  (15.3)

**power** -- work done per second, measured in watts.  (17.8)

**proportional control** -- making a correction in proportion to the error:  big error, big push.  (5.6)

**pulley** -- a wheel engaging a belt to carry turning motion.  (18.5)

## R

**reading** -- what a sensor measured, like `true` or `4.2`.  (4.2)

**reset** -- put a state machine back on its first step.  `BillyRapidFire`'s `reset` also sets the ball count.  (12.8)

**resource** -- a part whose competing command requests need coordination.  (13.1)

**return type** -- the type of value a method hands back.  `void` means it hands back nothing.  (1.4)

**reuse first** -- before writing a component, look for one that already owns the same hardware, and build another one from it.  (7.6)

**REV DUO** -- REV Robotics' build system, built on a 15mm grid and a 5mm hex shaft.  The team builds with it exclusively.  (16.1)

**rigid body** -- an object whose parts are fixed to one another and move together.  (15.6)

**Robot** -- the layer that owns the whole robot:  one drive train, one mech assembly, and robot-wide sensors.  (6.1)

**robot-wide sensor** -- a sensor describing the whole robot or supplying information to more than one subsystem.  (10.8)

**`robotPose`** -- the webcam route's field pose for the robot, on each detection.  (14.2)

**rotating an offset** -- turning an arrow to match the direction something faces, before adding it on.  (15.6)

**run mode** -- the setting that decides what `setPower` means:  raw power, a held speed, or a drive to a position.  (3.3)

## S

**safety factor** -- a multiplier allowing for loads and losses beyond a simple calculation.  (17.8)

**scrub** -- sideways sliding of a wheel as the robot turns.  (18.6)

**sensor** -- a device that answers a question about the robot or the field.  (4.1)

**sensor fusion** -- combining different sensors so each covers the others' weak spots.  (14.5)

**sensor ownership rule** -- a sensor belongs to the layer that can act on what it says:  the component, the assembly, or the robot.  (4.6)

**service loop** -- a deliberate loop of extra wire at a moving joint, so the wire flexes along the loop.  (19.9)

**Servo** -- a servo that turns to a position from `0.0` to `1.0` and holds it.  (3.4)

**Servo Power Module** -- a device supplying servo power from the battery bus, with control signals from the hub.  (17.7)

**set screw** -- a small screw that threads through a hub and presses on a shaft to hold the hub in place.  (16.6, 16.7)

**shaft collar** -- a metal ring that clamps onto a hex shaft with a set screw, to keep the shaft from sliding.  (16.7)

**signature** -- a method's name and its parameter list.  (1.4)

**single flush point** -- the method that sends the telemetry buffer after the subsystems have written their readings.  In this codebase, it's `Robot.updateTelemetry()`.  (10.6)

**six degrees of freedom** -- the six independent ways a thing can move:  three slides and three turns.  (14.1)

**SLAM** -- Simultaneous Localization and Mapping:  building a map of landmarks while finding your place on it.  (14.8)

**Smart Robot Servo** -- a programmable servo with angular and continuous rotation modes.  (17.6)

**spacer** -- a short Delrin tube with a hex hole, 1.5mm, 3mm, or 6mm long, that fills a gap along a shaft.  (16.7)

**sprocket** -- a toothed wheel engaging a chain.  (18.3)

**squaring a frame** -- making a frame's corners true right angles.  (20.7)

**stall torque** -- the torque a motor produces with its output stopped.  (17.5)

**standard deviation** -- a measure of how much a reading wobbles around the true value.  Small means steady.  (14.6)

**state** -- one step of a job.  (12.2)

**state machine** -- a chain of states, each naming the next, run one step at a time.  (12.2, 12.3)

**StateMachine** -- the team's abstract class that keeps the current step and runs it once per call to `updateState()`.  (12.3)

**`static`** -- belonging to the class, with no object.  You call a `static` method on the class's name.  (2.2)

**strategy pattern** -- a design that separates a variable rule from the object that calls it and lets that object receive an implementation.  (Chapter 8)

**stream** -- a list's items lined up to be worked through by a chain of steps.  (5.7)

**Studs Up** -- the rule for extrusion:  the screw head goes in the slot, the stud points out, and the nut goes on from outside.  (16.4)

**subclass** -- a class that extends another, its **superclass**, and gets everything the superclass has.  (1.6)

**`super(...)`** -- the call, first in a subclass's constructor, that runs the superclass's constructor.  (1.6)

**surface speed** -- the speed of a wheel or roller's rim.  (17.9)

## T

**tag ID** -- the number an AprilTag carries.  The game manual says which ID is where.  (5.1)

**tag library** -- the SDK's table of each tag's ID, size, and field position.  (14.2)

**telemetry buffer** -- the page of lines waiting to go to the Driver Station.  `addData` and `addLine` write to it, and `telemetry.update()` sends it.  (7.5)

**TeleOp** -- the period of a match when drivers control the robot with gamepads.  Also the code written for it.  (6.3)

**tell** -- the one detail that gives a bug shape away before you've traced the whole bug.  (22.1)

**template** -- a starting shape for a class, with its blanks marked.  The team's are in `doc/NewRobotDesignWorkflow.md`.  (21.4)

**template method** -- a base-class method that fixes a job's steps and order while other code supplies what each step does.  `Robot.update` is a template-method-style design using the robot's chosen parts.  (10.3)

**Thread.sleep** -- Java's method for stopping the program for a number of milliseconds.  It blocks.  (12.1)

**three-question test** -- scope from the top, scope from the bottom, and presentation:  the three questions that decide whether a helper belongs in `Extensions`.  (2.9)

**threshold** -- a line a reading has to cross for an answer to change.  (4.3)

**tick, of an encoder** -- one count of an encoder.  (3.2)

**tick, of the loop** -- one trip around the loop.  (12.1, 22.2)

**TODO** -- a comment marking known, open work, written where the work is.  (1.10)

**top-down design** -- deciding what the robot must do, which subsystems exist, and which behaviors belong at each layer, before writing code.  (21.1)

**torque** -- twisting force, measured in this book in N-m.  (16.6, 17.1)

**torsional rigidity** -- how hard a part is to twist.  (16.2)

**TouchSensor** -- a button sensor whose `isPressed()` is `true` while it's pushed.  (3.5)

**traction wheel** -- a treaded wheel that grips the floor and resists sideways sliding.  (18.6)

**two kinds of component** -- a wrapper around one output device, named for the device;  or a set of devices and the sensors that guide them, named for what it does.  (7.7)

**two-phase construction** -- building an object in two steps:  the constructor with the hardware, then `initializeTelemetry` with telemetry and the autonomous twin.  (7.4)

**`tx`** -- how far a tag sits left or right of the middle of the picture, in degrees.  Zero is dead ahead.  (5.5)

**`ty`** -- the same, up and down.  (5.5)

**type** -- the kind of value a name can have.  (1.2)

## U

**UltraPlanetary** -- a modular gearbox with interchangeable reduction cartridges.  (17.3)

**union** -- a set containing all values found in either of the combined sets.  (13.6)

**utility class** -- a class with only `static` members, a named home for a set of related helpers.  (2.2)

## V

**varargs** -- a parameter written with three dots, like `int... tags`, that takes any number of values.  Inside the method they arrive as an array.  (2.7)

**VisionPortal** -- the SDK object that feeds a webcam's frames to vision processors on the Control Hub.  (3.8)

**void** -- the return type of a method that hands nothing back.  (1.4)

## W

**webcam** -- a USB camera, configured as a `WebcamName`.  (3.8)

**weighted average** -- an average where each value counts in proportion to its weight.  (15.8)

**work** -- energy transferred by a force moving something through a distance, measured in joules.  (17.8)

## Y

**yaw** -- turning left or right, flat on the field.  It goes up as the robot turns counterclockwise.  (3.7)

## Z

**zeroing** -- finding a known physical position and resetting the encoder count there.  (19.7)
