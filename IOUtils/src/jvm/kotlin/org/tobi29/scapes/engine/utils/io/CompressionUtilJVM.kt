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

import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

/* impl */ class ZDeflater(level: Int,
                           buffer: Int = 8192) : CompressionUtil.Filter {
    private val deflater = Deflater(level)
    private val output = ByteBuffer(buffer)
    private var input = ByteBuffer(buffer)

    /* impl */ override fun input(buffer: ReadableByteStream): Boolean {
        if (!input.hasRemaining()) {
            val newInput = ByteBuffer(input.capacity() shl 1)
            input.flip()
            newInput.put(input)
            input = newInput
        }
        if (!buffer.getSome(input)) {
            return false
        }
        deflater.setInput(input.array(), input.arrayOffset(), input.position())
        return true
    }

    /* impl */ override fun output(buffer: WritableByteStream): Int {
        val len = deflater.deflate(output.array())
        output.limit(len)
        buffer.put(output)
        output.clear()
        input.clear()
        return len
    }

    /* impl */ override fun finish() {
        deflater.finish()
    }

    /* impl */ override fun needsInput(): Boolean {
        return deflater.needsInput()
    }

    /* impl */ override fun finished(): Boolean {
        return deflater.finished()
    }

    /* impl */ override fun reset() {
        deflater.reset()
    }

    /* impl */ override fun close() {
        deflater.end()
    }
}

/* impl */ class ZInflater(buffer: Int = 8192) : CompressionUtil.Filter {
    private val inflater = Inflater()
    private val output = ByteBuffer(buffer)
    private var input = ByteBuffer(buffer)

    /* impl */ override fun input(buffer: ReadableByteStream): Boolean {
        if (!input.hasRemaining()) {
            val newInput = ByteBuffer(input.capacity() shl 1)
            input.flip()
            newInput.put(input)
            input = newInput
        }
        if (!buffer.getSome(input)) {
            return false
        }
        inflater.setInput(input.array(), input.arrayOffset(), input.position())
        return true
    }

    /* impl */ override fun output(buffer: WritableByteStream): Int {
        try {
            val len = inflater.inflate(output.array())
            output.limit(len)
            buffer.put(output)
            output.clear()
            input.clear()
            return len
        } catch (e: DataFormatException) {
            return -1
        }

    }

    /* impl */ override fun finish() {
    }

    /* impl */ override fun needsInput(): Boolean {
        return inflater.needsInput()
    }

    /* impl */ override fun finished(): Boolean {
        return inflater.finished()
    }

    /* impl */ override fun reset() {
        inflater.reset()
    }

    /* impl */ override fun close() {
        inflater.end()
    }
}
