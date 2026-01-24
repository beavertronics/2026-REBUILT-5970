package beaverlib.utils.Units

import beaverlib.utils.Units.Linear.Acceleration
import beaverlib.utils.Units.Linear.earthGravity

@JvmInline
value class Force (val asNewtons: Double) {
    operator fun plus(other : Force) = Force(asNewtons + other.asNewtons)
    operator fun minus(other : Force) = Force(asNewtons - other.asNewtons)
    operator fun times(factor : Double) = Force(asNewtons * factor.toDouble())
    operator fun div(factor: Double) = Force(asNewtons / factor.toDouble())
    operator fun div(factor : Acceleration) = Weight(asNewtons / factor.asMetersPerSecondSquared)
    operator fun div(factor : Weight) = Acceleration(asNewtons / factor.asKilograms)
    operator fun rem(other: Weight) = Force(asNewtons % other.asKilograms)
    operator fun unaryPlus() = this
    operator fun unaryMinus() = Force(-asNewtons)
}
// Constructors
inline val Number.newtons get() = Force(toDouble())
inline val Number.lbF     get() = Force((toDouble()* 0.453592) * earthGravity.asMetersPerSecondSquared)

// Destructors

inline val Force.asLbF     get() = Force((asNewtons / 0.453592) / earthGravity.asMetersPerSecondSquared)
