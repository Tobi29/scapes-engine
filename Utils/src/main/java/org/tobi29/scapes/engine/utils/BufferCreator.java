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

package org.tobi29.scapes.engine.utils;

import java.nio.*;

/**
 * Utility class for creating buffers
 */
public final class BufferCreator {
    private BufferCreator() {
    }

    /**
     * Creates a {@link ByteBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@link ByteBuffer} with big-endian byte-order
     */
    public static ByteBuffer bytes(int size) {
        return ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Creates a {@link ShortBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@link ShortBuffer} with big-endian byte-order
     */
    public static ShortBuffer shorts(int size) {
        return ShortBuffer.allocate(size);
    }

    /**
     * Creates a {@link IntBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@link IntBuffer} with big-endian byte-order
     */
    public static IntBuffer ints(int size) {
        return IntBuffer.allocate(size);
    }

    /**
     * Creates a {@link LongBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@link LongBuffer} with big-endian byte-order
     */
    public static LongBuffer longs(int size) {
        return LongBuffer.allocate(size);
    }

    /**
     * Creates a {@link FloatBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@link FloatBuffer} with big-endian byte-order
     */
    public static FloatBuffer floats(int size) {
        return FloatBuffer.allocate(size);
    }

    /**
     * Creates a {@link DoubleBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@link DoubleBuffer} with big-endian byte-order
     */
    public static DoubleBuffer doubles(int size) {
        return DoubleBuffer.allocate(size);
    }

    /**
     * Creates a {@link ByteBuffer} and copies the array into it
     *
     * @param array Array to write
     * @return A {@link ByteBuffer}, with position at 0 and limit at length of
     * array
     */
    public static ByteBuffer wrap(byte... array) {
        ByteBuffer buffer = bytes(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }

    /**
     * Creates a {@link ShortBuffer} and copies the array into it
     *
     * @param array Array to write
     * @return A {@link ShortBuffer}, with position at 0 and limit at length of
     * array
     */
    public static ShortBuffer wrap(short... array) {
        ShortBuffer buffer = shorts(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }

    /**
     * Creates a {@link IntBuffer} and copies the array into it
     *
     * @param array Array to write
     * @return A {@link IntBuffer}, with position at 0 and limit at length of
     * array
     */
    public static IntBuffer wrap(int... array) {
        IntBuffer buffer = ints(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }

    /**
     * Creates a {@link LongBuffer} and copies the array into it
     *
     * @param array Array to write
     * @return A {@link LongBuffer}, with position at 0 and limit at length of
     * array
     */
    public static LongBuffer wrap(long... array) {
        LongBuffer buffer = longs(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }

    /**
     * Creates a {@link FloatBuffer} and copies the array into it
     *
     * @param array Array to write
     * @return A {@link FloatBuffer}, with position at 0 and limit at length of
     * array
     */
    public static FloatBuffer wrap(float... array) {
        FloatBuffer buffer = floats(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }

    /**
     * Creates a {@link DoubleBuffer} and copies the array into it
     *
     * @param array Array to write
     * @return A {@link DoubleBuffer}, with position at 0 and limit at length of
     * array
     */
    public static DoubleBuffer wrap(double... array) {
        DoubleBuffer buffer = doubles(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }
}
