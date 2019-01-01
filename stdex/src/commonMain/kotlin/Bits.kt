/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.stdex

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

/**
 * Rotates the bits [other] times to the left
 */
expect infix fun Int.lrot(other: Int): Int

/**
 * Rotates the bits [other] times to the right
 */
expect infix fun Int.rrot(other: Int): Int

/**
 * Rotates the bits [other] times to the left
 */
expect infix fun Long.lrot(other: Int): Long

/**
 * Rotates the bits [other] times to the right
 */
expect infix fun Long.rrot(other: Int): Long

/**
 * Counts the amount of leading zeros
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clz(value: Byte): Int = clz(value.toInt() and 0xFF)

/**
 * Counts the amount of trailing zeros
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun ctz(value: Byte): Int = ctz(value.toInt() and 0xFF) - 24

/**
 * Counts the amount of set bits
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun popcount(value: Byte): Int = popcount(value.toInt() and 0xFF)

/**
 * Counts the amount of leading zeros
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun clz(value: Short): Int = clz(value.toInt() and 0xFFFF)

/**
 * Counts the amount of trailing zeros
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun ctz(value: Short): Int = ctz(value.toInt() and 0xFFFF) - 16

/**
 * Counts the amount of set bits
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun popcount(value: Short): Int = popcount(value.toInt() and 0xFFFF)

/**
 * Counts the amount of leading zeros
 */
expect fun clz(value: Int): Int

/**
 * Counts the amount of trailing zeros
 */
expect fun ctz(value: Int): Int

/**
 * Counts the amount of set bits
 */
expect fun popcount(value: Int): Int

/**
 * Counts the amount of leading zeros
 */
expect fun clz(value: Long): Int

/**
 * Counts the amount of trailing zeros
 */
expect fun ctz(value: Long): Int

/**
 * Counts the amount of set bits
 */
expect fun popcount(value: Long): Int

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun bitMaskB(index: Int): Byte = bitMaskI(index).toByte()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun bitMaskS(index: Int): Short = bitMaskI(index).toShort()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun bitMaskI(index: Int): Int = 1 shl index

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun bitMaskL(index: Int): Long = 1L shl index

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.maskAll(mask: Byte): Boolean = this and mask == mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.maskAll(mask: Short): Boolean = this and mask == mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.maskAll(mask: Int): Boolean = this and mask == mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.maskAll(mask: Long): Boolean = this and mask == mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.maskAny(mask: Byte): Boolean = this and mask != 0.toByte()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.maskAny(mask: Short): Boolean = this and mask != 0.toShort()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.maskAny(mask: Int): Boolean = this and mask != 0

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.maskAny(mask: Long): Boolean = this and mask != 0L

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.maskAt(index: Int): Boolean = maskAll(bitMaskB(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.maskAt(index: Int): Boolean = maskAll(bitMaskS(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.maskAt(index: Int): Boolean = maskAll(bitMaskI(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.maskAt(index: Int): Boolean = maskAll(bitMaskL(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.set(mask: Byte): Byte = this or mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.set(mask: Short): Short = this or mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.set(mask: Int): Int = this or mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.set(mask: Long): Long = this or mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.setAt(index: Int): Byte = set(bitMaskB(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.setAt(index: Int): Short = set(bitMaskS(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.setAt(index: Int): Int = set(bitMaskI(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.setAt(index: Int): Long = set(bitMaskL(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.unset(mask: Byte): Byte = this and mask.inv()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.unset(mask: Short): Short = this and mask.inv()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.unset(mask: Int): Int = this and mask.inv()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.unset(mask: Long): Long = this and mask.inv()

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.unsetAt(index: Int): Byte = unset(bitMaskB(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.unsetAt(index: Int): Short = unset(bitMaskS(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.unsetAt(index: Int): Int = unset(bitMaskI(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.unsetAt(index: Int): Long = unset(bitMaskL(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.toggle(mask: Byte): Byte = this xor mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.toggle(mask: Short): Short = this xor mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.toggle(mask: Int): Int = this xor mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.toggle(mask: Long): Long = this xor mask

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.toggleAt(index: Int): Byte = toggle(bitMaskB(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.toggleAt(index: Int): Short = toggle(bitMaskS(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.toggleAt(index: Int): Int = toggle(bitMaskI(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.toggleAt(index: Int): Long = toggle(bitMaskL(index))

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.set(mask: Byte, value: Boolean): Byte =
    if (value) set(mask) else unset(mask)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.set(mask: Short, value: Boolean): Short =
    if (value) set(mask) else unset(mask)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.set(mask: Int, value: Boolean): Int =
    if (value) set(mask) else unset(mask)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.set(mask: Long, value: Boolean): Long =
    if (value) set(mask) else unset(mask)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.setAt(index: Int, value: Boolean): Byte =
    if (value) setAt(index) else unsetAt(index)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.setAt(index: Int, value: Boolean): Short =
    if (value) setAt(index) else unsetAt(index)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.setAt(index: Int, value: Boolean): Int =
    if (value) setAt(index) else unsetAt(index)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.setAt(index: Int, value: Boolean): Long =
    if (value) setAt(index) else unsetAt(index)

/**
 * Splits the given number into bytes, going from high bytes to low
 * @param output Called once with split bytes
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <R> Short.splitToBytes(output: (Byte, Byte) -> R): R =
    toInt().let { s ->
        output(
            (s ushr 8).toByte(),
            (s ushr 0).toByte()
        )
    }

/**
 * Combines the given bytes into a number, going from high bytes to low
 * @param b1 1st byte (if big-endian)
 * @param b0 2nd byte (if big-endian)
 * @return Combined number
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun combineToShort(b1: Byte, b0: Byte): Short =
    ((b1.toInt() and 0xFF shl 8) or
            (b0.toInt() and 0xFF shl 0)).toShort()

/**
 * Splits the given number into bytes, going from high bytes to low
 * @param output Called once with split bytes
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <R> Int.splitToBytes(output: (Byte, Byte, Byte, Byte) -> R): R =
    let { i ->
        output(
            (i ushr 24).toByte(),
            (i ushr 16).toByte(),
            (i ushr 8).toByte(),
            (i ushr 0).toByte()
        )
    }

/**
 * Splits the given number into bytes, going from high shorts to low
 * @param output Called once with split shorts
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <R> Int.splitToShorts(output: (Short, Short) -> R): R =
    let { l ->
        output(
            (l ushr 16).toShort(),
            (l ushr 0).toShort()
        )
    }

/**
 * Combines the given bytes into a number, going from high bytes to low
 * @param b3 1st byte (if big-endian)
 * @param b2 2nd byte (if big-endian)
 * @param b1 3rd byte (if big-endian)
 * @param b0 4th byte (if big-endian)
 * @return Combined number
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun combineToInt(b3: Byte, b2: Byte, b1: Byte, b0: Byte): Int =
    (b3.toInt() and 0xFF shl 24) or
            (b2.toInt() and 0xFF shl 16) or
            (b1.toInt() and 0xFF shl 8) or
            (b0.toInt() and 0xFF shl 0)

/**
 * Combines the given shorts into a number, going from high bytes to low
 * @param s1 1st short (if big-endian)
 * @param s0 2nd short (if big-endian)
 * @return Combined number
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun combineToInt(s1: Short, s0: Short): Int =
    (s1.toInt() and 0xFFFF shl 16) or
            (s0.toInt() and 0xFFFF shl 0)

/**
 * Splits the given number into bytes, going from high bytes to low
 * @param output Called once with split bytes
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
expect fun <R> Long.splitToBytes(output: (Byte, Byte, Byte, Byte, Byte, Byte, Byte, Byte) -> R): R

/**
 * Splits the given number into bytes, going from high shorts to low
 * @param output Called once with split shorts
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
expect fun <R> Long.splitToShorts(output: (Short, Short, Short, Short) -> R): R

/**
 * Splits the given number into bytes, going from high ints to low
 * @param output Called once with split ints
 * @param R Return type
 * @receiver Number to split
 * @return Return value of [output]
 */
expect inline fun <R> Long.splitToInts(output: (Int, Int) -> R): R

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
expect fun combineToLong(
    b7: Byte,
    b6: Byte,
    b5: Byte,
    b4: Byte,
    b3: Byte,
    b2: Byte,
    b1: Byte,
    b0: Byte
): Long

/**
 * Combines the given shorts into a number, going from high bytes to low
 * @param s3 1st short (if big-endian)
 * @param s2 2nd short (if big-endian)
 * @param s1 3rd short (if big-endian)
 * @param s0 4th short (if big-endian)
 * @return Combined number
 */
expect fun combineToLong(s3: Short, s2: Short, s1: Short, s0: Short): Long

/**
 * Combines the given ints into a number, going from high bytes to low
 * @param i1 1st int (if big-endian)
 * @param i0 2nd int (if big-endian)
 * @return Combined number
 */
expect fun combineToLong(i1: Int, i0: Int): Long
