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

import org.tobi29.scapes.engine.utils.math.min
import java.io.IOException
import java.nio.ByteBuffer

class LimitedBufferStream(private val stream: ReadableByteStream,
                          private var remaining: Int) : SizedReadableByteStream {

    override fun available(): Int {
        return min(stream.available(), remaining)
    }

    @Throws(IOException::class)
    override fun skip(len: Int) {
        check(len)
        stream.skip(len)
    }

    @Throws(IOException::class)
    override fun get(buffer: ByteBuffer,
                     len: Int): ReadableByteStream {
        check(len)
        return stream[buffer, len]
    }

    @Throws(IOException::class)
    override fun getSome(buffer: ByteBuffer,
                         len: Int): Boolean {
        var len = len
        len = min(len, remaining)
        remaining -= len
        return stream.getSome(buffer, len) && remaining > 0
    }

    @Throws(IOException::class)
    override fun get(): Byte {
        check(1)
        return stream.get()
    }

    override val short: Short
        @Throws(IOException::class)
        get() {
            check(2)
            return stream.short
        }

    override val int: Int
        @Throws(IOException::class)
        get() {
            check(4)
            return stream.int
        }

    override val long: Long
        @Throws(IOException::class)
        get() {
            check(8)
            return stream.long
        }

    override val float: Float
        @Throws(IOException::class)
        get() {
            check(4)
            return stream.float
        }

    override val double: Double
        @Throws(IOException::class)
        get() {
            check(8)
            return stream.double
        }

    @Throws(IOException::class)
    private fun check(len: Int) {
        if (remaining < len) {
            throw IOException("End of stream")
        }
        remaining -= len
    }

    override fun remaining(): Int {
        return remaining
    }

    override fun hasRemaining(): Boolean {
        return remaining > 0
    }
}
