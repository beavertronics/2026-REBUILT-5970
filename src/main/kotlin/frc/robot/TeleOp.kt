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
import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import frc.robot.commands.drive.ChildModeDriveCommand
import frc.robot.commands.drive.TeleopDriveCommand
import frc.robot.commands.vision.MoveHoodToAngle
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.Hood
import frc.robot.subsystems.Intake
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.Hopper
import frc.robot.subsystems.IntakeArm
import frc.robot.subsystems.Kicker
import frc.robot.subsystems.Lights
import frc.robot.triggers.General

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
//    val childDrive: ChildModeDriveCommand =
//        ChildModeDriveCommand(
//            { OI.parentDrive },
//            { OI.parentStrafe },
//            { OI.parentOmega },
//            { OI.toggleChild.asBoolean },
//            { OI.driverY },
//            { OI.driverX },
//            { OI.driverOmega },
//            { OI.toggleFieldOriented.asBoolean },
//            { OI.toggleSlow.asBoolean }
//        )

    init {
        // SWAP THIS WITH WHATEVER COMMAND YOU WANT TO BE DRIVING THE ROBOT!
        Drivetrain.defaultCommand = teleOpDrive

        // SUBSYSTEMS!
        // lights
        Lights.defaultCommand = Lights.applyPatterns(
            mutableListOf(
                Pair("system ready", "intake left"),
                Pair("system ready", "intake right")
            )
        )
        // intake
        Intake.defaultCommand = Intake.RunIntakeCommand(0.0.volts)
        // intake mover
//        IntakeArm.MoveIntakeCommand((-5.0).volts) // todo fix limit switches!
        // hopper
        Hopper.defaultCommand = Hopper.RunHopperCommand(0.0.volts)
//         shooter feed
        Kicker.defaultCommand = Kicker.RunKickerCommand(0.0.volts)
        // hood
//        Hood.defaultCommand = MoveHoodToAngle( // todo test
//            Hood.autoCalculateHood(false),
//            0.25.volts
//        )
//         shooter
        Shooter.defaultCommand = Shooter.ShootRPMCommand()
    }

    /**
     * configures things to run on specific inputs
     */
    fun configureBindings() {
        //===== DRIVETRAIN =====//
//        OI.pointHub.whileTrue( // todo test
//            TeleopDriveCommand(
//                { OI.driverY },
//                { OI.driverX },
//                { Drivetrain.facingHubPID() },
//                { OI.driveMode.asBoolean },
//                { OI.slowMode.asBoolean }
//            )
//        )
//        OI.hubAlign.whileTrue(Drivetrain.HubAlignCommand())
//        OI.leftTrenchAlign.whileTrue(Drivetrain.TrenchAlignCommand(false))
//        OI.rightTrenchAlign.whileTrue(Drivetrain.TrenchAlignCommand(true))

        //===== SUBSYSTEMS =====//
        // run the intake
        OI.runIntake.whileTrue(
            Intake.RunIntakeCommand(12.0.volts)
                .alongWith(
                    Lights.applyPatterns(
                        mutableListOf(
                            Pair("intaking", "intake left"),
                            Pair("intaking", "intake right")
                        )
                    )
                )
        )
        OI.runOuttake.whileTrue(
            Intake.RunIntakeCommand((-12.0).volts)
                .alongWith(
                    Lights.applyPatterns(
                        mutableListOf(
                            Pair("outtaking", "intake left"),
                            Pair("outtaking", "intake right")
                        )
                    )
                )
        )

        // move the intake in or out
        OI.intakeIn.whileTrue(IntakeArm.MoveIntakeCommand(5.0.volts))
        OI.intakeOut.whileTrue(IntakeArm.MoveIntakeCommand((-5.0).volts))

        // spindexer and shooter kicker independent controls
        OI.indexIn
//            .and(General.rpmTrigger)  // todo test
            .whileTrue(
            ParallelCommandGroup(
                Hopper.RunHopperCommand(9.0.volts),
                Kicker.RunKickerCommand(12.0.volts)
            )
        )
        OI.indexOut.whileTrue(
            ParallelCommandGroup(
                Hopper.RunHopperCommand((-9.0).volts),
                Kicker.RunKickerCommand((-10.0).volts)
            )
        )

        // shooter
        OI.runShooter.whileTrue(
//            Shooter.ShootRPMCommand(5500.0.RPM)
            ParallelCommandGroup(
                // HOPPER AND KICKER
                if (General.rpmTrigger.asBoolean) {
                    ParallelCommandGroup(
                        Hopper.RunHopperCommand(9.0.volts),
                        Kicker.RunKickerCommand(12.0.volts)
                    )
                }
                else { ParallelCommandGroup(
                    Kicker.RunKickerCommand((-10.0).volts),
                    Hopper.RunHopperCommand(0.0.volts) )},
                // SHOOTER
                Shooter.ShootRPMCommand(3000.0.RPM)
                    .alongWith(
            MoveHoodToAngle( // todo test
                        Hood.autoCalculateHood(false),
                0.25.volts
                        )
                    )
            )
                .alongWith(
                    Lights.applyPatterns(
                        mutableListOf(
                            Pair("shooting", "intake left"),
                            Pair("shooting", "intake right")
                        )
                    )
                )
        )

        // alternate features
        OI.alternate
            .and(OI.zeroHood)
            .whileTrue(Hood.ZeroHoodCommand())
        OI.alternate
            .and(OI.hoodUp)
            .whileTrue(Hood.MoveHoodVoltageCommand(0.25.volts))
        OI.alternate
            .and(OI.hoodDown)
            .whileTrue(Hood.MoveHoodVoltageCommand((-0.25).volts))
//        OI.alternate
//            .and(OI.hoodTest)
//            .whileTrue(MoveHoodToAngle(
//                Hood.autoCalculateHood(),
//                0.25.volts
//            )
//                .alongWith(
//                    Shooter.ShootRPMCommand(200.RPM)
//                )
//        )
    }

    /**
     * Class for the operator interface
     * getting inputs from controllers and whatnot.
     */
    object OI : SubsystemBase() {
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

            init { addRequirements(OI) }

            override fun initialize() {
                timer.restart(); controller.setRumble(rumbleSide, rumblePower)
            }

            override fun execute() {
                controller.setRumble(rumbleSide, rumblePower)
            }

            override fun end(interrupted: Boolean) {
                controller.setRumble(rumbleSide, 0.0)
            }

            override fun isFinished(): Boolean {
                return timer.hasElapsed(time)
            }
        }

        /**
         * Input devices go here
         */
        val driverController = CommandXboxController(0)
        val operatorController = CommandXboxController(1)
//        val leftJoystick = CommandJoystick(1)
//        val rightJoystick = CommandJoystick(2)

        /**
         * Values for inputs go here
         */
        //===== DRIVETRAIN =====//
            val driverX get() = -driverController.leftX.processInput()
            val driverY get() = -driverController.leftY.processInput()
            val driverOmega get() = -driverController.rightX.processInput()
            val slowMode get() = driverController.leftTrigger()
            val driveMode get() = driverController.rightTrigger()
//            val pointHub get() = driverController.rightBumper()
//            val hubAlign get() = driverController.povUp()
//            val resetOdometry get() = driverController.back()
//            val leftTrenchAlign get() = driverController.povLeft()
//            val rightTrenchAlign get() = driverController.povRight()
//            val driverX get() = leftJoystick.x.processInput()
//            val driverY get() = leftJoystick.y.processInput()
//            val driverOmega get() = rightJoystick.x.processInput()
//            val slowMode get() = leftJoystick.trigger()
//            val driveMode get() = rightJoystick.trigger()
//            val pointHub get() = leftJoystick.button(0) // todo figure out a button
        //===== SUBSYSTEMS =====//
            // intake
            val runIntake get() = operatorController.a()
            val runOuttake get() = operatorController.y()
            val intakeOut get() = operatorController.rightTrigger()
            val intakeIn get() = operatorController.rightBumper()
            // spindexer and shooter kicker
            val indexIn get() = operatorController.b()
            val indexOut get() = operatorController.x()
            // shooter
            val runShooter get() = operatorController.leftTrigger()
            // shooter hood
            val zeroHood get() = operatorController.povLeft()
            val hoodTest get() = operatorController.povRight()
            val hoodUp get() = operatorController.povUp()
            val hoodDown get() = operatorController.povDown()
            // all
            val alternate get() = operatorController.leftBumper()
        //==== CHILDMODE ====//
            val parentDrive get() = operatorController.leftY.processInput()
            val parentStrafe get() = operatorController.leftX.processInput()
            val parentOmega get() = operatorController.rightX.processInput()
            val toggleChild get() = operatorController.rightTrigger()
            val toggleSlow get() = operatorController.leftTrigger()
            val toggleFieldOriented get() = operatorController.leftBumper()
    }
}






































































































// uwu