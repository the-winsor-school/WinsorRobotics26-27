package org.firstinspires.ftc.teamcode.RobotModel.Robots;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Tank.StandardTankDrive;

public class StandardTankRobot extends Robot
{
    public StandardTankRobot(HardwareMap hardwareMap, Telemetry telemetry)
    {
        super(telemetry);
        driveTrain = new StandardTankDrive(hardwareMap);
        initializeSubsystems();
    }

    @Override
    public <T extends AutonomousRobot> T getAutonomousRobot() {
        return null;
    }
}
