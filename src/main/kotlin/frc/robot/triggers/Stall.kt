package frc.robot.triggers

import beaverlib.utils.Units.Angular.RPM
import beaverlib.utils.Units.Angular.asRPM
import beaverlib.utils.Units.Angular.rotationsPerSecond
import beaverlib.utils.Units.Electrical.amps
import edu.wpi.first.wpilibj2.command.button.Trigger
import frc.robot.Constants
import frc.robot.subsystems.Hood
import frc.robot.subsystems.Hopper
import frc.robot.subsystems.Intake
import frc.robot.subsystems.IntakeArm
import frc.robot.subsystems.Kicker

object Stall {
    val intakeStall = Trigger {
        Intake.intakeMotor.outputCurrent.amps >= Constants.VORTEX_STALL
//                &&
//        Intake.intakeMotor.encoder.velocity.RPM.asRPM <= 0.0.RPM.asRPM // todo?
    }
    val intakeArmStall = Trigger {
        IntakeArm.leftIntakeMotor.outputCurrent.amps >= Constants.k775_STALL
                ||
        IntakeArm.rightIntakeMotor.outputCurrent.amps >= Constants.k775_STALL
    }
    val hopperStall = Trigger {
        Hopper.hopperMotor.outputCurrent.amps >= Constants.NEO_STALL
//                ||
//        Hopper.hopperMotor.encoder.velocity.RPM.asRPM >= 0.0.RPM.asRPM // todo?
    }
    val kickerStall = Trigger {
        Kicker.kickerMotor.outputCurrent.amps >= Constants.k775_STALL
//                &&
//        Kicker.kickerMotor.encoder.velocity.RPM.asRPM <= 0.0.RPM.asRPM // todo?
    }
    val hoodStall = Trigger { Hood.hoodMotor.outputCurrent.amps >= Constants.k775_STALL }
//    val shooterStall = Trigger { Shooter.krakenShooter.statorCurrent.valueAsDouble.amps >= Constants.KRAKEN_STALL }
}
