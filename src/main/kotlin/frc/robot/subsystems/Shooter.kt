package frc.robot.subsystems

import beaverlib.controls.PIDConstants
import beaverlib.controls.PidFF
import beaverlib.controls.SimpleMotorFeedForwardConstants
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.AngularVelocity
import beaverlib.utils.Units.Angular.RPM
import beaverlib.utils.Units.Angular.asRPM
import beaverlib.utils.Units.Angular.rotationsPerSecond
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import com.ctre.phoenix6.hardware.TalonFX
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.robot.Constants

object ShooterConstants {
//    val shooterID = 9
    val krakenID = 0 // todo kraken ID
    val MAX_RPM_DIFF = 5.0.RPM
    val RPM_LIMIT = 7000.0.RPM // todo figure out
}

/**
 * NOTE: The usage of the JVM name stuff here is unknown, but I had to because it got angry.
 * So therefore, "BiteMe!"
 */
object Shooter : SubsystemBase() {
//    val shooterMotor = SparkMax(ShooterConstants.shooterID, SparkLowLevel.MotorType.kBrushless) // NEO
    val krakenShooter = TalonFX(ShooterConstants.krakenID) // Kraken X60

    val PIDConstants = PIDConstants(0.0, 0.0, 0.0) // todo tune
    val feedForwardConstants = SimpleMotorFeedForwardConstants(0.0, 0.0, 0.0) // todo tune
    val pidff = PidFF(PIDConstants, feedForwardConstants)

    /**
     * This is the current RPM for the shooter flywheel, in RPM
     */
    var currentRPM: AngularVelocity = 0.0.RPM

    /**
     * This is the target RPM for the shooter flywheel, in RPM
     */
    @get:JvmName("BiteMe!!")
    var targetRPM: AngularVelocity = 0.0.RPM

    init { // todo configure kraken here
//        initMotorControllers(40, SparkBaseConfig.IdleMode.kCoast, shooterMotor)
        ""
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
            val calculatedAll = pidff.calculate(currentRPM.asRPM)
//            val calculatedP023ID = pidff.pid.calculate(currentRPM.asRPM)
//            println("Running shooter at " + calculatedAll)
            runShooter((calculatedAll * Constants.MAX_VOLTS.asVolts).volts)
        }
            .repeatedly()
            .beforeStarting(
                {
                    run {
                        setTargetRPM(rpm)
                        pidff.setpoint = targetRPM.asRPM
                    }
                }
            )
            .finallyDo({ interrupted ->
                runShooter(0.0.volts)
            })
    }

    override fun periodic() {
//        currentRPM = (shooterMotor.encoder.velocity * -1.0).RPM
        currentRPM = (krakenShooter.velocity.valueAsDouble.rotationsPerSecond.asRPM.RPM) // todo need to invert?
        // put data on dashboard
        SmartDashboard.putNumber("Subsystems/Shooter/Shooter RPM", currentRPM.asRPM)
        SmartDashboard.putNumber("Subsystems/Shooter/Target RPM", targetRPM.asRPM)
    }

    /**
     * Sets the target RPM for the shooter flywheel, in RPM.
     */
    @JvmName("BiteMe!!!!")
    fun setTargetRPM(target: AngularVelocity) { targetRPM = target }

    /**
     * Runs the shooter flywheel with a voltage.
     * @param voltage the voltage to run the motor at.
     * Positive is to shoot, negative is to reverse.
     */
    fun runShooter(voltage: VoltageUnit = 1.0.volts) {
//        shooterMotor.setVoltage(
        krakenShooter.setVoltage(
        -voltage.asVolts.clamp(
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