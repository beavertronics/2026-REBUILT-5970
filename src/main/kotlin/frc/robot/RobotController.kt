package frc.robot

import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Transform2d
import edu.wpi.first.wpilibj.TimedRobot
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import edu.wpi.first.wpilibj2.command.Commands
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup
import frc.robot.commands.general.Move
import frc.robot.commands.vision.AlignToTag
import frc.robot.commands.tests.Wait
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.Phatplanner
import frc.robot.subsystems.`according to all known laws of aviation, our robot should not be able to fly`

/*
 Main code for controlling the robot. Mainly just links everything together.

 Driver control is defined in TeleOp.kt.
*/

/**
 * main object for controlling robot, based off
 * of the timed robot class
 */
object RobotController : TimedRobot() {
    val commandScheduler = CommandScheduler.getInstance()
//    val manualAutoCommands: Map<String,Command> = mapOf()
    var selectedManualAuto: Command? = null
    val ManualAutoChooser = SendableChooser<Command>()
    val AutoTypeChooser = SendableChooser<Boolean>()
    var selectedPathAuto: Command? = null

    /**
     * runs when robot turns on, should be used for any initialization of robot or subsystems
     */
    override fun robotInit() {
        // all subsystems
        TeleOp
        `according to all known laws of aviation, our robot should not be able to fly`
        Phatplanner

        // start drive cam // todo replaced by vision feed
        // CameraServer.startAutomaticCapture(0)

        // load manual autos
        ManualAutoChooser.setDefaultOption("no auto", Commands.none())
        ManualAutoChooser.addOption("Align to multiple tags",
            SequentialCommandGroup(
                    AlignToTag(2),
                    Wait(2.0),
                    AlignToTag(3),
                    Wait(2.0),
                    AlignToTag(4)
                )
        )
        ManualAutoChooser.addOption("1 meter square",
            SequentialCommandGroup(
                Move(Transform2d(1.0, 0.0, Rotation2d(0.0, 0.0)))))
        ManualAutoChooser.addOption("Drive Sys ID",
            Drivetrain.sysIdDriveMotor()
        )
        ManualAutoChooser.addOption("Angle Sys ID",
            Drivetrain.sysIdAngleMotorCommand()
        )
        SmartDashboard.putData("Autos/Manual auto choices", ManualAutoChooser)

        // load pathplanner autos
        Phatplanner.autoChooser.setDefaultOption("no auto", Commands.none())
        SmartDashboard.putData("Autos/Pathplanner auto choices", Phatplanner.autoChooser)

        // make thing to choose between pathplanner and manual autos
        AutoTypeChooser.setDefaultOption("Default - Manual", true)
        AutoTypeChooser.addOption("Pathplanner", true)
        AutoTypeChooser.addOption("Manual", false)
        SmartDashboard.putData("Autos/Auto chooser", AutoTypeChooser)

    }

    /**
     * runs when the robot is on, regardless of enabled or not
     * used for telemetry, command scheduler, etc
     */
    override fun robotPeriodic() { commandScheduler.run() }

    override fun autonomousInit() {
        if (!AutoTypeChooser.selected) {
            println("Using manual auto")
            selectedManualAuto = ManualAutoChooser.selected
            commandScheduler.schedule(selectedManualAuto)
            println("Auto selected: " + selectedManualAuto)
        }
        else {
            println("using pathplanner auto")
            selectedPathAuto = Phatplanner.getAutonomousCommand()
            commandScheduler.schedule(selectedPathAuto)
            println("Auto selected: " + selectedPathAuto)
        }
    }
    override fun autonomousPeriodic() {} //TODO: Unnecesary with command-based programming?

    /**
     * runs when teleop is ready
     */
    override fun teleopInit() {
        TeleOp.configureBindings()
        if (!AutoTypeChooser.selected && selectedManualAuto != null) { selectedManualAuto?.cancel() }
        else if (AutoTypeChooser.selected && selectedPathAuto != null) { selectedPathAuto?.cancel() }
    }

    /**
     * runs on every frame of teleop
     */
    override fun teleopPeriodic() {} //TODO: Unnecessary with command-based programming?

    /**
     * runs only in simulation mode,
     * other functions will run regardless of whether the robot is
     * simulated or not
     */
    override fun simulationInit() {}

    /**
     * runs immediately when the robot is disabled, helpful for safe
     * deactivation of robot and whatnot
     */
    override fun disabledInit() {}

    /**
     * runs while robot is disabled, used to hold motors
     * in place.
     * try not to put code here, is often unsafe
     */
    override fun disabledPeriodic() {}

    override fun testInit() { commandScheduler.cancelAll() }

    override fun testPeriodic() {}
}