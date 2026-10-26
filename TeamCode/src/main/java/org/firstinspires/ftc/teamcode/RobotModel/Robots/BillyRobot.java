package org.firstinspires.ftc.teamcode.RobotModel.Robots;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.AutonStrategies.LimelightAutoTarget;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Mecanum.MecanumDrive;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.BillyMA;



import com.qualcomm.hardware.limelightvision.Limelight3A;

import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import java.util.Set;
import java.util.EnumSet;
import java.util.Collections;


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
