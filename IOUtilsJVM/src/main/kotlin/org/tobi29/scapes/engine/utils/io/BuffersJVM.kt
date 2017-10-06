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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils.io

import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface BufferProvider<out T : Buffer> {
    fun allocate(capacity: Int): T
}

typealias ByteBufferProvider = BufferProvider<ByteBuffer>

/**
 * Returns a view on the given array
 * @param offset Offset in the array
 * @param size Length in the array
 * @receiver The array to back into
 * @return A [ByteBuffer] using the array for storage
 */
inline fun ByteArray.asByteBuffer(offset: Int = 0,
                                  size: Int = this.size - offset): ByteBuffer =
        java.nio.ByteBuffer.wrap(this, offset, size)

impl typealias ByteOrder = java.nio.ByteOrder

impl inline val BIG_ENDIAN: ByteOrder get() = ByteOrder.BIG_ENDIAN

impl inline val LITTLE_ENDIAN: ByteOrder get() = ByteOrder.LITTLE_ENDIAN

impl inline val NATIVE_ENDIAN: ByteOrder get() = ByteOrder.nativeOrder()

object DefaultByteBufferProvider : ByteBufferProvider {
    override fun allocate(capacity: Int): ByteBuffer =
            java.nio.ByteBuffer.allocate(capacity).order(BIG_ENDIAN)
}

object DefaultLEByteBufferProvider : ByteBufferProvider {
    override fun allocate(capacity: Int): ByteBuffer =
            java.nio.ByteBuffer.allocate(capacity).order(LITTLE_ENDIAN)
}

object NativeByteBufferProvider : ByteBufferProvider {
    override fun allocate(capacity: Int): ByteBuffer =
            java.nio.ByteBuffer.allocateDirect(capacity).order(NATIVE_ENDIAN)
}

/**
 * Creates a [ByteBuffer] with big-endian byte-order
 * @param size Capacity of the buffer
 * @return A [ByteBuffer] with big-endian byte-order
 */
inline fun ByteBuffer(size: Int): ByteBuffer =
        DefaultByteBufferProvider.allocate(size)

fun ByteBufferNative(capacity: Int): ByteBuffer =
        java.nio.ByteBuffer.allocateDirect(capacity).order(NATIVE_ENDIAN)

/**
 * Fills a buffer with the given value
 * @receiver Buffer to fill
 * @param supplier Supplier called for each value written to the buffer
 * @return The given buffer
 */
inline fun ByteBuffer.fill(supplier: () -> Byte): ByteBuffer {
    while (hasRemaining()) {
        put(supplier())
    }
    return this
}

fun ByteBuffer.asArray() =
        ByteArray(remaining()).also {
            val position = position()
            get(it)
            position(position)
        }

// TODO: Make shorter for inline
inline fun <R> ByteView.mutateAsByteBuffer(block: (ByteBuffer) -> R): R {
    var buffer = asByteBuffer()
    val mapped = if (buffer == null) {
        buffer = ByteBuffer(size)
        // TODO: Optimize?
        for (i in 0 until size) {
            buffer.put(i, getByte(i))
        }
        false
    } else true
    try {
        return block(buffer)
    } finally {
        if (!mapped) {
            // TODO: Optimize?
            for (i in 0 until size) {
                setByte(i, buffer.get(i))
            }
        }
    }
}

fun ByteViewRO.readAsByteBuffer(): ByteBuffer =
        asByteBuffer() ?: ByteBuffer(size).also { buffer ->
            for (i in 0 until size) {
                buffer.put(i, getByte(i))
            }
        }

fun ByteViewRO.asByteBuffer(): ByteBuffer? = when (this) {
    is ByteBufferView -> byteBuffer.slice().order(byteBuffer.order())
    is ArrayByteView -> byteArray.asByteBuffer(offset, size).slice().also {
        if (this is MemorySegmentE) {
            it.order(if (isBigEndian) BIG_ENDIAN else LITTLE_ENDIAN)
        }
    }
    else -> null
}

fun ByteViewRO.readAsNativeByteBuffer(): ByteBuffer =
        asByteBuffer()?.let {
            if (!it.isDirect) {
                val buffer = java.nio.ByteBuffer.allocateDirect(
                        it.remaining()).order(NATIVE_ENDIAN)
                buffer.put(it)
                buffer.flip()
                buffer
            } else it
        } ?: java.nio.ByteBuffer.allocateDirect(size)
                .order(NATIVE_ENDIAN).also { buffer ->
            for (i in 0 until size) {
                buffer.put(getByte(i))
            }
            buffer.flip()
        }
