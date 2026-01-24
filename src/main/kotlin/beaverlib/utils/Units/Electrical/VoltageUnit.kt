package beaverlib.utils.Units.Electrical

@JvmInline
value class VoltageUnit(val asVolts: Double) {
    // Basic Math
    operator fun plus(other: VoltageUnit) = VoltageUnit(asVolts + other.asVolts)
    operator fun minus(other: VoltageUnit) = VoltageUnit(asVolts - other.asVolts)
    operator fun times(factor: Double) = VoltageUnit(asVolts * factor)
    operator fun div(factor: Double) = VoltageUnit(asVolts / factor)
    operator fun unaryPlus() = this
    operator fun unaryMinus() = VoltageUnit(-asVolts)
    operator fun compareTo(other: VoltageUnit) = asVolts.compareTo(other.asVolts)
    override fun toString() = "$asVolts v"

    //Unit conversion math
    operator fun div(other: Current) = Resistance(asVolts / other.asAmps)
    operator fun div(other: Resistance) = Current(asVolts / other.asOhms)

}

//Constructors
val Number.milliVolts get() = VoltageUnit(toDouble()/1000)
val Number.volts get() = VoltageUnit(toDouble())

/**
 * WARNING: IF YOU ARE USING THIS YOU ARE PROBABLY GOING TO BLOW SOMETHING UP
 */
val Number.kiloVolts get() = VoltageUnit(toDouble()*1000)

//Destructors
val VoltageUnit.asMilliVolts get() = asVolts*1000
val VoltageUnit.asKilovolts get() = asVolts/1000

