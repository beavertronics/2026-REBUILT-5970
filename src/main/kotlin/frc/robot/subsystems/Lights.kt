package frc.robot.subsystems

import Engine.BeaverLight
import edu.wpi.first.units.Units
import edu.wpi.first.wpilibj.AddressableLED
import edu.wpi.first.wpilibj.LEDPattern
import edu.wpi.first.wpilibj.util.Color
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase

object Lights : SubsystemBase() {
    val lights = BeaverLight(
        0,
        35,
        AddressableLED.ColorOrder.kRGB
    )
    // how many LEDs there are per meter
    val density = Units.Meters.of(1 / 60.0)
    // different patterns
    val patterns: MutableMap<String, LEDPattern> = mutableMapOf(
        "system ready" to
                LEDPattern.gradient(
                    LEDPattern.GradientType.kDiscontinuous,
                    Color.kBlack,
                    Color.kOrange
                ).scrollAtAbsoluteSpeed(Units.MetersPerSecond.of(3.0), density),
        "intaking" to
                LEDPattern.gradient(
                    LEDPattern.GradientType.kDiscontinuous,
                    Color.kBlack,
                    Color.kGreen
                ).scrollAtAbsoluteSpeed(Units.MetersPerSecond.of(3.0), density),
        "outtaking" to
                LEDPattern.gradient(
                    LEDPattern.GradientType.kDiscontinuous,
                    Color.kBlack,
                    Color.kRed
                ).scrollAtAbsoluteSpeed(Units.MetersPerSecond.of(3.0), density)
    )

    /**
     * A command that sets the pattern for the chosen light buffer.
     * @param name the name of the pattern.
     * @param buffer the buffer to apply the pattern to.
     */
    fun setPattern(name: String, buffer: String) : Command {
        return run { lights.applyTo(buffer, patterns[name]) }
    }

    init {

        // register buffers for left and right side (mirrored to be matching)
        lights.registerBuffer(
            "intake left",
            19,
            34,
            true
        )
        lights.registerBuffer(
            "intake right",
            0,
            17,
            false
        )

//        /**
//         * A command to set the pattern of multiple buffers at the same time.
//         * @param names the list of names of each pattern.
//         * @param buffers the buffers of each pattern.
//         *
//         * There should be  1:1 ratio of names to buffers.
//         */
//        fun setPatterns(names: List<String>, buffers: List<String>) : Command {
//            return run {}
//        }
    }
}
