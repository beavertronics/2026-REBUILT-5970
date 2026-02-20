package frc.robot.subsystems

import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers

object ShooterConstants {
    val hoodID = 0 // todo
    val shooterID = 0 // todo
    val feederID = 0 // todo
}

object Shooter : SubsystemBase() {
    private val hoodMotor = SparkMax(ShooterConstants.hoodID, SparkLowLevel.MotorType.kBrushed) // 775
    private val shooterMotor = SparkMax(ShooterConstants.shooterID, SparkLowLevel.MotorType.kBrushless) // NEO
    private val feedMotor = SparkMax(ShooterConstants.feederID, SparkLowLevel.MotorType.kBrushed) // 775

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, hoodMotor, shooterMotor, feedMotor)
    }

    /**
     * Runs the shooter flywheel.
     * @param voltage the voltage to run the motor at.
     */
    fun runShooter(voltage: Double = 1.0) { shooterMotor.setVoltage(voltage) } // todo figure out sign

    /**
     * Runs the feed motor for the shooter.
     * @param voltage the voltage to run the motor at.
     */
    fun runFeed(voltage: Double = 1.0) { feedMotor.set(voltage) } // todo figure out sign
}