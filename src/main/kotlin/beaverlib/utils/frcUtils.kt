package frc.engine.utils

import edu.wpi.first.wpilibj.DriverStation

object frcUtils {
    fun getAlliance() : Boolean {
        val alliance = DriverStation.getAlliance();
        if (alliance.isPresent) {
            return alliance.get() == DriverStation.Alliance.Red;
        }
        return false;
    }

}