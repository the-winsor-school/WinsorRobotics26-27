package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class PusherServo extends MechComponent {

    public class AutonomousBallPusherBehaviors extends AutonomousComponentBehaviors {
        public AutonomousBallPusherBehaviors(Telemetry telemetry) {
            super(telemetry);
        }

        // TODO: pushBalls() and retractPusher() (the two verbs BillyRapidFire actually
        // uses) never call servoR.setDirection(...) - only setPosition(double) does, and
        // only the teleop strategy lambda in BillyMA sets REVERSE otherwise. A pure-
        // autonomous run that calls pushBalls()/retractPusher() before move() has ever
        // run leaves the servo at its hardware-default FORWARD direction, so these
        // position values (tuned assuming REVERSE) land in the wrong physical spot. Set
        // the direction once, e.g. in the constructor or initializeTelemetry(), instead
        // of leaving it to whichever caller happens to run first.
        public void pushBalls() {
            servoR.setPosition(0.8);
            reportStatus("Pusher: push");
        }

        public void setPosition(double position) {
            servoR.setDirection(Servo.Direction.REVERSE);
            servoR.setPosition(position);
            reportData("Pusher position", position);
        }

        public void retractPusher() {
            servoR.setPosition(0.0);
            reportStatus("Pusher: retract");
        }
    }

    public interface BallPusherControlStrategy extends IControlStrategy {
        void controlPusher(Servo servoR, Gamepad gamepad);
    }

    public interface PusherTelemetryStrategy
    {
        public void update(Servo servo, Telemetry telemetry);
    }

    private final Servo servoR;
    private final BallPusherControlStrategy strategy;
    protected final PusherTelemetryStrategy telemetryStrategy;
    private AutonomousBallPusherBehaviors auton;

    public PusherServo(HardwareMap hardwareMap,
                       String servoNameR,
                       BallPusherControlStrategy strategy,
                       PusherTelemetryStrategy telemetryStrategy) {
        super(strategy);
        this.servoR = hardwareMap.get(Servo.class, servoNameR);
        this.strategy = strategy;
        this.telemetryStrategy = telemetryStrategy;
    }

    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousBallPusherBehaviors(telemetry);
    }

    @Override
    public AutonomousBallPusherBehaviors getAutonomousBehaviors() {
        return auton;
    }

    @Override
    public void move(Gamepad gamepad) {
        strategy.controlPusher(servoR, gamepad);
    }

    /**
     * Delegates to {@code telemetryStrategy} when present. Previously,
     * {@code telemetryStrategy.update()} was never called even though a
     * strategy was configured (Susan Zuo — Bug #4: "telemetryStrategy is
     * ignored — strategies are either ignored or inconsistently used").
     * Never flushes.
     */
    @Override
    public void update() {
        if (telemetryStrategy != null) {
            telemetryStrategy.update(servoR, telemetry);
        } else {
            telemetry.addData("Right Pusher Position", "%.2f", servoR.getPosition());
        }
    }
}
