package frc.robot.commands.subsystems

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.asRotations
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Angular.rotations
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.ShooterConstants

/**
 * Moves the hood to an inputted angle within 0 to 55 degrees.
 * @param angle the angle, in degrees to rotate the hood to.
 * @param voltage the voltage to run the hood motor at.
 */
class MoveHoodToAngle(
    val angle: Double = 0.0,
    val voltage: Double = 1.0
) : Command() {

    init { addRequirements(Shooter) }

    override fun execute() {
        // keep inputted angle within safe toleranes
        val clamped = (angle.degrees.asDegrees - Shooter.zeroValue.rotations.asDegrees) // degrees
            .clamp(
                ShooterConstants.HOOD_MIN.degrees.asDegrees,
                ShooterConstants.HOOD_MAX.degrees.asDegrees) // degrees
            .rotations.asRotations // rotations

        // position of encoder for shooter hod
        val pos = Shooter.hoodMotor.absoluteEncoder.position.rotations.asRotations
        val calculated = Shooter.hoodPID.calculate(pos, clamped)
        while (!Shooter.hoodPID.atSetpoint()) { Shooter.runHood(calculated * voltage) }
        Shooter.runHood(0.0)
        return
    }

    override fun isFinished(): Boolean { return Shooter.hoodPID.atSetpoint() }

    override fun end(interrupted: Boolean) {}
}