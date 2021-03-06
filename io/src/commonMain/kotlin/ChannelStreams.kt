/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.io

import org.tobi29.arrays.Bytes
import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.sliceOver

class BufferedReadChannelStream(
    private val channel: ReadableByteChannel,
    buffer: ByteViewE = ByteArray(8192).viewBE
) : ReadableByteStream {
    private val mbuffer = MemoryViewStream(buffer).apply { limit = 0 }

    override val available: Int get() = mbuffer.remaining

    override fun skip(length: Int) {
        var skip = (length - mbuffer.remaining).toLong()
        if (skip < 0) {
            mbuffer.position += length
        } else {
            mbuffer.position = mbuffer.limit
            while (skip > 0) {
                skip = channel.skip(skip)
            }
        }
    }

    override fun get(buffer: Bytes) {
        if (ensure(buffer.size)) {
            mbuffer.get(buffer)
        } else {
            val flushed = mbuffer.remaining
            if (flushed > 0) mbuffer.get(buffer.slice(0, flushed))
            val read = channel.read(buffer.slice(flushed))
            if (read < 0 && flushed <= 0) throw EndOfStreamException()
        }
    }

    override fun getSome(buffer: Bytes): Int =
        if (mbuffer.remaining >= buffer.size) {
            mbuffer.get(buffer)
            buffer.size
        } else {
            val flushed = mbuffer.remaining
            if (flushed > 0) mbuffer.get(buffer.slice(0, flushed))
            val read = channel.read(buffer.slice(flushed))
            if (read < 0) {
                if (flushed <= 0) -1 else flushed
            } else read + flushed
        }

    override fun getTry(): Int {
        ensureTry(1) ?: return -1
        return mbuffer.get().toInt() and 0xFF
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

    private fun ensure(len: Int) =
        ensureTry(len) ?: throw EndOfStreamException()

    private fun ensureTry(len: Int): Boolean? {
        if (len == 0) return true
        if (len > mbuffer.buffer().size) return false
        mbuffer.compact()
        while (mbuffer.position < len) {
            val read = channel.read(mbuffer.bufferSlice())
            if (read < 0) return null
            mbuffer.position += read
        }
        mbuffer.flip()
        return true
    }
}

class BufferedWriteChannelStream(
    private val channel: WritableByteChannel
) : WritableByteStream {
    private val mbuffer = MemoryViewStreamDefault().apply { limit = 8192 }

    override fun put(buffer: BytesRO) {
        if (ensure(buffer.size)) {
            mbuffer.put(buffer)
        } else {
            flush()
            write(buffer)
        }
    }

    override fun put(value: Byte) {
        ensure(1)
        mbuffer.put(value)
    }

    override fun putShort(value: Short) {
        ensure(2)
        mbuffer.putShort(value)
    }

    override fun putInt(value: Int) {
        ensure(4)
        mbuffer.putInt(value)
    }

    override fun putLong(value: Long) {
        ensure(8)
        mbuffer.putLong(value)
    }

    override fun putFloat(value: Float) {
        ensure(4)
        mbuffer.putFloat(value)
    }

    override fun putDouble(value: Double) {
        ensure(8)
        mbuffer.putDouble(value)
    }

    fun flush() {
        mbuffer.flip()
        write(mbuffer.bufferSlice())
        mbuffer.reset()
    }

    private fun ensure(len: Int): Boolean {
        if (len > mbuffer.limit) {
            return false
        }
        if (mbuffer.remaining < len) {
            flush()
        }
        return true
    }

    private fun write(buffer: BytesRO) {
        var currentBuffer = buffer
        while (currentBuffer.size > 0) {
            val wrote = channel.write(currentBuffer)
            if (wrote < 0) throw EndOfStreamException()
            currentBuffer = currentBuffer.slice(wrote)
        }
    }
}
