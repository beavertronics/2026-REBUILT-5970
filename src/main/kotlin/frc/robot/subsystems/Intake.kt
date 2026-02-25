package frc.robot.subsystems

import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj.DigitalInput
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
    private val leftIntakeMotor = SparkMax(IntakeConstants.leftMoveIntakeID, SparkLowLevel.MotorType.kBrushed) // 775
    private val rightIntakeMotor = SparkMax(IntakeConstants.rightMoveIntakeID, SparkLowLevel.MotorType.kBrushed) // 775
    private val runIntakeMotor = SparkMax(IntakeConstants.runIntakeID, SparkLowLevel.MotorType.kBrushed) // 775
    private val upperLimitSwitch = DigitalInput(IntakeConstants.upperLimitSwitchID)
    private val lowerLimitSwitch = DigitalInput(IntakeConstants.lowerLimitSwitchID)

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kBrake, leftIntakeMotor, rightIntakeMotor)
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, runIntakeMotor)
    }

    /**
     * Moves the intake to the up or down position.
     * @param up whether the intake should be moved up or not.
     * @param voltage the voltage to run the motor at.
     *
     * NOTE: Moving intake has a 35:1 gear ratio.
     */
    fun moveIntake(up: Boolean = true, voltage: Double = 1.0) {
        val direction = when (up) { // TODO figure out sign
            true -> 1.0
            false -> -1.0
        }
        val limitSwitch = when (up) {
            true -> upperLimitSwitch
            false -> lowerLimitSwitch
        }
        while (!limitSwitch.get()) {
            leftIntakeMotor.setVoltage(abs(voltage) * direction)
            rightIntakeMotor.setVoltage(abs(voltage) * direction)
        }
        leftIntakeMotor.stopMotor()
        rightIntakeMotor.stopMotor()
        return
    }

    /**
     * Runs the intake wheels.
     * @param intake whether to intake our outtake game pieces.
     * @param voltage the voltage to run the motors at.
     * NOTE: Running intake has a 10:1 gear ratio.
     */
    fun runIntake(intake: Boolean = true, voltage: Double = 1.0) {
        val direction = when (intake) { // TODO figure out sign
            true -> 1.0
            false -> -1.0
        }
        runIntakeMotor.setVoltage( abs(voltage) * direction )
    }
}