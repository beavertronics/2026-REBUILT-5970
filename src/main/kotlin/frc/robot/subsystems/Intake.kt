package frc.robot.subsystems

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
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
    val MAX_VOLTS = 12.0.volts
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
    fun runIntake(intake: Boolean = true, voltage: VoltageUnit = 1.0.volts) {
        val direction = when (intake) {
            true -> -1.0
            false -> 1.0
        }
        runIntakeMotor.setVoltage( abs(voltage.asVolts.clamp(
            -IntakeConstants.MAX_VOLTS.asVolts,
            IntakeConstants.MAX_VOLTS.asVolts
        )) * direction
        )
    }
}