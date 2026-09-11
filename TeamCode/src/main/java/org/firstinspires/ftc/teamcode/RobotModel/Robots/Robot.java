package org.firstinspires.ftc.teamcode.RobotModel.Robots;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.RobotModel.DriveTrain.DriveTrain;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.MechAssembly;
import org.firstinspires.ftc.vision.VisionPortal;

/**
 * Abstract base for all robots in the system.
 *
 * <p><b>Telemetry contract (Susan Zuo):</b> Telemetry is injected at
 * construction via {@code Robot(Telemetry)} and propagated to every subsystem
 * by {@link #initializeSubsystems()}. This class is the <em>single flush
 * point</em> for the entire robot — only {@link #updateTelemetry()} calls
 * {@code telemetry.update()}. No other layer may call it.
 *
 * <p>Previously, telemetry was passed as a parameter on every loop
 * ({@code updateTelemetry(Telemetry)}), causing multiple competing flush
 * points and mid-cycle updates. Susan Zuo identified this as the root cause
 * of Bugs #1, #2, and #7: "Split Ownership — multiple layers assume they
 * can call telemetry.update()."
 *
 * @author Susan Zuo (telemetry refactor)
 */
public abstract class Robot
{

    protected interface IRobotStrategy { }
    protected Robot.IRobotStrategy strategy;

    /**
     * Autonomous-facing surface for the full robot. Provides
     * {@link #reportStatus}, {@link #reportData}, and {@link #clearTelemetry}
     * so top-level autonomous strategies do not need a raw {@code Telemetry}
     * parameter (Susan Zuo — fixes Bug #6: "AutonomousRobot provides no
     * telemetry support — forces strategies to use raw telemetry").
     */
    public abstract class AutonomousRobot
    {
        protected final Telemetry telemetry;

        /**
         * Requires that any extending class provide matching parameter types in its constructor.
         * @param driveTrain Any AutonomousDriving implementation
         * @param mechAssembly Any AutonomousMechBehaviors implementation
         * @param telemetry Telemetry injected at construction time
         */
        public AutonomousRobot(
                DriveTrain.AutonomousDriving driveTrain,
                MechAssembly.AutonomousMechBehaviors mechAssembly,
                Telemetry telemetry)
        {
            this.telemetry = telemetry;
        }

        /** Adds a status line to the telemetry buffer. Does NOT flush. */
        public void reportStatus(String status) { telemetry.addLine(status); }
        /** Adds a key-value pair to the telemetry buffer. Does NOT flush. */
        public void reportData(String key, Object value) { telemetry.addData(key, value); }
        public void clearTelemetry() { telemetry.clear(); }
    }

    protected interface IControlStrategy {  }


    /**
     * This abstract method definition tells inheriting classes that they MUST define a
     * way to run Autonomously.
     * @return a CONCRETE implementation of the ABSTRACT AutonomousRobot type.
     * @param <T> Any type which extends AutonomousRobot
     */
    public abstract <T extends AutonomousRobot> T getAutonomousRobot();

    protected VisionPortal visionPortal;
    protected DriveTrain driveTrain;
    protected MechAssembly mechAssembly;
    protected Telemetry telemetry;

    protected Robot(Telemetry telemetry)
    {
        this.telemetry = telemetry;
    }

    /**
     * Propagates the owned telemetry reference to driveTrain and mechAssembly,
     * triggering their two-phase initialization. Call this in subclass
     * constructors after both fields are assigned (Susan Zuo — "construct →
     * initializeTelemetry" two-phase pattern).
     */
    protected void initializeSubsystems()
    {
        if (driveTrain != null) driveTrain.initializeTelemetry(telemetry);
        if (mechAssembly != null) mechAssembly.initializeTelemetry(telemetry);
    }

    /**
     * The single flush point for all telemetry in the robot. Collects data
     * from driveTrain and mechAssembly, then calls {@code telemetry.update()}
     * exactly once per loop (Susan Zuo — "Single Point of Control: only Robot
     * is allowed to call telemetry.update(). All other layers write data but
     * never flush." Fixes Bugs #1, #2, #7).
     */
    public void updateTelemetry() {
        if (driveTrain != null) driveTrain.updateTelemetry();
        if (mechAssembly != null) mechAssembly.updateTelemetry();
        telemetry.update();
    }

    /**
     * update the robot state!  Pass along the gamepads to the different modules.
     * @param gamepad1 driver gamepad
     * @param gamepad2 mech gamepad
     */
    public void update(Gamepad gamepad1, Gamepad gamepad2)
    {
        if (driveTrain != null) driveTrain.drive(gamepad1);
        if (mechAssembly != null) mechAssembly.giveInstructions(gamepad2);
    }
}
