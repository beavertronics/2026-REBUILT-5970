package frc.robot

import kotlin.math.*
import beaverlib.utils.Sugar.within
import beaverlib.utils.Units.Angular.RPM
import beaverlib.utils.Units.Electrical.volts
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup
import edu.wpi.first.wpilibj2.command.SubsystemBase
import edu.wpi.first.wpilibj2.command.button.CommandJoystick
import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import frc.robot.commands.drive.ChildModeDriveCommand
import frc.robot.commands.drive.TeleopDriveCommand
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.Hood
import frc.robot.subsystems.Intake
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.Hopper
import frc.robot.subsystems.IntakeMover
import frc.robot.subsystems.ShooterFeed
import frc.robot.subsystems.general.HedgieHelmet

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

        // SUBSYSTEMS!
        // intake
//        Intake.defaultCommand = Intake.RunIntakeCommand(true, 0.0.volts) // todo test
        // intake mover
//        IntakeMover.defaultCommand = IntakeMover.ProtectIntakeCommand() // todo test
        // hopper
//        Hopper.defaultCommand = Hopper.AgitateHopperCommand() // todo test
        // shooter feed
//        ShooterFeed.defaultCommand = ShooterFeed.RunShooterFeedCommand((-12.0).volts) // todo test
        // hood
//        Hood.defaultCommand = MoveHoodToAngle( // todo test
//            Hood.autoCalculateHood(false),
//            0.25.volts
//        )
        // shooter
//        Shooter.defaultCommand = Shooter.ShootRPMCommand(0.0.RPM) // todo test
    }

    /**
     * configures things to run on specific inputs
     */
    fun configureBindings() {
        // run the intake
        OI.runIntake.whileTrue(Intake.RunIntakeCommand(true, 12.0.volts))
        OI.runOuttake.whileTrue(Intake.RunIntakeCommand(false, 12.0.volts))

        // move the intake in or out
        OI.intakeIn.whileTrue(IntakeMover.MoveIntakeCommand(5.0.volts))
        OI.intakeOut.and(HedgieHelmet.trenchDriveTrigger.negate()).whileTrue(IntakeMover.MoveIntakeCommand((-5.0).volts))

        // spindexer and shooter kicked independent controls
        OI.indexIn.whileTrue(
            ParallelCommandGroup(
                Hopper.RunHopperCommand(12.0.volts),
                ShooterFeed.RunShooterFeedCommand(12.0.volts)
            )
        )
        OI.indexOut.whileTrue(
            ParallelCommandGroup(
                Hopper.RunHopperCommand((-12.0).volts),
                ShooterFeed.RunShooterFeedCommand((-12.0).volts)
            )
        )

        // shooter
        OI.runShooter.whileTrue(Shooter.ShootRPMCommand(100.0.RPM)) // todo test

        // safety override features
        OI.runShooter
            .and(OI.safetyOverride)
//            .whileTrue(Shooter.ShootVoltageCommand(0.3.volts))
            .whileTrue(Shooter.ShootVoltageCommand(12.0.volts))
        OI.zeroHood
            .and(OI.safetyOverride)
            .onTrue(Hood.ZeroHoodCommand())
        OI.hoodUp
            .and(OI.safetyOverride)
            .whileTrue(Hood.MoveHoodVoltageCommand(0.25.volts))
        OI.hoodDown
            .and(OI.safetyOverride)
            .whileTrue(Hood.MoveHoodVoltageCommand((-0.25).volts))
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
            val intakeOut get() = xboxController.rightTrigger()
            val intakeIn get() = xboxController.rightBumper()
            // spindexer and shooter kicker
            val indexIn get() = xboxController.b()
            val indexOut get() = xboxController.x()
            // shooter
            val runShooter get() = xboxController.leftTrigger()
            // shooter hood
            val zeroHood get() = xboxController.povLeft()
            val hoodTest get() = xboxController.povRight()
            val hoodUp get() = xboxController.povUp()
            val hoodDown get() = xboxController.povDown()
            // all
            val safetyOverride get() = xboxController.leftBumper()
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