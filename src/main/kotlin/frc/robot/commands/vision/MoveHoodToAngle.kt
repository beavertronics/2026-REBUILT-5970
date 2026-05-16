package frc.robot.commands.vision

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.AngleUnit
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Hood
import frc.robot.subsystems.HoodConstants
import kotlin.math.abs

/**
 * Moves the hood to an inputted angle within 0 to 55 degrees.
 * @param angle the angle, in degrees to rotate the hood to.
 * @param voltage the voltage to run the hood motor at.
 */
class MoveHoodToAngle(
    val angle: AngleUnit = 0.0.degrees,
    val voltage: VoltageUnit = 1.0.volts
) : Command() {

    init { addRequirements(Hood) }

    override fun initialize() {
        Hood.targetAngle = angle
        Hood.hoodPID.setpoint = Hood.targetAngle.asDegrees
    }

    override fun execute() {
        // position of encoder for shooter hood
        val pos = Hood.currentAngle
        val calculated = Hood.hoodPID.calculate(pos.asDegrees)
        Hood.runHood((calculated * abs(voltage.asVolts)).volts)
    }

    override fun isFinished(): Boolean { return Hood.hoodPID.atSetpoint() }

    override fun end(interrupted: Boolean) {
        Hood.runHood(0.0.volts)
    }
}