package frc.robot.commands.subsystems

import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.general.HedgieHelmet
import frc.robot.subsystems.Intake

/**
 * Protects the intake by moving it in if necessary.
 */
class ProtectIntake() : Command() {
    init { addRequirements(Intake) }

    override fun execute() {
        if (HedgieHelmet.trenchDriveTrigger.asBoolean) { MoveIntake(true, 9.0) }
    }

    override fun isFinished(): Boolean { return false }

    override fun end(interrupted: Boolean) {}
}