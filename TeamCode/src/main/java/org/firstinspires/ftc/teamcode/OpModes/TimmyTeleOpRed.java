package org.firstinspires.ftc.teamcode.OpModes;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RobotModel.Robots.Robot;
import org.firstinspires.ftc.teamcode.RobotModel.Robots.TimmyRobot;

@TeleOp(name="Timmy")
public class TimmyTeleOpRed extends LinearOpMode {

    private static final int tagID = 20;
    Robot robot;
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new TimmyRobot(hardwareMap, telemetry, tagID);

        telemetry.addLine("Initialized");
        telemetry.update();

        waitForStart();
        while(opModeIsActive()){
            robot.update(gamepad1, gamepad2);
            robot.updateTelemetry();
        }
    }

}
