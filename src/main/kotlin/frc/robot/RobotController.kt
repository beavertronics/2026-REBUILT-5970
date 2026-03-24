package frc.robot

import beaverlib.fieldmap.FieldMapREBUILTWelded
import beaverlib.utils.Units.Linear.meters
import com.ctre.phoenix6.SignalLogger
import com.ctre.phoenix6.hardware.TalonFX
import com.revrobotics.util.StatusLogger
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.wpilibj.TimedRobot
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import edu.wpi.first.wpilibj2.command.Commands
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup
import frc.robot.commands.general.MoveTo
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.Hood
import frc.robot.subsystems.Hopper
import frc.robot.subsystems.Intake
import frc.robot.subsystems.Kicker
import frc.robot.subsystems.Orchestrator
import frc.robot.subsystems.Phatplanner
import frc.robot.subsystems.Shooter
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
    var selectedManualAuto: Command? = null
    val ManualAutoChooser = SendableChooser<Command>()
    val AutoTypeChooser = SendableChooser<Boolean>()
    var selectedPathAuto: Command? = null

    /**
     * runs when robot turns on, should be used for any initialization of robot or subsystems
     */
    override fun robotInit() {
        // logging things
        if (isSimulation()) { SignalLogger.enableAutoLogging(true) }
        else { SignalLogger.enableAutoLogging(false); StatusLogger.disableAutoLogging() }

        // all subsystems
        TeleOp
        Drivetrain
        `according to all known laws of aviation, our robot should not be able to fly`
        Hood
        Hopper
        Intake
        Shooter
        Kicker

        // add all TalonFX motors to orchestrator
        println("ORCHESTRA: Shooter motor added (" +
                Shooter.krakenShooter.deviceID +
                "): " + Orchestrator.register(Shooter.krakenShooter)
        )
        Drivetrain.swerveDrive.modules.forEach {
            val driveMotor = it.driveMotor.motor as TalonFX
            println("ORCHESTRA: Drive motor added (" +
                    driveMotor.deviceID +
                    "): " + Orchestrator.register(driveMotor)
            )
        }

        // start drive cam // todo replaced by vision feed
        // CameraServer.startAutomaticCapture(0)

        // load manual autos
        ManualAutoChooser.setDefaultOption("no auto", Commands.none())
        ManualAutoChooser.addOption("Align to hub",
            SequentialCommandGroup(
                MoveTo(
                    Pose2d(
                        FieldMapREBUILTWelded.teamHub.center.x - 2.0.meters.asMeters,
                        FieldMapREBUILTWelded.teamHub.center.y,
                        Rotation2d()
                    )
                )
            )
        )
        ManualAutoChooser.addOption("Drive Sys ID",
            Drivetrain.sysIdDriveMotor()
        )
        ManualAutoChooser.addOption("Angle Sys ID",
            Drivetrain.sysIdAngleMotorCommand()
        )
//        ManualAutoChooser.addOption("Shooter quasistatic reverse (forwards)",
//            Shooter.shooterSysID()[2]
//            )
//        ManualAutoChooser.addOption("Shooter quasistatic forwards (reverse)",
//            Shooter.shooterSysID()[3]
//            )
//        ManualAutoChooser.addOption("Shooter dynamic reverse (forwards)",
//            Shooter.shooterSysID()[0]
//            )
//        ManualAutoChooser.addOption("Shooter dynamic forwards (reverse)",
//            Shooter.shooterSysID()[1]
//            )

        ManualAutoChooser.addOption("Orchestra - Never Gonna Give You Up by Rick Astley",
            InstantCommand( { Orchestrator.loadnplay("orchestra/never_gonna_give_you_up.chrp")}, Orchestrator)
        )
        ManualAutoChooser.addOption("Orchestra - Silver Springs by Fleetwood Mac",
            InstantCommand( { Orchestrator.loadnplay("orchestra/silver_springs_fleetwood_mac.chrp")}, Orchestrator)
        )
        SmartDashboard.putData("Autos/Manual auto choices", ManualAutoChooser)

        // load pathplanner autos
        Phatplanner.autoChooser.setDefaultOption("no auto", Commands.none())
        SmartDashboard.putData("Autos/Pathplanner auto choices", Phatplanner.autoChooser)

        // make thing to choose between pathplanner and manual autos
        AutoTypeChooser.setDefaultOption("Default - Manual", false)
        AutoTypeChooser.addOption("Manual", false)
        AutoTypeChooser.addOption("Pathplanner", true)
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

    /**
     * runs when teleop is ready
     */
    override fun teleopInit() {
        TeleOp.configureBindings()
        if (!AutoTypeChooser.selected && selectedManualAuto != null) { selectedManualAuto?.cancel() }
        else if (AutoTypeChooser.selected && selectedPathAuto != null) { selectedPathAuto?.cancel() }
        Orchestrator.stop()
    }

    override fun testInit() { commandScheduler.cancelAll() }
}