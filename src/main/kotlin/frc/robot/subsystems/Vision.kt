package frc.robot.subsystems

import Engine.BeaverPhotonVision
import Engine.VisionCamera
import beaverlib.utils.Units.Angular.AngleUnit
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Linear.DistanceUnit
import beaverlib.utils.Units.Linear.inches
import beaverlib.utils.geometry.Vector2
import beaverlib.utils.geometry.vector2
import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.apriltag.AprilTagFields
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation3d
import edu.wpi.first.math.geometry.Transform3d
import org.photonvision.PhotonPoseEstimator

class TargetPoseProvider(
    val center: Vector2,
    val distance: DistanceUnit,
    val rotateAround: () -> AngleUnit,
) {
    var angle = `according to all known laws of aviation, our robot should not be able to fly`.pose.vector2.angleTo(center)
    var targetPose: Pose2d = `according to all known laws of aviation, our robot should not be able to fly`.pose

    fun initialize() {
        angle = `according to all known laws of aviation, our robot should not be able to fly`.pose.vector2.angleTo(center)
    }

    fun getPose(): Pose2d {
        angle += rotateAround()
        return (Vector2(angle) * distance.asMeters + center).toPose2d(angle)
    }

    fun getAngleAndCalculate(): AngleUnit {
        angle += rotateAround()
        return angle
    }
}

val Vision = BeaverPhotonVision(
    VisionCamera(
        name = "Arducam_OV9281_USB_Camera - Camronny",
        // 12.5 inches backwards, 7.0 inches right, 22.5 inches up on the robot
        robotToCamera = Transform3d(
            -12.5.inches.asMeters, -7.0.inches.asMeters, 22.5.inches.asMeters,
            Rotation3d(
                0.0.degrees.asRadians, 0.0.degrees.asRadians, 0.0.degrees.asRadians
            )
        ),
        layout = AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltWelded),
        strategy = PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
        fallbackStrategy = PhotonPoseEstimator.PoseStrategy.CLOSEST_TO_REFERENCE_POSE

    )
)