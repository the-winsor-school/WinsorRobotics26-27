package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Extensions.GamepadExtensions;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.Claw;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.DoublyLimitedMotor;

public class CascadeArm extends MechAssembly
{
    public class AutonomousCascadeArm extends AutonomousMechBehaviors
    {
        public final DoublyLimitedMotor.AutonomousDLMBehaviors cascade;
        public final DoublyLimitedMotor.AutonomousDLMBehaviors drawbridge;
        public final Claw.AutonomousClawBehaviors claw;

        public AutonomousCascadeArm(
                DoublyLimitedMotor.AutonomousDLMBehaviors cascade,
                DoublyLimitedMotor.AutonomousDLMBehaviors drawbridge,
                Claw.AutonomousClawBehaviors claw,
                Telemetry telemetry)
        {
            super(telemetry);
            this.cascade = cascade;
            this.drawbridge = drawbridge;
            this.claw = claw;
        }
    }

    private AutonomousCascadeArm auton;

    @Override
    public AutonomousCascadeArm getAutonomousBehaviors()
    {
        return auton;
    }

    private final DoublyLimitedMotor cascade;
    private final DoublyLimitedMotor drawbridge;
    private final Claw claw;

    public CascadeArm (HardwareMap hardwareMap){
        cascade = new DoublyLimitedMotor(
                hardwareMap,
                "cascadeMotor",
                "topLiftLimit",
                "bottomLiftLimit",
                // Control strategy
                (gamepad, dlm) ->
                {
                     double power = GamepadExtensions.GetLeftStickY(gamepad);
                     dlm.setPower(power);
                },
                ((dlm, telemetry) ->
                {
                    telemetry.addData("Can Go Up", dlm.canGoForward());
                    telemetry.addData("Can Go Down", dlm.canGoReverse());
                })
                );
        drawbridge = new DoublyLimitedMotor(
                hardwareMap,
                "drawbridge",
                "topDrawLimit",
                "botDrawLimit",
                // Control Strategy
                (gamepad, dlm) -> {
                    double power = GamepadExtensions.GetRightStickY(gamepad);
                    dlm.setPower(power);
                },
                ((dlm, telemetry) -> {
                    telemetry.addData("Can Go Up", dlm.canGoForward());
                    telemetry.addData("Can Go Down", dlm.canGoReverse());
                }));
        claw = new Claw(
                hardwareMap,
                "servo",
                (servo, gamepad) -> {
                    if (gamepad.a) servo.setPower(1.0);
                    else if (gamepad.b) servo.setPower(-1.0);
                    else servo.setPower(0);
                },
                (servo, telemetry) -> {
                    telemetry.addData("Claw Position", servo.getPower());
                }
        );
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
    }

    @Override
    public void giveInstructions(Gamepad gamepad) {
        cascade.move(gamepad);
        drawbridge.move(gamepad);
        claw.move(gamepad);
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
