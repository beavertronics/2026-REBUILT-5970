package frc.robot.commands.general

import beaverlib.utils.Units.Angular.asRPM
import edu.wpi.first.wpilibj2.command.button.Trigger
import frc.robot.subsystems.Hood
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.ShooterConstants
import kotlin.math.abs

object GeneralTriggers {
    /**
     * A trigger for when the shooter is at its target RPM.
     * Specifically, when the difference between the current and target RPM is less than 5.0 RPM.
     * @see ShooterConstants.MAX_RPM_DIFF
     */
    val rpmTrigger = Trigger {
        abs(Shooter.currentRPM.asRPM - Shooter.targetRPM.asRPM) <= ShooterConstants.MAX_RPM_DIFF.asRPM
    }

    /**
     * A trigger for when the hood has pressed the limit switch.
     */
    val hoodDownTrigger = Trigger { Hood.lowerLimitSwitch.get() }
}