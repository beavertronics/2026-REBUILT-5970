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
import frc.robot.TeleOp
import frc.robot.subsystems.general.HedgieHelmet
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

    /**
     * A command to run the intake at the inputted voltage.
     * @param intake whether to intake or not.
     * @param voltage the voltage to run the intake motor at.
     */
    fun RunIntakeCommand(intake: Boolean = true, voltage: VoltageUnit = 1.0.volts) : Command {
        return run { runIntake(intake, voltage) }
            .finallyDo({ interrupted ->
                runIntake(intake, 0.0.volts)
            })
    }

    fun MoveIntakeCommand(voltage: VoltageUnit = 1.0.volts) : Command { // todo figure out how to stop when no button pressed
        return run { runIntakeMotors(voltage) }
            .until {
                if (voltage.asVolts > 0.0) { upperLimitSwitch.get() }
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

    /**
     * Runs the two motors that move the intake.
     * @param voltage the voltage to run the motors at.
     */
    fun runIntakeMotors(voltage: VoltageUnit = 1.0.volts) {
        leftIntakeMotor.setVoltage(-voltage.asVolts.clamp(
            -IntakeConstants.MAX_VOLTS.asVolts,
            IntakeConstants.MAX_VOLTS.asVolts
        ))
        rightIntakeMotor.setVoltage(voltage.asVolts.clamp(
            -IntakeConstants.MAX_VOLTS.asVolts,
            IntakeConstants.MAX_VOLTS.asVolts
        ))
    }
}