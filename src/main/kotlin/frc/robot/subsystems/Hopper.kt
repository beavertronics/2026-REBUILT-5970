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

object HopperConstants {
    val hopperID = 15
    val MAX_VOLTS = 12.0.volts
}

object Hopper : SubsystemBase() {
    private val hopperMotor = SparkMax(HopperConstants.hopperID, SparkLowLevel.MotorType.kBrushless) // NEO

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, hopperMotor)
    }

    /**
     * A command to run the spindexer at the inputted voltage.
     * @param voltage the voltage to run the spindexer motor at.
     */
    fun RunHopperCommand(voltage: VoltageUnit = 1.0.volts) : Command {
        return run { runHopper(voltage) }
            .finallyDo({ interrupted ->
                runHopper(0.0.volts)
            })
    }

    /**
     * Runs the hopper as the inputted voltage.
     * @param voltage the voltage to run the motor at.
     * Positive is to intake, negative is to outtake.
     *
     * NOTE: Running hopper has a 6:1 gear ratio.
     */
    fun runHopper(voltage: VoltageUnit = 1.0.volts) { hopperMotor.setVoltage(
        -voltage.asVolts.clamp(
            -HopperConstants.MAX_VOLTS.asVolts,
            HopperConstants.MAX_VOLTS.asVolts
        )
    ) }
}