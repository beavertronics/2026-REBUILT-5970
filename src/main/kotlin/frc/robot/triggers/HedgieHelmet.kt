package frc.robot.subsystems.general

import beaverlib.fieldmap.FieldMapREBUILTWelded
import beaverlib.utils.geometry.Vector2
import beaverlib.utils.geometry.vector2
import edu.wpi.first.wpilibj2.command.button.Trigger
import frc.robot.subsystems.DriveConstants
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.Vision

fun Vector2.crossesX(origin: Vector2, x: Double): Boolean {
    if (origin.x < x && (origin + this).x < x) return false
    if (origin.x > x && (origin + this).x > x) return false
    return true
}

/**
 * A trigger that tells you when you will collide with the trench.
 * Stolen from 2898.
 */
object HedgieHelmet {
    val trenchDriveTrigger = Trigger { willCollideWithTrench() && !Vision.cameras.isEmpty() }

    private fun willCollideWithTrench(): Boolean {
        val robotVelocityVector: Vector2 = Drivetrain.swerveDrive.robotVelocity.vector2 * 0.2
        val robotPoseVector: Vector2 = Drivetrain.swerveDrive.pose.vector2
        val robotWidthVector = Vector2(
            (DriveConstants.robotWidth + DriveConstants.bumperThickness)
                .asMeters  / 2, 0.0)

        for (area in
        arrayOf(
            FieldMapREBUILTWelded.RedAllianceAreaLineX,
            FieldMapREBUILTWelded.BlueAllianceAreaLineX,
        )) {
            if (
                (robotVelocityVector + robotWidthVector).crossesX(robotPoseVector, area.asMeters) ||
                (robotVelocityVector - robotWidthVector).crossesX(
                    robotPoseVector,
                    area.asMeters,
                )
            )
                return true
        }

        return false
    }
}