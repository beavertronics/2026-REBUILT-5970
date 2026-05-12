package frc.robot

import beaverlib.fieldmap.FieldMapREBUILTWelded
import beaverlib.utils.Units.Angular.RPM
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Electrical.volts
import beaverlib.utils.Units.Linear.meters
import com.revrobotics.spark.SparkMaxAlternateEncoder
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup
import edu.wpi.first.wpilibj2.command.ParallelRaceGroup
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup
import edu.wpi.first.wpilibj2.command.WaitCommand
import edu.wpi.first.wpilibj2.command.button.Trigger
import frc.robot.commands.general.MoveTo
import frc.robot.subsystems.Hood
import frc.robot.subsystems.Hopper
import frc.robot.subsystems.Kicker
import frc.robot.subsystems.Orchestrator
import frc.robot.subsystems.Shooter
import frc.robot.triggers.General

/**
 * All pre-made commands and autos that can be used.
 */
object Autos {
    /**
     * A command that runs for 17.5 seconds to score preloads.
     */
    val scorePreload =
        // zero hood
        Hood.ZeroHoodCommand()
            // then wait for shooter to spool up for 2.5 seconds
            .andThen(
                ParallelRaceGroup(
                    ParallelCommandGroup(
                        Shooter.ShootVoltageCommand(6.0.volts),
                        WaitCommand(2.5)
                            // then feed shooter
                            .andThen(
                                ParallelCommandGroup(
                                    Hopper.RunHopperCommand(10.0.volts),
                                    Kicker.RunKickerCommand(9.0.volts)
                                )
                            )
                    ),
                    // full thing ends after 17.5 seconds
                    WaitCommand(17.5)               )
            )

    /**
     * A command that has predefined inputs for the alignment to the hub.
     * It will account for side of the field.
     */
//    val alignToHub =
//        MoveTo(
//            Pose2d(
//                // X VALUE
//                if (FieldMapREBUILTWelded.getTeamAllianceArea() == FieldMapREBUILTWelded.AllianceArea.Blue) {
//                    FieldMapREBUILTWelded.teamHub.center.x - 2.0.meters.asMeters
//                }
//                else {
//                    FieldMapREBUILTWelded.teamHub.center.x + 2.0.meters.asMeters
//                }
//                ,
//
//                // Y VALUE
//                FieldMapREBUILTWelded.teamHub.center.y,
//
//                // ROTATION
//                if (FieldMapREBUILTWelded.getTeamAllianceArea() == FieldMapREBUILTWelded.AllianceArea.Blue) {
//                    Rotation2d()
//                }
//                else {
//                    Rotation2d(180.0.degrees.asRadians)
//                }
//            )
//        )

    /**
     * A command to play never gonna give you up using the TalonFX motors.
     */
    val never_gonna_give_you_up =
        InstantCommand(
            { Orchestrator.loadnplay("orchestra/never_gonna_give_you_up.chrp") },
            Orchestrator
        )

    /**
     * A command to play silver springs by using the TalonFX motors.
     */
    val silver_springs =
        InstantCommand(
            { Orchestrator.loadnplay("orchestra/silver_springs_fleetwood_mac.chrp") },
            Orchestrator
        )
}