package frc.robot.commands.general

import beaverlib.controls.PIDConstants
import beaverlib.controls.toPID
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.degrees
import edu.wpi.first.math.MathUtil
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.Odometry

// https://docs.wpilib.org/en/stable/docs/software/basic-programming/coordinate-system.html
/**
 * Moves the robot to an absolute point on the field.
 * @param target the target pose you want to move the robot to.
 * - An absolute point is a fixed point on the field. The right-blue corner of the field is the origin.
 * @param speedLimit the speed, in m/s, to limit the robot to.
 * */
// todo tune, get working
class RotateTo(val target: () -> Rotation2d, val speedLimit: Double = 0.0) : Command() {
    val kOPID = PIDConstants(1.0, 0.0, 0.0)
    // create all PID controllers
    val oPID = kOPID.toPID()

    init {
        addRequirements(Drivetrain)
        oPID.enableContinuousInput(-180.0.degrees.asRadians, 180.0.degrees.asRadians)
    }

    override fun initialize() {
        // reset all PID controllers
        oPID.reset()
        // set tolerances
        oPID.setTolerance(0.25)
        // set the setpoints for PID
        // removes full rotations and whatnot? keeps it within 0-360 (or 0-2pi)
        oPID.setpoint = MathUtil.angleModulus(target().radians)
        // disable vision updating odometry
        Odometry.doEnableVisionOdometry(false)
    }

    override fun execute() {
        // calculate the errors
        val oDrive = oPID.calculate(
            Odometry.pose.rotation.radians
                    * 1.0 // inversion
        )
        // drive the robot
        Drivetrain.drive(
            ChassisSpeeds(
                0.0,
                0.0,
                oDrive.clamp(-speedLimit, speedLimit)
            ),
            fieldOriented = true // todo necessary?
        )
    }

    override fun isFinished(): Boolean {
        return oPID.atSetpoint()
    }

    override fun end(interrupted: Boolean) {
        Drivetrain.stop()
        Odometry.doEnableVisionOdometry(true)
        return
    }
}