package frc.robot.commands.subsystems.triggers

import edu.wpi.first.wpilibj2.command.button.Trigger
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.ShooterConstants

object Triggers {
    val rpmTrigger = Trigger { Shooter.currentRPM > ShooterConstants.MIN_RPM }
}