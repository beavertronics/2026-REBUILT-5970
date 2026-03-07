package frc.robot.subsystems

import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers

object HopperConstants {
    val hopperID = 15
}

object Hopper : SubsystemBase() {
    private val hopperMotor = SparkMax(HopperConstants.hopperID, SparkLowLevel.MotorType.kBrushless) // NEO

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, hopperMotor)
    }

    /**
     * Runs the hopper as the inputted voltage.
     * @param voltage the voltage to run the motor at.
     *
     * NOTE: Running hopper has a 6:1 gear ratio.
     */
    fun runHopper(voltage: Double = 1.0) { hopperMotor.setVoltage(-voltage) }
}