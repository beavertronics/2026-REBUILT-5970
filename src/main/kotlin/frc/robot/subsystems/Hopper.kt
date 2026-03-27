package frc.robot.subsystems

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.ParallelRaceGroup
import edu.wpi.first.wpilibj2.command.SubsystemBase
import edu.wpi.first.wpilibj2.command.WaitCommand
import frc.engine.utils.initMotorControllers
import frc.robot.Constants
import frc.robot.triggers.Stall

object HopperConstants {
    val hopperID = 15
}

object Hopper : SubsystemBase() {
    val hopperMotor = SparkMax(HopperConstants.hopperID, SparkLowLevel.MotorType.kBrushless) // NEO

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, hopperMotor)
    }

    /**
     * A command to run the spindexer at the inputted voltage.
     * @param stall whether to invert the direction when a stall is detected.
     * @param voltage the voltage to run the spindexer motor at.
     */
    fun RunHopperCommand(voltage: VoltageUnit = 1.0.volts, stall: Boolean = true) : Command {
        return run {
//            if (Stall.hopperStall.asBoolean && stall) { runHopper(-voltage) }
//            else { runHopper(voltage) }
            runHopper(voltage)
        }
            .finallyDo({ interrupted ->
                runHopper(0.0.volts)
            })
    }

    /**
     * Agitates the hopper by running it backwwards for 5 seconds, and then forwards for 1 second.
     * @oaram voltage the voltage to agitate at.
     */
    fun AgitateHopperCommand(volts: VoltageUnit = 3.0.volts) : Command {
        return ParallelRaceGroup(
            RunHopperCommand(volts),
            WaitCommand(5.0)
        )
            .andThen(
                ParallelRaceGroup(
                    RunHopperCommand(-volts),
                    WaitCommand(2.0)
                )
            )
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
            -Constants.MAX_VOLTS.asVolts,
            Constants.MAX_VOLTS.asVolts
        )
    ) }
}