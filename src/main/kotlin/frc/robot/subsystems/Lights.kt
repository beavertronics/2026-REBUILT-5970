package frc.robot.subsystems

import Engine.BeaverLight
import edu.wpi.first.units.Units
import edu.wpi.first.wpilibj.AddressableLED
import edu.wpi.first.wpilibj.LEDPattern
import edu.wpi.first.wpilibj.util.Color

object Lights : BeaverLight(
    0,
    35,
    AddressableLED.ColorOrder.kRGB
) {

    // how many LEDs there are per meter
    val density = Units.Meters.of(1 / 60.0)

    // create patterns
    override val patterns: MutableMap<String, LEDPattern> = mutableMapOf(
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

    init {

        // register buffers for left and right side (mirrored to be matching)
        registerBuffer(
            "intake left",
            18,
            34,
            true
        )
        registerBuffer(
            "intake right",
            0,
            17,
            false
        )
    }
}
