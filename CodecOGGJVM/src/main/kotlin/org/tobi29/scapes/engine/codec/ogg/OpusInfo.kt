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

import com.jcraft.jogg.Buffer
import com.jcraft.jogg.Packet
import org.tobi29.scapes.engine.utils.io.IOException

class OpusInfo(packet: Packet) {
    val channels: Int
    val preSkip: Int
    val rate: Int
    val gain: Int
    val streams: Int
    val coupledStreams: Int
    val streamMap: ShortArray

    init {
        val buffer = Buffer()
        buffer.readinit(packet.packet_base, packet.packet, packet.bytes)
        val magic = ByteArray(8)
        buffer.read(magic, 8)
        if (magic[0] != 'O'.toByte() ||
                magic[1] != 'p'.toByte() ||
                magic[2] != 'u'.toByte() ||
                magic[3] != 's'.toByte() ||
                magic[4] != 'H'.toByte() ||
                magic[5] != 'e'.toByte() ||
                magic[6] != 'a'.toByte() ||
                magic[7] != 'd'.toByte()) {
            throw IOException("Invalid header signature")
        }
        if (buffer.read(8) != 1) {
            // We only support version 1
            throw IOException("Unsupported Opus version")
        }
        channels = buffer.read(8)
        if (channels <= 0) {
            throw IOException("Invalid channel amount: $channels")
        }
        preSkip = buffer.read(16)
        rate = buffer.read(32)
        gain = buffer.read(16)
        val channelMappingType = buffer.read(8)
        if (channelMappingType != 0) {
            streams = buffer.read(8)
            if (streams <= 0) {
                throw IOException("Invalid stream amount: $streams")
            }
            coupledStreams = buffer.read(8)
            if (coupledStreams > streams || coupledStreams + streams > 255) {
                throw IOException(
                        "Invalid stream and coupled stream amount: $streams + $coupledStreams")
            }
            streamMap = ShortArray(channels) { buffer.read(8).toShort() }
        } else {
            streams = 1
            coupledStreams = if (channels > 1) 1 else 0
            streamMap = shortArrayOf(0, 1)
        }
    }
}
