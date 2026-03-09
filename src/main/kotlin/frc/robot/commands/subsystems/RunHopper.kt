package frc.robot.commands.subsystems

import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Hopper

/**
 * Runs the hopper motor at the inputted voltage.
 * @param voltage the voltage to run the motor at.
 */
class RunHopper(
    val voltage: VoltageUnit = 1.0.volts
) : Command() {

    init { addRequirements(Hopper) }

    override fun execute() { Hopper.runHopper(voltage) }

    override fun isFinished(): Boolean { return false }

    override fun end(interrupted: Boolean) { Hopper.runHopper(0.0.volts) }
}