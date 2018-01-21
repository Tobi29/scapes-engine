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

package org.tobi29.utils

// FIXME: Cannot extend due to Kotlin/JS bug
data class UInt128(val high: Long,
                   val low: Long) /* : Number() */ : Comparable<UInt128> {
    operator fun plus(other: UInt128): UInt128 =
            plusImpl(high, low, other.high, other.low, ::UInt128)

    operator fun minus(other: UInt128): UInt128 =
            minusImpl(high, low, other.high, other.low, ::UInt128)

    operator fun times(other: UInt128): UInt128 =
            timesImpl(high, low, other.high, other.low, ::UInt128)

    operator fun div(other: UInt128): UInt128 =
            divImpl(high, low, other.high, other.low)

    operator fun rem(other: UInt128): UInt128 =
            this - this / other * other

    operator fun unaryPlus(): UInt128 = this

    infix fun and(other: UInt128): UInt128 =
            andImpl(high, low, other.high, other.low, ::UInt128)

    infix fun or(other: UInt128): UInt128 =
            orImpl(high, low, other.high, other.low, ::UInt128)

    infix fun xor(other: UInt128): UInt128 =
            xorImpl(high, low, other.high, other.low, ::UInt128)

    fun inv(): UInt128 =
            invImpl(high, low, ::UInt128)

    infix fun shl(bitCount: Int): UInt128 =
            shlImpl(high, low, bitCount, ::UInt128)

    infix fun shr(bitCount: Int): UInt128 =
            shrImpl(high, low, bitCount, ::UInt128)

    override fun compareTo(other: UInt128) =
            compareImpl(high, low, other.high, other.low)

    override fun toString() = toString(10)

    fun toString(radix: Int): String {
        if (radix < 2 || radix > 36) {
            throw IllegalArgumentException("Invalid radix: $radix")
        }

        return stringImpl(high, low, radix)
    }

    fun toByte() = low.toByte()

    fun toShort() = low.toShort()

    fun toInt() = low.toInt()

    fun toLong() = low

    fun toFloat() = (high.toFloat() * 1.8446744073709552e19f) + low.toFloat()

    fun toDouble() = (high.toDouble() * 1.8446744073709552e19) + low.toDouble()

    fun toChar() = low.toChar()

    companion object {
        val MIN_VALUE: UInt128 = UInt128(MINVH, MINVL)
        val MAX_VALUE: UInt128 = UInt128(MAXVH, MAXVL)
    }
}

fun String.toUInt128(radix: Int = 10): UInt128 =
        toUInt128OrNull(radix)
                ?: throw IllegalArgumentException("Invalid number: $this")

fun String.toUInt128OrNull(radix: Int = 10): UInt128? {
    var oh = 0L
    var ol = 0L
    for (c in this) {
        timesImpl(oh, ol, 0L, radix.toLong()) { th, tl -> oh = th; ol = tl }
        val n = c.toInt() - '0'.toInt()
        val d = if (n < 0 || n > radix.coerceAtMost(10)) {
            val l = c.toInt() - 'a'.toInt()
            (if (l < 0 || l > (radix - 10)) {
                val u = c.toInt() - 'A'.toInt()
                if (u < 0 || u > (radix - 10)) return null else u
            } else l) + 10
        } else n
        plusImpl(oh, ol, 0L, d.toLong()) { ph, pl -> oh = ph; ol = pl }
    }
    return UInt128(oh, ol)
}

fun UInt128.toUInt128(): UInt128 = this

fun Byte.toUInt128(): UInt128 = toLong().toUInt128()

fun Short.toUInt128(): UInt128 = toLong().toUInt128()

fun Int.toUInt128(): UInt128 = toLong().toUInt128()

fun Long.toUInt128(): UInt128 = if (this < 0) UInt128(-1L, this)
else UInt128(0L, this)

fun Double.toUInt128(): UInt128 =
        UInt128((this / (256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0)).toLong(),
                (rem(256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0 * 256.0)).toLong())

fun UInt128.toInt128(): Int128 = Int128(high, low)
