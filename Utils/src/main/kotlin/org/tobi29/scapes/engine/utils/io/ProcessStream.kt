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

import org.tobi29.scapes.engine.utils.ByteBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

fun <E> process(input: ReadableByteStream,
                processor: StreamProcessor<E>,
                bufferSize: Int = 1024): E {
    val buffer = ByteBuffer(bufferSize)
    var available = true
    while (available) {
        available = input.getSome(buffer)
        buffer.flip()
        processor.process(buffer)
        buffer.clear()
    }
    return processor.result()
}

@Throws(IOException::class)
fun process(input: ReadableByteStream,
            processor: (ByteBuffer) -> Unit,
            bufferSize: Int = 1024) {
    val buffer = ByteBuffer(bufferSize)
    var available = true
    while (available) {
        available = input.getSome(buffer)
        buffer.flip()
        processor(buffer)
        buffer.clear()
    }
}

fun asArray(): StreamProcessor<ByteArray> {
    return object : StreamProcessor<ByteArray> {
        private val stream = ByteBufferStream()

        override fun process(buffer: ByteBuffer) {
            stream.put(buffer)
        }

        override fun result(): ByteArray {
            stream.buffer().flip()
            val array = ByteArray(stream.buffer().remaining())
            stream.buffer().get(array)
            return array
        }
    }
}

fun asBuffer(
        supplier: (Int) -> ByteBuffer = ::ByteBuffer,
        growth: (Int) -> Int = { it + 8192 }): StreamProcessor<ByteBuffer> {
    return object : StreamProcessor<ByteBuffer> {
        private val stream = ByteBufferStream(supplier, growth)

        override fun process(buffer: ByteBuffer) {
            stream.put(buffer)
        }

        override fun result(): ByteBuffer {
            stream.buffer().flip()
            return stream.buffer()
        }
    }
}

fun asString(charset: Charset = StandardCharsets.UTF_8): StreamProcessor<String> {
    return object : StreamProcessor<String> {
        private val stream = ByteBufferStream(
                { ByteBuffer.wrap(ByteArray(it)) }, { it + 1024 })

        override fun process(buffer: ByteBuffer) {
            stream.put(buffer)
        }

        override fun result(): String {
            return String(stream.buffer().array(), 0,
                    stream.buffer().position(), charset)
        }
    }
}

fun put(stream: WritableByteStream): StreamProcessor<Unit?> {
    return object : StreamProcessor<Unit?> {
        override fun process(buffer: ByteBuffer) {
            stream.put(buffer)
        }

        override fun result(): Unit? {
            return null
        }
    }
}

interface StreamProcessor<out E> {
    @Throws(IOException::class)
    fun process(buffer: ByteBuffer)

    fun result(): E
}
