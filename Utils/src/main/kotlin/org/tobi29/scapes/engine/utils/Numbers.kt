@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

/**
 * Converts the given number to a string, forcing a certain number of digits,
 * throwing when too long
 * @param radix Radix for output format
 * @param length Number of digits to force (excluding sign)
 * @receiver Number to format
 * @throws IllegalArgumentException When number is too great
 * @return String with exact length of [length] or with leading dash when negative
 */
fun Int.toString(radix: Int = 10,
                 length: Int): String =
        toString(radix).forceDigits(length)

/**
 * Converts the given number to a string, forcing a certain number of digits,
 * throwing when too long
 * @param radix Radix for output format
 * @param length Number of digits to force (excluding sign)
 * @receiver Number to format
 * @throws IllegalArgumentException When number is too great
 * @return String with exact length of [length] or with leading dash when negative
 */
fun Long.toString(radix: Int = 10,
                  length: Int): String =
        toString(radix).forceDigits(length)

/**
 * Splits the given number into bytes, going from high bytes to low
 * @param output Called once with split bytes
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
inline fun <R> Short.splitToBytes(output: (Byte, Byte) -> R): R =
        toInt().let { s ->
            output((s ushr 8 and 0xFF).toByte(),
                    (s ushr 0 and 0xFF).toByte())
        }

/**
 * Combines the given byte into a number, going from high bytes to low
 * @param b1 1st byte (if big-endian)
 * @param b0 2nd byte (if big-endian)
 * @return Combined number
 */
inline fun combineToShort(b1: Byte,
                          b0: Byte): Short =
        ((b1.toInt() and 0xFF shl 8) or
                (b0.toInt() and 0xFF shl 0)).toShort()

/**
 * Splits the given number into bytes, going from high bytes to low
 * @param output Called once with split bytes
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
inline fun <R> Int.splitToBytes(output: (Byte, Byte, Byte, Byte) -> R): R =
        let { i ->
            output((i ushr 24 and 0xFF).toByte(),
                    (i ushr 16 and 0xFF).toByte(),
                    (i ushr 8 and 0xFF).toByte(),
                    (i ushr 0 and 0xFF).toByte())
        }

/**
 * Combines the given byte into a number, going from high bytes to low
 * @param b3 1st byte (if big-endian)
 * @param b2 2nd byte (if big-endian)
 * @param b1 3rd byte (if big-endian)
 * @param b0 4th byte (if big-endian)
 * @return Combined number
 */
inline fun combineToInt(b3: Byte,
                        b2: Byte,
                        b1: Byte,
                        b0: Byte): Int =
        (b3.toInt() and 0xFF shl 24) or
                (b2.toInt() and 0xFF shl 16) or
                (b1.toInt() and 0xFF shl 8) or
                (b0.toInt() and 0xFF shl 0)

/**
 * Splits the given number into bytes, going from high bytes to low
 * @param output Called once with split bytes
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
inline fun <R> Long.splitToBytes(output: (Byte, Byte, Byte, Byte, Byte, Byte, Byte, Byte) -> R): R =
        let { l ->
            output((l ushr 56 and 0xFF).toByte(),
                    (l ushr 48 and 0xFF).toByte(),
                    (l ushr 40 and 0xFF).toByte(),
                    (l ushr 32 and 0xFF).toByte(),
                    (l ushr 24 and 0xFF).toByte(),
                    (l ushr 16 and 0xFF).toByte(),
                    (l ushr 8 and 0xFF).toByte(),
                    (l ushr 0 and 0xFF).toByte())
        }

/**
 * Combines the given byte into a number, going from high bytes to low
 * @param b7 1st byte (if big-endian)
 * @param b6 2nd byte (if big-endian)
 * @param b5 3rd byte (if big-endian)
 * @param b4 4th byte (if big-endian)
 * @param b3 5th byte (if big-endian)
 * @param b2 6th byte (if big-endian)
 * @param b1 7th byte (if big-endian)
 * @param b0 8th byte (if big-endian)
 * @return Combined number
 */
inline fun combineToLong(b7: Byte,
                         b6: Byte,
                         b5: Byte,
                         b4: Byte,
                         b3: Byte,
                         b2: Byte,
                         b1: Byte,
                         b0: Byte): Long =
        (b7.toLong() and 0xFF shl 56) or
                (b6.toLong() and 0xFF shl 48) or
                (b5.toLong() and 0xFF shl 40) or
                (b4.toLong() and 0xFF shl 32) or
                (b3.toLong() and 0xFF shl 24) or
                (b2.toLong() and 0xFF shl 16) or
                (b1.toLong() and 0xFF shl 8) or
                (b0.toLong() and 0xFF shl 0)

private fun String.forceDigits(length: Int,
                               zero: Char = '0'): String {
    val negative = getOrNull(0) == '-'
    val str = if (negative) substring(1) else this
    val output = str.prefixToLength(zero, length, length)
    return if (negative) "-$output" else output
}
