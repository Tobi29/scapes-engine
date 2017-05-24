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

import java.nio.ByteOrder

/* impl */ fun ByteBuffer.asString(): String =
        if (hasArray()) {
            String(array(), arrayOffset(), remaining())
        } else {
            String(asArray())
        }

/* impl */ fun byteBuffer(size: Int): ByteBuffer {
    return java.nio.ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN)
}

/* impl */ fun byteBufferLE(size: Int): ByteBuffer {
    return java.nio.ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
}

/* impl */ fun floatBuffer(size: Int): FloatBuffer {
    return java.nio.FloatBuffer.allocate(size)
}

/* impl */ internal fun ReadableByteStream.writeArray(src: ByteArray,
                                                      off: Int,
                                                      len: Int): ReadableByteStream =
        get(ByteBuffer.wrap(src, off, len))

/* impl */ internal fun WritableByteStream.readArray(dest: ByteArray,
                                                     off: Int,
                                                     len: Int): WritableByteStream =
        put(ByteBuffer.wrap(dest, off, len))
