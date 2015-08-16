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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Utility class for creating direct buffers
 */
public final class BufferCreatorNative {
    private BufferCreatorNative() {
    }

    /**
     * Creates a indirect {@code ByteBuffer} with native byte-order
     *
     * @param size Capacity of the buffer
     * @return A direct {@code ByteBuffer} with native byte-order, usable for native calls
     */
    public static ByteBuffer bytes(int size) {
        return ByteBuffer.allocate(size).order(ByteOrder.nativeOrder());
    }

    /**
     * Creates a indirect {@code IntBuffer} with native byte-order
     *
     * @param size Capacity of the buffer
     * @return A direct {@code IntBuffer} with native byte-order, usable for native calls
     */
    public static IntBuffer ints(int size) {
        return bytes(size << 2).asIntBuffer();
    }

    /**
     * Creates a indirect {@code FloatBuffer} with native byte-order
     *
     * @param size Capacity of the buffer
     * @return A direct {@code FloatBuffer} with native byte-order, usable for native calls
     */
    public static FloatBuffer floats(int size) {
        return bytes(size << 2).asFloatBuffer();
    }

    /**
     * Creates a direct {@code ByteBuffer} with native byte-order
     *
     * @param size Capacity of the buffer
     * @return A direct {@code ByteBuffer} with native byte-order, usable for native calls
     */
    public static ByteBuffer bytesD(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    /**
     * Creates a direct {@code IntBuffer} with native byte-order
     *
     * @param size Capacity of the buffer
     * @return A direct {@code IntBuffer} with native byte-order, usable for native calls
     */
    public static IntBuffer intsD(int size) {
        return bytesD(size << 2).asIntBuffer();
    }

    /**
     * Creates a direct {@code FloatBuffer} with native byte-order
     *
     * @param size Capacity of the buffer
     * @return A direct {@code FloatBuffer} with native byte-order, usable for native calls
     */
    public static FloatBuffer floatsD(int size) {
        return bytesD(size << 2).asFloatBuffer();
    }
}
