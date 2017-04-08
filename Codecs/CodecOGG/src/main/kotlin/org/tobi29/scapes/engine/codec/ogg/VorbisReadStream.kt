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
import com.jcraft.jorbis.Block
import com.jcraft.jorbis.Comment
import com.jcraft.jorbis.DspState
import com.jcraft.jorbis.Info
import mu.KLogging
import org.tobi29.scapes.engine.codec.AudioMetaData
import org.tobi29.scapes.engine.utils.math.min
import org.tobi29.scapes.engine.utils.tag.TagMap
import org.tobi29.scapes.engine.utils.tag.set
import java.nio.FloatBuffer

class VorbisInitializer(private val info: Info,
                        private val comment: Comment) : CodecInitializer {
    private var state = State.HEADER

    override fun packet(packet: Packet) = when (state) {
        State.HEADER -> {
            info.synthesis_headerin(comment, packet)
            state = State.READY
            null
        }
        State.READY -> {
            val dspState = DspState()
            val block = Block(dspState)
            info.synthesis_headerin(comment, packet)
            dspState.synthesis_init(info)
            block.init(dspState)
            Pair(VorbisReadStream(info, dspState, block), {
                AudioMetaData(comment.getVendor(),
                        TagMap {
                            (0..comment.comments - 1).asSequence().map {
                                comment.getComment(it).split('=', limit = 2)
                            }.filter { it.size == 2 }.forEach { this[it[0]] = it[1] }
                        })
            })
        }
    }

    private enum class State {
        HEADER,
        READY
    }
}

class VorbisReadStream(info: Info,
                       private val dspState: DspState,
                       private val block: Block) : CodecDecoder {
    private val channels = info.channels
    private val index = IntArray(info.channels)
    private val pcm = arrayOfNulls<Array<FloatArray>>(1)

    override fun get(buffer: FloatBuffer): Boolean {
        val samples = dspState.synthesis_pcmout(pcm, index)
        if (samples == 0) {
            return false
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

    override fun packet(page: Page,
                        packet: Packet) {
        if (block.synthesis(packet) != 0) {
            return
        }
        dspState.synthesis_blockin(block)
    }

    companion object : KLogging()
}
