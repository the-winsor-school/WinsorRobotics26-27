package org.firstinspires.ftc.teamcode.RobotModel.Robots;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Builder class for creating a BillyRobot instance.
 * It sets up a BillyRobot instead of replacing it.
 */
public class BillyRobotBuilder
{
    private final HardwareMap hardwareMap;
    private final Telemetry telemetry;
    private Integer tagID; // Integer, not int, so "never set" is null

    public BillyRobotBuilder(HardwareMap hardwareMap, Telemetry telemetry)
    {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;
    }

    public BillyRobotBuilder tagID(int tagID)
    {
        this.tagID = tagID;
        return this;
    }

    public BillyRobot build()
    {
        if (tagID == null)
            throw new IllegalStateException("BillyRobotBuilder: call tagID(...) before build()");
        return new BillyRobot(hardwareMap, telemetry, tagID);
    }
}

