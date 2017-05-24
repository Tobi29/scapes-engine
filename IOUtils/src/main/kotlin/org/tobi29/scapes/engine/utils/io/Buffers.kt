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

package org.tobi29.scapes.engine.utils.io

typealias Buffer = java.nio.Buffer
typealias ByteBuffer = java.nio.ByteBuffer
typealias FloatBuffer = java.nio.FloatBuffer

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

/*
header fun ByteBuffer.asString(): String
*/

/**
 * Creates a [ByteBuffer] with big-endian byte-order
 * @param size Capacity of the buffer
 * @return A [ByteBuffer] with big-endian byte-order
 */
fun ByteBuffer(size: Int): ByteBuffer = byteBuffer(size)

/**
 * Creates a [FloatBuffer]
 * @param size Capacity of the buffer
 * @return A [FloatBuffer]
 */
fun FloatBuffer(size: Int): FloatBuffer = floatBuffer(size)

/*
/**
 * Creates a [ByteBuffer] with big-endian byte-order
 * @param size Capacity of the buffer
 * @return A [ByteBuffer] with big-endian byte-order
 */
header fun byteBuffer(size: Int): ByteBuffer

/**
 * Creates a [ByteBuffer] with little-endian byte-order
 * @param size Capacity of the buffer
 * @return A [ByteBuffer] with little-endian byte-order
 */
header fun byteBufferLE(size: Int): ByteBuffer

/**
 * Creates a [FloatBuffer]
 * @param size Capacity of the buffer
 * @return A [FloatBuffer]
 */
header fun floatBuffer(size: Int): FloatBuffer

header internal fun ReadableByteStream.writeArray(src: ByteArray,
                                                  off: Int,
                                                  len: Int): ReadableByteStream

header internal fun WritableByteStream.readArray(dest: ByteArray,
                                                 off: Int,
                                                 len: Int): WritableByteStream
*/
