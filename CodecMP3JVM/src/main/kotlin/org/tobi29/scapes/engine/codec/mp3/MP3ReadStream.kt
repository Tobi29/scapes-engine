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

package org.tobi29.scapes.engine.codec.mp3

import javazoom.jl.decoder.*
import org.tobi29.scapes.engine.codec.AudioBuffer
import org.tobi29.scapes.engine.codec.AudioMetaData
import org.tobi29.scapes.engine.codec.ReadableAudioStream
import org.tobi29.scapes.engine.utils.io.ByteViewBE
import org.tobi29.scapes.engine.utils.io.*

class MP3ReadStream(private val channel: ReadableByteChannel) : ReadableAudioStream {
    private val bitstream = Bitstream(
            Channels.newInputStream(channel.toJavaChannel()))
    private val decoder = Decoder()
    private val output = OutputBuffer()
    private var channels = 0
    private var initialized = false
    private var rate = 0
    private var outputRate = 0
    private var eos = false
    private var outputPosition = 0
    override val metaData = AudioMetaData(null, null)

    private fun getSampleRate(header: Header): Int {
        val version = header.version()
        when (header.sample_frequency()) {
            0 -> {
                if (version == 1) {
                    return 44100
                } else if (version == 0) {
                    return 22050
                }
                return 11025
            }
            1 -> {
                if (version == 1) {
                    return 48000
                } else if (version == 0) {
                    return 24000
                }
                return 12000
            }
            2 -> {
                if (version == 1) {
                    return 32000
                } else if (version == 0) {
                    return 16000
                }
                return 8000
            }
        }
        return 0
    }

    override fun get(buffer: AudioBuffer?): ReadableAudioStream.Result {
        if (!initialized) {
            val header = readFrame()
            if (header == null) {
                throw IOException("Unable to read first frame")
            } else {
                channels = if (header.mode() == Header.SINGLE_CHANNEL) 1 else 2
                decoder.setOutputBuffer(output)
                decodeFrame(header)
                outputRate = rate
            }
            initialized = true
        }
        if (buffer == null) {
            return ReadableAudioStream.Result.BUFFER
        }
        val pcmBuffer = buffer.buffer(channels, rate)
        while (pcmBuffer.hasRemaining() && !eos) {
            if (!decodeFrame(pcmBuffer)) {
                break
            }
        }
        buffer.done()
        outputRate = rate
        return if (eos) ReadableAudioStream.Result.EOS else
            ReadableAudioStream.Result.BUFFER
    }

    override fun close() {
        try {
            channel.close()
        } catch (e: IOException) {
        }
    }

    private fun decodeFrame(buffer: MemoryViewStream<ByteViewBE>): Boolean {
        if (outputRate != rate) {
            // TODO: Need to git an mp3 file that actually does this to test
            return false
        }
        if (!checkFrame()) {
            return false
        }
        val len = buffer.remaining().coerceAtMost(
                output.size - outputPosition)
        buffer.put(output.buffer.slice(outputPosition, len))
        outputPosition += len
        return true
    }

    private fun checkFrame(): Boolean {
        if (outputPosition >= output.size) {
            val header = readFrame()
            if (header == null) {
                eos = true
                return false
            }
            decodeFrame(header)
        }
        return true
    }

    private fun readFrame(): Header? {
        try {
            return bitstream.readFrame()
        } catch (e: BitstreamException) {
            throw IOException(e)
        }
    }

    private fun decodeFrame(header: Header) {
        try {
            outputPosition = 0
            rate = getSampleRate(header)
            decoder.decodeFrame(header, bitstream)
            bitstream.closeFrame()
        } catch (e: DecoderException) {
            throw IOException(e)
        }
    }

    private class OutputBuffer : Obuffer() {
        val buffer = (OBUFFERSIZE * MAXCHANNELS).let {
            HeapViewFloatBE(FloatArray(it), 0, it shl 2)
        }
        val index = IntArray(Obuffer.MAXCHANNELS)
        inline val size get() = index[0]
        private val increment = index.size shl 2

        init {
            clear_buffer()
        }

        override fun append(channel: Int,
                            value: Short) {
            buffer.setFloat(index[channel], value.toFloat() / Short.MAX_VALUE)
            index[channel] += increment
        }

        override fun appendSamples(channel: Int,
                                   f: FloatArray) {
            for (sample in f) {
                buffer.setFloat(index[channel], sample / Short.MAX_VALUE)
                index[channel] += increment
            }
        }

        override fun write_buffer(`val`: Int) {
        }

        override fun close() {
        }

        override fun clear_buffer() {
            for (i in index.indices) {
                index[i] = i shl 2
            }
        }

        override fun set_stop_flag() {
        }
    }
}
