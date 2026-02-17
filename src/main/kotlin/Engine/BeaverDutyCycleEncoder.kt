package Engine

import beaverlib.utils.Units.Angular.AngleUnit
import beaverlib.utils.Units.Angular.radians
import beaverlib.utils.Units.Angular.radiansPerSecond
import beaverlib.utils.Units.seconds
import edu.wpi.first.wpilibj.DutyCycleEncoder
import edu.wpi.first.wpilibj.Timer
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.sign

/*
Objective - The actual subsystem the encoder is attached to, eg: arm
lower-bound - the lowest angle of the objective
 */
/**
 * A duty cycle encoder wrapper to allow the encoder to be set during runtime, as well as allowing a gear ratio before the objective.
 * This is necessary to ensure if the objective was rotated asynchronously of the encoder, the subsystem will still function as intended.
 * For this solution, two offsets are used to ensure that encoder wrapping is not an issue in the arm's range of motion.
 * The first offset [encoderOffset] is set during runtime, it will adjust the encoder value such that when the encoder is at the lower bound, the value is zero
 * The gear ratio is then applied (The encoder value is multiplied by [ratio])
 * Then, the [armOffset] is added to the value, so that the resulting value is the actual rotation of the objective.
 * !! WARNING: If the the range of motion is greater than the ratio * 2PI this solution will NOT WORK and needs to be replaced with a relative encoder
 *
 * @param encoderOffset The offset of the encoder, this should be set at runtime when the lower limit switch is hit.
 * @param ratio the gear ratio between the encoder and the objective. This should be set so that when the encoder value is multiplied by it, the objective rotation is resulted.
 * @param armOffset The value to be added to the encoder, this should be the angle of the objective at the lower bound. (Applied AFTER ratio)
 *
 */
class BeaverDutyCycleEncoder(channel : Int,
                             val ratio : Double = 1.0,
                             var encoderOffset : AngleUnit = 0.0.radians,
                             var armOffset : AngleUnit = 0.0.radians // todo rename to something less specific
                            ) : DutyCycleEncoder(channel, 2* PI, 0.0) {
    companion object {
        inline val AngleUnit.standardPosition: AngleUnit
            get() =
                if (this.asRadians >= 0.0) { AngleUnit(this.asRadians % (2 * PI)) }
                else { AngleUnit((2 * PI) + (this.asRadians % (2 * PI))) }
        fun AngleUnit.angleDistanceTo(other: AngleUnit): AngleUnit {
            val normal = this - other
            val wrap = -((2 * PI).radians * normal.asRadians.sign - normal)
//            println("normal: ${normal} wrap: ${wrap} sign: ${normal.sign} angleA: $angleA angleB: $angleB")
            return if (normal.asRadians.absoluteValue < wrap.asRadians.absoluteValue) { normal }
                    else { wrap }
        }
    }

    /** Gives the encoders position WITHOUT applying [ratio] or [armOffset] */
    val encoderPosition: AngleUnit get() = (get().radians + encoderOffset).standardPosition

    /** Gives a value that should correspond to the objectives rotation */
    val position: AngleUnit get() = (encoderPosition * ratio) + armOffset
    private val rateTimer = Timer()
    private var lastPosition = 0.0.radians
    var rate = 0.0.radiansPerSecond

    init {
        rateTimer.restart()
    }

    /**
     * Run every frame to update the encoders rate
     */
    fun updateRate() {
        rate = lastPosition.angleDistanceTo(position) / rateTimer.get().seconds
        lastPosition = position
        rateTimer.restart()
    }
    /** Resets the encoder such that the position it is at is now [armOffset]*/
    fun reset() {
        encoderOffset = -get().radians
    }
}