package org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Mecanum;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Extensions.AngleExtensions;
import org.firstinspires.ftc.teamcode.Extensions.GamepadExtensions;
import org.firstinspires.ftc.teamcode.Extensions.ThreadExtensions;
import org.firstinspires.ftc.teamcode.Extensions.TurnDirection;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.DriveTrain;

public class MecanumDrive extends DriveTrain
{
    public class AutonomousMecanumDrive extends AutonomousDriving
    {
        public AutonomousMecanumDrive(Telemetry telemetry) {
            super(telemetry);
        }

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

        public void drive(double x, double y, double t)
        {
            double angle = Math.atan2(y, x);
            double magnitude = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

            double y1 = Math.sin(angle + Math.PI/4) * magnitude;
            double y2 = Math.sin(angle - Math.PI/4) * magnitude;

            double lf = y1 + t;
            double lb = y2 + t;
            double rf = y2 - t;
            double rb = y1 - t;

            double n = Math.max(
                    Math.abs(lf),
                    Math.max(
                            Math.abs(lb),
                            Math.max(
                                    Math.abs(rf),
                                    Math.abs(rb))));

            if (n > 1.0)
            {
                lf/=n;
                lb/=n;
                rf/=n;
                rb/=n;
            }

            reportData("LF", lf);
            reportData("LB", lb);
            reportData("RF", rf);
            reportData("RB", rb);

            RF.setPower(rf);
            RB.setPower(rb);
            LF.setPower(lf);
            LB.setPower(lb);
        }

        public void spin(TurnDirection direction)
        {
            double dir = direction==TurnDirection.RIGHT? 1:-1;
            LB.setPower(dir);
            LF.setPower(dir);
            RB.setPower(-dir);
            RF.setPower(-dir);
        }
    }



    // these can be declared Final because once they are initialized they should not be changed.
    private final DcMotor LB;
    private final DcMotor LF;
    private final DcMotor RB;
    private final DcMotor RF;
    private final IMU imu;

    public static class OrientationConfiguration{
        DcMotorSimple.Direction lb, lf, rb, rf;

        public OrientationConfiguration(
                DcMotorSimple.Direction lb,
                DcMotorSimple.Direction lf,
                DcMotorSimple.Direction rb,
                DcMotorSimple.Direction rf
        )
        {
            this.lb = lb;
            this.lf = lf;
            this.rb = rb;
            this.rf = rf;
        }
        public DcMotorSimple.Direction getLb(){
            return lb;
        }
        public DcMotorSimple.Direction getLf(){
            return lf;
        }
        public DcMotorSimple.Direction getRb(){
            return rb;
        }
        public DcMotorSimple.Direction getRf(){
            return rf;
        }
    }

    public MecanumDrive(
            HardwareMap hardwareMap,
            OrientationConfiguration orientationConfiguration)
    {
        LB = hardwareMap.get(DcMotor.class, "lb");
        LB.setDirection(orientationConfiguration.getLb());
        LF = hardwareMap.get(DcMotor.class, "lf");
        LF.setDirection(orientationConfiguration.getLf());
        RB = hardwareMap.get(DcMotor.class, "rb");
        RB.setDirection(orientationConfiguration.getRb());
        RF = hardwareMap.get(DcMotor.class, "rf");
        RF.setDirection(orientationConfiguration.getRf());

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
        imu = hardwareMap.get(IMU.class, "imu");
    }

    // Lazily initialized — created in initializeTelemetry once telemetry is
    // available. Was previously a final field-initializer, which prevented
    // the AutonomousMecanumDrive from holding a telemetry reference at all
    // (Susan Zuo — Bug #3: "Dead telemetry hook in AutonomousMecanumDrive").
    private AutonomousMecanumDrive auton;

    /**
     * Stores the telemetry reference and creates the autonomous driving object.
     * Called once by {@code Robot.initializeSubsystems()} (Susan Zuo —
     * two-phase initialization pattern).
     */
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
        auton = new AutonomousMecanumDrive(telemetry);
    }

    @Override
    public AutonomousMecanumDrive getAutonomousDriving() {
        return auton;
    }

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
    @Override
    public void drive(Gamepad gamepad)
    {
        double vertical = GamepadExtensions.GetLeftStickY(gamepad);
        double horizontal = GamepadExtensions.GetLeftStickX(gamepad);
        double turn = GamepadExtensions.GetRightStickX(gamepad);

        double angle = Math.atan2(vertical, horizontal);
        double magnitude = Math.sqrt(Math.pow(horizontal, 2) + Math.pow(vertical, 2));

        double y1 = Math.sin(angle + Math.PI/4) * magnitude;
        double y2 = Math.sin(angle - Math.PI/4) * magnitude;

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

        double n = Math.max(
                Math.abs(lf),
                Math.max(
                        Math.abs(lb),
                        Math.max(
                                Math.abs(rf),
                                Math.abs(rb))));

        if (n > 1.0)
        {
            lf/=n;
            lb/=n;
            rf/=n;
            rb/=n;
        }

        RF.setPower(rf);
        RB.setPower(rb);
        LF.setPower(lf);
        LB.setPower(lb);
    }

    /**
     * Writes IMU yaw and motor powers to the telemetry buffer. Previously
     * empty (Susan Zuo — Bug #3: "Dead telemetry hook"). Never flushes.
     */
    @Override
    public void updateTelemetry()
    {
        double yaw = imu.getRobotYawPitchRollAngles().getYaw();
        telemetry.addData("yaw:", yaw);
        telemetry.addData("LF power:", LF.getPower());
        telemetry.addData("LB power:", LB.getPower());
        telemetry.addData("RF power:", RF.getPower());
        telemetry.addData("RB power:", RB.getPower());
    }
}
