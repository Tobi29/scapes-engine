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

package org.tobi29.scapes.engine.utils.tests

import org.junit.Assert
import org.junit.Test
import org.tobi29.scapes.engine.utils.BufferCreator
import org.tobi29.scapes.engine.utils.io.BufferedReadChannelStream
import org.tobi29.scapes.engine.utils.io.BufferedWriteChannelStream
import org.tobi29.scapes.engine.utils.io.ByteBufferChannel
import org.tobi29.scapes.engine.utils.tests.util.RandomInput

import java.io.IOException

class BufferedStreamTest {
    @Test
    @Throws(IOException::class)
    fun testWriteRead() {
        val channel = ByteBufferChannel(BufferCreator.bytes(1 shl 16))
        val arrays = RandomInput.createRandomArrays(16, 8)
        for (size in 0..15) {
            channel.buffer().clear()
            write(BufferedWriteChannelStream(channel), arrays)
            channel.buffer().flip()
            read(BufferedReadChannelStream(channel), arrays)
        }
    }

    @Throws(IOException::class)
    private fun write(stream: BufferedWriteChannelStream,
                      arrays: Array<ByteArray>) {
        for (array in arrays) {
            stream.put(array)
        }
        stream.put(123)
        stream.putShort(1234)
        stream.putInt(12345678)
        stream.putLong(123456789101112L)
        stream.flush()
    }

    @Throws(IOException::class)
    private fun read(stream: BufferedReadChannelStream,
                     arrays: Array<ByteArray>) {
        for (array in arrays) {
            val check = ByteArray(array.size)
            stream[check]
            Assert.assertArrayEquals("Arrays did not match", array, check)
        }
        Assert.assertEquals("Byte did not match", 123, stream.get().toLong())
        Assert.assertEquals("Short did not match", 1234, stream.short.toLong())
        Assert.assertEquals("Integer did not match", 12345678,
                stream.int.toLong())
        Assert.assertEquals("Long did not match", 123456789101112L,
                stream.long)
    }
}
