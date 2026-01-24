package Engine

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.math.geometry.Transform3d
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.photonvision.targeting.PhotonPipelineResult

class VisionCamera(val name: String, val robotToCamera: Transform3d, layout: AprilTagFieldLayout, strategy: PhotonPoseEstimator.PoseStrategy, fallbackStrategy : PhotonPoseEstimator.PoseStrategy? = null ) {
    val cam = PhotonCamera(name)
    val results: List<PhotonPipelineResult> get() = cam.allUnreadResults
    val poseEstimator = PhotonPoseEstimator(layout, strategy, robotToCamera)


    init {
        if(fallbackStrategy != null) poseEstimator.setMultiTagFallbackStrategy(fallbackStrategy)
    }


    fun getEstimatedRobotPose(result: PhotonPipelineResult) : Pose3d? {
        val estimatedPose = poseEstimator.update(result) ?: return null
        if (estimatedPose.isEmpty) return null
        return estimatedPose.get().estimatedPose
    }

    fun setReference(pose: Pose2d) {
        poseEstimator.setReferencePose(pose)
    }
}