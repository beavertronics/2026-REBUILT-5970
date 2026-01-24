package Engine

import beaverlib.utils.Units.Angular.*
import com.revrobotics.RelativeEncoder

/**
 * @param positionConversionFactor Rotations of the encoder are multiplied by this value to get rotations of the objective
 */
class BeaverRelativeEncoder(private val encoder: RelativeEncoder, val startingPosition : AngleUnit = 0.0.radians, var positionConversionFactor: Double = 1.0) {
    val position get() = (encoder.position).rotations * positionConversionFactor
    val velocity : AngularVelocity get() = (encoder.velocity * positionConversionFactor).rotationsPerSecond
    fun resetPosition(newPostion : AngleUnit = 0.0.radians) {
        encoder.setPosition(newPostion.asRotations/positionConversionFactor)
    }
    init {
        resetPosition(startingPosition)
    }
}