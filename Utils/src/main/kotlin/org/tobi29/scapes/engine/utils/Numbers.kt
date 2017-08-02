package org.tobi29.scapes.engine.utils

fun Int.toString(radix: Int = 10,
                 length: Int): String =
        toString(radix).forceDigits(length)

fun Long.toString(radix: Int = 10,
                  length: Int): String =
        toString(radix).forceDigits(length)

private fun String.forceDigits(length: Int,
                               zero: Char = '0'): String {
    val negative = getOrNull(0) == '-'
    val str = if (negative) substring(1) else this
    val output = str.prefixToLength(zero, length, length)
    return if (negative) "-$output" else output
}
