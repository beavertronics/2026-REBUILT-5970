package frc.robot.commands.vision

import beaverlib.controls.PIDConstants
import beaverlib.controls.toPID
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Sugar.roundTo
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.Vision
import org.photonvision.targeting.PhotonTrackedTarget
import edu.wpi.first.wpilibj.Timer
import kotlin.random.Random

/**
 * Aligns to a given april tag with the set up FRC field.
 * @param aprilTagID the April Tag ID to align to.
 * @param speedLimit the max speed (in meters per second) to run the robot at.
 * @param offsets the offsets from the april tag in a Pose2D.
 * - A Pose2d(0.0, -1.0) will move the robot to the left of the tag by a meter.
 * - A Pose2d(0.0, 1.0) will move the robot to the right of the tag by a meter.
 * - A Pose2d(1.0, 0.0) will move the robot away from the tag by a meter.
 * - A Pose2d(-1.0, 0.0) will move the robot towards the tag by a meter.
 *
 * An example offset of Pose2d(2.0, 1.0, Rotation2d(0.0)) will move the robot:
 * - 2 meters away from the tag
 * - 1 meter to the right of the tag
 * - 0.0 radians rotated
 * @param end whether to end after aligning to the April Tag or not.
 */
class AlignToTag(
    val aprilTagID: Int,
    val speedLimit: Double = 1.0,
    val offsets: Pose2d = Pose2d(1.0, 0.0, Rotation2d()),
    val end: Boolean = true
) : Command() {
    var firstCalculation = false
    val kXPID = PIDConstants(2.0, 0.0, 0.0)
    val kYPID = PIDConstants(2.0, 0.0, 0.0)
    val kOPID = PIDConstants(2.0, 0.0, 0.0)
    val xPID = kXPID.toPID()
    val yPID = kYPID.toPID()
    val oPID = kOPID.toPID()
    val listenerName = "AlignTag" + aprilTagID + "-" + Random(1)
    var timeSinceTagSeen = 0.0
    val timer = Timer()

    // add the needed subsystems to requirements
    init {
        timer.reset()
        timer.restart()
        addRequirements(Drivetrain)
        // give it some deadzone
        oPID.setTolerance(0.075) // degrees // NOTE NOT USED FOR DEADZONE ONLY FOR FINISH!
        xPID.setTolerance(0.175) // meters
        yPID.setTolerance(0.175) // meters
    }

    // get the correct april tag
    var desiredTag : PhotonTrackedTarget? = null
    override fun initialize() {
        // set up vision
        println("Aligning to tag: " + aprilTagID)
        println("Listener name: " + listenerName)
        Vision.listeners.add(
            "AlignTag",
            { result, camera ->
                val desiredTagA = result.targets.filter { it.fiducialId == aprilTagID }
                if (desiredTagA.isEmpty() || desiredTagA.first().poseAmbiguity > 0.5) {
                    return@add
                }
                timeSinceTagSeen = timer.get()
                desiredTag = desiredTagA.first()
//                 val robotToTag = camera.poseEstimator.update(result)
            },
        )

        // reset things
        oPID.reset()
        xPID.reset()
        yPID.reset()
        oPID.setpoint = offsets.rotation.degrees
        xPID.setpoint = offsets.x
        yPID.setpoint = offsets.y
        firstCalculation = false
    }

    // align with tag in respects to X, Y, and rotation
    override fun execute() {
        // if we lost the april tag, stop driving (and don't ram into things!)
        if (
            (desiredTag == null || timer.get() - timeSinceTagSeen > 0.1) // doesnt see tag OR time since tag > 0.1
            ||
            (desiredTag == null && timer.get() - timeSinceTagSeen > 0.1) // doesnt see tag AND time since tag > 0.1
        ) {
            Drivetrain.stop()
            println("Tag " + aprilTagID + " is not found! is coprocessor online?")
            return
        }

        // generate a value to align with the tag via z (rotation)
        val yawToTag = desiredTag!!.bestCameraToTarget.rotation.z
        // 180 degrees is facing tag, so gets difference from 180 - current rotation
        // also, rotation2D handles the issue with a half-turn rotation and wraps to 360 (?)
        val rotateError = Rotation2d.fromDegrees(180.0).minus(Rotation2d.fromRadians(yawToTag)).radians
        val calculatedRotate = (oPID.calculate(rotateError) * -1.0)
        var driveRotate = (calculatedRotate).clamp(-1.0 * speedLimit, speedLimit)
        if (oPID.atSetpoint()) {
            driveRotate = 0.0
        }


        // generate a value to align with the tag via x (forwards/backwards)
        val xToTag = desiredTag!!.bestCameraToTarget.x
        val xError = xToTag
        val calculatedX = (xPID.calculate(xError))
        var driveX = (calculatedX).clamp(-1.0 * speedLimit, speedLimit)
        if (xPID.atSetpoint()) {
            driveX = 0.0
        }


        // generate a value to align with the tag via y (left/right)
        val yToTag = desiredTag!!.bestCameraToTarget.y
        val yError = yToTag
        val calculatedY = (yPID.calculate(yError))
        var driveY = (calculatedY).clamp(-1.0 * speedLimit, speedLimit)
        if (yPID.atSetpoint()) {
            driveY = 0.0
        }

        println("Tag, rotation error, x error, y error: "
                + aprilTagID
                + ", "
                + calculatedRotate.roundTo(3)
                + ", "
                + calculatedX.roundTo(3)
                + ", "
                + calculatedY.roundTo(3)
        )

        // add everything into a chassis speed and drive robot
        Drivetrain.drive(ChassisSpeeds(driveX, driveY, driveRotate)) // in m/s and radians/s

        // after first calculation has been implemented, change boolean to true
        if (firstCalculation == false) { firstCalculation = true }
    }

    override fun isFinished(): Boolean {
        if (
            xPID.atSetpoint() &&
            yPID.atSetpoint() &&
            oPID.atSetpoint() &&
            firstCalculation &&
            end
//            && desiredTag != null
        ) {
            println("Done for tag: " + aprilTagID)
            return true
        }
        return false
    }

    override fun end(interrupted: Boolean) {
        Vision.listeners.remove(listenerName)
        Drivetrain.stop()
        return
    }
}