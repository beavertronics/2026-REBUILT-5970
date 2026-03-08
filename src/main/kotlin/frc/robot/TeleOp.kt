package frc.robot

import kotlin.math.*
import beaverlib.utils.Sugar.within
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup
import edu.wpi.first.wpilibj2.command.SubsystemBase
import edu.wpi.first.wpilibj2.command.button.CommandJoystick
import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import frc.robot.commands.drive.ChildModeDriveCommand
import frc.robot.commands.drive.TeleopDriveCommand
import frc.robot.commands.subsystems.ShootVoltage
import frc.robot.commands.subsystems.MoveIntake
import frc.robot.commands.subsystems.RunHopper
import frc.robot.commands.subsystems.RunIntake
import frc.robot.commands.subsystems.RunShooterFeed
import frc.robot.commands.subsystems.triggers.Triggers
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
            { OI.driverY },
            { OI.driverX },
            { OI.driverOmega },
            { OI.driveMode.asBoolean },
            { OI.slowMode.asBoolean }
        )
//    val swankDrive: SwankDriveCommand =
//        SwankDriveCommand(
//            { OI.C_LY },
//            { OI.C_LX },
//            { OI.C_LT.asBoolean }
//        )
    val childDrive: ChildModeDriveCommand =
        ChildModeDriveCommand(
            { OI.parentDrive },
            { OI.parentStrafe },
            { OI.parentOmega },
            { OI.toggleChild.asBoolean },
            { OI.driverY },
            { OI.driverX },
            { OI.driverOmega },
            { OI.toggleFieldOriented.asBoolean },
            { OI.toggleSlow.asBoolean }
        )

    init {
        // SWAP THIS WITH WHATEVER COMMAND YOU WANT TO BE DRIVING THE ROBOT!
        Drivetrain.defaultCommand = teleOpDrive
    }

    /**
     * configures things to run on specific inputs
     */
    fun configureBindings() {
        // run the intake
        OI.runIntake.whileTrue(RunIntake(true, 9.0))
        OI.runOuttake.whileTrue(RunIntake(false, 9.0))

        // move the intake in or out
        OI.intakeIn.whileTrue(MoveIntake(true, 5.0))
        OI.intakeOut.whileTrue(MoveIntake(false, 5.0))

        // spindexer and shooter kicked independent controls
        OI.indexIn.whileTrue(
            ParallelCommandGroup(
                RunHopper(10.0),
                RunShooterFeed(9.0)
            )
        )
        OI.indexOut.whileTrue(
            ParallelCommandGroup(
                RunHopper(-10.0),
                RunShooterFeed(-9.0)
            )
        )

        // shooter
        OI.runShooter.whileTrue(ShootVoltage(12.0)) // todo replace with RPM

        // run all subsystems together
        OI.doScoring.whileTrue(ShootVoltage(12.0)) // todo replace with RPM
        OI.doScoring.and(Triggers.rpmTrigger).whileTrue(
            ParallelCommandGroup(
                RunHopper(10.0),
                RunShooterFeed(9.0)
            )
        )
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
            val driverX get() = leftJoystick.x.processInput()
            val driverY get() = leftJoystick.y.processInput()
            val driverOmega get() = rightJoystick.x.processInput()
            val slowMode get() = leftJoystick.trigger()
            val driveMode get() = rightJoystick.trigger()
        //===== SUBSYSTEMS =====//
            // intake
            val runIntake get() = xboxController.a()
            val runOuttake get() = xboxController.y()
            val intakeOut get() = xboxController.leftTrigger()
            val intakeIn get() = xboxController.leftBumper()
            // spindexer and shooter kicker
            val indexIn get() = xboxController.button(0) // todo left back paddle
            val indexOut get() = xboxController.button(0) // todo right back paddle
            // shooter
            val runShooter get() = xboxController.rightBumper()
            // all
            val doScoring get() = xboxController.rightTrigger()
        //==== CHILDMODE ====//
            val parentDrive get() = xboxController.leftY.processInput()
            val parentStrafe get() = xboxController.leftX.processInput()
            val parentOmega get() = xboxController.rightX.processInput()
            val toggleChild get() = xboxController.rightTrigger()
            val toggleSlow get() = xboxController.leftTrigger()
            val toggleFieldOriented get() = xboxController.leftBumper()
    }
}






































































































// uwu