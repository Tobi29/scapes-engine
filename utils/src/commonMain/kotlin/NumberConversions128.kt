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

// GENERATED FILE, DO NOT EDIT DIRECTLY!!!
// Generation script can be found in `resources/codegen/GenNumberConversions128.kts`.
// Run `resources/codegen/codegen.sh` to update sources.

package org.tobi29.utils

import org.tobi29.stdex.InlineUtility

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.toInt128Clamped(): Int128 = toInt128()

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Byte.toUInt128Clamped(): UInt128 = when {
    this <= UInt128.MIN_VALUE.toByte() -> UInt128.MIN_VALUE
    else -> toUInt128()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.toInt128Clamped(): Int128 = toInt128()

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Short.toUInt128Clamped(): UInt128 = when {
    this <= UInt128.MIN_VALUE.toShort() -> UInt128.MIN_VALUE
    else -> toUInt128()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.toInt128Clamped(): Int128 = toInt128()

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int.toUInt128Clamped(): UInt128 = when {
    this <= UInt128.MIN_VALUE.toInt() -> UInt128.MIN_VALUE
    else -> toUInt128()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.toInt128Clamped(): Int128 = toInt128()

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Long.toUInt128Clamped(): UInt128 = when {
    this <= UInt128.MIN_VALUE.toLong() -> UInt128.MIN_VALUE
    else -> toUInt128()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int128.toByteClamped(): Byte = when {
    this <= Byte.MIN_VALUE.toInt128() -> Byte.MIN_VALUE
    this >= Byte.MAX_VALUE.toInt128() -> Byte.MAX_VALUE
    else -> toByte()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int128.toShortClamped(): Short = when {
    this <= Short.MIN_VALUE.toInt128() -> Short.MIN_VALUE
    this >= Short.MAX_VALUE.toInt128() -> Short.MAX_VALUE
    else -> toShort()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int128.toIntClamped(): Int = when {
    this <= Int.MIN_VALUE.toInt128() -> Int.MIN_VALUE
    this >= Int.MAX_VALUE.toInt128() -> Int.MAX_VALUE
    else -> toInt()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int128.toLongClamped(): Long = when {
    this <= Long.MIN_VALUE.toInt128() -> Long.MIN_VALUE
    this >= Long.MAX_VALUE.toInt128() -> Long.MAX_VALUE
    else -> toLong()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int128.toInt128Clamped(): Int128 = this

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Int128.toUInt128Clamped(): UInt128 = when {
    this <= UInt128.MIN_VALUE.toInt128() -> UInt128.MIN_VALUE
    else -> toUInt128()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun UInt128.toByteClamped(): Byte = when {
    this >= Byte.MAX_VALUE.toUInt128() -> Byte.MAX_VALUE
    else -> toByte()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun UInt128.toShortClamped(): Short = when {
    this >= Short.MAX_VALUE.toUInt128() -> Short.MAX_VALUE
    else -> toShort()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun UInt128.toIntClamped(): Int = when {
    this >= Int.MAX_VALUE.toUInt128() -> Int.MAX_VALUE
    else -> toInt()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun UInt128.toLongClamped(): Long = when {
    this >= Long.MAX_VALUE.toUInt128() -> Long.MAX_VALUE
    else -> toLong()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun UInt128.toInt128Clamped(): Int128 = when {
    this >= Int128.MAX_VALUE.toUInt128() -> Int128.MAX_VALUE
    else -> toInt128()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun UInt128.toUInt128Clamped(): UInt128 = this
