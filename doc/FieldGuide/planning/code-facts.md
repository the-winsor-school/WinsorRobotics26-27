# Code Facts:  the Flint Lessons Checked Against Current Code

Audited 2026-10-26 against `master` at `305fb81`.  Lessons last edited at `34c22ea` (2026-09-25).

**Read section E as well.**  The continuation found further hardware-reference
errors and a limit-switch timing limitation in `DoublyLimitedMotor`.  The
original audit is preserved below, with those corrections added at the end.

**Student-work guidance, clarified by Jason on 2026-10-26:**  this codebase
was written entirely by students, with some recent AI assistance he has not
reviewed.  This audit supports accurate descriptions of behavior;  its
historical labels are not an editorial instruction to call out students'
mistakes.  Minor bugs may be ignored.  A confirmed bug that prevents a
feature from working gets a neutral source TODO naming the next step.
Follow PROCESS's student-work section when using any observation below.

**Conventions**

- Java paths are relative to `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.  Lesson paths are relative to `doc/FlintLessons/`.  Line numbers are file line numbers.
- Every fenced `java` block is copied from the file by script, verbatim (CRLF stripped, nothing else changed).  The `path:lines` label above each block is exact.
- Verdicts for lesson code blocks:  **UNCHANGED** (matches, allowing the lesson's `...` or reformatting; "abridged" means the lesson silently drops lines), **CHANGED**, **REMOVED**, **ILLUSTRATIVE** (invented on purpose, or quoted from the SDK rather than team code).
- Verdicts for prose claims:  **TRUE NOW** / **FALSE NOW**.  **UNVERIFIED** says why.
- Bugs:  **OPEN**, **FIXED** (commit), **CHANGED SHAPE**.

---

## 0.  What changed since the lessons, in one table

`git log 34c22ea..HEAD`.  Doc files (`doc/`) did **not** change in that range;  only 12 Java files did.

| Commit | Date | What it did (verified from the diff) |
|---|---|---|
| `e89bec0` | 10-02 | `StateMachine.abort()` now nulls `elapsedTime`.  Deleted the dead `AutonomousBillyMA`/`BillyRapidFire` construction and the old strategy (with its mid-loop `telemetry.update()`) from `BillyMA`'s constructor. |
| `d5e2642`..`0acfd09` | 10-02 | "step 1-5" (the plan's Phase 2):  `IMacroAssemblyStrategy<R>` added to `MechAssembly`;  `BillyRapidFire` implements it (`claims/start/update/cancel`);  new `MacroManager<R>`;  `BillyMA.Part` enum;  claims-aware `BillyMA` strategy;  `BRF.abort()` removed from `initializeTelemetry` (`0acfd09`). |
| `54433ed`..`30acb74` | 10-05 | Phase 3:  `Robot.IAssistRobotStrategy<R>`;  `LimelightAutoTarget` implements it;  `BillyRobot` gets an `assist` slot, `startAssist`, `assistClaims`, `gamepad1.y` toggle, passes `assistClaims()` into `BillyMA.giveInstructions(gamepad, robotClaims)`.  `super.update(...)` dropped from `BillyRobot`'s strategy. |
| `c34c7c8`, `653f95b`, `ef6697b` | 10-09 | Phase 4:  all button requests moved into `BillyRobot`'s strategy;  `stopAll()` on `gamepad1.b`;  `gamepad2.a` requests rapid fire, `gamepad2.b` cancels it;  overlap-based cross-cancel ("newest request wins"). |
| `962e265` | 10-09 | Phase 5:  `name()` on both strategy interfaces;  `MacroManager.lastEnd()/activeName()`;  end reasons;  `BillyRobot.updateTelemetry()` override that prints Assist / Macro / Last macro / Last assist. |
| `1a6809a` "rest of changes" | 10-09 | Parallel branch, merged with Phase 5 in `e1d8e24`.  **Fixed most of the lesson-taught bugs**:  intake `if` chain, pusher direction moved to constructor, `SpinnyIntake` auton signs, `Turret` sleeps removed, `LimelightAutoTarget.rotateCW` default sign, `LimelightExtensions` null guard, `BillyMA` telemetry shadow field and `tel` ctor param removed, `BillyMA.updateTelemetry()` filled in, `BillyRobot`'s unused `AprilTagProcessor` removed.  Also the Phase 5 telemetry pieces (duplicated with `962e265`). |
| `5b6ec11` / `b580b23` | 10-26 | "telemetry testing" and its revert.  Net zero. |
| `7480be5`, `305fb81` | 10-26 | Merges of `phase4-arbitration` and `phase5-telemetry` into `master`. |

Note:  `git log -S` attributes each fix to the commit that first changed it on its branch;  on `master` they all arrive through merge `305fb81`.

---

## Chapter 1 -- Reading Java in This Codebase  (`GettingStarted/01`)

### Code blocks

| # | Lesson lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | 31-43 | `SpinnyIntake` skeleton | UNCHANGED | `RobotModel/Mechs/Components/SpinnyIntake.java:9-82` |
| 2 | 54-56 | `private final DcMotor intake;` | UNCHANGED | `SpinnyIntake.java:51` |
| 3 | 94-96 | `public void move(Gamepad gamepad)` | UNCHANGED | `SpinnyIntake.java:79` |
| 4 | 109-114 | `canGoForward()` | UNCHANGED | `DoublyLimitedMotor.java:136-139` |
| 5 | 129-131 | `imu.getRobotYawPitchRollAngles().getYaw()` | UNCHANGED | `MecanumDrive.java:37`, `:286` |
| 6 | 139-145 | `SpinnyIntake` constructor | UNCHANGED (abridged:  drops line 64 `this.strategy = (SpinnyIntakeControlStrategy) super.strategy;` with no `...`) | `SpinnyIntake.java:57-65` |
| 7 | 153-155 | `intake = new SpinnyIntake(hardwareMap, "intakeMotor", ...);` | UNCHANGED | `BillyMA.java:53` |
| 8 | 165-175 | turret `if / else if / else` | UNCHANGED | `BillyMA.java:101-111` |
| 9 | 182-191 | intake "two separate `if`s" bug | **CHANGED** (bug fixed) | below |
| 10 | 208-212 | `(motor, gamepad) -> { ... }` | UNCHANGED | `BillyMA.java:54` |
| 11 | 251-253 | `public boolean canGoForward()` | UNCHANGED | `DoublyLimitedMotor.java:136` |

Block 9, current code (fixed in `1a6809a`;  the TODO above it is gone):

`RobotModel/Mechs/Assemblies/BillyMA.java:53-63`
```java
        intake = new SpinnyIntake(hardwareMap, "intakeMotor",
                (motor, gamepad) -> {
                    if (gamepad.dpad_up) {
                    motor.setPower(0.75); }
                    else if (gamepad.dpad_down) {
                    motor.setPower(-0.75);
                    } 
                    else {
                        motor.setPower(0);
                    }
                });
```

`DoublyLimitedMotor.setPower` (named at lesson 119, 243):

`RobotModel/Mechs/Components/DoublyLimitedMotor.java:93-100`
```java
    public void setPower(double power){

        if ((power > 0 && !canGoForward() )|| (power < 0 && !canGoReverse())) {
            power = 0;
        }

        motor.setPower(power);
    }
```

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 45-46 | `SpinnyIntake` "knows" a motor and "does" `move` | TRUE NOW | `SpinnyIntake.java:51`, `:79` |
| 62-69 | `intake` is `private final` | TRUE NOW | `SpinnyIntake.java:51` |
| 198-200 | intake two-`if` bug is "real, currently-open ... marked with a `TODO` right above it" | **FALSE NOW** | fixed `1a6809a`;  `BillyMA.java:55-62` is one chain, no TODO |
| 230 | "the bug they found above is one of eighteen" | **FALSE NOW** | that bug is gone;  `grep -rn TODO` gives 17 lines = 12 distinct TODO comments (section A).  At `34c22ea` it gave 30 lines / 25 distinct, so "eighteen" was never exact |
| 234-238 (practice) | `Claw` holds a `CRServo`;  `open()` is `void`;  `servo.setPower(1)`;  `update()` has `if/else` | TRUE NOW | `Claw.java:88`, `:18-22`, `:20`, `:125-132` |

### Bugs taught

- Intake two-`if` bug:  **FIXED** `1a6809a` (code above).  A book can still show the old shape as an ILLUSTRATIVE counter-example, but must not call it current.

---

## Chapter 2 -- Extension Methods  (`Advanced/extension-methods`)

### Code blocks

| # | Lesson lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | 28-35 | `GetLeftStickX` | UNCHANGED | `Extensions/GamepadExtensions.java:40-45` |
| 2 | 72-79 | `TrySleep` | UNCHANGED (reformatted onto fewer lines) | `Extensions/ThreadExtensions.java:10-21` |
| 3 | 98-106 | `mapToIMURange` doc comment + signature | UNCHANGED | `Extensions/AngleExtensions.java:4-10` |
| 4 | 118-121 | `getDoppleganger` / `getSmol` signatures | UNCHANGED | `AngleExtensions.java:26`, `:41` |
| 5 | 141-147 | `GetBillyIntakeCommand` | ILLUSTRATIVE (counter-example) | -- |

`Extensions/GamepadExtensions.java:10-45`
```java
public final class GamepadExtensions
{
    /**
     * Java is so gross.... why can't I just have an explicitly static class ;_;
     */
    private GamepadExtensions() { }

    /**
     * Defines the size of the dead-zone for Zero on
     * a gamepad.
     */
    public static final float DeadZoneRadius = 0.05f;

    /**
     * Helper Method for Applying a standard Dead-Zone calculation to a Gamepad Axis reading.
     * @param input pass any gamepad axis here.
     * @return 0 if abs(input) < DeadZoneRadius, otherwise, input is unchanged.
     */
    public static float ApplyDeadZone(float input)
    {
        if(Math.abs(input) < DeadZoneRadius)
            return 0.0f;
        return input;
    }

    /**
     * apply dead zone to `gamepad.left_stick_x`
     * @param gamepad
     * @return value between -1 and 1 with dead zone applied.
     */
    public static float GetLeftStickX(Gamepad gamepad)
    {
        if(gamepad == null)
            return 0f;
        return ApplyDeadZone(gamepad.left_stick_x);
    }
```

`Extensions/ThreadExtensions.java:3-22`
```java
public class ThreadExtensions
{
    /**
     * Encapsulate Thread.sleep(...) in a try catch block.
     * @param millis try to sleep this long
     * @return returns false if sleep was interrupted.
     */
    public static boolean TrySleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            return false;
        }
        return true;
    }
}
```

`Extensions/AngleExtensions.java:3-58`
```java
public class AngleExtensions {
    /**
     * used when u give bad data bc ur bad
     * converts number outside of (-180, 180) range to equivalent angle within that range
     * @param degrees
     * @return angle in range (-180, 180)
     */
    public static double mapToIMURange (double degrees) {
        while(Math.abs(degrees)>180) {
            if (degrees<0)
                degrees+=360;
            else {
                degrees-=360;
            }
        }
        return degrees;
    }

    /**
     * used to find the other "name" of the same angle; helps in getSmol Calculatioins
     * @param degrees
     * @return angle in range (-360, 360)
     */
    public static double getDoppleganger(double degrees) {
        if (degrees < 0)
            return degrees+360;

        return degrees-360;
    }

    /**
     * looks at all the possible ways to get from angle A to Angle B and finds the shortest
     * possible path, aka angle with smallest magnitutde
     * @param degrees
     * @param yaw
     * @return angle in range (-180, 180);
     */

    public static double getSmol(double degrees, double yaw) {
        double yeeyaw = getDoppleganger(yaw);
        double dees = getDoppleganger(degrees);
        double[] possibleValues = new double[]{
                degrees-yaw,
                degrees-yeeyaw,
                dees-yaw,
                dees-yeeyaw
        };
        double smol=possibleValues[0];
        for (int i =1; i<4; i++) {
            if (Math.abs(possibleValues[i])<Math.abs(smol)) {
                smol = possibleValues[i];
            }
        }
        return smol;
    }
}
```

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 40-41 | doc comment says "Java is so gross.... why can't I just have an explicitly static class ;_;" | TRUE NOW | `GamepadExtensions.java:13` |
| 45-46 | the three files discussed are "the actual, current inventory of `Extensions/`" | **FALSE NOW** (and then) | `Extensions/` also holds `LimelightExtensions.java`, `IState.java`, `TurnDirection.java`.  `LimelightExtensions` is a real extension class the lesson never mentions |
| 82-83 | `TrySleep` is "used for timing turret rotations" (implied current use) | **FALSE NOW** | `Turret` sleeps removed `1a6809a`.  Current callers:  `MecanumDrive.java:50`, `ExampleAutonomousStrategies.java:15,17,20,25` |
| 62-66 | `AngleExtensions` touches no hardware/telemetry | TRUE NOW | `AngleExtensions.java:1-58` |
| 137 | no current `Extensions` file is "too narrow" | TRUE NOW (arguable) | `LimelightExtensions` is FTC-vendor specific but not robot specific |
| 139 | counter-example matches "`BillyMA`'s actual intake lambda exactly" | TRUE NOW (behaviorally) | fixed lambda `BillyMA.java:55-62` returns 0.75 / -0.75 / 0, same as the counter-example |

### Writer note

`BillyRobot.AutonomousMecanumRobot.angleWrap(double)` (`BillyRobot.java:46-51`) duplicates `AngleExtensions.mapToIMURange` with a different implementation, and is never called.  `getHeading()` (`:42-45`) is also never called.  Useful as a "should have been an extension" example if wanted.

`RobotModel/Robots/BillyRobot.java:42-51`
```java
        public double getHeading() {
            YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
            return orientation.getYaw(AngleUnit.DEGREES);
        }
        public double angleWrap(double angle)
        {
            while (angle > 180) angle -= 360;
            while (angle < -180) angle += 360;
            return angle;
        }
```

`LimelightExtensions` (now null-guarded;  see chapter 5):

`Extensions/LimelightExtensions.java:9-50`
```java
public class LimelightExtensions {
    // TODO (students): BIOBUZZ goals are AprilTag *clusters* (4 tags sharing one origin).
    // Add cluster search + origin estimation here - see doc/limelight3a/cluster-targeting-plan.md
    /**
     * *look for a specific AprilTag Id using the limelight camera
     * If the tag is not found, this method returns NULL
     * @param limelight camera configured to read Fiducials
     * @param tagId which tag id are you looking for.
     * @return The desired FiducialResult or NULL if not found.
     */
    public static LLResultTypes.FiducialResult tryGetFiducial(
            Limelight3A limelight,
            int tagId)
    {
        LLResult result = limelight.getLatestResult();
        if (result == null)
            return null;

        return result
                .getFiducialResults()
                .stream()
                .filter(fr -> fr.getFiducialId() == tagId)
                .findFirst()
                .orElse(null);
    }

    public static LLResultTypes.FiducialResult tryFindOneOf(
            Limelight3A limelight,
            int... tags)
    {
        LLResult result = limelight.getLatestResult();
        if (result == null)
            return null;

        return result
                .getFiducialResults()
                .stream()
                .filter(fr ->  Arrays.stream(tags).anyMatch(i -> i == fr.getFiducialId()))
                .findFirst()
                .orElse(null);
    }
}
```

### Bugs taught

None in this lesson.

---

## Chapter 3 -- The FTC Hardware Toolbox  (`GettingStarted/02`, minus "Reuse first" / "two kinds")

### Code blocks

| # | Lesson lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | 29-31 | `intake = hardwareMap.get(DcMotor.class, "intakeMotor");` | ILLUSTRATIVE (merges `SpinnyIntake.java:63`, which uses `motorName`, with the literal from `BillyMA.java:53`) | -- |
| 2 | 45-49 | `DcMotor` API lines | ILLUSTRATIVE | -- |
| 3 | 73-76 | `Servo` API lines | ILLUSTRATIVE (values match `PusherServo.java:17`, `:52`) | -- |
| 4 | 92-96 | `TouchSensor` / `DigitalChannel` | ILLUSTRATIVE | -- |
| 5 | 105-107 | `DistanceSensor` | ILLUSTRATIVE | -- |
| 6 | 116-120 | `NormalizedColorSensor` | ILLUSTRATIVE | -- |
| 7 | 129-137 | IMU init + yaw | ILLUSTRATIVE (close to `BillyRobot.java:106-116`, `:43-44`, not verbatim) | below |
| 8 | 180-184 | SDK sample `leftDrive` | ILLUSTRATIVE (SDK style) | -- |
| 9 | 188-191 | `robot.update(gamepad1, gamepad2);` | UNCHANGED | `OpModes/BillyTeleOpBLUE.java:24` |

The real IMU setup a writer should quote:

`RobotModel/Robots/BillyRobot.java:106-116`
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

The MecanumDrive lookup that skips initialization (still open):

`RobotModel/DriveTrain/Mecanum/MecanumDrive.java:174-174`
```java
        imu = hardwareMap.get(IMU.class, "imu");
```

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 52-55 | `MecanumDrive.OrientationConfiguration` takes all four directions as ctor args | TRUE NOW | `MecanumDrive.java:116-143` |
| 68-69 | `DoubleShooter` uses plain power;  driver trims with dpad | TRUE NOW | `DoubleShooter.java:17-18` (`DcMotor`);  `BillyMA.java:80-86`.  Note dpad up/down **also** drives the intake on the same gamepad (`BillyMA.java:55-58`) |
| 78-80 | `PusherServo` wraps a `Servo`;  `0.8` = pushed;  auton API says `pushBalls()` | TRUE NOW | `PusherServo.java:41`, `:16-19` |
| 83 | `Turret` and `Claw` both wrap a `CRServo` | TRUE NOW | `Turret.java:76`, `Claw.java:88` |
| 99-101 | `DoublyLimitedMotor` uses two `TouchSensor`s as travel limits | TRUE NOW | `DoublyLimitedMotor.java:47-48`, `:72-73` |
| 143-146 | `MecanumDrive` fetches the IMU without `initialize`;  `BillyRobot` initializes it;  `Wildbots2025` reads an unconfigured IMU;  marked `TODO` | TRUE NOW | `MecanumDrive.java:158-174`;  `BillyRobot.java:106-116`;  `Wildbots2025.java:38-58` |
| 155-157, 206 | `BallDetectionComponent` is the team's webcam + two `ColorBlobLocatorProcessor` example | **FALSE NOW** (and never true in this repo) | no such file;  `git log --all -- '*BallDetectionComponent*'` is empty.  Still named in `MechComponent.java:40-42` and `doc/Migration-2026-27.md:31` |
| 205 | "four `DcMotor` + `IMU` -> `MecanumDrive`, `StandardTankDrive`" | **FALSE NOW** for `StandardTankDrive` | `StandardTankDrive.java:56`, `:64-69`:  two `DcMotor`, no IMU.  `OneStickTank` (two motors) is omitted from the table |
| 207 | `Limelight3A` held by `BillyRobot` itself | TRUE NOW | `BillyRobot.java:61`, `:101-104` |
| 350 | `doc/NewRobotDesignWorkflow.md` has the full templates | TRUE NOW | `doc/NewRobotDesignWorkflow.md:337-546` |

### Bugs taught

- IMU ownership (`MecanumDrive` ctor TODO):  **OPEN** (`MecanumDrive.java:158-174`).

---

## Chapter 4 -- Sensors and Where They Live  (`GettingStarted/03`)

### Code blocks

| # | Lesson lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | 30-33 | command vs. question | ILLUSTRATIVE | -- |
| 2 | 64-69 | `canGoForward()` | UNCHANGED | `DoublyLimitedMotor.java:136-139` |
| 3 | 84-87 | hysteresis | ILLUSTRATIVE | -- |
| 4 | 117-124 | `DoublyLimitedMotor.setPower` | UNCHANGED (reformatted) | `DoublyLimitedMotor.java:93-100` |

`RobotModel/Mechs/Components/DoublyLimitedMotor.java:132-148`
```java
    /**
     *
     * @return true if the forward sensor is not pressed
     */
    public boolean canGoForward()
    {
        return ! forwardSensor.isPressed();
    }

    /**
     *
     * @return true if the reverse sensor is not pressed
     */
    public boolean canGoReverse()
    {
        return ! reverseSensor.isPressed();
    }
```

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 39-42 | `BillyRapidFire` spins up, waits 2200 ms, then fires | TRUE NOW | `AutonStrategies/BillyRapidFire.java:86-92` |
| 89-91 | `±2` deadband in `LimelightAutoTarget`;  `0.05` stick deadzone | TRUE NOW | `LimelightAutoTarget.java:122-124`;  `GamepadExtensions.java:21` |
| 98-101 | Limelight returns `null` before a frame;  "Lesson 4 has a live example of exactly that bug" | **FALSE NOW** (the live bug) | `LimelightExtensions.java:23-25` now returns `null` on a `null` result (`1a6809a`).  Also UNVERIFIED whether `getLatestResult()` can return `null` at all in SDK 12.0.0:  `doc/limelight3a/api.md:94` says in 11.2.1 it "synthesizes an empty invalid result when its cache is null" |
| 131-132 | no assembly owns a sensor | TRUE NOW | `MechAssembly.java:27-33` (TODO);  no `hardwareMap.get` of a sensor in any assembly |
| 141-142 | the 2200 ms guess could be replaced by a seated-ball signal | TRUE NOW (as a proposal) | `MechAssembly.java:29-33` |
| 146-150 | `BillyRobot` owns, configures, and passes the Limelight into `LimelightAutoTarget`;  `Turret` knows no camera | TRUE NOW | `BillyRobot.java:101-104`, `:127-130`;  `Turret.java` has no Limelight reference |
| 152-156 | IMU done wrong:  `MecanumDrive` own copy, `BillyRobot` another, `Wildbots2025` none;  `TODO` | TRUE NOW | `MecanumDrive.java:158-174`;  `Robot.java:25-29` says the same |
| 167-170 | `Turret` needs limits, `Claw` needs grip sensor, both `TODO`s | TRUE NOW | `Turret.java:47-75`;  `Claw.java:58-87` |
| 174-176 | `MecanumDrive` uses the IMU only for `turnToAngle()`;  teleop ignores it;  two projects marked `TODO` | TRUE NOW (plus yaw telemetry at `MecanumDrive.java:286`) | `MecanumDrive.java:199-231` |

### Bugs taught

- Limelight `null` crash (forward reference):  **FIXED** `1a6809a` (see chapter 5).
- IMU ownership:  **OPEN**.

---

## Chapter 5 -- AprilTags and the Limelight  (`GettingStarted/04`)

### Code blocks

| # | Lesson lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | 58-63 | Limelight setup in `BillyRobot` | UNCHANGED | `BillyRobot.java:101-104` |
| 2 | 78-87 | `LLResult` / fiducial loop | ILLUSTRATIVE (SDK-style;  not in team code) | -- |
| 3 | 103-107 | `power = 1; if (tx > -10) power = -tx / 10.0;` | UNCHANGED | `LimelightAutoTarget.java:79-81` |
| 4 | 122-133 | `tryGetFiducial` | **CHANGED** | below |
| 5 | 162-177 | `lookForTag()` | UNCHANGED (reformatted) | `LimelightAutoTarget.java:108-126` |
| 6 | 187-190 | `rotateCCW` / `rotateCW` one-liners | **CHANGED** (`rotateCW` default is now `-1`) | below |
| 7 | 201-205 | webcam `easyCreateWithDefaults` | ILLUSTRATIVE (SDK;  `ConceptAprilTagEasy.java:126-133`) | -- |
| 8 | 209-218 | `AprilTagDetection` loop using `detection.id`, `detection.metadata` | ILLUSTRATIVE, and **stale against SDK 12.0** | `README.md:65-89`:  v12 moved per-tag fields to `AprilTagSingleDetection`;  samples now cast (`ConceptAprilTag.java:225-239`).  `detection.id` will not compile.  UNVERIFIED against the jar (not in the gradle cache) |
| 9 | 230-232 | `aprilTag = AprilTagProcessor.easyCreateWithDefaults();` in `BillyRobot` | **REMOVED** `1a6809a` | `BillyRobot.java` has no `AprilTagProcessor` |

Block 4, current:

`Extensions/LimelightExtensions.java:19-33`
```java
    public static LLResultTypes.FiducialResult tryGetFiducial(
            Limelight3A limelight,
            int tagId)
    {
        LLResult result = limelight.getLatestResult();
        if (result == null)
            return null;

        return result
                .getFiducialResults()
                .stream()
                .filter(fr -> fr.getFiducialId() == tagId)
                .findFirst()
                .orElse(null);
    }
```

Block 6, current:

`AutonStrategies/LimelightAutoTarget.java:76-97`
```java
    public IState rotateCCW(double tx){
        return () ->
        {
            double power = 1;
            if(tx > -10)
                power = -tx / 10.0;
            turret.setPower(power);
            turret.reportData("Turret CCW", power);
            return lookForTag();
        };
    }
    public IState rotateCW(double tx) {
        return () ->
        {
            double power = -1;
            if (tx < 10)
                power = -tx / 10.0;
            turret.setPower(power);
            turret.reportData("Turret CW", power);
            return lookForTag();
        };
    }
```

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 7-8, 284-285 | lesson "ends on two real, open bugs" | **FALSE NOW** | both fixed `1a6809a` |
| 50-52 | `BillyRobot` holds both a `Limelight3A` and an unattached `AprilTagProcessor` | **FALSE NOW** | processor removed `1a6809a`.  `Robot.visionPortal` field still exists, never assigned (`Robot.java:111`) |
| 148-152 | `tryGetFiducial` NPEs on a `null` result;  marked `TODO` | **FALSE NOW** | guarded, `LimelightExtensions.java:23-25`;  TODO deleted.  Note:  it does **not** check `isValid()`, which the lesson (and cluster plan Task 1) names as part of the fix |
| 152-153 (fix) | SDK sample guards with `result != null && result.isValid()` | **FALSE** (misquote) | `SensorLimelight3A.java:102-103` checks only `result.isValid()` |
| 193-195 | `rotateCW` sign flip, marked `TODO` | **FALSE NOW** | fixed `1a6809a`:  `LimelightAutoTarget.java:90` is `double power = -1;` |
| 182-184 | `turret.setPower(...)` / `turret.reportData(...)` go through the turret's autonomous behaviors | TRUE NOW | `LimelightAutoTarget.java:28`, `:82-83` |
| 234-237 | `visionPortal` stays null;  `aprilTag` `TODO` | `visionPortal` null TRUE NOW;  `aprilTag` part **FALSE NOW** | `Robot.java:111` |
| 246-248 | three OpModes carry `TARGET_TAG_ID = -1` with a `TODO` | TRUE NOW | `BillyTeleOpBLUE.java:11-12`, `BillyTeleOpRED.java:11-12`, `ExampleStateMachineAuton.java:19-21` |
| 266-268 | mid-match crash -> look at unguarded `null` | **FALSE NOW** | guarded |

### Bugs taught

- `tryGetFiducial` null crash:  **FIXED** `1a6809a` (no `isValid()` check added).
- `rotateCW` sign:  **FIXED** `1a6809a`.  Trace now:  `tx > 2` -> `rotateCW`;  `tx >= 10` -> `-1`;  `2 < tx < 10` -> `-tx/10` (negative).  Both branches agree.
- Unattached `AprilTagProcessor`:  **CHANGED SHAPE** -- resolved by deletion, not by adding a portal.

### Writer note

With `TARGET_TAG_ID = -1` (all three OpModes), `lookForTag()` never finds a tag, so every loop it reports `"Tag -1 not found."`, goes to `stopTurret()`, and writes `"Turret: stopped"` and `"Turret Stopped"`.  The targeter is started by default (`BillyRobot.java:161`), so in TeleOp the turret is held at 0 and the manual bumpers are ignored until the driver presses `gamepad1.y` (chapter 13).

---

## Chapter 6 -- The Layered Robot Model  (`01`)

### Code blocks

| # | Lesson lines | Content | Verdict |
|---|---|---|---|
| 1 | 14-22 | layer diagram | ILLUSTRATIVE (TRUE NOW as a description) |
| 2 | 40-45 | autonomous twin diagram | ILLUSTRATIVE (TRUE NOW) |

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 27 | `Robot` holds one `DriveTrain`, one `MechAssembly`, plus robot-wide sensors | TRUE NOW | `Robot.java:112-113`;  `BillyRobot.java:61-62` |
| 24, 32 | each layer talks only to the layer directly below | Mostly TRUE NOW;  one standing exception | `BillyRobot.java:127-130` hands `LimelightAutoTarget` a **component's** autonomous object (`autonTurret`) two layers down.  New since the lessons:  `BillyRobot` also calls `BillyMA`'s macro API (`requestRapidFire`, `cancelMacro`, `macroClaims`, `macroName`, `giveInstructions(gamepad, robotClaims)`), which is one layer down and fine |
| 34 | `BillyRobot` = `MecanumDrive` + `BillyMA`;  `Wildbots2025` = `MecanumDrive` + `CascadeArm` | TRUE NOW | `BillyRobot.java:92-99`;  `Wildbots2025.java:41-50` |
| 56 | `robot.getAutonomousRobot().mechAssembly.autonFlywheel.setPower(0.6)` is a real path | TRUE NOW | `ExampleStateMachineAuton.java:133-136` |

New fact a writer should add to the model:  the strategy layers now have **declared** types above the component layer (`MechAssembly.IMacroAssemblyStrategy<R>`, `Robot.IAssistRobotStrategy<R>`;  section B).

---

## Chapter 7 -- Components  (`02`, plus `GettingStarted/02` "Reuse first" and "The two kinds of component")

### Code blocks

| # | Source:lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | `02`:12-33 | `MechComponent` contract | UNCHANGED (comments dropped) | `MechComponent.java:58-117` (section C) |
| 2 | `02`:48-82 | `SpinnyIntake` | **CHANGED** (`startIntake` / `reverseIntake` signs swapped) | below |
| 3 | `GS02`:227-233 | `SpinnyIntake` built twice | UNCHANGED (abridged) | `BillyMA.java:53`, `ExampleIntakeAssembly.java:35` |

Block 2, current (full file is short):

`RobotModel/Mechs/Components/SpinnyIntake.java:9-93`
```java
public class SpinnyIntake extends MechComponent
{
    public class AutonomousIntakeBehaviors extends MechComponent.AutonomousComponentBehaviors
    {
        public AutonomousIntakeBehaviors(Telemetry telemetry) {
            super(telemetry);
        }

        public void startIntake()
        {
            intake.setPower(1);
            reportStatus("Intake: running");
        }

        public void stopIntake()
        {
            intake.setPower(0);
            reportStatus("Intake: stopped");
        }

        public void reverseIntake()
        {
            intake.setPower(-1);
            reportStatus("Intake: reversed");
        }
    }


    public interface SpinnyIntakeControlStrategy extends IControlStrategy
    {
        /**
         * abstract definition of how a Spinny Intake gets controlled.
         * this Implementation should translate the Gamepad Input into motor movement
         * of the Spinny Intake.
         * The name of this method does not matter at all~ so make it descriptive in the
         * context of the Spinny Intake!
         * @param motor the `intake` motor will be passed here.
         * @param gamepad the gamepad will be passed down from the MechAssembly to this strategy
         */
        void nomNomNom(DcMotor motor, Gamepad gamepad);
    }

    private final DcMotor intake;

    protected SpinnyIntakeControlStrategy strategy;

    private AutonomousIntakeBehaviors auton;

    public SpinnyIntake(
            HardwareMap hardwareMap,
            String motorName,
            SpinnyIntakeControlStrategy strategy)
    {
        super(strategy);
        intake = hardwareMap.get(DcMotor.class, motorName);
        this.strategy = (SpinnyIntakeControlStrategy) super.strategy;
    }

    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousIntakeBehaviors(telemetry);
    }

    @Override
    public AutonomousIntakeBehaviors getAutonomousBehaviors() {
        return auton;
    }

    @Override
    public void move(Gamepad gamepad)
    {
        strategy.nomNomNom(intake, gamepad);
    }

    /**
     * Writes intake power and encoder position to the buffer. Previously
     * empty (Susan Zuo — Bug #3: "No telemetry data reported"). Never flushes.
     */
    @Override
    public void update() {
        telemetry.addData("intake power:", intake.getPower());
        telemetry.addData("intake position:", intake.getCurrentPosition());
    }
}
```

Block 3, current:

`RobotModel/Mechs/Assemblies/ExampleIntakeAssembly.java:35-43`
```java
        intake = new SpinnyIntake(hardwareMap, "spinner",
                (motor, gamepad) -> {
                       if(gamepad.a)
                           motor.setPower(1);
                       else if(gamepad.b)
                           motor.setPower(-1);
                       else
                           motor.setPower(0);
                });
```

### Claims

| Source:line | Claim | Verdict | Evidence |
|---|---|---|---|
| `02`:37 | ctor takes an `IControlStrategy` | TRUE NOW | `MechComponent.java:84-87` |
| `02`:43-44 | ctor does not take `Telemetry`;  `initializeTelemetry` is separate | TRUE NOW | `MechComponent.java:84`, `:99-102` |
| `02`:44 | only `Robot.updateTelemetry()` calls `telemetry.update()` | TRUE NOW inside `RobotModel` | `Robot.java:143`.  OpModes also flush once before the loop (`BillyTeleOpBLUE.java:20` etc.).  `BillyRobot` now overrides `updateTelemetry()` but still flushes only via `super` (`BillyRobot.java:178-189`) |
| `02`:86 | `SpinnyIntake` owns exactly one `DcMotor` | TRUE NOW | `SpinnyIntake.java:51` |
| `02`:88 | auton exposes `startIntake/stopIntake/reverseIntake` | TRUE NOW | `SpinnyIntake.java:17-33` |
| `GS02`:224-236 | `SpinnyIntake` reused in two assemblies, no new code | TRUE NOW | above |
| `GS02`:266-274 | inventory table, incl. `BallDetectionComponent` | **FALSE NOW** for `BallDetectionComponent` (does not exist);  other six rows TRUE NOW | `Components/` holds exactly:  `Claw, DoubleShooter, DoublyLimitedMotor, MechComponent, PusherServo, SpinnyIntake, Turret` |
| `GS02`:279-282 | `Claw` vs `Turret` vocabulary:  `open/close` vs `turnCW/turnCCW/setPower` | TRUE NOW (both also have `stop()`) | `Claw.java:18-36`;  `Turret.java:18-25` |
| `GS02`:284-286 | same telemetry bug and same two-phase fix repaired separately in each | TRUE NOW | `Claw.java:104-112`, `:119-132`;  `Turret.java:92-102`, `:109-122` |
| `GS02`:291-299 | turret limits / claw grip sensor marked `TODO` | TRUE NOW | `Turret.java:47-75`;  `Claw.java:58-87` |
| `GS02`:312-315 | naming convention open, `TODO` on `MechComponent` | TRUE NOW | `MechComponent.java:30-45` |

Current rule text the chapter teaches (verbatim javadoc):

`RobotModel/Mechs/Components/MechComponent.java:10-45`
```java
 * <p><b>What belongs in a component.</b> Every concrete MechComponent should be
 * exactly one of two things:
 *
 * <ol>
 *   <li><b>A wrapper around a single output device</b> - one motor, one servo,
 *       one CR servo. The class adds the strategy seam, the autonomous verbs,
 *       and telemetry, and nothing else. We need exactly <em>one</em> of these
 *       per device type, and it should be named for the <em>device</em>, since
 *       any mechanism built on that device reuses it as-is.</li>
 *   <li><b>A purposeful collection of output devices plus the sensors that
 *       guide them</b> - a motor bounded by two limit switches, two motors
 *       driven as an opposed pair, a camera and the processors reading it.
 *       These are named for what the collection <em>does</em>, because the
 *       coupling between the outputs and the sensors is the whole point.</li>
 * </ol>
 *
 * <p>A class that is neither - a second single-device wrapper named after one
 * robot's job for it - is duplication, and every fix to one is a fix owed to
 * the other.
 *
 * <p>TODO: the current components have yet to be sorted against that rule, and
 *  the naming convention for category 1 is still an open question. As it stands:
 *  <ul>
 *    <li>{@code SpinnyIntake} (one DcMotor), {@code PusherServo} (one Servo),
 *        {@code Claw} and {@code Turret} (one CRServo <em>each</em>) are all
 *        category 1, and all four are named for a job rather than a device.</li>
 *    <li>{@code Claw} and {@code Turret} are therefore two wrappers of the same
 *        device. One device-named class should serve both - until each grows the
 *        sensors it needs, at which point they become category 2 and separate
 *        classes are earned. See the TODOs on both files.</li>
 *    <li>{@code DoublyLimitedMotor}, {@code DoubleShooter} and
 *        {@code BallDetectionComponent} are already category 2 and already named
 *        for what they do.</li>
 *  </ul>
 *  Settle the category-1 naming convention before renaming anything, since every
 *  assembly's construction code moves with it.
```

### Bugs taught

- `SpinnyIntake` auton signs vs. teleop:  **FIXED** `1a6809a` (`startIntake` = `+1`, `reverseIntake` = `-1`, matching dpad_up = `+0.75`).
- `Claw`/`Turret` duplication, missing sensors, public `servo` field, raw-servo strategy signature:  **OPEN** (`Claw.java:58-88`, `Turret.java:47-76`, `MechComponent.java:30-45`).

---

## Chapter 8 -- Control Strategies  (`03`)

### Code blocks

| # | Lesson lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | 20-42 | `BillyMA` intake / pusher / turret constructions | **CHANGED** (intake chain fixed;  `setDirection` removed from pusher lambda) | below |
| 2 | 54-64 | `DoublyLimitedMotor.setPower` + `canGo*` | UNCHANGED (reformatted) | `DoublyLimitedMotor.java:93-100`, `:136-148` |

Block 1, current:

`RobotModel/Mechs/Assemblies/BillyMA.java:52-115`
```java
    public BillyMA(HardwareMap hardwareMap) {
        intake = new SpinnyIntake(hardwareMap, "intakeMotor",
                (motor, gamepad) -> {
                    if (gamepad.dpad_up) {
                    motor.setPower(0.75); }
                    else if (gamepad.dpad_down) {
                    motor.setPower(-0.75);
                    } 
                    else {
                        motor.setPower(0);
                    }
                });

        ballPusher = new PusherServo(hardwareMap,
                "ballPusherServo",
                (servoR, gamepad) -> {
                    if (gamepad.x) {
                        servoR.setPosition(0.8);
                    } else {
                        servoR.setPosition(0.0);
                    }
                },
                (servo, telemetry) -> {
                    telemetry.addData("pusher position", servo.getPosition());
                });

        flywheel = new DoubleShooter(hardwareMap, "flywheelMotorF", "flywheelMotorB",
                (motorF, motorB,gamepad) -> {
                    double power = 0.45;
                    if (gamepad.dpad_up) {
                        power += 0.05;
                    }
                    if (gamepad.dpad_down) {
                        power -= 0.05;
                    }

                    if (gamepad.y) {
                        motorF.setPower(power);
                        motorB.setPower(-power);
                    } else {
                        motorF.setPower(0);
                        motorB.setPower(0);
                    }
                }, (motorF, motorB, telemetry) -> {
                    telemetry.addData("power:", motorF.getPower());
                });

        turret = new Turret(hardwareMap, "turretServo",
                (servo, gamepad) -> {
                    if (gamepad.right_bumper)
                    {
                        servo.setPower(-1);
                    }
                    else if (gamepad.left_bumper)
                    {
                        servo.setPower(1);
                    }
                    else {
                        servo.setPower(0);
                    }
                },
                (servo, telemetry) -> {
                    telemetry.addData("turret position", servo.getPower());
                });
```

Pusher direction now set once, in the component constructor:

`RobotModel/Mechs/Components/PusherServo.java:46-55`
```java
    public PusherServo(HardwareMap hardwareMap,
                       String servoNameR,
                       BallPusherControlStrategy strategy,
                       PusherTelemetryStrategy telemetryStrategy) {
        super(strategy);
        this.servoR = hardwareMap.get(Servo.class, servoNameR);
        this.servoR.setDirection(Servo.Direction.REVERSE);
        this.strategy = strategy;
        this.telemetryStrategy = telemetryStrategy;
    }
```

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 44 | "two of those three lambdas contain real, known bugs ... marked with `TODO`" | **FALSE NOW** | both fixed `1a6809a`;  no TODOs remain in `BillyMA.java` |
| 46 | three classes, each its own strategy interface | TRUE NOW | `SpinnyIntake.java:37-49`;  `PusherServo.java:32-34`;  `Turret.java:37-40` |
| 66 | `DoublyLimitedMotor.setPower` filters every caller | TRUE NOW | `DoublyLimitedMotor.java:93-100`;  auton verbs call it, `:19-35` |
| 70 | example "don't let the turret spin while rapid fire owns the shooter" is assembly-level | Still a valid example, but note the **current** design:  rapid fire claims `PUSHER` and `FLYWHEEL` only, not `TURRET` | `BillyRapidFire.java:43-47` |

### Bugs taught

- Intake two-`if`:  **FIXED** `1a6809a`.
- Pusher `setDirection` inside teleop lambda:  **FIXED** `1a6809a` (moved to `PusherServo.java:52`).

---

## Chapter 9 -- Assemblies  (`04`)

### Code blocks

| # | Lesson lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | 14-30 | `MechAssembly` contract | **CHANGED** (new public `IMacroAssemblyStrategy<R>`;  lesson also omits the `strategy` and `telemetry` fields) | below |
| 2 | 40-55 | `BillyMA.initializeTelemetry` | **CHANGED** (lesson omits the last line, `BRF = new BillyRapidFire(auton, 3);`) | below |
| 3 | 59-67 | `AutonomousBillyMA` fields | UNCHANGED | `BillyMA.java:131-149` |
| 4 | 75-79 | empty `BillyMA.updateTelemetry()` | **CHANGED** (filled in) | below |

Block 1, current (class body;  long javadoc at 9-55 omitted):

`RobotModel/Mechs/Assemblies/MechAssembly.java:56-113`
```java
public abstract class MechAssembly
{
    protected interface IAssemblyStrategy { }
    
    public interface IMacroAssemblyStrategy<R>
    { 
        Set<R> claims();
        void start();
        void update(Gamepad gamepad);
        boolean isComplete();
        void cancel();
        String name();
    }
    protected MechAssembly.IAssemblyStrategy strategy;

    /**
     * Autonomous-facing surface for this assembly. Provides
     * {@link #reportStatus} and {@link #reportData} so autonomous strategies
     * do not need a raw {@code Telemetry} parameter (Susan Zuo — fixes
     * Bug #6: "Autonomous code forced to bypass the object model").
     */
    public abstract class AutonomousMechBehaviors {
        protected final Telemetry telemetry;
        public AutonomousMechBehaviors(Telemetry telemetry) {
            this.telemetry = telemetry;
        }
        /** Adds a status line to the telemetry buffer. Does NOT flush. */
        public void reportStatus(String status) { telemetry.addLine(status); }
        /** Adds a key-value pair to the telemetry buffer. Does NOT flush. */
        public void reportData(String key, Object value) { telemetry.addData(key, value); }
    }
    public abstract <T extends AutonomousMechBehaviors> T getAutonomousBehaviors();

    /**
     * Pass along gamepad instructions to subcomponents.
     * @param gamepad the Gamepad
     */
    public abstract void giveInstructions(Gamepad gamepad);

    protected Telemetry telemetry;

    /**
     * Stores the telemetry reference, propagates it to every child component,
     * then creates the autonomous behavior object. Called once by
     * {@code Robot.initializeSubsystems()} (Susan Zuo — two-phase
     * initialization: "construct → initializeTelemetry").
     */
    public abstract void initializeTelemetry(Telemetry telemetry);

    /**
     * Write assembly and component state to the owned telemetry buffer. Must
     * call {@code update()} on every child component so no component is
     * silently missing from the display (Susan Zuo — fixes Bug #5:
     * "Assemblies fail to report telemetry from all their child components").
     * Never calls {@code telemetry.update()}.
     */
    public abstract void updateTelemetry();
}
```

Block 2, current:

`RobotModel/Mechs/Assemblies/BillyMA.java:161-175`
```java
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
        intake.initializeTelemetry(telemetry);
        ballPusher.initializeTelemetry(telemetry);
        flywheel.initializeTelemetry(telemetry);
        turret.initializeTelemetry(telemetry);
        auton = new AutonomousBillyMA(
                intake.getAutonomousBehaviors(),
                ballPusher.getAutonomousBehaviors(),
                flywheel.getAutonomousBehaviors(),
                turret.getAutonomousBehaviors(),
                telemetry);
        BRF = new BillyRapidFire(auton, 3);
    }
```

Block 4, current:

`RobotModel/Mechs/Assemblies/BillyMA.java:211-218`
```java
    @Override
    public void updateTelemetry() {
        telemetry.addLine("--- BillyMA ---");
        intake.update();
        ballPusher.update();
        flywheel.update();
        turret.update();
    }
```

The two "correct shape" examples the lesson points at:

`RobotModel/Mechs/Assemblies/CascadeArm.java:119-125`
```java
    @Override
    public void updateTelemetry() {
        telemetry.addLine("--- Cascade Arm ---");
        claw.update();
        cascade.update();
        drawbridge.update();
    }
```

`RobotModel/Mechs/Assemblies/ExampleIntakeAssembly.java:80-84`
```java
    @Override
    public void updateTelemetry() {
        claw.update();
        intake.update();
    }
```

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 38 | `BillyMA` owns four components | TRUE NOW | `BillyMA.java:44-47` |
| 69 | `AutonomousBillyMA` is pure aggregation | TRUE NOW | `BillyMA.java:131-149` |
| 73-81 | `BillyMA.updateTelemetry()` is "the real, current code" and empty | **FALSE NOW** | fixed `1a6809a`, `BillyMA.java:211-218` |
| 83 | `MechComponent.update()` is public | TRUE NOW | `MechComponent.java:116` |
| 83 | `CascadeArm` / `ExampleIntakeAssembly` show the correct shape | TRUE NOW | above (`ExampleIntakeAssembly` writes no header line) |
| 91 | adding a 5th component means editing two methods | TRUE NOW;  for `BillyMA` it is now **four** places:  ctor, `initializeTelemetry`, the claims-aware strategy (`BillyMA.java:117-128`), `updateTelemetry` -- plus a new `Part` constant (`:25-30`) if it can be claimed |

### Bugs taught

- Empty `BillyMA.updateTelemetry()`:  **FIXED** `1a6809a`.
- `MechAssembly` "no assembly owns a sensor" TODO:  **OPEN** (`MechAssembly.java:27-46`).

### Writer note

`BillyMA` declares `protected BillyAssemblyStrategy strategy;` (`BillyMA.java:50`), which shadows `MechAssembly.strategy` (`MechAssembly.java:69`).  The base field is never assigned.  The removed `telemetry` shadow was the same pattern (TelemetryContract checklist, `doc/TelemetryContract.md:172-173`).

---

## Chapter 10 -- The Robot Layer  (`06`)

### Code blocks

| # | Lesson lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | 12-38 | `Robot` contract | UNCHANGED for the members shown (abridged);  but `Robot` now also has `IAssistRobotStrategy<R>` | `Robot.java:52-156` (section C) |
| 2 | 48-53 | `Robot.update` | UNCHANGED | `Robot.java:151-155` |
| 3 | 57-68 | `BillyRobot` strategy calling `super.update(...)` then `targeter.updateState()` | **CHANGED** | below |
| 4 | 76-81 | `auton = new BillyRobot.AutonomousMecanumRobot(...)` | UNCHANGED | `BillyRobot.java:122-125` |

Block 3, current:

`RobotModel/Robots/BillyRobot.java:132-176`
```java
        strategy = (robot, gamepad1, gamepad2)  -> {
            if (gamepad1.yWasPressed()) {
                if (assist == null)
                    startAssist(targeter);
                else
                    stopAssist("turned off by y");
            }

            BillyMA mech = (BillyMA)mechAssembly;
            if (gamepad2.aWasPressed()) {
                mech.requestRapidFire();
                if (overlaps(mech.macroClaims(), assistClaims()))
                    stopAssist("replaced by " + mech.macroName());
            }

            if (gamepad2.bWasPressed()) {
                mech.cancelMacro("cancelled by b");
            }

            if(gamepad1.bWasPressed()) {
                stopAll();
            }

            driveTrain.drive(gamepad1);
            mech.giveInstructions(gamepad2, assistClaims());
            if (assist != null) {
                assist.update(gamepad1, gamepad2);
            }
        };
        startAssist(targeter);
    }

    public void stopAll() {
        ((BillyMA)mechAssembly).cancelMacro("stopped by stop all");
        stopAssist("stopped by stop all");
    }

    private boolean overlaps(Set<BillyMA.Part> a, Set<BillyMA.Part> b) {
        return !Collections.disjoint(a, b);
    }

    @Override
    public void update(Gamepad gamepad1, Gamepad gamepad2) {
       strategy.execute(this, gamepad1, gamepad2);
    }
```

`BillyRobot` now also overrides `updateTelemetry()` (calls `super`, which flushes):

`RobotModel/Robots/BillyRobot.java:178-189`
```java
    @Override
    public void updateTelemetry() {
        BillyMA mech = (BillyMA) mechAssembly;
        if (assist == null)
            telemetry.addData("Assist", "None");
        else
            telemetry.addData("Assist", assist.name() + " " + assist.claims());
        telemetry.addData("Macro", mech.macroName() + " " + mech.macroClaims());
        telemetry.addData("Last macro", mech.lastMacroEnd());
        telemetry.addData("Last assist", lastAssistEnd);
        super.updateTelemetry();
    }
```

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 40 | `update` and `updateTelemetry` are concrete on `Robot` | TRUE NOW | `Robot.java:140-155` |
| 44 | `Robot.updateTelemetry()` is the only flush point | TRUE NOW (in `RobotModel`) | `Robot.java:143` |
| 55, 70 | `BillyRobot` builds around the base algorithm via `super.update(...)`;  that line "proves" a template method | **FALSE NOW** | `BillyRobot.java:132-160` calls `driveTrain.drive(gamepad1)` and `mech.giveInstructions(gamepad2, assistClaims())` itself.  No subclass of `Robot` calls `super.update` anymore |
| 74 | ctor builds `MecanumDrive`, `BillyMA`, Limelight, IMU, calls `initializeSubsystems()`, builds `AutonomousMecanumRobot` | TRUE NOW, incomplete | it then builds `targeter` (`:127-130`), the strategy (`:132-160`), and calls `startAssist(targeter)` (`:161`) |
| 91 | "If `BillyRobot` had written its own `update()` from scratch ... without `super.update()`" | this hypothetical **is now the real code** | `BillyRobot.java:155-156` |
| 93 | add a second assist "after `super.update(...)`, same shape as `targeter.updateState()`" | **FALSE NOW** (shape changed) | a second assist would be an `IAssistRobotStrategy<BillyMA.Part>` passed to `startAssist(...)`;  only one assist slot exists (`BillyRobot.java:65`) |
| 97 | orchestration order is the same for every robot, so it lives in the base | TRUE NOW for `StandardTankRobot`, `OneStickTankRobot`, `Wildbots2025` (none override `update`);  **not** for `BillyRobot` |

### Writer note

`BillyRobot` declares `protected BillyRobotStrategy strategy;` (`BillyRobot.java:67`), shadowing `Robot.strategy` (`Robot.java:57`).  The lambda ignores its `robot` parameter and uses captured fields.

---

## Chapter 11 -- Autonomous Verbs  (`08`)

### Code blocks

| # | Lesson lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | 25-43 | three `initializeTelemetry` / auton builds | UNCHANGED (with `...`) | `SpinnyIntake.java:67-71`;  `BillyMA.java:161-175`;  `BillyRobot.java:120-125` |
| 2 | 51-53 | `mechAssembly.autonFlywheel.setPower(0.6);` | UNCHANGED (one-lined) | `BillyRapidFire.java:87-89` |
| 3 | 57-61 | three `robot.getAutonomousRobot()...` calls | UNCHANGED (one-lined, from lambdas) | `ExampleStateMachineAuton.java:96-98`, `:133-136`, `:140-143`, `:103-105` |

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 12 | every layer exposes an autonomous type | TRUE NOW | `MechComponent.java:68-78`;  `MechAssembly.java:77-87`;  `DriveTrain.java:26-36`;  `Robot.java:75-98` |
| 16 | `SpinnyIntake` verbs | TRUE NOW | `SpinnyIntake.java:17-33` |
| 74 | teleop methods need a gamepad;  autonomous verbs need nothing | TRUE NOW for verbs.  New nuance:  the strategy interfaces (`IMacroAssemblyStrategy.update(Gamepad)`, `IAssistRobotStrategy.update(Gamepad, Gamepad)`) take gamepads, though both current implementations ignore them (`BillyRapidFire.java:55-59`;  `LimelightAutoTarget.java:63-67`) |
| 84 | `robot.getAutonomousRobot().mechAssembly.autonTurret.stop()` is a real path | TRUE NOW | `BillyRobot.java:30`;  `BillyMA.java:135`;  `Turret.java:25` |

Current verb sets (for reference):

`RobotModel/Mechs/Components/Turret.java:12-26`
```java
    public class AutonomousTurretBehaviors extends AutonomousComponentBehaviors
    {
        public AutonomousTurretBehaviors(Telemetry telemetry) {
            super(telemetry);
        }

        public void setPower(double power)
        {
            servo.setPower(power);
            reportData("Turret power", power);
        }
        public void turnCCW() { servo.setPower(1); reportStatus("Turret: CCW"); }
        public void turnCW() { servo.setPower(-1); reportStatus("Turret: CW"); }
        public void stop() { servo.setPower(0); reportStatus("Turret: stopped"); }
    }
```

`RobotModel/Mechs/Components/PusherServo.java:11-30`
```java
    public class AutonomousBallPusherBehaviors extends AutonomousComponentBehaviors {
        public AutonomousBallPusherBehaviors(Telemetry telemetry) {
            super(telemetry);
        }

        public void pushBalls() {
            servoR.setPosition(0.8);
            reportStatus("Pusher: push");
        }

        public void setPosition(double position) {
            servoR.setPosition(position);
            reportData("Pusher position", position);
        }

        public void retractPusher() {
            servoR.setPosition(0.0);
            reportStatus("Pusher: retract");
        }
    }
```

`RobotModel/Mechs/Components/DoubleShooter.java:23-44`
```java
    public class AutonomousShooterBehavior extends AutonomousComponentBehaviors {
        public AutonomousShooterBehavior(Telemetry telemetry) {
            super(telemetry);
        }

        public void StartShoot() {
            shooterF.setPower(0.76);
            shooterB.setPower(-0.76);
            reportStatus("Shooter: running");
        }
        public void StopShoot(){
            shooterF.setPower(0);
            shooterB.setPower(0);
            reportStatus("Shooter: stopped");
        }

        public void setPower(double power){
            shooterF.setPower(power);
            shooterB.setPower(-power);
            reportData("Shooter power", power);
        }
    }
```

### Bugs taught

None in this lesson.  (Fixed since:  `Turret.turnCW/turnCCW` no longer sleep;  `SpinnyIntake` signs;  `PusherServo` direction.)

---

## Chapter 12 -- State Machines  (`09`, `Advanced/state-machines`)

### Code blocks

| # | Source:lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | `09`:19-23 | `IState` | UNCHANGED | `Extensions/IState.java:3-5` |
| 2 | `09`:29-39 | `StateMachine` | **CHANGED** (`abort()` now nulls `elapsedTime`) | below |
| 3 | `09`:45-58 | `doAndWait` | UNCHANGED (comments dropped) | `StateMachine.java:45-67` |
| 4 | `09`:66-68 | rapid-fire state diagram | ILLUSTRATIVE (TRUE NOW) | `BillyRapidFire.java:83-134` |
| 5 | `09`:74-76 | `IAutonStrategy` | UNCHANGED | `AutonStrategies/IAutonStrategy.java:3-6` |
| 6 | `09`:80-88 | `ExampleStateMachineAuton.execute()` | UNCHANGED | `ExampleStateMachineAuton.java:37-44` |
| 7 | `09`:94-101 | `MecanumAutonDance` lambda | UNCHANGED (with `...`) | `ExampleAutonomousStrategies.java:11-26` |
| 8 | `SM`:24-38 | `BillyMA` strategy driving `BRF` | **REMOVED** | replaced by `MacroManager` + claims (chapter 13) |
| 9 | `SM`:42-48 | `BillyRobot` strategy | **CHANGED** | chapter 10 block 3 |
| 10 | `SM`:67-82 | `doAndWait` + `private ElapsedTime elapsedTime;` | UNCHANGED | `StateMachine.java:35`, `:45-67` |
| 11 | `SM`:101-104 | `BRF = new BillyRapidFire(auton, 3); BRF.abort();` | **CHANGED** (`BRF.abort()` removed `0acfd09`) | `BillyMA.java:174` |
| 12 | `SM`:116-120 | `if(gamepad.a && BRF.isComplete()) { BRF.reset(3); }` | **REMOVED** | `MacroManager.request` (below) |
| 13 | `SM`:134-138 | old `abort()` | **CHANGED** (fixed `e89bec0`) | below |

Blocks 2 and 13, current (whole file):

`AutonStrategies/StateMachine.java:1-68`
```java
package org.firstinspires.ftc.teamcode.AutonStrategies;


import com.qualcomm.robotcore.util.ElapsedTime;


import org.firstinspires.ftc.teamcode.Extensions.IState;


public abstract class StateMachine {
    /* Every State Machine must be aware of its Current State */
    protected IState currentState;


    /* if this state machine can terminate, it does so by setting currentState to NULL. ...*/
    public boolean isComplete() {
        return currentState == null;
    }


    /* Abort the Current execution - no questions asked! */
    public void abort() {
        currentState = null;
        elapsedTime = null;
    }


    /* Every State Machine must be able to update its state.*/
    public void updateState() {
        if (currentState != null)
            currentState = currentState.execute();
    }


    private ElapsedTime elapsedTime;


    /**
     * helper method for non-blocking, time based states!
     * @param action the thing you're doing!
     * @param millis how long to wait before proceeding
     * @param nextState the next thing to do!
     * @return the appropriate state.
     */
    protected IState doAndWait(Runnable action, int millis, IState nextState) {
        return () ->
        {
            if (elapsedTime == null) // if we are STARTING this state
            {   // this is an initialization step.
                elapsedTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
                // DO THE THING!
                action.run();
            }


            // if we are still waiting... go let other things happen!!
            // this is the non blocking step.
            if(elapsedTime.milliseconds() < millis)
                return doAndWait(action, millis, nextState);
            // if we have waited long enough, clear the clock
            // and go on over to the next state!
            elapsedTime = null;
            return nextState;
        };


    }
}
```

Block 12's replacement -- the restart guard now lives in `MacroManager.request` and the button is edge-triggered (`gamepad2.aWasPressed()`, `BillyRobot.java:141`):

`RobotModel/Mechs/Assemblies/MacroManager.java:14-19`
```java
    public void request(MechAssembly.IMacroAssemblyStrategy<R> macro) 
    { 
        if (active != null) return;   // != here: something's already running, so ignore this request
        active = macro;   
        active.start();
    }
```

`BillyRapidFire` lifecycle (how "construct, then start" works now -- the constructor no longer sets `currentState`, so a new instance is idle):

`AutonStrategies/BillyRapidFire.java:37-79`
```java
    public BillyRapidFire(BillyMA.AutonomousBillyMA ma, int ballCount)
    {
        mechAssembly = ma;
        shotsPerRun = ballCount;
    }

    @Override
    public Set<BillyMA.Part> claims()
    {
        return EnumSet.of(BillyMA.Part.PUSHER, BillyMA.Part.FLYWHEEL);
    }

    @Override
    public void start()
    {
        reset(shotsPerRun);
    }

    @Override
    public void update(Gamepad gamepad)
    {
        updateState();
    }

    @Override
    public void cancel()
    {
        abort();
        mechAssembly.autonFlywheel.StopShoot();
        mechAssembly.autonBallPusher.retractPusher();
    }

    public void reset(int ballCount)
    {
        this.ballCount = ballCount;
        currentState = startShooter();
    }

    @Override
    public String name()
    {
        return "BillyRapidFire";
    }
```

### Claims

| Source:line | Claim | Verdict | Evidence |
|---|---|---|---|
| `09`:41 | `BillyMA`'s strategy lambda and `BillyRobot`'s strategy lambda call `.updateState()` | **FALSE NOW** | `BRF` is stepped by `MacroManager.update` -> `BRF.update(gamepad)` -> `updateState()` (`MacroManager.java:50-59`, `BillyRapidFire.java:55-59`).  `targeter` by `assist.update(...)` -> `updateState()` (`BillyRobot.java:157-159`, `LimelightAutoTarget.java:63-67`) |
| `09`:103 | `BillyRapidFire` runs during TeleOp | TRUE NOW | started from `gamepad2.a` (`BillyRobot.java:141-145`) |
| `09`:111 | `LimelightAutoTarget` never completes in normal operation;  graph cycles | TRUE NOW | every state returns `lookForTag()` (`LimelightAutoTarget.java:84`, `:95`, `:104`);  `IAssistRobotStrategy` has no `isComplete` (`Robot.java:59-66`).  It ends only via `stop()` -> `abort()` |
| `09` (table) | `ExampleStateMachineAuton` implements `IAutonStrategy` and uses `IState`/`doAndWait` | TRUE NOW | `ExampleStateMachineAuton.java:15-17`;  it has its **own private copy** of `doAndWait` (`:46-62`), not `StateMachine`'s |
| `SM`:52-56 | `BRF` and `targeter` don't know about each other | TRUE NOW for the objects;  but their **owners now coordinate** through claims in `BillyRobot` | chapter 13 |
| `SM`:84-87 | `elapsedTime` is one field per `StateMachine` | TRUE NOW | `StateMachine.java:35` |
| `SM`:107-108 | `BillyRapidFire`'s ctor sets `currentState = startShooter()` | **FALSE NOW** | ctor stores `shotsPerRun` only (`BillyRapidFire.java:37-41`);  `start()` calls `reset(shotsPerRun)` |
| `SM`:131 | `abort()` bug "marked with a `TODO` in `StateMachine.java`" | **FALSE NOW** | fixed `e89bec0` |
| `SM`:176 | turret two-owner conflict "marked with a `TODO` in `BillyMA.java`" | **FALSE NOW** | TODO deleted;  conflict resolved by claims (chapter 13) |
| `SM`:186-194 (practice trace) | tick-by-tick trace of the old lambda | **FALSE NOW** | new flow:  tick 1 `aWasPressed` -> `request` -> `start()` -> `reset(3)`;  same tick `giveInstructions` -> `macros.update` -> first `updateState()`;  turret still driven by the default strategy unless the assist claims it.  Holding `a` does nothing further (edge-triggered + `request` ignores while active) |

### Bugs taught

- `abort()` stale timer:  **FIXED** `e89bec0`.
- Turret two-owner conflict:  **FIXED** (CHANGED SHAPE) by claims, `b9b2a25` onward.

### Writer note -- eager chain construction (true before and after)

`startShooter()` evaluates its `nextState` argument immediately, so `start()` builds the whole chain at once:  `fire()` -> `retract()` -> `--ballCount > 0 ? fire() : stopShooter()` recurses until `ballCount` hits 0.  Every `reportStatus` in those methods (`BillyRapidFire.java:84, 98, 110, 126`) therefore fires once, in the tick where rapid fire starts, not when each step runs.  The sequencing itself is correct.

---

## Chapter 13 -- Control Ownership  (`05`, `07`, `Advanced/control-ownership`, plus the arbitration code)

The design these lessons call "not built yet" is now **partly built**.  Section B describes it.  This section audits the lessons.

### Code blocks

| # | Source:lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | `05`:18-29 | `BillyRapidFire` `startShooter/fire/retract` | UNCHANGED (abridged:  drops each method's `reportStatus` line) | `BillyRapidFire.java:83-123` |
| 2 | `05`:37-51 | `BillyMA` strategy with `BRF` guard | **REMOVED** | below |
| 3 | `05`:59-63 | "This line is a bug!" comment + `turret.move(gamepad)` | **REMOVED** | below |
| 4 | `07`:16-47 | `LimelightAutoTarget` | **CHANGED** (implements `IAssistRobotStrategy`;  `claims/name/start/update/stop`) | section C |
| 5 | `07`:55-66 | `targeter = new ...` + strategy | targeter construction UNCHANGED (`BillyRobot.java:127-130`);  strategy **CHANGED** | chapter 10 block 3 |
| 6 | `07`:74-78 | bug comment | **REMOVED** | -- |
| 7 | `CO`:25-39 | `BillyMA` strategy | **REMOVED** | below |
| 8 | `CO`:61-67 | empty `IAssemblyStrategy` / `IRobotStrategy` | UNCHANGED, but no longer the whole story | `MechAssembly.java:58`;  `Robot.java:55`.  New siblings:  `MechAssembly.java:60-68`, `Robot.java:59-66` |

Blocks 2, 3, 7 -- the current default strategy and how it receives claims:

`RobotModel/Mechs/Assemblies/BillyMA.java:117-128`
```java
        strategy = (mechAssembly, gamepad, claimed) -> {

            if (!claimed.contains(Part.INTAKE))
                intake.move(gamepad);
            if (!claimed.contains(Part.PUSHER))
                ballPusher.move(gamepad);
            if (!claimed.contains(Part.FLYWHEEL))
                flywheel.move(gamepad);
            if (!claimed.contains(Part.TURRET))
                turret.move(gamepad);

        };
```

`RobotModel/Mechs/Assemblies/BillyMA.java:190-203`
```java
    @Override
    public void giveInstructions(Gamepad gamepad) {
        giveInstructions(gamepad, EnumSet.noneOf(Part.class));
    }

    public void giveInstructions(Gamepad gamepad, Set<Part> robotClaims) {                     

        macros.update(gamepad);     // 1. step the macro (and release it when done)  

        Set<Part> claimed = EnumSet.noneOf(Part.class);
        claimed.addAll(macros.claimed());
        claimed.addAll(robotClaims);
        strategy.execute(this, gamepad, claimed);
    }
```

Block 8's new siblings:

`RobotModel/Mechs/Assemblies/MechAssembly.java:58-69`
```java
    protected interface IAssemblyStrategy { }
    
    public interface IMacroAssemblyStrategy<R>
    { 
        Set<R> claims();
        void start();
        void update(Gamepad gamepad);
        boolean isComplete();
        void cancel();
        String name();
    }
    protected MechAssembly.IAssemblyStrategy strategy;
```

`RobotModel/Robots/Robot.java:55-66`
```java
    protected interface IRobotStrategy {}

    protected Robot.IRobotStrategy strategy;

    public interface IAssistRobotStrategy<R>
    { 
        Set<R> claims();
        void start();
        void update(Gamepad gamepad1, Gamepad gamepad2);
        void stop();
        String name();
    }
```

What each strategy claims:

`AutonStrategies/BillyRapidFire.java:43-47`
```java
    @Override
    public Set<BillyMA.Part> claims()
    {
        return EnumSet.of(BillyMA.Part.PUSHER, BillyMA.Part.FLYWHEEL);
    }
```

`AutonStrategies/LimelightAutoTarget.java:48-52`
```java
    @Override
    public Set<BillyMA.Part> claims()
    {
        return EnumSet.of(BillyMA.Part.TURRET);
    }
```

### Claims

| Source:line | Claim | Verdict | Evidence |
|---|---|---|---|
| `05`:53 | while rapid fire runs, `BillyMA` skips intake/pusher/flywheel `move` | **CHANGED** | now skips only **claimed** parts:  `PUSHER`, `FLYWHEEL`.  Intake stays manual during rapid fire (`BillyMA.java:119-124`) |
| `05`:65 | turret uncontrolled during rapid fire;  two owners;  "whichever runs last wins" | **FALSE NOW** | turret is skipped by the default strategy only when an assist claims it (`BillyMA.java:125-126`, `BillyRobot.java:156`) |
| `05`:69 | vocabulary "isn't fully built yet" | PARTLY TRUE | macro and assist exist as interfaces + manager logic;  mode strategies and lifecycle states do not (section B) |
| `05`:78 | moving `turret.move` out of the guard fixes only half | moot | that guard no longer exists |
| `07`:49 | `LimelightAutoTarget` never touches `Gamepad` | TRUE NOW in behavior;  its `update(Gamepad driver, Gamepad mech)` receives two and ignores them | `LimelightAutoTarget.java:63-67` |
| `07`:80 | targeter runs "every single loop" from `BillyRobot.update()` | **CHANGED** | runs only while it is the active assist.  Starts active (`BillyRobot.java:161`);  `gamepad1.y` toggles (`:133-138`);  `gamepad1.b` stops all (`:151-153`) |
| `07`:80-82 | nothing decides who wins the turret;  ownership exists only in a comment | **FALSE NOW** | claims-based skip, above |
| `07`:88 | `BillyRobot` and `BillyMA` share no signal | **FALSE NOW** | `giveInstructions(Gamepad, Set<Part> robotClaims)` (`BillyMA.java:195-203`) |
| `07`:86 | `LimelightAutoTarget` is an assist strategy | TRUE NOW, and now **typed** as one (`LimelightAutoTarget.java:24`) |
| `CO`:12-17 | `ControlStrategyExpansionPlan.md` "nobody has built it yet";  "no `TODO` comment to land on" | **FALSE NOW** (first half);  TRUE NOW (no TODO) | phases 2-5 built (section B) |
| `CO`:41-55 | `if(!BRF.isComplete()) return;` is the undeclared ownership rule | **FALSE NOW** (code gone) | now declared:  `claims()` |
| `CO`:69-75 | marker interfaces are empty placeholders | TRUE NOW for those two;  real interfaces now sit beside them | above |
| `CO`:88-91 | under the model, `BillyRapidFire` would declare flywheel + pusher, not turret | TRUE NOW (built exactly so) | `BillyRapidFire.java:43-47` |
| `CO`:98-99 | `LimelightAutoTarget` owns only the turret;  `BillyRapidFire` owns flywheel + pusher | TRUE NOW | above |
| `CO`:114-119 | rule 3 run by hand:  default strategy wouldn't move the turret while the assist holds it | TRUE NOW (implemented) | `BillyMA.java:125-126` |
| `CO`:121-126 | strategy vs. strategy selection are separate | TRUE NOW at the assembly layer (`MacroManager`);  at the robot layer selection is inline in `BillyRobot` (`:132-219`), not a separate class |
| `CO`:160-169 | open question:  does driver input interrupt an assist? | **answered in code:  no.**  While the assist claims `TURRET`, the bumpers are ignored;  the driver must press `gamepad1.y` (or `gamepad1.b`) | `BillyMA.java:125-126`;  `BillyRobot.java:133-138`, `:151-153` |

### Bugs taught

- `BillyMA` turret double-owner:  **FIXED** (CHANGED SHAPE) via claims.
- Rapid fire scattering guard checks:  **FIXED** (CHANGED SHAPE) via `MacroManager` + `claims()`.

---

## Chapter 14 -- Pose and Navigation  (`Advanced/apriltags-and-navigation`)

### Code blocks

| # | Lesson lines | Content | Verdict | Source |
|---|---|---|---|---|
| 1 | 28-30 | `double tx = tag.getTargetXDegrees();` | UNCHANGED | `LimelightAutoTarget.java:121` |
| 2 | 47-51 | `detection.ftcPose.range/bearing/yaw` | ILLUSTRATIVE (SDK) | `ftcPose` stays on the parent `AprilTagDetection` in v12 (`ConceptAprilTag.java:230-232` uses `detection.ftcPose`) |
| 3 | 68-77 | `robotPose` telemetry | ILLUSTRATIVE (SDK, verbatim) | `ConceptAprilTagLocalization.java:256-263` -- in v12 it sits inside `if (detection instanceof AprilTagSingleDetection)` (`:251`) |
| 4 | 82-88 | camera pose | ILLUSTRATIVE (SDK, verbatim) | `ConceptAprilTagLocalization.java:99-102`, `:187` |
| 5 | 99-101 | `Pose3D botpose = result.getBotpose();` | ILLUSTRATIVE (SDK, verbatim) | `SensorLimelight3A.java:105` |
| 6 | 116-118 | `getBotpose_MT2()` | ILLUSTRATIVE | `doc/limelight3a/api.md:96` |

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 12-13 | nothing in the codebase computes pose | TRUE NOW | no `getBotpose`, `robotPose`, or `ftcPose` in TeamCode |
| 16-20 | `java-objects.md` was checked against "FTC Hardware 11.2.1 ... the exact SDK version this repo builds against" | **FALSE NOW** (already false at `34c22ea`) | repo builds `12.0.0` since `7cf073d` (2026-09-07), `build.dependencies.gradle:7-14`;  `doc/limelight3a/README.md:3` still says 11.2.1 "matching this repository's `build.dependencies.gradle`" |
| 32-33 | `tx` is the only thing turret tracking pulls from a detection | TRUE NOW (plus the ID filter) | `LimelightAutoTarget.java:111-121`;  `LimelightExtensions.java:30` |
| 103-106 | Billy builds a `Limelight3A` **and an unused `AprilTagProcessor`**, both marked `TODO` | **FALSE NOW** | processor removed `1a6809a`;  neither TODO exists |
| 121-123 | MegaTag2 quote is "from this team's own reference doc" (after naming `java-objects.md`) | TRUE (quote exists) but it is in **`doc/limelight3a/api.md:96`**, not `java-objects.md` |
| 143-145 | zero-pose warning quote | TRUE | `doc/limelight3a/java-objects.md:342` |
| 154-156 | `getBotposeTagCount/Span/AvgDist`, `getStddevMt1/Mt2` | TRUE (doc) | `java-objects.md:80-84` |
| 164-166 | `FiducialResult.getRobotPoseFieldSpace()` | TRUE (doc) | `java-objects.md:172` |
| 238-243 | a pose getter "need not declare ownership the way `BillyRapidFire` or `LimelightAutoTarget` do" | TRUE NOW, and sharper now:  those two literally declare `claims()` | chapter 13 |

### Bugs taught

None.

---

## Chapter 15 -- AprilTag Clusters  (`Advanced/apriltag-clusters`, `doc/limelight3a/cluster-targeting-plan.md`)

### Code blocks

| # | Lesson lines | Content | Verdict | Source |
|---|---|---|---|---|
| 1 | 33-35 | `double tx = tag.getTargetXDegrees();` | UNCHANGED | `LimelightAutoTarget.java:121` |
| 2 | 66-70 | `AprilTagClusterMemberMetadata` fields | ILLUSTRATIVE (SDK) | UNVERIFIED:  Vision 12.0.0 sources not in repo or gradle cache |
| 3 | 83-90 | `doClusterSolve` excerpt | ILLUSTRATIVE (SDK) | UNVERIFIED, same reason |
| 4 | 118-126 | `instanceof AprilTagSingleDetection` | ILLUSTRATIVE (SDK) | matches `ConceptAprilTag.java:225-239` |

### Claims

| Source:line | Claim | Verdict | Evidence |
|---|---|---|---|
| lesson 19-21 | v12.0 release notes at top of repo `README.md` | TRUE NOW | `README.md:62-108` |
| lesson 54-57 | release-note quote on clusters | TRUE (README splits it into two bullets) | `README.md:92-93` |
| lesson 37-40, plan 21 | BIOBUZZ offsets from `AprilTagGameDatabase` | UNVERIFIED (SDK source not available) |
| lesson 141-144 | `clusterMembers` package-private, `getMemberMetadata()` protected | UNVERIFIED (same) |
| lesson 196-197 | "Tie this to the existing `TODO` in `LimelightAutoTarget.rotateCW`" | **FALSE NOW** | fixed `1a6809a`, TODO gone.  Still usable as history:  "this codebase had a real sign bug" |
| lesson 253-255 | Task 1 fixes the null crash first | the crash is fixed;  `isValid()` part of Task 1 is **not** done | `LimelightExtensions.java:23-25` |
| lesson 280-281 | "name the existing bug ... from getting a sign wrong" | **FALSE NOW** as "existing" | as above |
| plan 49-53 (Task 1) | `TODO` in `LimelightExtensions` about `getLatestResult()` null | **FALSE NOW** | that TODO is gone;  the remaining TODO at `LimelightExtensions.java:10-11` is the cluster one |
| plan 55-65 (Task 2) | create `Extensions/TagCluster.java` | not started | no such file |
| plan 67-79 (Task 3) | `OpModes/LimelightClusterProbe.java` | not started | no such file |
| plan 81-97 (Tasks 4-5) | `estimateClusterOrigin`, `tryGetClusterOrigin` | not started | absent from `LimelightExtensions.java` |
| plan 103-108 (Task 7) | `LimelightAutoTarget` takes an `int tagId`;  "read the `TODO` in `rotateCW`" | first part TRUE NOW;  `rotateCW` TODO **FALSE NOW** | `LimelightAutoTarget.java:37-46` |
| plan 103-105 (Task 7) | RED/BLUE TeleOps have a `TODO` asking for this season's target | TRUE NOW | `BillyTeleOpRED.java:11`, `BillyTeleOpBLUE.java:11` |
| plan "Results" | empty | TRUE NOW | -- |

Current cluster TODO:

`Extensions/LimelightExtensions.java:10-11`
```java
    // TODO (students): BIOBUZZ goals are AprilTag *clusters* (4 tags sharing one origin).
    // Add cluster search + origin estimation here - see doc/limelight3a/cluster-targeting-plan.md
```

New constraint for Task 7:  `LimelightAutoTarget` is now an `IAssistRobotStrategy<BillyMA.Part>` started by `BillyRobot.startAssist(...)`, so a cluster version must keep `claims()`, `name()`, `start()`, `update(Gamepad, Gamepad)`, `stop()`.

---

## Chapters 16-20 -- Mechanical lessons  (`Mechanical/*.md` vs `doc/rev-duo/*.md`)

Abbreviations:  SF = `rev-duo/structure-and-fasteners.md`, AG = `actuators-and-gearboxes.md`, DDG = `design-decision-guide.md`, PTM = `power-transmission-and-motion.md`, LM = `linear-motion.md`, CS = `control-and-sensors.md`, KSB = `kits-and-starter-bots.md`.

**Citations:**  every cited rev-duo **file** exists.  No lesson cites a rev-duo heading by its exact text;  every section a lesson relies on exists (mapping below).  Almost all numbers MATCH.  Mismatches and unsupported claims are listed.

### 16 -- Structure and Fasteners  (`Mechanical/structure-and-fasteners.md`)

Sections relied on, all exist:  SF:10 "1. Extrusion vs. Channel", SF:37 "2. The Extended Motion Pattern", SF:56 "3. Fastener Rules & Assembly Techniques", SF:92 "4. The 5mm Hex Shaft Standard", SF:124 "5. Cutting and Modifying Structure".

All numbers MATCH:  15mm grid / 5mm hex (SF:6);  8mm/16mm pitch (SF:17);  9mm bearing seats every 16mm (SF:18, 52);  REV-41-1432, four T-slots, M3x0.5 (SF:22-24);  15x30mm REV-41-1433 (SF:27-28);  diagram (SF:42-46);  16/32mm bolt circles (SF:51);  1-2 turns (SF:71);  90/45 degrees (SF:78);  REV-41-1493 (SF:86, 88);  six faces / 5mm bore (SF:97-98);  75/90/135/400mm shafts (SF:101-104);  M3 set screw (SF:113).

Problems:

- Lesson 72-75:  "Studs Up" reason is "a nut in the slot spins uselessly";  SF:66 gives a different reason (heads bind or strip slot edges).
- Lesson 77-80 (pre-load nuts, slide in) contradicts the lesson's own rule 1 (L70-75);  inherited from SF:72.
- Lesson 61:  "one hole pattern serves every motor and bearing part REV sells" -- NOT IN REV-DUO (overgeneralizes SF:51).
- Lesson 83:  "plastic brackets" -- SF:76 says plastic and many stamped aluminum.  Minor.
- Lesson 116-117:  High Strength Hex Hub as axial constraint -- SF:119-120 says it prevents bore rounding.  (SF:107 says "three methods", lists four:  a rev-duo error.)
- Lesson 130-131:  bandsaw speed reason -- NOT IN REV-DUO.

### 17 -- Actuators and Mechanism Sizing  (`Mechanical/actuators-and-mechanism-sizing.md`)

Sections relied on, all exist:  AG:42, AG:50 "Cartridge Ratio Reference", AG:61-66 "Golden Rule of UltraPlanetary Stacking" box, AG:76 "Common Gearbox Configurations & Specs", AG:90 "Load Limits & Safety Warning", AG:101 "3. Smart Robot Servo", AG:127 "4. Servo Power Module", DDG:45, DDG:53 "Worked Example: Sizing an Elevator Motor", DDG:67, DDG:78 "Archetype 1".

All quoted numbers MATCH rev-duo:  20:1 Core Hex, 100-2,000+ RPM, 125 RPM, 3.2 N·m, 28 / 288 ticks (AG:16-23);  REV-41-1601 2.896:1, -1602 3.619:1, -1603 5.230:1 (AG:55-57);  3:1 = 2.89:1, 2,076 RPM, 0.30 N·m (AG:80);  5:1+4:1 = 18.88:1, 318 RPM, 1.98 N·m (AG:85);  5:1+4:1+3:1 = 54.68:1, 110 RPM, 5.74 N·m (AG:87);  40 N·m max, 3 stages (AG:91-92);  25T spline, 270°, setPosition/setPower ranges, 30-120° (AG:103-123);  SPM ~2.0A, six ports, 5-10% margin, 12V (AG:131-136);  2.0 kg, 1.2 m, 23.54 J, 1.5 s, 15.7 W, SF 2.0-2.5, 31.4 W (DDG:50-62);  HD Hex ~40W, Core Hex ~10W (DDG:63);  V = πD·RPM/60, 90mm mecanum, ≈1.50 m/s, 1.4-1.8 m/s (DDG:69-87);  ~15:1 from 5:1+3:1 (AG:84, 15.15:1).

Arithmetic problems inherited from rev-duo (lesson and rev-duo agree with each other, both are off):

- 18.88:1 is the product of truncated 5.23 x 3.61.  With the "exact" ratios the lesson stresses (L44, L49-55):  5.230 x 3.619 = **18.93:1**.
- 54.68:1 -- exact product 5.230 x 3.619 x 2.896 = **54.83:1** (109 RPM).
- 6000 / 2.896 = **2,072 RPM**, not 2,076 (AG:80).
- "HD Hex ~40W peak" (L124, DDG:63) conflicts with AG's own torque table:  0.30 N·m / 2.89 ≈ 0.105 N·m bare stall;  peak = stall x free speed / 4 ≈ **16.5 W**, below the 31.4 W design power.  So "a single HD Hex survives" is not supported by rev-duo's own figures.  Core Hex ~10 W is consistent.
- L140 (1.5 kg, 0.9 m, 1 s) -- practice values, NOT IN REV-DUO (fine).

### 18 -- Power Transmission and Wheels  (`Mechanical/power-transmission-and-wheels.md`)

Sections relied on, all exist:  PTM:10 "1. 0.5 Module Gears & The 15mm Grid", PTM:38 "2. Sprockets and #25 Roller Chain", PTM:62 "3. GT2 Timing Belts & Pulleys", PTM:77 / PTM:89 "Wheel Comparison Matrix", PTM:98 "The Mecanum Wheel O-Pattern Rule" (lesson calls it the "mecanum orientation rule"), PTM:126 "Compliant Wheel Durometer Guide".

All numbers MATCH:  0.5 module, PD = Nx0.5, CD = (N1+N2)/4 (PTM:12-18);  20T/40T 15.0mm, 40T/80T 30.0mm, 20T/100T 30.0mm 5:1 (PTM:25-27);  multiples of 60 -> 15mm (PTM:21);  #25 chain 0.25" pitch, 10T-40T (PTM:43-44);  1/4" flex (PTM:58);  GT2 3mm pitch 9mm width, 2,000-6,000 RPM (PTM:67-71);  rollers form an "X" (PTM:105);  30A / 45A / 60A (PTM:129-131).

Problems:

- Lesson 58:  chain tool presses "a *new* pin" back in -- PTM:50 says press the **original** pin back flush.  **MISMATCH.**
- Lesson 46-47:  center-distance formula used to show idlers don't change ratio -- the formula is unrelated;  not in rev-duo.
- Lesson 125-126:  a tooth sum not a multiple of 60 lands "off any standard hole spacing" -- too strong;  sums of 32 or 64 hit the 8/16mm pattern (SF:50-52).  True for its own 18T+35T example.
- Lesson 106-107 vs 132:  "swap two wheels" becomes "a single swapped wheel".
- Lesson 123-124 (18T + 35T), 129-130 (4,000 RPM flywheel, <30mm) -- NOT IN REV-DUO (practice values).
- PTM:122's "swap two same-side wheels -> diagonal/spin" (repeated at lesson 105-106) is physically doubtful.  Outside rev-duo scope;  flag, don't repeat.

### 19 -- Linear Motion and Control Electronics  (`Mechanical/linear-motion-and-control-electronics.md`)

Sections relied on, all exist:  LM:21, LM:39 "Cascading Rigging Logic", LM:59 "Dual-Spool Active Retraction", LM:68 "4. Constant-Force Springs", LM:77 "5. Software Protection with `DoublyLimitedMotor`", LM:100 "Autonomous Zeroing Procedure", CS:10, CS:35 "Wiring Rules for Winsor Builders", CS:52 "4. Sensor Catalog & Usage Guide".

All numbers MATCH:  n x speed / n x torque (LM:28-29);  1 inch -> 2 inches, 1/3 time (LM:43-45);  2 kg carriage, two 1.5 lb springs (LM:72-73);  hub ports, RS485 + XT30 (CS:14-31);  ToF 10-2000mm, 8,192 counts/rev (CS:55-80).

Problems:

- Lesson 52-55:  2 kg = 4.4 lb, two springs = 3 lb, so ~1.4 lb of gravity load remains;  "only fights friction" doesn't follow.  Inherited from LM:72-73.
- Lesson 88-89 omit LM:101's `-0.15` power step;  `RUN_TO_POSITION` also needs `setTargetPosition` first.

**The `DoublyLimitedMotor` snippet does not compile and wires the limits backwards.**  Lesson snippet, `Mechanical/linear-motion-and-control-electronics.md:68-79` (identical to LM:88-97 minus a comment), quoted from the lesson:

```text
lift = new DoublyLimitedMotor(
    hardwareMap,
    "liftMotor",
    "liftBottomLimit",
    "liftTopLimit",
    (motor, gamepad) -> {
        double power = -gamepad.left_stick_y;
        motor.setPower(power);
    }
);
```

Real constructor and strategy interfaces:

`RobotModel/Mechs/Components/DoublyLimitedMotor.java:38-44`
```java
    public interface DoublyLimitedMotorControlStrategy extends IControlStrategy{
        public void move(Gamepad gamepad, DoublyLimitedMotor doublyLimitedMotor);
    }

    public interface DoublyLimitedMotorTelemetryStrategy {
        public void update(DoublyLimitedMotor dlm, Telemetry telemetry);
    }
```

`RobotModel/Mechs/Components/DoublyLimitedMotor.java:54-76`
```java
    /**
     * The forward sensor is the sensor that is hit when the motor is given positive power
     * @param hardwareMap pass this down from on high
     * @param motorName name of the motor in the driver station configuration
     * @param forwardSensorName name of the forward sensor in the driver station configuration
     * @param reverseSensorName name of the reverse sensor in the driver station configuration
     * @param strategy lambda expression (Gamepad, DoublyLimitedMotor) for translating gamepad input into motor movement
     */
    public DoublyLimitedMotor(
            HardwareMap hardwareMap,
            String motorName,
            String forwardSensorName,
            String reverseSensorName,
            DoublyLimitedMotorControlStrategy strategy,
            DoublyLimitedMotorTelemetryStrategy telemetryStrategy
            ) {
        super(strategy);
        motor = hardwareMap.get(DcMotor.class, motorName);
        forwardSensor = hardwareMap.get(TouchSensor.class, forwardSensorName);
        reverseSensor = hardwareMap.get(TouchSensor.class, reverseSensorName);
        this.strategy = strategy;
        this.telemetryStrategy = telemetryStrategy;
    }
```

A correct real use:

`RobotModel/Mechs/Assemblies/CascadeArm.java:45-61`
```java
        cascade = new DoublyLimitedMotor(
                hardwareMap,
                "cascadeMotor",
                "topLiftLimit",
                "bottomLiftLimit",
                // Control strategy
                (gamepad, dlm) ->
                {
                     double power = GamepadExtensions.GetLeftStickY(gamepad);
                     dlm.setPower(power);
                },
                ((dlm, telemetry) ->
                {
                    telemetry.addData("Can Go Up", dlm.canGoForward());
                    telemetry.addData("Can Go Down", dlm.canGoReverse());
                })
                );
```

Differences:

1. Five arguments given;  the only constructor takes six (`telemetryStrategy` missing).  Passing `null` works:  `update()` null-checks it (`DoublyLimitedMotor.java:122`).
2. Lambda parameter order is `(Gamepad, DoublyLimitedMotor)` (`:39`).  The lesson's `(motor, gamepad)` binds `motor` to the `Gamepad`, so neither `gamepad.left_stick_y` nor `motor.setPower` compiles.
3. The second lambda argument is the `DoublyLimitedMotor` wrapper, not a `DcMotor`;  the raw motor is private (`:46`) with no accessor.
4. **Limits reversed.**  Arg 3 is `forwardSensorName`, "the sensor that is hit when the motor is given positive power" (`:55`).  The lesson passes `"liftBottomLimit"` there while mapping stick-up to positive power, so at the bottom it blocks up and allows down.  That defeats the very protection lesson 61-63 / 81-84 claim.  `CascadeArm.java:48-49` passes top first, correctly.
5. No `setMode`, `getPower`, or encoder access on `DoublyLimitedMotor`, so the zeroing procedure (lesson 87-89) can't go through this class as written.
6. `setPower` javadoc expects the dead zone already applied (`:91`);  the lesson uses raw `-gamepad.left_stick_y`.  `CascadeArm` uses `GamepadExtensions.GetLeftStickY`.

### 20 -- Kits and Starter Bots  (`Mechanical/kits-and-starter-bots.md`)

Sections relied on, all exist:  KSB:10, KSB:24, KSB:34, KSB:45 "4. FTC Kickoff Starter Bot Reference Designs", KSB:91 "5. How Winsor Students Should Use These Designs".  Lesson 55 cites `NewRobotDesignWorkflow.md`:  exists at `doc/NewRobotDesignWorkflow.md`.

Numbers MATCH:  five seasons named (KSB:52-87);  DECODE / INTO THE DEEP / CENTERSTAGE / POWERPLAY / Ultimate Goal mechanisms (KSB:55-85);  3 rules (KSB:94-96);  drivetrain kit descriptions (KSB:16-19);  lift kits (KSB:28-29);  Class Bot (KSB:39).

Problems:

- Lesson 5-6, 10:  "all three prior Mechanical lessons" / "the other three" -- there are **four** other Mechanical lessons;  L74-76 relies on linear-motion, which isn't listed as a prerequisite.  **MISMATCH.**
- Lesson 30:  omits the 2026-27 Preview Starter Bot (KSB:49), this team's current season.
- Lesson 66-67:  four drivetrain kits;  KSB:16-20 lists five ("Basic Channel Drivetrain" omitted;  "Channel Drivetrain" ambiguous between KSB:17 and KSB:20;  KSB:17's channel kit is chain-driven, not direct-drive).
- Lesson 25-26:  REV "expects teams to build it unmodified" -- NOT IN REV-DUO, and contradicts KSB:6 ("analyze, build, and adapt").
- Lesson 80, 123:  Class Bot "scores nothing" -- NOT IN REV-DUO.
- Lesson 41-56 adds `MechAssembly` to rule 3;  KSB:96 says only `MechComponent` (README:62 names both).  Minor.

---

## Chapter 21 -- Designing a New Robot  (`10`, `doc/NewRobotDesignWorkflow.md`)

### Code blocks

| # | Source:lines | Content | Verdict |
|---|---|---|---|
| 1 | `10`:29-36 | build order | ILLUSTRATIVE (TRUE NOW) |
| 2 | `10`:42-62 | component skeleton | ILLUSTRATIVE.  Matches the current base class |
| 3 | `10`:64-77 | assembly skeleton | ILLUSTRATIVE.  **Incomplete:**  no `this.telemetry = telemetry;`, no constructor, no `getAutonomousBehaviors()` (abstract on `MechAssembly`, so it would not compile).  The Workflow doc's version (`NewRobotDesignWorkflow.md:389-447`) has all three |
| 4 | `10`:79-92 | thin OpMode | ILLUSTRATIVE (TRUE NOW;  matches `BillyTeleOpBLUE.java:16-27`) |
| 5 | Workflow:341-546 | component / assembly / robot / OpMode / auton templates | ILLUSTRATIVE.  Consistent with current base classes.  None shows the new macro / assist interfaces |

### Claims

| Source:line | Claim | Verdict | Evidence |
|---|---|---|---|
| `10`:127 | Q5:  "does it change `update()` beyond the default template?" | still a fair question;  note Billy now **replaces** `update()` entirely | `BillyRobot.java:173-176` |
| `10`:96-102 | Strategy / Composite / Facade / DI / State Machine | TRUE NOW;  "informally" for assembly/robot strategy is now **formal** (typed interfaces) |
| Workflow:83 | alliance tag passed as ctor param | TRUE NOW | `BillyRobot.java:89`;  `BillyTeleOpBLUE.java:17` |
| Workflow:106-109 | component verb lists | TRUE NOW | chapter 11 |
| Workflow:168-172 | `BillyMA` "blocks manual control while rapid fire is running" | **CHANGED** | blocks only claimed parts (`PUSHER`, `FLYWHEEL`) |
| Workflow:228-235 | Billy's `update()` = `super.update(...)` then `targeter.updateState()` | **FALSE NOW** | chapter 10 block 3 |
| Workflow:251-256 | RED/BLUE differ only in tag ID | TRUE NOW | the two files differ only in `@TeleOp(name=...)`, class name and TODO text;  both IDs are `-1` |
| Workflow:309 | robot strategy example "`LimelightAutoTarget` in `BillyRobot.update()`" | **CHANGED** | it runs as the active assist, stepped inside `BillyRobot`'s strategy lambda |

### Writer note

A new robot that wants macros or assists can reuse `MacroManager<R>` and the two interfaces, but **all robot-layer arbitration code (`startAssist`, `stopAssist`, `assistClaims`, `stopAll`, `overlaps`, the telemetry lines) lives in `BillyRobot`, not in `Robot`.**  There is no reusable robot-level manager.

---

## Chapter 22 -- Common Pitfalls  (`Advanced/common-pitfalls`)

### Code blocks

| # | Lesson lines | Content | Verdict | Current |
|---|---|---|---|---|
| 1 | 28-41 | `turnToAngle` | UNCHANGED (abridged:  drops the `reportStatus`/`reportData` lines and the comment) | `MecanumDrive.java:35-55` |
| 2 | 51-53 | `turnCCW` with `TrySleep(100)` | **CHANGED** (sleep removed `1a6809a`) | `Turret.java:23` |
| 3 | 74-76 | `if (gamepad.dpad_up) { motor.setPower(0.75); }` | UNCHANGED | `BillyMA.java:55-56` |
| 4 | 80-85 | `startIntake()` with `-1` | **CHANGED** (`+1`) | `SpinnyIntake.java:17-21` |
| 5 | 115-123 | IMU TODO excerpt + lookup | UNCHANGED (abridged excerpt of the TODO) | `MecanumDrive.java:158-163`, `:174` |
| 6 | 148-157 | intake two-`if`s | **CHANGED** (fixed) | `BillyMA.java:55-62` |
| 7 | 180-193 | `tryGetFiducial` without null check | **CHANGED** (fixed) | `LimelightExtensions.java:19-33` |
| 8 | 215-224 | `pushBalls` / `retractPusher` | UNCHANGED (code);  the bug told around it is gone | `PusherServo.java:16-29` |
| 9 | 239-256 | `rotateCCW` / `rotateCW` | **CHANGED** (`rotateCW` default `-1`;  lesson also drops `reportData` lines) | `LimelightAutoTarget.java:76-97` |
| 10 | 267-270 | `arm.setPosition` two-`if`s | ILLUSTRATIVE | -- |

Block 1, current (still the live category-1 example):

`RobotModel/DriveTrain/Mecanum/MecanumDrive.java:25-55`
```java
        // TODO: two problems here, one structural and one physical.
        //  (1) This blocks. It sits in a while loop calling TrySleep(100) until it's
        //  done, so nothing else on the robot updates - no telemetry, no other
        //  subsystem - for the entire turn. Compare doAndWait() in StateMachine, which
        //  exists specifically so timed actions DON'T do this (see Flint Lesson 9).
        //  (2) spin() commands full power (+/-1) no matter how far off we are, but the
        //  loop exits only inside a 1-degree window. A full-power turn moves much more
        //  than 1 degree in the 100ms between checks, so it overshoots, turns back,
        //  overshoots again, and never settles. Scale the power down as |smol| shrinks
        //  (the way LimelightAutoTarget scales by -tx/10) and widen the deadband.
        public void turnToAngle(double degrees) {
            degrees = AngleExtensions.mapToIMURange(degrees);
            double yaw = imu.getRobotYawPitchRollAngles().getYaw();
            double smol = AngleExtensions.getSmol(degrees, yaw);
            while(Math.abs(smol) > 1 ) {  //can change dead-zone here
                if (smol > 0) {
                    spin(TurnDirection.LEFT);
                    reportStatus("Turning LEFT");
                }
                else {
                    spin(TurnDirection.RIGHT);
                    reportStatus("Turning RIGHT");
                }
                reportData("Yaw", yaw);
                reportData("Target", degrees);
                ThreadExtensions.TrySleep(100);
                yaw = imu.getRobotYawPitchRollAngles().getYaw();
                smol = AngleExtensions.getSmol(degrees, yaw);
            }
            reportStatus("Turn complete");
        }
```

Block 2, current:

`RobotModel/Mechs/Components/Turret.java:18-25`
```java
        public void setPower(double power)
        {
            servo.setPower(power);
            reportData("Turret power", power);
        }
        public void turnCCW() { servo.setPower(1); reportStatus("Turret: CCW"); }
        public void turnCW() { servo.setPower(-1); reportStatus("Turret: CW"); }
        public void stop() { servo.setPower(0); reportStatus("Turret: stopped"); }
```

Block 4, current:

`RobotModel/Mechs/Components/SpinnyIntake.java:17-33`
```java
        public void startIntake()
        {
            intake.setPower(1);
            reportStatus("Intake: running");
        }

        public void stopIntake()
        {
            intake.setPower(0);
            reportStatus("Intake: stopped");
        }

        public void reverseIntake()
        {
            intake.setPower(-1);
            reportStatus("Intake: reversed");
        }
```

Category 2's other example (still open):

`RobotModel/DriveTrain/Mecanum/MecanumDrive.java:245-255`
```java
        // TODO: compare these signs to AutonomousMecanumDrive.drive(x,y,t) above and to
        // spin() below. lb and rf here are flipped relative to both - a pure-turn input
        // (vertical=horizontal=0, turn!=0) works out to lf=+turn, lb=-turn, rf=+turn, rb=-turn,
        // which splits the wheels front-vs-back (LF/RF one way, LB/RB the other), not
        // left-vs-right the way spin() and the autonomous drive() do. That's not a clean
        // in-place rotation - the wheels fight each other. lb should match lf's sign and rf
        // should match rb's sign (mirroring the autonomous version) for turning to work right.
        double lf = y1 + turn;   // swapped sign
        double lb = y2 - turn;   // swapped sign
        double rf = y2 + turn;
        double rb = y1 - turn;
```

### Claims

| Lesson line | Claim | Verdict | Evidence |
|---|---|---|---|
| 45-47 | `turnToAngle` blocks;  marked `TODO` pointing at `doAndWait` | TRUE NOW | `MecanumDrive.java:25-34` |
| 54-59 | `LimelightAutoTarget` calls a sleeping turret verb every loop | **FALSE NOW** | sleeps removed;  also `LimelightAutoTarget` calls `setPower`/`stop`, never `turnCW/turnCCW` (`:82`, `:93`, `:102`) |
| 87-91 | `startIntake` opposite sign to dpad_up;  own `TODO` | **FALSE NOW** | fixed `1a6809a` |
| 93-99 | teleop `drive()` flips `lb`/`rf`;  `TODO` | TRUE NOW | `MecanumDrive.java:245-255` |
| 113-131 | IMU initialized only by `BillyRobot`;  `Wildbots2025` uninitialized | TRUE NOW | chapter 3 |
| 146-170 | intake two-`if` bug | **FALSE NOW** | fixed |
| 178-208 | `tryGetFiducial` NPE | **FALSE NOW** | fixed |
| 226-228 | only `setPosition(double)` calls `setDirection(REVERSE)`;  teleop calls it;  auton verbs leave the servo `FORWARD` | **FALSE NOW** | direction set once in the ctor (`PusherServo.java:52`);  `setPosition` no longer touches direction (`:21-24`) |
| 258-262 | `rotateCW` drives full power away from center | **FALSE NOW** | fixed |

### Bugs taught -- status

| Category | Example | Status |
|---|---|---|
| 1 Loop must never stop | `turnToAngle` | **OPEN** |
| 1 | `Turret.turnCW/turnCCW` sleeps | **FIXED** `1a6809a` |
| 2 Two conventions | `SpinnyIntake` signs | **FIXED** `1a6809a` |
| 2 | `MecanumDrive` teleop signs | **OPEN** |
| 2 (practice) | `PusherServo` direction | **FIXED** `1a6809a` |
| 3 One handle | IMU | **OPEN** |
| 4 Independent guards | intake two-`if` | **FIXED** `1a6809a` |
| 5 SDK silence | `tryGetFiducial` null | **FIXED** `1a6809a` (no `isValid()`) |
| practice | `rotateCW` sign | **FIXED** `1a6809a` |

Of the lesson's repo examples, three are still live:  `turnToAngle`, `MecanumDrive` teleop signs, IMU ownership.  Everything else needs ILLUSTRATIVE replacements or "this was the code, here is the fix" framing.

---

## A.  TODO inventory

`grep -rn TODO TeamCode/src/main/java` = **17 lines**.  Five of those are cross-references inside another TODO's text or a doc, so there are **12 distinct TODO comments**.

| # | path:line | Full TODO comment text | Chapter(s) |
|---|---|---|---|
| 1 | `Extensions/LimelightExtensions.java:10-11` | `// TODO (students): BIOBUZZ goals are AprilTag *clusters* (4 tags sharing one origin).` / `// Add cluster search + origin estimation here - see doc/limelight3a/cluster-targeting-plan.md` | 15 (5) |
| 2 | `OpModes/BillyTeleOpBLUE.java:11` | `// TODO: this season's BLUE-alliance target AprilTag ID goes here (last season's Limelight tag ID was removed during 2027 season prep).` | 5, 15 |
| 3 | `OpModes/BillyTeleOpRED.java:11` | `// TODO: this season's RED-alliance target AprilTag ID goes here (last season's Limelight tag ID was removed during 2027 season prep).` | 5, 15 |
| 4 | `OpModes/ExampleStateMachineAuton.java:19-20` | `// TODO: this season's target AprilTag ID goes here (last season's ID was removed` / `//  during the 2026-27 migration). Same for the three IDs in findTags() below.` | 5, 12 |
| 5 | `RobotModel/DriveTrain/Mecanum/MecanumDrive.java:25-34` | full text below | 22, 12 |
| 6 | `RobotModel/DriveTrain/Mecanum/MecanumDrive.java:158-173` | full text below | 3, 4, 10, 22 |
| 7 | `RobotModel/DriveTrain/Mecanum/MecanumDrive.java:199-231` | full text below (line 203 is a cross-reference inside it) | 4 |
| 8 | `RobotModel/DriveTrain/Mecanum/MecanumDrive.java:245-251` | full text below | 22 |
| 9 | `RobotModel/Mechs/Assemblies/MechAssembly.java:27-46` | full text below | 4, 9 |
| 10 | `RobotModel/Mechs/Components/Claw.java:58-87` | full text below (line 82 is a cross-reference inside it) | 3, 4, 7 |
| 11 | `RobotModel/Mechs/Components/MechComponent.java:30-45` | full text below (line 39 is a cross-reference inside it) | 7 |
| 12 | `RobotModel/Mechs/Components/Turret.java:47-75` | full text below | 3, 4, 7 |

Cross-reference lines that `grep` also returns:  `MecanumDrive.java:203`, `Claw.java:82`, `MechComponent.java:39` (inside #7, #10, #11);  `Robot.java:28` (javadoc:  "See the TODO in `MecanumDrive`'s constructor");  `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/readme.md:28` ("Search the project for `TODO`...").

Full text of the multi-line TODOs:

`RobotModel/DriveTrain/Mecanum/MecanumDrive.java:25-34`
```java
        // TODO: two problems here, one structural and one physical.
        //  (1) This blocks. It sits in a while loop calling TrySleep(100) until it's
        //  done, so nothing else on the robot updates - no telemetry, no other
        //  subsystem - for the entire turn. Compare doAndWait() in StateMachine, which
        //  exists specifically so timed actions DON'T do this (see Flint Lesson 9).
        //  (2) spin() commands full power (+/-1) no matter how far off we are, but the
        //  loop exits only inside a 1-degree window. A full-power turn moves much more
        //  than 1 degree in the 100ms between checks, so it overshoots, turns back,
        //  overshoots again, and never settles. Scale the power down as |smol| shrinks
        //  (the way LimelightAutoTarget scales by -tx/10) and widen the deadband.
```

`RobotModel/DriveTrain/Mecanum/MecanumDrive.java:158-173`
```java
        // TODO: this grabs the IMU but never calls imu.initialize(...) with a
        //  RevHubOrientationOnRobot, so it has no idea how the hub is mounted. BillyRobot
        //  happens to initialize the same device separately in its own constructor, so
        //  Billy works by luck - but Wildbots2025 also uses a MecanumDrive and never
        //  initializes the IMU anywhere, so its turnToAngle() and yaw telemetry read from
        //  an unconfigured device. Two classes own the same piece of hardware, and only
        //  one of them knows it needs setup.
        //
        //  The IMU is a ROBOT-level sensor. It reports the whole chassis's heading, not
        //  this drive train's state, and more than one subsystem can want it. So the fix
        //  follows the pattern BillyRobot already uses for the Limelight: the Robot
        //  fetches it, initializes it once with the hub orientation for THAT robot, and
        //  hands it down to whoever needs it. Take the IMU as a constructor parameter
        //  here rather than fetching it - the same way OrientationConfiguration already
        //  passes in what varies per robot - and delete this lookup. One owner, one
        //  initialize, and Wildbots2025 stops reading garbage.
```

`RobotModel/DriveTrain/Mecanum/MecanumDrive.java:199-231`
```java
    // TODO (project, and a good one for somebody who wants the drive train to feel
    //  better rather than just work): this drive train owns an IMU and uses it for
    //  exactly one thing - turnToAngle() in autonomous. Teleop ignores it completely.
    //  Two worthwhile projects live here, in increasing order of difficulty. Both want
    //  the IMU ownership TODO in the constructor sorted out first.
    //
    //  1. FIELD-RELATIVE DRIVE. Right now pushing the stick "forward" means "forward for
    //     the robot", so the driver has to think in the robot's frame the whole match and
    //     it gets genuinely confusing once the robot is facing back at them. Field-relative
    //     drive rotates the stick vector by the robot's heading, so "forward" always means
    //     "away from the driver" no matter which way the robot is pointing. The whole
    //     trick is three lines, done before the existing wheel math:
    //         double theta = Math.atan2(vertical, horizontal);
    //         double r     = Math.hypot(horizontal, vertical);
    //         theta = AngleUnit.normalizeRadians(
    //                 theta - imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));
    //     then recompute horizontal/vertical from r and theta and carry on as now.
    //     See FtcRobotController's RobotTeleopMecanumFieldRelativeDrive sample. Needs a
    //     driver-accessible imu.resetYaw() so "away from me" can be re-zeroed mid-match.
    //
    //  2. HEADING HOLD / DRIFT CORRECTION. Mecanum wheels scrub, so a robot told to
    //     strafe straight drifts in rotation. Remember the heading the driver last asked
    //     for, and when the turn stick is at rest, feed a small proportional correction
    //     into `turn` to hold it:
    //         double error = AngleExtensions.getSmol(targetHeading, currentYaw);
    //         turn = error * P_TURN_GAIN;      // start near 0.02, clip to +/- 1
    //     Note AngleExtensions.getSmol() and mapToIMURange() already do the +/-180
    //     wraparound that the SDK's RobotAutoDriveByGyro_Linear sample writes inline in
    //     getSteeringCorrection() - we have those helpers, so use them.
    //
    //  Both belong HERE, in the drive train, rather than in an OpMode: they are part of
    //  what it means to drive this chassis, and every robot built on a MecanumDrive
    //  should get them for free.
```

`RobotModel/DriveTrain/Mecanum/MecanumDrive.java:245-251`
```java
        // TODO: compare these signs to AutonomousMecanumDrive.drive(x,y,t) above and to
        // spin() below. lb and rf here are flipped relative to both - a pure-turn input
        // (vertical=horizontal=0, turn!=0) works out to lf=+turn, lb=-turn, rf=+turn, rb=-turn,
        // which splits the wheels front-vs-back (LF/RF one way, LB/RB the other), not
        // left-vs-right the way spin() and the autonomous drive() do. That's not a clean
        // in-place rotation - the wheels fight each other. lb should match lf's sign and rf
        // should match rb's sign (mirroring the autonomous version) for turning to work right.
```

`RobotModel/Mechs/Assemblies/MechAssembly.java:27-46`
```java
 * <p>TODO: no assembly in this codebase owns a sensor yet, so there is no
 *  reference implementation to copy, and the `doc/FlintLessons` have no example
 *  to teach from. Billy is the obvious candidate. A beam-break or color sensor
 *  sitting between the intake and the pusher would answer "is a ball actually
 *  seated and ready to fire?" - a question that spans intake, pusher and
 *  flywheel, so it fits in none of them. With it, BillyRapidFire could wait on
 *  a real signal instead of the fixed 2200ms guess it currently uses.
 *
 *  What this layer needs, whenever the first one gets built:
 *  <ul>
 *    <li>the assembly holds the sensor handle itself, fetched in its constructor
 *        alongside its components</li>
 *    <li>its readings get reported in {@link #updateTelemetry()}, the same as any
 *        component's state</li>
 *    <li>the assembly's {@code AutonomousMechBehaviors} exposes what the sensor
 *        <em>means</em> as a verb or a question ({@code isBallSeated()}), rather
 *        than the raw reading</li>
 *    <li>the strategy lambda is where the reading changes what the components are
 *        told to do</li>
 *  </ul>
```

`RobotModel/Mechs/Components/Claw.java:58-87`
```java
    // TODO: the claw is running blind. A CRServo has no notion of position, so right now
    //  nothing in this class can answer either of the two questions a driver actually
    //  cares about: "is it fully closed?" and "am I holding something?"
    //
    //  Two different sensors, answering two different questions - decide which this
    //  season's claw needs before wiring anything:
    //    - Travel limits. Two TouchSensors at fully-open and fully-closed, enforced the
    //      way DoublyLimitedMotor enforces a motor's limits. This stops the servo
    //      straining against a hard stop, which is what burns them out.
    //    - A grip sensor. One TouchSensor on the jaw, or a DistanceSensor or color
    //      sensor looking into the grip, answering "is there a game piece in there?"
    //      That turns a blind close() into something autonomous can actually branch on,
    //      and gives isHolding() as an autonomous verb worth having.
    //
    //  Whichever gets added, the same three leaks have to be closed first, or the new
    //  sensor is decoration that nothing is obliged to respect:
    //    1. `servo` below is public - make it private.
    //    2. ClawControlStrategy.chomp(CRServo, Gamepad) hands the raw servo to the
    //       teleop lambda. Change it to chomp(Gamepad, Claw) and call
    //       `strategy.chomp(gamepad, this)`, so the lambda goes through this class.
    //    3. AutonomousClawBehaviors.open/close/stop call servo.setPower(...) directly.
    //       Route them through a decorated setPower on Claw.
    //  DoublyLimitedMotor is the worked example of all three - read it before starting.
    //
    //  See the matching TODO on Turret.java, and the rule on MechComponent. Both classes
    //  are bare single-device wrappers of one CRServo today, so there should be ONE of
    //  them, named for the device rather than for a job. Adding a sensor here turns this
    //  into the other kind of component - output device plus the sensors that guide it -
    //  which is what earns a class of its own. Collapse the bare wrappers first, then
    //  build the sensing claw on top.
```

`RobotModel/Mechs/Components/MechComponent.java:30-45`
```java
 * <p>TODO: the current components have yet to be sorted against that rule, and
 *  the naming convention for category 1 is still an open question. As it stands:
 *  <ul>
 *    <li>{@code SpinnyIntake} (one DcMotor), {@code PusherServo} (one Servo),
 *        {@code Claw} and {@code Turret} (one CRServo <em>each</em>) are all
 *        category 1, and all four are named for a job rather than a device.</li>
 *    <li>{@code Claw} and {@code Turret} are therefore two wrappers of the same
 *        device. One device-named class should serve both - until each grows the
 *        sensors it needs, at which point they become category 2 and separate
 *        classes are earned. See the TODOs on both files.</li>
 *    <li>{@code DoublyLimitedMotor}, {@code DoubleShooter} and
 *        {@code BallDetectionComponent} are already category 2 and already named
 *        for what they do.</li>
 *  </ul>
 *  Settle the category-1 naming convention before renaming anything, since every
 *  assembly's construction code moves with it.
```

`RobotModel/Mechs/Components/Turret.java:47-75`
```java
    // TODO: the turret needs a pair of travel limits. Right now it will happily keep
    //  rotating in one direction for as long as something commands it to, which wraps the
    //  wiring and eventually damages the mechanism. Add two TouchSensors at the ends of
    //  turret travel and enforce them here, the way DoublyLimitedMotor does for a motor:
    //  take both sensor names in the constructor, add canGoCW()/canGoCCW(), and put a
    //  decorated `public void setPower(double)` on Turret that zeroes any power heading
    //  into a limit that is already pressed.
    //
    //  The decorator alone is not enough, though - there are three ways to get at this
    //  servo that would walk straight past it, and all three have to close:
    //    1. `servo` below is a public field, so any caller can grab the CRServo and
    //       command it directly. Make it private.
    //    2. TurretControlStrategy.move(CRServo, Gamepad) hands the raw servo to the
    //       teleop lambda, which then calls servo.setPower(...) itself (see BillyMA).
    //       Change the interface to move(Gamepad, Turret) and call
    //       `strategy.move(gamepad, this)`, so the lambda has to go through the
    //       decorated setPower. DoublyLimitedMotorControlStrategy already has exactly
    //       this signature - copy the shape of it, not the class.
    //    3. AutonomousTurretBehaviors.setPower/turnCW/turnCCW/stop all call
    //       servo.setPower(...) directly too. Route them through the decorated method
    //       as well, or LimelightAutoTarget will drive the turret past its own limits.
    //
    //  Worth noticing what this does to the Claw/Turret duplication. Today both classes
    //  are bare single-device wrappers of one CRServo, which by the rule on
    //  MechComponent means there should be ONE of them, named for the device. Adding the
    //  limits turns this class into the other kind of component - an output device
    //  coupled to the sensors that guide it - and that is what earns it a class of its
    //  own. So the fix is two moves, not one: collapse the bare wrappers into a single
    //  device-named component, and build the limited turret on top of it.
```

Note:  `MechComponent.java:40-42` (TODO #11) names `BallDetectionComponent`, which does not exist.

### TODOs the lessons mention that no longer exist (13, all removed between `34c22ea` and `HEAD`)

| Old location (at `34c22ea`) | Topic | Lessons that cite it | Removed by |
|---|---|---|---|
| `LimelightAutoTarget.java:56` | `rotateCW` sign | GS04, clusters, cluster plan Task 7, pitfalls | `1a6809a` |
| `StateMachine.java:22` | `abort()` stale `elapsedTime` | Advanced/state-machines | `e89bec0` |
| `LimelightExtensions.java:19` | `getLatestResult()` null | GS03, GS04, pitfalls, clusters, cluster plan Task 1 | `1a6809a` |
| `BillyMA.java:28` | `telemetry` field shadows base | (TelemetryContract checklist) | `1a6809a` |
| `BillyMA.java:36` | leftover `tel` ctor param | -- | `1a6809a` |
| `BillyMA.java:44` | intake two-`if` | GS01, 03, pitfalls | `1a6809a` |
| `BillyMA.java:113` | dead auton/BRF construction in ctor | -- | `e89bec0` |
| `BillyMA.java:149` | turret two owners | 05, 07, state-machines, control-ownership | `1a6809a` |
| `BillyMA.java:233` | empty `updateTelemetry()` | 04, TelemetryContract | `1a6809a` |
| `PusherServo.java:16` | direction set only by teleop path | 03, pitfalls | `1a6809a` |
| `SpinnyIntake.java:17` | auton signs | 02 (implicitly), pitfalls | `1a6809a` |
| `Turret.java:24` | `turnCW/turnCCW` sleep 100 ms | pitfalls | `1a6809a` |
| `BillyRobot.java:113` | unattached `AprilTagProcessor` | GS04, apriltags-and-navigation | `1a6809a` |

The "eighteen" in `README.md:10` and `GettingStarted/01:230`:  at `34c22ea` there were 25 distinct TODO comments (30 grep lines);  now 12 (17 grep lines).  Neither count is 18.

---

## B.  What the Phase 2-5 work built

### B.1  The pieces

1. **`MechAssembly.IMacroAssemblyStrategy<R>`** -- a public generic interface on the abstract base (`MechAssembly.java:60-68`).  `R` is the resource type (Billy uses `BillyMA.Part`).
2. **`MacroManager<R>`** -- new class in `RobotModel/Mechs/Assemblies/`.  Holds at most one active macro.
3. **`Robot.IAssistRobotStrategy<R>`** -- a public generic interface on the abstract base (`Robot.java:59-66`).  No manager class;  `BillyRobot` manages its single assist inline.
4. **`BillyMA.Part`** -- `INTAKE, PUSHER, FLYWHEEL, TURRET` (`BillyMA.java:25-30`).  The resource vocabulary.
5. **Claims-aware default strategy** -- `BillyAssemblyStrategy.execute(BillyMA, Gamepad, Set<Part> claimed)`;  skips any claimed part.
6. **`BillyRapidFire implements IMacroAssemblyStrategy<BillyMA.Part>`** -- claims `PUSHER`, `FLYWHEEL`.
7. **`LimelightAutoTarget implements Robot.IAssistRobotStrategy<BillyMA.Part>`** -- claims `TURRET`.
8. **`BillyRobot` arbitration** -- `assist` slot, `startAssist`, `stopAssist`, `assistClaims`, `stopAll`, `overlaps`, all button handling, and the strategy-status telemetry.

### B.2  `MacroManager` -- whole file (public surface:  `lastEnd()`, `request(...)`, `cancel(String)`, `claimed()`, `activeName()`, `isRunning()`, `update(Gamepad)`)

`RobotModel/Mechs/Assemblies/MacroManager.java:1-61`
```java
package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Collections;
import java.util.Set;

public class MacroManager<R>
{
    private MechAssembly.IMacroAssemblyStrategy<R> active;   // null = no macro running
    private String lastEnd = "None";  
    public String lastEnd() { return lastEnd; }

    public void request(MechAssembly.IMacroAssemblyStrategy<R> macro) 
    { 
        if (active != null) return;   // != here: something's already running, so ignore this request
        active = macro;   
        active.start();
    }
    public void cancel(String reason)
    {
        if (active == null)
            return;
        active.cancel();
        lastEnd = active.name() + " " + reason;
        active = null;
    }

    public Set<R> claimed() 
    {  
        if (active == null)   
            return Collections.emptySet();           
        return active.claims();      // return what active claims    
    }  
    

    public String activeName()
    {           
        if (active == null)   
            return "None";           
        
        return active.name();       
    }

    public boolean isRunning() 
    {           
        return active != null;      // true while a macro is running
    }

    public void update(Gamepad gamepad)
    {
        if (active == null)
            return;
        active.update(gamepad);
        if (active.isComplete()) {
            lastEnd = active.name() + " completed";
            active = null;
        }
    }

}
```

Behavior:

- `request` is **ignored** if a macro is already active (`:16`).  Macro-vs-macro is "first wins", not newest.
- `update` steps the macro and, when `isComplete()`, records `"<name> completed"` and releases it (`:50-59`).
- `cancel(reason)` calls the macro's `cancel()`, records `"<name> <reason>"`, releases it (`:20-27`).
- `isRunning()` is never called anywhere.

### B.3  What `BillyMA` does differently

- Constructor takes only `HardwareMap` (`BillyMA.java:52`);  the old `Telemetry tel` param and shadow field are gone.
- Owns `private final MacroManager<Part> macros` (`:49`) and `BRF` (built in `initializeTelemetry`, `:174`;  no `abort()`).
- Public macro facade:

`RobotModel/Mechs/Assemblies/BillyMA.java:32-42`
```java
    public void requestRapidFire()
    {
        macros.request(BRF);
    }
    public void cancelMacro(String reason) { macros.cancel(reason); }
    public String lastMacroEnd() { return macros.lastEnd(); }
    public Set<Part> macroClaims()
    {
        return macros.claimed();
    }
    public String macroName() { return macros.activeName(); }
```

- `giveInstructions(Gamepad)` delegates to `giveInstructions(gamepad, EnumSet.noneOf(Part.class))`;  the two-arg form steps the macro first, then runs the default strategy with `macro claims ∪ robot claims` (`:190-203`, chapter 13).
- `BillyMA` no longer reads `gamepad.a` itself;  **the trigger moved up to `BillyRobot`** (`gamepad2.aWasPressed()`).
- `updateTelemetry()` writes `"--- BillyMA ---"` then visits all four components (`:211-218`).

### B.4  What `Robot` does differently

Only the new nested interface (`Robot.java:59-66`) and a `java.util.Set` import.  `Robot.update`, `updateTelemetry`, `initializeSubsystems` are unchanged.  The base class has **no** assist slot, manager, or arbitration.

### B.5  What `BillyRapidFire` does differently

- Implements `IMacroAssemblyStrategy<BillyMA.Part>`.
- Constructor no longer starts the machine;  stores `shotsPerRun` (`BillyRapidFire.java:37-41`).
- `start()` -> `reset(shotsPerRun)`;  `update(gamepad)` -> `updateState()`;  `cancel()` -> `abort()` + `StopShoot()` + `retractPusher()`;  `name()` -> `"BillyRapidFire"`;  `claims()` -> `PUSHER, FLYWHEEL` (`:43-79`).
- `isComplete()` is inherited from `StateMachine` and satisfies the interface.

### B.6  What `LimelightAutoTarget` does differently

- Implements `Robot.IAssistRobotStrategy<BillyMA.Part>`:  `claims()` = `TURRET`, `name()` = `"LimelightAutoTarget"`, `start()` = `currentState = lookForTag()`, `update(driver, mech)` = `updateState()`, `stop()` = `abort()` + `turret.stop()` (`LimelightAutoTarget.java:48-73`).
- `rotateCW` default power is `-1` (`:90`).
- Imports `BillyMA` and `Robot`, so it is now Billy-specific by type.

### B.7  What `BillyRobot` does differently

Constructor tail, strategy and arbitration methods:

`RobotModel/Robots/BillyRobot.java:120-219`
```java
        initializeSubsystems();

        auton = new BillyRobot.AutonomousMecanumRobot(
                driveTrain.getAutonomousDriving(),
                mechAssembly.getAutonomousBehaviors(),
                telemetry);

        targeter = new LimelightAutoTarget(
                limelight,
                ((BillyMA)mechAssembly).getAutonomousBehaviors().autonTurret,
                tagID);

        strategy = (robot, gamepad1, gamepad2)  -> {
            if (gamepad1.yWasPressed()) {
                if (assist == null)
                    startAssist(targeter);
                else
                    stopAssist("turned off by y");
            }

            BillyMA mech = (BillyMA)mechAssembly;
            if (gamepad2.aWasPressed()) {
                mech.requestRapidFire();
                if (overlaps(mech.macroClaims(), assistClaims()))
                    stopAssist("replaced by " + mech.macroName());
            }

            if (gamepad2.bWasPressed()) {
                mech.cancelMacro("cancelled by b");
            }

            if(gamepad1.bWasPressed()) {
                stopAll();
            }

            driveTrain.drive(gamepad1);
            mech.giveInstructions(gamepad2, assistClaims());
            if (assist != null) {
                assist.update(gamepad1, gamepad2);
            }
        };
        startAssist(targeter);
    }

    public void stopAll() {
        ((BillyMA)mechAssembly).cancelMacro("stopped by stop all");
        stopAssist("stopped by stop all");
    }

    private boolean overlaps(Set<BillyMA.Part> a, Set<BillyMA.Part> b) {
        return !Collections.disjoint(a, b);
    }

    @Override
    public void update(Gamepad gamepad1, Gamepad gamepad2) {
       strategy.execute(this, gamepad1, gamepad2);
    }

    @Override
    public void updateTelemetry() {
        BillyMA mech = (BillyMA) mechAssembly;
        if (assist == null)
            telemetry.addData("Assist", "None");
        else
            telemetry.addData("Assist", assist.name() + " " + assist.claims());
        telemetry.addData("Macro", mech.macroName() + " " + mech.macroClaims());
        telemetry.addData("Last macro", mech.lastMacroEnd());
        telemetry.addData("Last assist", lastAssistEnd);
        super.updateTelemetry();
    }

    public void startAssist(IAssistRobotStrategy<BillyMA.Part> newAssist) {
        BillyMA mech = (BillyMA) mechAssembly;
        if (overlaps(newAssist.claims(), mech.macroClaims()))
            mech.cancelMacro("replaced by " + newAssist.name());

        if (assist != null) {
            lastAssistEnd = assist.name() + " replaced by " + newAssist.name();
            assist.stop();
        }
        assist = newAssist;
        assist.start();
    }

    private String lastAssistEnd = "None";

    public Set<BillyMA.Part> assistClaims() {
        if(assist == null) {
            return EnumSet.noneOf(BillyMA.Part.class);
        }
        return assist.claims();
    }

    public void stopAssist(String reason) {
        if (assist == null)
            return;
        assist.stop();
        lastAssistEnd = assist.name() + " " + reason;
        assist = null;
    }
```

**Button map now** (all in `BillyRobot`'s strategy, edge-triggered SDK `*WasPressed()`):

| Button | Action | Line |
|---|---|---|
| `gamepad1.y` | toggle the targeter assist (start if none, else stop with `"turned off by y"`) | `:133-138` |
| `gamepad2.a` | `requestRapidFire()`;  if the active macro's claims overlap the assist's, stop the assist (`"replaced by <macro>"`) | `:141-145` |
| `gamepad2.b` | `cancelMacro("cancelled by b")` | `:147-149` |
| `gamepad1.b` | `stopAll()` | `:151-153` |

Manual mappings unchanged in `BillyMA`:  `gamepad2` dpad up/down = intake ±0.75 **and** flywheel trim ±0.05;  `x` = pusher;  `y` = flywheel;  bumpers = turret.

**Per-tick order:**  y-toggle -> a-request -> b2-cancel -> b1-stopAll -> `driveTrain.drive(gamepad1)` -> `mech.giveInstructions(gamepad2, assistClaims())` [= `macros.update` then default strategy with claims] -> `assist.update(...)`.

**Default state at construction:**  the targeter is started (`:161`), so the turret is claimed from the first loop.

### B.8  How turret ownership is decided now

- If an assist is active and claims `TURRET` (the targeter does), `BillyMA`'s default strategy skips `turret.move(gamepad)`;  only the assist commands the turret.
- If no assist is active, the default strategy drives the turret from the bumpers.
- Rapid fire never claims the turret, so it never affects turret ownership.
- Driver input does **not** take the turret back from an assist;  the driver must press `gamepad1.y` or `gamepad1.b`.
- Nothing prevents other code from calling `autonTurret.setPower(...)` directly (the public `servo` field and raw-servo strategy signature are still open, `Turret.java:47-76`).  Ownership is a convention enforced at the two call sites, not by the component.

### B.9  "Newest request wins" in code

- **Assist arrives** (`startAssist`, `:191-202`):  if its claims overlap the active macro's, the macro is cancelled (`"replaced by <assist>"`);  any existing assist is stopped (`"<old> replaced by <new>"`).  New assist starts.
- **Macro requested** (`:141-145`):  `requestRapidFire()`;  then if the (now) active macro overlaps the assist, the assist is stopped (`"replaced by <macro>"`).
- **Exceptions:**  macro-vs-macro is **first wins** (`MacroManager.java:16`).  If `request` was ignored because a macro was already running, the overlap check still runs against the *already running* macro.
- **With today's strategies the cross-cancel branches never fire:**  `{PUSHER, FLYWHEEL}` and `{TURRET}` are disjoint.  They are untested by real use.

### B.10  "stopAll" in code

`RobotModel/Robots/BillyRobot.java:164-167`
```java
    public void stopAll() {
        ((BillyMA)mechAssembly).cancelMacro("stopped by stop all");
        stopAssist("stopped by stop all");
    }
```

Cancels the macro (flywheel stopped, pusher retracted via `BillyRapidFire.cancel()`) and stops the assist (turret power 0).  It does **not** stop the drive train or the manual mechanisms;  default control resumes in the same tick.  It is a method, not a strategy object.

### B.11  New telemetry

`BillyRobot.updateTelemetry()` (`:178-189`) adds four lines **before** the drive train and assembly:

| Key | Value | Example |
|---|---|---|
| `Assist` | `"None"` or `name + " " + claims` | `LimelightAutoTarget [TURRET]` |
| `Macro` | `macroName() + " " + macroClaims()` | `None []` / `BillyRapidFire [PUSHER, FLYWHEEL]` |
| `Last macro` | `lastMacroEnd()` | `None`, `BillyRapidFire completed`, `BillyRapidFire cancelled by b`, `BillyRapidFire stopped by stop all`, `BillyRapidFire replaced by LimelightAutoTarget` |
| `Last assist` | `lastAssistEnd` | `None`, `LimelightAutoTarget turned off by y`, `... stopped by stop all`, `... replaced by BillyRapidFire`, `<old> replaced by <new>` |

Then `super.updateTelemetry()`:  `MecanumDrive` (`yaw:`, `LF/LB/RF/RB power:`), then `"--- BillyMA ---"`, `intake power:`, `intake position:`, `pusher position`, `power:` (flywheel), `turret position` (actually `servo.getPower()`), then the single flush.  Status lines written by the strategies during `update` (`Tag N not found.`, `Turret Stopped`, `Turret: stopped`, `Turret CW/CCW`, `Turret power`, rapid-fire statuses) sit in the same frame, above these.  Lifecycle state is **not** reported.

### B.12  Plan vs. built (`doc/ControlStrategyExpansionPlan.md`)

| Plan idea (plan line) | Built? | Where / notes |
|---|---|---|
| Phase 1:  vocabulary (484-496) | Yes (in docs/lessons) | |
| Phase 2:  formal assembly strategy (498-512) | **Yes, partly** | `IMacroAssemblyStrategy<R>`, `MacroManager<R>`, `BillyRapidFire` migrated.  Default strategy is still a plain lambda, not a strategy object |
| Phase 3:  formal robot strategy (514-528) | **Yes, partly** | `IAssistRobotStrategy<R>`, `LimelightAutoTarget` migrated.  No base-class support |
| Phase 4:  runtime selection + arbitration (530-541) | **Yes, simple** | button requests, claims, overlap cancel, auto-return to default |
| Phase 5:  telemetry (543-554):  name, owned resources, lifecycle state, end reason | name **yes**, resources **yes**, end reason **yes**, lifecycle state **no** | `BillyRobot.java:178-189` |
| Phase 6:  expand robot-by-robot (556-566) | **No** | only Billy uses any of it |
| Resource ownership / declared claims (326-350) | **Yes** at assembly sub-resource level | drive-train resources (translation/rotation) not claimable |
| Arbitration rule 1:  exclusive beats default on owned resources (356) | **Yes** | claims skip in `BillyMA` |
| Rule 2:  safety/stop beats everything (357) | **Partly** | `stopAll()` button;  not a priority level, doesn't touch drive |
| Rule 3:  assists coexist only if disjoint (358) | **Partly** | only one assist slot exists, so assist-vs-assist is replace, not coexist;  assist-vs-macro overlap cancels the older |
| Rule 4:  loser cleanly interrupted (359) | **Yes** | `cancel()` / `stop()` called on the loser |
| Assembly strategy manager:  register, track, select defaults, resolve conflicts (242-250) | **Partly** | `MacroManager` tracks one active macro;  no registry, no default selection, no conflict resolution between macros |
| Robot strategy manager (252-259) | **No class** | inline in `BillyRobot` |
| Lifecycle states registered/available/active/suspended/completed/cancelled (369-378) | **No** | only active vs. not;  "completed"/"cancelled" exist as end-reason strings |
| Lifecycle events enter/update/complete/interrupt/cancel/exit (380-389) | **Partly** | `start`/`update`/`isComplete`/`cancel` (macro);  `start`/`update`/`stop` (assist);  no interrupt/suspend/exit |
| Selection triggers:  press / hold / toggle / sensor / timer / auton (411-420) | press **yes**, toggle **yes** (`y`), others **no** | |
| Mode strategies (307-318) | **No** | |
| Assembly-level assist ("spin up shooter while aiming", 187) | **No** | |
| Strategy separate from selection (135-140) | **Yes** at assembly layer;  robot layer mixes them in `BillyRobot` | |
| State machines as implementation detail (147-152, 452-477) | **Yes** | both strategies extend `StateMachine`, managers ignore that |

---

## C.  Current shape of the key classes

### C.1  `MechComponent` (javadoc 7-57 omitted;  the rule text is in chapter 7)

`RobotModel/Mechs/Components/MechComponent.java:58-117`
```java
public abstract class MechComponent
{
    /**
     * Autonomous-facing surface for this component. Exposes
     * {@link #reportStatus} and {@link #reportData} so autonomous strategies
     * can write telemetry through the robot object model without receiving a
     * raw {@code Telemetry} parameter directly (Susan Zuo — fixes Bug #6:
     * "Autonomous code forced to use raw telemetry because proper abstractions
     * don't exist").
     */
    public abstract class AutonomousComponentBehaviors {
        protected final Telemetry telemetry;
        public AutonomousComponentBehaviors(Telemetry telemetry) {
            this.telemetry = telemetry;
        }
        /** Adds a status line to the telemetry buffer. Does NOT flush. */
        public void reportStatus(String status) { telemetry.addLine(status); }
        /** Adds a key-value pair to the telemetry buffer. Does NOT flush. */
        public void reportData(String key, Object value) { telemetry.addData(key, value); }
    }
    public abstract <T extends AutonomousComponentBehaviors> T getAutonomousBehaviors();
    protected interface IControlStrategy { }

    protected IControlStrategy strategy;
    protected Telemetry telemetry;

    protected MechComponent(IControlStrategy strategy)
    {
        this.strategy = strategy;
    }

    /**
     * Two-phase initializer: stores the telemetry reference and creates the
     * autonomous behavior object. Called by the enclosing assembly's own
     * {@code initializeTelemetry} — never call from a constructor.
     *
     * <p>Kept separate from the constructor so existing robot construction
     * code requires minimal changes (Susan Zuo — "Benefit: Existing robot
     * construction code requires minimal changes. Cost: Two-phase
     * initialization creates potential for incomplete setup.").
     */
    public void initializeTelemetry(Telemetry telemetry)
    {
        this.telemetry = telemetry;
    }

    public abstract void move(Gamepad gamepad);

    /**
     * Write component state to the owned telemetry buffer. Never calls
     * {@code telemetry.update()} — that is the sole responsibility of
     * {@code Robot.updateTelemetry()} (Susan Zuo — "Single Point of Control:
     * Only Robot is allowed to call telemetry.update()").
     */
    // Note: this and its overrides were package-private until the 2026-27 migration.
    // MechAssembly subclasses live in a different package (…Mechs.Assemblies), so they
    // could not call a component's update() at all - which is why CascadeArm and
    // ExampleIntakeAssembly wouldn't compile. Both are public now; keep them that way.
    public abstract void update();
}
```

### C.2  `MechAssembly` (javadoc 9-55 omitted;  see chapter 9 and TODO #9)

`RobotModel/Mechs/Assemblies/MechAssembly.java:56-113`
```java
public abstract class MechAssembly
{
    protected interface IAssemblyStrategy { }
    
    public interface IMacroAssemblyStrategy<R>
    { 
        Set<R> claims();
        void start();
        void update(Gamepad gamepad);
        boolean isComplete();
        void cancel();
        String name();
    }
    protected MechAssembly.IAssemblyStrategy strategy;

    /**
     * Autonomous-facing surface for this assembly. Provides
     * {@link #reportStatus} and {@link #reportData} so autonomous strategies
     * do not need a raw {@code Telemetry} parameter (Susan Zuo — fixes
     * Bug #6: "Autonomous code forced to bypass the object model").
     */
    public abstract class AutonomousMechBehaviors {
        protected final Telemetry telemetry;
        public AutonomousMechBehaviors(Telemetry telemetry) {
            this.telemetry = telemetry;
        }
        /** Adds a status line to the telemetry buffer. Does NOT flush. */
        public void reportStatus(String status) { telemetry.addLine(status); }
        /** Adds a key-value pair to the telemetry buffer. Does NOT flush. */
        public void reportData(String key, Object value) { telemetry.addData(key, value); }
    }
    public abstract <T extends AutonomousMechBehaviors> T getAutonomousBehaviors();

    /**
     * Pass along gamepad instructions to subcomponents.
     * @param gamepad the Gamepad
     */
    public abstract void giveInstructions(Gamepad gamepad);

    protected Telemetry telemetry;

    /**
     * Stores the telemetry reference, propagates it to every child component,
     * then creates the autonomous behavior object. Called once by
     * {@code Robot.initializeSubsystems()} (Susan Zuo — two-phase
     * initialization: "construct → initializeTelemetry").
     */
    public abstract void initializeTelemetry(Telemetry telemetry);

    /**
     * Write assembly and component state to the owned telemetry buffer. Must
     * call {@code update()} on every child component so no component is
     * silently missing from the display (Susan Zuo — fixes Bug #5:
     * "Assemblies fail to report telemetry from all their child components").
     * Never calls {@code telemetry.update()}.
     */
    public abstract void updateTelemetry();
}
```

### C.3  `Robot` (javadoc 11-51 omitted)

`RobotModel/Robots/Robot.java:52-156`
```java
public abstract class Robot
{

    protected interface IRobotStrategy {}

    protected Robot.IRobotStrategy strategy;

    public interface IAssistRobotStrategy<R>
    { 
        Set<R> claims();
        void start();
        void update(Gamepad gamepad1, Gamepad gamepad2);
        void stop();
        String name();
    }

    /**
     * Autonomous-facing surface for the full robot. Provides
     * {@link #reportStatus}, {@link #reportData}, and {@link #clearTelemetry}
     * so top-level autonomous strategies do not need a raw {@code Telemetry}
     * parameter (Susan Zuo — fixes Bug #6: "AutonomousRobot provides no
     * telemetry support — forces strategies to use raw telemetry").
     */
    public abstract class AutonomousRobot
    {
        protected final Telemetry telemetry;

        /**
         * Requires that any extending class provide matching parameter types in its constructor.
         * @param driveTrain Any AutonomousDriving implementation
         * @param mechAssembly Any AutonomousMechBehaviors implementation
         * @param telemetry Telemetry injected at construction time
         */
        public AutonomousRobot(
                DriveTrain.AutonomousDriving driveTrain,
                MechAssembly.AutonomousMechBehaviors mechAssembly,
                Telemetry telemetry)
        {
            this.telemetry = telemetry;
        }

        /** Adds a status line to the telemetry buffer. Does NOT flush. */
        public void reportStatus(String status) { telemetry.addLine(status); }
        /** Adds a key-value pair to the telemetry buffer. Does NOT flush. */
        public void reportData(String key, Object value) { telemetry.addData(key, value); }
        public void clearTelemetry() { telemetry.clear(); }
    }

    protected interface IControlStrategy {  }


    /**
     * This abstract method definition tells inheriting classes that they MUST define a
     * way to run Autonomously.
     * @return a CONCRETE implementation of the ABSTRACT AutonomousRobot type.
     * @param <T> Any type which extends AutonomousRobot
     */
    public abstract <T extends AutonomousRobot> T getAutonomousRobot();

    protected VisionPortal visionPortal;
    protected DriveTrain driveTrain;
    protected MechAssembly mechAssembly;
    protected Telemetry telemetry;

    protected Robot(Telemetry telemetry)
    {
        this.telemetry = telemetry;
    }

    /**
     * Propagates the owned telemetry reference to driveTrain and mechAssembly,
     * triggering their two-phase initialization. Call this in subclass
     * constructors after both fields are assigned (Susan Zuo — "construct →
     * initializeTelemetry" two-phase pattern).
     */
    protected void initializeSubsystems()
    {
        if (driveTrain != null) driveTrain.initializeTelemetry(telemetry);
        if (mechAssembly != null) mechAssembly.initializeTelemetry(telemetry);
    }

    /**
     * The single flush point for all telemetry in the robot. Collects data
     * from driveTrain and mechAssembly, then calls {@code telemetry.update()}
     * exactly once per loop (Susan Zuo — "Single Point of Control: only Robot
     * is allowed to call telemetry.update(). All other layers write data but
     * never flush." Fixes Bugs #1, #2, #7).
     */
    public void updateTelemetry() {
        if (driveTrain != null) driveTrain.updateTelemetry();
        if (mechAssembly != null) mechAssembly.updateTelemetry();
        telemetry.update();
    }

    /**
     * update the robot state!  Pass along the gamepads to the different modules.
     * @param gamepad1 driver gamepad
     * @param gamepad2 mech gamepad
     */
    public void update(Gamepad gamepad1, Gamepad gamepad2)
    {
        if (driveTrain != null) driveTrain.drive(gamepad1);
        if (mechAssembly != null) mechAssembly.giveInstructions(gamepad2);
    }
}
```

### C.4  `StateMachine` (whole file, includes `doAndWait`)

`AutonStrategies/StateMachine.java:1-68`
```java
package org.firstinspires.ftc.teamcode.AutonStrategies;


import com.qualcomm.robotcore.util.ElapsedTime;


import org.firstinspires.ftc.teamcode.Extensions.IState;


public abstract class StateMachine {
    /* Every State Machine must be aware of its Current State */
    protected IState currentState;


    /* if this state machine can terminate, it does so by setting currentState to NULL. ...*/
    public boolean isComplete() {
        return currentState == null;
    }


    /* Abort the Current execution - no questions asked! */
    public void abort() {
        currentState = null;
        elapsedTime = null;
    }


    /* Every State Machine must be able to update its state.*/
    public void updateState() {
        if (currentState != null)
            currentState = currentState.execute();
    }


    private ElapsedTime elapsedTime;


    /**
     * helper method for non-blocking, time based states!
     * @param action the thing you're doing!
     * @param millis how long to wait before proceeding
     * @param nextState the next thing to do!
     * @return the appropriate state.
     */
    protected IState doAndWait(Runnable action, int millis, IState nextState) {
        return () ->
        {
            if (elapsedTime == null) // if we are STARTING this state
            {   // this is an initialization step.
                elapsedTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
                // DO THE THING!
                action.run();
            }


            // if we are still waiting... go let other things happen!!
            // this is the non blocking step.
            if(elapsedTime.milliseconds() < millis)
                return doAndWait(action, millis, nextState);
            // if we have waited long enough, clear the clock
            // and go on over to the next state!
            elapsedTime = null;
            return nextState;
        };


    }
}
```

### C.5  `IState`

`Extensions/IState.java:1-5`
```java
package org.firstinspires.ftc.teamcode.Extensions;

public interface IState {
    public IState execute();
}
```

### C.6  `IAutonStrategy`

`AutonStrategies/IAutonStrategy.java:1-6`
```java
package org.firstinspires.ftc.teamcode.AutonStrategies;

public interface IAutonStrategy
{
    void execute();
}
```

### C.7  `BillyMA` -- skeleton, constructor, strategy lambda, instructions

`RobotModel/Mechs/Assemblies/BillyMA.java:18-50`
```java
public class BillyMA extends MechAssembly {

    public interface BillyAssemblyStrategy extends IAssemblyStrategy
    {
        void execute(BillyMA mechAssembly, Gamepad gamepad, Set<Part> claimed);
    }

    public enum Part {
        INTAKE,
        PUSHER,
        FLYWHEEL,
        TURRET
    }

    public void requestRapidFire()
    {
        macros.request(BRF);
    }
    public void cancelMacro(String reason) { macros.cancel(reason); }
    public String lastMacroEnd() { return macros.lastEnd(); }
    public Set<Part> macroClaims()
    {
        return macros.claimed();
    }
    public String macroName() { return macros.activeName(); }

    private final SpinnyIntake intake;
    private final PusherServo ballPusher;
    private final DoubleShooter flywheel;
    private final Turret turret;
    private BillyRapidFire BRF = null;
    private final MacroManager<Part> macros = new MacroManager<>();
    protected BillyAssemblyStrategy strategy;
```

Constructor (components + strategy lambda):

`RobotModel/Mechs/Assemblies/BillyMA.java:52-129`
```java
    public BillyMA(HardwareMap hardwareMap) {
        intake = new SpinnyIntake(hardwareMap, "intakeMotor",
                (motor, gamepad) -> {
                    if (gamepad.dpad_up) {
                    motor.setPower(0.75); }
                    else if (gamepad.dpad_down) {
                    motor.setPower(-0.75);
                    } 
                    else {
                        motor.setPower(0);
                    }
                });

        ballPusher = new PusherServo(hardwareMap,
                "ballPusherServo",
                (servoR, gamepad) -> {
                    if (gamepad.x) {
                        servoR.setPosition(0.8);
                    } else {
                        servoR.setPosition(0.0);
                    }
                },
                (servo, telemetry) -> {
                    telemetry.addData("pusher position", servo.getPosition());
                });

        flywheel = new DoubleShooter(hardwareMap, "flywheelMotorF", "flywheelMotorB",
                (motorF, motorB,gamepad) -> {
                    double power = 0.45;
                    if (gamepad.dpad_up) {
                        power += 0.05;
                    }
                    if (gamepad.dpad_down) {
                        power -= 0.05;
                    }

                    if (gamepad.y) {
                        motorF.setPower(power);
                        motorB.setPower(-power);
                    } else {
                        motorF.setPower(0);
                        motorB.setPower(0);
                    }
                }, (motorF, motorB, telemetry) -> {
                    telemetry.addData("power:", motorF.getPower());
                });

        turret = new Turret(hardwareMap, "turretServo",
                (servo, gamepad) -> {
                    if (gamepad.right_bumper)
                    {
                        servo.setPower(-1);
                    }
                    else if (gamepad.left_bumper)
                    {
                        servo.setPower(1);
                    }
                    else {
                        servo.setPower(0);
                    }
                },
                (servo, telemetry) -> {
                    telemetry.addData("turret position", servo.getPower());
                });

        strategy = (mechAssembly, gamepad, claimed) -> {

            if (!claimed.contains(Part.INTAKE))
                intake.move(gamepad);
            if (!claimed.contains(Part.PUSHER))
                ballPusher.move(gamepad);
            if (!claimed.contains(Part.FLYWHEEL))
                flywheel.move(gamepad);
            if (!claimed.contains(Part.TURRET))
                turret.move(gamepad);

        };
    }
```

Autonomous aggregate, `initializeTelemetry`, `giveInstructions`, `updateTelemetry`:

`RobotModel/Mechs/Assemblies/BillyMA.java:131-220`
```java
    public class AutonomousBillyMA extends AutonomousMechBehaviors {
        public final SpinnyIntake.AutonomousIntakeBehaviors autonIntake;
        public final PusherServo.AutonomousBallPusherBehaviors autonBallPusher;
        public final DoubleShooter.AutonomousShooterBehavior autonFlywheel;
        public final Turret.AutonomousTurretBehaviors autonTurret;

        public AutonomousBillyMA(
                SpinnyIntake.AutonomousIntakeBehaviors autonIntake,
                PusherServo.AutonomousBallPusherBehaviors autonBallPusher,
                DoubleShooter.AutonomousShooterBehavior autonFlywheel,
                Turret.AutonomousTurretBehaviors autonTurret,
                Telemetry telemetry) {
            super(telemetry);
            this.autonIntake = autonIntake;
            this.autonBallPusher = autonBallPusher;
            this.autonFlywheel = autonFlywheel;
            this.autonTurret = autonTurret;
        }
    }

    private AutonomousBillyMA auton;

    /**
     * Propagates telemetry to all four components so each can lazily create its own
     * autonomous behavior object, then assembles {@code AutonomousBillyMA} from the
     * results. {@code BillyRapidFire} is also created here so
     * it is never null when {@link #giveInstructions} first runs (Susan Zuo —
     * two-phase initialization pattern; previously the constructor accepted a
     * {@code Telemetry} arg, coupling construction to telemetry lifetime).
     */
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
        intake.initializeTelemetry(telemetry);
        ballPusher.initializeTelemetry(telemetry);
        flywheel.initializeTelemetry(telemetry);
        turret.initializeTelemetry(telemetry);
        auton = new AutonomousBillyMA(
                intake.getAutonomousBehaviors(),
                ballPusher.getAutonomousBehaviors(),
                flywheel.getAutonomousBehaviors(),
                turret.getAutonomousBehaviors(),
                telemetry);
        BRF = new BillyRapidFire(auton, 3);
    }

    @Override
    public AutonomousBillyMA getAutonomousBehaviors() {
        return auton;
    }

    /**
     * Drives all four components and advances the BillyRapidFire state machine each
     * loop. Does NOT call {@code telemetry.update()} — flushing is the exclusive
     * responsibility of {@code Robot.updateTelemetry()} (Susan Zuo — Bug #7:
     * "mid-cycle {@code telemetry.update()} inside giveInstructions caused the
     * driver-station display to flicker and showed partial data from the previous
     * loop iteration").
     */
    @Override
    public void giveInstructions(Gamepad gamepad) {
        giveInstructions(gamepad, EnumSet.noneOf(Part.class));
    }

    public void giveInstructions(Gamepad gamepad, Set<Part> robotClaims) {                     

        macros.update(gamepad);     // 1. step the macro (and release it when done)  

        Set<Part> claimed = EnumSet.noneOf(Part.class);
        claimed.addAll(macros.claimed());
        claimed.addAll(robotClaims);
        strategy.execute(this, gamepad, claimed);
    }

    /**
     * Collects telemetry from all four components. Previously omitted
     * {@code intake.update()} entirely (Susan Zuo — Bug #5: "BillyMA.updateTelemetry
     * omits intake — drivers had no visibility into intake state during matches").
     * Never flushes.
     */
    @Override
    public void updateTelemetry() {
        telemetry.addLine("--- BillyMA ---");
        intake.update();
        ballPusher.update();
        flywheel.update();
        turret.update();
    }

}
```

### C.8  `BillyRobot` -- whole file

`RobotModel/Robots/BillyRobot.java:26-220`
```java
public class BillyRobot extends Robot {
    public class AutonomousMecanumRobot extends AutonomousRobot
    {
        public final MecanumDrive.AutonomousMecanumDrive driveTrain;
        public final BillyMA.AutonomousBillyMA mechAssembly;

        public AutonomousMecanumRobot(MecanumDrive.AutonomousMecanumDrive driveTrain,
                                      BillyMA.AutonomousBillyMA mechAssembly,
                                      Telemetry telemetry)
        {
            super(driveTrain, mechAssembly, telemetry);
            this.driveTrain = driveTrain;
            this.mechAssembly = mechAssembly;
        }


        public double getHeading() {
            YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
            return orientation.getYaw(AngleUnit.DEGREES);
        }
        public double angleWrap(double angle)
        {
            while (angle > 180) angle -= 360;
            while (angle < -180) angle += 360;
            return angle;
        }
    }

    public interface BillyRobotStrategy extends IRobotStrategy
    {
        void execute (BillyRobot robot, Gamepad g, Gamepad h);
    }


    private final AutonomousMecanumRobot auton;
    public final Limelight3A limelight;
    public final IMU imu;

    public final LimelightAutoTarget targeter;
    private IAssistRobotStrategy<BillyMA.Part> assist = null; //null = no assist on

    protected BillyRobotStrategy strategy;


    @Override
    public BillyRobot.AutonomousMecanumRobot getAutonomousRobot() {
        return auton;
    }

    /**
     * Constructs all subsystems and wires them together. Key decisions (Susan Zuo):
     * <ul>
     *   <li>{@code super(telemetry)} stores the telemetry reference in {@code Robot}
     *       before any subsystem is initialized.</li>
     *   <li>{@code BillyMA} no longer accepts a {@code Telemetry} arg — telemetry is
     *       injected later via {@link #initializeSubsystems()} which calls
     *       {@code initializeTelemetry} on every subsystem (two-phase init).</li>
     *   <li>{@code LimelightAutoTarget} no longer accepts a {@code Telemetry} arg —
     *       it reports through {@code Turret.AutonomousTurretBehaviors} which already
     *       holds the telemetry reference (Bug #6: "autonomous strategies held raw
     *       telemetry references, bypassing the object model").</li>
     * </ul>
     */
    public BillyRobot(HardwareMap hardwareMap, Telemetry telemetry, int tagID) {
        super(telemetry);

        driveTrain = new MecanumDrive(hardwareMap, new MecanumDrive.OrientationConfiguration(
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.REVERSE,
                DcMotorSimple.Direction.FORWARD)
        );

        mechAssembly = new BillyMA(hardwareMap);

        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.start();
        limelight.pipelineSwitch(0);

        imu = hardwareMap.get(IMU.class, "imu");

        IMU.Parameters parameters = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP
                )
        );

        imu.initialize(parameters);
        imu.resetYaw();

        

        initializeSubsystems();

        auton = new BillyRobot.AutonomousMecanumRobot(
                driveTrain.getAutonomousDriving(),
                mechAssembly.getAutonomousBehaviors(),
                telemetry);

        targeter = new LimelightAutoTarget(
                limelight,
                ((BillyMA)mechAssembly).getAutonomousBehaviors().autonTurret,
                tagID);

        strategy = (robot, gamepad1, gamepad2)  -> {
            if (gamepad1.yWasPressed()) {
                if (assist == null)
                    startAssist(targeter);
                else
                    stopAssist("turned off by y");
            }

            BillyMA mech = (BillyMA)mechAssembly;
            if (gamepad2.aWasPressed()) {
                mech.requestRapidFire();
                if (overlaps(mech.macroClaims(), assistClaims()))
                    stopAssist("replaced by " + mech.macroName());
            }

            if (gamepad2.bWasPressed()) {
                mech.cancelMacro("cancelled by b");
            }

            if(gamepad1.bWasPressed()) {
                stopAll();
            }

            driveTrain.drive(gamepad1);
            mech.giveInstructions(gamepad2, assistClaims());
            if (assist != null) {
                assist.update(gamepad1, gamepad2);
            }
        };
        startAssist(targeter);
    }

    public void stopAll() {
        ((BillyMA)mechAssembly).cancelMacro("stopped by stop all");
        stopAssist("stopped by stop all");
    }

    private boolean overlaps(Set<BillyMA.Part> a, Set<BillyMA.Part> b) {
        return !Collections.disjoint(a, b);
    }

    @Override
    public void update(Gamepad gamepad1, Gamepad gamepad2) {
       strategy.execute(this, gamepad1, gamepad2);
    }

    @Override
    public void updateTelemetry() {
        BillyMA mech = (BillyMA) mechAssembly;
        if (assist == null)
            telemetry.addData("Assist", "None");
        else
            telemetry.addData("Assist", assist.name() + " " + assist.claims());
        telemetry.addData("Macro", mech.macroName() + " " + mech.macroClaims());
        telemetry.addData("Last macro", mech.lastMacroEnd());
        telemetry.addData("Last assist", lastAssistEnd);
        super.updateTelemetry();
    }

    public void startAssist(IAssistRobotStrategy<BillyMA.Part> newAssist) {
        BillyMA mech = (BillyMA) mechAssembly;
        if (overlaps(newAssist.claims(), mech.macroClaims()))
            mech.cancelMacro("replaced by " + newAssist.name());

        if (assist != null) {
            lastAssistEnd = assist.name() + " replaced by " + newAssist.name();
            assist.stop();
        }
        assist = newAssist;
        assist.start();
    }

    private String lastAssistEnd = "None";

    public Set<BillyMA.Part> assistClaims() {
        if(assist == null) {
            return EnumSet.noneOf(BillyMA.Part.class);
        }
        return assist.claims();
    }

    public void stopAssist(String reason) {
        if (assist == null)
            return;
        assist.stop();
        lastAssistEnd = assist.name() + " " + reason;
        assist = null;
    }
}
```

### C.9  `LimelightExtensions.tryGetFiducial`

`Extensions/LimelightExtensions.java:12-33`
```java
    /**
     * *look for a specific AprilTag Id using the limelight camera
     * If the tag is not found, this method returns NULL
     * @param limelight camera configured to read Fiducials
     * @param tagId which tag id are you looking for.
     * @return The desired FiducialResult or NULL if not found.
     */
    public static LLResultTypes.FiducialResult tryGetFiducial(
            Limelight3A limelight,
            int tagId)
    {
        LLResult result = limelight.getLatestResult();
        if (result == null)
            return null;

        return result
                .getFiducialResults()
                .stream()
                .filter(fr -> fr.getFiducialId() == tagId)
                .findFirst()
                .orElse(null);
    }
```

### C.10  `LimelightAutoTarget` -- whole class, all states

`AutonStrategies/LimelightAutoTarget.java:24-127`
```java
public class LimelightAutoTarget extends StateMachine implements Robot.IAssistRobotStrategy<BillyMA.Part>
{
    private final int targetTagId;
    private final Limelight3A limelight;
    private final Turret.AutonomousTurretBehaviors turret;

    /**
     * @param limelight  the Limelight3A sensor
     * @param turret     the live autonomous turret behavior — carries its own
     *                   telemetry reference, so no raw {@code Telemetry} arg is
     *                   needed here (Susan Zuo — Bug #6)
     * @param tagId      AprilTag ID to track
     */
    public LimelightAutoTarget(
            Limelight3A limelight,
            Turret.AutonomousTurretBehaviors turret,
            int tagId)
    {
        this.limelight = limelight;
        this.turret = turret;
        this.targetTagId = tagId;
        currentState = lookForTag();
    }

    @Override
    public Set<BillyMA.Part> claims()
    {
        return EnumSet.of(BillyMA.Part.TURRET);
    }
    @Override 
    public String name()
    {
        return "LimelightAutoTarget";
    }
    @Override
    public void start()
    {
        currentState = lookForTag();
    }
    @Override 
    public void update(Gamepad driver, Gamepad mech)
    {
        updateState();
    }
    @Override 
    public void stop()
    {
        abort();
        turret.stop();
    }
    

    public IState rotateCCW(double tx){
        return () ->
        {
            double power = 1;
            if(tx > -10)
                power = -tx / 10.0;
            turret.setPower(power);
            turret.reportData("Turret CCW", power);
            return lookForTag();
        };
    }
    public IState rotateCW(double tx) {
        return () ->
        {
            double power = -1;
            if (tx < 10)
                power = -tx / 10.0;
            turret.setPower(power);
            turret.reportData("Turret CW", power);
            return lookForTag();
        };
    }

    public IState stopTurret() {
        return () ->
        {
            turret.stop();
            turret.reportStatus("Turret Stopped");
            return lookForTag();
        };
    }

    public IState lookForTag() {
        return()->
        {
            LLResultTypes.FiducialResult tag =
                    LimelightExtensions.tryGetFiducial(
                            limelight,
                            targetTagId);

            if(tag == null)
            {
                turret.reportStatus("Tag " + targetTagId + " not found.");
                return stopTurret();
            }
            double tx = tag.getTargetXDegrees();
            if(tx < -2) return rotateCCW(tx);
            else if(tx > 2) return rotateCW(tx);
            else            return stopTurret();
        };
    }
}
```

### C.11  `DoublyLimitedMotor` -- constructor and `setPower`

`RobotModel/Mechs/Components/DoublyLimitedMotor.java:54-100`
```java
    /**
     * The forward sensor is the sensor that is hit when the motor is given positive power
     * @param hardwareMap pass this down from on high
     * @param motorName name of the motor in the driver station configuration
     * @param forwardSensorName name of the forward sensor in the driver station configuration
     * @param reverseSensorName name of the reverse sensor in the driver station configuration
     * @param strategy lambda expression (Gamepad, DoublyLimitedMotor) for translating gamepad input into motor movement
     */
    public DoublyLimitedMotor(
            HardwareMap hardwareMap,
            String motorName,
            String forwardSensorName,
            String reverseSensorName,
            DoublyLimitedMotorControlStrategy strategy,
            DoublyLimitedMotorTelemetryStrategy telemetryStrategy
            ) {
        super(strategy);
        motor = hardwareMap.get(DcMotor.class, motorName);
        forwardSensor = hardwareMap.get(TouchSensor.class, forwardSensorName);
        reverseSensor = hardwareMap.get(TouchSensor.class, reverseSensorName);
        this.strategy = strategy;
        this.telemetryStrategy = telemetryStrategy;
    }

    /**
     * Stores the telemetry reference (via super) and lazily creates the autonomous
     * behaviors object (Susan Zuo — two-phase initialization pattern).
     */
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousDLMBehaviors(telemetry);
    }

    /**
     * this is a decorator method that checks to see if the motor can go forward or reverse before setting motor power
     * if the motor cannot move in that direction, it sets the power to zero
     * @param power assumes that power is between -1 and 1, and that the dead zone is applied
     */
    public void setPower(double power){

        if ((power > 0 && !canGoForward() )|| (power < 0 && !canGoReverse())) {
            power = 0;
        }

        motor.setPower(power);
    }
```

### C.12  `SpinnyIntake` -- whole class

`RobotModel/Mechs/Components/SpinnyIntake.java:9-93`
```java
public class SpinnyIntake extends MechComponent
{
    public class AutonomousIntakeBehaviors extends MechComponent.AutonomousComponentBehaviors
    {
        public AutonomousIntakeBehaviors(Telemetry telemetry) {
            super(telemetry);
        }

        public void startIntake()
        {
            intake.setPower(1);
            reportStatus("Intake: running");
        }

        public void stopIntake()
        {
            intake.setPower(0);
            reportStatus("Intake: stopped");
        }

        public void reverseIntake()
        {
            intake.setPower(-1);
            reportStatus("Intake: reversed");
        }
    }


    public interface SpinnyIntakeControlStrategy extends IControlStrategy
    {
        /**
         * abstract definition of how a Spinny Intake gets controlled.
         * this Implementation should translate the Gamepad Input into motor movement
         * of the Spinny Intake.
         * The name of this method does not matter at all~ so make it descriptive in the
         * context of the Spinny Intake!
         * @param motor the `intake` motor will be passed here.
         * @param gamepad the gamepad will be passed down from the MechAssembly to this strategy
         */
        void nomNomNom(DcMotor motor, Gamepad gamepad);
    }

    private final DcMotor intake;

    protected SpinnyIntakeControlStrategy strategy;

    private AutonomousIntakeBehaviors auton;

    public SpinnyIntake(
            HardwareMap hardwareMap,
            String motorName,
            SpinnyIntakeControlStrategy strategy)
    {
        super(strategy);
        intake = hardwareMap.get(DcMotor.class, motorName);
        this.strategy = (SpinnyIntakeControlStrategy) super.strategy;
    }

    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousIntakeBehaviors(telemetry);
    }

    @Override
    public AutonomousIntakeBehaviors getAutonomousBehaviors() {
        return auton;
    }

    @Override
    public void move(Gamepad gamepad)
    {
        strategy.nomNomNom(intake, gamepad);
    }

    /**
     * Writes intake power and encoder position to the buffer. Previously
     * empty (Susan Zuo — Bug #3: "No telemetry data reported"). Never flushes.
     */
    @Override
    public void update() {
        telemetry.addData("intake power:", intake.getPower());
        telemetry.addData("intake position:", intake.getCurrentPosition());
    }
}
```

### C.13  `MacroManager` -- whole file

See B.2.

### C.14  `BillyRapidFire` -- whole class (for chapters 12 and 13)

`AutonStrategies/BillyRapidFire.java:23-136`
```java
public class BillyRapidFire extends StateMachine
        implements MechAssembly.IMacroAssemblyStrategy<BillyMA.Part>
{
    private final BillyMA.AutonomousBillyMA mechAssembly;
    private final int shotsPerRun;
    private int ballCount;

    /**
     * @param ma         the live autonomous behavior object — not constructed here,
     *                   so no raw {@code Telemetry} reference is needed (Susan Zuo
     *                   — Bug #6: "autonomous strategies held raw telemetry
     *                   references, bypassing the object model").
     * @param ballCount  initial number of balls to fire
     */
    public BillyRapidFire(BillyMA.AutonomousBillyMA ma, int ballCount)
    {
        mechAssembly = ma;
        shotsPerRun = ballCount;
    }

    @Override
    public Set<BillyMA.Part> claims()
    {
        return EnumSet.of(BillyMA.Part.PUSHER, BillyMA.Part.FLYWHEEL);
    }

    @Override
    public void start()
    {
        reset(shotsPerRun);
    }

    @Override
    public void update(Gamepad gamepad)
    {
        updateState();
    }

    @Override
    public void cancel()
    {
        abort();
        mechAssembly.autonFlywheel.StopShoot();
        mechAssembly.autonBallPusher.retractPusher();
    }

    public void reset(int ballCount)
    {
        this.ballCount = ballCount;
        currentState = startShooter();
    }

    @Override
    public String name()
    {
        return "BillyRapidFire";
    }



    public IState startShooter() {
        mechAssembly.reportStatus("startShooter");
        return
                doAndWait(
                        () -> mechAssembly
                                .autonFlywheel
                                .setPower(0.6),
                        2200,
                        fire()
                );


    }
    public IState fire()
    {
        mechAssembly.reportStatus("fire");
        return
                doAndWait(
                        mechAssembly
                                .autonBallPusher
                                ::pushBalls,
                        1000,
                        retract()
                );
    }
    public IState retract()
    {
        mechAssembly.reportStatus("retract");
        return
                doAndWait(
                        mechAssembly
                                .autonBallPusher
                                ::retractPusher,
                        1000,
                        --ballCount > 0
                                ? fire()
                                : stopShooter()


                );
    }
    public IState stopShooter()
    {
        mechAssembly.reportStatus("stopShooter");
        return() ->
        {
            mechAssembly
                    .autonFlywheel
                    .StopShoot();
            return null;
        };
    }

}
```

---

## D.  Other things a writer would get wrong by trusting the lessons

1. **`BallDetectionComponent` does not exist** and never existed in this repo's history.  Named in GS02 (5 places), `MechComponent.java:40-42`, `doc/Migration-2026-27.md:31`.  Do not quote it.  The `Components/` folder holds exactly seven files:  `Claw`, `DoubleShooter`, `DoublyLimitedMotor`, `MechComponent`, `PusherServo`, `SpinnyIntake`, `Turret`.
2. **New classes and interfaces no lesson mentions:**  `MacroManager<R>`;  `MechAssembly.IMacroAssemblyStrategy<R>`;  `Robot.IAssistRobotStrategy<R>`;  `BillyMA.Part`;  `BillyMA.BillyAssemblyStrategy` now has a third parameter `Set<Part> claimed`.
3. **New public methods:**  `BillyMA.requestRapidFire()`, `cancelMacro(String)`, `lastMacroEnd()`, `macroClaims()`, `macroName()`, `giveInstructions(Gamepad, Set<Part>)`;  `BillyRobot.stopAll()`, `startAssist(...)`, `stopAssist(String)`, `assistClaims()`, `updateTelemetry()` override;  `BillyRapidFire.claims/start/update/cancel/name`;  `LimelightAutoTarget.claims/name/start/update/stop`.
4. **Signature changes:**  `BillyMA(HardwareMap, Telemetry)` -> `BillyMA(HardwareMap)`.  `BillyRapidFire`'s constructor no longer sets `currentState`.
5. **`BillyRobot` no longer calls `super.update(...)`.**  Every "template method" passage (lesson 06, Advanced/state-machines, `doc/AbstractRobotObjectModel.md:304-322`, `doc/NewRobotDesignWorkflow.md:228-235`) is stale for Billy.  The other three robots don't override `update` at all, so they still use the template as-is.
6. **Rapid fire is no longer started by `gamepad.a` inside `BillyMA`.**  It is `gamepad2.aWasPressed()` in `BillyRobot`, edge-triggered.  `gamepad2.b` cancels.  `gamepad1.y` toggles targeting;  `gamepad1.b` stops all.
7. **The targeter is on by default and the tag ID is `-1`**, so in TeleOp today the turret sits at 0 power and ignores the bumpers until `gamepad1.y` is pressed.
8. **`ExampleStateMachineAuton` ignores `TARGET_TAG_ID`** and hardcodes `21, 22, 23` (`:68-70`).  It never flushes telemetry inside its loop, so `"Tags 21, 22, 23 not found"` (`:74-75`) is never shown.  It duplicates `doAndWait` privately (`:46-62`) instead of extending `StateMachine`.
9. **SDK version:**  the repo builds against **FTC SDK 12.0.0** (`build.dependencies.gradle:7-14`, since `7cf073d`, 2026-09-07).  `doc/limelight3a/*.md` all say 11.2.1 and claim that matches the build;  that's false.  Under v12, webcam AprilTag code must cast to `AprilTagSingleDetection` for `id`/`metadata` (`README.md:65-89`);  the GS04 webcam loop is pre-v12 code.
10. **SensorLimelight3A misquote:**  GS04 says the sample guards with `result != null && result.isValid()`;  the sample (`SensorLimelight3A.java:102-103`) checks only `result.isValid()`.
11. **`doc/limelight3a/api.md:94`** says that in 11.2.1 `getLatestResult()` never returns `null` (it synthesizes an empty invalid result).  If that still holds in 12.0.0, the new `null` guard is harmless but insufficient, and `isValid()` is the check that matters.  UNVERIFIED for 12.0.0.
12. **The MegaTag2 quote** attributed (by position) to `java-objects.md` is actually in `doc/limelight3a/api.md:96`.
13. **Docs that went stale with the code but were not updated** (none changed after `34c22ea`):  `doc/TelemetryContract.md:157-159` ("`BillyMA.updateTelemetry()` currently stands empty");  `doc/ControlStrategyExpansionPlan.md:80-95`, `:156`, `:437-441` (describe the pre-Phase-2 code as "today");  `doc/NewRobotDesignWorkflow.md:168-172`, `:228-235`, `:309`;  `doc/AbstractRobotObjectModel.md:316-319`.  `doc/FlintLessons/README.md:10` ("eighteen").
14. **Strategy field shadowing is the norm, not a bug the code flags:**  `BillyMA.strategy` shadows `MechAssembly.strategy`;  `BillyRobot.strategy` shadows `Robot.strategy`;  `SpinnyIntake.strategy`, `Claw.strategy`, `Turret.strategy`, `PusherServo.strategy`, `DoublyLimitedMotor.strategy`, `DoubleShooter.pewpew` all sit beside `MechComponent.strategy`.  Base-class `strategy` fields are assigned for components (via `super(strategy)`) but **never** for `MechAssembly` or `Robot`.
15. **Both top-level strategy lambdas ignore their first parameter** (`mechAssembly` in `BillyMA.java:117`, `robot` in `BillyRobot.java:132`) and use captured fields.
16. **Shared dpad on `gamepad2`:**  dpad up/down runs the intake **and** trims the flywheel in the same loop (`BillyMA.java:55-58`, `:81-86`).  The flywheel trim is not cumulative:  power is 0.45 ± 0.05 only while held, and only matters while `y` is held.
17. **`turret position` telemetry reports power, not position** (`BillyMA.java:114`, `servo.getPower()` on a `CRServo`).  `CascadeArm`'s `Claw Position` does the same (`CascadeArm.java:85`).
18. **`Robot.visionPortal`** (`Robot.java:111`) is declared and never assigned anywhere;  `Robot` still imports `VisionPortal`.
19. **`StandardTankDrive` vs `OneStickTank`:**  GS02's table puts `StandardTankDrive` with "four `DcMotor` + `IMU`";  both tank drives have two motors and no IMU.  Config names `"leftTread"` / `"rightTread"` (`StandardTankDrive.java:66-67`, `OneStickTank.java:88-89`).
20. **Mechanical lesson 19's `DoublyLimitedMotor` snippet** (and `doc/rev-duo/linear-motion.md:88-97`) won't compile and reverses the limits;  see chapter 19.  Use `CascadeArm.java:45-61` as the real example.
21. **Paths the lessons cite all still exist**, except the absent `BallDetectionComponent`.  Not yet created (the cluster plan asks students to make them):  `Extensions/TagCluster.java`, `OpModes/LimelightClusterProbe.java`.

---

## E.  Continuation findings, 2026-10-26

Added by Codex while drafting and reviewing Chapters 16-20.  The original
audit above remains a record of its checks against `305fb81`.

1. **DoublyLimitedMotor checks a limit only on a call to `setPower`.**
   `goForward()` and `goBackward()` call it once.  The component's `update()`
   only reports telemetry.  A switch pressed after a one-time autonomous
   command will require another check and a stop request.  The guard is not
   continuous enforcement or an independent hardware cutoff.  Chapter 19
   now says this;  carry it into software chapters describing the decorator,
   autonomous verbs, state machines, and safeguards.  Following Jason's
   student-work guidance, a neutral source TODO now records the next step
   for stopping autonomous movement when a travel limit is reached.
2. **Core Hex reference errors extend beyond the original audit.**
   [REV's specifications](https://docs.revrobotics.com/duo-build/motion/motors/core-hex-motor)
   give a 72:1 internal ratio, four counts per motor revolution, and 288 per
   output revolution.  The local 20:1 value and physical output-encoder
   description are wrong.  Use the published count's reference shaft.
3. **HD Hex maximum output is 15 W in the manufacturer specification.**
   The earlier audit correctly questioned 40 W using a simplified estimate.
   [REV's motor page](https://docs.revrobotics.com/duo-build/motion/motors/hd-hex-motor)
   supplies the actual published rating.  The 31.4 W lift example cannot be
   approved for one motor on the local guide's stated reasoning.
4. **Use exact fractions before rounding compounded ratios.**
   From [REV's cartridge table](https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/cartridge-details),
   `(68/13)*(76/21)` is about 18.93040 and multiplying by `84/29` gives
   54.83289.  Chapter 17 rounds these to 18.93 and 54.83.
5. **Servo current limits are grouped.**
   [Control Hub specifications](https://docs.revrobotics.com/duo-control/control-system-overview/control-hub-basics)
   give 2A per servo-port pair and 5A shared across servo and auxiliary 5V
   ports.  Local "2A total" is wrong.  Analog and digital connectors are
   four-pin JST PH, while RS485 is three-pin.  [SPM documentation](https://docs.revrobotics.com/rev-crossover-products/servo/spm)
   gives 15A total, 3A per output, and screw-terminal power input.
6. **Mecanum source conflict resolved in favor of the manufacturer.**
   [REV's setup guide](https://docs.revrobotics.com/duo-build/mecanum-drivetrain-v2/mecanum-wheel-setup-and-behavior)
   specifies the roller X viewed from above.  The local top-O/bottom-X
   wording and its diagram are confusing.  Chapter 18 uses the manufacturer's
   instruction and a labeled top-view sketch.
7. **The preview starter-bot URL is stale.**
   The manufacturer now has a [2026-27 starter-bot page](https://docs.revrobotics.com/ftc-kickoff-concepts/home/2026-27-rev-duo-ftc-starter-bot.md).
   Chapter 20 points there.  Historical bot summaries were checked only
   against the local reference during this continuation.

Manufacturer pages above were read on 2026-10-26.  Chapter notes hold the
specific decisions, remaining uncertainties, and intended next checks.
