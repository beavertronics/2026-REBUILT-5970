package frc.robot.commands.subsystems

import beaverlib.fieldmap.FieldMapREBUILTWelded
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.asRPM
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Angular.radians
import beaverlib.utils.Units.Electrical.volts
import beaverlib.utils.Units.Linear.earthGravity
import beaverlib.utils.Units.Linear.feet
import beaverlib.utils.Units.Linear.inches
import beaverlib.utils.Units.Linear.meters
import beaverlib.utils.Units.Linear.metersPerSecond
import beaverlib.utils.Units.Linear.metersPerSecondSquared
import beaverlib.utils.geometry.Vector2
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.ShooterConstants
import frc.robot.subsystems.`according to all known laws of aviation, our robot should not be able to fly`
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.sqrt

object AAC {
    val shooterHeight = 6.0.feet.asMeters.meters
    val hubHeight = 72.0.inches.asMeters.meters
    val heightDiff = hubHeight - shooterHeight
    val yOffset = 1.7.meters // todo how is this used?
}

class AutoAngleHood(
    val dynamic: Boolean = false
) : Command() {
    init { addRequirements(Shooter) }

    var velocity = 0.0
    var calculatedSqrt = 0.0
    var calculatedPos = 0.0
    var calculatedNeg = 0.0
    var vsq = 0.0.metersPerSecondSquared
    var posAngle = 0.0.degrees
    var negAngle = 0.0.degrees
    var distance = 0.0.meters
    var hoodAngle = 0.0.degrees

    override fun execute() {
        // get flywheel velocity (inches / min)
        if (dynamic) { velocity = Shooter.currentRPM.asRPM * (PI * 4) } // live RPM
        else { velocity = Shooter.targetRPM.asRPM * (PI * 4) } // fixed RPM

        // inches per min to meters per sec^2
        vsq = (velocity / 2362.0)  // todo is this right?
            .metersPerSecond
            .asMetersPerSecond
            .metersPerSecondSquared

        // get distance (hypotenuse) from hub
        distance = Vector2(
            `according to all known laws of aviation, our robot should not be able to fly`.pose)
            .distance(FieldMapREBUILTWelded.teamHub.center)
            .meters

        // calculated the sqrt
        calculatedSqrt = sqrt(
            vsq.asMetersPerSecondSquared * vsq.asMetersPerSecondSquared
            - earthGravity.asMetersPerSecondSquared
                    * (
                        earthGravity.asMetersPerSecondSquared
                            * distance.asMeters
                            * distance.asMeters
                            + 2
                            * AAC.heightDiff.asMeters
                            * vsq.asMetersPerSecondSquared
                    )
        )

        // get the positive and negative parts of sqrt
        calculatedPos = atan((vsq.asMetersPerSecondSquared + calculatedSqrt) /
                (earthGravity.asMetersPerSecondSquared * distance.asMeters))
        calculatedNeg = atan((vsq.asMetersPerSecondSquared - calculatedSqrt) /
                (earthGravity.asMetersPerSecondSquared * distance.asMeters))

        // clamp to constrained degrees
        posAngle =
            (90.0.degrees.asDegrees - calculatedPos.radians.asDegrees)
                .clamp(
                    0.0.degrees.asDegrees, ShooterConstants.HOOD_MAX.asDegrees
                ).degrees
        negAngle =
            (90.0.degrees.asDegrees - calculatedPos.radians.asDegrees)
                .clamp(
                    0.0.degrees.asDegrees, ShooterConstants.HOOD_MAX.asDegrees
                ).degrees

        // get final (bigger) angle
        hoodAngle = when {
            (posAngle.asDegrees > negAngle.asDegrees) -> posAngle
            (negAngle.asDegrees > posAngle.asDegrees) -> negAngle
            (negAngle.asDegrees == posAngle.asDegrees) -> posAngle
            else -> 0.0.degrees
        }

        // move hood to angle
        CommandScheduler.getInstance().schedule(MoveHoodToAngle(hoodAngle, 3.0.volts)) // todo find out voltage, if this works?
    }

    override fun isFinished(): Boolean {
        return super.isFinished()
    }

    override fun end(interrupted: Boolean) {
        super.end(interrupted)
    }
}