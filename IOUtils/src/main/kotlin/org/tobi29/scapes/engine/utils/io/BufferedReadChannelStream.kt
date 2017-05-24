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

class BufferedReadChannelStream(private val channel: ReadableByteChannel,
                                private val buffer: ByteBuffer = ByteBuffer(
                                        8192)) : ReadableByteStream {

    init {
        buffer.limit(0)
    }

    override fun available(): Int {
        return buffer.remaining()
    }

    override fun skip(len: Int) {
        var skip = (len - buffer.remaining()).toLong()
        if (skip < 0) {
            buffer.position(buffer.position() + len)
        } else {
            buffer.position(buffer.limit())
            while (skip > 0) {
                skip = channel.skip(skip)
            }
        }
    }

    override fun get(buffer: ByteBuffer,
                     len: Int): ReadableByteStream {
        if (ensure(len)) {
            val limit = this.buffer.limit()
            this.buffer.limit(this.buffer.position() + len)
            buffer.put(this.buffer)
            this.buffer.limit(limit)
        } else {
            val limit = buffer.limit()
            buffer.limit(buffer.position() + len)
            buffer.put(this.buffer)
            if (!read(buffer)) {
                throw IOException("End of stream")
            }
            buffer.limit(limit)
        }
        return this
    }

    override fun getSome(buffer: ByteBuffer,
                         len: Int): Boolean {
        if (this.buffer.remaining() >= len) {
            val limit = this.buffer.limit()
            this.buffer.limit(this.buffer.position() + len)
            buffer.put(this.buffer)
            this.buffer.limit(limit)
            return true
        } else {
            val limit = buffer.limit()
            buffer.limit(buffer.position() + len)
            buffer.put(this.buffer)
            val available = read(buffer)
            buffer.limit(limit)
            return available
        }
    }

    override fun get(): Byte {
        ensure(1)
        return buffer.get()
    }

    override fun getShort(): Short {
        ensure(2)
        return buffer.short
    }

    override fun getInt(): Int {
        ensure(4)
        return buffer.int
    }

    override fun getLong(): Long {
        ensure(8)
        return buffer.long
    }

    override fun getFloat(): Float {
        ensure(4)
        return buffer.float
    }

    override fun getDouble(): Double {
        ensure(8)
        return buffer.double
    }

    private fun ensure(len: Int): Boolean {
        if (len > buffer.capacity()) {
            return false
        }
        if (buffer.remaining() < len) {
            buffer.compact()
            if (!read(buffer)) {
                if (buffer.position() < len) {
                    throw IOException("End of stream")
                }
            }
            buffer.flip()
        }
        return true
    }

    private fun read(buffer: ByteBuffer): Boolean {
        while (buffer.hasRemaining()) {
            val length = channel.read(buffer)
            if (length == -1) {
                return false
            }
        }
        return true
    }
}
