package beaverlib.utils.Units.Electrical

@JvmInline
value class Resistance(val asOhms : Double) {
    // Basic Math
    operator fun plus(other: Resistance) = Resistance(asOhms + other.asOhms)
    operator fun minus(other: Resistance) = Resistance(asOhms - other.asOhms)
    operator fun times(factor: Double) = Resistance(asOhms * factor)
    operator fun div(factor: Double) = Resistance(asOhms / factor)
    operator fun unaryPlus() = this
    operator fun unaryMinus() = Resistance(-asOhms)
    operator fun compareTo(other: Resistance) = asOhms.compareTo(other.asOhms)
    override fun toString() = "$asOhms ohms"
    //Unit conversion math
    operator fun times(other: Current) = VoltageUnit(asOhms*other.asAmps)
}

//Constructors
val Number.milliOhms get() = Resistance(toDouble()/1000)
val Number.ohms get() = Resistance(toDouble())
val Number.kiloOhms get() = Resistance(toDouble()*1000)

//Destructors
val Resistance.asMilliOhms get() = asOhms*1000
val Resistance.asOhms get() = asOhms/1000