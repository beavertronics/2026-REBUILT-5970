package frc.robot.subsystems

import Engine.BeaverPhotonVision
import Engine.VisionCamera
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Linear.feet
import beaverlib.utils.Units.Linear.inches
import edu.wpi.first.apriltag.AprilTag
import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.apriltag.AprilTagFields
import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.math.geometry.Rotation3d
import edu.wpi.first.math.geometry.Transform3d
import org.photonvision.PhotonPoseEstimator

// using the bunnybots 2025 "Carrot Chaos" layout for the april tag layout
// todo GET POSITIONS BETTER!
val customAprilTags : MutableList<AprilTag> = mutableListOf(

    // blue feeder station
    AprilTag(1, Pose3d(
        180.0.inches.asMeters, 147.65.inches.asMeters, 44.75.inches.asMeters,
        Rotation3d(
            0.0.degrees.asRadians, 0.0.degrees.asRadians, -90.degrees.asRadians
        ))
    ),
    AprilTag(2, Pose3d(
        190.8.inches.asMeters, 158.4.inches.asMeters, 44.75.inches.asMeters,
        Rotation3d(
            0.0.degrees.asRadians, 0.0.degrees.asRadians, 0.0.degrees.asRadians
        ))
    ),
    AprilTag(3, Pose3d(
        180.0.inches.asMeters, 169.15.inches.asMeters, 44.75.inches.asMeters,
        Rotation3d(
            0.0.degrees.asRadians, 0.0.degrees.asRadians, 90.degrees.asRadians
        ))
    ),
    AprilTag(4, Pose3d(
        169.2.inches.asMeters, 158.4.inches.asMeters, 44.75.inches.asMeters,
        Rotation3d(
            0.0.degrees.asRadians, 0.0.degrees.asRadians, 180.degrees.asRadians
        ))
    ),

    // red feeder station
    AprilTag(5, Pose3d(
        540.0.inches.asMeters, 147.65.inches.asMeters, 44.75.inches.asMeters,
        Rotation3d(
            0.0.degrees.asRadians, 0.0.degrees.asRadians, 90.0.degrees.asRadians
        ))
    ),
    AprilTag(6, Pose3d(
        529.2.inches.asMeters, 158.4.inches.asMeters, 44.75.inches.asMeters,
        Rotation3d(
            0.0.degrees.asRadians, 0.0.degrees.asRadians, 180.0.degrees.asRadians
        ))
    ),
    AprilTag(7, Pose3d(
        540.0.inches.asMeters, 169.15.inches.asMeters, 44.75.inches.asMeters,
        Rotation3d(
            0.0.degrees.asRadians, 0.0.degrees.asRadians, -90.0.degrees.asRadians
        ))
    ),
    AprilTag(8, Pose3d(
        550.8.inches.asMeters, 158.4.inches.asMeters, 44.75.inches.asMeters,
        Rotation3d(
            0.0.degrees.asRadians, 0.0.degrees.asRadians, 0.0.degrees.asRadians
        ))
    )
)
val customFieldLayout = AprilTagFieldLayout(customAprilTags, 62.75.feet.asMeters, 26.4.feet.asMeters)

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
        layout = AprilTagFieldLayout.loadField(AprilTagFields.k2024Crescendo),
        strategy = PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
        fallbackStrategy = PhotonPoseEstimator.PoseStrategy.CLOSEST_TO_REFERENCE_POSE

    )
)