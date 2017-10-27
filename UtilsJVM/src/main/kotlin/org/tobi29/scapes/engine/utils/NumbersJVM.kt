@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

actual inline fun Float.bits(): Int =
        java.lang.Float.floatToRawIntBits(this)

actual inline fun Double.bits(): Long =
        java.lang.Double.doubleToRawLongBits(this)

actual inline fun Int.bitsToFloat(): Float =
        java.lang.Float.intBitsToFloat(this)

actual inline fun Long.bitsToDouble(): Double =
        java.lang.Double.longBitsToDouble(this)

actual inline fun Int.toString(radix: Int): String =
        java.lang.Integer.toString(this, radix)

actual inline fun Long.toString(radix: Int): String =
        java.lang.Long.toString(this, radix)
