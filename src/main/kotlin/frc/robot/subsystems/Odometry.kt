package frc.robot.subsystems

import edu.wpi.first.math.VecBuilder
import edu.wpi.first.math.geometry.*
import edu.wpi.first.util.sendable.SendableBuilder
import edu.wpi.first.util.sendable.SendableRegistry
import edu.wpi.first.wpilibj.smartdashboard.Field2d
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.SubsystemBase

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
                )
                    return
                val newPose = camera.getEstimatedRobotPose(result) ?: return
                addVisionMeasurement(newPose.toPose2d(), result.timestampSeconds, true)
            },
        )
        setVisionMeasurementStdDevs(3.0, 4.0, 5.0)

        // sets the offset of the gyro
//        Drivetrain.swerveDrive.setGyroOffset(Rotation3d(0.0, 0.0, PI)) // TODO
    }

    // pose of the robot
    val pose get() = Drivetrain.swerveDrive.pose
    var updateVisionOdometry = true
    val field = Field2d()

    override fun periodic() {
        field.robotPose = pose
        SmartDashboard.putData("Odometry/field", field)
    }

    /**
     * Enables or disables the updating of the odometry with vision.
     * @param enable whether to enable or disable
     */
    fun doEnableVisionOdometry(enable: Boolean = true) =
        InstantCommand({ updateVisionOdometry = enable })

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
        if (updateRotation) Drivetrain.swerveDrive.addVisionMeasurement(measurement, timestamp)
        else
            Drivetrain.swerveDrive.addVisionMeasurement(
                Pose2d(measurement.x, measurement.y, Drivetrain.swerveDrive.pose.rotation),
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
        Drivetrain.swerveDrive.swerveDrivePoseEstimator.setVisionMeasurementStdDevs(
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