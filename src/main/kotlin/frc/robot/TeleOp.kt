package frc.robot

import kotlin.math.*
import beaverlib.utils.Sugar.within
import beaverlib.utils.Units.Angular.RPM
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Electrical.volts
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup
import edu.wpi.first.wpilibj2.command.SubsystemBase
import edu.wpi.first.wpilibj2.command.WaitUntilCommand
import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import frc.robot.commands.drive.ChildModeDriveCommand
import frc.robot.commands.drive.TeleopDriveCommand
import frc.robot.commands.general.MoveTo
import frc.robot.commands.general.RotateTo
import frc.robot.commands.vision.MoveHoodToAngle
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.Hood
import frc.robot.subsystems.Intake
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.Hopper
import frc.robot.subsystems.IntakeArm
import frc.robot.subsystems.Kicker
import frc.robot.subsystems.Lights
import frc.robot.subsystems.Odometry
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

    val childDrive: ChildModeDriveCommand =
        ChildModeDriveCommand(
            { OI.driverY },
            { OI.driverX },
            { OI.driverOmega },
            { OI.toggleChild.asBoolean },
            { OI.childDrive },
            { OI.childStrafe },
            { OI.childOmega },
            { OI.toggleFieldOriented.asBoolean },
            { OI.toggleSlow.asBoolean }
        )

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
        // hopper
        Hopper.defaultCommand = Hopper.RunHopperCommand(0.0.volts)
        // shooter feed
        Kicker.defaultCommand = Kicker.RunKickerCommand(0.0.volts)
        // shooter
        Shooter.defaultCommand = Shooter.ShootRPMCommand()
    }

    /**
     * configures things to run on specific inputs
     */
    fun configureBindings() {
        //===== DRIVETRAIN =====//
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
        // safe is false due to the absence of limit switches
        OI.intakeIn.whileTrue(IntakeArm.MoveIntakeCommand(5.0.volts, false))
        OI.intakeOut.whileTrue(IntakeArm.MoveIntakeCommand((-5.0).volts, false))

        // spindexer and shooter kicker independent controls
        OI.indexIn.whileTrue(
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
            // moves hood to angle
//            MoveHoodToAngle(
////                Odometry.getApproxHoodAngle().asDegrees.degrees
//                0.0.degrees,
//                voltage = 0.5.volts
//            // with timeout, along with,
//            ).withTimeout(5.0).alongWith(
                // sets target RPM
                InstantCommand({
//                    Shooter.targetRPM = Odometry.getApproxFlywheelRPM()
                    Shooter.targetRPM = 6000.0.RPM
                // and then,
                }, Shooter).andThen(
                    // runs shooter flywheel to target RPM
                    Shooter.ShootRPMCommand(6000.0.RPM).alongWith(
                        // while rotating to face the hub
//                        RotateTo ({
//                            Odometry.getRotationToHub()
//                            // and then,
//                        }).andThen(
                            // one General.rmpTrigger,
                            WaitUntilCommand(General.rpmTrigger).andThen(
                                // run the hopper
                                Hopper.RunHopperCommand(9.0.volts).alongWith(
                                    // while running the kicker
                                    Kicker.RunKickerCommand(12.0.volts)
                                )
                            )
                        ),
                        // while running the LEDs
                        Lights.applyPatterns(mutableListOf(
                            Pair("shooting", "intake left"),
                            Pair("shooting", "intake right")
                        )
                        )
                    )
                )
//            )
//        )

        // alternate features
        OI.alternate
            .and(OI.zeroHood)
            .whileTrue(Hood.ZeroHoodCommand())
        OI.alternate
            .and(OI.hoodUp)
            .whileTrue(Hood.MoveHoodVoltageCommand(0.3.volts))
        OI.alternate
            .and(OI.hoodDown)
            .whileTrue(Hood.MoveHoodVoltageCommand((-0.3).volts))
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
            val childDrive get() = -operatorController.leftY.processInput()
            val childStrafe get() = -operatorController.leftX.processInput()
            val childOmega get() = -operatorController.rightX.processInput()
            val toggleChild get() = driverController.rightTrigger()
            val toggleSlow get() = driverController.leftTrigger()
            val toggleFieldOriented get() = driverController.leftBumper()
    }
}






































































































// uwu