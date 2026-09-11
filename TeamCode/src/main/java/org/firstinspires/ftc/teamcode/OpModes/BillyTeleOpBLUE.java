package org.firstinspires.ftc.teamcode.OpModes;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RobotModel.Robots.BillyRobot;
import org.firstinspires.ftc.teamcode.RobotModel.Robots.Robot;

@TeleOp(name="BillyBLUE")
public class BillyTeleOpBLUE extends LinearOpMode {

    // TODO: this season's BLUE-alliance target AprilTag ID goes here (last season's Limelight tag ID was removed during 2027 season prep).
    private static final int TARGET_TAG_ID = -1;

    Robot robot;
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new BillyRobot(hardwareMap, telemetry, TARGET_TAG_ID);

        telemetry.addLine("Billy Initialized");
        telemetry.update();

        waitForStart();
        while(opModeIsActive()){
            robot.update(gamepad1, gamepad2);
            robot.updateTelemetry();
        }
    }

}
