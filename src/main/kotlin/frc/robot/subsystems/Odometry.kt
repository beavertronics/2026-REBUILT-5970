package frc.robot.subsystems

import beaverlib.fieldmap.FieldMapREBUILTWelded
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.AngleUnit
import beaverlib.utils.Units.Angular.AngularVelocity
import beaverlib.utils.Units.Angular.RPM
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.asRPM
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import beaverlib.utils.Units.Linear.inches
import beaverlib.utils.geometry.Vector2
import edu.wpi.first.math.VecBuilder
import edu.wpi.first.math.geometry.*
import edu.wpi.first.math.interpolation.InterpolatingDoubleTreeMap
import edu.wpi.first.util.sendable.SendableBuilder
import edu.wpi.first.util.sendable.SendableRegistry
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.Field2d
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.robot.subsystems.Drivetrain.swerveDrive

object Odometry : SubsystemBase() {

    val pose get() = swerveDrive.pose
    var updateVisionOdometry = true
    val field = Field2d()
    // offset from robot to shooter
    val robotToShooter = Transform2d(Translation2d(
        (-26.0/2).inches.asMeters,
        (8.0).inches.asMeters
    ), Rotation2d()) // todo
    // interpolating values
    // pairs of <distance (meters), hood angle (degrees)>
    // distance from the front bumpers to hub
    val hoodApprox = InterpolatingDoubleTreeMap()
    // pairs of <distance (meters), flywheel rpm (rpm)>
    val flywheelApprox = InterpolatingDoubleTreeMap()
    // pairs of <distance (meters), hopper voltage>
    val hopperApprox = InterpolatingDoubleTreeMap()
    // pairs of <distance (meters), kicker voltage>
    val kickerApprox = InterpolatingDoubleTreeMap()

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

        // put in hood values
        // for d = 0
        hoodApprox.put(0.0, 0.0)
        hopperApprox.put(0.0, 12.0)
        kickerApprox.put(0.0, 12.0)
        flywheelApprox.put(0.0, 3000.0)
        // for d = 17 inches (converted to meters)
        hoodApprox.put(15.0.inches.asMeters, 5.0)
        hopperApprox.put(15.0.inches.asMeters, 12.0)
        kickerApprox.put(15.0.inches.asMeters, 12.0)
        flywheelApprox.put(15.0.inches.asMeters, 3000.0)
        // for d = 19 inches
        hoodApprox.put(19.0.inches.asMeters, 5.0)
        hopperApprox.put(19.0.inches.asMeters, 12.0)
        kickerApprox.put(19.0.inches.asMeters, 12.0)
        flywheelApprox.put(19.0.inches.asMeters, 3000.0)
        // for d = 40 inches
        hoodApprox.put(40.0.inches.asMeters, 7.0)
        hopperApprox.put(40.0.inches.asMeters, 12.0)
        kickerApprox.put(40.0.inches.asMeters, 12.0)
        flywheelApprox.put(40.0.inches.asMeters, 3350.0)
    }

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
     * @param flip whether to flip to the opposite hub or not.
     * @return Vector2
     */
    fun getVectorToHub(flip: Boolean = false) : Vector2 {
        // for some reason it can be weird sometimes and the hub will be the opposite so we can flip it here
        if (flip) {
            if (FieldMapREBUILTWelded.getAllianceSafe() == DriverStation.Alliance.Red) {
                return FieldMapREBUILTWelded.BlueHub.center.minus(getShooterPose())
            }
            return FieldMapREBUILTWelded.RedHub.center.minus(getShooterPose())
        }
        else {
            return FieldMapREBUILTWelded.teamHub.center.minus(getShooterPose())
        }
    }

    /**
     * Gets the same pose as the robot with rotation applied to face the hub (rotation in radians!)
     */
    fun getRotationToHub() : Rotation2d {
        return Rotation2d(getVectorToHub(true).angle.asRadians)
    }

    /**
     * Returns the interpolated guess for the hood angle (degrees)
     * @return AngleUnit
     */
    fun getApproxHoodAngle() : AngleUnit {
        return hoodApprox.get(getVectorToHub(true).magnitude).clamp(
            HoodConstants.HOOD_MIN.asDegrees, HoodConstants.HOOD_MAX.asDegrees
        ).degrees
    }

    /**
     * Returns the interpolated guess for the flywheel rpm
     * @return AngularVelocity
     */
    fun getApproxFlywheelRPM() : AngularVelocity {
        return flywheelApprox.get(getVectorToHub(true).magnitude).clamp(
            0.0, ShooterConstants.RPM_LIMIT.asRPM
        ).RPM
    }

    /**
     * Returns the interpolated guess for the hopper voltage
     * @return VoltageUnit
     */
    fun getApproxHopperVoltage() : VoltageUnit {
        return hopperApprox.get(getVectorToHub(true).magnitude).clamp(
            0.0, 12.0
        ).volts
    }

    /**
     * Returns the interpolated guess for the hopper voltage
     * @return VoltageUnit
     */
    fun getApproxKickerVoltage() : VoltageUnit {
        return kickerApprox.get(getVectorToHub(true).magnitude).clamp(
            0.0, 12.0
        ).volts
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