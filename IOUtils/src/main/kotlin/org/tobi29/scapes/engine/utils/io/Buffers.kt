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

typealias Buffer = java.nio.Buffer
typealias ByteBuffer = java.nio.ByteBuffer
typealias FloatBuffer = java.nio.FloatBuffer
typealias CharBuffer = java.nio.CharBuffer
typealias ByteOrder = java.nio.ByteOrder

fun ByteBuffer.asArray() =
        ByteArray(remaining()).also {
            val position = position()
            get(it)
            position(position)
        }

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

/**
 * Creates a [ByteBuffer] with big-endian byte-order
 * @param size Capacity of the buffer
 * @return A [ByteBuffer] with big-endian byte-order
 */
inline fun ByteBuffer(size: Int): ByteBuffer =
        DefaultByteBufferProvider.allocate(size)

/**
 * Creates a [FloatBuffer]
 * @param size Capacity of the buffer
 * @return A [FloatBuffer]
 */
inline fun FloatBuffer(size: Int): FloatBuffer =
        DefaultFloatBufferProvider.allocate(size)

/**
 * Creates a [CharBuffer]
 * @param size Capacity of the buffer
 * @return A [CharBuffer]
 */
inline fun CharBuffer(size: Int): CharBuffer =
        DefaultCharBufferProvider.allocate(size)

/**
 * Returns a view on the given array
 * @receiver The array to back into
 * @return A [ByteBuffer] using the array for storage
 */
inline fun ByteArray.asByteBuffer(): ByteBuffer = asByteBuffer(0, size)

interface BufferProvider<T : Buffer> {
    fun allocate(capacity: Int): T

    fun reallocate(buffer: T): T
}

typealias ByteBufferProvider = BufferProvider<ByteBuffer>

fun forceReallocate(buffer: ByteBuffer,
                    bufferProvider: ByteBufferProvider): ByteBuffer =
        bufferProvider.allocate(buffer.capacity()).apply {
            val position = buffer.position()
            val limit = buffer.limit()
            buffer.rewind()
            put(buffer)
            buffer.limit(limit)
            buffer.position(position)
            clear()
        }

typealias FloatBufferProvider = BufferProvider<FloatBuffer>

fun forceReallocate(buffer: FloatBuffer,
                    bufferProvider: FloatBufferProvider): FloatBuffer =
        bufferProvider.allocate(buffer.capacity()).apply {
            val position = buffer.position()
            val limit = buffer.limit()
            buffer.rewind()
            put(buffer)
            buffer.limit(limit)
            buffer.position(position)
            clear()
        }

typealias CharBufferProvider = BufferProvider<CharBuffer>

fun forceReallocate(buffer: CharBuffer,
                    bufferProvider: CharBufferProvider): CharBuffer =
        bufferProvider.allocate(buffer.capacity()).apply {
            val position = buffer.position()
            val limit = buffer.limit()
            buffer.rewind()
            put(buffer)
            buffer.limit(limit)
            buffer.position(position)
            clear()
        }
