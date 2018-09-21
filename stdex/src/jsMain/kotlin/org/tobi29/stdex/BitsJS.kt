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

package org.tobi29.stdex

import org.tobi29.stdex.math.floorToInt
import kotlin.math.ln

actual fun clz(value: Int) = clzImpl(value)

actual fun ctz(value: Int) =
    if (value == 0) 32 else 31 - clz(value xor (value - 1))

actual fun popcount(value: Int): Int {
    var t = value - ((value ushr 1) and 0x55555555)
    t = (t and 0x33333333) + ((t ushr 2) and 0x33333333)
    return ((t + (t ushr 4) and 0xF0F0F0F) * 0x1010101) ushr 24
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun clz(value: Long) = value.splitToInts { i1, i0 ->
    if (i1 == 0) clz(i0) + 32
    else clz(i1)
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun ctz(value: Long) = value.splitToInts { i1, i0 ->
    if (i0 == 0) ctz(i1) + 32
    else ctz(i0)
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun popcount(value: Long) = value.splitToInts { i1, i0 ->
    popcount(i1) + popcount(i0)
}

@Suppress("UnsafeCastFromDynamic")
private val clzImpl: (Int) -> Int = if (Math.clz32 !== undefined) Math.clz32
else { value: Int ->
    if (value == 0) 32
    else (ln((value ushr 0).toDouble()) * Math.LOG2E)
        .floorToInt().let { it: Int -> 31 - it }
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun <R> Long.splitToBytes(output: (Byte, Byte, Byte, Byte, Byte, Byte, Byte, Byte) -> R): R =
    splitToInts { i1, i0 ->
        i1.splitToBytes { b7, b6, b5, b4 ->
            i0.splitToBytes { b3, b2, b1, b0 ->
                output(b7, b6, b5, b4, b3, b2, b1, b0)
            }
        }
    }

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun <R> Long.splitToShorts(output: (Short, Short, Short, Short) -> R): R =
    splitToInts { i1, i0 ->
        i1.splitToShorts { s3, s2 ->
            i0.splitToShorts { s1, s0 ->
                output(s3, s2, s1, s0)
            }
        }
    }

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
actual inline fun <R> Long.splitToInts(output: (Int, Int) -> R): R {
    val l: Kotlin.Long = asDynamic()
    return output(l.getHighBits(), l.getLowBits())
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun combineToLong(
    b7: Byte, b6: Byte, b5: Byte, b4: Byte,
    b3: Byte, b2: Byte, b1: Byte, b0: Byte
): Long = combineToLong(
    combineToInt(b7, b6, b5, b4), combineToInt(b3, b2, b1, b0)
)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun combineToLong(
    s3: Short, s2: Short, s1: Short, s0: Short
): Long = combineToLong(
    combineToInt(s3, s2), combineToInt(s1, s0)
)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
actual inline fun combineToLong(i1: Int, i0: Int): Long =
    Kotlin.Long(i0, i1).asDynamic()

// Dirty hack, but should be reasonably stable

@PublishedApi
internal external object Kotlin {
    class Long(low: Int, high: Int) {
        fun getHighBits(): Int
        fun getLowBits(): Int
    }
}

private external object Math {
    val LOG2E: Double
    val clz32: ((Int) -> Int)?
}
