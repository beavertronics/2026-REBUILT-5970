package frc.robot.commands.subsystems

import beaverlib.utils.Units.Angular.AngularVelocity
import beaverlib.utils.Units.Angular.RPM
import beaverlib.utils.Units.Angular.asRPM
import beaverlib.utils.Units.Electrical.volts
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.ShooterConstants

/**
 * Runs the shooter flywheel at the inputted RPM.
 * @param targetRPM the RPM to run the motor at.
 */
class ShootRPM(
    val targetRPM: AngularVelocity = 0.0.RPM
) : Command() {

    init { addRequirements(Shooter) }

    override fun initialize() {
        Shooter.setTargetRPM(targetRPM)
        Shooter.shooterPID.setpoint = Shooter.targetRPM.asRPM
    }

    override fun execute() {
        val calculated = Shooter.shooterPID.calculate(
            Shooter.currentRPM.asRPM
        )
        Shooter.runShooter((calculated * ShooterConstants.MAX_VOLTS.asVolts).volts)
    }

    override fun isFinished(): Boolean { return false }

    override fun end(interrupted: Boolean) { Shooter.runShooter(0.0.volts) }
}