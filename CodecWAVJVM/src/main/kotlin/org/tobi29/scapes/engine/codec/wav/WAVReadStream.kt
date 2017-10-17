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

// Based on: http://www.labbookpages.co.uk/audio/wavFiles.html
package org.tobi29.scapes.engine.codec.wav

import org.tobi29.scapes.engine.codec.AudioBuffer
import org.tobi29.scapes.engine.codec.AudioMetaData
import org.tobi29.scapes.engine.codec.ReadableAudioStream
import org.tobi29.scapes.engine.utils.io.*

class WAVReadStream(private val channel: ReadableByteChannel) : ReadableAudioStream {
    private val buffer = MemoryViewStream(
            ByteArray(BUFFER_SIZE).viewLE).apply { limit(12) }
    private var channels = 0
    private var rate = 0
    private var align = 0
    private var format: Format? = null
    private var bits = 0
    private var bytes = 0
    private var offset = 0.0f
    private var scale = 0.0f
    private var state: (() -> Boolean)? = null
    private var eos = false
    override val metaData = AudioMetaData(null, null)

    init {
        state = { this.init1() }
    }

    private fun skip(skip: Long,
                     next: () -> (() -> Boolean)?): Boolean {
        val newSkip = skip - channel.skip(skip)
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
            val riffTypeID = buffer.getInt().toLong()
            if (header.id != RIFF_CHUNK_ID) {
                throw IOException(
                        "Invalid Wav Header data, incorrect riff chunk ID")
            }
            if (riffTypeID != RIFF_TYPE_ID.toLong()) {
                throw IOException(
                        "Invalid Wav Header data, incorrect riff type ID")
            }
            buffer.reset()
            buffer.limit(8)
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
                    buffer.reset()
                    buffer.limit(16)
                    state = { init3(chunk) }
                    return true
                }
                else -> {
                    state = {
                        skip(chunk.bytes.toLong(), {
                            buffer.reset()
                            buffer.limit(8);
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
            val formatCode = buffer.getShort().toInt()
            format = when (formatCode) {
                1 -> Format.PCM
                3 -> Format.IEEE
                else -> throw IOException(
                        "Format Code $formatCode not supported")
            }
            channels = buffer.getShort().toInt()
            rate = buffer.getInt()
            buffer.position(12)
            align = buffer.getShort().toInt()
            bits = buffer.getShort().toInt()
            val bytes = bits + 7 shr 3
            if (bytes * channels != align) {
                throw IOException(
                        "Block Align does not agree with bytes required for validBits and number of channels")
            }
            when (format) {
                Format.PCM -> if (bits != 8 && bits != 16 && bits != 24 && bits != 32 && bits != 64) {
                    throw IOException("$bits bit pcm data not supported")
                }
                Format.IEEE -> if (bits != 32 && bits != 64) {
                    throw IOException("$bits bit float data not supported")
                }
            }
            sanityCheck()
            state = {
                skip((chunk.bytes - 16).toLong(), {
                    buffer.reset()
                    buffer.limit(8);
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
                            buffer.reset()
                            buffer.limit(8);
                            { this.init4() }
                        })
                    }
                    return true
                }
            }
        }
        return false
    }

    override fun get(buffer: AudioBuffer?): ReadableAudioStream.Result {
        while (state?.invoke() == true) {
        }
        if (state != null) {
            return if (eos) ReadableAudioStream.Result.EOS else
                ReadableAudioStream.Result.YIELD
        }
        if (buffer == null) {
            return ReadableAudioStream.Result.BUFFER
        }
        val pcmBuffer = buffer.buffer(channels, rate)
        var i = 0
        while (i < pcmBuffer.size && !eos) {
            if (this.buffer.remaining() < bits shr 3) {
                this.buffer.compact()
                val read = channel.read(this.buffer)
                if (read == -1) {
                    eos = true
                    break
                }
                this.buffer.flip()
                if (read == 0) {
                    return ReadableAudioStream.Result.YIELD
                }
            }
            if (!eos) {
                when (format) {
                    Format.PCM -> {
                        when (bits) {
                            8 -> pcmBuffer[i++] =
                                    offset + (this.buffer.get().toInt() and 0xFF) / scale
                            16 -> pcmBuffer[i++] =
                                    offset + this.buffer.getShort() / scale
                            24 -> pcmBuffer[i++] =
                                    offset + this.buffer.get24Bit() / scale
                            32 -> pcmBuffer[i++] =
                                    offset + this.buffer.getInt() / scale
                            64 -> pcmBuffer[i++] =
                                    offset + this.buffer.getLong() / scale
                            else -> throw IllegalStateException(
                                    "Invalid bits: $bits")
                        }
                    }
                    Format.IEEE -> {
                        when (bits) {
                            32 -> pcmBuffer[i++] =
                                    this.buffer.getFloat()
                            64 -> pcmBuffer[i++] =
                                    this.buffer.getDouble().toFloat()
                            else -> throw IllegalStateException(
                                    "Invalid bits: $bits")
                        }
                    }
                }
            }
        }
        buffer.done(i)
        return if (eos) ReadableAudioStream.Result.EOS else
            ReadableAudioStream.Result.BUFFER
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

    private data class Chunk(val id: Int,
                             val size: Int,
                             val bytes: Int)

    private fun chunk(buffer: MemoryViewStream<*>): Chunk {
        val chunkID = buffer.getInt()
        val chunkSize = buffer.getInt()
        val numChunkBytes = if (chunkSize % 2 == 0) chunkSize else chunkSize + 1
        return Chunk(chunkID, chunkSize, numChunkBytes)
    }

    companion object {
        private val BUFFER_SIZE = 4096
        private val FMT_CHUNK_ID = 0x20746D66
        private val DATA_CHUNK_ID = 0x61746164
        private val RIFF_CHUNK_ID = 0x46464952
        private val RIFF_TYPE_ID = 0x45564157

        private fun MemoryViewStream<ByteViewLE>.get24Bit(): Int {
            val low = get().toInt() and 0xFF
            val high = getShort().toInt() shl 8
            return high or low
        }
    }

    private enum class Format {
        PCM,
        IEEE
    }
}
