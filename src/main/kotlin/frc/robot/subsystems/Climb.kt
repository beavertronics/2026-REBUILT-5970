package frc.robot.subsystems

import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj.DigitalInput
import frc.engine.utils.initMotorControllers

object ClimbConstants {
    val climbMotorID = 16
    val lowerLimitSwitchID = 3
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
    fun runClimb(voltage: Double = 0.0) { climbMotor.setVoltage(voltage) }
}