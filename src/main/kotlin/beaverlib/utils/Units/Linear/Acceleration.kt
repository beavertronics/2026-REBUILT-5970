package beaverlib.utils.Units.Linear

import beaverlib.utils.Units.Time

@JvmInline
value class Acceleration (val asMetersPerSecondSquared: Double) {
    operator fun plus(other: Acceleration) = Acceleration(asMetersPerSecondSquared + other.asMetersPerSecondSquared)
    operator fun minus(other: Acceleration) = Acceleration(asMetersPerSecondSquared - other.asMetersPerSecondSquared)
    operator fun times(factor: Double) = Acceleration(asMetersPerSecondSquared * factor)
    operator fun times(factor: Time) = VelocityUnit(asMetersPerSecondSquared * factor.asSeconds)
    operator fun div(factor: Double) = Acceleration(asMetersPerSecondSquared / factor)
    operator fun unaryPlus() = this
    operator fun unaryMinus() = Acceleration(-asMetersPerSecondSquared)
    operator fun compareTo(other: Acceleration) = asMetersPerSecondSquared.compareTo(other.asMetersPerSecondSquared)
    override fun toString() = "$asMetersPerSecondSquared m/s"
}

// Constructors
inline val Number.metersPerSecondSquared get() = Acceleration(this.toDouble())
inline val Number.`Mps^2` get() = this.metersPerSecondSquared

inline val Number.feetPerSecondSquared get() = Acceleration(this.toDouble() * 0.3048)

// Destructors
inline val Acceleration.asFeetPerSecondSquared get() = asMetersPerSecondSquared / 0.3048

val earthGravity = Acceleration(9.80665)