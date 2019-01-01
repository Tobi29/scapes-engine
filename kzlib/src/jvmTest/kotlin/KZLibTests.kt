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

package org.tobi29.kzlib

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.arrays.readAsByteArray
import org.tobi29.arrays.sliceOver
import org.tobi29.assertions.shouldEqual
import org.tobi29.io.*
import org.tobi29.stdex.utf8ToArray
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterOutputStream

object KZLibTests : Spek({
    describe("inflating and deflating data") {
        val array = str.repeat(25).utf8ToArray()
        for (level in 0..9) {
            val refCompressed = MemoryViewStreamDefault().also { stream ->
                val def = java.util.zip.Deflater(level)
                try {
                    DeflaterOutputStream(
                        ByteStreamOutputStream(stream),
                        def
                    ).use {
                        it.write(array)
                    }
                } finally {
                    def.end()
                }
                stream.flip()
            }.bufferSlice()
            for (windowBits in 8..15) {
                describe("compressing and decompressing (level: $level, windowBits: $windowBits)") {
                    val compressed = DeflateHandle(
                        level = level,
                        windowBits = windowBits
                    ).use {
                        it.deflate(MemoryViewReadableStream(array.viewBE))
                    }

                    val refDecompressed =
                        MemoryViewStreamDefault().also { stream ->
                            InflaterOutputStream(ByteStreamOutputStream(stream)).use {
                                it.write(compressed.readAsByteArray())
                            }
                            stream.flip()
                        }.bufferSlice()

                    val decompressed = InflateHandle().use {
                        it.inflate(MemoryViewReadableStream(compressed.viewBE))
                    }

                    val decompressedRef = InflateHandle().use {
                        it.inflate(MemoryViewReadableStream(refCompressed.viewBE))
                    }

                    it("decompressing the compressed data should result in the original data") {
                        refDecompressed shouldEqual array.sliceOver()
                        decompressed shouldEqual array.sliceOver()
                        decompressedRef shouldEqual array.sliceOver()
                    }
                }
            }
        }
    }
})

private val str = """/*
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

package com.jcraft.kzlib

import org.tobi29.arrays.readAsByteArray
import org.tobi29.arrays.toHexadecimal
import org.tobi29.io.*
import org.tobi29.kzlib.*
import org.tobi29.stdex.utf8ToArray
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPOutputStream

fun main(args: Array<String>) {
    val array =
        "Arn is a proper pleb, as everyone knows.".repeat(1000).utf8ToArray()
    val refCompressed = MemoryViewStreamDefault().also { stream ->
        DeflaterOutputStream(ByteStreamOutputStream(stream)).use {
            it.write(array)
        }
        stream.flip()
    }.bufferSlice()
    val refCompressedGzip = MemoryViewStreamDefault().also { stream ->
        GZIPOutputStream(ByteStreamOutputStream(stream)).use {
            it.write(array)
        }
        stream.flip()
    }.bufferSlice()

    val compressed = DeflateHandle().use {
        it.deflate(MemoryViewReadableStream(array.viewBE))
    }
    val compressedGzip = DeflateHandle(gzip = true).use {
        it.deflate(MemoryViewReadableStream(array.viewBE))
    }

    println("com     d: ${'$'}{compressed.asByteArray().toHexadecimal(1)}")
    println("com ref d: ${'$'}{refCompressed.asByteArray().toHexadecimal(1)}")
    //println("com gzip     d: ${'$'}{compressedGzip.asByteArray().toHexadecimal(1)}")
    //println("com gzip ref d: ${'$'}{refCompressedGzip.asByteArray().toHexadecimal(1)}")
    println("com: $${'$'}{compressed == refCompressed}")
    //println("com gzip: ${'$'}{compressedGzip == refCompressedGzip}")

    val decompressed = InflateHandle().use {
        it.inflate(MemoryViewReadableStream(compressed.viewBE))
    }
    /*val decompressedGzip = InflateHandle(any = true).use {
        it.inflate(MemoryViewReadableStream(compressedGzip.viewBE))
    }*/

    println("decom: ${'$'}{array.view == decompressed}")
    //println("decom gzip: ${'$'}{array.view == decompressedGzip}")
}

class DeflateHandle(
    level: Int = -1, internal val bufferSize: Int = 16384,
    gzip: Boolean = false
) : AutoCloseable {
    internal val outputBuffer = ByteArray(bufferSize)
    internal val inputBuffer = MemoryViewStreamDefault()
    internal val deflater =
        Deflater(
            level,
            wrapperType = if (gzip) W_GZIP else W_ZLIB
        )

    constructor(level: Int, bufferSize: Int) : this(level, bufferSize, false)

    fun reset() {
        deflater.first.reset()
        inputBuffer.reset()
    }

    override fun close() {
        deflater.first.end()
    }
}

class InflateHandle(
    internal val bufferSize: Int = 16384,
    any: Boolean = false
) : AutoCloseable {
    internal val outputBuffer = ByteArray(bufferSize)
    internal val inputBuffer = MemoryViewStreamDefault()
    internal val inflater =
        Inflater(wrapperType = if (any) W_ANY else W_ZLIB)

    constructor(bufferSize: Int) : this(bufferSize, false)

    fun reset() {
        inflater.reset()
        inputBuffer.reset()
    }

    override fun close() {
        inflater.end()
    }
}

fun DeflateHandle.deflate(
    input: ReadableByteStream,
    output: WritableByteStream
) {
    var finishing = false
    while (true) {
        if (!finishing && !bufferInput(input)) finishing = true
        while (finishing || deflater.second.avail_in > 0) {
            val status =
                bufferOutput(if (finishing) Z_FINISH else Z_NO_FLUSH, output)
            if (status == Z_STREAM_END) return
            if (status != Z_OK) throw GZIPException(
                "ZLib error: ${'$'}status, ${'$'}{deflater.second.msg}"
            )
        }
    }
}

fun InflateHandle.inflate(
    input: ReadableByteStream,
    output: WritableByteStream
) {
    var finishing = false
    while (true) {
        if (!finishing && !bufferInput(input)) finishing = true
        while (finishing || inflater.first.avail_in > 0) {
            val status =
                bufferOutput(if (finishing) Z_FINISH else Z_NO_FLUSH, output)
            if (status == Z_STREAM_END) return
            if (status != Z_OK) throw GZIPException(
                "ZLib error: ${'$'}status, ${'$'}{inflater.first.msg}"
            )
        }
    }
}

fun DeflateHandle.bufferInput(
    buffer: ReadableByteStream
): Boolean = deflater.second.bufferInput(bufferSize, inputBuffer, buffer)

fun InflateHandle.bufferInput(
    buffer: ReadableByteStream
): Boolean = inflater.first.bufferInput(bufferSize, inputBuffer, buffer)

private fun <B : ByteViewE> ZStream.bufferInput(
    bufferSize: Int,
    inputBuffer: MemoryViewStream<B>,
    buffer: ReadableByteStream
): Boolean {
    inputBuffer.limit(inputBuffer.position() + bufferSize)
    val read = buffer.getSome(inputBuffer.bufferSlice())
    if (read < 0) return false
    inputBuffer.position(inputBuffer.position() + read)
    @Suppress("UNCHECKED_CAST")
    setInput(inputBuffer.buffer().readAsByteArray(), 0, inputBuffer.position())
    return true
}

private fun <B : ByteViewE> z_stream.bufferInput(
    bufferSize: Int,
    inputBuffer: MemoryViewStream<B>,
    buffer: ReadableByteStream
): Boolean {
    inputBuffer.limit(inputBuffer.position() + bufferSize)
    val read = buffer.getSome(inputBuffer.bufferSlice())
    if (read < 0) return false
    inputBuffer.position(inputBuffer.position() + read)
    next_in = inputBuffer.buffer().readAsByteArray()
    next_in_i = 0
    avail_in = inputBuffer.position()
    return true
}

fun DeflateHandle.bufferOutput(
    flush: Int,
    buffer: WritableByteStream
): Int = deflater.second.bufferOutput(inputBuffer, outputBuffer, buffer) {
    deflater.first.deflate(flush)
}

fun InflateHandle.bufferOutput(
    flush: Int,
    buffer: WritableByteStream
): Int = inflater.first.bufferOutput(inputBuffer, outputBuffer, buffer) {
    inflater.inflate(flush)
}

private inline fun ZStream.bufferOutput(
    inputBuffer: MemoryViewStream<*>,
    outputBuffer: ByteArray,
    buffer: WritableByteStream,
    output: () -> Int
): Int {
    setOutput(outputBuffer)
    val status = output()
    buffer.put(outputBuffer.view.slice(0, next_out_index))
    inputBuffer.reset()
    return status
}

private inline fun z_stream.bufferOutput(
    inputBuffer: MemoryViewStream<*>,
    outputBuffer: ByteArray,
    buffer: WritableByteStream,
    output: () -> Int
): Int {
    next_out = outputBuffer
    next_out_i = 0
    avail_out = outputBuffer.size
    val status = output()
    buffer.put(outputBuffer.view.slice(0, next_out_i))
    inputBuffer.reset()
    return status
}

// Common

fun deflate(
    input: ReadableByteStream,
    level: Int = -1
): ByteView = DeflateHandle(level).use { it.deflate(input) }

fun deflate(
    input: ReadableByteStream,
    output: WritableByteStream,
    level: Int = -1
) = DeflateHandle(level).use { it.deflate(input, output) }

fun DeflateHandle.deflate(
    input: ReadableByteStream,
    level: Int = -1
): ByteView {
    val stream = MemoryViewStreamDefault()
    deflate(input, stream)
    stream.flip()
    return stream.bufferSlice()
}

fun inflate(
    input: ReadableByteStream
): ByteView = InflateHandle().use { it.inflate(input) }

fun inflate(
    input: ReadableByteStream,
    output: WritableByteStream
) = InflateHandle().use { it.inflate(input, output) }

fun InflateHandle.inflate(
    input: ReadableByteStream
): ByteView {
    val stream = MemoryViewStreamDefault()
    inflate(input, stream)
    stream.flip()
    return stream.bufferSlice()
}
"""
