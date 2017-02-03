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
package org.tobi29.scapes.engine.utils.math

object FastMath {
    fun convertFloatToHalf(a: Float): Short {
        val bits = java.lang.Float.floatToIntBits(a)
        val sign = bits.ushr(16) and 0x8000
        var value = (bits and 0x7fffffff) + 0x1000
        if (value >= 0x47800000) {
            if (bits and 0x7fffffff >= 0x47800000) {
                if (value < 0x7f800000) {
                    return (sign or 0x7c00).toShort()
                }
                return (sign or 0x7c00 or (bits and 0x007fffff).ushr(
                        13)).toShort()
            }
            return (sign or 0x7bff).toShort()
        }
        if (value >= 0x38800000) {
            return (sign or (value - 0x38000000).ushr(13)).toShort()
        }
        if (value < 0x33000000) {
            return sign.toShort()
        }
        value = (bits and 0x7fffffff).ushr(23)
        return (sign or ((bits and 0x7fffff or 0x800000) + 0x800000.ushr(
                value - 102)).ushr(126 - value)).toShort()
    }

    fun diff(value1: Double,
             value2: Double,
             modulus: Double): Double {
        var diff = (value2 - value1) % modulus
        val h = modulus * 0.5
        while (diff > h) {
            diff -= modulus
        }
        while (diff <= -h) {
            diff += modulus
        }
        return diff
    }

    fun nextPowerOfTwo(value: Int): Int {
        var output = value - 1
        output = output or (output shr 1)
        output = output or (output shr 2)
        output = output or (output shr 4)
        output = output or (output shr 8)
        output = output or (output shr 16)
        return output + 1
    }

    fun lb(bits: Int): Int {
        if (bits == 0) {
            throw IllegalArgumentException("Calling lb on 0 is not allowed")
        }
        return 31 - Integer.numberOfLeadingZeros(bits)
    }
}
