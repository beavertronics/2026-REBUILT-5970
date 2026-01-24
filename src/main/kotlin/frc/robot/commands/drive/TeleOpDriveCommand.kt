// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package frc.robot.commands.drive

import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.DriveConstants
import frc.robot.subsystems.Drivetrain
import java.util.function.BooleanSupplier
import java.util.function.DoubleSupplier

///////////////////////////////////////////////////
/*
Responsible for running the drivetrain ONLY
 */
///////////////////////////////////////////////////

/**
 * A command that controls the swerve drive using controller inputs.
 * @param vForward The x velocity of the robot.
 * @param vStrafe The y velocity of the robot.
 * @param omega The angular velocity of the robot.
 * @param driveMode Boolean supplier that returns true if the robot should drive in field-oriented mode.
 * @param slowMode Boolean supplier that returns true if the robot should drive in slow mode.
 * @see Drivetrain
 */
class TeleopDriveCommand(
    val vForward: DoubleSupplier,
    val vStrafe: DoubleSupplier,
    val omega: DoubleSupplier,
    val driveMode: BooleanSupplier,
    val slowMode: BooleanSupplier,
) : Command() {
    private val controller = Drivetrain.swerveDrive.getSwerveController()

    // each subsystem adds itself as a requirement
    init { addRequirements(Drivetrain) }

    /** @suppress */
    override fun execute() {
        var forwardVelocity = vForward.asDouble
        var strafeVelocity = vStrafe.asDouble
        var angVelocity = omega.asDouble
        val slowMode = slowMode.asBoolean
        SmartDashboard.putNumber("vX", forwardVelocity)
        SmartDashboard.putNumber("vY", strafeVelocity)
        SmartDashboard.putNumber("omega", angVelocity)

        if (slowMode) {
            forwardVelocity *= 0.125
            strafeVelocity *= 0.125
            angVelocity *= 0.125
        }

        // Drive using raw values
        Drivetrain.drive(
            Translation2d(
                forwardVelocity * DriveConstants.MaxSpeed,
                strafeVelocity * DriveConstants.MaxSpeed),
            angVelocity * controller.config.maxAngularVelocity,
            driveMode.asBoolean
        )
    }

    /** @suppress */
    override fun end(interrupted: Boolean) {}

    /** @suppress */
    override fun isFinished(): Boolean {
        return false
    }
}