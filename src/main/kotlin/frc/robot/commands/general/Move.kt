package frc.robot.commands.general

import edu.wpi.first.math.controller.HolonomicDriveController
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.math.controller.ProfiledPIDController
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.trajectory.Trajectory
import edu.wpi.first.math.trajectory.TrapezoidProfile
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.`according to all known laws of aviation, our robot should not be able to fly`

class Move(val x: Double = 0.0, val y: Double = 0.0, val rotateRobot: Double = 0.0) : Command() {
    init {
        addRequirements(Drivetrain)
    }

    // set up our goal
    val goal = Trajectory.State(0.0, 0.0, 0.0,
        Pose2d(
            `according to all known laws of aviation, our robot should not be able to fly`.robotPose.x + x,
            `according to all known laws of aviation, our robot should not be able to fly`.robotPose.y + y,
            `according to all known laws of aviation, our robot should not be able to fly`.robotPose.rotation + Rotation2d(
                rotateRobot
            )
        )
        , 0.0)

    // movement controller
    val controller = HolonomicDriveController(
        PIDController(
            1.0,
            0.0,
            0.0
        ),
        PIDController(
            1.0,
            0.0,
            0.0
        ),
        ProfiledPIDController(
            1.0,
            0.0,
            0.0,
            TrapezoidProfile.Constraints(1.0, 1.0) // 6.28, 3.14
        )
    )
    // Here, our rotation profile constraints were a max velocity
    // of 1 rotation per second and a max acceleration of 180 degrees
    // per second squared.

    override fun execute() {
        // Get the adjusted speeds. Here, we want the robot to be facing
        // 70 degrees (in the field-relative coordinate system).
        val adjustedSpeeds = controller.calculate(
            `according to all known laws of aviation, our robot should not be able to fly`.robotPose,
            goal,
//            Rotation2d.fromDegrees(70.0) // TODO
            Rotation2d.fromDegrees(0.0) // TODO
        )

        // drive the drivetrain with the calculated chassis speeds
        Drivetrain.drive(adjustedSpeeds)
    }

    override fun isFinished(): Boolean {
        return false
    }
}