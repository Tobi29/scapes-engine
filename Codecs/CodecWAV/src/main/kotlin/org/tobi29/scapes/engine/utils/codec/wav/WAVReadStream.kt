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

// Based on: http://www.labbookpages.co.uk/audio/wavFiles.html
package org.tobi29.scapes.engine.utils.codec.wav

import org.tobi29.scapes.engine.utils.BufferCreator
import org.tobi29.scapes.engine.utils.codec.AudioBuffer
import org.tobi29.scapes.engine.utils.codec.ReadableAudioStream
import org.tobi29.scapes.engine.utils.io.ChannelUtil
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.ReadableByteChannel

class WAVReadStream(private val channel: ReadableByteChannel) : ReadableAudioStream {
    private val buffer = BufferCreator.bytes(BUFFER_SIZE).order(
            ByteOrder.LITTLE_ENDIAN)
    private var channels = 0
    private var rate = 0
    private var align = 0
    private var bits = 0
    private var bytes = 0
    private var offset = 0.0f
    private var scale = 0.0f
    private var state: (() -> Boolean)? = null
    private var eos = false

    init {
        buffer.clear().limit(12)
        state = { this.init1() }
    }

    private fun skip(skip: Long,
                     next: () -> (() -> Boolean)?): Boolean {
        val newSkip = ChannelUtil.skip(channel, skip)
        if (newSkip == 0L) {
            state = next()
            return true
        }
        state = { skip(newSkip, next) }
        return false
    }

    private fun init1(): Boolean {
        if (channel.read(buffer) == -1) {
            throw IOException("End of stream during header")
        }
        if (!buffer.hasRemaining()) {
            buffer.flip()
            val header = chunk(buffer)
            val riffTypeID = buffer.int.toLong()
            if (header.id != RIFF_CHUNK_ID) {
                throw IOException(
                        "Invalid Wav Header data, incorrect riff chunk ID")
            }
            if (riffTypeID != RIFF_TYPE_ID.toLong()) {
                throw IOException(
                        "Invalid Wav Header data, incorrect riff type ID")
            }
            buffer.clear().limit(8)
            state = { this.init2() }
            return true
        }
        return false
    }

    private fun init2(): Boolean {
        if (channel.read(buffer) == -1) {
            throw IOException("End of stream during header")
        }
        if (!buffer.hasRemaining()) {
            buffer.flip()
            val chunk = chunk(buffer)
            when (chunk.id) {
                FMT_CHUNK_ID -> {
                    buffer.clear().limit(16)
                    state = { init3(chunk) }
                    return true
                }
                else -> {
                    state = {
                        skip(chunk.bytes.toLong(), {
                            buffer.clear().limit(8);
                            { this.init2() }
                        })
                    }
                    return true
                }
            }
        }
        return false
    }

    private fun init3(chunk: Chunk): Boolean {
        if (channel.read(buffer) == -1) {
            throw IOException("End of stream during header")
        }
        if (!buffer.hasRemaining()) {
            buffer.flip()
            val compressionCode = buffer.short.toInt()
            if (compressionCode != 1) {
                throw IOException("Compression Code " + compressionCode +
                        " not supported")
            }
            channels = buffer.short.toInt()
            rate = buffer.int
            buffer.position(12)
            align = buffer.short.toInt()
            bits = buffer.short.toInt()
            val bytes = bits + 7 shr 3
            if (bytes * channels != align) {
                throw IOException(
                        "Block Align does not agree with bytes required for validBits and number of channels")
            }
            sanityCheck()
            state = {
                skip((chunk.bytes - 16).toLong(), {
                    buffer.clear().limit(8);
                    { this.init4() }
                })
            }
            return true
        }
        return false
    }

    private fun init4(): Boolean {
        if (channel.read(buffer) == -1) {
            throw IOException("End of stream during header")
        }
        if (!buffer.hasRemaining()) {
            buffer.flip()
            val chunk = chunk(buffer)
            when (chunk.id) {
                DATA_CHUNK_ID -> {
                    data(chunk)
                    bytes = bits + 7 shr 3
                    if (bits > 8) {
                        offset = 0.0f
                        scale = (1 shl bits - 1).toFloat()
                    } else {
                        offset = -1.0f
                        scale = 0.5f * ((1 shl bits) - 1)
                    }
                    state = null
                    return false
                }
                else -> {
                    state = {
                        skip(chunk.bytes.toLong(), {
                            buffer.clear().limit(8);
                            { this.init4() }
                        })
                    }
                    return true
                }
            }
        }
        return false
    }

    override fun get(buffer: AudioBuffer): Boolean {
        while (state?.invoke() ?: false) {
        }
        if (state != null) {
            return !eos
        }
        val pcmBuffer = buffer.buffer(channels, rate)
        while (pcmBuffer.hasRemaining() && !eos) {
            var value: Long = 0
            for (b in 0..bytes - 1) {
                if (!this.buffer.hasRemaining()) {
                    this.buffer.clear()
                    if (channel.read(this.buffer) == -1) {
                        eos = true
                        break
                    }
                    this.buffer.flip()
                }
                var v = this.buffer.get().toInt()
                if (b < bytes - 1 || bytes == 1) {
                    v = v and 0xFF
                }
                value += (v shl (b shl 3)).toLong()
            }
            if (!eos) {
                pcmBuffer.put(offset + value / scale)
            }
        }
        buffer.done()
        return !eos
    }

    override fun close() {
        try {
            channel.close()
        } catch (e: IOException) {
        }
    }

    private fun data(chunk: Chunk) {
        if (chunk.size % align != 0) {
            throw IOException(
                    "Data Chunk size is not multiple of Block Align")
        }
    }

    private fun sanityCheck() {
        if (channels <= 0) {
            throw IOException(
                    "Number of channels specified in header is equal to zero")
        }
        if (align == 0) {
            throw IOException(
                    "Block Align specified in header is equal to zero")
        }
        if (bits < 2) {
            throw IOException(
                    "Valid Bits specified in header is less than 2")
        }
        if (bits > 64) {
            throw IOException(
                    "Valid Bits specified in header is greater than 64, this is greater than a long can hold")
        }
    }

    private data class Chunk(val id: Int, val size: Int, val bytes: Int)

    private fun chunk(buffer: ByteBuffer): Chunk {
        val chunkID = buffer.int
        val chunkSize = buffer.int
        val numChunkBytes = if (chunkSize % 2 == 0) chunkSize else chunkSize + 1
        return Chunk(chunkID, chunkSize, numChunkBytes)
    }

    companion object {
        private val BUFFER_SIZE = 4096
        private val FMT_CHUNK_ID = 0x20746D66
        private val DATA_CHUNK_ID = 0x61746164
        private val RIFF_CHUNK_ID = 0x46464952
        private val RIFF_TYPE_ID = 0x45564157
    }
}