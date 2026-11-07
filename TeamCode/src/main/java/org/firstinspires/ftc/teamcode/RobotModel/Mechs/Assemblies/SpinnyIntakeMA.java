package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.SpinnyIntake;

public class SpinnyIntakeMA extends MechAssembly
{
    private final SpinnyIntake intake;

    public SpinnyIntakeMA(HardwareMap hardwareMap) {
        intake = new SpinnyIntake(hardwareMap, "intakeMotor",
                (motor, gamepad) -> {
                    if (gamepad.dpad_up) {
                        motor.setPower(0.75); }
                    else if (gamepad.dpad_down) {
                        motor.setPower(-0.75);
                    }
                    else {
                        motor.setPower(0);
                    }
                });
    }


    @Override
    public <T extends AutonomousMechBehaviors> T getAutonomousBehaviors() {
        return null;
    }

    @Override
    public void giveInstructions(Gamepad gamepad) {
        intake.move(gamepad);

    }

    @Override
    public void initializeTelemetry(Telemetry telemetry) {

    }

    @Override
    public void updateTelemetry() {

    }
}
