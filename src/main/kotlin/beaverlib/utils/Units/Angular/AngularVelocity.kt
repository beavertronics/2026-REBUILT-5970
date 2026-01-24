package beaverlib.utils.Units.Angular

import beaverlib.utils.Sugar.TAU
import beaverlib.utils.Units.Linear.DistanceUnit
import beaverlib.utils.Units.Linear.VelocityUnit
import beaverlib.utils.Units.Time

class AngularVelocity (val asRadiansPerSecond: Double) {
    //Basic Math
    operator fun plus(other : AngularVelocity) = AngularAcceleration(asRadiansPerSecond + other.asRadiansPerSecond)
    operator fun minus(other : AngularVelocity) = AngularAcceleration(asRadiansPerSecond - other.asRadiansPerSecond)
    operator fun times(factor : Number) = AngularAcceleration(asRadiansPerSecond * factor.toDouble())
    operator fun div(factor: Number) = AngularAcceleration(asRadiansPerSecond / factor.toDouble())

    operator fun rem(other: AngularVelocity) = AngularVelocity(asRadiansPerSecond % other.asRadiansPerSecond)
    operator fun unaryPlus() = this
    operator fun unaryMinus() = AngularVelocity(-asRadiansPerSecond)
    // Unit conversion math
    operator fun times(other: Time) = AngleUnit(asRadiansPerSecond * other.asSeconds)
    operator fun div(other: Time) = AngularAcceleration(asRadiansPerSecond / other.asSeconds)

    operator fun times(other: DistanceUnit) = VelocityUnit(asRadiansPerSecond * other.asMeters)
}
// Constructors
inline val Number.radiansPerSecond get() = AngularVelocity(this.toDouble())
inline val Number.rotationsPerSecond get() = AngularVelocity(this.toDouble()*TAU)
inline val Number.RPM get() = AngularVelocity((this.toDouble()*TAU)/60)

// Destructors
inline val AngularVelocity.asRotationsPerSecond get() = asRadiansPerSecond / TAU
inline val AngularVelocity.asRPM get() = ((asRadiansPerSecond / TAU) * 60)