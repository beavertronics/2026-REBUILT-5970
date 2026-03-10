package frc.robot.commands.subsystems

import beaverlib.utils.Units.Electrical.volts
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Shooter

/**
 * Zeros the hood to 0 degrees. // todo test
 */
class ZeroHood() : Command() {

    init { addRequirements(Shooter) }

    var done = false

    override fun initialize() { done = false }

    override fun execute() {
        while (!Shooter.lowerLimitSwitch.get()) { Shooter.runHood(-0.5.volts) }
        Shooter.runHood(0.0.volts)
        done = Shooter.setZero()
    }

    override fun isFinished(): Boolean { return done }

    override fun end(interrupted: Boolean) {}
}