package frc.robot.commands.subsystems

import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.TeleOp
import frc.robot.subsystems.Intake
import kotlin.math.abs

/**
 * Extends the intake in or out of the robot.
 * @param up whether to move the intake up or not.
 * @param voltage the voltage to move the intake at.
 */
class MoveIntake(
    val up: Boolean = true,
    val voltage: VoltageUnit = 1.0.volts
) : Command() {

    init { addRequirements(Intake) }

    // default to lower
    lateinit var limitSwitch: DigitalInput
    var direction = 0.0

    override fun initialize() {
        direction = when (up) {
            true -> 1.0
            false -> -1.0
        }
        limitSwitch = when (up) {
            true -> Intake.upperLimitSwitch
            false -> Intake.lowerLimitSwitch
        }
    }

    override fun execute() {
        while (!limitSwitch.get()) {
            Intake.leftIntakeMotor.setVoltage(abs(voltage.asVolts) * -direction)
            Intake.rightIntakeMotor.setVoltage(abs(voltage.asVolts) * direction)
        }
    }

    override fun isFinished(): Boolean {
        return limitSwitch.get()
                || TeleOp.OI.intakeOut.negate().asBoolean
                || TeleOp.OI.intakeIn.negate().asBoolean
    }

    override fun end(interrupted: Boolean) {
        Intake.leftIntakeMotor.stopMotor()
        Intake.rightIntakeMotor.stopMotor()
    }
}