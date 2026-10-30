package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Mecanum.MecanumDriveBuilder;
import org.firstinspires.ftc.teamcode.RobotModel.Robots.Robot;
import org.firstinspires.ftc.teamcode.RobotModel.Robots.RobotBuilder;

/**
 * Drives Billy's chassis as a simple robot:  just the four mecanum wheels
 * ({@code lf}, {@code lb}, {@code rf}, {@code rb}), no mechanisms, no auto-aim.
 *
 * <p>Runs on Billy's own configuration.  It shows {@link RobotBuilder} and
 * {@link MecanumDriveBuilder} working together:  the drive builder names each wheel's
 * direction, and the robot builder wraps the finished drive train in a robot.
 */
@TeleOp(name = "Example TeleOp (Simple Mecanum)", group = "Example")
public class ExampleSimpleMecanumTeleOp extends LinearOpMode
{
    Robot robot;

    @Override
    public void runOpMode() throws InterruptedException
    {
        // The robot owns the IMU:  set it up once with Billy's hub orientation.
        IMU imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                RevHubOrientationOnRobot.UsbFacingDirection.UP)));
        imu.resetYaw();

        // Same wheel directions as BillyRobot.
        robot = new RobotBuilder(telemetry)
                .driveTrain(new MecanumDriveBuilder(hardwareMap)
                        .leftFront(DcMotorSimple.Direction.FORWARD)
                        .leftBack(DcMotorSimple.Direction.FORWARD)
                        .rightFront(DcMotorSimple.Direction.FORWARD)
                        .rightBack(DcMotorSimple.Direction.REVERSE)
                        .imu(imu)
                        .build())
                .build();

        telemetry.addLine("initialized");
        telemetry.update();

        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive())
        {
            robot.update(gamepad1, gamepad2);
            robot.updateTelemetry();
        }
    }
}
