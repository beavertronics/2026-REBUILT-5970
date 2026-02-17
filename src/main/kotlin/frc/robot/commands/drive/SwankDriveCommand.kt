package frc.robot.commands.drive

import beaverlib.utils.Sugar.clamp
import edu.wpi.first.wpilibj2.command.Command
import frc.robot.subsystems.Drivetrain
import java.util.function.BooleanSupplier
import java.util.function.DoubleSupplier

/**
 * A command that drives swerve like Tank drive.
 * @param vLeft the controller input for left drive
 * @param vRight the controller input for right drive
 * @param slowMode the toggle for enabling slow mode
 * @see Drivetrain
 */
class SwankDriveCommand(
    var vLeft: DoubleSupplier,
    var vRight: DoubleSupplier,
    val slowMode: BooleanSupplier
) : Command() {

    val slowMult = 0.125
    val maxTurnAngle = 3.0 // max Angle allowed for turning wheels in degrees (needed to avoid wheel wear / tire marks)

    init {
        addRequirements(Drivetrain)
    }

    // acquire all the swerve modules
//    val flm = Drivetrain.swerveDrive.moduleMap["frontLeft"] // todo exists?
    val flm = Drivetrain.swerveDrive.modules.get(0)
//    val frm = Drivetrain.swerveDrive.moduleMap["frontRight"] // todo exists?
    val frm = Drivetrain.swerveDrive.modules.get(1)
//    val blm = Drivetrain.swerveDrive.moduleMap["backLeft"] // todo exists?
    val blm = Drivetrain.swerveDrive.modules.get(2)
//    val brm = Drivetrain.swerveDrive.moduleMap["backRight"] // todo exists?
    val brm = Drivetrain.swerveDrive.modules.get(3)

//    private fun setSteering(leftVel: Double, rightVel: Double, brake: Boolean = false) {
//        // get the difference between velocites
//        val velDiff = leftVel - rightVel
//        // determine how much to rotate all wheels by
//        val rotation = velDiff * maxTurnAngle
//        // rotate specific modules, some inverted
//        /**
//         * CASE 1
//         * ---------------
//         * left is 0.6
//         * right is 0.5
//         * difference is 0.6-0.5=0.1, more power to left, we are turning right
//         * 0.1 * 3.0=0.3, that is rotation for all modules (ignoring sign)
//         * if CCW rotation increases degrees, FL and BR must be rotated -
//         * if CCW rotation increases degrees, FR and BL must be rotated +
//         *
//         * CASE 2
//         * ---------------
//         * left is 0.1
//         * right is 0.9
//         * difference is 0.1-0.9=-0.8, more power to right, we are turning left
//         * -0.8*3.0=-2.4, that is rotation for all modules (ignoring sign)
//         * if CCW rotation increases degrees, FL and BR must be rotated
//         */
//        flm?.setAngle(-1.0 * rotation)
//        brm?.setAngle(-1.0 * rotation)
//        frm?.setAngle(rotation)
//        blm?.setAngle(rotation)
//        // if brake, brake all steering motors
//        Drivetrain.swerveDrive.modules.forEach { it.angleMotor.setMotorBrake(brake) }
//    }

    /**
     * Stops the driving motors from moving.
     */
    private fun siezeDriving() {
        // stop all drive motors from moving
        Drivetrain.swerveDrive.modules.forEach {
            it.driveMotor.set(0.0)
        }
    }

    /**
     * Runs the left modules drive motors at the set speed.
     * @param mult decimal form of a percent to run the motors at.
     */
    private fun driveLeft(mult: Double) {
        flm?.driveMotor?.set(100 * mult)
        blm?.driveMotor?.set(100 * mult)
    }

    /**
     * Runs the right modules drive motors at the set speed.
     * @param mult decimal form of a percent to run the motors at.
     */
    private fun driveRight(mult: Double) {
        frm?.driveMotor?.set(100 * mult)
        brm?.driveMotor?.set(100 * mult)
    }

    override fun initialize() {
        // disable steering
//         setSteering(0.0) // todo
        // stop any driving
        siezeDriving()
    }

    override fun execute() {
        // clamped just in case someone does a wrong value
        var leftVelocity = vLeft.asDouble.clamp(-1.0, 1.0)
        var rightVelocity = vRight.asDouble.clamp(-1.0, 1.0)
        // disable any steering
//        setSteering(0.0) // todo
        // drive left and right motors
        if (slowMode.asBoolean) {
            leftVelocity *= slowMult
            rightVelocity *= slowMult
        }
        driveLeft(leftVelocity)
        driveRight(rightVelocity)
    }

    override fun isFinished(): Boolean { return false }

    // stop driving
    override fun end(interrupted: Boolean) { siezeDriving() }
}