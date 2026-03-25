package frc.robot.subsystems

import Engine.BeaverLight
import edu.wpi.first.wpilibj.LEDPattern
import edu.wpi.first.wpilibj.util.Color
import edu.wpi.first.wpilibj2.command.SubsystemBase

object Lights : SubsystemBase() {
    val lights = BeaverLight(
        0, // todo
        0 // todo
    )

    init {
        lights.defaultCommand = run { LEDPattern.solid(Color.kOrange) }
    }
}
