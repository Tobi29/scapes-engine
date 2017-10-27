@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

actual inline fun Int.toString(radix: Int): String =
        java.lang.Integer.toString(this, radix)

actual inline fun Long.toString(radix: Int): String =
        java.lang.Long.toString(this, radix)
