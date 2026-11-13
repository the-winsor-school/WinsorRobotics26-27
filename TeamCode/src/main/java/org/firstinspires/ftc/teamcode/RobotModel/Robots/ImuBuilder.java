package org.firstinspires.ftc.teamcode.RobotModel.Robots;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;

/**
 * Builder class for creating an IMU with specified orientation. 
 * Before this, the same IMU setup was copied into BillyRobot and Wildbots. 
 * The robot keeps the IMU it gets back and passes it to the drive train.
 */
public class ImuBuilder
{
    private final HardwareMap hardwareMap;

    private RevHubOrientationOnRobot.LogoFacingDirection logoFacing;
    private RevHubOrientationOnRobot.UsbFacingDirection usbFacing;
    
    public ImuBuilder(HardwareMap hardwareMap)
    {
        this.hardwareMap = hardwareMap;
    }

    public ImuBuilder logoFacing(RevHubOrientationOnRobot.LogoFacingDirection direction)
    {
        this.logoFacing = direction;
        return this;
    }

    public ImuBuilder usbFacing(RevHubOrientationOnRobot.UsbFacingDirection direction)
    {
        this.usbFacing = direction;
        return this;
    }

    public IMU build()
    {
        if (logoFacing == null)
            throw new IllegalStateException("ImuBuilder: call logoFacing(...) before build()");
        if (usbFacing == null)
            throw new IllegalStateException("ImuBuilder: call usbFacing(...) before build()");

        IMU imu = hardwareMap.get(IMU.class, "imu");

        IMU.Parameters parameters = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        this.logoFacing,
                        this.usbFacing
                )
        );
        imu.initialize(parameters);
        imu.resetYaw();
        return imu;
    }
}
