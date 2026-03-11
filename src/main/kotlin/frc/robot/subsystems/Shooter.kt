package frc.robot.subsystems

import beaverlib.controls.PIDConstants
import beaverlib.controls.PidFF
import beaverlib.controls.SimpleMotorFeedForwardConstants
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.AngularVelocity
import beaverlib.utils.Units.Angular.RPM
import beaverlib.utils.Units.Angular.asRPM
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers

object ShooterConstants {
    val shooterID = 9
    val MIN_RPM = 3000.0.RPM // todo
    val RPM_LIMIT = 5000.0.RPM
    val MAX_VOLTS = 12.0.volts
}

/**
 * NOTE: The usage of the JVM name stuff here is unknown, but I had to because it got angry.
 * So therefore, "BiteMe!"
 */
object Shooter : SubsystemBase() {
    val shooterMotor = SparkMax(ShooterConstants.shooterID, SparkLowLevel.MotorType.kBrushless) // NEO

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

    init {
        // configure motors
        initMotorControllers(40, SparkBaseConfig.IdleMode.kCoast, shooterMotor)
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
     * A command to run the shooter at target RPM.
     * @see targetRPM
     */
    fun ShootRPMCommand() : Command {
        return run {
            runShooter((pidff.calculate(currentRPM.asRPM) * ShooterConstants.MAX_VOLTS.asVolts).volts)
        }
            .finallyDo({ interrupted ->
                runShooter(0.0.volts)
            })
    }

    /**
     * A command to set the target RPM of the shooter.
     * @param rpm the target RPM to run the shooter motor at.
     */
    fun SetTargetRPMCommand(rpm: AngularVelocity = 0.0.RPM) : Command {
        return run { setTargetRPM(
            rpm.asRPM.clamp(-ShooterConstants.RPM_LIMIT.asRPM, ShooterConstants.RPM_LIMIT.asRPM).RPM) }
    }

    override fun periodic() {
        pidff.setpoint = targetRPM.asRPM
        currentRPM = (shooterMotor.encoder.velocity * -1.0).RPM
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
    fun runShooter(voltage: VoltageUnit = 1.0.volts) { shooterMotor.setVoltage(
        -voltage.asVolts.clamp(
            -ShooterConstants.MAX_VOLTS.asVolts,
            ShooterConstants.MAX_VOLTS.asVolts
        )) }
}