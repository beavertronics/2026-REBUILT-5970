package frc.robot

import kotlin.math.*
import beaverlib.utils.Sugar.within
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import edu.wpi.first.wpilibj2.command.button.CommandJoystick
import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import frc.robot.commands.drive.ChildModeDriveCommand
import frc.robot.commands.drive.SwankDriveCommand
import frc.robot.commands.drive.TeleopDriveCommand
import frc.robot.commands.vision.FollowAprilTag
//import frc.robot.commands.tests.MoveIntake
import frc.robot.subsystems.Drivetrain

/*
Sets up the operator interface (controller inputs), as well as
setting up the commands for running the drivetrain and the subsystems
 */

/**
 * class for managing systems and inputs
 */
object TeleOp {
    val teleOpDrive: TeleopDriveCommand =
        TeleopDriveCommand(
            { OI.C_LY },
            { OI.C_LX },
            { OI.C_RX },
            { OI.C_RT.asBoolean },
            { OI.C_LT.asBoolean }
        )
    val swankDrive: SwankDriveCommand =
        SwankDriveCommand(
            { OI.C_LY },
            { OI.C_LX },
            { OI.C_LT.asBoolean }
        )
    val childDrive: ChildModeDriveCommand =
        ChildModeDriveCommand(
            { OI.C_LY },
            { OI.C_LX },
            { OI.C_RX },
            { OI.C_LB.asBoolean },
            { OI.LJS_Y },
            { OI.LJS_X },
            { OI.RJS_X },
            { OI.C_RT.asBoolean },
            { OI.C_LT.asBoolean }

        )

    init {
        // SWAP THIS WITH WHATEVER COMMAND YOU WANT TO BE DRIVING THE ROBOT!
        Drivetrain.defaultCommand = teleOpDrive
    }

    /**
     * configures things to run on specific inputs
     */
    fun configureBindings() {
        OI.C_LB.whileTrue(FollowAprilTag(1))
//        OI.movement.whileTrue(Move(1.0, 0.0, 0.0))
//        OI.driveCircle.whileTrue(Circle())
//        OI.lowerIntake.whileTrue(MoveIntake(DoubleSolenoid.Value.kForward))
//        OI.raiseIntake.whileTrue(MoveIntake(DoubleSolenoid.Value.kReverse))
    }

    /**
     * Class for the operator interface
     * getting inputs from controllers and whatnot.
     */
    object OI : SubsystemBase() {
        val xboxController = CommandXboxController(0)
        val leftJoystick = CommandJoystick(1)
        val rightJoystick = CommandJoystick(2)

        /**
         * Allows you to tweak controller inputs (ie get rid of deadzone, make input more sensitive by squaring or cubing it, etc).
         */
        private fun Double.processInput(
            deadzone: Double = 0.1,
            squared: Boolean = false,
            cubed: Boolean = false,
            readjust: Boolean = true
        ): Double {
            var processed = this
            if (readjust) processed = ((this.absoluteValue - deadzone) / (1 - deadzone)) * this.sign
            return when {
                this.within(deadzone) -> 0.0
                squared -> processed.pow(2) * this.sign
                cubed -> processed.pow(3)
                else -> processed
            }
        }

        private fun Double.abs_GreaterThan(target: Double): Boolean {
            return this.absoluteValue > target
        }

        /**
         * Allows the inputted controller to rumble
         */
        class Rumble(
            val controller: CommandXboxController,
            val time: Double = 1.0,
            val rumblePower: Double = 1.0,
            val rumbleSide: GenericHID.RumbleType = GenericHID.RumbleType.kRightRumble
        ) : Command() {
            val timer = Timer()

            init {
                addRequirements(OI)
            }

            override fun initialize() {
                timer.restart(); controller.setRumble(rumbleSide, rumblePower)
            }

            override fun execute() {
                controller.setRumble(rumbleSide, rumblePower)
                // update the pose

            }

            override fun end(interrupted: Boolean) {
                controller.setRumble(rumbleSide, 0.0)
            }

            override fun isFinished(): Boolean {
                return timer.hasElapsed(time)
            }
        }

        /**
         * Values for inputs go here
         */
        //===== DRIVETRAIN =====//
        val C_LX get() = xboxController.leftX.processInput()
        val C_LY get() = xboxController.leftY.processInput()
        val C_RX get() = xboxController.rightX.processInput()
        val C_LB get() = xboxController.leftBumper()
        val C_LT get() = xboxController.leftTrigger()
        val C_RT get() = xboxController.rightTrigger()
        val LJS_X get() = leftJoystick.x.processInput()
        val LJS_Y get() = leftJoystick.y.processInput()
        val RJS_X get() = rightJoystick.x.processInput()
        val RJS_Y get() = rightJoystick.y.processInput()
        //===== SUBSYSTEMS =====//
    }
}






































































































// uwu