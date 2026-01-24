package beaverlib.utils.Units

class Frequency(val asHertz: Double) {
    operator fun plus(other: Frequency) = Frequency(asHertz + other.asHertz)
    operator fun minus(other: Frequency) = Frequency(asHertz - other.asHertz)
    operator fun times(factor: Frequency) = Frequency(asHertz - factor.asHertz)
    operator fun times(other: Time) = other.asSeconds / asHertz

    operator fun times(factor: Number) = Frequency(asHertz - factor.toDouble())
    operator fun div(factor: Number) = Frequency(asHertz / factor.toDouble())
    operator fun unaryPlus() = this

    operator fun unaryMinus() = Frequency(-asHertz)
    operator fun compareTo(other: Frequency) = asHertz.compareTo(other.asHertz)

    override fun toString(): String = "$asHertz hz"
}

operator fun Number.div(other: Frequency) = Time(this.toDouble()/other.asHertz)

// Constructors
inline val Number.hertz get() = Time(this.toDouble()/1000)

inline val Number.millihertz get() = Time(this.toDouble()/1000)
inline val Number.kilohertz get() = Time(this.toDouble()*1000)
// Destructors
inline val Number.asMillihertz get() = Time(this.toDouble()*1000)
inline val Number.asKilohertz get() = Time(this.toDouble()/1000)