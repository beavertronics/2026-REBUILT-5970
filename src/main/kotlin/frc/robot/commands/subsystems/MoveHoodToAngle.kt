package frc.robot.commands.subsystems

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.AngleUnit
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.ShooterConstants

/**
 * Moves the hood to an inputted angle within 0 to 55 degrees.
 * @param angle the angle, in degrees to rotate the hood to.
 * @param voltage the voltage to run the hood motor at.
 */
class MoveHoodToAngle(
    val angle: AngleUnit = 0.0.degrees,
    val voltage: VoltageUnit = 1.0.volts
) : Command() {

    init { addRequirements(Shooter) }

    override fun execute() {
        // keep inputted angle within safe tolerances
        val clamped = angle.asDegrees.clamp(
            ShooterConstants.HOOD_MIN.asDegrees,
            ShooterConstants.HOOD_MAX.asDegrees
        ) // degrees

        // position of encoder for shooter hood
        val pos = Shooter.currentAngle
        val calculated = Shooter.hoodPID.calculate(pos.asDegrees, clamped)
        while (!Shooter.hoodPID.atSetpoint()) { Shooter.runHood((calculated * voltage.asVolts).volts) }
        Shooter.runHood(0.0.volts)
        return
    }

    override fun isFinished(): Boolean { return Shooter.hoodPID.atSetpoint() }

    override fun end(interrupted: Boolean) {}
}