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
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * Utility class for compressing and decompressing data
 */
object CompressionUtil {

    @Throws(IOException::class)
    @JvmOverloads @JvmStatic fun compress(input: ReadableByteStream,
                               level: Int = Deflater.DEFAULT_COMPRESSION,
                               supplier: (Int) -> ByteBuffer = {
                                   BufferCreator.bytes(it)
                               },
                               growth: (Int) -> Int = { length -> length + 1024 }): ByteBuffer {
        val stream = ByteBufferStream(supplier, growth)
        compress(input, stream, level)
        return stream.buffer()
    }

    @Throws(IOException::class)
    @JvmOverloads @JvmStatic fun compress(input: ReadableByteStream,
                                              output: WritableByteStream,
                                              level: Int = 1) {
        ZDeflater(level).use { filter -> filter(input, output, filter) }
    }

    @Throws(IOException::class)
    @JvmOverloads @JvmStatic fun decompress(input: ReadableByteStream,
                                 supplier: (Int) -> ByteBuffer = {
                                     BufferCreator.bytes(it)
                                 },
                                 growth: (Int) -> Int = { length -> length + 1024 }): ByteBuffer {
        val output = ByteBufferStream(supplier, growth)
        decompress(input, output)
        return output.buffer()
    }

    @Throws(IOException::class)
    @JvmStatic fun decompress(input: ReadableByteStream,
                   output: WritableByteStream) {
        ZInflater().use { filter -> filter(input, output, filter) }
    }

    @Throws(IOException::class)
    @JvmStatic fun filter(input: ReadableByteStream,
               output: WritableByteStream,
               filter: Filter) {
        while (input.hasAvailable()) {
            filter.input(input)
            while (!filter.needsInput()) {
                val len = filter.output(output)
                if (len <= 0) {
                    break
                }
            }
        }
        filter.finish()
        while (!filter.finished()) {
            val len = filter.output(output)
            if (len <= 0) {
                break
            }
        }
        filter.reset()
    }

    interface Filter : AutoCloseable {
        @Throws(IOException::class)
        fun input(buffer: ReadableByteStream)

        @Throws(IOException::class)
        fun output(buffer: WritableByteStream): Int

        fun finish()

        fun needsInput(): Boolean

        fun finished(): Boolean

        fun reset()

        override fun close()
    }

    class ZDeflater @JvmOverloads constructor(level: Int, buffer: Int = 8192) : Filter {
        private val deflater: Deflater
        private val output: ByteBuffer
        private var input: ByteBuffer

        init {
            deflater = Deflater(level)
            input = BufferCreator.bytes(buffer)
            output = BufferCreator.bytes(buffer)
        }

        @Throws(IOException::class)
        override fun input(buffer: ReadableByteStream) {
            if (!input.hasRemaining()) {
                val newInput = BufferCreator.bytes(input.capacity() shl 1)
                input.flip()
                newInput.put(input)
                input = newInput
            }
            buffer.getSome(input)
            input.flip()
            deflater.setInput(input.array(), input.arrayOffset(),
                    input.remaining())
        }

        @Throws(IOException::class)
        override fun output(buffer: WritableByteStream): Int {
            val len = deflater.deflate(output.array())
            output.limit(len)
            buffer.put(output)
            output.clear()
            return len
        }

        override fun finish() {
            deflater.finish()
        }

        override fun needsInput(): Boolean {
            return deflater.needsInput()
        }

        override fun finished(): Boolean {
            return deflater.finished()
        }

        override fun reset() {
            deflater.reset()
        }

        override fun close() {
            deflater.end()
        }
    }

    class ZInflater @JvmOverloads constructor(buffer: Int = 8192) : Filter {
        private val inflater = Inflater()
        private val output: ByteBuffer
        private var input: ByteBuffer

        init {
            input = BufferCreator.bytes(buffer)
            output = BufferCreator.bytes(buffer)
        }

        @Throws(IOException::class)
        override fun input(buffer: ReadableByteStream) {
            if (!input.hasRemaining()) {
                val newInput = BufferCreator.bytes(input.capacity() shl 1)
                input.flip()
                newInput.put(input)
                input = newInput
            }
            buffer.getSome(input)
            input.flip()
            inflater.setInput(input.array(), input.arrayOffset(),
                    input.remaining())
        }

        @Throws(IOException::class)
        override fun output(buffer: WritableByteStream): Int {
            try {
                val len = inflater.inflate(output.array())
                output.limit(len)
                buffer.put(output)
                output.clear()
                return len
            } catch (e: DataFormatException) {
                return -1
            }

        }

        override fun finish() {
        }

        override fun needsInput(): Boolean {
            return inflater.needsInput()
        }

        override fun finished(): Boolean {
            return inflater.finished()
        }

        override fun reset() {
            inflater.reset()
        }

        override fun close() {
            inflater.end()
        }
    }
}
