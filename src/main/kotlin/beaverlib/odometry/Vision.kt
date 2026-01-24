package beaverlib.odometry
// Filed adapted from 2898s 2023 Charged Up code
import edu.wpi.first.apriltag.AprilTag
import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.apriltag.AprilTagFields
import edu.wpi.first.math.Matrix
import edu.wpi.first.math.Nat
import edu.wpi.first.math.geometry.*
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N3
import edu.wpi.first.networktables.NetworkTableEvent
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj2.command.SubsystemBase
import beaverlib.utils.Sugar.degreesToRadians
import org.photonvision.EstimatedRobotPose
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import java.util.*

val aprilTags1: MutableList<AprilTag> = mutableListOf(
    AprilTag(2,
        Pose3d(
            Translation3d(0.00,0.0,0.0),
            Rotation3d(0.0,0.0,90.0.degreesToRadians())
        )
    ) ,
    AprilTag(1,
        Pose3d(
            Translation3d(0.4445,0.0,0.0),
            Rotation3d(0.0,0.0,90.0.degreesToRadians())
    )
)
)
val testLayout1 = AprilTagFieldLayout(aprilTags1,10.0,5.0)
val aprilTagFieldLayout = AprilTagFieldLayout.loadFromResource(AprilTagFields.k2024Crescendo.m_resourceFile)

/* Needs to be part of main season robot code, because it depends on robot-specific constants

(Needs to import Constants.OdometryConstants.VisionDeviation)

Good example code though...

//TODO: Update for Photon
class Vision (
    CameraName: String,
    aprilTagLayout: AprilTagFieldLayout
) {
    val cam = PhotonCamera(CameraName);
    var robotToCam = Transform3d(
        Translation3d(0.0, 0.0, 0.0),
        Rotation3d(0.0, 0.0, 0.0)
    )

    val aprilTagFieldLayout : AprilTagFieldLayout = aprilTagLayout
    val PoseEstimator = PhotonPoseEstimator(
        aprilTagFieldLayout,
        PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
        cam,
        robotToCam
    )
    fun getEstimatedPose(prevEstimatedRobotPose: Pose2d?): EstimatedRobotPose? {
        return null
        PoseEstimator.setReferencePose(prevEstimatedRobotPose)
        val pose = PoseEstimator.update() ?: return null
        if(pose.isPresent) return pose.get()
        return null
    }
    fun getStdDev() : Matrix<N3,N1 > {
        return Constants.OdometryConstants.VisionDeviation
    }
}
*/