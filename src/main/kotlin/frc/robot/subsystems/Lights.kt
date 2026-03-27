package frc.robot.subsystems

import Engine.BeaverLight
import edu.wpi.first.wpilibj.LEDPattern
import edu.wpi.first.wpilibj.util.Color
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.SubsystemBase

object Lights : SubsystemBase() {
    val lights = BeaverLight(
        0,
        35
    )

    init {

        // register buffer
//        lights.registerBuffer(
//            "Intake left",
//            19,
//            34,
//            true
//        )
//        lights.registerBuffer(
//            "Intake right",
//            0,
//            17,
//            false
//        )

        // set them to orange
//        val pattern = LEDPattern.solid(Color.kOrange)
//        lights.applyTo("Intake left", pattern)
//        lights.applyTo("Intake right", pattern)
    }
}
