package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Extensions.GamepadExtensions;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.Claw;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.DoubleShooter;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.DoublyLimitedMotor;

import java.util.Set;

import org.firstinspires.ftc.teamcode.AutonStrategies.CascadeStow;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.PusherServo;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.SpinnyIntake;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.Turret;

public class TestingMA extends MechAssembly
{
    public interface TestingMAStrategy extends IAssemblyStrategy
    {
        void execute(TestingMA mechAssembly, Gamepad gamepad);
    }

    public enum Part {
        INTAKE,
        FLYWHEEL
    }

    protected TestingMAStrategy strategy;

    public class AutonomousTestingMA extends AutonomousMechBehaviors
    {
        public final SpinnyIntake.AutonomousIntakeBehaviors intake;
        public final DoubleShooter.AutonomousShooterBehavior shooter;

        public AutonomousTestingMA(
                SpinnyIntake.AutonomousIntakeBehaviors intake,
                DoubleShooter.AutonomousShooterBehavior shooter,
                Telemetry telemetry)
        {
            super(telemetry);
            this.intake = intake;
            this.shooter = shooter;
        }
    }

    private AutonomousTestingMA auton;

    @Override
    public AutonomousTestingMA getAutonomousBehaviors()
    {
        return auton;
    }

    private final SpinnyIntake intake;
    private final DoubleShooter shooter;

    public TestingMA (HardwareMap hardwareMap){
        intake = new SpinnyIntake(
                hardwareMap,  "intakeMotor",
                (motor, gamepad) -> {
                    if (gamepad.dpad_up) {
                        motor.setPower(0.75); }
                    else if (gamepad.dpad_down) {
                        motor.setPower(-0.75);
                    }
                    else {
                        motor.setPower(0);
                    }
                }
        );
        shooter = new DoubleShooter(hardwareMap, "flywheelMotorF", "flywheelMotorB",
                (motorF, motorB,gamepad) -> {
                    double power = 0.45;
                    if (gamepad.dpad_up) {
                        power += 0.05;
                    }
                    if (gamepad.dpad_down) {
                        power -= 0.05;
                    }

                    if (gamepad.y) {
                        motorF.setPower(power);
                        motorB.setPower(-power);
                    } else {
                        motorF.setPower(0);
                        motorB.setPower(0);
                    }
                }, (motorF, motorB, telemetry) -> {
            telemetry.addData("power:", motorF.getPower());
        });
    }

    /**
     * Propagates telemetry to the three components so each can lazily create its own
     * autonomous behavior object, then assembles {@code AutonomousCascadeArm}
     * (Susan Zuo — two-phase initialization pattern).
     */
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
        intake.initializeTelemetry(telemetry);
        shooter.initializeTelemetry(telemetry);
        auton = new AutonomousTestingMA(
                intake.getAutonomousBehaviors(),
                shooter.getAutonomousBehaviors(),
                telemetry);
    }

    @Override
    public void giveInstructions(Gamepad gamepad) {
        strategy.execute(this, gamepad);
    }

    /**
     * Collects telemetry from all three components. Previously empty (Susan Zuo
     * — Bug #3: "No telemetry data reported"). Never flushes.
     */
    @Override
    public void updateTelemetry() {
        telemetry.addLine("--- Cascade Arm ---");
        intake.update();
        shooter.update();
    }
}
