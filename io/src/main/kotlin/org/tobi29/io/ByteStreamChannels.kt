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

class WritableByteStreamChannel(
    private val stream: WritableByteStream
) : WritableByteChannel {
    override fun write(buffer: ByteViewRO): Int {
        stream.put(buffer)
        return buffer.size
    }

    override fun isOpen() = true
    override fun close() {}
}

class ReadableByteStreamChannel(
    private val stream: ReadableByteStream
) : ReadableByteChannel {
    override fun read(buffer: ByteView) = stream.getSome(buffer)

    override fun isOpen() = true
    override fun close() {}
}

class RandomReadableByteStreamChannel(
    private val stream: RandomReadableByteStream
) : SeekableReadByteChannel {
    override fun read(buffer: ByteView) = stream.getSome(buffer)

    override fun position() = stream.position().toLong()

    override fun position(newPosition: Long) =
        stream.position(newPosition.toIntClamped())

    override fun size() = stream.limit().toLong()

    override fun isOpen() = true
    override fun close() {}
}