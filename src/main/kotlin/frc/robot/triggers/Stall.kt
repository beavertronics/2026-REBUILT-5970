package frc.robot.triggers

import beaverlib.utils.Units.Electrical.amps
import edu.wpi.first.wpilibj2.command.button.Trigger
import frc.robot.Constants
import frc.robot.subsystems.Hopper
import frc.robot.subsystems.Intake
import frc.robot.subsystems.IntakeMover
import frc.robot.subsystems.Kicker
import frc.robot.subsystems.Shooter

object Stall {
    val intakeStall = Trigger { Intake.runIntakeMotor.outputCurrent.amps >= Constants.VORTEX_STALL }
    val moveIntakeStall = Trigger {
        IntakeMover.leftIntakeMotor.outputCurrent.amps >= Constants.k775_STALL
                ||
        IntakeMover.rightIntakeMotor.outputCurrent.amps >= Constants.k775_STALL
    }
    val hopperStall = Trigger { Hopper.hopperMotor.outputCurrent.amps >= Constants.NEO_STALL }
    val kickerStall = Trigger { Kicker.feedMotor.outputCurrent.amps >= Constants.k775_STALL }
    val shooterStall = Trigger { Shooter.krakenShooter.statorCurrent.valueAsDouble.amps >= Constants.KRAKEN_STALL }
}
