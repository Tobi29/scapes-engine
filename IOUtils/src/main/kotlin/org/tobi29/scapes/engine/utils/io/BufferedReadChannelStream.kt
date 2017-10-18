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
                                buffer: ByteViewE = ByteArray(
                                        8192).viewBE) : ReadableByteStream {
    private val mbuffer = MemoryViewStream(buffer).apply { limit(0) }

    override fun available(): Int {
        return mbuffer.remaining()
    }

    override fun skip(length: Int): ReadableByteStream = apply {
        var skip = (length - mbuffer.remaining()).toLong()
        if (skip < 0) {
            mbuffer.position(mbuffer.position() + length)
        } else {
            mbuffer.position(mbuffer.limit())
            while (skip > 0) {
                skip = channel.skip(skip)
            }
        }
    }

    override fun get(buffer: ByteView) = apply {
        if (ensure(buffer.size)) {
            mbuffer.get(buffer)
        } else {
            val flushed = mbuffer.remaining()
            if (flushed > 0) mbuffer.get(buffer.slice(0, flushed))
            val read = channel.read(buffer.slice(flushed))
            if (read < 0 && flushed <= 0) throw IOException("End of stream")
        }
    }

    override fun getSome(buffer: ByteView): Int =
            if (mbuffer.remaining() >= buffer.size) {
                mbuffer.get(buffer)
                buffer.size
            } else {
                val flushed = mbuffer.remaining()
                if (flushed > 0) mbuffer.get(buffer.slice(0, flushed))
                val read = channel.read(buffer.slice(flushed))
                if (read < 0) {
                    if (flushed <= 0) -1 else flushed
                } else read + flushed
            }

    override fun get(): Byte {
        ensure(1)
        return mbuffer.get()
    }

    override fun getShort(): Short {
        ensure(2)
        return mbuffer.getShort()
    }

    override fun getInt(): Int {
        ensure(4)
        return mbuffer.getInt()
    }

    override fun getLong(): Long {
        ensure(8)
        return mbuffer.getLong()
    }

    override fun getFloat(): Float {
        ensure(4)
        return mbuffer.getFloat()
    }

    override fun getDouble(): Double {
        ensure(8)
        return mbuffer.getDouble()
    }

    private fun ensure(len: Int): Boolean {
        if (len == 0) return true
        if (len > mbuffer.buffer().size) return false
        mbuffer.compact()
        while (mbuffer.position() < len) {
            val read = channel.read(mbuffer.bufferSlice())
            if (read < 0) throw IOException("End of stream")
            mbuffer.position(mbuffer.position() + read)
        }
        mbuffer.flip()
        return true
    }
}
