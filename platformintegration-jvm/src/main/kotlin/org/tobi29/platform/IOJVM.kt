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

package org.tobi29.platform

import org.tobi29.io.*
import org.tobi29.stdex.Readable
import java.io.InputStream
import java.io.InputStreamReader

private inline val unstableStdin: InputStream? get() = System.`in`

private val stableStdin = object : InputStream() {
    override fun read() = unstableStdin?.read() ?: -1

    override fun read(b: ByteArray) = unstableStdin?.read(b) ?: -1

    override fun read(b: ByteArray?, off: Int, len: Int) =
        unstableStdin?.read(b, off, len) ?: -1

    override fun skip(n: Long) = unstableStdin?.skip(n) ?: -1L

    override fun available() = unstableStdin?.available() ?: 0

    override fun close() {
        unstableStdin?.close()
    }

    override fun mark(readlimit: Int) {
        unstableStdin?.mark(readlimit)
    }

    override fun reset() {
        unstableStdin?.reset()
    }

    override fun markSupported() = unstableStdin?.markSupported() ?: false
}

private val stdinReader = InputStreamReader(stableStdin)

actual val stdout: WritableByteStream = OutputStreamByteStream(System.out)

actual val stderr: WritableByteStream = OutputStreamByteStream(System.err)

actual val stdin: ReadableByteChannel =
    Channels.newChannel(stableStdin).toChannel()

actual val stdinText: Readable = object : Readable {
    override fun read(): Char =
        readTry().also { if (it < 0) throw EndOfStreamException() }.toChar()

    override fun readTry(): Int {
        return stdinReader.read()
    }

    override fun read(array: CharArray, offset: Int, size: Int) {
        var currentOffset = offset
        var currentSize = size
        while (currentSize > 0) {
            val read = readSome(array, currentOffset, currentSize)
            if (read < 0) throw EndOfStreamException()
            currentOffset += read
            currentSize -= read
        }
    }

    override fun readSome(array: CharArray, offset: Int, size: Int): Int =
        stdinReader.read(array, offset, size)
}

actual inline val stdln: String get() = System.lineSeparator()
