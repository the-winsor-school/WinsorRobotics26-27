package org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.Tank;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Extensions.GamepadExtensions;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.DriveTrain;


public class OneStickTank extends DriveTrain
{

    private class LeroyState extends AutonomousDriving
    {
        ElapsedTime _stateTimer;
        double _chargeTime;
        boolean _isActive;

        /**
         * Instantiate a Leroy Jenkins state
         * @param chargeTime duration of the blind charge in seconds
         * @param telemetry telemetry injected at construction time
         */
        public LeroyState(double chargeTime, Telemetry telemetry)
        {
            super(telemetry);
            _stateTimer = new ElapsedTime(ElapsedTime.Resolution.SECONDS);
            _chargeTime = chargeTime;
            _isActive = false;
        }

        /**
         * LEROY JEEENKINS~!
         */
        public void activate()
        {
            left.setPower(1);
            right.setPower(1);
            _stateTimer.reset();
            _isActive = true;
            reportStatus("LEROY JENKINS!");
        }

        /**
         * Are we still Charging?
         * @return true if the state has completed
         */
        public boolean isCompleted()
        {
            if(!_isActive)
                return true;

            double duration = _stateTimer.time();
            if(duration < _chargeTime) {
                reportData("Leroy charge remaining", _chargeTime - duration);
                return false;
            }

            left.setPower(0);
            right.setPower(0);
            _isActive = false;
            reportStatus("Leroy complete");
            return true;
        }
    }

    @Override
    public LeroyState getAutonomousDriving()
    {
        return leroyState;
    }

    // these can be declared Final because once they are initialized they should not be changed.
    private final DcMotor left, right;
    private LeroyState leroyState;

    /**
     * Initialize the Tank Drive with Configuration motors named:
     * left -> "leftTread"
     * right -> "rightTread"
     * @param hardwareMap pass down from OpMode.
     */
    public OneStickTank(HardwareMap hardwareMap)
    {
        left = hardwareMap.get(DcMotor.class, "leftTread");
        right = hardwareMap.get(DcMotor.class, "rightTread");
    }

    /**
     * Creates the LeroyState with telemetry so it can report via
     * {@code reportStatus/reportData}. Must be called before {@link #drive}
     * (Susan Zuo — two-phase initialization pattern).
     */
    @Override
    public void initializeTelemetry(Telemetry telemetry)
    {
        this.telemetry = telemetry;
        leroyState = new LeroyState(10, telemetry);
    }

    @Override
    public void drive(Gamepad gamepad)
    {
        if(!leroyState.isCompleted())
            return; // Leroy is in control!

        if(gamepad.left_bumper && gamepad.right_bumper)
        {
            // LEROY JENKINS!
            leroyState.activate();
            return;
        }

        float throttle = GamepadExtensions.GetLeftStickY(gamepad);
        float turnBias = GamepadExtensions.GetLeftStickX(gamepad);
        float extraTurnBias = GamepadExtensions.GetRightStickX(gamepad);

        if( (extraTurnBias < 0 && turnBias < 0) ||
            (extraTurnBias > 0 && turnBias > 0) )
        {
            turnBias += extraTurnBias;
            if(Math.abs(turnBias) > 1)
                throttle = Math.max(Math.abs(throttle), Math.abs(turnBias) - 1);
        }

        left.setPower(calculateLeftPower(throttle, turnBias));
        right.setPower(calculateRightPower(throttle, turnBias));
    }

    private float calculateRightPower(float throttle, float turnBias)
    {
        if(turnBias <= 0)
            return throttle;

        return throttle * (1 - turnBias);
    }
    private float calculateLeftPower(float throttle, float turnBias)
    {
        if(turnBias >= 0)
            return throttle;

        return throttle * (1 + turnBias);
    }

    /**
     * Writes left/right power and Leroy active status to the buffer.
     * Previously empty (Susan Zuo — Bug #3: "No telemetry despite complex
     * Leroy Jenkins state"). Never flushes.
     */
    @Override
    public void updateTelemetry()
    {
        telemetry.addData("Left power:", left.getPower());
        telemetry.addData("Right power:", right.getPower());
        telemetry.addData("Leroy active:", leroyState != null && leroyState._isActive);
    }
}
