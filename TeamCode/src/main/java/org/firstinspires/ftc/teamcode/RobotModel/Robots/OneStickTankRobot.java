package org.firstinspires.ftc.teamcode.RobotModel.Robots;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Tank.OneStickTank;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.ExampleIntakeAssembly;

public class OneStickTankRobot extends Robot
{
    public OneStickTankRobot(HardwareMap hardwareMap, Telemetry telemetry)
    {
        super(telemetry);
        mechAssembly = new ExampleIntakeAssembly(hardwareMap);
        driveTrain = new OneStickTank(hardwareMap);
        initializeSubsystems();
    }

    @Override
    public <T extends AutonomousRobot> T getAutonomousRobot() {
        return null;
    }
}
