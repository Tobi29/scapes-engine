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

package org.tobi29.scapes.engine.codec.ogg

import com.jcraft.jogg.Packet
import com.jcraft.jogg.Page
import com.jcraft.jogg.StreamState
import com.jcraft.jogg.SyncState
import com.jcraft.jorbis.Comment
import com.jcraft.jorbis.Info
import org.tobi29.scapes.engine.codec.AudioBuffer
import org.tobi29.scapes.engine.codec.ReadableAudioStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.channels.ReadableByteChannel

class OGGReadStream(private val channel: ReadableByteChannel) : ReadableAudioStream {
    internal val packet = Packet()
    internal val page = Page()
    internal val streamState = StreamState()
    internal val syncState = SyncState()
    internal var initializer: CodecInitializer? = null
    internal var decoder: CodecDecoder? = null
    internal var channels = 0
    internal var rate = 0
    internal var eos = false

    init {
        syncState.init()
    }

    override fun get(buffer: AudioBuffer): Boolean {
        if (decoder == null) {
            if (initializer == null) {
                if (readPage()) {
                    streamState.init(page.serialno())
                    if (streamState.pagein(page) == -1) {
                        throw IOException("Error reading first header page")
                    }
                    if (streamState.packetout(packet) != 1) {
                        throw IOException("Error reading first header packet")
                    }
                    when (decideCodec(packet)) {
                        OGGCodec.VORBIS -> {
                            val info = Info()
                            val comment = Comment()
                            info.synthesis_headerin(comment, packet)
                            channels = info.channels
                            rate = info.rate
                            initializer = VorbisInitializer(info, comment)
                        }
                        OGGCodec.OPUS -> {
                            val info = OpusInfo(packet)
                            channels = info.channels
                            // Sample rate in Opus is always 48000
                            rate = 48000
                            initializer = OpusInitializer(info)
                        }
                        else -> throw IOException(
                                "Invalid header packet, possibly unsupported codec")
                    }
                }
            }
            initializer?.let { initializer ->
                if (readPacket()) {
                    initializer.packet(packet)?.let {
                        decoder = it
                        this.initializer = null
                    }
                }
            }
        }
        decoder?.let { decoder ->
            val pcmBuffer = buffer.buffer(channels, rate)
            while (pcmBuffer.hasRemaining()) {
                if (!decoder.get(pcmBuffer)) {
                    if (packet.e_o_s != 0) {
                        eos = true
                        break
                    }
                    if (readPacket()) {
                        decoder.packet(page, packet)
                    } else {
                        break
                    }
                }
            }
            buffer.done()
        }
        return !eos
    }

    override fun close() {
        try {
            channel.close()
        } catch (e: IOException) {
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

interface CodecInitializer {
    fun packet(packet: Packet): CodecDecoder?
}

interface CodecDecoder {
    fun get(buffer: FloatBuffer): Boolean

    fun packet(page: Page,
               packet: Packet)
}
