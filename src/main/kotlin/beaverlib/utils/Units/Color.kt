package beaverlib.utils.Units

@JvmInline
value class RGBA(val packed: UInt) {
    constructor(r: UByte, g: UByte, b: UByte, a: UByte = 255.toUByte()) : this(r.toUInt() or (g.toUInt() shl 8) or (b.toUInt() shl 16) or (a.toUInt() shl 24))

    constructor(r: Int, g: Int, b: Int) : this(r.toUByte(), g.toUByte(), b.toUByte())

    val r get() = packed.toUByte()
    val g get() = (packed shr 8).toUByte()
    val b get() = (packed shr 16).toUByte()
    val a get() = (packed shr 24).toUByte()
}

@JvmInline
value class HSVA(val packed: UInt) {
    /**
     * s, b, and a are from 0 to 255, h is from 0 to 180
     */
    constructor(h: Int, s: UByte, b: UByte, a: UByte = 255.toUByte()) : this(h.toUInt() or (s.toUInt() shl 8) or (b.toUInt() shl 16) or (a.toUInt() shl 24))

    /**
     * s, b, and a are from 0 to 255, h is from 0 to 180
     */
    constructor(h: Int, s: Int, v: Int) : this(h, s.toUByte(), v.toUByte())

    /** 0 to 180 */
    val h get() = packed.toUByte()
    val s get() = (packed shr 8).toUByte()
    val v get() = (packed shr 16).toUByte()
    val a get() = (packed shr 24).toUByte()
}

@Suppress("EXPERIMENTAL_API_USAGE", "FINAL_UPPER_BOUND")
@kotlin.ExperimentalUnsignedTypes
@JvmInline
value class RGBAArray(val array: UIntArray) {
    constructor(size: Int) : this(UIntArray(size))

    companion object {
        // this is dumb, but inline classes can't be varargs, so we trick it into having them be objects
        fun <T : RGBA> of(vararg colors: T): RGBAArray {
            return RGBAArray(UIntArray(colors.size) { colors[it].packed })
        }
    }

    val size get() = array.size
    val indices get() = array.indices

    operator fun get(index: Int) = RGBA(array[index])

    operator fun set(index: Int, value: RGBA) {
        array[index] = value.packed
    }

    fun fill(value: RGBA, fromIndex: Int = 0, toIndex: Int = size - 1) {
        array.fill(value.packed, fromIndex, toIndex)
    }
}