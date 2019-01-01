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

package org.tobi29.arrays

import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.splitToBytes

expect open class IntsBytesRO<out D : BytesRO>(
    array: D
) : IntsRO {
    val array: D
    final override val size: Int
    final override fun get(index: Int): Int
}

expect class IntsBytes<out D : Bytes>(
    array: D
) : IntsBytesRO<D>, Ints

open class Ints2IntsRO<out D : IntsRO>(
    val array: D,
    final override val width: Int,
    final override val height: Int
) : IntsRO2 {
    final override fun get(index1: Int, index2: Int): Int {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            throw IndexOutOfBoundsException("$index1 $index2")
        }
        return array[index2 * width + index1]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ints2IntsRO<*>) return false

        return array == other.array &&
                width == other.width &&
                height == other.height
    }

    override fun hashCode(): Int = array.hashCode()
}

class Ints2Ints<out D : Ints>(
    array: D,
    width: Int,
    height: Int
) : Ints2IntsRO<D>(array, width, height), Ints2 {
    override fun set(index1: Int, index2: Int, value: Int) {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            throw IndexOutOfBoundsException("$index1 $index2")
        }
        array[index2 * width + index1] = value
    }
}

typealias Ints2BytesRO<D> = Ints2IntsRO<IntsBytesRO<D>>
typealias Ints2Bytes<D> = Ints2Ints<IntsBytes<D>>

inline fun <D : BytesRO> Ints2BytesRO(
    array: D, width: Int, height: Int
): Ints2BytesRO<D> =
    Ints2IntsRO(IntsBytesRO(array), width, height)

inline fun <D : Bytes> Ints2Bytes(
    array: D, width: Int, height: Int
): Ints2Bytes<D> =
    Ints2Ints(IntsBytes(array), width, height)

typealias Ints2ByteArray = Ints2Ints<IntsBytes<HeapBytes>>

inline fun Ints2ByteArray(
    width: Int, height: Int
): Ints2ByteArray =
    Ints2ByteArray(ByteArray(width * height shl 2), width, height)

inline fun Ints2ByteArray(
    array: ByteArray, width: Int, height: Int
): Ints2ByteArray =
    Ints2Bytes(array.sliceOver(), width, height)

fun Ints2IntsRO<*>.asBytesRO(): BytesRO =
    asFastBytesRO() ?: ByteInt2ArrayRO(this)

fun IntsRO2.asBytesRO(): BytesRO =
    asFastBytesRO() ?: ByteInt2ArrayRO(this)

fun Ints2IntsRO<*>.asFastBytesRO(): BytesRO? {
    val array = array
    if (array is IntsBytesRO<*>) {
        return array.array
    }
    return null
}

fun IntsRO2.asFastBytesRO(): BytesRO? {
    if (this is Ints2IntsRO<*>) return asBytesRO()
    if (this is Int2ByteArrayRO<*>) return array
    return null
}

class ByteInt2ArrayRO<out D : IntsRO2>(
    val array: D
) : BytesRO {
    override val size: Int get() = array.width * array.height shl 2

    override fun get(index: Int): Byte {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("$index")
        }
        val i = index shr 2
        return array[i % array.width, i / array.width].splitToBytes { b3, b2, b1, b0 ->
            when (index and 3) {
                0 -> b3
                1 -> b2
                2 -> b1
                3 -> b0
                else -> error("Impossible")
            }
        }
    }
}

// TODO: Remove after 0.0.14

@Deprecated("Use new wrapper classes")
open class Int2ByteArrayRO<out D : BytesRO>(
    val array: D,
    final override val width: Int,
    final override val height: Int
) : IntsRO2 {
    final override fun get(index1: Int, index2: Int): Int {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            throw IndexOutOfBoundsException("$index1 $index2")
        }
        val i = (index2 * width + index1) shl 2
        return combineToInt(
            array[i + 0], array[i + 1], array[i + 2], array[i + 3]
        )
    }
}

@Deprecated("Use new wrapper classes")
class Int2ByteArray<out D : Bytes>(
    array: D,
    width: Int,
    height: Int
) : Int2ByteArrayRO<D>(array, width, height), Ints2 {
    override fun set(index1: Int, index2: Int, value: Int) {
        if (index1 < 0 || index2 < 0 || index1 >= width || index2 >= height) {
            throw IndexOutOfBoundsException("$index1 $index2")
        }
        val i = (index2 * width + index1) shl 2
        value.splitToBytes { b3, b2, b1, b0 ->
            array[i + 0] = b3
            array[i + 1] = b2
            array[i + 2] = b1
            array[i + 3] = b0
        }
    }
}
