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

import org.tobi29.scapes.engine.utils.BufferCreator

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.WritableByteChannel

class BufferedWriteChannelStream(private val channel: WritableByteChannel) : WritableByteStream {
    private val buffer = BufferCreator.bytes(8192)

    @Throws(IOException::class)
    override fun put(buffer: ByteBuffer,
                     len: Int): WritableByteStream {
        val limit = buffer.limit()
        buffer.limit(buffer.position() + len)
        if (ensure(len)) {
            this.buffer.put(buffer)
        } else {
            flush()
            write(buffer)
        }
        buffer.limit(limit)
        return this
    }

    @Throws(IOException::class)
    override fun put(b: Int): WritableByteStream {
        ensure(1)
        buffer.put(b.toByte())
        return this
    }

    @Throws(IOException::class)
    override fun putShort(value: Int): WritableByteStream {
        ensure(2)
        buffer.putShort(value.toShort())
        return this
    }

    @Throws(IOException::class)
    override fun putInt(value: Int): WritableByteStream {
        ensure(4)
        buffer.putInt(value)
        return this
    }

    @Throws(IOException::class)
    override fun putLong(value: Long): WritableByteStream {
        ensure(8)
        buffer.putLong(value)
        return this
    }

    @Throws(IOException::class)
    override fun putFloat(value: Float): WritableByteStream {
        ensure(4)
        buffer.putFloat(value)
        return this
    }

    @Throws(IOException::class)
    override fun putDouble(value: Double): WritableByteStream {
        ensure(8)
        buffer.putDouble(value)
        return this
    }

    @Throws(IOException::class)
    fun flush() {
        buffer.flip()
        write(buffer)
        buffer.clear()
    }

    @Throws(IOException::class)
    private fun ensure(len: Int): Boolean {
        if (len > buffer.capacity()) {
            return false
        }
        if (buffer.remaining() < len) {
            flush()
        }
        return true
    }

    @Throws(IOException::class)
    private fun write(buffer: ByteBuffer) {
        while (buffer.hasRemaining()) {
            val length = channel.write(buffer)
            if (length == -1) {
                throw IOException("End of stream")
            }
        }
    }
}
