package org.firstinspires.ftc.teamcode.AutonStrategies;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Extensions.IState;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.CascadeArm;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.MechAssembly;

import java.util.EnumSet;
import java.util.Set;

//TODO: not yet tested on hardware - needs a lift and drawbridge with limit switches.


public class CascadeStow extends StateMachine
        implements MechAssembly.IMacroAssemblyStrategy<CascadeArm.Part>
{
    private static final int TIMEOUT_MS = 3000;

    private final CascadeArm.AutonomousCascadeArm arm;
    private ElapsedTime timer;

    public CascadeStow(CascadeArm.AutonomousCascadeArm arm)
    {
        this.arm = arm;
    }

    @Override
    public Set<CascadeArm.Part> claims()
    {
        return EnumSet.of(CascadeArm.Part.CASCADE, CascadeArm.Part.DRAWBRIDGE);
    }

    @Override
    public void start()
    {
        timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
        currentState = lowerBoth();
    }

    @Override
    public void update(Gamepad gamepad) { updateState(); }

    @Override
    public void cancel()
    {
        arm.cascade.stop();
        arm.drawbridge.stop();
        abort();
    }

    @Override
    public String name() { return "Cascade Stow"; }

    // State 1: start both motors moving down. Runs once.
    public IState lowerBoth()
    {
        return () ->
        {
            arm.cascade.goBackward();
            arm.drawbridge.goBackward();
            return waitForLimits();
        };
    }

    // State 2: runs EVERY loop until done
    public IState waitForLimits()
    {
        return () ->
        {
            boolean cascadeDown = arm.cascade.isAtReverseLimit();
            boolean drawbridgeDown = arm.drawbridge.isAtReverseLimit();

            if (cascadeDown)
                arm.cascade.stop();
            if (drawbridgeDown)
                arm.drawbridge.stop();

            if (cascadeDown && drawbridgeDown)
            {
                return stopBoth("both at limit");
            }
            else if (timer.milliseconds() > TIMEOUT_MS)
            {
                return stopBoth("timeout");
            }
            else
            {
                return waitForLimits();
            }
        };
    }

    // State 3: stop everything, then finish
    public IState stopBoth(String reason)
    {
        return () ->
        {
            arm.cascade.stop();
            arm.drawbridge.stop();
            arm.reportStatus("Cascade Stow: " + reason);
            return null;   // null = the macro is complete
        };
    }
}
