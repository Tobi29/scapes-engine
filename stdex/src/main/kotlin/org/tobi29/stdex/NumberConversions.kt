/*
 * Copyright 2012-2018 Tobi29
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
// Generation script can be found in `resources/codegen/GenNumberConversions.kts`.
// Run `resources/codegen/codegen.sh` to update sources.

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.stdex

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Byte.toByteClamped(): Byte = this

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Byte.toShortClamped(): Short = toShort()

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Byte.toIntClamped(): Int = toInt()

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Byte.toLongClamped(): Long = toLong()

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Short.toByteClamped(): Byte = when {
    this <= Byte.MIN_VALUE.toShort() -> Byte.MIN_VALUE
    this >= Byte.MAX_VALUE.toShort() -> Byte.MAX_VALUE
    else -> toByte()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Short.toShortClamped(): Short = this

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Short.toIntClamped(): Int = toInt()

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Short.toLongClamped(): Long = toLong()

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Int.toByteClamped(): Byte = when {
    this <= Byte.MIN_VALUE.toInt() -> Byte.MIN_VALUE
    this >= Byte.MAX_VALUE.toInt() -> Byte.MAX_VALUE
    else -> toByte()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Int.toShortClamped(): Short = when {
    this <= Short.MIN_VALUE.toInt() -> Short.MIN_VALUE
    this >= Short.MAX_VALUE.toInt() -> Short.MAX_VALUE
    else -> toShort()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Int.toIntClamped(): Int = this

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Int.toLongClamped(): Long = toLong()

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Long.toByteClamped(): Byte = when {
    this <= Byte.MIN_VALUE.toLong() -> Byte.MIN_VALUE
    this >= Byte.MAX_VALUE.toLong() -> Byte.MAX_VALUE
    else -> toByte()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Long.toShortClamped(): Short = when {
    this <= Short.MIN_VALUE.toLong() -> Short.MIN_VALUE
    this >= Short.MAX_VALUE.toLong() -> Short.MAX_VALUE
    else -> toShort()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Long.toIntClamped(): Int = when {
    this <= Int.MIN_VALUE.toLong() -> Int.MIN_VALUE
    this >= Int.MAX_VALUE.toLong() -> Int.MAX_VALUE
    else -> toInt()
}

/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
inline fun Long.toLongClamped(): Long = this
