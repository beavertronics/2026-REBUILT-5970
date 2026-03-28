package frc.robot

import beaverlib.fieldmap.FieldMapREBUILTWelded
import beaverlib.utils.Units.Electrical.volts
import beaverlib.utils.Units.Linear.meters
import com.ctre.phoenix6.SignalLogger
import com.ctre.phoenix6.hardware.TalonFX
import com.revrobotics.spark.SparkMaxAlternateEncoder
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
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup
import edu.wpi.first.wpilibj2.command.ParallelRaceGroup
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup
import edu.wpi.first.wpilibj2.command.WaitCommand
import frc.robot.commands.general.MoveTo
import frc.robot.subsystems.Drivetrain
import frc.robot.subsystems.Hood
import frc.robot.subsystems.Hopper
import frc.robot.subsystems.Intake
import frc.robot.subsystems.Kicker
import frc.robot.subsystems.Lights
//import frc.robot.subsystems.Lights
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
        Orchestrator
        Lights

        // add all TalonFX motors to orchestrator
//        println("ORCHESTRA: Shooter motor added (" +
//                Shooter.krakenShooter.deviceID +
//                "): " + Orchestrator.register(Shooter.krakenShooter)
//        )
//        Drivetrain.swerveDrive.modules.forEach {
//            val driveMotor = it.driveMotor.motor as TalonFX
//            println("ORCHESTRA: Drive motor added (" +
//                    driveMotor.deviceID +
//                    "): " + Orchestrator.register(driveMotor)
//            )
//        }

        // start drive cam // todo replaced by vision feed
        // CameraServer.startAutomaticCapture(0)

        /////////// MANUAL ///////////
        ManualAutoChooser.setDefaultOption("no auto", Commands.none())
        ManualAutoChooser.addOption(
            "Align to hub",
            Autos.alignToHub
        )

        ManualAutoChooser.addOption(
            "Shoot preload",
            Autos.scorePreload
        )
        ManualAutoChooser.addOption(
            "Drive Sys ID",
            Drivetrain.sysIdDriveMotor()
        )
        ManualAutoChooser.addOption(
            "Angle Sys ID",
            Drivetrain.sysIdAngleMotorCommand()
        )

        ManualAutoChooser.addOption(
            "Orchestra - Never Gonna Give You Up by Rick Astley",
            Autos.never_gonna_give_you_up
        )
        ManualAutoChooser.addOption(
            "Orchestra - Silver Springs by Fleetwood Mac",
            Autos.silver_springs
        )

        /////////// PATHPLANNER ///////////
        Phatplanner.autoChooser.setDefaultOption("no auto", Commands.none())

        /////////// TYPE CHOOSER ///////////
        AutoTypeChooser.setDefaultOption("Default - Manual", false)
        AutoTypeChooser.addOption("Manual", false)
        AutoTypeChooser.addOption("Pathplanner", true)

        // PUTTING EVERYTHING ON DASHBOARD
        SmartDashboard.putData("Autos/Manual auto choices", ManualAutoChooser)
        SmartDashboard.putData("Autos/Pathplanner auto choices", Phatplanner.autoChooser)
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