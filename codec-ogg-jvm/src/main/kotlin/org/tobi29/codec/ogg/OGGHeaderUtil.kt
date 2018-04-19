/*
 * Copyright 2012-2018 Tobi29
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

import com.jcraft.jogg.Buffer
import com.jcraft.jogg.Packet

fun decideCodec(op: Packet): OGGCodec? {
    val opb = Buffer()
    for (codec in OGGCodec.values()) {
        opb.readinit(op.packet_base, op.packet, op.bytes)
        val buffer = ByteArray(codec.header.size)
        opb.read(buffer, buffer.size)
        if (codec.header contentEquals buffer) {
            return codec
        }
    }
    return null
}

enum class OGGCodec(internal val header: ByteArray) {
    VORBIS(byteArrayOf(1,
            *charArrayOf('v', 'o', 'r', 'b', 'i', 's').toByteArray())),
    OPUS(charArrayOf('O', 'p', 'u', 's', 'H', 'e', 'a', 'd').toByteArray())
}

private fun CharArray.toByteArray() = ByteArray(size) { this[it].toByte() }
