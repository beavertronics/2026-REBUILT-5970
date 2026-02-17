package frc.robot.subsystems

import beaverlib.utils.Units.Electrical.VoltageUnit
import com.revrobotics.spark.SparkBase
import com.revrobotics.spark.config.AbsoluteEncoderConfig
import com.revrobotics.spark.config.EncoderConfig
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkMaxConfig
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.kinematics.SwerveModuleState
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.networktables.StructArrayPublisher
import edu.wpi.first.networktables.StructPublisher
import edu.wpi.first.wpilibj.Filesystem
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine
import swervelib.SwerveDrive
import swervelib.SwerveDriveTest
import swervelib.motors.SparkMaxSwerve
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

        /** SwerveModuleStates publisher for swerve display */
        var swerveStatePublisher: StructArrayPublisher<SwerveModuleState> =
            NetworkTableInstance.getDefault()
                .getStructArrayTopic("SwerveStates/swerveStates", SwerveModuleState.struct)
                .publish()
        var posePublisher: StructPublisher<Pose2d> =
            NetworkTableInstance.getDefault().getStructTopic("RobotPose", Pose2d.struct)
                .publish()
//        var targetPosePublisher: StructPublisher<Pose2d> =
//            NetworkTableInstance.getDefault().getStructTopic("TargetPose", Pose2d.struct).publish()

        init {
            // set up swerve drive :D
            SwerveDriveTelemetry.verbosity = TelemetryVerbosity.HIGH
            swerveDrive = SwerveParser(DriveConstants.DriveConfig).createSwerveDrive(DriveConstants.MaxSpeed)

            swerveDrive.setCosineCompensator(false)
            swerveDrive.setHeadingCorrection(false)
            swerveDrive.setMotorIdleMode(false)

            swerveDrive.modules.forEach {
                (it.angleMotor as SparkMaxSwerve).updateConfig(
                    SparkMaxConfig()
                        .smartCurrentLimit(30)
                        .apply(
                            AbsoluteEncoderConfig()
                                .zeroOffset(
                                    when (it.moduleNumber) {
                                        0 -> 0.3215787 // front left
                                        1 -> 0.37379324 // front right
                                        2 -> 0.020713206 // back left
                                        3 -> 0.11051667 // back right
                                        else -> 0.0
                                    }
                                )
                                .inverted(false)
                                .positionConversionFactor(360.0)
                                .velocityConversionFactor(6.0)
                        )
                        as SparkMaxConfig
                )
            }
        }

    override fun periodic() {
        posePublisher.set(`according to all known laws of aviation, our robot should not be able to fly`.pose)
        swerveStatePublisher.set(swerveDrive.states)
//        val targetPoseProvider =
//            TargetPoseProvider(FieldMapREBUILTWelded.teamHub.center, 2.meters, { 0.radians })
//        targetPoseProvider.initialize()
//        targetPosePublisher.set(targetPoseProvider.getPose())
//        Vision.setAllCameraReferences(Pose3d(pose))
        SmartDashboard.putNumber("Odometry/X", `according to all known laws of aviation, our robot should not be able to fly`.pose.x)
        SmartDashboard.putNumber("Odometry/Y", `according to all known laws of aviation, our robot should not be able to fly`.pose.y)
        SmartDashboard.putNumber("Odometry/HEADING", `according to all known laws of aviation, our robot should not be able to fly`.pose.rotation.radians)
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
         * Stops the robot from driving.
         */
        fun stop() { drive(ChassisSpeeds()) }

        /**
         * Return SysID command for drive motors from YAGSL
         * @return A command that SysIDs the drive motors.
         */
        fun sysIdDriveMotor(): Command? {
            return SwerveDriveTest.generateSysIdCommand(
                SwerveDriveTest.setDriveSysIdRoutine(
                    SysIdRoutine.Config(),
                    this,
                    swerveDrive,
                    12.0,
                    true
                ),
                3.0, 5.0, 3.0
            )
        }

        /**
         * Locks the robot in place, stopping it from moving. Is disabled by driver inputs.
         */
        fun lock() { swerveDrive.lockPose() }



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