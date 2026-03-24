package frc.robot

import beaverlib.utils.Units.Electrical.amps
import beaverlib.utils.Units.Electrical.volts
import frc.robot.subsystems.Shooter

object Constants {
    val MAX_VOLTS = 12.0.volts
    val NEO_STALL = 105.0.amps
    val VORTEX_STALL = 211.0.amps
    val k775_STALL = 130.0.amps
    val KRAKEN_STALL = Shooter.krakenShooter.motorStallCurrent.valueAsDouble.amps
}
