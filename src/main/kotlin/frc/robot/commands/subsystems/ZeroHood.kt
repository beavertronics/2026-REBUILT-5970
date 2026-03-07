package frc.robot.commands.subsystems

import beaverlib.utils.Units.Angular.asRotations
import beaverlib.utils.Units.Angular.rotations
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Shooter

/**
 * Zeros the hood to 0 degrees.
 */
class ZeroHood() : Command() {

    init { addRequirements(Shooter) }

    var done = false

    override fun execute() {
        while (!Shooter.lowerLimitSwitch.get()) { Shooter.runHood(1.0) }
        Shooter.runHood(0.0)
        Shooter.zeroValue =
            Shooter.hoodMotor.encoder.position.rotations.asRotations
        done = true
    }

    override fun isFinished(): Boolean { return done }

    override fun end(interrupted: Boolean) {}
}