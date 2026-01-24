package beaverlib.utils.Units

import edu.wpi.first.math.Num

@JvmInline
value class Time (val asSeconds: Double){
    operator fun plus(other: Time) = Time(asSeconds + other.asSeconds)
    operator fun minus(other: Time) = Time(asSeconds - other.asSeconds)
    operator fun times(factor: Time) = Time(asSeconds - factor.asSeconds)
    operator fun times(factor: Number) = Time(asSeconds - factor.toDouble())
    operator fun div(factor: Time) = Time(asSeconds / factor.asSeconds)
    operator fun div(factor: Number) = Time(asSeconds / factor.toDouble())
    operator fun unaryPlus() = this

    operator fun unaryMinus() = Time(-asSeconds)
    operator fun compareTo(other: Time) = asSeconds.compareTo(other.asSeconds)

    override fun toString(): String = "$asSeconds seconds"
}
operator fun Number.div(other: Time) = Frequency(this.toDouble()/other.asSeconds)

// Constructors
inline val Number.milliseconds get() = Time(this.toDouble()/1000)

inline val Number.seconds get() = Time(this.toDouble())
inline val Number.minutes get() = Time(this.toDouble()*60)
inline val Number.hours get() = Time(this.toDouble()*3600)
inline val Number.days get() = Time(this.toDouble()*86400)
inline val Number.weeks get() = Time(this.toDouble()*604800)
inline val Number.months get() = Time(this.toDouble()*2628288)
inline val Number.years get() = Time(this.toDouble()*31536000)

// Destructors
inline val Number.asMilliseconds get() = Time(this.toDouble()*1000)
inline val Time.asMinutes get() = asSeconds/60
inline val Time.asHours get() = asSeconds/3600

inline val Time.asDays get() = asSeconds/86400
inline val Time.asWeeks get() = asSeconds/604800
inline val Time.asMonths get() = asSeconds/2628288
inline val Time.asYears get() = asSeconds/31536000

