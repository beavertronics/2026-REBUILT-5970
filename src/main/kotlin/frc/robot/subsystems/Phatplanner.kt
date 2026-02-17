package frc.robot.subsystems//package frc.robot.subsystems

import com.pathplanner.lib.auto.AutoBuilder
import com.pathplanner.lib.commands.PathPlannerAuto
import com.pathplanner.lib.config.PIDConstants
import com.pathplanner.lib.config.RobotConfig
import com.pathplanner.lib.controllers.PPHolonomicDriveController
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.DriverStation.Alliance
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import java.util.stream.Stream


object Phatplanner : SubsystemBase() {
    val isCompetition = true
    lateinit var config: RobotConfig

    init {
        // All other subsystem initialization
        // ...
        // Load the RobotConfig from the GUI settings. You should probably
        // store this in your Constants file
        try {
            config = RobotConfig.fromGUISettings()
        } catch (e: Exception) {
            // Handle exception as needed
            e.printStackTrace()
        }
        // Configure AutoBuilder last
        AutoBuilder.configure(
            { `according to all known laws of aviation, our robot should not be able to fly`.pose },  // Robot pose supplier
            { newPose : Pose2d -> Drivetrain.swerveDrive.resetOdometry(newPose) },  // Method to reset odometry (will be called if your auto has a starting pose)
            { Drivetrain.swerveDrive.robotVelocity },  // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
            { speeds, feedforwards -> Drivetrain.drive(speeds) },  // Method that will drive the robot given ROBOT RELATIVE ChassisSpeeds. Also optionally outputs individual module feedforwards
            PPHolonomicDriveController( // PPHolonomicController is the built in path following controller for holonomic drive trains
                PIDConstants(5.0, 0.0, 0.0),  // Translation PID constants
                PIDConstants(5.0, 0.0, 0.0) // Rotation PID constants
            ),
            config,  // The robot configuration
            {
                // Boolean supplier that controls when the path will be mirrored for the red alliance
                // This will flip the path being followed to the red side of the field.
                // THE ORIGIN WILL REMAIN ON THE BLUE SIDE
                val alliance = DriverStation.getAlliance()
                if (alliance.isPresent) {
                    return@configure alliance.get() == Alliance.Red
                }
                false
            },
            Drivetrain // Reference to this subsystem to set requirements
        )
    }

    fun getAutonomousCommand(): Command { return autoChooser.selected }


    // Build an auto chooser. This will use Commands.none() as the default option.
    // As an example, this will only show autos that start with "comp" while at
    // competition as defined by the programmer
    val autoChooser: SendableChooser<Command> =
        AutoBuilder.buildAutoChooserWithOptionsModifier { stream: Stream<PathPlannerAuto> ->
            if (isCompetition) stream.filter { auto: PathPlannerAuto ->
                auto.name.startsWith("comp")
            }
            else stream
        }
}