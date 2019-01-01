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

@file:JvmName("BitsJVMKt")

package org.tobi29.stdex

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline infix fun Int.lrot(other: Int): Int =
    java.lang.Integer.rotateLeft(this, other)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline infix fun Int.rrot(other: Int): Int =
    java.lang.Integer.rotateRight(this, other)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline infix fun Long.lrot(other: Int): Long =
    java.lang.Long.rotateLeft(this, other)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline infix fun Long.rrot(other: Int): Long =
    java.lang.Long.rotateRight(this, other)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun clz(value: Int) =
    java.lang.Integer.numberOfLeadingZeros(value)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun ctz(value: Int) =
    java.lang.Integer.numberOfTrailingZeros(value)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun popcount(value: Int) =
    java.lang.Integer.bitCount(value)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun clz(value: Long) =
    java.lang.Long.numberOfLeadingZeros(value)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun ctz(value: Long) =
    java.lang.Long.numberOfTrailingZeros(value)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun popcount(value: Long) =
    java.lang.Long.bitCount(value)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun <R> Long.splitToBytes(output: (Byte, Byte, Byte, Byte, Byte, Byte, Byte, Byte) -> R): R =
    let { l ->
        output(
            (l ushr 56).toByte(),
            (l ushr 48).toByte(),
            (l ushr 40).toByte(),
            (l ushr 32).toByte(),
            (l ushr 24).toByte(),
            (l ushr 16).toByte(),
            (l ushr 8).toByte(),
            (l ushr 0).toByte()
        )
    }

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun <R> Long.splitToShorts(output: (Short, Short, Short, Short) -> R): R =
    let { l ->
        output(
            (l ushr 48).toShort(),
            (l ushr 32).toShort(),
            (l ushr 16).toShort(),
            (l ushr 0).toShort()
        )
    }

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun <R> Long.splitToInts(output: (Int, Int) -> R): R =
    let { l ->
        output(
            (l ushr 32).toInt(),
            (l ushr 0).toInt()
        )
    }

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun combineToLong(
    b7: Byte, b6: Byte, b5: Byte, b4: Byte,
    b3: Byte, b2: Byte, b1: Byte, b0: Byte
): Long = (b7.toLong() and 0xFF shl 56) or
        (b6.toLong() and 0xFF shl 48) or
        (b5.toLong() and 0xFF shl 40) or
        (b4.toLong() and 0xFF shl 32) or
        (b3.toLong() and 0xFF shl 24) or
        (b2.toLong() and 0xFF shl 16) or
        (b1.toLong() and 0xFF shl 8) or
        (b0.toLong() and 0xFF shl 0)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun combineToLong(
    s3: Short, s2: Short, s1: Short, s0: Short
): Long = (s3.toLong() and 0xFFFF shl 48) or
        (s2.toLong() and 0xFFFF shl 32) or
        (s1.toLong() and 0xFFFF shl 16) or
        (s0.toLong() and 0xFFFF shl 0)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun combineToLong(
    i1: Int, i0: Int
): Long = (i1.toLong() and 0xFFFFFFFF shl 32) or
        (i0.toLong() and 0xFFFFFFFF shl 0)