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
import org.tobi29.scapes.engine.codec.AudioMetaData
import org.tobi29.scapes.engine.utils.HeapFloatArraySlice
import org.tobi29.scapes.engine.utils.tag.TagMap
import org.tobi29.scapes.engine.utils.tag.toTag

class OpusInitializer(private val info: OpusInfo) : CodecInitializer {
    override fun packet(packet: Packet) = run {
        val comment = OpusComment(packet)
        Pair(OpusReadStream(info), {
            AudioMetaData(comment.vendor,
                    TagMap {
                        comment.userComments.forEach { (key, value) ->
                            this[key] = value.toTag()
                        }
                    })
        })
    }
}

class OpusReadStream(info: OpusInfo) : CodecDecoder {
    private val channels = info.channels
    private val granuleOffset = info.preSkip.toLong()
    private val decoder = org.concentus.OpusMSDecoder.create(48000,
            info.channels, info.streams, info.coupledStreams,
            info.streamMap).apply {
        gain = info.gain
    }
    private val pcm = ShortArray(MAX_FRAME_SIZE * channels)
    private var preSkip = info.preSkip
    private var out = 0L
    private var pcmOffset = 0
    private var pcmLength = 0
    private var granulePos = 0L

    override fun get(buffer: HeapFloatArraySlice): Int {
        while (true) {
            if (pcmOffset >= pcmLength) {
                return 0
            }
            if (preSkip > 0) {
                val skip = preSkip.coerceAtMost(pcmLength - pcmOffset)
                pcmOffset += skip
                preSkip -= skip
            }
            if (preSkip == 0) {
                val granuleLimit = (granulePos - granuleOffset - out)
                if (granuleLimit <= 0L) {
                    return 0
                }
                val size = granuleLimit
                        .coerceAtMost((pcmLength - pcmOffset).toLong()).toInt()
                        .coerceAtMost(buffer.size / channels)
                var j = 0
                for (i in pcmOffset * channels until (pcmOffset + size) * channels) {
                    buffer[j++] = pcm[i] / Short.MAX_VALUE.toFloat()
                }
                out += size
                pcmOffset += size
                return j
            }
        }
    }

    override fun packet(page: Page,
                        packet: Packet) {
        pcmLength = decoder.decodeMultistream(packet.packet_base, packet.packet,
                packet.bytes, pcm, 0, MAX_FRAME_SIZE, 0)
        pcmOffset = 0
        granulePos = page.granulepos()
    }

    companion object {
        private const val MAX_FRAME_SIZE = 960 * 6
    }
}
