package frc.robot.subsystems

import beaverlib.fieldmap.FieldMapREBUILTWelded
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.AngleUnit
import beaverlib.utils.Units.Angular.AngularVelocity
import beaverlib.utils.Units.Angular.RPM
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.asRPM
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Linear.inches
import beaverlib.utils.geometry.Vector2
import edu.wpi.first.math.VecBuilder
import edu.wpi.first.math.geometry.*
import edu.wpi.first.math.interpolation.InterpolatingDoubleTreeMap
import edu.wpi.first.util.sendable.SendableBuilder
import edu.wpi.first.util.sendable.SendableRegistry
import edu.wpi.first.wpilibj.smartdashboard.Field2d
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.robot.subsystems.Drivetrain.swerveDrive

object Odometry : SubsystemBase() {

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
        setVisionMeasurementStdDevs(1.0, 1.0, 0.25)

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
    // offset from robot to shooter
    val robotToShooter = Transform2d(Translation2d(
        (-26.0/2).inches.asMeters,
        (26.0/2).inches.asMeters
    ), Rotation2d()) // todo
    // interpolater for guessing hood angle for distance
    val distanceInterpolator = InterpolatingDoubleTreeMap.ofEntries(
    ) // pairs of <distance (meters), hood angle (degrees)>
    val flywheelInterpolator = InterpolatingDoubleTreeMap.ofEntries(
    ) // pairs of <distance (meters), flywheel rpm (rpm)<

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
        if (updateRotation) swerveDrive.addVisionMeasurement(measurement, timestamp)
        else
            swerveDrive.addVisionMeasurement(
                Pose2d(measurement.x, measurement.y, swerveDrive.pose.rotation),
                timestamp,
            )
    }

    /**
     * Gets the true distance from the shooter in the back-left corner to the hub.
     * @return Pose2d
     */
    fun getShooterPose() : Pose2d {
        return swerveDrive.pose.plus(robotToShooter)
    }

    /**
     * Gets the pose to be facing the hub, with offsets.
     * @return Vector2
     */
    fun getVectorToHub() : Vector2 {
        // will get the difference in poses
        return FieldMapREBUILTWelded.teamHub.center.minus(getShooterPose())
    }

    /**
     * Gets the same pose as the robot with rotation applied to face the hub (rotation in radians!)
     */
    fun getRotationToHub() : Pose2d {
        return Pose2d(
            pose.x,
            pose.y,
            Rotation2d(getVectorToHub().angle.asRadians)
        )
    }

    /**
     * Returns the interpolated guess for the hood angle (degrees)
     * @return AngleUnit
     */
    fun getApproxHoodAngle() : AngleUnit {
        return distanceInterpolator.get(getVectorToHub().magnitude).clamp(
            HoodConstants.HOOD_MIN.asDegrees, HoodConstants.HOOD_MAX.asDegrees
        ).degrees
    }

    /**
     * Returns the interpolated guess for the flywheel rpm
     * @return AngularVelocity
     */
    fun getApproxFlywheelRPM() : AngularVelocity {
        return flywheelInterpolator.get(getVectorToHub().magnitude).clamp(
            0.0, ShooterConstants.RPM_LIMIT.asRPM
        ).RPM
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