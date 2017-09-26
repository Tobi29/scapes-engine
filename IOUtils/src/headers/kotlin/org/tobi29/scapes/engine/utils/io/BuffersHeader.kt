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

header fun ByteBuffer.asString(): String

/**
 * Returns a view on the given array
 * @param offset Offset in the array
 * @param length Length in the array
 * @receiver The array to back into
 * @return A [ByteBuffer] using the array for storage
 */
header fun ByteArray.asByteBuffer(offset: Int,
                                  length: Int): ByteBuffer

/**
 * Big endian byte order
 */
header val BIG_ENDIAN: ByteOrder

/**
 * Little endian byte order
 */
header val LITTLE_ENDIAN: ByteOrder

/**
 * Native endianness depending on current hardware, either [BIG_ENDIAN] or
 * [LITTLE_ENDIAN]
 */
header val NATIVE_ENDIAN: ByteOrder

header object DefaultByteBufferProvider : ByteBufferProvider {
    override fun allocate(capacity: Int): ByteBuffer

    override fun reallocate(buffer: ByteBuffer): ByteBuffer
}

header object DefaultLEByteBufferProvider : ByteBufferProvider {
    override fun allocate(capacity: Int): ByteBuffer

    override fun reallocate(buffer: ByteBuffer): ByteBuffer
}

header object DefaultFloatBufferProvider : FloatBufferProvider {
    override fun allocate(capacity: Int): FloatBuffer

    override fun reallocate(buffer: FloatBuffer): FloatBuffer
}
