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

package org.tobi29.arrays

import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.splitToBytes

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
        value.splitToBytes { r, g, b, a ->
            array[i + 0] = r
            array[i + 1] = g
            array[i + 2] = b
            array[i + 3] = a
        }
    }
}

fun IntsRO2.asBytesRO(): BytesRO = when (this) {
    is Int2ByteArrayRO<*> -> array
    else -> ByteInt2ArrayRO(this)
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
        return array[i / array.width, i % array.width].splitToBytes { b3, b2, b1, b0 ->
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
