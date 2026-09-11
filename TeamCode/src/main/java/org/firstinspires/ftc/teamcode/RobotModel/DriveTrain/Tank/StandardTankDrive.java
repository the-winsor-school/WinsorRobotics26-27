package org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Tank;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Extensions.GamepadExtensions;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.DriveTrain;

public class StandardTankDrive extends DriveTrain
{
    public class AutonomousTankDrive extends AutonomousDriving
    {
        public AutonomousTankDrive(Telemetry telemetry) {
            super(telemetry);
        }

        public void driveForward() {
            left.setPower(1);
            right.setPower(1);
            reportStatus("Driving forward");
        }
        public void driveBackward() {
            left.setPower(-1);
            right.setPower(-1);
            reportStatus("Driving backward");
        }
        public void stop() {
            left.setPower(0);
            right.setPower(0);
            reportStatus("Stopped");
        }
        public void turnLeft() {
            left.setPower(-1);
            right.setPower(1);
            reportStatus("Turning left");
        }
        public void turnRight() {
            left.setPower(1);
            right.setPower(-1);
            reportStatus("Turning right");
        }
    }

    private AutonomousTankDrive auton;

    @Override
    public AutonomousTankDrive getAutonomousDriving()
    {
        return auton;
    }

    // these can be declared Final because once they are initialized they should not be changed.
    private final DcMotor left, right;

    /**
     * Initialize the Tank Drive with Configuration motors named:
     * left -> "leftTread"
     * right -> "rightTread"
     * @param hardwareMap pass down from OpMode.
     */
    public StandardTankDrive(HardwareMap hardwareMap)
    {
        left = hardwareMap.get(DcMotor.class, "leftTread");
        right = hardwareMap.get(DcMotor.class, "rightTread");
        right.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
        auton = new AutonomousTankDrive(telemetry);
    }

    @Override
    public void drive(Gamepad gamepad)
    {
        float leftDrive = GamepadExtensions.GetLeftStickY(gamepad);
        float rightDrive = GamepadExtensions.GetRightStickY(gamepad);
        left.setPower(leftDrive);
        right.setPower(rightDrive);
    }

    @Override
    public void updateTelemetry()
    {
        telemetry.addData("Left power:", left.getPower());
        telemetry.addData("Right power:", right.getPower());
    }
}
