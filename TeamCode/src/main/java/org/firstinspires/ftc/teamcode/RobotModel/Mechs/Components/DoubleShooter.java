package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class DoubleShooter extends MechComponent
{
    public DoubleShooter(
            HardwareMap hardwareMap,
            String motorNameF, String motorNameB,
            ShooterControlStrategy pewpew,
            ShooterTelemetryStrategy telemetryStrategy) {
        super(pewpew);
        shooterF = hardwareMap.get(DcMotor.class, motorNameF);
        shooterB = hardwareMap.get(DcMotor.class, motorNameB);
        this.pewpew = pewpew;
        this.telemetryStrategy = telemetryStrategy;
    }

    public class AutonomousShooterBehavior extends AutonomousComponentBehaviors {
        public AutonomousShooterBehavior(Telemetry telemetry) {
            super(telemetry);
        }

        public void StartShoot() {
            shooterF.setPower(0.76);
            shooterB.setPower(-0.76);
            reportStatus("Shooter: running");
        }
        public void StopShoot(){
            shooterF.setPower(0);
            shooterB.setPower(0);
            reportStatus("Shooter: stopped");
        }

        public void setPower(double power){
            shooterF.setPower(power);
            shooterB.setPower(-power);
            reportData("Shooter power", power);
        }
    }

    private AutonomousShooterBehavior auton;

    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousShooterBehavior(telemetry);
    }

    @Override
    public AutonomousShooterBehavior getAutonomousBehaviors() {
        return auton;
    }

    public interface ShooterControlStrategy extends IControlStrategy
    {
        void shoot(DcMotor motorF, DcMotor motorB, Gamepad gamepad);
    }

    public interface ShooterTelemetryStrategy
    {
        public void update(DcMotor motorF, DcMotor motorB, Telemetry telemetry);
    }
    private final DcMotor shooterF;
    private final DcMotor shooterB;

    protected ShooterControlStrategy pewpew;
    protected ShooterTelemetryStrategy telemetryStrategy;

    @Override
    public void move(Gamepad gamepad) {
        pewpew.shoot(shooterF, shooterB, gamepad);
    }

    /**
     * Delegates to {@code telemetryStrategy} if one was provided, otherwise
     * writes shooter powers directly. Previously empty despite having a
     * strategy (Susan Zuo — Bug #3: "No telemetry data reported despite
     * having telemetryStrategy"). Never flushes.
     */
    @Override
    public void update() {
        if (telemetryStrategy != null) {
            telemetryStrategy.update(shooterF, shooterB, telemetry);
        } else {
            telemetry.addData("shooterF power:", shooterF.getPower());
            telemetry.addData("shooterB power:", shooterB.getPower());
        }
    }
}
