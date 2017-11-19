/*
 * Copyright 2012-2017 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            output((s ushr 8).toByte(),
                    (s ushr 0).toByte())
        }

/**
 * Combines the given bytes into a number, going from high bytes to low
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
            output((i ushr 24).toByte(),
                    (i ushr 16).toByte(),
                    (i ushr 8).toByte(),
                    (i ushr 0).toByte())
        }

/**
 * Splits the given number into bytes, going from high shorts to low
 * @param output Called once with split shorts
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
inline fun <R> Int.splitToShorts(output: (Short, Short) -> R): R =
        let { l ->
            output((l ushr 16).toShort(),
                    (l ushr 0).toShort())
        }

/**
 * Combines the given bytes into a number, going from high bytes to low
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
 * Combines the given shorts into a number, going from high bytes to low
 * @param s1 1st short (if big-endian)
 * @param s9 2nd short (if big-endian)
 * @return Combined number
 */
inline fun combineToInt(s1: Short,
                        s0: Short): Int =
        (s1.toInt() and 0xFFFF shl 16) or
                (s0.toInt() and 0xFFFF shl 0)

/**
 * Splits the given number into bytes, going from high bytes to low
 * @param output Called once with split bytes
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
inline fun <R> Long.splitToBytes(output: (Byte, Byte, Byte, Byte, Byte, Byte, Byte, Byte) -> R): R =
        let { l ->
            output((l ushr 56).toByte(),
                    (l ushr 48).toByte(),
                    (l ushr 40).toByte(),
                    (l ushr 32).toByte(),
                    (l ushr 24).toByte(),
                    (l ushr 16).toByte(),
                    (l ushr 8).toByte(),
                    (l ushr 0).toByte())
        }

/**
 * Splits the given number into bytes, going from high shorts to low
 * @param output Called once with split shorts
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
inline fun <R> Long.splitToShorts(output: (Short, Short, Short, Short) -> R): R =
        let { l ->
            output((l ushr 48).toShort(),
                    (l ushr 32).toShort(),
                    (l ushr 16).toShort(),
                    (l ushr 0).toShort())
        }

/**
 * Splits the given number into bytes, going from high ints to low
 * @param output Called once with split ints
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
inline fun <R> Long.splitToInts(output: (Int, Int) -> R): R =
        let { l ->
            output((l ushr 32).toInt(),
                    (l ushr 0).toInt())
        }

/**
 * Combines the given bytes into a number, going from high bytes to low
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

/**
 * Combines the given shorts into a number, going from high bytes to low
 * @param s3 1st short (if big-endian)
 * @param s2 2nd short (if big-endian)
 * @param s1 3rd short (if big-endian)
 * @param s0 4th short (if big-endian)
 * @return Combined number
 */
inline fun combineToLong(s3: Short,
                         s2: Short,
                         s1: Short,
                         s0: Short): Long =
        (s3.toLong() and 0xFFFF shl 48) or
                (s2.toLong() and 0xFFFF shl 32) or
                (s1.toLong() and 0xFFFF shl 16) or
                (s0.toLong() and 0xFFFF shl 0)

/**
 * Combines the given ints into a number, going from high bytes to low
 * @param i1 1st int (if big-endian)
 * @param i0 2nd int (if big-endian)
 * @return Combined number
 */
inline fun combineToLong(i1: Int,
                         i0: Int): Long =
        (i1.toLong() and 0xFFFFFFFF shl 32) or
                (i0.toLong() and 0xFFFFFFFF shl 0)

inline fun Boolean.hashCodePrimitive(): Int = if (this) 1231 else 1237

inline fun Byte.hashCodePrimitive(): Int = this.toInt()

inline fun Short.hashCodePrimitive(): Int = this.toInt()

inline fun Char.hashCodePrimitive(): Int = this.toInt()

inline fun Int.hashCodePrimitive(): Int = this

inline fun Float.hashCodePrimitive(): Int = toBits()

inline fun Long.hashCodePrimitive(): Int = (this xor this.ushr(32)).toInt()

inline fun Double.hashCodePrimitive(): Int = toBits().hashCodePrimitive()

private fun String.forceDigits(length: Int,
                               zero: Char = '0'): String {
    val negative = getOrNull(0) == '-'
    val str = if (negative) substring(1) else this
    val output = str.prefixToLength(zero, length, length)
    return if (negative) "-$output" else output
}
