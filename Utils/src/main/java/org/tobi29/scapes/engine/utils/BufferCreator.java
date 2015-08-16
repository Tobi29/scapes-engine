/*
 * Copyright 2012-2015 Tobi29
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
     * Creates a {@code ByteBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@code ByteBuffer} with big-endian byte-order
     */
    public static ByteBuffer bytes(int size) {
        return ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Creates a {@code ShortBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@code ShortBuffer} with big-endian byte-order
     */
    public static ShortBuffer shorts(int size) {
        return bytes(size << 1).asShortBuffer();
    }

    /**
     * Creates a {@code IntBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@code IntBuffer} with big-endian byte-order
     */
    public static IntBuffer ints(int size) {
        return bytes(size << 2).asIntBuffer();
    }

    /**
     * Creates a {@code LongBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@code LongBuffer} with big-endian byte-order
     */
    public static LongBuffer longs(int size) {
        return bytes(size << 3).asLongBuffer();
    }

    /**
     * Creates a {@code FloatBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@code FloatBuffer} with big-endian byte-order
     */
    public static FloatBuffer floats(int size) {
        return bytes(size << 2).asFloatBuffer();
    }

    /**
     * Creates a {@code DoubleBuffer} with big-endian byte-order
     *
     * @param size Capacity of the buffer
     * @return A {@code DoubleBuffer} with big-endian byte-order
     */
    public static DoubleBuffer doubles(int size) {
        return bytes(size << 3).asDoubleBuffer();
    }

    public static ByteBuffer wrap(byte... array) {
        ByteBuffer buffer = bytes(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }

    public static ShortBuffer wrap(short... array) {
        ShortBuffer buffer = shorts(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }

    public static IntBuffer wrap(int... array) {
        IntBuffer buffer = ints(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }

    public static LongBuffer wrap(long... array) {
        LongBuffer buffer = longs(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }

    public static FloatBuffer wrap(float... array) {
        FloatBuffer buffer = floats(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }

    public static DoubleBuffer wrap(double... array) {
        DoubleBuffer buffer = doubles(array.length);
        buffer.put(array);
        buffer.rewind();
        return buffer;
    }
}
