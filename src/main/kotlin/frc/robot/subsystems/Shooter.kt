package frc.robot.subsystems

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.AngularVelocity
import beaverlib.utils.Units.Angular.RPM
import beaverlib.utils.Units.Angular.asRPM
import beaverlib.utils.Units.Angular.asRotationsPerSecond
import beaverlib.utils.Units.Angular.rotationsPerSecond
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.amps
import beaverlib.utils.Units.Electrical.volts
import com.ctre.phoenix6.configs.CurrentLimitsConfigs
import com.ctre.phoenix6.configs.MotorOutputConfigs
import com.ctre.phoenix6.configs.TalonFXConfiguration
import com.ctre.phoenix6.controls.MotionMagicVelocityVoltage
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.NeutralModeValue
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.robot.Constants

object ShooterConstants {
//    val shooterID = 9
    val krakenID = 53 // todo kraken ID
    val MAX_RPM_DIFF = 500.0.RPM
    val RPM_LIMIT = 6250.0.RPM // for a KrakenX60
    val SUPPLY_CURRENT_LIMIT = 35.0.amps
}

/**
 * NOTE: The usage of the JVM name stuff here is unknown, but I had to because it got angry.
 * So therefore, "BiteMe!"
 */
object Shooter : SubsystemBase() {
//    val shooterMotor = SparkMax(ShooterConstants.shooterID, SparkLowLevel.MotorType.kBrushless) // NEO
    val krakenShooter = TalonFX(ShooterConstants.krakenID) // Kraken X60
//    val PIDConstants = PIDConstants(0.0, 0.0, 0.0) // todo tune
//    val feedForwardConstants = SimpleMotorFeedForwardConstants(0.3, 2.0, 0.0) // todo tune
//    val pidff = PidFF(PIDConstants, feedForwardConstants)

    /**
     * This is the current RPM for the shooter flywheel, in RPM
     */
    var currentRPM: AngularVelocity = 0.0.RPM

    /**
     * This is the target RPM for the shooter flywheel, in RPM
     */
    @get:JvmName("BiteMe!!")
    var targetRPM: AngularVelocity = 0.0.RPM

    init {
        setTargetRPM(0.0.RPM)

//        initMotorControllers(40, SparkBaseConfig.IdleMode.kCoast, shooterMotor)

        // configure base features of kraken
        val config = TalonFXConfiguration()
            .withCurrentLimits(
                CurrentLimitsConfigs()
                    .withSupplyCurrentLimit(ShooterConstants.SUPPLY_CURRENT_LIMIT.asAmps) // todo is this the right current limit?
                    .withSupplyCurrentLimitEnable(true)
            )
            .withMotorOutput(
                MotorOutputConfigs()
                    .withInverted(InvertedValue.Clockwise_Positive) // todo which one?
                    .withNeutralMode(NeutralModeValue.Coast)
            )
            .withMotionMagic(
                TalonFXConfiguration().MotionMagic
            )

        // configure PID
        val slots = config.Slot0
        slots.kS = 0.25
        slots.kV = 0.119047
        slots.kA = 0.666
        slots.kP = 0.11
        slots.kI = 0.0
        slots.kD = 0.0
        val motionMagic = config.MotionMagic
        motionMagic.withMotionMagicAcceleration(200.0)
        motionMagic.withMotionMagicJerk(2000.0)

        // apply config
        krakenShooter.configurator.apply(config)
    }

    /**
     * A command to run the shooter at the inputted voltage.
     * @param voltage the voltage to run the shooter motor at.
     */
    fun ShootVoltageCommand(voltage: VoltageUnit = 1.0.volts) : Command {
        return run { runShooter(voltage) }
            .finallyDo({ interrupted ->
                runShooter(0.0.volts)
            })
    }

    /**
     * A command to run the shooter at target RPM. Whether the shooter flywheel is up to speed
     * can be checked by its trigger.
     * @see targetRPM
     * @see frc.robot.triggers.General.rpmTrigger
     */
    fun ShootRPMCommand(rpm: AngularVelocity = 0.0.RPM) : Command { // todo test
        return run {
//            pidff.setpoint = targetRPM.asRPM
            setTargetRPM(
                rpm.asRPM.clamp(
                -ShooterConstants.RPM_LIMIT.asRPM,
                ShooterConstants.RPM_LIMIT.asRPM
            ).RPM)
//            val calculatedAll = pidff.calculate(
//                currentRPM.asRPM
//            )
//            println("Calc " + calculatedAll)
//            runShooter((calculatedAll * Constants.MAX_VOLTS.asVolts).volts)
            krakenShooter.setControl(
                MotionMagicVelocityVoltage(rpm.asRotationsPerSecond)
            )
        }
            .repeatedly()
            .beforeStarting(
                {
                    run {
                        setTargetRPM(
                            rpm.asRPM.clamp(
                                -ShooterConstants.RPM_LIMIT.asRPM,
                                ShooterConstants.RPM_LIMIT.asRPM
                            ).RPM
                        )
//                        pidff.setpoint = targetRPM.asRPM
                    }
                }
            )
            .finallyDo({ interrupted ->
                runShooter(0.0.volts)
            })
    }

    override fun periodic() {
        // get current RPM
//        currentRPM = (shooterMotor.encoder.velocity * -1.0).RPM
        currentRPM = (krakenShooter.velocity.valueAsDouble.rotationsPerSecond.asRPM.RPM) // todo need to invert?
        // get current RPM from dashboard
        targetRPM = SmartDashboard.getNumber("Subsystems/Shooter/Target RPM", 0.0).RPM
        // put data on dashboard
        SmartDashboard.putNumber("Subsystems/Shooter/Shooter RPM", currentRPM.asRPM)
//        SmartDashboard.putNumber("Subsystems/Shooter/Target RPM", targetRPM.asRPM)
    }

    /**
     * Sets the target RPM for the shooter flywheel, in RPM.
     */
    @JvmName("BiteMe!!!!")
    fun setTargetRPM(target: AngularVelocity) {
        SmartDashboard.putNumber("Subsystems/Shooter/Target RPM", target.asRPM)
    }

    /**
     * Runs the shooter flywheel with a voltage.
     * @param voltage the voltage to run the motor at.
     * Positive is to shoot, negative is to reverse.
     */
    fun runShooter(voltage: VoltageUnit = 1.0.volts) {
//        shooterMotor.setVoltage(
        krakenShooter.setVoltage(
        voltage.asVolts.clamp( // REMEMBER TO INVERT VOLTAGE FOR THE NEO BUT NOT FOR KRAKEN!
            -Constants.MAX_VOLTS.asVolts,
            Constants.MAX_VOLTS.asVolts
        ))
    }

//    fun shooterSysID(): Array<Command> {
//        val motor = BeaverSysIDMotor("Shooter", shooterMotor)
//        val routine = BeaverSysIDRoutine(Shooter, )
//        return arrayOf(
//            routine.sysIdDynamic(SysIdRoutine.Direction.kReverse),
//            routine.sysIdDynamic(SysIdRoutine.Direction.kForward),
//            routine.sysIdQuasistatic(SysIdRoutine.Direction.kReverse),
//            routine.sysIdQuasistatic(SysIdRoutine.Direction.kForward)
//        )
//    }
}