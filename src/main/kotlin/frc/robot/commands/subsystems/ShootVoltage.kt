package frc.robot.commands.subsystems

import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Shooter

class ShootVoltage(
    val voltage: Double = 1.0
) : Command() {

    init { addRequirements(Shooter) }

    override fun execute() { Shooter.runShooter(voltage) }

    override fun isFinished(): Boolean { return false }

    override fun end(interrupted: Boolean) { Shooter.runShooter(0.0) }
}