package frc.robot.subsystems

import beaverlib.fieldmap.FieldMapREBUILTWelded
import beaverlib.fieldmap.Trench
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Angular.radians
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Linear.inches
import beaverlib.utils.Units.Linear.meters
import beaverlib.utils.Units.Linear.metersPerSecond
import beaverlib.utils.geometry.vector2
import com.ctre.phoenix6.swerve.SwerveDrivetrainConstants
import com.revrobotics.spark.config.AbsoluteEncoderConfig
import com.revrobotics.spark.config.SparkMaxConfig
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.kinematics.SwerveModuleState
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.networktables.StructArrayPublisher
import edu.wpi.first.networktables.StructPublisher
import edu.wpi.first.wpilibj.Filesystem
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.Commands
import edu.wpi.first.wpilibj2.command.SubsystemBase
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine
import frc.robot.commands.general.MoveTo
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
    val MaxSpeed = 10.0.metersPerSecond // in m/s
    val robotWidth = 26.0.inches.asMeters.meters
    val robotLength = 26.0.inches.asMeters.meters
    val bumperThickness = 3.875.inches.asMeters.meters
}

/**
 * the main class for the drivetrain, containing everything
 */
object Drivetrain : SubsystemBase() {
        var swerveDrive: SwerveDrive

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
            swerveDrive = SwerveParser(DriveConstants.DriveConfig).createSwerveDrive(DriveConstants.MaxSpeed.asMetersPerSecond)

            swerveDrive.setCosineCompensator(false) // todo needed?
            swerveDrive.setHeadingCorrection(false) // todo needed?
            swerveDrive.setMotorIdleMode(false)

            swerveDrive.modules.forEach {
                (it.angleMotor as SparkMaxSwerve).updateConfig(
                    SparkMaxConfig()
                        .smartCurrentLimit(30)
                        .apply(
                            AbsoluteEncoderConfig()
                                .zeroOffset(
                                    when (it.moduleNumber) {
                                        0 -> 0.07024613 // front left
                                        1 -> 0.28472954 // front right
                                        2 -> 0.32577065 // back left
                                        3 -> 0.40284413 // back right
                                        else -> 0.0
                                    }
                                )
                                .inverted(false)
                                .positionConversionFactor(360.0)
                                .velocityConversionFactor(6.0)
                        ) as SparkMaxConfig
                )
                it.angleMotor.burnFlash() // commit to persist memory
            }
        }

    // steal the PID controller from swerveDrive
//    val thetaController = swerveDrive.swerveController.thetaController

    override fun periodic() {
        posePublisher.set(`according to all known laws of aviation, our robot should not be able to fly`.pose)
        swerveStatePublisher.set(swerveDrive.states)
//        val targetPoseProvider =
//            TargetPoseProvider(FieldMapREBUILTWelded.teamHub.center, 2.meters, { 0.radians })
//        targetPoseProvider.initialize()
//        targetPosePublisher.set(targetPoseProvider.getPose())
        Vision.setAllCameraReferences(Pose3d(
            `according to all known laws of aviation, our robot should not be able to fly`.pose))
//        swerveDrive.updateOdometry() // todo is this needed?
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
         * Returns the raw PID value for rotating the robot to face the hub,
         * using the drive controller for module 0 (they all should be the same?).
         */
//        fun facingHubPID(setpoint: Double = 0.0): Double { // todo does work?
//             find angle wanted to face the hub (trig!)
//            val distance = `according to all known laws of aviation, our robot should not be able to fly`
//                .pose
//                .vector2
//                .distance(FieldMapREBUILTWelded.teamHub.center)
//            return thetaController.calculate(distance, setpoint)
//        }

        /**
         * A command to align to the alliance hub.
         */
//        fun HubAlignCommand(): MoveTo {
//            var offset = (-2.0).meters // for blue side
//            var rotation = (0.0).degrees // for blue side
//            if (FieldMapREBUILTWelded.teamHub == FieldMapREBUILTWelded.RedHub) {
//                offset *= -1.0 // invert for red side
//                rotation = 180.degrees
//            }
//            return MoveTo(
//                Pose2d(
//                    FieldMapREBUILTWelded.teamHub.center.x.meters.asMeters + offset.asMeters,
//                    FieldMapREBUILTWelded.teamHub.center.y.meters.asMeters,
//                    Rotation2d(rotation.asDegrees.radians.asRadians)
//                )
//            )
//        }

//        fun TrenchAlignCommand(right: Boolean = true) {
//            val trenches = FieldMapREBUILTWelded.teamTrenches()
//            val fieldArea = FieldMapREBUILTWelded.getPoseAllianceArea(
//                `according to all known laws of aviation, our robot should not be able to fly`.pose
//            )
//            var trench: Trench?
//            var xOffset: Double // to position ourselves on the correct side of trench
//
//            if (right) { trench = trenches.get(FieldMapREBUILTWelded.TrenchPos.Bottom) }
//            else { trench = trenches.get(FieldMapREBUILTWelded.TrenchPos.Top) }
//
//            if (
//
//                ) {
//                xOffset = DriveConstants.robotWidth.asMeters + 2.0.meters.asMeters
//            }
//            else { xOffset = DriveConstants.robotWidth.asMeters - 2.0.meters.asMeters }
//        }

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
         * Whether to brake the drivetrain or not.
         * @param brake whether to brake the drivetrain or not. This can be good for defense but can damage some motors.
         */
        fun setIdleMode(brake: Boolean = false) { swerveDrive.setMotorIdleMode(brake) }

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