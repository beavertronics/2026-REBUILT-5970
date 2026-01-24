package frc.robot.commands.tests

import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Drivetrain
import kotlin.math.cos
import kotlin.math.sin

class Circle : Command() {
    var degree = 0.0
    init {
        addRequirements(Drivetrain)
    }

    override fun execute() {
        val forwards = sin(degree)
        val strafe = cos(degree)
        Drivetrain.swerveDrive.drive(ChassisSpeeds(forwards * 0.25, strafe * 0.25, 0.0))
        degree += 0.02
    }

    override fun isFinished(): Boolean {
        return false
    }
}