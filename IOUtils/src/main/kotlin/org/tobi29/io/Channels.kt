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

package org.tobi29.io

import org.tobi29.stdex.toIntClamped

expect interface Channel : Closeable {
    fun isOpen(): Boolean

    /**
     * @throws IOException
     */
    override fun close()
}

expect interface InterruptibleChannel : Channel

interface ReadableByteChannel : Channel {
    fun read(buffer: ByteView): Int

    fun skip(length: Long): Long {
        val buffer = ByteArray(length.coerceAtMost(4096).toInt()).view
        while (length > 0) {
            val read = read(buffer.slice(0,
                    buffer.size.coerceAtMost(length.toIntClamped())))
            if (read == -1) {
                throw EndOfStreamException()
            }
            if (read == 0) {
                return length
            }
        }
        return length
    }
}

fun ReadableByteChannel.read(stream: MemoryViewStream<*>): Int =
        read(stream.bufferSlice()).also {
            if (it > 0) stream.position(stream.position() + it)
        }

interface WritableByteChannel : Channel {
    fun write(buffer: ByteViewRO): Int
}

fun WritableByteChannel.write(stream: MemoryViewStream<*>): Int =
        write(stream.bufferSlice()).also {
            if (it > 0) stream.position(stream.position() + it)
        }

interface ByteChannel : ReadableByteChannel, WritableByteChannel

interface SeekableByteChannel : ByteChannel {
    /**
     * @throws IOException
     */
    fun position(): Long

    /**
     * @throws IOException
     */
    fun position(newPosition: Long)

    /**
     * @throws IOException
     */
    fun size(): Long

    /**
     * @throws IOException
     */
    fun truncate(size: Long)

    /**
     * @throws IOException
     */
    fun remaining(): Long = size() - position()

    override fun skip(length: Long) = length.also {
        position(position() + length)
    }
}
