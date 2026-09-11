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

    // TODO: the claw is running blind. A CRServo has no notion of position, so right now
    //  nothing in this class can answer either of the two questions a driver actually
    //  cares about: "is it fully closed?" and "am I holding something?"
    //
    //  Two different sensors, answering two different questions - decide which this
    //  season's claw needs before wiring anything:
    //    - Travel limits. Two TouchSensors at fully-open and fully-closed, enforced the
    //      way DoublyLimitedMotor enforces a motor's limits. This stops the servo
    //      straining against a hard stop, which is what burns them out.
    //    - A grip sensor. One TouchSensor on the jaw, or a DistanceSensor or color
    //      sensor looking into the grip, answering "is there a game piece in there?"
    //      That turns a blind close() into something autonomous can actually branch on,
    //      and gives isHolding() as an autonomous verb worth having.
    //
    //  Whichever gets added, the same three leaks have to be closed first, or the new
    //  sensor is decoration that nothing is obliged to respect:
    //    1. `servo` below is public - make it private.
    //    2. ClawControlStrategy.chomp(CRServo, Gamepad) hands the raw servo to the
    //       teleop lambda. Change it to chomp(Gamepad, Claw) and call
    //       `strategy.chomp(gamepad, this)`, so the lambda goes through this class.
    //    3. AutonomousClawBehaviors.open/close/stop call servo.setPower(...) directly.
    //       Route them through a decorated setPower on Claw.
    //  DoublyLimitedMotor is the worked example of all three - read it before starting.
    //
    //  See the matching TODO on Turret.java, and the rule on MechComponent. Both classes
    //  are bare single-device wrappers of one CRServo today, so there should be ONE of
    //  them, named for the device rather than for a job. Adding a sensor here turns this
    //  into the other kind of component - output device plus the sensors that guide it -
    //  which is what earns a class of its own. Collapse the bare wrappers first, then
    //  build the sensing claw on top.
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
