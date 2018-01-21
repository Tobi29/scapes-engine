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

package org.tobi29.codec.ogg

import com.jcraft.jogg.Packet
import com.jcraft.jogg.Page
import com.jcraft.jorbis.Block
import com.jcraft.jorbis.Comment
import com.jcraft.jorbis.DspState
import com.jcraft.jorbis.Info
import org.tobi29.arrays.HeapFloatArraySlice
import org.tobi29.logging.KLogging
import org.tobi29.codec.AudioMetaData
import org.tobi29.io.tag.TagMap
import org.tobi29.io.tag.toTag
import kotlin.collections.asSequence
import kotlin.collections.set

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
                            (0 until comment.comments).asSequence().map {
                                comment.getComment(it).split('=', limit = 2)
                            }.filter { it.size == 2 }.forEach {
                                this[it[0]] = it[1].toTag()
                            }
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

    override fun get(buffer: HeapFloatArraySlice): Int {
        val samples = dspState.synthesis_pcmout(pcm, index)
        if (samples == 0) {
            return 0
        }
        val pcmSamples = pcm[0] ?: throw IllegalStateException(
                "Null in pcm array, JOrbis bug?")
        val length = samples.coerceAtMost(buffer.size / channels)
        for (i in 0 until channels) {
            val channel = pcmSamples[i]
            val location = index[i]
            var position = i
            for (j in 0 until length) {
                buffer[position] = channel[location + j]
                position += channels
            }
        }
        dspState.synthesis_read(length)
        return length * channels
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
