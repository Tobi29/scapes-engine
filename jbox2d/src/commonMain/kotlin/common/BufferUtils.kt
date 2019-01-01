/*
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.jbox2d.common

import org.tobi29.stdex.assert
import org.tobi29.stdex.copy

object BufferUtils {
    inline fun <reified T> reallocateBuffer(
        oldBuffer: Array<T>?,
        oldCapacity: Int,
        newCapacity: Int,
        init: (Int) -> T
    ): Array<T> {
        assert { newCapacity > oldCapacity }
        return Array(newCapacity) {
            if (oldBuffer != null && it < oldCapacity) oldBuffer[it]
            else init(it)
        }
    }

    /** Reallocate a buffer.  */
    inline fun <reified T> reallocateBufferDeferred(
        oldBuffer: Array<T>?,
        oldCapacity: Int,
        newCapacity: Int,
        init: (Int) -> T
    ): Array<T>? =
        oldBuffer?.let {
            reallocateBuffer(oldBuffer, oldCapacity, newCapacity, init)
        }

    /** Reallocate a buffer.  */
    fun reallocateBuffer(
        oldBuffer: IntArray?,
        oldCapacity: Int,
        newCapacity: Int
    ): IntArray {
        assert { newCapacity > oldCapacity }
        val newBuffer = IntArray(newCapacity)
        if (oldBuffer != null) {
            copy(oldBuffer, newBuffer, oldCapacity)
        }
        return newBuffer
    }

    /** Reallocate a buffer.  */
    fun reallocateBuffer(
        oldBuffer: DoubleArray?,
        oldCapacity: Int,
        newCapacity: Int
    ): DoubleArray {
        assert { newCapacity > oldCapacity }
        val newBuffer = DoubleArray(newCapacity)
        if (oldBuffer != null) {
            copy(oldBuffer, newBuffer, oldCapacity)
        }
        return newBuffer
    }

    /**
     * Reallocate an int buffer. A 'deferred' buffer is reallocated only if it is not NULL. If
     * 'userSuppliedCapacity' is not zero, buffer is user supplied and must be kept.
     */
    fun reallocateBuffer(
        buffer: IntArray?,
        userSuppliedCapacity: Int,
        oldCapacity: Int,
        newCapacity: Int,
        deferred: Boolean
    ): IntArray? {
        var buffer = buffer
        assert { newCapacity > oldCapacity }
        assert { userSuppliedCapacity == 0 || newCapacity <= userSuppliedCapacity }
        if ((!deferred || buffer != null) && userSuppliedCapacity == 0) {
            buffer = reallocateBuffer(buffer, oldCapacity, newCapacity)
        }
        return buffer
    }

    /**
     * Reallocate a float buffer. A 'deferred' buffer is reallocated only if it is not NULL. If
     * 'userSuppliedCapacity' is not zero, buffer is user supplied and must be kept.
     */
    fun reallocateBuffer(
        buffer: DoubleArray?,
        userSuppliedCapacity: Int,
        oldCapacity: Int,
        newCapacity: Int,
        deferred: Boolean
    ): DoubleArray? {
        var buffer = buffer
        assert { newCapacity > oldCapacity }
        assert { userSuppliedCapacity == 0 || newCapacity <= userSuppliedCapacity }
        if ((!deferred || buffer != null) && userSuppliedCapacity == 0) {
            buffer = reallocateBuffer(buffer, oldCapacity, newCapacity)
        }
        return buffer
    }

    /** Rotate an array, see std::rotate  */
    fun <T> rotate(
        ray: Array<T>,
        first: Int,
        new_first: Int,
        last: Int
    ) {
        var first = first
        var newfirst = new_first
        var next = newfirst
        while (next != first) {
            val temp = ray[first]
            ray[first] = ray[next]
            ray[next] = temp
            first++
            next++
            if (next == last) {
                next = newfirst
            } else if (first == newfirst) {
                newfirst = next
            }
        }
    }

    /** Rotate an array, see std::rotate  */
    fun rotate(
        ray: IntArray,
        first: Int,
        new_first: Int,
        last: Int
    ) {
        var first = first
        var newfirst = new_first
        var next = newfirst
        while (next != first) {
            val temp = ray[first]
            ray[first] = ray[next]
            ray[next] = temp
            first++
            next++
            if (next == last) {
                next = newfirst
            } else if (first == newfirst) {
                newfirst = next
            }
        }
    }

    /** Rotate an array, see std::rotate  */
    fun rotate(
        ray: DoubleArray,
        first: Int,
        new_first: Int,
        last: Int
    ) {
        var first = first
        var newfirst = new_first
        var next = newfirst
        while (next != first) {
            val temp = ray[first]
            ray[first] = ray[next]
            ray[next] = temp
            first++
            next++
            if (next == last) {
                next = newfirst
            } else if (first == newfirst) {
                newfirst = next
            }
        }
    }
}

inline fun <T, reified R> Array<T>.mapArray(block: (T) -> R) =
    Array(size) { block(this[it]) }
