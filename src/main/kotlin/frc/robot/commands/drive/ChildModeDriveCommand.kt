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
class ChildModeDriveCommand(
    // all parent inputs
    val parentForward: DoubleSupplier,
    val parentStrafe: DoubleSupplier,
    val parentOmega: DoubleSupplier,
    val toggleChildMode: BooleanSupplier,

    // all child inputs
    val childForward: DoubleSupplier,
    val childStrafe: DoubleSupplier,
    val childOmega: DoubleSupplier,

    // general inputs
    val driveMode: BooleanSupplier, // only applies for parent, not child
    val slowMode: BooleanSupplier, // by default on for child
) : Command() {

    private val swerveController = Drivetrain.swerveDrive.getSwerveController()
    val slowMult = 0.125

    // each subsystem adds itself as a requirement
    init { addRequirements(Drivetrain) }

    /** @suppress */
    override fun execute() {
        // parent inputs
        var pFV = parentForward.asDouble
        var pSV = parentStrafe.asDouble
        var pAV = parentOmega.asDouble

        // child inputs
        var cFV = childForward.asDouble
        var cSV = childStrafe.asDouble
        var cAV = childOmega.asDouble

        // intially make velocities
        var forwardVelocity = 0.0
        var strafeVelocity = 0.0
        var angVelocity = 0.0

        // if child mode is on, apply slow mode to their inputs
        if (toggleChildMode.asBoolean) {
            forwardVelocity = cFV * slowMult
            strafeVelocity = cSV * slowMult
            angVelocity = cAV * slowMult
        }
        // if its off, get raw inputs from parent then apply slow mode if enabled
        else {
            forwardVelocity = pFV
            strafeVelocity = pSV
            angVelocity = pAV
            if (slowMode.asBoolean) {
                forwardVelocity * slowMult
                strafeVelocity * slowMult
                angVelocity * slowMult
            }
        }

        // Drive using values
        Drivetrain.drive(
            Translation2d(
                forwardVelocity * DriveConstants.MaxSpeed,
                strafeVelocity * DriveConstants.MaxSpeed),
            angVelocity * swerveController.config.maxAngularVelocity,
            driveMode.asBoolean
        )
    }

    /** @suppress */
    override fun end(interrupted: Boolean) {
        Drivetrain.drive(Translation2d(0.0, 0.0), 0.0, false)
    }

    /** @suppress */
    override fun isFinished(): Boolean {
        return false
    }
}