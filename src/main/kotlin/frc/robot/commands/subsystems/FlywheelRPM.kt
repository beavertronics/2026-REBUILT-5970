package frc.robot.commands.subsystems

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.asRotationsPerSecond
import beaverlib.utils.Units.Angular.rotationsPerSecond
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.ShooterConstants

/**
 * Runs the shooter flywheel at the inputted RPM.
 * @param targetRPM the RPM to run the motor at.
 */
class FlywheelRPM(
    val targetRPM: Double = 0.0
) : Command() {

    init { addRequirements(Shooter) }

    override fun initialize() { Shooter.targetRPM =
        (targetRPM.rotationsPerSecond.asRotationsPerSecond) / 60.0 // convert RPS to RPM
    }

    override fun execute() {
        val calculated = Shooter.shooterPID.calculate(
            Shooter.currentRPM, Shooter.targetRPM
        )
        Shooter.runShooter(
            (calculated * -ShooterConstants.MAX_VOLTS)
                .clamp(
                    -ShooterConstants.MAX_VOLTS,
                    ShooterConstants.MAX_VOLTS
                )
        )
    }

    override fun isFinished(): Boolean { return false }

    override fun end(interrupted: Boolean) { Shooter.runShooter(0.0) }
}