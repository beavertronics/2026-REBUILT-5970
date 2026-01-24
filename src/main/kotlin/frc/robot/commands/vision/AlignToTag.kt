package frc.robot.commands.vision

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Sugar.roundTo
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.Vision
import org.photonvision.targeting.PhotonTrackedTarget
import kotlin.random.Random

class AlignToTag(
    val aprilTagID: Int,
    val speedLimit: Double = 1.0,
    val rotateSetpoint: Double = 0.0, // degrees
    val xSetpoint: Double = 0.0, // meters, make sure to consider robot dimensions
    val ySetpoint: Double = 0.0 // meters
) : Command() {
    var firstCalculation = false
    val rotateKP = 0.45 // 0.25
    val rotateKD = 0.1
    val xKP = 1.45
    val yKP = 1.45
    val rotatePID = PIDController(rotateKP, 0.0, rotateKD)
    val xPID = PIDController(xKP, 0.0, 0.0)
    val yPID = PIDController(yKP, 0.0, 0.0)
    val listenerName = "AlignTag" + aprilTagID + "-" + Random(1)

    // add the needed subsystems to requirements
    init {
        addRequirements(Drivetrain)
        // give it some deadzone
        rotatePID.setTolerance(0.1) // degrees // NOTE NOT USED FOR DEADZONE ONLY FOR FINISH!
        xPID.setTolerance(0.15) // meters
        yPID.setTolerance(0.15) // meters

    }

    // get the correct april tag
    var desiredTag : PhotonTrackedTarget? = null
    override fun initialize() {
        // set up vision
        println("Aligning to tag: " + aprilTagID)
        println("Listener name: " + listenerName)
        Vision.listeners.add(listenerName) { result, _ ->
            val desiredTagA = result.targets.filter { it.fiducialId == aprilTagID }
            if (desiredTagA.isNotEmpty()) {
                desiredTag = desiredTagA.first()
            }
        }

        // reset things
        rotatePID.reset()
        xPID.reset()
        yPID.reset()
        rotatePID.setpoint = rotateSetpoint
        xPID.setpoint = xSetpoint
        yPID.setpoint = ySetpoint
        firstCalculation = false
    }

    // align with tag in respects to X, Y, and rotation
    override fun execute() {
        // if we lost the april tag, stop driving (and don't ram into things!)
        if (desiredTag == null) {
            Drivetrain.drive(ChassisSpeeds())
            return
        }

        // generate a value to align with the tag via z (rotation)
        val yawToTag = desiredTag!!.bestCameraToTarget.rotation.z
        // 180 degrees is facing tag, so gets difference from 180 - current rotation
        // also, rotation2D handles the issue with a half-turn rotation and wraps to 360 (?)
        val rotateError = Rotation2d.fromDegrees(180.0).minus(Rotation2d.fromRadians(yawToTag)).radians
        val calculatedRotate = (rotatePID.calculate(rotateError) * -1.0)
        var driveRotate = (calculatedRotate).clamp(-1.0 * speedLimit, speedLimit)
        if (rotatePID.atSetpoint()) {
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

        println("Rotation error, x error, y error: "
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
        if (xPID.atSetpoint() && yPID.atSetpoint() && rotatePID.atSetpoint() && firstCalculation) {
            println("Done for tag: " + aprilTagID)
            return true
        }
        return false
    }

    override fun end(interrupted: Boolean) {
        Vision.listeners.remove(listenerName)
        return
    }
}