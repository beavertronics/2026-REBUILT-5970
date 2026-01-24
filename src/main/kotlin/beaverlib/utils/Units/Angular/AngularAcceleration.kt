package beaverlib.utils.Units.Angular

import beaverlib.utils.Sugar.TAU
import beaverlib.utils.Units.Linear.Acceleration
import beaverlib.utils.Units.Linear.DistanceUnit
import beaverlib.utils.Units.Time

class AngularAcceleration (val asRadiansPerSecondSquared: Double) {
    //Basic Math
    operator fun plus(other : AngularAcceleration) = AngularAcceleration(asRadiansPerSecondSquared + other.asRadiansPerSecondSquared)
    operator fun minus(other : AngularAcceleration) = AngularAcceleration(asRadiansPerSecondSquared - other.asRadiansPerSecondSquared)
    operator fun times(factor : Double) = AngularAcceleration(asRadiansPerSecondSquared * factor)
    operator fun div(factor: Double) = AngularAcceleration(asRadiansPerSecondSquared / factor)

    operator fun rem(other: AngularAcceleration) = AngularAcceleration(asRadiansPerSecondSquared % other.asRadiansPerSecondSquared)
    operator fun unaryPlus() = this
    operator fun unaryMinus() = AngularAcceleration(-asRadiansPerSecondSquared)
    // Unit conversion math
    operator fun times(other: Time) = AngularVelocity(asRadiansPerSecondSquared / other.asSeconds)
    operator fun times(other: DistanceUnit) = Acceleration(asRadiansPerSecondSquared * other.asMeters)

}
// Constructors
inline val Number.radiansPerSecondSquared get() = AngularAcceleration(this.toDouble())
inline val Number.rotationsPerSecondSquared get() = AngularAcceleration(this.toDouble()*TAU)
inline val Number.RPMSquared get() = AngularAcceleration((this.toDouble()*TAU)/60)
// Destructors

inline val AngularAcceleration.asRotationsPerSecondSquared get() = asRadiansPerSecondSquared / TAU
inline val AngularAcceleration.asRPMSquared get() = ((asRadiansPerSecondSquared / TAU) * 60)