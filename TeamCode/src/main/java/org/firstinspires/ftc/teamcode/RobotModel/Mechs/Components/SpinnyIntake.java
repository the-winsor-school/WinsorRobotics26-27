package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class SpinnyIntake extends MechComponent
{
    public class AutonomousIntakeBehaviors extends MechComponent.AutonomousComponentBehaviors
    {
        public AutonomousIntakeBehaviors(Telemetry telemetry) {
            super(telemetry);
        }

        // TODO: check these signs against BillyMA's teleop strategy for this same motor -
        // there, dpad_up (the "intake" gesture) uses +0.75 and dpad_down (reverse) uses
        // -0.75. startIntake() and reverseIntake() here use the opposite signs from each
        // other's teleop equivalents, so an autonomous routine calling startIntake() would
        // actually run the intake backwards.
        public void startIntake()
        {
            intake.setPower(-1);
            reportStatus("Intake: running");
        }

        public void stopIntake()
        {
            intake.setPower(0);
            reportStatus("Intake: stopped");
        }

        public void reverseIntake()
        {
            intake.setPower(1);
            reportStatus("Intake: reversed");
        }
    }


    public interface SpinnyIntakeControlStrategy extends IControlStrategy
    {
        /**
         * abstract definition of how a Spinny Intake gets controlled.
         * this Implementation should translate the Gamepad Input into motor movement
         * of the Spinny Intake.
         * The name of this method does not matter at all~ so make it descriptive in the
         * context of the Spinny Intake!
         * @param motor the `intake` motor will be passed here.
         * @param gamepad the gamepad will be passed down from the MechAssembly to this strategy
         */
        void nomNomNom(DcMotor motor, Gamepad gamepad);
    }

    private final DcMotor intake;

    protected SpinnyIntakeControlStrategy strategy;

    private AutonomousIntakeBehaviors auton;

    public SpinnyIntake(
            HardwareMap hardwareMap,
            String motorName,
            SpinnyIntakeControlStrategy strategy)
    {
        super(strategy);
        intake = hardwareMap.get(DcMotor.class, motorName);
        this.strategy = (SpinnyIntakeControlStrategy) super.strategy;
    }

    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousIntakeBehaviors(telemetry);
    }

    @Override
    public AutonomousIntakeBehaviors getAutonomousBehaviors() {
        return auton;
    }

    @Override
    public void move(Gamepad gamepad)
    {
        strategy.nomNomNom(intake, gamepad);
    }

    /**
     * Writes intake power and encoder position to the buffer. Previously
     * empty (Susan Zuo — Bug #3: "No telemetry data reported"). Never flushes.
     */
    @Override
    public void update() {
        telemetry.addData("intake power:", intake.getPower());
        telemetry.addData("intake position:", intake.getCurrentPosition());
    }
}
