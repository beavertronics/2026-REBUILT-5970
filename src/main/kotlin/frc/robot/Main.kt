package frc.robot

import edu.wpi.first.hal.FRCNetComm
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.RobotBase

/*
Go to RobotController.kt unless you know what you're doing.

All this file does is tell the VM to run the robot code in RobotController.kt
You're not supposed to run other code here.
*/

object Main {
    @JvmStatic
    fun main(args: Array<String>) = RobotBase.startRobot {
        HAL.report(FRCNetComm.tResourceType.kResourceType_Language,FRCNetComm.tInstances.kLanguage_Kotlin)
            RobotController
    }
}