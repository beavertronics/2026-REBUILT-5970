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
import kotlin.math.abs

object IntakeConstants {
    val runIntakeID = 14
}

object Intake : SubsystemBase() {
     val runIntakeMotor = SparkMax(IntakeConstants.runIntakeID, SparkLowLevel.MotorType.kBrushed) // 775

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, runIntakeMotor) // 20?
    }

    /**
     * A command to run the intake at the inputted voltage.
     * @param intake whether to intake or not.
     * @param voltage the voltage to run the intake motor at.
     */
    fun RunIntakeCommand(intake: Boolean = true, voltage: VoltageUnit = 1.0.volts) : Command {
        return run { runIntake(intake, voltage) }
            .finallyDo({ interrupted ->
                runIntake(intake, 0.0.volts)
            })
    }

    /**
     * Runs the intake wheels.
     * @param intake whether to intake our outtake game pieces.
     * @param voltage the voltage to run the motors at.
     * NOTE: Running intake has a 10:1 gear ratio.
     */
    fun runIntake(intake: Boolean = true, voltage: VoltageUnit = 1.0.volts) {
        val direction = when (intake) {
            true -> -1.0
            false -> 1.0
        }
        runIntakeMotor.setVoltage( abs(voltage.asVolts.clamp(
            -Constants.MAX_VOLTS.asVolts,
            Constants.MAX_VOLTS.asVolts
        )) * direction
        )
    }
}