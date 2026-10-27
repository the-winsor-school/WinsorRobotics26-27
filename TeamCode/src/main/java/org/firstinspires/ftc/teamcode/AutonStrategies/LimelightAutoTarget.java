package org.firstinspires.ftc.teamcode.AutonStrategies;

import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.firstinspires.ftc.teamcode.Extensions.IState;
import org.firstinspires.ftc.teamcode.Extensions.LimelightExtensions;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.Turret;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.BillyMA;
import org.firstinspires.ftc.teamcode.RobotModel.Robots.Robot;
import com.qualcomm.robotcore.hardware.Gamepad;
import java.util.EnumSet;
import java.util.Set;

/**
 * State machine that rotates the turret until the target AprilTag is centred in
 * the Limelight's field of view. Previously held a raw {@code Telemetry} reference
 * and called {@code telemetry.update()} between state transitions (Susan Zuo —
 * Bug #2: "mid-cycle {@code telemetry.update()} in state machines", and Bug #6:
 * "autonomous strategies held raw telemetry references, bypassing the object
 * model"). All reporting now goes through
 * {@code turret.reportStatus/reportData} so the single-flush rule is respected.
 */
public class LimelightAutoTarget extends StateMachine implements Robot.IAssistRobotStrategy<BillyMA.Part>
{
    private final int targetTagId;
    private final Limelight3A limelight;
    private final Turret.AutonomousTurretBehaviors turret;

    /**
     * @param limelight  the Limelight3A sensor
     * @param turret     the live autonomous turret behavior — carries its own
     *                   telemetry reference, so no raw {@code Telemetry} arg is
     *                   needed here (Susan Zuo — Bug #6)
     * @param tagId      AprilTag ID to track
     */
    public LimelightAutoTarget(
            Limelight3A limelight,
            Turret.AutonomousTurretBehaviors turret,
            int tagId)
    {
        this.limelight = limelight;
        this.turret = turret;
        this.targetTagId = tagId;
        currentState = lookForTag();
    }

    @Override
    public Set<BillyMA.Part> claims()
    {
        return EnumSet.of(BillyMA.Part.TURRET);
    }
    @Override 
    public String name()
    {
        return "LimelightAutoTarget";
    }
    @Override
    public void start()
    {
        currentState = lookForTag();
    }
    @Override 
    public void update(Gamepad driver, Gamepad mech)
    {
        updateState();
    }
    @Override 
    public void stop()
    {
        abort();
        turret.stop();
    }
    

    public IState rotateCCW(double tx){
        return () ->
        {
            double power = 1;
            if(tx > -10)
                power = -tx / 10.0;
            turret.setPower(power);
            turret.reportData("Turret CCW", power);
            return lookForTag();
        };
    }
    public IState rotateCW(double tx) {
        return () ->
        {
            double power = -1;
            if (tx < 10)
                power = -tx / 10.0;
            turret.setPower(power);
            turret.reportData("Turret CW", power);
            return lookForTag();
        };
    }

    public IState stopTurret() {
        return () ->
        {
            turret.stop();
            turret.reportStatus("Turret Stopped");
            return lookForTag();
        };
    }

    public IState lookForTag() {
        return()->
        {
            LLResultTypes.FiducialResult tag =
                    LimelightExtensions.tryGetFiducial(
                            limelight,
                            targetTagId);

            if(tag == null)
            {
                turret.reportStatus("Tag " + targetTagId + " not found.");
                return stopTurret();
            }
            double tx = tag.getTargetXDegrees();
            if(tx < -2) return rotateCCW(tx);
            else if(tx > 2) return rotateCW(tx);
            else            return stopTurret();
        };
    }
}
