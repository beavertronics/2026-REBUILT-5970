package beaverlib.odometry
// File adapted from 2898's bpsrobotics engine
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.util.sendable.Sendable
import edu.wpi.first.util.sendable.SendableBuilder
import edu.wpi.first.util.sendable.SendableRegistry
import beaverlib.utils.Units.Angular.AngleUnit
import beaverlib.utils.Units.Angular.radians
import beaverlib.utils.Units.Linear.DistanceUnit
import beaverlib.utils.Units.Linear.meters
import java.util.function.Supplier

/** Provides an orientation and position for the robot */
interface PoseProvider : Sendable {
    /** Provides the pose as a WPIlib Pose2d */
    val pose: Pose2d

    /** Updates the [pose] variable */
    fun periodic()

    fun reset(x: DistanceUnit, y: DistanceUnit, theta: AngleUnit)
    fun reset(pose2d: Pose2d) {
        reset(pose2d.x.meters, pose2d.y.meters, pose2d.rotation.radians.radians)
    }
    /*fun getPose() : Pose2d{
        return pose
    }*/
    override fun initSendable(builder: SendableBuilder) {
        SendableRegistry.setName(this, toString())
        builder.addDoubleProperty("x", { pose.x }, null)
        builder.addDoubleProperty("y", { pose.y }, null)
        builder.addDoubleProperty("rotation", { pose.rotation.radians }, null)
    }
}