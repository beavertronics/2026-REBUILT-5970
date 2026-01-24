package beaverlib.utils.Units.Linear

import beaverlib.utils.Units.Time
import kotlin.math.pow

// this is mine now from Mean Machine (2471)
@JvmInline
value class DistanceUnit(val asMeters: Double) {
    operator fun plus(other: DistanceUnit) = DistanceUnit(asMeters+other.asMeters)
    operator fun minus(other: DistanceUnit) = DistanceUnit(asMeters-other.asMeters)
    operator fun times(factor: Number) = DistanceUnit(asMeters*factor.toDouble())
    operator fun div(factor: Number) = DistanceUnit(asMeters/factor.toDouble())
    operator fun div(factor: Time) = VelocityUnit(asMeters/factor.asSeconds)
    operator fun rem(other: DistanceUnit) = DistanceUnit(asMeters % other.asMeters)

    operator fun unaryMinus() = DistanceUnit(-asMeters)

    operator fun unaryPlus() = this

    operator fun compareTo(other: DistanceUnit) = asMeters.compareTo(other.asMeters)

    override fun toString() = "$asInches inches"

}
// Constructor
inline val Number.meters get() = DistanceUnit(this.toDouble())
inline val Number.feet get() = DistanceUnit(this.toDouble() * 0.3048)
inline val Number.inches get() = DistanceUnit(this.toDouble() * 0.0254)
// Destructor

inline val DistanceUnit.asFeet get() = asMeters / 0.3048
inline val DistanceUnit.asInches get() = asMeters / 0.0254
