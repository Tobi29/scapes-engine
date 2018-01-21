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

package org.tobi29.io

class BufferedWriteChannelStream(private val channel: WritableByteChannel) : WritableByteStream {
    private val mbuffer = MemoryViewStreamDefault().apply { limit(8192) }

    // TODO: @Throws(IOException::class)
    override fun put(buffer: ByteViewRO) = apply {
        if (ensure(buffer.size)) {
            mbuffer.put(buffer)
        } else {
            flush()
            write(buffer)
        }
    }

    // TODO: @Throws(IOException::class)
    override fun put(b: Byte): WritableByteStream {
        ensure(1)
        mbuffer.put(b)
        return this
    }

    // TODO: @Throws(IOException::class)
    override fun putShort(value: Short): WritableByteStream {
        ensure(2)
        mbuffer.putShort(value)
        return this
    }

    // TODO: @Throws(IOException::class)
    override fun putInt(value: Int): WritableByteStream {
        ensure(4)
        mbuffer.putInt(value)
        return this
    }

    // TODO: @Throws(IOException::class)
    override fun putLong(value: Long): WritableByteStream {
        ensure(8)
        mbuffer.putLong(value)
        return this
    }

    // TODO: @Throws(IOException::class)
    override fun putFloat(value: Float): WritableByteStream {
        ensure(4)
        mbuffer.putFloat(value)
        return this
    }

    // TODO: @Throws(IOException::class)
    override fun putDouble(value: Double): WritableByteStream {
        ensure(8)
        mbuffer.putDouble(value)
        return this
    }

    // TODO: @Throws(IOException::class)
    fun flush() {
        mbuffer.flip()
        write(mbuffer.bufferSlice())
        mbuffer.reset()
    }

    // TODO: @Throws(IOException::class)
    private fun ensure(len: Int): Boolean {
        if (len > mbuffer.limit()) {
            return false
        }
        if (mbuffer.remaining() < len) {
            flush()
        }
        return true
    }

    // TODO: @Throws(IOException::class)
    private fun write(buffer: ByteViewRO) {
        var currentBuffer = buffer
        while (currentBuffer.size > 0) {
            val wrote = channel.write(currentBuffer)
            if (wrote < 0) throw EndOfStreamException()
            currentBuffer = currentBuffer.slice(wrote)
        }
    }
}
