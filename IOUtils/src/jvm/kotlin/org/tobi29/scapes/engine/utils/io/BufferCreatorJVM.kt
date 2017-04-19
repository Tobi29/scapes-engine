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

import java.nio.ByteOrder

/**
 * Creates a [ByteBuffer] with big-endian byte-order
 * @param size Capacity of the buffer
 * @return A [ByteBuffer] with big-endian byte-order
 */
inline fun ByteBuffer(size: Int): ByteBuffer {
    return ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN)
}

/**
 * Creates a [ByteBuffer] with little-endian byte-order
 * @param size Capacity of the buffer
 * @return A [ByteBuffer] with little-endian byte-order
 */
inline fun ByteBufferLE(size: Int): ByteBuffer {
    return ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
}

/**
 * Creates a [FloatBuffer] with big-endian byte-order
 * @param size Capacity of the buffer
 * @return A [FloatBuffer] with big-endian byte-order
 */
inline fun FloatBuffer(size: Int): FloatBuffer {
    return FloatBuffer.allocate(size)
}
