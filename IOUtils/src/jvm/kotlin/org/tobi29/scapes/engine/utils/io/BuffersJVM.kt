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

/* impl */ fun floatBuffer(size: Int): FloatBuffer {
    return java.nio.FloatBuffer.allocate(size)
}

/* impl */ inline val BIG_ENDIAN: ByteOrder get() = ByteOrder.BIG_ENDIAN

/* impl */ inline val LITTLE_ENDIAN: ByteOrder get() = ByteOrder.LITTLE_ENDIAN

/* impl */ inline val NATIVE_ENDIAN: ByteOrder get() = ByteOrder.nativeOrder()

/* impl */ object DefaultByteBufferProvider : ByteBufferProvider {
    /* impl */ override fun allocate(capacity: Int): ByteBuffer =
            java.nio.ByteBuffer.allocate(capacity).order(BIG_ENDIAN)

    override fun reallocate(buffer: ByteBuffer): ByteBuffer {
        if (buffer.hasArray()) {
            return buffer.order(BIG_ENDIAN)
        }
        return super.reallocate(buffer)
    }
}

/* impl */ object DefaultLEByteBufferProvider : ByteBufferProvider {
    /* impl */ override fun allocate(capacity: Int): ByteBuffer =
            java.nio.ByteBuffer.allocate(capacity).order(LITTLE_ENDIAN)

    override fun reallocate(buffer: ByteBuffer): ByteBuffer {
        if (buffer.hasArray()) {
            return buffer.order(LITTLE_ENDIAN)
        }
        return super.reallocate(buffer)
    }
}

object NativeByteBufferProvider : ByteBufferProvider {
    override fun allocate(capacity: Int): ByteBuffer =
            java.nio.ByteBuffer.allocateDirect(capacity).order(NATIVE_ENDIAN)

    override fun reallocate(buffer: ByteBuffer): ByteBuffer {
        if (buffer.isDirect) {
            return buffer.order(NATIVE_ENDIAN)
        }
        return super.reallocate(buffer)
    }
}

/* impl */ internal fun ReadableByteStream.writeArray(src: ByteArray,
                                                      off: Int,
                                                      len: Int): ReadableByteStream =
        get(ByteBuffer.wrap(src, off, len))

/* impl */ internal fun WritableByteStream.readArray(dest: ByteArray,
                                                     off: Int,
                                                     len: Int): WritableByteStream =
        put(ByteBuffer.wrap(dest, off, len))
