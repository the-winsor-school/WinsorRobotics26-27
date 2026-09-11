package org.firstinspires.ftc.teamcode.AutonStrategies;


import org.firstinspires.ftc.teamcode.Extensions.IState;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies.BillyMA;


/**
 * State machine that fires a fixed number of balls by sequencing the shooter,
 * ball pusher, and retract states. Previously accepted a raw {@code Telemetry}
 * argument and called {@code telemetry.update()} mid-state, which produced
 * flickering on the driver station and could flush incomplete data (Susan Zuo —
 * Bug #2: "mid-cycle {@code telemetry.update()} in state machines"). All
 * reporting now goes through {@code mechAssembly.reportStatus/reportData} so the
 * single-flush rule in {@code Robot.updateTelemetry()} is respected.
 */
public class BillyRapidFire extends StateMachine
{
    private final BillyMA.AutonomousBillyMA mechAssembly;
    private int ballCount;

    /**
     * @param ma         the live autonomous behavior object — not constructed here,
     *                   so no raw {@code Telemetry} reference is needed (Susan Zuo
     *                   — Bug #6: "autonomous strategies held raw telemetry
     *                   references, bypassing the object model").
     * @param ballCount  initial number of balls to fire
     */
    public BillyRapidFire(BillyMA.AutonomousBillyMA ma, int ballCount)
    {
        mechAssembly = ma;
        reset(ballCount);
    }

    public void reset(int ballCount)
    {
        this.ballCount = ballCount;
        currentState = startShooter();
    }


    public IState startShooter() {
        mechAssembly.reportStatus("startShooter");
        return
                doAndWait(
                        () -> mechAssembly
                                .autonFlywheel
                                .setPower(0.6),
                        2200,
                        fire()
                );


    }
    public IState fire()
    {
        mechAssembly.reportStatus("fire");
        return
                doAndWait(
                        mechAssembly
                                .autonBallPusher
                                ::pushBalls,
                        1000,
                        retract()
                );
    }
    public IState retract()
    {
        mechAssembly.reportStatus("retract");
        return
                doAndWait(
                        mechAssembly
                                .autonBallPusher
                                ::retractPusher,
                        1000,
                        --ballCount > 0
                                ? fire()
                                : stopShooter()


                );
    }
    public IState stopShooter()
    {
        mechAssembly.reportStatus("stopShooter");
        return() ->
        {
            mechAssembly
                    .autonFlywheel
                    .StopShoot();
            return null;
        };
    }

}
