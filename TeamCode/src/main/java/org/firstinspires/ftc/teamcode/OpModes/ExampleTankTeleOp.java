package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RobotModel.Robots.Robot;

import org.firstinspires.ftc.teamcode.RobotModel.Robots.RobotBuilder;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Tank.StandardTankDrive;


/**
 * The smallest complete OpMode in the project: build a Robot, then loop.
 *
 * <p>Every OpMode in this codebase has the same four steps — construct the robot,
 * {@code waitForStart()}, then {@code update()} + {@code updateTelemetry()} until the
 * driver stops it. Everything that makes one robot different from another lives in the
 * {@code RobotModel}, not here.
 *
 * <p>To drive a different robot, change the one statement that builds it.  For the
 * one-stick, arcade-style robot with an intake, build:
 * <pre>
 * new RobotBuilder(telemetry)
 *         .driveTrain(new OneStickTank(hardwareMap))
 *         .mechAssembly(new ExampleIntakeAssembly(hardwareMap))
 *         .build();
 * </pre>
 * Nothing else in this file changes.
 */

@TeleOp(name = "Example TeleOp (Tank)", group = "Example")
public class ExampleTankTeleOp extends LinearOpMode
{
    Robot robot;

    @Override
    public void runOpMode() throws InterruptedException
    {
        robot = new RobotBuilder(telemetry)
        .driveTrain(new StandardTankDrive(hardwareMap))
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
