package frc.robot.subsystems

import beaverlib.utils.geometry.vector2
import edu.wpi.first.math.VecBuilder
import edu.wpi.first.math.geometry.*
import edu.wpi.first.util.sendable.SendableBuilder
import edu.wpi.first.util.sendable.SendableRegistry
import edu.wpi.first.wpilibj.smartdashboard.Field2d
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.robot.subsystems.Drivetrain.swerveDrive

object `according to all known laws of aviation, our robot should not be able to fly` : SubsystemBase() {

    init {
        // Updates odometry whenever vision sees apriltag
        Vision.listeners.add(
            "UpdateOdometry",
            fun(result, camera) {
                if (!updateVisionOdometry) return
                if (result.targets.isEmpty()) return
                if (
                    !result.multitagResult.isPresent && (result.targets.first().poseAmbiguity > 0.3)
                ) return
                val newPose = camera.getMultiTagPoseWithFallback(result) ?: return
                addVisionMeasurement(newPose.toPose2d(), result.timestampSeconds, true)
            },
        )
        setVisionMeasurementStdDevs(1.0, 1.0, 0.25) // todo tune

//        swerveDrive.setGyroOffset( // todo
//            Rotation3d(
//                0.0,
//                0.0,
//                0.0
//            )
//        )
    }

    // pose of the robot
    val pose get() = swerveDrive.pose
    var updateVisionOdometry = true
    val field = Field2d()

    override fun periodic() {
        field.robotPose = pose
        SmartDashboard.putData("Odometry/field", field)
    }

//    /**
//     * A command that wraps over the function to reset odometry.
//     * @param trans whether to reset translation.
//     * @param rot whether to reset rotation.
//     */
//    fun doResetOdometry(trans: Boolean, rot: Boolean) : Command {
//        return run { resetOdometry(trans, rot) }
//    }
//
//    /**
//     * A command that resets field oriented specifically.
//     * @param global whether to reset the robot globally.
//     * - If true, the robot will reset facing forwards from your alliance
//     * to
//     */
//    fun doResetFieldOriented(
//        global: Boolean
//    ) : Command {
//        return run {
//            if (global) {
//
//            }
//            else { resetOdometry(false, true) }
//        }
//
//    }

    /**
     * Enables or disables the updating of the odometry with vision.
     * @param enable whether to enable or disable
     */
    fun doEnableVisionOdometry(enable: Boolean = true) =
        InstantCommand({ updateVisionOdometry = enable })

    /**
     * Resets the pose of the drivetrain.
     * @param trans whether to reset translation.
     * @param rot whether to reset rotation.
     */
    fun resetOdometry(trans: Boolean, rot: Boolean) {
        // get current translation and rotation
        var translation = pose.translation
        var rotation = pose.rotation
        // reset depending on the input
        if (trans) { translation = Translation2d() }
        if (rot) { rotation = Rotation2d() }
        // reset odometry in swerve drive
        swerveDrive.resetOdometry(Pose2d(translation, rotation))
    }

    /**
     * Add a vision measurement to the swerve drive's pose estimator.
     *
     * @param measurement The pose measurement to add.
     * @param timestamp The timestamp of the pose measurement.
     */
    fun addVisionMeasurement(
        measurement: Pose2d,
        timestamp: Double,
        updateRotation: Boolean = false,
    ) {
        if (updateRotation) swerveDrive.addVisionMeasurement(measurement, timestamp)
        else
            swerveDrive.addVisionMeasurement(
                Pose2d(measurement.x, measurement.y, swerveDrive.pose.rotation),
                timestamp,
            )
    }

    /**
     * Set the standard deviations of the vision measurements.
     *
     * @param stdDevX The standard deviation of the X component of the vision measurements.
     * @param stdDevY The standard deviation of the Y component of the vision measurements.
     * @param stdDevTheta The standard deviation of the rotational component of the vision
     *   measurements.
     */
    fun setVisionMeasurementStdDevs(stdDevX: Double, stdDevY: Double, stdDevTheta: Double) {
        swerveDrive.swerveDrivePoseEstimator.setVisionMeasurementStdDevs(
            VecBuilder.fill(stdDevX, stdDevY, stdDevTheta)
        )
    }

    override fun initSendable(builder: SendableBuilder) {
        SendableRegistry.setName(this, toString())
        if (pose != null) {
            builder.addDoubleProperty("x", { pose.x }, null)
            builder.addDoubleProperty("y", { pose.y }, null)
            builder.addDoubleProperty("rotation", { pose.rotation.radians }, null)}
    }
}