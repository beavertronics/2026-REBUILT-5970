package beaverlib.utils
// File adapted from 2898's bpsrobotics engine
import beaverlib.utils.Units.Frequency
import beaverlib.utils.Units.hertz
import kotlin.reflect.KProperty

@Suppress("NOTHING_TO_INLINE")
class MovingAverage(val size: Int) {
    private val buffer = DoubleArray(size)
    private var i = 0

    var average = 0.0
        private set
    val median get() = buffer.sorted()[buffer.size / 2]

    fun add(v: Double) {
        buffer[i++ % buffer.size] = v
        average = buffer.average()
    }

    inline operator fun getValue(thisRef: Any?, property: KProperty<*>): Double = average
}