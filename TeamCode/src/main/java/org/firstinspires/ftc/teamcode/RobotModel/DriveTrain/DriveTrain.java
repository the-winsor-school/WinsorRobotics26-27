package org.firstinspires.ftc.teamcode.RobotModel.DriveTrain;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Abstract base for all drive train implementations.
 *
 * <p><b>Telemetry contract (Susan Zuo):</b> Telemetry is injected once via
 * {@link #initializeTelemetry} (called by {@code Robot.initializeSubsystems()})
 * rather than passed on every loop. {@link #updateTelemetry()} writes to the
 * buffer but never flushes — only {@code Robot.updateTelemetry()} calls
 * {@code telemetry.update()}.
 *
 * @author Susan Zuo (telemetry refactor)
 */
public abstract class DriveTrain
{
    /**
     * Autonomous-facing surface for this drive train. Provides
     * {@link #reportStatus} and {@link #reportData} so autonomous driving
     * code does not need a raw {@code Telemetry} parameter (Susan Zuo —
     * fixes Bug #6).
     */
    public abstract class AutonomousDriving {
        protected final Telemetry telemetry;
        public AutonomousDriving(Telemetry telemetry) {
            this.telemetry = telemetry;
        }
        /** Adds a status line to the telemetry buffer. Does NOT flush. */
        public void reportStatus(String status) { telemetry.addLine(status); }
        /** Adds a key-value pair to the telemetry buffer. Does NOT flush. */
        public void reportData(String key, Object value) { telemetry.addData(key, value); }
    }
    public abstract <T extends AutonomousDriving> T getAutonomousDriving();

    public abstract void drive(Gamepad gamepad);

    protected Telemetry telemetry;

    /**
     * Stores the telemetry reference and creates the autonomous driving
     * object. Called once by {@code Robot.initializeSubsystems()} after
     * construction — never call from a constructor (Susan Zuo — two-phase
     * initialization pattern).
     */
    public abstract void initializeTelemetry(Telemetry telemetry);

    /**
     * Write drive train state to the owned telemetry buffer. Never calls
     * {@code telemetry.update()} (Susan Zuo — "Single Point of Control").
     */
    public abstract void updateTelemetry();
}
