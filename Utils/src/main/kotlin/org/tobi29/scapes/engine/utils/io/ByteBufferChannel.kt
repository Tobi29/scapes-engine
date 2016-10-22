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

package org.tobi29.scapes.engine.utils.io

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel

class ByteBufferChannel(private val buffer: ByteBuffer) : ByteChannel {

    fun buffer(): ByteBuffer {
        return buffer
    }

    @Throws(IOException::class)
    override fun read(dst: ByteBuffer): Int {
        val len = dst.remaining()
        val limit = buffer.limit()
        buffer.limit(buffer.position() + len)
        dst.put(buffer)
        buffer.limit(limit)
        return len
    }

    @Throws(IOException::class)
    override fun write(src: ByteBuffer): Int {
        val len = src.remaining()
        buffer.put(src)
        return len
    }

    override fun isOpen(): Boolean {
        return true
    }

    @Throws(IOException::class)
    override fun close() {
    }
}
