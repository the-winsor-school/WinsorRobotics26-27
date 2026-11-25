package org.firstinspires.ftc.teamcode.OpModes;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RobotModel.Robots.BillyRobot;
import org.firstinspires.ftc.teamcode.RobotModel.Robots.Robot;
import org.firstinspires.ftc.teamcode.RobotModel.Robots.Testing;

//@Disabled   // temporarily hidden while testing Example TeleOp (Simple Mecanum)
@TeleOp(name="Testing")
public class TestingTeleOp extends LinearOpMode {

    // TODO: this season's BLUE-alliance target AprilTag ID goes here (last season's Limelight tag ID was removed during 2027 season prep).
    Robot robot;
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Testing(hardwareMap, telemetry);

        telemetry.addLine("Testing Initialized");
        telemetry.update();

        waitForStart();
        while(opModeIsActive()){
            robot.update(gamepad1, gamepad2);
            robot.updateTelemetry();
        }
    }

}
