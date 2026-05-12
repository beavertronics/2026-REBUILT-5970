package Engine

import edu.wpi.first.wpilibj.AddressableLED
import edu.wpi.first.wpilibj.AddressableLEDBuffer
import edu.wpi.first.wpilibj.AddressableLEDBufferView
import edu.wpi.first.wpilibj.LEDPattern
import edu.wpi.first.wpilibj.util.Color
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import kotlin.math.roundToInt

/**
 * A wrapper for a color for custom color values.
 * @param red the red value.
 * @param green the green value.
 * @param blue the blue value.
 * @param alpha the brightness value.
 */
fun BeaverColor(red: Int, green: Int, blue: Int, alpha: Double = 1.0): Color {
    return Color((green * alpha).roundToInt(), (red * alpha).roundToInt(), (blue * alpha).roundToInt())
}

/**
 * A class to manage a strip of LED lights. This class must be sub-classed in order to be used.
 * @param port the port that the LED strip is plugged into on the RIO. Note that the RIO can only manage one strip of LEDs at a time.
 * @param length the total length of the strip of LEDs.
 * @param order the color order for R, G, and B. Refer to your purchased LEDs for this information.
 */
open class BeaverLight(
    port: Int,
    length: Int,
    order: AddressableLED.ColorOrder
) : SubsystemBase() {

    // create the LED, the total length of the LED, and each of the segments (for different parts)
    val led: AddressableLED = AddressableLED(port)
    val buffer: AddressableLEDBuffer = AddressableLEDBuffer(length)
    val segments: MutableMap<String, AddressableLEDBufferView> = mutableMapOf()

    /**
     * A mutable map that must be overriden with your patterns.
     */
    open val patterns: MutableMap<String, LEDPattern> = mutableMapOf()

    // set the length (which takes a little) and start the lights, default to lights off
    init {
        led.setLength(buffer.length)
        led.setColorOrder(order)
        led.start()
        defaultCommand = getCommand(LEDPattern.solid(Color.kBlack))
    }

    /**
     * A function that returns a command to make set as the default command.
     * @param pattern the new pattern to be the default.
     * @see setDefaultCommand
     */
    fun getCommand(pattern: LEDPattern?) : Command {
        return run {
            pattern?.applyTo(buffer)
        }
            .ignoringDisable(true)
    }

    /**
     * A function that registers a new buffer. A "buffer" is a section of the lights. This is designed so that you can
     * address different sections of lights individually (for example, on different subsystems) so that you can do different things.
     * @param name The name of the buffer.
     * @param lower the starting LED in the buffer.
     * @param upper the ending LED in the buffer.
     * @param reversed whether to invert the lights. This would be used if you wanted to mirror lights on the other side of the robot.
     */
    fun registerBuffer(name: String, lower: Int, upper: Int, reversed: Boolean = false) {
        val view = buffer.createView(lower, upper)
        if (reversed) { segments[name] = view.reversed() }
        else { segments[name] = view }
    }

    /**
     * A function that applies a pattern to a chosen buffer.
     * @param buffer The name of the buffer.
     * @param pattern the LED pattern to apply to the buffer.
     */
    fun applyToBuffer(buffer: String, pattern: LEDPattern?) {
        pattern?.applyTo(segments[buffer])
    }

    /**
     * A command that sets the pattern for the chosen light buffer.
     * @param name the name of the pattern.
     * @param buffer the buffer to apply the pattern to.
     */
    fun applyPattern(name: String, buffer: String) : Command {
        return run { applyToBuffer(buffer, patterns[name]) }.ignoringDisable(true) // todo test
    }

    /**
     * A command that sets the pattern for multiple buffer views at once.
     * @param patterns a dictionary containing the name of the pattern and the name of the buffer.
     */
    fun applyPatterns(patternsIn: MutableList<Pair<String, String>>) : Command {
        return run {
            for (pairing in patternsIn) {
                applyToBuffer(
                    pairing.second,
                    patterns[pairing.first]
                )
            }
        }.ignoringDisable(true) // todo test
    }

    /**
     * A command that sets the pattern for multiple buffer views at once.
     * @param patterns a dictionary containing the LED pattern and the name of the buffer.
     */
    @JvmName("Legally dinstinct")
    fun applyPatterns(patternsIn: MutableList<Pair<LEDPattern, String>>) : Command {
        return run {
            for (pairing in patternsIn) {
                applyToBuffer(
                    pairing.second,
                    pairing.first
                )
            }
        }.ignoringDisable(true) // todo test
    }

    // runs every frame updating the lights
    override fun periodic() {
        led.setData(buffer)
    }
}
