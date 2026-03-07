package frc.robot.commands.subsystems

import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Intake

/**
 * Runs the intake motor at the inputted voltage and direction.
 * @param intake whether to intake or not
 * @param voltage the voltage to run the intake motor at.
 */
class RunIntake(
    val intake: Boolean = true,
    val voltage: Double = 1.0
) : Command() {

    init { addRequirements(Intake) }

    override fun execute() { Intake.runIntake(intake, voltage) }

    override fun isFinished(): Boolean { return Intake.upperLimitSwitch.get() }

    override fun end(interrupted: Boolean) { Intake.runIntake(intake, 0.0) }
}