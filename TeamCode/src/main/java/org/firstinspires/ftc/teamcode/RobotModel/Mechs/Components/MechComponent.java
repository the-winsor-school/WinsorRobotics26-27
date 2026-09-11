package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Base class for every mechanism component on the robot.
 *
 * <p><b>What belongs in a component.</b> Every concrete MechComponent should be
 * exactly one of two things:
 *
 * <ol>
 *   <li><b>A wrapper around a single output device</b> - one motor, one servo,
 *       one CR servo. The class adds the strategy seam, the autonomous verbs,
 *       and telemetry, and nothing else. We need exactly <em>one</em> of these
 *       per device type, and it should be named for the <em>device</em>, since
 *       any mechanism built on that device reuses it as-is.</li>
 *   <li><b>A purposeful collection of output devices plus the sensors that
 *       guide them</b> - a motor bounded by two limit switches, two motors
 *       driven as an opposed pair, a camera and the processors reading it.
 *       These are named for what the collection <em>does</em>, because the
 *       coupling between the outputs and the sensors is the whole point.</li>
 * </ol>
 *
 * <p>A class that is neither - a second single-device wrapper named after one
 * robot's job for it - is duplication, and every fix to one is a fix owed to
 * the other.
 *
 * <p>TODO: the current components have yet to be sorted against that rule, and
 *  the naming convention for category 1 is still an open question. As it stands:
 *  <ul>
 *    <li>{@code SpinnyIntake} (one DcMotor), {@code PusherServo} (one Servo),
 *        {@code Claw} and {@code Turret} (one CRServo <em>each</em>) are all
 *        category 1, and all four are named for a job rather than a device.</li>
 *    <li>{@code Claw} and {@code Turret} are therefore two wrappers of the same
 *        device. One device-named class should serve both - until each grows the
 *        sensors it needs, at which point they become category 2 and separate
 *        classes are earned. See the TODOs on both files.</li>
 *    <li>{@code DoublyLimitedMotor}, {@code DoubleShooter} and
 *        {@code BallDetectionComponent} are already category 2 and already named
 *        for what they do.</li>
 *  </ul>
 *  Settle the category-1 naming convention before renaming anything, since every
 *  assembly's construction code moves with it.
 *
 * <p><b>Telemetry contract (Susan Zuo):</b> Telemetry is injected once via
 * {@link #initializeTelemetry} rather than passed as a loop-time parameter.
 * Each component owns its {@code Telemetry} reference and writes freely to the
 * buffer; {@code telemetry.update()} is never called here — that is the
 * exclusive responsibility of {@code Robot.updateTelemetry()}.
 *
 * <p>Subclasses must call {@link #initializeTelemetry} (through their
 * assembly's {@code initializeTelemetry}) before the first loop iteration.
 *
 * @author Susan Zuo (telemetry refactor)
 */
public abstract class MechComponent
{
    /**
     * Autonomous-facing surface for this component. Exposes
     * {@link #reportStatus} and {@link #reportData} so autonomous strategies
     * can write telemetry through the robot object model without receiving a
     * raw {@code Telemetry} parameter directly (Susan Zuo — fixes Bug #6:
     * "Autonomous code forced to use raw telemetry because proper abstractions
     * don't exist").
     */
    public abstract class AutonomousComponentBehaviors {
        protected final Telemetry telemetry;
        public AutonomousComponentBehaviors(Telemetry telemetry) {
            this.telemetry = telemetry;
        }
        /** Adds a status line to the telemetry buffer. Does NOT flush. */
        public void reportStatus(String status) { telemetry.addLine(status); }
        /** Adds a key-value pair to the telemetry buffer. Does NOT flush. */
        public void reportData(String key, Object value) { telemetry.addData(key, value); }
    }
    public abstract <T extends AutonomousComponentBehaviors> T getAutonomousBehaviors();
    protected interface IControlStrategy { }

    protected IControlStrategy strategy;
    protected Telemetry telemetry;

    protected MechComponent(IControlStrategy strategy)
    {
        this.strategy = strategy;
    }

    /**
     * Two-phase initializer: stores the telemetry reference and creates the
     * autonomous behavior object. Called by the enclosing assembly's own
     * {@code initializeTelemetry} — never call from a constructor.
     *
     * <p>Kept separate from the constructor so existing robot construction
     * code requires minimal changes (Susan Zuo — "Benefit: Existing robot
     * construction code requires minimal changes. Cost: Two-phase
     * initialization creates potential for incomplete setup.").
     */
    public void initializeTelemetry(Telemetry telemetry)
    {
        this.telemetry = telemetry;
    }

    public abstract void move(Gamepad gamepad);

    /**
     * Write component state to the owned telemetry buffer. Never calls
     * {@code telemetry.update()} — that is the sole responsibility of
     * {@code Robot.updateTelemetry()} (Susan Zuo — "Single Point of Control:
     * Only Robot is allowed to call telemetry.update()").
     */
    // Note: this and its overrides were package-private until the 2026-27 migration.
    // MechAssembly subclasses live in a different package (…Mechs.Assemblies), so they
    // could not call a component's update() at all - which is why CascadeArm and
    // ExampleIntakeAssembly wouldn't compile. Both are public now; keep them that way.
    public abstract void update();
}
