/*
 * Copyright 2012-2019 Tobi29
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

@file:JvmName("ReadableJVMKt")

package org.tobi29.stdex

import java.io.IOException
import java.nio.Buffer
import java.nio.CharBuffer

/**
 * Wraps a [java.lang.Readable] into a [Readable]
 * @receiver The readable to wrap
 * @return A readable which forwards all calls to the given one
 */
fun java.lang.Readable.toReadable(): Readable = when (this) {
    is SEReadable -> se
    else -> object : JavaReadable {
        override val java = this@toReadable
    }
}

/**
 * Wraps a [Readable] into a [java.lang.Readable]
 * @receiver The readable to wrap
 * @return A readable which forwards all calls to the given one
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Readable.toJavaReadable(): java.lang.Readable = when (this) {
    is JavaReadable -> java
    else -> object : SEReadable {
        override val se = this@toJavaReadable
    }
}

interface JavaReadable : Readable {
    val java: java.lang.Readable

    override fun read(): Char {
        val single = single.get()
        single._clear()._limit(1)
        while (single.hasRemaining()) {
            // We have no access to EndOfStreamException in this module
            if (java.read(single) < 0) throw IOException("End of stream")
        }
        single._flip()
        return single.get()
    }

    override fun readTry(): Int {
        val single = single.get()
        single._clear()._limit(1)
        while (single.hasRemaining()) {
            if (java.read(single) < 0) return -1
        }
        single._flip()
        return single.get().toInt()
    }

    override fun read(array: CharArray, offset: Int, size: Int) {
        val buffer = CharBuffer.wrap(array, offset, size)
        while (buffer.hasRemaining()) {
            // We have no access to EndOfStreamException in this module
            if (java.read(buffer) < 0) throw IOException("End of stream")
        }
    }

    override fun readSome(array: CharArray, offset: Int, size: Int): Int {
        val buffer = CharBuffer.wrap(array, offset, size)
        return java.read(buffer)
    }
}

interface SEReadable : java.lang.Readable {
    val se: Readable

    override fun read(cb: CharBuffer): Int {
        val position = cb.position()
        if (cb.hasArray()) {
            val read = se.readSome(
                cb.array(),
                cb.arrayOffset() + position,
                cb.remaining()
            )
            if (read >= 0) cb._position(cb.position() + read)
            return read
        } else {
            var read = 0
            while (cb.hasRemaining()) {
                val c = se.readTry()
                if (c < 0) {
                    if (read == 0) return -1
                    break
                }
                cb.put(c.toChar())
                read++
            }
            return read
        }
    }
}

private val single = ThreadLocal { CharBuffer.allocate(1) }

// We do not have IOUtils in this module, so we use a local private copy

@Suppress("NOTHING_TO_INLINE")
private inline fun <B : Buffer> B._position(newPosition: Int): B =
    apply { position(newPosition) }

@Suppress("NOTHING_TO_INLINE")
private inline fun <B : Buffer> B._limit(newLimit: Int): B =
    apply { limit(newLimit) }

@Suppress("NOTHING_TO_INLINE")

private inline fun <B : Buffer> B._clear(): B =
    apply { clear() }

@Suppress("NOTHING_TO_INLINE")
private inline fun <B : Buffer> B._flip(): B =
    apply { flip() }
