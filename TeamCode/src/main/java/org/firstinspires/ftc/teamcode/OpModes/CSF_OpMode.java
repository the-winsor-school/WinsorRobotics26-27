package org.firstinspires.ftc.teamcode.OpModes;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RobotModel.Robots.BillyRobot;

@TeleOp(name="CSF")
public class CSF_OpMode extends LinearOpMode {

    private static final int TARGET_TAG_ID = -1;

    BillyRobot robot;
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new BillyRobot(hardwareMap, telemetry, TARGET_TAG_ID);

        // Turn off Limelight auto-aim so the bumpers on gamepad2 control the turret
        robot.targeter.abort();

        telemetry.addLine("CSF Initialized");
        telemetry.update();

        waitForStart();
        while(opModeIsActive()){
            robot.update(gamepad1, gamepad2);
            robot.updateTelemetry();
        }
    }

}
