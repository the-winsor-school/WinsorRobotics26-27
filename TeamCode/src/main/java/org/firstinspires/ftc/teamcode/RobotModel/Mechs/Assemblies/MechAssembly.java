package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Composite of all mech components on the robot. Inheritors hold discrete
 * {@link org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.MechComponent}
 * implementations and delegate instructions to them.
 *
 * <p><b>Telemetry contract (Susan Zuo):</b> Telemetry is injected once via
 * {@link #initializeTelemetry}, which must propagate the reference to every
 * child component before creating the autonomous behavior object.
 * {@link #updateTelemetry()} calls each component's {@code update()} to
 * collect data into the buffer — it never calls {@code telemetry.update()}.
 *
 * @author Susan Zuo (telemetry refactor)
 */
public abstract class MechAssembly
{
    protected interface IAssemblyStrategy { }
    protected MechAssembly.IAssemblyStrategy strategy;

    /**
     * Autonomous-facing surface for this assembly. Provides
     * {@link #reportStatus} and {@link #reportData} so autonomous strategies
     * do not need a raw {@code Telemetry} parameter (Susan Zuo — fixes
     * Bug #6: "Autonomous code forced to bypass the object model").
     */
    public abstract class AutonomousMechBehaviors {
        protected final Telemetry telemetry;
        public AutonomousMechBehaviors(Telemetry telemetry) {
            this.telemetry = telemetry;
        }
        /** Adds a status line to the telemetry buffer. Does NOT flush. */
        public void reportStatus(String status) { telemetry.addLine(status); }
        /** Adds a key-value pair to the telemetry buffer. Does NOT flush. */
        public void reportData(String key, Object value) { telemetry.addData(key, value); }
    }
    public abstract <T extends AutonomousMechBehaviors> T getAutonomousBehaviors();

    /**
     * Pass along gamepad instructions to subcomponents.
     * @param gamepad the Gamepad
     */
    public abstract void giveInstructions(Gamepad gamepad);

    protected Telemetry telemetry;

    /**
     * Stores the telemetry reference, propagates it to every child component,
     * then creates the autonomous behavior object. Called once by
     * {@code Robot.initializeSubsystems()} (Susan Zuo — two-phase
     * initialization: "construct → initializeTelemetry").
     */
    public abstract void initializeTelemetry(Telemetry telemetry);

    /**
     * Write assembly and component state to the owned telemetry buffer. Must
     * call {@code update()} on every child component so no component is
     * silently missing from the display (Susan Zuo — fixes Bug #5:
     * "Assemblies fail to report telemetry from all their child components").
     * Never calls {@code telemetry.update()}.
     */
    public abstract void updateTelemetry();
}
