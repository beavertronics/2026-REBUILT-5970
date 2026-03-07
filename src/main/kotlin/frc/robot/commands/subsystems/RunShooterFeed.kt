package frc.robot.commands.subsystems

import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.ShooterFeed

/**
 * Runs the shooter feed motor at the inputted voltage.
 * @param voltage the voltage to run the motor at.
 */
class RunShooterFeed(
    val voltage: Double = 0.0
) : Command() {

    init { addRequirements(ShooterFeed) }

    override fun execute() { ShooterFeed.runFeed(voltage) }

    override fun isFinished(): Boolean { return false }

    override fun end(interrupted: Boolean) { ShooterFeed.runFeed(0.0) }
}