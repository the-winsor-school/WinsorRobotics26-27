package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RobotModel.Robots.Robot;
import org.firstinspires.ftc.teamcode.RobotModel.Robots.StandardTankRobot;

/**
 * The smallest complete OpMode in the project: build a Robot, then loop.
 *
 * <p>Every OpMode in this codebase has the same four steps — construct the robot,
 * {@code waitForStart()}, then {@code update()} + {@code updateTelemetry()} until the
 * driver stops it. Everything that makes one robot different from another lives in the
 * {@code RobotModel}, not here.
 *
 * <p>To drive a different robot, change the one line that constructs it. Try swapping
 * {@link StandardTankRobot} (two sticks, one motor each) for
 * {@link org.firstinspires.ftc.teamcode.RobotModel.Robots.OneStickTankRobot}
 * (one stick, arcade-style, plus an intake assembly) — nothing else in this file changes.
 */
@TeleOp(name = "Example TeleOp (Tank)", group = "Example")
public class ExampleTankTeleOp extends LinearOpMode
{
    Robot robot;

    @Override
    public void runOpMode() throws InterruptedException
    {
        robot = new StandardTankRobot(hardwareMap, telemetry);

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
