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

package org.tobi29.scapes.engine.utils.codec.ogg

import com.jcraft.jogg.Packet
import com.jcraft.jogg.Page
import com.jcraft.jogg.StreamState
import com.jcraft.jogg.SyncState
import com.jcraft.jorbis.Block
import com.jcraft.jorbis.Comment
import com.jcraft.jorbis.DspState
import com.jcraft.jorbis.Info
import org.tobi29.scapes.engine.utils.codec.AudioBuffer
import org.tobi29.scapes.engine.utils.codec.ReadableAudioStream
import org.tobi29.scapes.engine.utils.math.min
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.channels.ReadableByteChannel

class OGGReadStream(private val channel: ReadableByteChannel) : ReadableAudioStream {
    private val packet = Packet()
    private val page = Page()
    private val streamState = StreamState()
    private val syncState = SyncState()
    private val dspState = DspState()
    private val block = Block(dspState)
    private val comment = Comment()
    private val info = Info()
    private val pcm = arrayOfNulls<Array<FloatArray>>(1)
    private var state: (() -> Boolean)? = null
    private var channels = 0
    private var rate = 0
    private var index: IntArray = IntArray(0)
    private var eos = false

    init {
        syncState.init()
        info.init()
        comment.init()
        state = { this.init1() }
    }

    private fun init1(): Boolean {
        if (readPage()) {
            streamState.init(page.serialno())
            if (streamState.pagein(page) == -1) {
                throw IOException("Error reading first header page")
            }
            if (streamState.packetout(packet) != 1) {
                throw IOException("Error reading first header packet")
            }
            if (info.synthesis_headerin(comment, packet) < 0) {
                throw IOException("Error interpreting first header packet")
            }
            state = { this.init2() }
            return true
        }
        return false
    }

    private fun init2(): Boolean {
        if (readPacket()) {
            info.synthesis_headerin(comment, packet)
            state = { this.init3() }
            return true
        }
        return false
    }

    private fun init3(): Boolean {
        if (readPacket()) {
            info.synthesis_headerin(comment, packet)
            dspState.synthesis_init(info)
            block.init(dspState)
            channels = info.channels
            rate = info.rate
            index = IntArray(info.channels)
            state = null
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
            if (!decodePacket(pcmBuffer)) {
                break
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

    private fun decodePacket(buffer: FloatBuffer): Boolean {
        while (true) {
            val samples = dspState.synthesis_pcmout(pcm, index)
            if (samples == 0) {
                if (!readPacket()) {
                    return false
                }
                if (block.synthesis(packet) != 0) {
                    return true
                }
                dspState.synthesis_blockin(block)
                continue
            }
            val pcmSamples = pcm[0] ?: throw IllegalStateException(
                    "Null in pcm array, JOrbis bug?")
            val length = min(samples, buffer.remaining() / channels)
            val offset = buffer.position()
            for (i in 0..channels - 1) {
                val channel = pcmSamples[i]
                val location = index[i]
                var position = offset + i
                for (j in 0..length - 1) {
                    buffer.put(position, channel[location + j])
                    position += channels
                }
            }
            buffer.position(offset + length * channels)
            dspState.synthesis_read(length)
            return true
        }
    }

    private fun readPacket(): Boolean {
        while (true) {
            when (streamState.packetout(packet)) {
                -1 -> throw IOException("Hole in packet")
                1 -> return true
            }
            if (!readPage()) {
                return false
            }
            streamState.pagein(page)
        }
    }

    private fun readPage(): Boolean {
        while (true) {
            when (syncState.pageout(page)) {
                -1 -> throw IOException("Hole in page")
                1 -> return true
            }
            if (!fillBuffer()) {
                return false
            }
        }
    }

    private fun fillBuffer(): Boolean {
        val offset = syncState.buffer(BUFFER_SIZE)
        val read = channel.read(
                ByteBuffer.wrap(syncState.data, offset, BUFFER_SIZE))
        if (read == -1) {
            eos = true
        }
        if (read <= 0) {
            return false
        }
        syncState.wrote(read)
        return true
    }

    companion object {
        private val BUFFER_SIZE = 4096
    }
}
