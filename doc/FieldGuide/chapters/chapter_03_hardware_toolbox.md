# Chapter 3 - The FTC Hardware Toolbox

*The SDK gives you a raw handle.  This team always wraps it.*

---

The FTC SDK knows how to talk to a few dozen kinds of device:  motors,
servos, switches, distance and color sensors, the hub's own heading sensor,
and cameras.  This chapter is the tour.  Each entry takes one kind of device,
shows the SDK type that talks to it, and shows where this team's code uses it.

Look here when a mechanism needs to move or sense something and you want to
know which type to reach for, or when you meet `DcMotor` or `CRServo` in the
code and want to know what it can do.

The SDK samples live in
`FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/`.
They're the reference for each type, and this chapter names them by file.
Their style belongs to the SDK's authors.  They call `motor.setPower(...)`
straight from the OpMode, and this team's code wraps every device in a class
of its own first.  3.10 says why.

Read first:  Chapter 1, for reading a class, a method call, and an `if`.

| Entry | The question it answers |
|---|---|
| 3.1 | How does the code find a device? |
| 3.2 | How do I run a motor, and which way is forward? |
| 3.3 | What are run modes, and when do I need `DcMotorEx`? |
| 3.4 | What's the difference between a `Servo` and a `CRServo`? |
| 3.5 | How does the robot know it's touching something? |
| 3.6 | How far away is it, and what color is it? |
| 3.7 | Which way is the robot facing? |
| 3.8 | How does the robot see? |
| 3.9 | What else is on the shelf? |
| 3.10 | Who talks to the hardware? |

---

## 3.1  The hardware map

*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java`

Every device on the robot starts with one line.  Here it is in `SpinnyIntake`,
the class that runs an intake motor:

```java
        intake = hardwareMap.get(DcMotor.class, motorName);
```

The **hardware map** is the SDK's list of every device plugged into the robot.
`hardwareMap.get(...)` looks one up and hands back a **handle**, an object your
code uses to talk to that device.  From this line on, `intake` is the motor as
far as the code is concerned.

Two things go in.  The first is the type you expect back, here a `DcMotor`.
The second is the device's name.  `motorName` is a parameter, so the name comes
from whoever builds the intake.  `BillyMA`, the class that holds Billy's
mechanisms, passes it in:

```java
        intake = new SpinnyIntake(hardwareMap, "intakeMotor",
```

So where does `"intakeMotor"` come from?  A person typed it.  The **Driver
Station configuration** is the list, set up on the Driver Station, that gives a
name to every motor, servo, and sensor on each port.  It's saved on the robot
itself, outside the code.  The string in the code has to match the name in
that list letter for letter, capitals included.  Chapter 1 calls this string a
configuration name.

Both halves have to be right.  Ask for a name that's missing from the
configuration, or ask for a `DcMotor` where the configuration has a servo, and
the OpMode stops during init, before anything moves.  For a missing name, the
error names the string the code went looking for, which tells you which line
to check.

> **Aside:  `DcMotor.class`**
>
> `DcMotor.class` is the type itself, handed over as a value.  `get` uses it
> to know what kind of handle to give back.  Hand it `DcMotor.class` and you
> get back a `DcMotor`.  Hand it `Servo.class` and you get back a `Servo`.

> **Try it.**  Open the active configuration on the Driver Station and find
> the name on the intake motor's port.  Then search the code for that string.
> It turns up once, in `BillyMA.java`, as the line above.  A name in exactly
> one place means a rename is a one-line change.

*See also:*  3.10, Chapter 1, Chapter 6

## 3.2  DcMotor:  power, direction, and ticks

*Where it lives:*  `RobotModel/DriveTrain/Mecanum/MecanumDrive.java`

A **`DcMotor`** is the SDK's type for an ordinary motor, the workhorse of
every robot.  It spins for as long as it has power.  You run it with
`setPower`.  The intake's autonomous verb `startIntake()` is one line of it,
`intake.setPower(1);`.

Power runs from `-1.0` to `1.0`.  `1` is full speed one way, `-1` is full
speed the other way, and `0` stops it.

**Direction.**  Which way is "one way"?  Picture the four wheels of a drive
train.  The motors on the left face one way and the motors on the right face
the other, mirror images of each other.  Give all four the same power and the
two sides turn opposite ways, so the robot spins.  The fix is to reverse half
of them, so that positive power means "forward" to every wheel.

`MecanumDrive` does it as it fetches each motor:

```java
        LB = hardwareMap.get(DcMotor.class, "lb");
        LB.setDirection(orientationConfiguration.getLb());
        LF = hardwareMap.get(DcMotor.class, "lf");
        LF.setDirection(orientationConfiguration.getLf());
```

The directions arrive from outside, in an `OrientationConfiguration` object
that takes all four as constructor arguments, in the order left back, left
front, right back, right front.  Here's Billy's:

```java
        driveTrain = new MecanumDrive(hardwareMap, new MecanumDrive.OrientationConfiguration(
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.REVERSE,
                DcMotorSimple.Direction.FORWARD)
        );
```

`Wildbots2025` builds the same class with `REVERSE`, `FORWARD`, `FORWARD`,
`REVERSE`.  Two robots, two wiring jobs, one `MecanumDrive`.

> **Watch out.**  The right directions come from the robot, so the way to find
> them is a test drive.  Put the robot up on a box with its wheels off the
> ground, push the stick forward, and watch each wheel.  Any wheel turning
> backward gets its direction flipped.

**Encoders and ticks.**  A `DcMotor` on its own knows only the power it was
given.  An **encoder** is a sensor built onto the motor that counts how far
the shaft has turned.  Each count is a **tick**.  `getCurrentPosition()` hands
back the count so far, and `SpinnyIntake` puts it on the Driver Station
screen:

```java
        telemetry.addData("intake position:", intake.getCurrentPosition());
```

How many ticks make one full turn depends on the motor.  REV's Core Hex
Motor, for example, counts 288 ticks for each turn of its output shaft
([REV's Core Hex Motor page](https://docs.revrobotics.com/duo-build/motion/motors/core-hex-motor)).
Chapter 17 has the motors themselves.

*See also:*  3.3, Chapter 17, Chapter 18

## 3.3  Run modes, and DcMotorEx

*Where it lives:*  `RobotAutoDriveByEncoder_Linear.java`, in the SDK samples

A motor's **run mode** changes what `setPower` means.  You pick one with
`setMode`.

| Mode | What it does |
|---|---|
| `RUN_WITHOUT_ENCODER` | power goes straight to the motor.  The default. |
| `RUN_USING_ENCODER` | power is a speed request, and the hub uses the encoder to hold that speed steady |
| `RUN_TO_POSITION` | pair it with `setTargetPosition(...)`.  The hub drives the motor to that tick count, and `isBusy()` says whether it's still on the way. |
| `STOP_AND_RESET_ENCODER` | sets the tick count back to zero |

The SDK sample `RobotAutoDriveByEncoder_Linear.java` uses all four.  A line
of it looks like this:

```java
        leftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
```

To drive a set distance, the sample hands each motor a target tick count,
switches to `RUN_TO_POSITION`, and waits in a `while` loop until `isBusy()`
turns false.  This team's code avoids loops that wait like that, because the
rest of the robot gets its turn only after the loop ends (Chapter 12).  The
team's motors all run in their starting mode, on plain power.

**`DcMotorEx`** is a `DcMotor` with extra methods.  The useful ones read and
set speed in ticks per second:  `getVelocity()` and `setVelocity(...)`.  You
ask the hardware map for the bigger type, `DcMotorEx.class`, as the sample
`ConceptMotorBulkRead.java` does.

Reach for it when a mechanism needs a held speed, and a flywheel is the
classic case.  Billy's flywheel, `DoubleShooter`, runs on plain power.
`BillyMA`'s button mapping starts it at `0.45`, and the driver trims it by
`0.05` up or down from the D-pad.

Plain power is a share of whatever the battery has.  As the battery runs down
through a match, the same power spins the flywheel a little slower, and the
driver's trim makes up the difference by hand.

> **Argue it.**  Should the flywheel move to `DcMotorEx` and `setVelocity`?
> The cost comes first:  an encoder on a flywheel motor, a target speed, and
> time at the robot to tune it.  Then the gain:  the same speed on a fresh
> battery and a tired one, and one less correction for the driver.  Which
> side of that trade-off wins for this season's robot?

*See also:*  3.2, Chapter 4, Chapter 12

## 3.4  Servo and CRServo

*Where it lives:*  `RobotModel/Mechs/Components/PusherServo.java`, `RobotModel/Mechs/Components/Turret.java`

A **`Servo`** turns to a position and holds it there.  It moves over a
limited arc, and `setPosition` takes a number from `0.0` to `1.0`, one end of
the arc to the other.  That range is the whole of it.

Billy's ball pusher is a servo.  `PusherServo` fetches it and flips its
direction, the same idea as a motor's direction in 3.2:

```java
        this.servoR = hardwareMap.get(Servo.class, servoNameR);
        this.servoR.setDirection(Servo.Direction.REVERSE);
```

Here's its autonomous verb for pushing:

```java
        public void pushBalls() {
            servoR.setPosition(0.8);
            reportStatus("Pusher: push");
        }
```

`0.8` means "pushed" because somebody tried positions on this robot until
the pusher pushed.  That's why the method is named for what it does, and
the number lives inside it.  Code that calls `pushBalls()` says what it
wants, and only this class has to know `0.8`.  Chapter 11 makes that a rule.

A **`CRServo`** is a servo rebuilt for continuous rotation, which is what CR
stands for.  It takes `setPower` like a motor, from `-1` to `1`, and it has no
sense of position at all.  Billy's turret is one:

```java
        servo = hardwareMap.get(CRServo.class, servoName);
```

The turret's autonomous `turnCCW()` calls `servo.setPower(1)`, turning it
counterclockwise.

The two types have different methods.  A `Servo` has `setPosition`, and a
`CRServo` has `setPower`.  Call the wrong one and the compiler rejects the
line, which is the best place to find out.

A `CRServo` reports its power and only its power.  So when a turret or a claw
needs to know where it is, it needs a sensor beside it:  a switch at each end
of its travel, or a sensor on the jaw.  Chapter 4 is about exactly that, and
Chapter 7 is about what adding one does to the class.

*See also:*  3.2, Chapter 4, Chapter 7, Chapter 11

## 3.5  Touch sensors:  TouchSensor and DigitalChannel

*Where it lives:*  `RobotModel/Mechs/Components/DoublyLimitedMotor.java`

The rest of the sensors in this chapter are sorted by the question each one
answers.  The first question is the simplest:  am I touching something?

A **`TouchSensor`** answers it with `isPressed()`, which is `true` while the
button is pushed in.  From the SDK sample `SensorTouch.java`:

```java
            if (touchSensor.isPressed()) {
```

The same button can also be read at a lower level, as a **`DigitalChannel`**:
a raw on-or-off wire on one of the hub's digital ports.  You set it to be an
input with `setMode(DigitalChannel.Mode.INPUT)`, then read its state.  From
`SensorDigitalTouch.java`:

```java
            if (digitalTouch.getState() == false) {
```

The sample's comment above that line says "button is pressed if value
returned is LOW or false."  So the two types answer the same question with
opposite booleans.  `isPressed()` is `true` on contact, and `getState()` is
`false` on contact.

> **Watch out.**  That flip is easy to get backward, and a backward limit
> switch stops a mechanism everywhere except at its limit.  Prefer
> `TouchSensor`, whose method name says which way round it is.

The team's code uses `TouchSensor`.  `DoublyLimitedMotor` is a motor with a
switch at each end of its travel, and it fetches each switch the usual way:

```java
        forwardSensor = hardwareMap.get(TouchSensor.class, forwardSensorName);
```

`CascadeArm`, the lift on `Wildbots2025`, builds one with three names from
the configuration:  `"cascadeMotor"`, `"topLiftLimit"`, and
`"bottomLiftLimit"`.  A switch used this way is a limit switch (Chapter 19).
4.6 shows how `DoublyLimitedMotor` uses the switches to protect its motor.

*See also:*  4.2, 4.6, Chapter 19

## 3.6  Distance and color sensors

*Where it lives:*  `SensorREV2mDistance.java` and `SensorColor.java`, in the SDK samples

**How far away is it?**  A **`DistanceSensor`** answers with
`getDistance(...)`, in whatever unit you hand it.  From the SDK sample
`SensorREV2mDistance.java`:

```java
            telemetry.addData("range", String.format("%.01f cm", sensorDistance.getDistance(DistanceUnit.CM)));
```

`DistanceUnit` also has `MM`, `METER`, and `INCH`.  REV's 2m Distance Sensor
has its own type, `Rev2mDistanceSensor`, with a few extra methods.  It's also
a `DistanceSensor`, so the sample asks the hardware map for the general type.
Code written against `DistanceSensor` works with any distance sensor you plug
in.  A distance sensor pointed at nothing reads as a very large number, and
4.4 says what to do about it.

**What color is it?**  Prefer **`NormalizedColorSensor`**.  The sample
`SensorColor.java` gives the reason in a comment:  "`NormalizedColorSensor`
consistently gives values between 0 and 1, while the values you get from
`ColorSensor` are dependent on the specific sensor you're using."  The sample
also sets the sensor's gain, a multiplier on its raw readings, so that a dim
reading still spreads across the range.  Then it reads the color:

```java
      NormalizedRGBA colors = colorSensor.getNormalizedColors();
```

`colors` has a red, a green, and a blue value.  Comparing red against blue
works badly, because a shadow or a brighter room changes all three at once.
The steadier answer is **hue**:  the color itself, as an angle around a color
wheel, from 0 to 360.  The SDK converts for you:

```java
      Color.colorToHSV(colors.toColor(), hsvValues);
```

After that line, `hsvValues[0]` is the hue, and it moves much less than the
raw values when the lighting changes.

The REV and AndyMark color sensors measure distance too, though the sample
warns the reading is "only useful at very small distances."

Billy has neither sensor yet.  Two places in the code name them as
candidates:  a grip sensor on a claw, and a sensor that sees whether a ball is
ready to fire.  Chapter 4 works through both.

*See also:*  4.2, 4.4, 4.7

## 3.7  The IMU:  which way is the robot facing?

*Where it lives:*  `RobotModel/Robots/BillyRobot.java`

The Control Hub (Chapter 19) has a sensor built in called an **IMU**, an
inertial measurement unit.  It feels the hub turning and tilting, and from
that it works out which way the hub is pointing.  The hub is bolted to the
robot, so that's which way the robot is pointing.

Here's how `BillyRobot` sets it up:

```java
        imu = hardwareMap.get(IMU.class, "imu");

        IMU.Parameters parameters = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP
                )
        );

        imu.initialize(parameters);
        imu.resetYaw();
```

Three steps.  Fetch it, the same as any device.  Tell it how the hub is
mounted.  Zero it.

The middle step matters most.  The hub can be bolted on flat, on its side, or
upside down, and the IMU measures turning relative to the hub.
`RevHubOrientationOnRobot` describes the mounting with two facts:  which way
the printed logo on the hub faces, and which way its USB ports face.  On
Billy, the logo faces the robot's right and the USB ports face up.  Get these
wrong and every reading is a turn about the wrong axis, while the screen
shows ordinary-looking numbers.

Then the reading.  `BillyRobot`'s `getHeading()` asks the IMU for
`getRobotYawPitchRollAngles()` and returns the `getYaw(AngleUnit.DEGREES)`
part.  The IMU reports three angles.  **Yaw** is turning left or right, flat on the
field.  Pitch is tipping nose up or down, and roll is tipping side to side.
The direction the robot faces, measured as yaw, is its **heading**.  The
sample `SensorIMUOrthogonal.java` says which way the numbers go:  "Yaw value
should INCREASE as the robot is rotated Counter Clockwise."  `resetYaw()`
makes wherever the robot faces right now count as zero, so on Billy, heading
`0` means "the way the robot faced at init."

> **Try it.**  Run the sample `SensorIMUOrthogonal` with the hub's real
> mounting directions typed in.  Turn the robot by hand, counterclockwise as
> seen from above.  The yaw should climb.  If pitch or roll changes instead,
> the two mounting directions are wrong.

The drive train uses the IMU too, for `turnToAngle()` in autonomous.  So two
parts of the robot want one sensor, and one of them has to be in charge of
setting it up.  That question has an entry of its own, 4.9.

*See also:*  4.9, Chapter 14

## 3.8  Cameras:  a webcam or a Limelight

*Where it lives:*  `ConceptVisionColorLocator_Circle.java`, in the SDK samples;  `RobotModel/Robots/BillyRobot.java`

A camera answers the biggest question of all:  what can I see?  There are two
ways to put one on an FTC robot, and Chapter 5 is all about them.  Here's
enough to recognize each one.

**A webcam and a `VisionPortal`.**  A USB webcam appears in the configuration
as a `WebcamName`.  The webcam only takes pictures.  The work of finding
things in them happens on the Control Hub, in **processors**:  one finds
AprilTags, another finds patches of a color.  A **`VisionPortal`** is the SDK
object that connects them, feeding each camera frame to every processor you
give it.

The sample `ConceptVisionColorLocator_Circle.java` builds a processor that
looks for purple game pieces, then builds a portal and hands it the processor
and the webcam:

```java
        VisionPortal portal = new VisionPortal.Builder()
                .addProcessor(colorLocator)
                .setCameraResolution(new Size(320, 240))
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .build();
```

Every loop after that, `colorLocator.getBlobs()` has a fresh list of
**blobs**, patches of the target color, each with a size and a position in
the picture.

**A Limelight.**  A `Limelight3A` is a camera with its own small computer
inside.  It does the vision work on board and hands back answers, so the
Control Hub only reads them.  Billy has one, and `BillyRobot` sets it up in
four lines:

```java
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.start();
        limelight.pipelineSwitch(0);
```

5.3 explains each line.  The `Robot` base class also has a field ready for
the webcam route, `protected VisionPortal visionPortal;`, for a robot that
adds one.  Billy uses the Limelight, so that field is unused on Billy.

*See also:*  4.8, 5.2, 5.3, 5.10

## 3.9  The rest of the shelf

The SDK knows more devices than this robot uses.  Here are the ones worth
knowing by name, so you can search for them when a design needs one.  Each is
fetched with `hardwareMap.get`, like everything else, and each has a sample
showing its methods.

| Device type | What it's for | Sample |
|---|---|---|
| `SparkFunOTOS`, `GoBildaPinpointDriver` | odometry:  tracking the robot's position on the field in a way that survives wheel slip | `SensorSparkFunOTOS.java`, `SensorGoBildaPinpoint.java` |
| `OctoQuad` | reads up to eight encoders through one port | `SensorOctoQuad.java` |
| `HuskyLens` | a small vision sensor that does its own processing | `SensorHuskyLens.java` |
| `RevBlinkinLedDriver`, `LED`, `SparkFunLEDStick` | signal lights, and a real help when debugging | `SampleRevBlinkinLedDriver.java`, `ConceptRevLED.java`, `ConceptLEDStick.java` |
| `VoltageSensor` | the battery's voltage, for making up power as the battery runs down | `ConceptTelemetry.java` reads one |

*See also:*  3.1, 3.3, Chapter 14

## 3.10  Who talks to the hardware?

*Where it lives:*  `OpModes/BillyTeleOpBLUE.java`

Here's a motor in an SDK sample, `BasicOpMode_Iterative.java`.  The OpMode,
the program the Driver Station runs, fetches `leftDrive` from the hardware map
at init.  Then, every loop, it works out a power from the stick and sets it:

```java
        leftDrive.setPower(leftPower);
```

It's short, it's clear, and it works.  For one robot in one season, it's hard
to beat.

Here's Billy's TeleOp loop, the whole thing:

```java
        while(opModeIsActive()){
            robot.update(gamepad1, gamepad2);
            robot.updateTelemetry();
        }
```

There's no motor in it.  Everything between the gamepad and the hardware is
the team's robot model (Chapter 6).  A `MechComponent` owns one device, or one
device and the sensors that guide it.  A `MechAssembly` owns the components
for one part of the robot.  A `Robot` owns the drive train and the assemblies.
The mapping from buttons to motors arrives in each component as a lambda, a
chunk of code handed over to run later (Chapter 1).

That costs something.  To follow one button press to one motor, you open four
or five files where the sample needed one.  Here's what it buys.  A device's
configuration name is written in exactly one place.  A class like
`SpinnyIntake` works on any robot with an intake motor, with a different name
and a different button mapping.  Next season's robot starts with this
season's components.  That's the trade-off the whole codebase is built on:
more pieces now, in exchange for pieces you can use again.

Here's the hardware from this chapter, matched to the classes that own it:

| Hardware | Owned by | Which is a |
|---|---|---|
| one `DcMotor` | `SpinnyIntake` | `MechComponent` |
| two `DcMotor`, driven as a pair | `DoubleShooter` | `MechComponent` |
| `DcMotor` and two `TouchSensor` | `DoublyLimitedMotor` | `MechComponent` |
| `Servo` | `PusherServo` | `MechComponent` |
| `CRServo` | `Turret`, `Claw` | `MechComponent` |
| four `DcMotor` and the IMU | `MecanumDrive` | `DriveTrain` |
| two `DcMotor` | `StandardTankDrive`, `OneStickTank` | `DriveTrain` |
| `Limelight3A` | `BillyRobot` itself | robot-wide sensor |

The last row is the one to notice.  The Limelight belongs to the robot, above
every component, because it looks at the whole field and more than one part
of the robot might want what it sees.  4.8 explains, and Chapter 10 makes it
a rule for the whole robot layer.  When you're about to add a device, the next question is
whether a component already owns that shape of hardware.  Chapter 7 answers
it, with the rule the team calls reuse first.

*See also:*  3.1, 4.8, Chapter 6, Chapter 7, Chapter 10

---

## Chapter summary

The SDK has a type for every device, and every one is fetched the same way:
`hardwareMap.get`, a type, and a name typed into the Driver Station
configuration.  Motors take power and a direction, and an encoder counts their
ticks.  Servos take a position, and continuous-rotation servos take power.
Each sensor answers one question.  This team's code wraps every device in a
class before anything uses it, so the OpMode talks only to the robot.

| To | SDK types | Entry |
|---|---|---|
| find a device | `hardwareMap.get(Type.class, "name")` | 3.1 |
| move something | `DcMotor`, `DcMotorEx`, `Servo`, `CRServo` | 3.2-3.4 |
| sense touch, distance, or color | `TouchSensor`, `DigitalChannel`, `DistanceSensor`, `NormalizedColorSensor` | 3.5, 3.6 |
| know the heading | `IMU` | 3.7 |
| see | `WebcamName` and `VisionPortal`, or `Limelight3A` | 3.8 |

### Java in this chapter

| Syntax | Where |
|---|---|
| `DcMotor.class`, a type handed over as a value | 3.1 |

### Vocabulary

**CRServo** -- a servo rebuilt for continuous rotation.  It takes `setPower`
and has no sense of position.  (3.4)

**DcMotor** -- the SDK's type for an ordinary motor, run with `setPower` from
`-1.0` to `1.0`.  (3.2)

**DcMotorEx** -- a `DcMotor` with extra methods, including `getVelocity()`
and `setVelocity(...)`.  (3.3)

**DigitalChannel** -- a raw on-or-off wire on a digital port.  Read as a
button, `getState()` is `false` while it's pressed.  (3.5)

**DistanceSensor** -- the SDK's general type for a sensor that measures
distance.  (3.6)

**Driver Station configuration** -- the list, saved on the robot, that names
every device on every port.  (3.1)

**encoder** -- a sensor on a motor that counts how far its shaft has turned.
(3.2)

**hardware map** -- the SDK's list of every device plugged into the robot,
looked up by type and name.  (3.1)

**heading** -- the direction the robot faces, measured as yaw.  (3.7)

**hue** -- a color as an angle around the color wheel, from 0 to 360.  (3.6)

**IMU** -- the sensor inside the Control Hub that reports which way the robot
is pointing.  (3.7)

**run mode** -- the setting that decides what `setPower` means:  raw power,
a held speed, or a drive to a position.  (3.3)

**Servo** -- a servo that turns to a position from `0.0` to `1.0` and holds
it.  (3.4)

**tick** -- one count of an encoder.  (3.2)

**TouchSensor** -- a button sensor whose `isPressed()` is `true` while it's
pushed.  (3.5)

**VisionPortal** -- the SDK object that feeds a webcam's frames to vision
processors on the Control Hub.  (3.8)

**webcam** -- a USB camera, configured as a `WebcamName`.  (3.8)

**yaw** -- turning left or right, flat on the field.  It goes up as the robot
turns counterclockwise.  (3.7)

### Check your understanding

1. `hardwareMap.get(DcMotor.class, "intakeMotor")` needs something from the
   Driver Station before it works.  What is it, and what happens when it's
   wrong?  (3.1)
2. Billy and `Wildbots2025` use the same `MecanumDrive` class, wired
   differently.  How does one class handle both?  (3.2)
3. Name the SDK type for each:  a drive wheel, a claw on a continuous-rotation
   servo, a limit switch, the robot's heading, and the distance to a wall.
   (3.2, 3.4, 3.5, 3.6, 3.7)
4. What does `RevHubOrientationOnRobot` tell the IMU, and what happens when
   it describes the wrong mounting?  (3.7)
5. The SDK samples call `setPower` right inside the OpMode, and it works.  Why
   does this team put a class in between?  (3.10)

## Where to go next

Chapter 4 asks what a sensor's reading means, and which layer of the robot
should own each sensor.  Chapter 5 follows the cameras from 3.8 all the way to
a turning turret.  Chapter 7 covers the classes that wrap each device, and
when a new device earns a new class.  Chapters 17 and 19 cover the motors,
switches, and hubs as physical parts.
