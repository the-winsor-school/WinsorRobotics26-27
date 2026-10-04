package org.firstinspires.ftc.teamcode.OpModes;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RobotModel.Robots.BillyRobot;
import org.firstinspires.ftc.teamcode.RobotModel.Robots.Robot;

@TeleOp(name="CSF")

public class CSF_OpMode
{
    public class BillyTeleOpRED extends LinearOpMode {

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


}
