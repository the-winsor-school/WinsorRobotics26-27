package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Extensions.ThreadExtensions;

public class Turret extends MechComponent
{

    public class AutonomousTurretBehaviors extends AutonomousComponentBehaviors
    {
        public AutonomousTurretBehaviors(Telemetry telemetry) {
            super(telemetry);
        }

        public void setPower(double power)
        {
            servo.setPower(power);
            reportData("Turret power", power);
        }
        // TODO: these two block for 100ms every call. An autonomous verb should command
        //  the hardware and return immediately - the caller decides how long to wait.
        //  LimelightAutoTarget calls into this turret every single teleop loop, so a
        //  sleep here stalls the whole robot loop (drive train included) 10 times a
        //  second. Note setPower() and stop() right here don't sleep, so the component
        //  isn't even consistent with itself. Drop the sleeps; if a caller needs a timed
        //  turn, that's what StateMachine.doAndWait() is for (Flint Lesson 9).
        public void turnCCW() { servo.setPower(1); reportStatus("Turret: CCW"); ThreadExtensions.TrySleep(100); }
        public void turnCW() { servo.setPower(-1); reportStatus("Turret: CW"); ThreadExtensions.TrySleep(100); }
        public void stop() { servo.setPower(0); reportStatus("Turret: stopped"); }
    }

    private AutonomousTurretBehaviors auton;

    @Override
    public AutonomousTurretBehaviors getAutonomousBehaviors()
    {
        return auton;
    }


    public interface TurretControlStrategy extends IControlStrategy
    {
        public void move(CRServo servo, Gamepad gamepad);
    }

    public interface TurretTelemetryStrategy
    {
        public void update(CRServo servo, Telemetry telemetry);
    }

    // TODO: the turret needs a pair of travel limits. Right now it will happily keep
    //  rotating in one direction for as long as something commands it to, which wraps the
    //  wiring and eventually damages the mechanism. Add two TouchSensors at the ends of
    //  turret travel and enforce them here, the way DoublyLimitedMotor does for a motor:
    //  take both sensor names in the constructor, add canGoCW()/canGoCCW(), and put a
    //  decorated `public void setPower(double)` on Turret that zeroes any power heading
    //  into a limit that is already pressed.
    //
    //  The decorator alone is not enough, though - there are three ways to get at this
    //  servo that would walk straight past it, and all three have to close:
    //    1. `servo` below is a public field, so any caller can grab the CRServo and
    //       command it directly. Make it private.
    //    2. TurretControlStrategy.move(CRServo, Gamepad) hands the raw servo to the
    //       teleop lambda, which then calls servo.setPower(...) itself (see BillyMA).
    //       Change the interface to move(Gamepad, Turret) and call
    //       `strategy.move(gamepad, this)`, so the lambda has to go through the
    //       decorated setPower. DoublyLimitedMotorControlStrategy already has exactly
    //       this signature - copy the shape of it, not the class.
    //    3. AutonomousTurretBehaviors.setPower/turnCW/turnCCW/stop all call
    //       servo.setPower(...) directly too. Route them through the decorated method
    //       as well, or LimelightAutoTarget will drive the turret past its own limits.
    //
    //  Worth noticing what this does to the Claw/Turret duplication. Today both classes
    //  are bare single-device wrappers of one CRServo, which by the rule on
    //  MechComponent means there should be ONE of them, named for the device. Adding the
    //  limits turns this class into the other kind of component - an output device
    //  coupled to the sensors that guide it - and that is what earns it a class of its
    //  own. So the fix is two moves, not one: collapse the bare wrappers into a single
    //  device-named component, and build the limited turret on top of it.
    public CRServo servo;

    protected TurretControlStrategy strategy;
    protected TurretTelemetryStrategy telemetryStrategy;

    public Turret(HardwareMap hardwareMap,
                String servoName,
                TurretControlStrategy strategy,
                TurretTelemetryStrategy telemetryStrategy)
    {
        super(strategy);
        servo = hardwareMap.get(CRServo.class, servoName);
        this.strategy = strategy;
        this.telemetryStrategy = telemetryStrategy;
    }

    /**
     * Stores the telemetry reference (via super) and lazily creates the autonomous
     * behaviors object. Previously {@code auton} was a field-initializer that ran
     * before telemetry was available, so the inner class had no telemetry reference
     * (Susan Zuo — two-phase initialization pattern).
     */
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousTurretBehaviors(telemetry);
    }

    public void move(Gamepad gamepad)
    {
        strategy.move(servo, gamepad);
    }

    /**
     * Delegates to {@code telemetryStrategy} when present, otherwise writes
     * turret power directly. Previously empty (Susan Zuo — Bug #3: "No telemetry
     * data reported despite having telemetryStrategy"). Never flushes.
     */
    @Override
    public void update()
    {
        if (telemetryStrategy != null) {
            telemetryStrategy.update(servo, telemetry);
        } else {
            telemetry.addData("turret power:", servo.getPower());
        }
    }

}
