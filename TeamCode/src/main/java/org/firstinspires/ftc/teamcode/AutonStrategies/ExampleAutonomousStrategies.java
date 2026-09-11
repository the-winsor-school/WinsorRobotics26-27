package org.firstinspires.ftc.teamcode.AutonStrategies;

import org.firstinspires.ftc.teamcode.Extensions.ThreadExtensions;
import org.firstinspires.ftc.teamcode.RobotModel.Robots.Wildbots2025;

public class ExampleAutonomousStrategies
{
    /** make it dance! */
    public static IAutonStrategy MecanumAutonDance(Wildbots2025.AutonomousMecanumRobot robot)
    {
        return () ->
        {
            robot.driveTrain.drive(0, 1, 0);
            robot.mechAssembly.drawbridge.goForward();
            ThreadExtensions.TrySleep(500);
            robot.driveTrain.drive(0.5, 0, 0.5);
            ThreadExtensions.TrySleep(500);
            robot.mechAssembly.drawbridge.stop();
            robot.mechAssembly.cascade.goForward();
            ThreadExtensions.TrySleep(1000);
            robot.driveTrain.drive(0, 0, 0);
            robot.mechAssembly.cascade.stop();

            //robot.driveTrain.spinInPlace();
            ThreadExtensions.TrySleep(30000);
        };
    }
}
