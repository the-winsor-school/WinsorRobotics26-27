package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.AutonStrategies.BillyRapidFire;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.DoubleShooter;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.SpinnyIntake;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.PusherServo;
import org.firstinspires.ftc.teamcode.RobotModel.Mechs.Components.Turret;

public class BillyMA extends MechAssembly {

    public interface BillyAssemblyStrategy extends IAssemblyStrategy
    {
        void execute(BillyMA mechAssembly, Gamepad gamepad);
    }


    private final SpinnyIntake intake;
    private final PusherServo ballPusher;
    private final DoubleShooter flywheel;
    private final Turret turret;
    private BillyRapidFire BRF = null;

    // TODO: this field shadows the inherited `protected Telemetry telemetry` on
    //  MechAssembly. Two fields with the same name now exist on every BillyMA: this one
    //  gets assigned, the inherited one stays null forever. Any code written in
    //  MechAssembly itself that touches `telemetry` would silently hit the null one.
    //  Delete this declaration - the base class already provides the field.
    private Telemetry telemetry;
    protected BillyAssemblyStrategy strategy;

    // TODO: the `tel` parameter is left over from before the telemetry refactor.
    //  Telemetry now arrives via initializeTelemetry(), which BillyRobot triggers
    //  through initializeSubsystems(). Nothing needs it at construction time - see
    //  CascadeArm and ExampleIntakeAssembly, whose constructors take only a HardwareMap.
    //  Remove the parameter and update BillyRobot's call site.
    public BillyMA(HardwareMap hardwareMap, Telemetry tel) {
        intake = new SpinnyIntake(hardwareMap, "intakeMotor",
                (motor, gamepad) -> {
                    // TODO: dpad_up and dpad_down are two separate if-statements, not one
                    // if/else-if/else chain. Pressing dpad_up alone sets power to 0.75 here,
                    // then the dpad_down check below immediately overwrites it back to 0
                    // (since dpad_down is false, its else branch runs) - forward intake never
                    // actually turns the motor on. Combine these into a single chain.
                    if (gamepad.dpad_up) {
                        motor.setPower(0.75);
                    }
                    if (gamepad.dpad_down) {
                        motor.setPower(-0.75);
                    } else {
                        motor.setPower(0);
                    }
                });

        ballPusher = new PusherServo(hardwareMap,
                "ballPusherServo",
                (servoR, gamepad) -> {
                    servoR.setDirection(Servo.Direction.REVERSE);
                    if (gamepad.x) {
                        servoR.setPosition(0.8);
                    } else {
                        servoR.setPosition(0.0);
                    }
                },
                (servo, telemetry) -> {
                    telemetry.addData("pusher position", servo.getPosition());
                });

        flywheel = new DoubleShooter(hardwareMap, "flywheelMotorF", "flywheelMotorB",
                (motorF, motorB,gamepad) -> {
                    double power = 0.45;
                    if (gamepad.dpad_up) {
                        power += 0.05;
                    }
                    if (gamepad.dpad_down) {
                        power -= 0.05;
                    }

                    if (gamepad.y) {
                        motorF.setPower(power);
                        motorB.setPower(-power);
                    } else {
                        motorF.setPower(0);
                        motorB.setPower(0);
                    }
                }, (motorF, motorB, telemetry) -> {
                    telemetry.addData("power:", motorF.getPower());
                });

        turret = new Turret(hardwareMap, "turretServo",
                (servo, gamepad) -> {
                    if (gamepad.right_bumper)
                    {
                        servo.setPower(-1);
                    }
                    else if (gamepad.left_bumper)
                    {
                        servo.setPower(1);
                    }
                    else {
                        servo.setPower(0);
                    }
                },
                (servo, telemetry) -> {
                    telemetry.addData("turret position", servo.getPower());
                });
        this.telemetry = tel;

        // TODO: everything from here to the end of the constructor is dead - and worse,
        //  it builds objects that are quietly broken. Each component only creates its
        //  own autonomous-behaviors object inside initializeTelemetry(), so at this
        //  point all four getAutonomousBehaviors() calls below return null. That means
        //  this AutonomousBillyMA has four null fields, and the BillyRapidFire built
        //  from it points at nothing. initializeTelemetry() re-creates both correctly a
        //  moment later, which is the only reason this doesn't crash. Delete this block;
        //  initializeTelemetry() is the one place that should build them.
        auton = new AutonomousBillyMA(
                intake.getAutonomousBehaviors(),
                ballPusher.getAutonomousBehaviors(),
                flywheel.getAutonomousBehaviors(),
                turret.getAutonomousBehaviors(),
                telemetry
        );
        BRF = new BillyRapidFire(auton, 3);
        BRF.abort();
        strategy = (mechAssembly, gamepad) -> {
            
            if(gamepad.a && BRF.isComplete())
            {
                BRF.reset(3);
                telemetry.addLine("Start Rapid Fire");
                telemetry.update();
            }
            if(!BRF.isComplete())
            {
                BRF.updateState();
                return;
            }

            intake.move(gamepad);
            ballPusher.move(gamepad);
            flywheel.move(gamepad);
            // This line is a bug! because Turret has nothing to do with BillyRapidFire,
            // AND it is wholly owned by LimelightAutoTarget.
            // TODO: the turret has two uncoordinated owners at once - this manual gamepad
            // strategy AND LimelightAutoTarget (see BillyRobot's update strategy), which
            // runs every loop regardless of what happens here. Whichever one calls
            // turret.move()/setPower() last in a given loop silently wins. Give the turret
            // a real resource-ownership rule (see doc/ControlStrategyExpansionPlan.md and
            // Flint Lessons 5 & 7) so only one strategy commands it at a time.
            turret.move(gamepad);

        };
    }

    public class AutonomousBillyMA extends AutonomousMechBehaviors {
        public final SpinnyIntake.AutonomousIntakeBehaviors autonIntake;
        public final PusherServo.AutonomousBallPusherBehaviors autonBallPusher;
        public final DoubleShooter.AutonomousShooterBehavior autonFlywheel;
        public final Turret.AutonomousTurretBehaviors autonTurret;

        public AutonomousBillyMA(
                SpinnyIntake.AutonomousIntakeBehaviors autonIntake,
                PusherServo.AutonomousBallPusherBehaviors autonBallPusher,
                DoubleShooter.AutonomousShooterBehavior autonFlywheel,
                Turret.AutonomousTurretBehaviors autonTurret,
                Telemetry telemetry) {
            super(telemetry);
            this.autonIntake = autonIntake;
            this.autonBallPusher = autonBallPusher;
            this.autonFlywheel = autonFlywheel;
            this.autonTurret = autonTurret;
        }
    }

    private AutonomousBillyMA auton;

    /**
     * Propagates telemetry to all four components so each can lazily create its own
     * autonomous behavior object, then assembles {@code AutonomousBillyMA} from the
     * results. {@code BillyRapidFire} is also created here and immediately aborted so
     * it is never null when {@link #giveInstructions} first runs (Susan Zuo —
     * two-phase initialization pattern; previously the constructor accepted a
     * {@code Telemetry} arg, coupling construction to telemetry lifetime).
     */
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
        intake.initializeTelemetry(telemetry);
        ballPusher.initializeTelemetry(telemetry);
        flywheel.initializeTelemetry(telemetry);
        turret.initializeTelemetry(telemetry);
        auton = new AutonomousBillyMA(
                intake.getAutonomousBehaviors(),
                ballPusher.getAutonomousBehaviors(),
                flywheel.getAutonomousBehaviors(),
                turret.getAutonomousBehaviors(),
                telemetry);
        BRF = new BillyRapidFire(auton, 3);
        BRF.abort();
    }

    @Override
    public AutonomousBillyMA getAutonomousBehaviors() {
        return auton;
    }

    /**
     * Drives all four components and advances the BillyRapidFire state machine each
     * loop. Does NOT call {@code telemetry.update()} — flushing is the exclusive
     * responsibility of {@code Robot.updateTelemetry()} (Susan Zuo — Bug #7:
     * "mid-cycle {@code telemetry.update()} inside giveInstructions caused the
     * driver-station display to flicker and showed partial data from the previous
     * loop iteration").
     */
    @Override
    public void giveInstructions(Gamepad gamepad) {
       strategy.execute(this, gamepad);
    }

    /**
     * Collects telemetry from all four components. Previously omitted
     * {@code intake.update()} entirely (Susan Zuo — Bug #5: "BillyMA.updateTelemetry
     * omits intake — drivers had no visibility into intake state during matches").
     * Never flushes.
     */
    @Override
    public void updateTelemetry() {
        // TODO: this method never visits any component, so none of the four
        // mechanisms (intake, ballPusher, flywheel, turret) ever report telemetry to
        // the driver station. Call each component's own telemetry-reporting behavior
        // here, the same way giveInstructions() above visits every component for
        // gamepad input (see Flint Lesson 4). CascadeArm.updateTelemetry() and
        // ExampleIntakeAssembly.updateTelemetry() both show the shape you want.
    }
}
