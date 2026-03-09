package frc.robot.subsystems

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj.DigitalInput
import frc.engine.utils.initMotorControllers

object ClimbConstants {
    val climbMotorID = 16
    val lowerLimitSwitchID = 3
    val MAX_VOLTS = 12.0.volts
}

class Climb {
    private val climbMotor = SparkMax(ClimbConstants.climbMotorID, SparkLowLevel.MotorType.kBrushless) // NEO
    private val lowerLimitSwitch = DigitalInput(ClimbConstants.lowerLimitSwitchID) // todo utilize this?

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kBrake, climbMotor)
    }

    /**
     * Runs the climb motor.
     * @param voltage the voltage to run the climb motor at.
     * - Positive to climb, negative to descend. // todo figure out if true
     */
    fun runClimb(voltage: VoltageUnit = 1.0.volts) { climbMotor.setVoltage(
        voltage.asVolts.clamp(
            -ClimbConstants.MAX_VOLTS.asVolts,
            ClimbConstants.MAX_VOLTS.asVolts
        )
    ) }
}