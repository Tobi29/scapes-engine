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

package org.tobi29.math

import org.tobi29.stdex.math.floorToInt

internal object RandomJS : Random {
    override fun nextInt() = ((nextDouble() - 0.5) * (1L shl 31) * 2.0).floorToInt()

    override fun nextLong() = ((nextDouble() - 0.5) * (1L shl 63) * 2.0).let {
        if (it < 0.0) (it - 1.0).toLong() else it.toLong()
    }

    override fun nextBoolean() = nextDouble() >= 0.5

    override fun nextFloat() = nextDouble().toFloat()

    override fun nextDouble() = Math.random()
}

// Mimics `java.util.Random` to allow consistent seeded RNG (especially useful
// for cross-platform games)
internal class RandomJSJVM(seed: Long) : Random {
    private var seed = seed xor MULTIPLIER and MASK

    private fun next(bits: Int): Int {
        seed = seed * MULTIPLIER + OFFSET and MASK
        return seed.ushr(48 - bits).toInt()
    }

    override fun nextInt() = next(32)

    override fun nextInt(bound: Int): Int {
        if (bound <= 0) throw IllegalArgumentException("Invalid bound: $bound")
        val modulus = bound - 1

        // Fast path for power-of-2 bounds
        if (bound and modulus == 0) {
            return (bound * next(31).toLong() shr 31).toInt()
        }

        while (true) {
            val random = next(31)
            val ffs = random % bound
            if (random - ffs + modulus >= 0) {
                return ffs
            }
        }
    }

    override fun nextLong() =
            (next(32).toLong() shl 32) + next(32)

    override fun nextBoolean() =
            next(1) != 0

    override fun nextFloat() =
            next(24).toFloat() / (1 shl 24)

    override fun nextDouble() =
            ((next(26).toLong() shl 27) + next(27)).toDouble() / (1L shl 53)
}

actual fun Random(): Random = RandomJS

actual fun Random(seed: Long): Random = RandomJSJVM(
        seed)

actual fun threadLocalRandom(): Random = RandomJS

private const val MULTIPLIER = 0x5DEECE66DL
private const val OFFSET = 0xBL
private const val MASK = 0xFFFFFFFFFFFFL

private external object Math {
    fun random(): Double
}
