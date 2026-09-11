package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Claw extends MechComponent
{

    public class AutonomousClawBehaviors extends AutonomousComponentBehaviors
    {
        public AutonomousClawBehaviors(Telemetry telemetry) {
            super(telemetry);
        }

        public void open()
        {
            servo.setPower(1);
            reportStatus("Claw: open");
        }

        public void close()
        {
            servo.setPower(-1);
            reportStatus("Claw: close");
        }

        /** Halts the claw servo; added so autonomous routines can safely release
         *  the claw without setting power directly (Susan Zuo). */
        public void stop()
        {
            servo.setPower(0);
            reportStatus("Claw: stop");
        }
    }

    private AutonomousClawBehaviors auton;

    @Override
    public AutonomousClawBehaviors getAutonomousBehaviors()
    {
        return auton;
    }


    public interface ClawControlStrategy extends IControlStrategy
    {
        public void chomp(CRServo servo, Gamepad gamepad);
    }

    public interface ClawTelemetryStrategy
    {
        public void update(CRServo servo, Telemetry telemetry);
    }
    public CRServo servo;

    protected ClawControlStrategy strategy;
    protected ClawTelemetryStrategy telemetryStrategy;

    public Claw(HardwareMap hardwareMap,
                String servoName,
                ClawControlStrategy strategy,
                ClawTelemetryStrategy telemetryStrategy)
    {
        super(strategy);
        servo = hardwareMap.get(CRServo.class, servoName);
        this.strategy = strategy;
        this.telemetryStrategy = telemetryStrategy;
    }

    /**
     * Stores the telemetry reference (via super) and lazily creates the autonomous
     * behaviors object (Susan Zuo — two-phase initialization pattern).
     */
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousClawBehaviors(telemetry);
    }

    public void move(Gamepad gamepad)
    {
        strategy.chomp(servo, gamepad);
    }

    /**
     * Delegates to {@code telemetryStrategy} when present, otherwise writes
     * claw power directly. Previously empty (Susan Zuo — Bug #3: "No telemetry
     * data reported"). Never flushes.
     */
    @Override
    public void update()
    {
        if (telemetryStrategy != null) {
            telemetryStrategy.update(servo, telemetry);
        } else {
            telemetry.addData("Claw power:", servo.getPower());
        }
    }

}
