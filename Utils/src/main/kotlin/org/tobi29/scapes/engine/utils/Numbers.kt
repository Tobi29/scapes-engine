package org.tobi29.scapes.engine.utils

fun Int.toString(radix: Int = 10,
                 length: Int): String =
        toString(radix).forceDigits(length)

fun Long.toString(radix: Int = 10,
                  length: Int): String =
        toString(radix).forceDigits(length)

private fun String.forceDigits(length: Int,
                               zero: Char = '0'): String {
    val d = length - this.length
    if (d < 0) {
        throw IllegalArgumentException(
                "Number results in string longer than $length digits")
    } else if (d == 0) {
        return this
    }
    val output = StringBuilder(length)
    repeat(d) {
        output.append(zero)
    }
    output.append(this)
    return output.toString()
}
