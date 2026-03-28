package frc.robot

import beaverlib.fieldmap.FieldMapREBUILTWelded
import beaverlib.utils.Units.Electrical.volts
import beaverlib.utils.Units.Linear.meters
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup
import edu.wpi.first.wpilibj2.command.ParallelRaceGroup
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup
import edu.wpi.first.wpilibj2.command.WaitCommand
import frc.robot.commands.general.MoveTo
import frc.robot.subsystems.Hood
import frc.robot.subsystems.Hopper
import frc.robot.subsystems.Kicker
import frc.robot.subsystems.Orchestrator
import frc.robot.subsystems.Shooter

object Autos {
    val scorePreload =
        Hood.ZeroHoodCommand()
            .andThen(
                ParallelRaceGroup(
                    ParallelCommandGroup(
                        Shooter.ShootVoltageCommand(6.0.volts),
                        WaitCommand(2.5)
                            .andThen(
                                ParallelCommandGroup(
                                    Hopper.RunHopperCommand(10.0.volts),
                                    Kicker.RunKickerCommand(9.0.volts)
                                )
                            )
                    ),
                    WaitCommand(15.0)
                )
            )

    val alignToHub =
        SequentialCommandGroup(
            MoveTo(
                Pose2d(
                    FieldMapREBUILTWelded.teamHub.center.x - 2.0.meters.asMeters,
                    FieldMapREBUILTWelded.teamHub.center.y,
                    Rotation2d()
                )
            )
        )

    val never_gonna_give_you_up =
        InstantCommand(
            { Orchestrator.loadnplay("orchestra/never_gonna_give_you_up.chrp") },
            Orchestrator
        )

    val silver_springs =
        InstantCommand(
            { Orchestrator.loadnplay("orchestra/silver_springs_fleetwood_mac.chrp") },
            Orchestrator
        )
}