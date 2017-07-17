package org.tobi29.scapes.engine.utils

/*
/**
 * Converts the given number to an integer containing the IEEE 754
 * representation
 * @receiver The floating point number to convert
 * @return The data stored in an integer
 */
header fun Float.bits(): Int

/**
 * Converts the given number to an integer containing the IEEE 754
 * representation
 * @receiver The floating point number to convert
 * @return The data stored in an integer
 */
header fun Double.bits(): Long

/**
 * Converts the given integer to a floating point number using the IEEE 754
 * representation
 * @receiver The data bits to convert
 * @return The floating point containing the value encoded in the integer
 */
header fun Int.bitsToFloat(): Float

/**
 * Converts the given integer to a floating point number using the IEEE 754
 * representation
 * @receiver The data bits to convert
 * @return The floating point containing the value encoded in the integer
 */
header fun Long.bitsToDouble(): Double

header fun Int.toString(radix: Int): String
*/

fun Int.toString(radix: Int = 10,
                 length: Int): String =
        toString(radix).forceDigits(length)

/*
header fun Long.toString(radix: Int): String
*/

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
