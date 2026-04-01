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
                    Color.kOrangeRed
                ).scrollAtAbsoluteSpeed(Units.MetersPerSecond.of(0.5), density),
        "intaking" to
                LEDPattern.gradient(
                    LEDPattern.GradientType.kDiscontinuous,
                    Color.kBlack,
                    Color.kGreen
                ).scrollAtAbsoluteSpeed(Units.MetersPerSecond.of(1.5), density),
        "outtaking" to
                LEDPattern.gradient(
                    LEDPattern.GradientType.kDiscontinuous,
                    Color.kBlack,
                    Color.kRed
                ).scrollAtAbsoluteSpeed(Units.MetersPerSecond.of(-1.5), density),
        "shooting" to
                LEDPattern.gradient(
                    LEDPattern.GradientType.kDiscontinuous,
                    Color.kBlack,
                    Color.kBlue
                ).blink(Units.Seconds.of(0.25), Units.Seconds.of(0.25))
    )

    /**
     * A command that sets the pattern for the chosen light buffer.
     * @param name the name of the pattern.
     * @param buffer the buffer to apply the pattern to.
     */
    fun setPattern(name: String, buffer: String) : Command {
        return run { lights.applyTo(buffer, patterns[name]) }//.ignoringDisable(true)
    }

    /**
     * A command that sets the pattern for multiple buffer views at once.
     * @param patterns a dictionary containing the name of the pattern and the name of the buffer.
     */
    fun setPattern(patternsIn: MutableList<Pair<String, String>>) : Command {
        return run {
            for (pairing in patternsIn) {
                lights.applyTo(
                    pairing.second,
                    patterns[pairing.first]
                )
            }
        }//.ignoringDisable(true)
    }

    /**
     * A command that sets the pattern for multiple buffer views at once.
     * @param patterns a dictionary containing the LED pattern and the name of the buffer.
     */
    @JvmName("AAAAAAAAAAAAA")
    fun setPattern(patternsIn: MutableList<Pair<LEDPattern, String>>) : Command {
        return run {
            for (pairing in patternsIn) {
                lights.applyTo(
                    pairing.second,
                    pairing.first
                )
            }
        }//.ignoringDisable(true)
    }

    init {
        // register buffers for left and right side (mirrored to be matching)
        lights.registerBuffer(
            "intake left",
            18,
            34,
            true
        )
        lights.registerBuffer(
            "intake right",
            0,
            17,
            false
        )
    }
}
