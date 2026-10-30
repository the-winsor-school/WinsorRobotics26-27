package org.firstinspires.ftc.teamcode.RobotModel.Robots;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.DriveTrain;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.MechAssembly;


class SimpleRobot extends Robot       // no "public" - on purpose
{
    SimpleRobot(Telemetry telemetry, DriveTrain driveTrain, MechAssembly mechAssembly)
    {
        super(telemetry);
        this.driveTrain = driveTrain;
        this.mechAssembly = mechAssembly;
        initializeSubsystems();
    }

    @Override
    public <T extends AutonomousRobot> T getAutonomousRobot() {
        return null;    // simple robots have no autonomous API, same as the tank robots
    }
}
