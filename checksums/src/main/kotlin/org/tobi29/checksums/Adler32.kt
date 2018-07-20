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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.checksums

import org.tobi29.arrays.BytesRO
import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.splitToShorts

inline fun <R> initChainAdler32(
    output: (Short, Short) -> R
): R = output(1, 0)

inline fun <R> initChainAdler32(
    adler: Int,
    output: (Short, Short) -> R
): R = adler.splitToShorts { b, a -> output(a, b) }

inline fun finishChainAdler32(a: Short, b: Short): Int = combineToInt(b, a)

inline fun <R> chainAdler32(
    a: Short,
    b: Short,
    data: Byte,
    output: (Short, Short) -> R
): R = chainAdler32Fast(
    a.toInt() and 0xFFFF, b.toInt() and 0xFFFF, data
) { na, nb ->
    output(
        (na % ADLER32_BASE).toShort(),
        (nb % ADLER32_BASE).toShort()
    )
}

inline fun <R> chainAdler32Fast(
    a: Int,
    b: Int,
    data: Byte,
    output: (Int, Int) -> R
): R = (a + (data.toInt() and 0xFF)).let {
    output(it, b + it)
}

inline fun computeAdler32(
    data: Byte
): Int = computeAdler32(0, data)

inline fun computeAdler32(
    adler: Int,
    data: Byte
): Int = initChainAdler32(adler) { a, b ->
    chainAdler32(a, b, data, ::finishChainAdler32)
}

inline fun <R> chainAdler32(
    a: Short,
    b: Short,
    data: ByteArray,
    output: (Short, Short) -> R
): R = chainAdler32(a, b, data, 0, data.size, output)

inline fun <R> chainAdler32(
    a: Short,
    b: Short,
    data: ByteArray,
    offset: Int = 0,
    size: Int = data.size - offset,
    output: (Short, Short) -> R
): R {
    var ca = a.toInt() and 0xFFFF
    var cb = b.toInt() and 0xFFFF
    var fast = ADLER32_MAX_FAST
    for (i in offset until offset + size) {
        chainAdler32Fast(ca, cb, data[i]) { na, nb ->
            ca = na
            cb = nb
        }
        if (fast-- == 0) {
            fast = ADLER32_MAX_FAST
            ca %= ADLER32_BASE
            cb %= ADLER32_BASE
        }
    }
    ca %= ADLER32_BASE
    cb %= ADLER32_BASE
    return output(ca.toShort(), cb.toShort())
}

inline fun computeAdler32(
    data: ByteArray
): Int = computeAdler32(0, data)

inline fun computeAdler32(
    adler: Int,
    data: ByteArray
): Int = computeAdler32(adler, data, 0, data.size)

inline fun computeAdler32(
    adler: Int,
    data: ByteArray,
    offset: Int = 0,
    size: Int = data.size - offset
): Int = initChainAdler32(adler) { a, b ->
    chainAdler32(a, b, data, offset, size, ::finishChainAdler32)
}

inline fun <R> chainAdler32(
    a: Short,
    b: Short,
    data: BytesRO,
    output: (Short, Short) -> R
): R {
    var ca = a.toInt() and 0xFFFF
    var cb = b.toInt() and 0xFFFF
    var fast = ADLER32_MAX_FAST
    for (i in 0 until data.size) {
        chainAdler32Fast(ca, cb, data[i]) { na, nb ->
            ca = na
            cb = nb
        }
        if (fast-- == 0) {
            fast = ADLER32_MAX_FAST
            ca %= ADLER32_BASE
            cb %= ADLER32_BASE
        }
    }
    ca %= ADLER32_BASE
    cb %= ADLER32_BASE
    return output(ca.toShort(), cb.toShort())
}

inline fun computeAdler32(
    data: BytesRO
): Int = computeAdler32(0, data)

inline fun computeAdler32(
    adler: Int,
    data: BytesRO
): Int = initChainAdler32(adler) { a, b ->
    chainAdler32(a, b, data, ::finishChainAdler32)
}

const val ADLER32_BASE = 65521
// TODO: What would be highest safe value?
//const val ADLER32_MAX_FAST = 5552
const val ADLER32_MAX_FAST = 2000
