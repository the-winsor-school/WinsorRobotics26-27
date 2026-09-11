package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public  class DoublyLimitedMotor extends MechComponent {

    public class AutonomousDLMBehaviors extends MechComponent.AutonomousComponentBehaviors
    {
        public AutonomousDLMBehaviors(Telemetry telemetry) {
            super(telemetry);
        }

        public void goForward()
        {
            setPower(1);
            reportStatus("DLM: forward");
        }

        public void stop()
        {
            setPower(0);
            reportStatus("DLM: stop");
        }

        public void goBackward()
        {
            setPower(-1);
            reportStatus("DLM: backward");
        }
    }

    public interface DoublyLimitedMotorControlStrategy extends IControlStrategy{
        public void move(Gamepad gamepad, DoublyLimitedMotor doublyLimitedMotor);
    }

    public interface DoublyLimitedMotorTelemetryStrategy {
        public void update(DoublyLimitedMotor dlm, Telemetry telemetry);
    }

    private DcMotor motor;
    private TouchSensor forwardSensor;
    private TouchSensor reverseSensor;
    private DoublyLimitedMotorControlStrategy strategy;

    private AutonomousDLMBehaviors auton;
    protected DoublyLimitedMotorTelemetryStrategy telemetryStrategy;

    /**
     * The forward sensor is the sensor that is hit when the motor is given positive power
     * @param hardwareMap pass this down from on high
     * @param motorName name of the motor in the driver station configuration
     * @param forwardSensorName name of the forward sensor in the driver station configuration
     * @param reverseSensorName name of the reverse sensor in the driver station configuration
     * @param strategy lambda expression (Gamepad, DoublyLimitedMotor) for translating gamepad input into motor movement
     */
    public DoublyLimitedMotor(
            HardwareMap hardwareMap,
            String motorName,
            String forwardSensorName,
            String reverseSensorName,
            DoublyLimitedMotorControlStrategy strategy,
            DoublyLimitedMotorTelemetryStrategy telemetryStrategy
            ) {
        super(strategy);
        motor = hardwareMap.get(DcMotor.class, motorName);
        forwardSensor = hardwareMap.get(TouchSensor.class, forwardSensorName);
        reverseSensor = hardwareMap.get(TouchSensor.class, reverseSensorName);
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
        auton = new AutonomousDLMBehaviors(telemetry);
    }

    /**
     * this is a decorator method that checks to see if the motor can go forward or reverse before setting motor power
     * if the motor cannot move in that direction, it sets the power to zero
     * @param power assumes that power is between -1 and 1, and that the dead zone is applied
     */
    public void setPower(double power){

        if ((power > 0 && !canGoForward() )|| (power < 0 && !canGoReverse())) {
            power = 0;
        }

        motor.setPower(power);
    }


    @Override
    public AutonomousDLMBehaviors getAutonomousBehaviors() {
        return auton;
    }

    @Override
    public void move(Gamepad gamepad) {
        strategy.move(gamepad, this);

    }

    /**
     * Delegates to {@code telemetryStrategy} when present, otherwise writes
     * motor power and limit-sensor states directly. Previously empty (Susan Zuo
     * — Bug #3: "No telemetry data reported"). Never flushes.
     */
    @Override
    public void update()
    {
        if (telemetryStrategy != null) {
            telemetryStrategy.update(this, telemetry);
        } else {
            telemetry.addData("motor power:", motor.getPower());
            telemetry.addData("can go forward:", canGoForward());
            telemetry.addData("can go reverse:", canGoReverse());
        }
    }


    /**
     *
     * @return true if the forward sensor is not pressed
     */
    public boolean canGoForward()
    {
        return ! forwardSensor.isPressed();
    }

    /**
     *
     * @return true if the reverse sensor is not pressed
     */
    public boolean canGoReverse()
    {
        return ! reverseSensor.isPressed();
    }
}
