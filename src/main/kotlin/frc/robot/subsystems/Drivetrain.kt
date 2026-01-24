package frc.robot.subsystems

import beaverlib.utils.Units.Electrical.VoltageUnit
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.wpilibj.Filesystem
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine
import swervelib.SwerveDrive
import swervelib.SwerveDriveTest
import swervelib.parser.SwerveParser
import swervelib.telemetry.SwerveDriveTelemetry
import swervelib.telemetry.SwerveDriveTelemetry.*
import java.io.File

/**
 * class for all constants for drivetrain
 */
object DriveConstants {
    // for YAGSL to find swerve directory
    val DriveConfig = File(Filesystem.getDeployDirectory(), "swerve")
    val MaxSpeed = 10.0 // in m/s
}

/**
 * the main class for the drivetrain, containing everything
 */
object Drivetrain : SubsystemBase() {
        // create anything that is set later (late init)
        var swerveDrive: SwerveDrive

        /**
         * init file that runs on intialization of drivetrain class
         */
        init {
            // set up swerve drive :D
            SwerveDriveTelemetry.verbosity = TelemetryVerbosity.HIGH
            swerveDrive = SwerveParser(DriveConstants.DriveConfig).createSwerveDrive(DriveConstants.MaxSpeed)
        }
        /**
         * Directly send voltage to the drive motors.
         * @param volts The voltage to send to the motors.
         */
        fun setDriveMotorVoltageRaw(volts: VoltageUnit){
            swerveDrive.modules.forEach {
                it.driveMotor.voltage = volts.asVolts
            }
        }
        /**
         * Directly send voltage to the angle motors.
         * @param volts The voltage to send to the motors.
         */
        fun setAngleMotorVoltageRaw(volts: VoltageUnit){
            swerveDrive.modules.forEach {
                it.angleMotor.voltage = volts.asVolts
            }
        }

        /**
         * Advanced drive method that translates and rotates the robot, with a custom center of rotation.
         * @param translation The desired X and Y velocity of the robot.
         * @param rotation The desired rotational velocity of the robot.
         * @param fieldOriented Whether the robot's motion should be field oriented or robot oriented.
         * @param centerOfRotation The center of rotation of the robot.
         */
        fun drive(
            translation: Translation2d,
            rotation: Double = 0.0,
            fieldOriented: Boolean = false,
            centerOfRotation: Translation2d = Translation2d()
        ) {
            swerveDrive.drive(translation, rotation, fieldOriented, false, centerOfRotation)

        }
        /**
         * Advanced drive method that translates and rotates the robot, with a custom center of rotation.
         * @param translation The desired X and Y velocity of the robot.
         * @param rotation The desired rotational velocity of the robot.
         * @param fieldOriented Whether the robot's motion should be field oriented or robot oriented.
         * @param centerOfRotation The center of rotation of the robot.
         */
        fun driveOpenLoop(
            translation: Translation2d,
            rotation: Double = 0.0,
            fieldOriented: Boolean = false,
            centerOfRotation: Translation2d = Translation2d()
        ) {
            swerveDrive.drive(translation, rotation, fieldOriented, true, centerOfRotation)
        }

        /**
         * Simple drive method that uses ChassisSpeeds to control the robot.
         * @param velocity The desired ChassisSpeeds of the robot
         * @param fieldOriented if false, drive the robot such that forwards is in the direction the robot is facing
         * if true, forward will be forward relative to the field.
         */
        fun drive(velocity: ChassisSpeeds, fieldOriented: Boolean = false) {
            if(fieldOriented) swerveDrive.driveFieldOriented(velocity); else swerveDrive.drive(velocity)
        }

        /**
         * Return SysID command for drive motors from YAGSL
         * @return A command that SysIDs the drive motors.
         */
        fun sysIdDriveMotor(): Command? {
            return SwerveDriveTest.generateSysIdCommand(
                SwerveDriveTest.setDriveSysIdRoutine(
                    SysIdRoutine.Config(),
                    this,
                    swerveDrive, 12.0),
                3.0, 5.0, 3.0
            )
        }

        /**
         * Return SysID command for angle motors from YAGSL
         * @return A command that SysIDs the angle1 motors.
         */
        fun sysIdAngleMotorCommand(): Command {
            return SwerveDriveTest.generateSysIdCommand(
                SwerveDriveTest.setAngleSysIdRoutine(
                    SysIdRoutine.Config(),
                    this, swerveDrive
                ),
                3.0, 5.0, 3.0
            )
        }
}