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

package org.tobi29.scapes.engine.utils

// FIXME: Cannot extend due to Kotlin/JS bug
data class Int128(val high: Long,
                  val low: Long) /* : Number() */ : Comparable<Int128> {
    operator fun plus(other: Int128): Int128 =
            plusImpl(high, low, other.high, other.low, ::Int128)

    operator fun minus(other: Int128): Int128 =
            minusImpl(high, low, other.high, other.low, ::Int128)

    operator fun times(other: Int128): Int128 =
            timesImpl(high, low, other.high, other.low, ::Int128)

    operator fun div(other: Int128): Int128 =
            sdivImpl(high, low, other.high, other.low)

    operator fun rem(other: Int128): Int128 =
            this - this / other * other

    infix fun remP(other: Int128): Int128 =
            (this % other).let { if (it < 0.toInt128()) it + other else it }

    operator fun unaryPlus(): Int128 = this

    operator fun unaryMinus(): Int128 =
            negateImpl(high, low, ::Int128)

    infix fun and(other: Int128): Int128 =
            andImpl(high, low, other.high, other.low, ::Int128)

    infix fun or(other: Int128): Int128 =
            orImpl(high, low, other.high, other.low, ::Int128)

    infix fun xor(other: Int128): Int128 =
            xorImpl(high, low, other.high, other.low, ::Int128)

    fun inv(): Int128 =
            invImpl(high, low, ::Int128)

    infix fun shl(bitCount: Int): Int128 =
            shlImpl(high, low, bitCount, ::Int128)

    infix fun shr(bitCount: Int): Int128 =
            sshrImpl(high, low, bitCount, ::Int128)

    infix fun ushr(bitCount: Int): Int128 =
            shrImpl(high, low, bitCount, ::Int128)

    override fun compareTo(other: Int128) =
            scompareImpl(high, low, other.high, other.low)

    override fun toString() = toString(10)

    fun toString(radix: Int): String {
        if (radix < 2 || radix > 36) {
            throw IllegalArgumentException("Invalid radix: $radix")
        }

        return sstringImpl(high, low, radix)
    }

    fun toByte() = low.toByte()

    fun toShort() = low.toShort()

    fun toInt() = low.toInt()

    fun toLong() = low

    fun toFloat() = (high.toFloat() * 1.8446744073709552e19f) + low.toFloat()

    fun toDouble() = (high.toDouble() * 1.8446744073709552e19) + low.toDouble()

    fun toChar() = low.toChar()

    companion object {
        val MIN_VALUE: Int128 = Int128(sminvh, sminvl)
        val MAX_VALUE: Int128 = Int128(smaxvh, smaxvl)
    }
}

// TODO: Cache?

fun String.toInt128(radix: Int = 10): Int128 =
        toInt128OrNull(radix)
                ?: throw IllegalArgumentException("Invalid number: $this")

fun String.toInt128OrNull(radix: Int = 10): Int128? =
        if (getOrNull(0) == '-') substring(1).toUInt128OrNull(radix)
                ?.let { -Int128(it.high, it.low) }
        else toUInt128OrNull(radix)?.let { Int128(it.high, it.low) }

fun Int128.toInt128(): Int128 = this

fun Byte.toInt128(): Int128 = toLong().toInt128()

fun Short.toInt128(): Int128 = toLong().toInt128()

fun Int.toInt128(): Int128 = toLong().toInt128()

fun Long.toInt128(): Int128 = if (this < 0) Int128(-1L, this)
else Int128(0L, this)

fun Double.toInt128(): Int128 =
        Int128((this / (256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0)).toLong(),
                (rem(256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0)).toLong())

fun Int128.toUInt128(): UInt128 = UInt128(high, low)
