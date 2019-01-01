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

package org.tobi29.arrays

import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.primitiveHashCode
import org.tobi29.stdex.splitToBytes

actual open class IntsBytesRO<out D : BytesRO> actual constructor(
    actual val array: D
) : IntsRO {
    protected val data = array.asDataView()

    actual final override val size: Int get() = array.size shr 2

    actual final override fun get(index: Int): Int {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("$index")
        }
        val i = index shl 2
        return data?.getInt32(i, false)
                ?: combineToInt(
                    array[i + 0], array[i + 1], array[i + 2], array[i + 3]
                )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntsRO || size != other.size) return false
        for (i in 0 until size) {
            if (this[i] != other[i]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 1
        for (i in 0 until size) {
            h = h * 31 + this[i].primitiveHashCode()
        }
        return h
    }
}

actual class IntsBytes<out D : Bytes> actual constructor(
    array: D
) : IntsBytesRO<D>(array), Ints {
    override fun set(index: Int, value: Int) {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("$index")
        }
        val i = index shl 2
        if (data != null) data.setInt32(i, value, false)
        else value.splitToBytes { b3, b2, b1, b0 ->
            array[i + 0] = b3
            array[i + 1] = b2
            array[i + 2] = b1
            array[i + 3] = b0
        }
    }
}
