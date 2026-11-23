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

public class TimmyMA extends MechAssembly
{
    public interface TimmyMAStrategy extends IAssemblyStrategy
    {
        void execute(TimmyMA mechAssembly, Gamepad gamepad);
    }

    public enum Part {
        INTAKE,
        FLYWHEEL,
        PUSHER,
        TURRET
    }

    protected TimmyMAStrategy strategy;

    public class AutonomousTimmyMA extends AutonomousMechBehaviors
    {
        public final SpinnyIntake.AutonomousIntakeBehaviors intake;
        public final PusherServo.AutonomousBallPusherBehaviors pusher;
        public final DoubleShooter.AutonomousShooterBehavior shooter;
        public final Turret.AutonomousTurretBehaviors turret;

        public AutonomousTimmyMA(
                SpinnyIntake.AutonomousIntakeBehaviors intake,
                PusherServo.AutonomousBallPusherBehaviors pusher,
                DoubleShooter.AutonomousShooterBehavior shooter,
                Turret.AutonomousTurretBehaviors turret,
                Telemetry telemetry)
        {
            super(telemetry);
            this.intake = intake;
            this.pusher = pusher;
            this.shooter = shooter;
            this.turret = turret;
        }
    }

    private AutonomousTimmyMA auton;

    @Override
    public AutonomousTimmyMA getAutonomousBehaviors()
    {
        return auton;
    }

    private final SpinnyIntake intake;
    private final PusherServo pusher;
    private final DoubleShooter shooter;
    private final Turret turret;

    public TimmyMA (HardwareMap hardwareMap){
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
        pusher = new PusherServo(hardwareMap,
                "drawbridge",
                (servoR, gamepad) -> {
                    if (gamepad.x) {
                        servoR.setPosition(0.8);
                    } else {
                        servoR.setPosition(0.0);
                    }
                },
                (servo, telemetry) -> {
                    telemetry.addData("pusher position", servo.getPosition());
                });
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

        turret = new Turret(hardwareMap, "turretServo",
                (servo, gamepad) -> {
                    if (gamepad.right_bumper)
                    {
                        servo.setPower(-1);
                    }
                    else if (gamepad.left_bumper)
                    {
                        servo.setPower(1);
                    }
                    else {
                        servo.setPower(0);
                    }
                },
                (servo, telemetry) -> {
                    telemetry.addData("turret position", servo.getPower());
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
        cascade.initializeTelemetry(telemetry);
        drawbridge.initializeTelemetry(telemetry);
        claw.initializeTelemetry(telemetry);
        auton = new AutonomousCascadeArm(
                cascade.getAutonomousBehaviors(),
                drawbridge.getAutonomousBehaviors(),
                claw.getAutonomousBehaviors(),
                telemetry);
        stow = new CascadeStow(auton);
    }

    @Override
    public void giveInstructions(Gamepad gamepad) {
        macros.update(gamepad);
        strategy.execute(this, gamepad, macros.claimed());
    }

    /**
     * Collects telemetry from all three components. Previously empty (Susan Zuo
     * — Bug #3: "No telemetry data reported"). Never flushes.
     */
    @Override
    public void updateTelemetry() {
        telemetry.addLine("--- Cascade Arm ---");
        claw.update();
        cascade.update();
        drawbridge.update();
    }
}
