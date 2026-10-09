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

        public void pushBalls() {
            servoR.setPosition(0.8);
            reportStatus("Pusher: push");
        }

        public void setPosition(double position) {
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
        this.servoR.setDirection(Servo.Direction.REVERSE);
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
