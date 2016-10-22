/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils

import java.nio.*

object BufferCreator {

    /**
     * Creates a [ByteBuffer] with big-endian byte-order
     * @param size Capacity of the buffer
     * @return A [ByteBuffer] with big-endian byte-order
     */
    fun bytes(size: Int): ByteBuffer {
        return ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN)
    }

    /**
     * Creates a [ShortBuffer] with big-endian byte-order
     * @param size Capacity of the buffer
     * @return A [ShortBuffer] with big-endian byte-order
     */
    fun shorts(size: Int): ShortBuffer {
        return ShortBuffer.allocate(size)
    }

    /**
     * Creates a [IntBuffer] with big-endian byte-order
     * @param size Capacity of the buffer
     * @return A [IntBuffer] with big-endian byte-order
     */
    fun ints(size: Int): IntBuffer {
        return IntBuffer.allocate(size)
    }

    /**
     * Creates a [LongBuffer] with big-endian byte-order
     * @param size Capacity of the buffer
     * @return A [LongBuffer] with big-endian byte-order
     */
    fun longs(size: Int): LongBuffer {
        return LongBuffer.allocate(size)
    }

    /**
     * Creates a [FloatBuffer] with big-endian byte-order
     * @param size Capacity of the buffer
     * @return A [FloatBuffer] with big-endian byte-order
     */
    fun floats(size: Int): FloatBuffer {
        return FloatBuffer.allocate(size)
    }

    /**
     * Creates a [DoubleBuffer] with big-endian byte-order
     * @param size Capacity of the buffer
     * @return A [DoubleBuffer] with big-endian byte-order
     */
    fun doubles(size: Int): DoubleBuffer {
        return DoubleBuffer.allocate(size)
    }

    /**
     * Creates a [ByteBuffer] and copies the array into it
     * @param array Array to write
     * @return A [ByteBuffer], with position at 0 and limit at length of array
     */
    fun wrap(vararg array: Byte): ByteBuffer {
        val buffer = bytes(array.size)
        buffer.put(array)
        buffer.rewind()
        return buffer
    }

    /**
     * Creates a [ShortBuffer] and copies the array into it
     * @param array Array to write
     * @return A [ShortBuffer], with position at 0 and limit at length of array
     */
    fun wrap(vararg array: Short): ShortBuffer {
        val buffer = shorts(array.size)
        buffer.put(array)
        buffer.rewind()
        return buffer
    }

    /**
     * Creates a [IntBuffer] and copies the array into it
     * @param array Array to write
     * @return A [IntBuffer], with position at 0 and limit at length of array
     */
    fun wrap(vararg array: Int): IntBuffer {
        val buffer = ints(array.size)
        buffer.put(array)
        buffer.rewind()
        return buffer
    }

    /**
     * Creates a [LongBuffer] and copies the array into it
     * @param array Array to write
     * @return A [LongBuffer], with position at 0 and limit at length of array
     */
    fun wrap(vararg array: Long): LongBuffer {
        val buffer = longs(array.size)
        buffer.put(array)
        buffer.rewind()
        return buffer
    }

    /**
     * Creates a [FloatBuffer] and copies the array into it
     * @param array Array to write
     * @return A [FloatBuffer], with position at 0 and limit at length of array
     */
    fun wrap(vararg array: Float): FloatBuffer {
        val buffer = floats(array.size)
        buffer.put(array)
        buffer.rewind()
        return buffer
    }

    /**
     * Creates a [DoubleBuffer] and copies the array into it
     * @param array Array to write
     * @return A [DoubleBuffer], with position at 0 and limit at length of array
     */
    fun wrap(vararg array: Double): DoubleBuffer {
        val buffer = doubles(array.size)
        buffer.put(array)
        buffer.rewind()
        return buffer
    }
}
