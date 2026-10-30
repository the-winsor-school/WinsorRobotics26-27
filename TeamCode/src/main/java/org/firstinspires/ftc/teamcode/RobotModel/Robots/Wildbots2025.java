package org.firstinspires.ftc.teamcode.RobotModel.Robots;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Mecanum.MecanumDrive;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.CascadeArm;

import com.qualcomm.robotcore.hardware.Gamepad;

public class Wildbots2025 extends Robot
{
    public interface Wildbots2025Strategy extends IRobotStrategy
    {
        void execute(Wildbots2025 robot, Gamepad gamepad1, Gamepad gamepad2);
    }

    protected Wildbots2025Strategy strategy;

    public class AutonomousMecanumRobot extends AutonomousRobot
    {
        /**
         * public final exposes the AutonomousDriving functions of the MecanumDrive
         * but none of the drive trains actual STATE.
         */
        public final MecanumDrive.AutonomousMecanumDrive driveTrain;
        public final CascadeArm.AutonomousCascadeArm mechAssembly;
        public AutonomousMecanumRobot(
                MecanumDrive.AutonomousMecanumDrive driveTrain,
                CascadeArm.AutonomousCascadeArm mechAssembly,
                Telemetry telemetry)
        {
            super(driveTrain, mechAssembly, telemetry);
            this.driveTrain = driveTrain;
            this.mechAssembly = mechAssembly;
        }
    }

    private final AutonomousMecanumRobot auton;
    @Override
    public AutonomousMecanumRobot getAutonomousRobot() {
        return auton;
    }


    public Wildbots2025(HardwareMap hardwareMap, Telemetry telemetry)
    {
        super(telemetry);
        driveTrain = new MecanumDrive(
                hardwareMap,
                new MecanumDrive.OrientationConfiguration(
                        DcMotorSimple.Direction.REVERSE,
                        DcMotorSimple.Direction.FORWARD,
                        DcMotorSimple.Direction.FORWARD,
                        DcMotorSimple.Direction.REVERSE
                    )
                );
        mechAssembly = new CascadeArm(hardwareMap);

        initializeSubsystems();

        auton = new AutonomousMecanumRobot(
                driveTrain.getAutonomousDriving(),
                mechAssembly.getAutonomousBehaviors(),
                telemetry);
        strategy = (robot, gamepad1, gamepad2) -> {
            CascadeArm arm = (CascadeArm) mechAssembly;

            if (gamepad2.xWasPressed())
                arm.requestStow();                      // start the macro (ignored if one's already running)
            if (gamepad2.yWasPressed())
                arm.cancelMacro("cancelled by y");      // the reason shows up in "Last macro"

            driveTrain.drive(gamepad1);                 // driver controls the wheels
            arm.giveInstructions(gamepad2);             // steps the macro, then the sticks/claw move any part it hasn't claimed
        };
    }

    @Override
    public void update(Gamepad gamepad1, Gamepad gamepad2) {
        strategy.execute(this, gamepad1, gamepad2);
    }

    @Override
    public void updateTelemetry() {
        CascadeArm arm = (CascadeArm) mechAssembly;
        telemetry.addData("Macro", arm.macroName() + " " + arm.macroClaims());
        telemetry.addData("Last macro", arm.lastMacroEnd());
        super.updateTelemetry();
    }
}
