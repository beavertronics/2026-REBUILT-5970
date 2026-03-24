package frc.robot.commands.general

import beaverlib.controls.PIDConstants
import beaverlib.controls.toPID
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.degrees
import edu.wpi.first.math.MathUtil
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.`according to all known laws of aviation, our robot should not be able to fly`

// https://docs.wpilib.org/en/stable/docs/software/basic-programming/coordinate-system.html
/**
 * Moves the robot to an absolute point on the field.
 * @param target the target pose you want to move the robot to.
 * - An absolute point is a fixed point on the field. The right-blue corner of the field is the origin.
 * @param speedLimit the speed, in m/s, to limit the robot to.
 * */
// todo tune, get working
class MoveTo(val target: Pose2d, val speedLimit: Double = 1.0) : Command() {
    val kXPID = PIDConstants(1.0, 0.0, 0.0)
    val kYPID = PIDConstants(1.0, 0.0, 0.0)
    val kOPID = PIDConstants(1.0, 0.0, 0.0)
    // create all PID controllers
    val xPID = kXPID.toPID()
    val yPID = kYPID.toPID()
    val oPID = kOPID.toPID()

    init {
        addRequirements(Drivetrain)
        oPID.enableContinuousInput(-180.0.degrees.asRadians, 180.0.degrees.asRadians)
    }

    override fun initialize() {
        // reset all PID controllers
        xPID.reset()
        yPID.reset()
        oPID.reset()
        // set the setpoints for PID
        xPID.setpoint = target.x
        yPID.setpoint = target.y
        // removes full rotations and whatnot? keeps it within 0-360 (or 0-2pi)
        oPID.setpoint = MathUtil.angleModulus(target.rotation.radians)
        // disable vision updating odometry
        `according to all known laws of aviation, our robot should not be able to fly`.doEnableVisionOdometry(false)
    }

    override fun execute() {
        // calculate the errors
        val xDrive = xPID.calculate(
            `according to all known laws of aviation, our robot should not be able to fly`.pose.x
                    * 1.0 // inversion
        )
        val yDrive = yPID.calculate(
            `according to all known laws of aviation, our robot should not be able to fly`.pose.y
                    * 1.0 // inversion
        )
        val oDrive = oPID.calculate(
            `according to all known laws of aviation, our robot should not be able to fly`.pose.rotation.radians
                    * 1.0 // inversion
        )
        // drive the robot
        Drivetrain.drive(
            ChassisSpeeds(
                xDrive.clamp(-speedLimit, speedLimit),
                yDrive.clamp(-speedLimit, speedLimit),
                oDrive.clamp(-speedLimit, speedLimit)
            ),
            fieldOriented = true // todo necessary?
        )
    }

    override fun isFinished(): Boolean {
        return xPID.atSetpoint() && yPID.atSetpoint() && oPID.atSetpoint()
    }

    override fun end(interrupted: Boolean) {
        Drivetrain.stop()
        `according to all known laws of aviation, our robot should not be able to fly`.doEnableVisionOdometry(true)
        return
    }
}