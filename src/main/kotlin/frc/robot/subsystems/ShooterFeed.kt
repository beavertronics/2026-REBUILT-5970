package frc.robot.subsystems

import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers
import kotlin.text.set

object ShooterFeedConstants {
    val feederID = 10
}

object ShooterFeed : SubsystemBase() {

    val feedMotor = SparkMax(ShooterFeedConstants.feederID, SparkLowLevel.MotorType.kBrushed) // 775

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, feedMotor) // 20?
    }

    /**
     * Runs the feed motor for the shooter.
     * @param voltage the voltage to run the motor at.
     * Positive is to intake, negative is to outtake.
     *
     * NOTE: Running feeder has a 10:1 gear ratio.
     */
    fun runFeed(voltage: Double = 1.0) { feedMotor.set(-voltage); return }
}