package frc.robot.subsystems

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers
import frc.robot.Constants

object KickerConstants {
    val feederID = 10
}

object Kicker: SubsystemBase() {
    val feedMotor = SparkMax(KickerConstants.feederID, SparkLowLevel.MotorType.kBrushed) // 775

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, feedMotor)
    }

    /**
     * A command to run the shooter feed at the inputted voltage.
     * @param voltage the voltage to run the feeder motor at.
     */
    fun RunKickerCommand(voltage: VoltageUnit = 1.0.volts) : Command {
        return run { runKicker(voltage) }
            .finallyDo({ interrupted ->
                runKicker(0.0.volts)
            })
    }

    /**
     * Runs the feed motor for the shooter.
     * @param voltage the voltage to run the motor at.
     * Positive is to intake, negative is to outtake.
     *
     * NOTE: Running feeder has a 10:1 gear ratio.
     */
    fun runKicker(voltage: VoltageUnit = 1.0.volts) { feedMotor.set(
        -voltage.asVolts.clamp(
            -Constants.MAX_VOLTS.asVolts,
            Constants.MAX_VOLTS.asVolts
        )
    )}
}