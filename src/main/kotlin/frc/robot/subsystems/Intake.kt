package frc.robot.subsystems

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import com.revrobotics.PersistMode
import com.revrobotics.ResetMode
import com.revrobotics.spark.SparkFlex
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkFlexConfig
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.robot.Constants

object IntakeConstants {
    val runIntakeID = 61
}

object Intake : SubsystemBase() {
     val intakeMotor = SparkFlex(IntakeConstants.runIntakeID, SparkLowLevel.MotorType.kBrushless) // NEO Vortex

    init {
        intakeMotor.configure(
            SparkFlexConfig()
                .smartCurrentLimit(30)
                .idleMode(SparkBaseConfig.IdleMode.kCoast),
            ResetMode.kNoResetSafeParameters,
            PersistMode.kNoPersistParameters
        )
    }

    /**
     * A command to run the intake at the inputted voltage.
     * @param stall whether to invert the direction when a stall is detected.
     * @param voltage the voltage to run the intake motor at.
     */
    fun RunIntakeCommand(voltage: VoltageUnit = 1.0.volts, stall: Boolean = true) : Command {
        return run {
            runIntake(voltage)
        }
            .finallyDo({ interrupted ->
                runIntake(0.0.volts)
            })
    }

    /**
     * Runs the intake wheels.
     * @param voltage the voltage to run the motors at.
     * NOTE: Running intake has a 10:1 gear ratio.
     */
    fun runIntake(voltage: VoltageUnit = 1.0.volts) { // todo figure out sign
        intakeMotor.setVoltage( -voltage.asVolts.clamp(
            -Constants.MAX_VOLTS.asVolts,
            Constants.MAX_VOLTS.asVolts
        ))
    }
}