/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.scapes.engine.backends.opengl

import net.gitout.ktbindings.utils.DataBuffer
import org.tobi29.arrays.Bytes
import org.tobi29.arrays.BytesRO
import org.tobi29.io.*
import org.tobi29.stdex.BIG_ENDIAN
import org.tobi29.stdex.LITTLE_ENDIAN
import java.nio.ByteBuffer

actual class BytesRODataBuffer actual constructor(
    private val bytes: BytesRO
) : DataBuffer {
    override fun read(): Pair<ByteBuffer, () -> Unit> =
        bytes.readAsNativeByteBuffer() to {}

    override fun write() =
        error("Buffer not writeable")
}

actual class BytesDataBuffer actual constructor(
    private val bytes: Bytes
) : DataBuffer {
    override fun read(): Pair<ByteBuffer, () -> Unit> =
        bytes.readAsNativeByteBuffer() to {}

    override fun write(): Pair<ByteBuffer, () -> Unit> {
        var buffer = bytes.asNativeByteBuffer()
        if (buffer == null) {
            buffer = ByteBufferNative(bytes.size)
            if (bytes is MemorySegmentE) {
                buffer.order(if (bytes.isBigEndian) BIG_ENDIAN else LITTLE_ENDIAN)
            }
            val view = buffer.viewE
            bytes.getBytes(0, view)
            return buffer to { view.getBytes(0, bytes) }
        } else {
            return buffer to {}
        }
    }
}
