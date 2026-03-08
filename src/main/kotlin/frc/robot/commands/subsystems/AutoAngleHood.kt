package frc.robot.commands.subsystems

import beaverlib.fieldmap.FieldMapREBUILTWelded
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Angular.radians
import beaverlib.utils.Units.Linear.earthGravity
import beaverlib.utils.Units.Linear.feet
import beaverlib.utils.Units.Linear.inches
import beaverlib.utils.Units.Linear.meters
import beaverlib.utils.Units.Linear.metersPerSecond
import beaverlib.utils.Units.Linear.metersPerSecondSquared
import beaverlib.utils.geometry.Vector2
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import frc.robot.commands.general.Move
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.ShooterConstants
import frc.robot.subsystems.`according to all known laws of aviation, our robot should not be able to fly`
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.sqrt

object AAC {
    val shooterHeight = 6.0.feet.asMeters
    val hubHeight = 72.0.inches.asMeters
    val heightDiff = hubHeight - shooterHeight
    val yOffset = 1.7.meters // todo how is this used?
}

class AutoAngleHood(
    val dynamic: Boolean = false
) : Command() {
    init { addRequirements(Shooter) }

    var distance: Double = 0.0
    var velocity: Double = 0.0
    var rawAnglePlus: Double = 0.0
    var rawAngleMinus: Double = 0.0
    var hoodAngle: Double = 0.0

    override fun initialize() {
        super.initialize()
    }

    override fun execute() {
        // get flywheel velocity (inches / min)
        if (dynamic) { velocity = Shooter.currentRPM * (PI * 4) } // live RPM
        else { velocity = Shooter.targetRPM * (PI * 4) } // fixed RPM

        // inches per min to meters per sec^2
        velocity = (velocity / 2362.0)
            .metersPerSecond.asMetersPerSecond.metersPerSecondSquared.asMetersPerSecondSquared

        // get distance (hypotenuse) from hub
        distance = Vector2(
            `according to all known laws of aviation, our robot should not be able to fly`.pose)
            .distance(FieldMapREBUILTWelded.teamHub.center)
            .meters.asMeters

        // get angle for hood (+ of sqrt)
        rawAnglePlus =
            atan(
                    (
                        velocity - sqrt(
                            velocity * velocity - earthGravity.asMetersPerSecondSquared *
                                    (
                                            earthGravity.asMetersPerSecondSquared
                                            * distance * distance + 2 * AAC.heightDiff * velocity
                                    )
                    )) / (earthGravity.asMetersPerSecondSquared * distance)
            )
        // get angle for hood (- of sqrt)
        rawAngleMinus =
            atan(
                (
                        velocity + sqrt(
                            velocity * velocity - earthGravity.asMetersPerSecondSquared *
                                    (
                                            earthGravity.asMetersPerSecondSquared
                                                    * distance * distance + 2 * AAC.heightDiff * velocity
                                            )
                        )) / (earthGravity.asMetersPerSecondSquared * distance)
            )
        // clamp
        rawAnglePlus =
        90.0.degrees.asDegrees - rawAnglePlus.radians.asDegrees
            .clamp(0.0.degrees.asDegrees, ShooterConstants.HOOD_MAX.degrees.asDegrees)
        rawAngleMinus =
            90.0.degrees.asDegrees - rawAngleMinus.radians.asDegrees
                .clamp(0.0.degrees.asDegrees, ShooterConstants.HOOD_MAX.degrees.asDegrees)
        // get final (bigger) angle
        hoodAngle = when {
            (rawAnglePlus > rawAngleMinus) -> rawAnglePlus
            (rawAngleMinus > rawAnglePlus) -> rawAngleMinus
            (rawAngleMinus == rawAnglePlus) -> rawAnglePlus
            else -> 0.0
        }

        // move hood to angle
        CommandScheduler.getInstance().schedule(MoveHoodToAngle(hoodAngle, 3.0)) // todo find out voltage, if this works?
    }

    override fun isFinished(): Boolean {
        return super.isFinished()
    }

    override fun end(interrupted: Boolean) {
        super.end(interrupted)
    }
}