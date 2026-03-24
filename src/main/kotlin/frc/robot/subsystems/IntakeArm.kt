package frc.robot.subsystems

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers
import frc.robot.Constants
import frc.robot.subsystems.general.HedgieHelmet
import frc.robot.triggers.Stall
import kotlin.math.sign

object IntakeArmConstants {
    val leftMoveIntakeID = 12
    val rightMoveIntakeID = 13
    val upperLimitSwitchID = 1
    val lowerLimitSwitchID = 2
}

object IntakeArm : SubsystemBase() {
    val leftIntakeMotor = SparkMax(IntakeArmConstants.leftMoveIntakeID, SparkLowLevel.MotorType.kBrushed) // 775
    val rightIntakeMotor = SparkMax(IntakeArmConstants.rightMoveIntakeID, SparkLowLevel.MotorType.kBrushed) // 775
    val upperLimitSwitch = DigitalInput(IntakeArmConstants.upperLimitSwitchID)
    val lowerLimitSwitch = DigitalInput(IntakeArmConstants.lowerLimitSwitchID)

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kBrake, leftIntakeMotor, rightIntakeMotor) // 20?
    }

    /**
     * A command to move the intake arm up or down.
     * @param stall whether to invert the direction when a stall is detected.
     * @param voltage the voltage to move the arm at.
     */
    fun MoveIntakeCommand(voltage: VoltageUnit = 1.0.volts, stall: Boolean = true) : Command {
        return run {
            if (Stall.intakeArmStall.asBoolean && stall) { runIntakeMotors(-voltage) }
            else { runIntakeMotors(voltage) }
            }
            .onlyIf(Stall.intakeArmStall.negate()) // only move the intake if it is not stalled
            .until {
                if (voltage.asVolts.sign > 0.0) { upperLimitSwitch.get() }
                else { lowerLimitSwitch.get() }
            }
            .finallyDo({ interrupted ->
                runIntakeMotors(0.0.volts)
            })
    }

    /**
     * A command to protect the intake when appraoching the trench, bump or hub. // todo test!
     */
    fun ProtectIntakeCommand() : Command {
        return MoveIntakeCommand(9.0.volts)
            .until { !HedgieHelmet.trenchDriveTrigger.asBoolean }
            .repeatedly()
    }

    override fun periodic() {
        SmartDashboard.putBoolean("Subsystems/Intake/lowerPressed", lowerLimitSwitch.get())
        SmartDashboard.putBoolean("Subsystems/Intake/upperPressed", upperLimitSwitch.get())
    }

    /**
     * Runs the two motors that move the intake.
     * @param voltage the voltage to run the motors at.
     */
    fun runIntakeMotors(voltage: VoltageUnit = 1.0.volts) {
        leftIntakeMotor.setVoltage(-voltage.asVolts.clamp(
            -Constants.MAX_VOLTS.asVolts,
            Constants.MAX_VOLTS.asVolts
        ))
        rightIntakeMotor.setVoltage(voltage.asVolts.clamp(
            -Constants.MAX_VOLTS.asVolts,
            Constants.MAX_VOLTS.asVolts
        ))
    }
}