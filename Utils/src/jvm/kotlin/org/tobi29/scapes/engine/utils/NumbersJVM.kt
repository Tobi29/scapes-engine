@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

/* impl */ inline fun Float.bits(): Int =
        java.lang.Float.floatToIntBits(this)

/* impl */ inline fun Double.bits(): Long =
        java.lang.Double.doubleToLongBits(this)

/* impl */ inline fun Int.bitsToFloat(): Float =
        java.lang.Float.intBitsToFloat(this)

/* impl */ inline fun Long.bitsToDouble(): Double =
        java.lang.Double.longBitsToDouble(this)

/* impl */ inline fun Int.toString(radix: Int): String =
        java.lang.Integer.toString(this, radix)

/* impl */ inline fun Long.toString(radix: Int): String =
        java.lang.Long.toString(this, radix)
