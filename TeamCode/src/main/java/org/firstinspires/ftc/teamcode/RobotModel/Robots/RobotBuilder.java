package org.firstinspires.ftc.teamcode.RobotModel.Robots;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.DriveTrain;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.MechAssembly;


public class RobotBuilder
{
    private final Telemetry telemetry;
    private DriveTrain driveTrain;       // null until someone sets it
    private MechAssembly mechAssembly;   // optional

    public RobotBuilder(Telemetry telemetry)
    {
        this.telemetry = telemetry;
    }

    public RobotBuilder driveTrain(DriveTrain driveTrain)
    {
        this.driveTrain = driveTrain;
        return this;            // hand the builder back so the next call can chain
    }

    public RobotBuilder mechAssembly(MechAssembly mechAssembly)
    {
        this.mechAssembly = mechAssembly;
        return this;
    }

    public Robot build()
    {
        if (driveTrain == null)
            throw new IllegalStateException("RobotBuilder: call driveTrain(...) before build()");
        return new SimpleRobot(telemetry, driveTrain, mechAssembly);
    }

}