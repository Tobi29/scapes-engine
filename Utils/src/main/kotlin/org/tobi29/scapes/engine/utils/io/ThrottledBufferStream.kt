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

import org.tobi29.scapes.engine.utils.math.*

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel

class ThrottledBufferStream(private val channel: ReadableByteChannel, private val speed: Long) : ReadableByteChannel {
    private val time: Long
    private var used: Long = 0
    private var open = true

    init {
        time = System.nanoTime()
    }

    private fun check(): Int {
        val allowed = (System.nanoTime() - time) / speed
        return min(allowed - used, Int.MAX_VALUE.toLong()).toInt()
    }

    @Throws(IOException::class)
    override fun read(dst: ByteBuffer): Int {
        val len = min(check(), dst.remaining())
        if (len <= 0) {
            return 0
        }
        val limit = dst.limit()
        dst.limit(dst.position() + len)
        val read = channel.read(dst)
        dst.limit(limit)
        if (read > 0) {
            used += read.toLong()
        }
        return read
    }

    override fun isOpen(): Boolean {
        return open
    }

    @Throws(IOException::class)
    override fun close() {
        open = false
    }
}
