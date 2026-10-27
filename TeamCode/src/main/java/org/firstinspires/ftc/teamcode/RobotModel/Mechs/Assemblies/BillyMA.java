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

import java.util.EnumSet;

import java.util.Set;

public class BillyMA extends MechAssembly {

    public interface BillyAssemblyStrategy extends IAssemblyStrategy
    {
        void execute(BillyMA mechAssembly, Gamepad gamepad, Set<Part> claimed);
    }

    public enum Part {
        INTAKE,
        PUSHER,
        FLYWHEEL,
        TURRET
    }

    public void requestRapidFire()
    {
        macros.request(BRF);
    }
    public void cancelMacro(String reason) { macros.cancel(reason); }
    public String lastMacroEnd() { return macros.lastEnd(); }
    public Set<Part> macroClaims()
    {
        return macros.claimed();
    }
    public String macroName() { return macros.activeName(); }

    private final SpinnyIntake intake;
    private final PusherServo ballPusher;
    private final DoubleShooter flywheel;
    private final Turret turret;
    private BillyRapidFire BRF = null;
    private final MacroManager<Part> macros = new MacroManager<>();
    protected BillyAssemblyStrategy strategy;

    public BillyMA(HardwareMap hardwareMap) {
        intake = new SpinnyIntake(hardwareMap, "intakeMotor",
                (motor, gamepad) -> {
                    if (gamepad.dpad_up) {
                    motor.setPower(0.75); }
                    else if (gamepad.dpad_down) {
                    motor.setPower(-0.75);
                    } 
                    else {
                        motor.setPower(0);
                    }
                });

        ballPusher = new PusherServo(hardwareMap,
                "ballPusherServo",
                (servoR, gamepad) -> {
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

        strategy = (mechAssembly, gamepad, claimed) -> {

            if (!claimed.contains(Part.INTAKE))
                intake.move(gamepad);
            if (!claimed.contains(Part.PUSHER))
                ballPusher.move(gamepad);
            if (!claimed.contains(Part.FLYWHEEL))
                flywheel.move(gamepad);
            if (!claimed.contains(Part.TURRET))
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
     * results. {@code BillyRapidFire} is also created here so
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
        giveInstructions(gamepad, EnumSet.noneOf(Part.class));
    }

    public void giveInstructions(Gamepad gamepad, Set<Part> robotClaims) {                     

        macros.update(gamepad);     // 1. step the macro (and release it when done)  

        Set<Part> claimed = EnumSet.noneOf(Part.class);
        claimed.addAll(macros.claimed());
        claimed.addAll(robotClaims);
        strategy.execute(this, gamepad, claimed);
    }

    /**
     * Collects telemetry from all four components. Previously omitted
     * {@code intake.update()} entirely (Susan Zuo — Bug #5: "BillyMA.updateTelemetry
     * omits intake — drivers had no visibility into intake state during matches").
     * Never flushes.
     */
    @Override
    public void updateTelemetry() {
        telemetry.addLine("--- BillyMA ---");
        intake.update();
        ballPusher.update();
        flywheel.update();
        turret.update();
    }

}
