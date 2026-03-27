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
import frc.robot.triggers.Stall

object KickerConstants {
    val feederID = 10
}

object Kicker: SubsystemBase() {
    val kickerMotor = SparkMax(KickerConstants.feederID, SparkLowLevel.MotorType.kBrushed) // 775

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, kickerMotor)
    }

    /**
     * A command to run the shooter feed at the inputted voltage.
     * This commands will reverse the kicker if it is stalled
     * @param stall whether to invert direction when a stall is detected.
     * @param voltage the voltage to run the feeder motor at.
     */
    fun RunKickerCommand(voltage: VoltageUnit = 1.0.volts, stall: Boolean = true) : Command {
        return run {
//            if (Stall.kickerStall.asBoolean && stall) { runKicker(-voltage) }
//            else { runKicker(voltage) }
            runKicker(voltage)
        }
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
    fun runKicker(voltage: VoltageUnit = 1.0.volts) { kickerMotor.set(
        -voltage.asVolts.clamp(
            -Constants.MAX_VOLTS.asVolts,
            Constants.MAX_VOLTS.asVolts
        )
    )}
}