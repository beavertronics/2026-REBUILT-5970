package frc.robot.commands.subsystems

import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Shooter

/**
 * Runs the shooter with the specified voltage.
 * @param voltage the voltage to run the motor at.
 */
class ShootVoltage(
    val voltage: VoltageUnit = 1.0.volts
) : Command() {

    init { addRequirements(Shooter) }

    override fun execute() { Shooter.runShooter(voltage) }

    override fun isFinished(): Boolean { return false }

    override fun end(interrupted: Boolean) { Shooter.runShooter(0.0.volts) }
}