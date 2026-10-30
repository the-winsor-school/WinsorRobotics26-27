package org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Mecanum;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

public class MecanumDriveBuilder
{
    private final HardwareMap hardwareMap;
    private DcMotorSimple.Direction leftFront;    // null until set
    private DcMotorSimple.Direction leftBack;
    private DcMotorSimple.Direction rightFront;
    private DcMotorSimple.Direction rightBack;
    private IMU imu;

    public MecanumDriveBuilder(HardwareMap hardwareMap)
    {
        this.hardwareMap = hardwareMap;
    }

    public MecanumDriveBuilder leftFront(DcMotorSimple.Direction direction)
    {
        leftFront = direction;
        return this;
    }

    public MecanumDriveBuilder leftBack(DcMotorSimple.Direction direction)
    {
        leftBack = direction;
        return this;
    }

    public MecanumDriveBuilder rightFront(DcMotorSimple.Direction direction)
    {
        rightFront = direction;
        return this;
    }
    
    public MecanumDriveBuilder rightBack(DcMotorSimple.Direction direction)
    {
        rightBack = direction; 
        return this;
    }

    public MecanumDriveBuilder imu(IMU imu)
    {
        this.imu = imu;
        return this;
    }


    public MecanumDrive build()
    {
        if (leftFront == null)
            throw new IllegalStateException("MecanumDriveBuilder: call leftFront(...) before build()");
        if (leftBack == null)
            throw new IllegalStateException("MecanumDriveBuilder: call leftBack(...) before build()");
        if (rightFront == null)
            throw new IllegalStateException("MecanumDriveBuilder: call rightFront(...) before build()");
        if (rightBack == null)
            throw new IllegalStateException("MecanumDriveBuilder: call rightBack(...) before build()");
        if (imu == null)
            throw new IllegalStateException("MecanumDriveBuilder: call imu(...) before build()");

        return new MecanumDrive(hardwareMap,
            new MecanumDrive.OrientationConfiguration(leftBack, leftFront, rightBack, rightFront),
            imu);
    }
}
