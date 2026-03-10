package frc.robot.commands.subsystems

import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.TeleOp
import frc.robot.TeleOp.OI
import frc.robot.subsystems.Shooter

class MoveHoodVolt(
    val voltage: VoltageUnit = 1.0.volts
) : Command() {

    init {
        addRequirements(Shooter)
    }

    override fun execute() {
        Shooter.runHood(voltage)
    }

    override fun isFinished(): Boolean {
        return Shooter.lowerLimitSwitch.get() && OI.hoodUp.asBoolean == false
    }

    override fun end(interrupted: Boolean) { Shooter.runHood(0.0.volts) }
}