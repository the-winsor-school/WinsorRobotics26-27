package org.firstinspires.ftc.teamcode.OpModes;


import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.AutonStrategies.IAutonStrategy;
import org.firstinspires.ftc.teamcode.Extensions.IState;
import org.firstinspires.ftc.teamcode.Extensions.LimelightExtensions;
import org.firstinspires.ftc.teamcode.RobotModel.Robots.BillyRobot;

@Autonomous(name = "Example State Machine Auton", group = "Example")
public class ExampleStateMachineAuton
        extends LinearOpMode
        implements IAutonStrategy {

    // TODO: this season's target AprilTag ID goes here (last season's ID was removed
    //  during the 2026-27 migration). Same for the three IDs in findTags() below.
    private static final int TARGET_TAG_ID = -1;

    private BillyRobot robot;

    @Override
    public void runOpMode() throws InterruptedException
    {
        robot = new BillyRobot(hardwareMap, telemetry, TARGET_TAG_ID);
        telemetry.addLine("auton ready");
        telemetry.update();
        waitForStart();
        execute();
    }

    IState currentState;

    @Override
    public void execute()
    {
        currentState = findTags();
        while (opModeIsActive() && currentState != null){
            currentState = currentState.execute();
        }
    }

    private ElapsedTime elapsedTime;

    private IState doAndWait(Runnable action, int millis, IState nextState) {
        return () -> //lambda expression
        {
            if (elapsedTime == null)
            {
                elapsedTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
                action.run();
            }

            if (elapsedTime.milliseconds() < millis)
                return doAndWait(action, millis, nextState);
            elapsedTime = null;
            return nextState;
        };
    }
    private IState findTags()
    {
        return()->
        {
            LLResultTypes.FiducialResult tag =
                    LimelightExtensions.tryFindOneOf(
                            robot.limelight,
                            21, 22, 23);

            if(tag == null)
            {
                telemetry.addLine(
                        "Tags 21, 22, 23 not found");
                return findTags();
            }
            if(tag.getFiducialId() == 21)
            {
                return driveLeft();
            }
            if(tag.getFiducialId() == 22)
            {
                return driveForward();
            }
            if(tag.getFiducialId() == 23)
            {
                return driveRight();
            }
            return findTags();
        };
    }

    private IState driveLeft() {
        return doAndWait(
                () -> robot.getAutonomousRobot()
                        .driveTrain
                        .drive(-0.5, 0, 0), 2500, spinInPlace());
    }
    private IState driveForward()
    {
        return doAndWait(
                () -> robot.getAutonomousRobot()
                        .driveTrain
                        .drive(0, 0.5, 0), 2500, spinInPlace());
    }
    private IState driveRight()
    {
        return doAndWait(
                () -> robot.getAutonomousRobot()
                        .driveTrain
                        .drive(0.5,0, 0), 2500, spinInPlace());
    }
    private IState spinInPlace()
    {
        return doAndWait(
                () -> robot.getAutonomousRobot()
                        .driveTrain
                        .drive(0,0, 0.5), 2500, stopDriving());
    }

    private IState stopDriving()
    {
        return doAndWait(
                () -> robot.getAutonomousRobot()
                        .driveTrain
                        .drive(0, 0, 0), 1, startShooter()
        );
    }
    private IState startShooter()
    {
        return doAndWait(
                () -> robot.getAutonomousRobot()
                        .mechAssembly
                        .autonFlywheel
                        .setPower(0.6), 2200, pushBall());
    }
    private IState pushBall(){
        return doAndWait(
                () -> robot.getAutonomousRobot()
                        .mechAssembly
                        .autonBallPusher
                        .pushBalls(), 1000, reset());
    }
    private IState reset() {
        return doAndWait(
                () -> robot.getAutonomousRobot()
                        .mechAssembly
                        .autonBallPusher
                        .retractPusher(), 1500, null);
    }
}
