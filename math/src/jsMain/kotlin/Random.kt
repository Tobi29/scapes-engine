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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.math

import org.khronos.webgl.*
import org.tobi29.arrays.Bytes
import org.tobi29.arrays.Ints
import org.tobi29.arrays.Shorts
import org.tobi29.arrays.mutateAsTypedArray
import org.tobi29.stdex.math.floorToInt

internal object RandomJS : Random {
    override fun nextInt() =
        ((nextDouble() - 0.5) * (1L shl 31) * 2.0).floorToInt()

    override fun nextLong() = ((nextDouble() - 0.5) * (1L shl 63) * 2.0).let {
        if (it < 0.0) (it - 1.0).toLong() else it.toLong()
    }

    override fun nextBoolean() = nextDouble() >= 0.5

    override fun nextFloat() = nextDouble().toFloat()

    override fun nextDouble() = Math.random()
}

internal object SecureRandomCrypto : SecureRandom {
    private val buffer = ArrayBuffer(4)
    private val byte = Int8Array(buffer, 0, 1)
    private val short = Int16Array(buffer, 0, 1)
    private val int = Int32Array(buffer, 0, 1)

    override fun nextByte(): Byte {
        crypto.getRandomValues(byte)
        return byte[0]
    }

    override fun nextBytes(array: Bytes) {
        array.mutateAsTypedArray { crypto.getRandomValues(it) }
    }

    override fun nextShort(): Short {
        crypto.getRandomValues(short)
        return short[0]
    }

    override fun nextShorts(array: Shorts) {
        array.mutateAsTypedArray { crypto.getRandomValues(it) }
    }

    override fun nextInt(): Int {
        crypto.getRandomValues(int)
        return int[0]
    }

    override fun nextInts(array: Ints) {
        array.mutateAsTypedArray { crypto.getRandomValues(it) }
    }
}

actual inline fun Random(): Random = threadLocalRandom()

actual fun threadLocalRandom(): Random = RandomJS

actual fun SecureRandom(
    highQuality: Boolean
): SecureRandom = SecureRandomCrypto

private external object Math {
    fun random(): Double
}

private external val crypto: Crypto

private external class Crypto {
    fun getRandomValues(array: ArrayBufferView)
}
