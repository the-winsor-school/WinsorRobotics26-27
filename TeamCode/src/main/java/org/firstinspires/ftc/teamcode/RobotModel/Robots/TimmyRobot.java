package org.firstinspires.ftc.teamcode.RobotModel.Robots;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.AutonStrategies.IAutonStrategy;
import org.firstinspires.ftc.teamcode.AutonStrategies.LimelightAutoTarget;
import org.firstinspires.ftc.teamcode.Extensions.IState;
import org.firstinspires.ftc.teamcode.Extensions.ThreadExtensions;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Mecanum.MecanumDrive;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Mecanum.OldMecanumDrive;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.BillyMA;

import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.TimmyMA;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import com.qualcomm.hardware.limelightvision.Limelight3A;

import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;


public class TimmyRobot extends Robot {
    public class AutonomousTimmyRobot extends AutonomousRobot
    {
        public final OldMecanumDrive.AutonomousOldMecanumDrive driveTrain;
        public final TimmyMA.AutonomousTimmyMA mechAssembly;

        public AutonomousTimmyRobot(OldMecanumDrive.AutonomousOldMecanumDrive driveTrain,
                                      TimmyMA.AutonomousTimmyMA mechAssembly, Telemetry telemetry)
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

    public interface TimmyRobotStrategy extends IRobotStrategy
    {
        void execute (TimmyRobot robot, Gamepad g, Gamepad h);
    }

    private final AprilTagProcessor aprilTag;
    private final AutonomousTimmyRobot auton;
    public final Limelight3A limelight;
    public final IMU imu;

    public final LimelightAutoTarget targeter;

    protected TimmyRobotStrategy strategy;


    @Override
    public TimmyRobot.AutonomousTimmyRobot getAutonomousRobot() {
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
    public TimmyRobot(HardwareMap hardwareMap, Telemetry telemetry, int tagID) {
        super(telemetry);

        driveTrain = new OldMecanumDrive(hardwareMap, new OldMecanumDrive.OrientationConfiguration(
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.REVERSE,
                DcMotorSimple.Direction.FORWARD)
        );

        mechAssembly = new TimmyMA(hardwareMap);

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

        aprilTag = AprilTagProcessor.easyCreateWithDefaults();

        initializeSubsystems();

        auton = new TimmyRobot.AutonomousTimmyRobot(
                driveTrain.getAutonomousDriving(),
                mechAssembly.getAutonomousBehaviors(),
                telemetry);

        targeter = new LimelightAutoTarget(
                limelight,
                ((TimmyMA)mechAssembly).getAutonomousBehaviors().turret,
                tagID);
        /*
          IRobot strategy is initialized in the constructor that takes the input of robot, gamepad1, and gamepad2
          It updates the robot state with the parameters gamepad1 and gamepad2
          If the LimelightAutoTarget has not finished running, then the LimelightAutoTarget's state is updated
         */

        strategy = (robot, gamepad1, gamepad2)  -> {
            super.update(gamepad1, gamepad2);
            if(!targeter.isComplete())
                targeter.updateState();
        };
    }

    /**
     * The IRobotStrategy executes by taking the robot, gamepad1, and gamepad2 parameters
     * @param gamepad1 driver gamepad
     * @param gamepad2 mech gamepad
     */
    @Override
    public void update(Gamepad gamepad1, Gamepad gamepad2) {
        strategy.execute(this, gamepad1, gamepad2);
    }
}
