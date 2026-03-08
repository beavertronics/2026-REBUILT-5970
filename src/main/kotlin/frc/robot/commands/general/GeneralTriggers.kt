package frc.robot.commands.general

import edu.wpi.first.wpilibj2.command.button.Trigger
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.ShooterConstants

object GeneralTriggers {
    val rpmTrigger = Trigger { Shooter.currentRPM > ShooterConstants.MIN_RPM }
}