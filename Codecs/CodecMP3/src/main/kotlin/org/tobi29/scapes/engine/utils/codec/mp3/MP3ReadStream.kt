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

package org.tobi29.scapes.engine.utils.codec.mp3

import javazoom.jl.decoder.*
import org.tobi29.scapes.engine.utils.BufferCreator
import org.tobi29.scapes.engine.utils.codec.AudioBuffer
import org.tobi29.scapes.engine.utils.codec.ReadableAudioStream
import org.tobi29.scapes.engine.utils.math.min
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

class MP3ReadStream @Throws(IOException::class)
constructor(private val channel: ReadableByteChannel) : ReadableAudioStream {
    private val decoder: Decoder
    private val bitstream: Bitstream
    private val output: OutputBuffer
    private val channels: Int
    private var rate = 0
    private var outputRate = 0
    private var eos = false

    init {
        bitstream = Bitstream(Channels.newInputStream(channel))
        decoder = Decoder()
        val header = readFrame()
        if (header == null) {
            throw IOException("Unable to read first frame")
        } else {
            channels = if (header.mode() == Header.SINGLE_CHANNEL) 1 else 2
            output = OutputBuffer()
            decoder.setOutputBuffer(output)
            decodeFrame(header)
            outputRate = rate
        }
    }

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

    override fun get(buffer: AudioBuffer): Boolean {
        val pcmBuffer = buffer.buffer(channels, rate)
        while (pcmBuffer.hasRemaining() && !eos) {
            if (!decodeFrame(pcmBuffer)) {
                break
            }
        }
        buffer.done()
        outputRate = rate
        return !eos
    }

    override fun close() {
        try {
            channel.close()
        } catch (e: IOException) {
        }

    }

    private fun decodeFrame(buffer: FloatBuffer): Boolean {
        if (outputRate != rate) {
            // TODO: Need to git an mp3 file that actually does this to test
            return false
        }
        if (!checkFrame()) {
            return false
        }
        val len = min(buffer.remaining(), output.buffer.remaining())
        val limit = output.buffer.limit()
        output.buffer.limit(output.buffer.position() + len)
        buffer.put(output.buffer)
        output.buffer.limit(limit)
        checkFrame()
        return true
    }

    private fun checkFrame(): Boolean {
        if (!output.buffer.hasRemaining()) {
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
            rate = getSampleRate(header)
            output.buffer.clear()
            decoder.decodeFrame(header, bitstream)
            output.buffer.limit(output.index[0])
            bitstream.closeFrame()
        } catch (e: DecoderException) {
            throw IOException(e)
        }

    }

    private class OutputBuffer : Obuffer() {
        val buffer: FloatBuffer
        val index: IntArray

        init {
            buffer = BufferCreator.floats(
                    Obuffer.OBUFFERSIZE * Obuffer.MAXCHANNELS)
            index = IntArray(Obuffer.MAXCHANNELS)
            clear_buffer()
        }

        override fun append(channel: Int,
                            value: Short) {
            buffer.put(index[channel], value.toFloat() / Short.MAX_VALUE)
            index[channel] += index.size
        }

        override fun appendSamples(channel: Int,
                                   f: FloatArray) {
            for (sample in f) {
                buffer.put(index[channel], sample / Short.MAX_VALUE)
                index[channel] += index.size
            }
        }

        override fun write_buffer(`val`: Int) {
        }

        override fun close() {
        }

        override fun clear_buffer() {
            for (i in index.indices) {
                index[i] = i
            }
        }

        override fun set_stop_flag() {
        }
    }
}
