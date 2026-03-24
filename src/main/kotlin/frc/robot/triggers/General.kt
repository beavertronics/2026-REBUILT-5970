package frc.robot.triggers

import beaverlib.utils.Units.Angular.asRPM
import edu.wpi.first.wpilibj2.command.button.Trigger
import frc.robot.subsystems.Hood
import frc.robot.subsystems.Shooter
import frc.robot.subsystems.ShooterConstants
import frc.robot.subsystems.Vision
import kotlin.math.abs

object General {
    /**
     * A trigger for when the shooter is at its target RPM.
     * Specifically, when the difference between the current and target RPM is less than the set minimum.
     * @see Shooter.currentRPM
     * @see Shooter.targetRPM
     * @see ShooterConstants.MAX_RPM_DIFF
     */
    val rpmTrigger = Trigger {
        abs(Shooter.currentRPM.asRPM - Shooter.targetRPM.asRPM) <= ShooterConstants.MAX_RPM_DIFF.asRPM
    }

    /**
     * A trigger for when the hood has pressed the lower limit switch.
     */
    val hoodDownTrigger = Trigger { Hood.lowerLimitSwitch.get() }

    /**
     * A trigger for whether we have sight on an april tag or not.
     */
    val cameraEmptyTrigger = Trigger { Vision.cameras.isEmpty() }
}