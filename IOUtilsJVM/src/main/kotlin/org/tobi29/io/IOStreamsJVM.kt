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

import org.tobi29.arrays.ByteArraySliceRO
import org.tobi29.arrays.readAsByteArray
import java.io.InputStream
import java.io.OutputStream

class ByteStreamInputStream(private val stream: ReadableByteStream) : InputStream() {
    // TODO: @Throws(IOException::class)
    override fun read(): Int {
        return stream.get().toInt() and 0xFF
    }

    // TODO: @Throws(IOException::class)
    override fun read(b: ByteArray,
                      off: Int,
                      len: Int): Int {
        return stream.getSome(b.view.slice(off, len))
    }

    // TODO: @Throws(IOException::class)
    override fun skip(n: Long): Long {
        val len = n.toInt()
        stream.skip(len)
        return len.toLong()
    }

    // TODO: @Throws(IOException::class)
    override fun available(): Int {
        return stream.available()
    }
}

class ByteStreamOutputStream(private val stream: WritableByteStream) : OutputStream() {

    // TODO: @Throws(IOException::class)
    override fun write(b: Int) {
        stream.put(b.toByte())
    }

    // TODO: @Throws(IOException::class)
    override fun write(b: ByteArray,
                       off: Int,
                       len: Int) {
        stream.put(b.view.slice(off, len))
    }
}

class OutputStreamByteStream(private val stream: OutputStream) : WritableByteStream {
    override fun put(b: Byte) = also { stream.write(b.toInt()) }

    override fun put(buffer: ByteArraySliceRO) = also {
        buffer.readAsByteArray { array, offset, size ->
            stream.write(array, offset, size)
        }
    }
}
