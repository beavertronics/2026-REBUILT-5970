package frc.robot.subsystems

import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers
import kotlin.math.abs

object IntakeConstants {
    val leftMoveIntakeID = 12
    val rightMoveIntakeID = 13
    val runIntakeID = 14
    val upperLimitSwitchID = 1
    val lowerLimitSwitchID = 2
}

object Intake : SubsystemBase() {
    val leftIntakeMotor = SparkMax(IntakeConstants.leftMoveIntakeID, SparkLowLevel.MotorType.kBrushed) // 775
     val rightIntakeMotor = SparkMax(IntakeConstants.rightMoveIntakeID, SparkLowLevel.MotorType.kBrushed) // 775
     val runIntakeMotor = SparkMax(IntakeConstants.runIntakeID, SparkLowLevel.MotorType.kBrushed) // 775
     val upperLimitSwitch = DigitalInput(IntakeConstants.upperLimitSwitchID)
     val lowerLimitSwitch = DigitalInput(IntakeConstants.lowerLimitSwitchID)

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kBrake, leftIntakeMotor, rightIntakeMotor) // 20?
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, runIntakeMotor) // 20?
    }

    override fun periodic() {
        SmartDashboard.putBoolean("Subsystems/Intake/lowerPressed", lowerLimitSwitch.get())
        SmartDashboard.putBoolean("Subsystems/Intake/upperPressed", upperLimitSwitch.get())
    }

    /**
     * Runs the intake wheels.
     * @param intake whether to intake our outtake game pieces.
     * @param voltage the voltage to run the motors at.
     * NOTE: Running intake has a 10:1 gear ratio.
     */
    fun runIntake(intake: Boolean = true, voltage: Double = 1.0) {
        val direction = when (intake) {
            true -> -1.0
            false -> 1.0
        }
        runIntakeMotor.setVoltage( abs(voltage) * direction )
    }
}