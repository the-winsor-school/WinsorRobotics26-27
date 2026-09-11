package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Composite of all mech components on the robot. Inheritors hold discrete
 * {@link org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.MechComponent}
 * implementations and delegate instructions to them.
 *
 * <p><b>Assemblies may own sensors too.</b> A component owns the sensors that
 * guide its own single mechanism -
 * {@link org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.DoublyLimitedMotor}
 * and its two limit switches are the worked example. An assembly owns the
 * sensors that describe a <em>relationship between</em> its components, which
 * no single component can see. The sorting rule is the same one that separates
 * the layers everywhere else:
 *
 * <ul>
 *   <li>guides one mechanism, and protects it from itself -&gt; component</li>
 *   <li>reports on the handoff or interaction between two mechanisms -&gt; assembly</li>
 * </ul>
 *
 * <p>TODO: no assembly in this codebase owns a sensor yet, so there is no
 *  reference implementation to copy, and the `doc/FlintLessons` have no example
 *  to teach from. Billy is the obvious candidate. A beam-break or color sensor
 *  sitting between the intake and the pusher would answer "is a ball actually
 *  seated and ready to fire?" - a question that spans intake, pusher and
 *  flywheel, so it fits in none of them. With it, BillyRapidFire could wait on
 *  a real signal instead of the fixed 2200ms guess it currently uses.
 *
 *  What this layer needs, whenever the first one gets built:
 *  <ul>
 *    <li>the assembly holds the sensor handle itself, fetched in its constructor
 *        alongside its components</li>
 *    <li>its readings get reported in {@link #updateTelemetry()}, the same as any
 *        component's state</li>
 *    <li>the assembly's {@code AutonomousMechBehaviors} exposes what the sensor
 *        <em>means</em> as a verb or a question ({@code isBallSeated()}), rather
 *        than the raw reading</li>
 *    <li>the strategy lambda is where the reading changes what the components are
 *        told to do</li>
 *  </ul>
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
