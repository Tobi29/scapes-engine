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

import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.readAsByteArray
import org.tobi29.arrays.sliceOver
import org.tobi29.stdex.Throws
import java.io.InputStream
import java.io.OutputStream

class ByteStreamInputStream(private val stream: ReadableByteStream) :
    InputStream() {
    // FIXME @Throws(IOException::class)
    override fun read(): Int {
        return stream.get().toInt() and 0xFF
    }

    // FIXME @Throws(IOException::class)
    override fun read(
        b: ByteArray,
        off: Int,
        len: Int
    ): Int {
        return stream.getSome(b.sliceOver(off, len))
    }

    // FIXME @Throws(IOException::class)
    override fun skip(n: Long): Long {
        val len = n.toInt()
        stream.skip(len)
        return len.toLong()
    }

    // FIXME @Throws(IOException::class)
    override fun available(): Int = stream.available
}

class ByteStreamOutputStream(private val stream: WritableByteStream) :
    OutputStream() {

    // FIXME @Throws(IOException::class)
    override fun write(b: Int) {
        stream.put(b.toByte())
    }

    // FIXME @Throws(IOException::class)
    override fun write(
        b: ByteArray,
        off: Int,
        len: Int
    ) {
        stream.put(b.sliceOver(off, len))
    }
}

class OutputStreamByteStream(private val stream: OutputStream) :
    WritableByteStream {
    override fun put(value: Byte) {
        stream.write(value.toInt())
    }

    override fun put(buffer: BytesRO) {
        buffer.readAsByteArray { array, offset, size ->
            stream.write(array, offset, size)
        }
    }
}
