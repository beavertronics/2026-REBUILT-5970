package frc.robot.commands.general

import beaverlib.utils.Units.Angular.asRPM
import edu.wpi.first.wpilibj2.command.button.Trigger
import frc.robot.subsystems.Hood
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.ShooterConstants

object GeneralTriggers {
    val rpmTrigger = Trigger { Shooter.currentRPM.asRPM >= ShooterConstants.MIN_RPM.asRPM }
    val hoodDownTrigger = Trigger { Hood.lowerLimitSwitch.get() }
}