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
import org.tobi29.scapes.engine.utils.utf8ToString

class OpusComment(packet: Packet) {
    private val userCommentDatas = ArrayList<ByteArray>()
    private val userCommentSequence = userCommentDatas.asSequence().mapNotNull { comment ->
        (0..comment.lastIndex).asSequence()
                .filter { comment[it] == '='.toByte() }.firstOrNull()
                ?.let {
                    Pair(String(comment, 0, it),
                            String(comment, it + 1, comment.size - (it + 1)))
                }
    }
    private val vendorData: ByteArray

    init {
        val buffer = Buffer()
        buffer.readinit(packet.packet_base, packet.packet, packet.bytes)
        val magic = ByteArray(8)
        buffer.read(magic, 8)
        if (magic[0] != 'O'.toByte() ||
                magic[1] != 'p'.toByte() ||
                magic[2] != 'u'.toByte() ||
                magic[3] != 's'.toByte() ||
                magic[4] != 'T'.toByte() ||
                magic[5] != 'a'.toByte() ||
                magic[6] != 'g'.toByte() ||
                magic[7] != 's'.toByte()) {
            throw IOException("Invalid header signature")
        }
        val vendorLength = buffer.read(32)
        if (vendorLength < 0) {
            throw IOException("Invalid vendor length: $vendorLength")
        }
        vendorData = ByteArray(vendorLength + 1)
        buffer.read(vendorData, vendorLength)
        val comments = buffer.read(32)
        if (comments < 0) {
            throw IOException("Invalid comment count: $vendorLength")
        }
        for (i in 0 until comments) {
            val len = buffer.read(32)
            if (len < 0) {
                throw IOException("Invalid comment length: $vendorLength")
            }
            userCommentDatas.add(ByteArray(len + 1).also {
                buffer.read(it, len)
            })
        }
    }

    val vendor by lazy { vendorData.utf8ToString() }

    val userComments by lazy { userCommentSequence.toList() }

    fun query(tag: String) =
            userComments.filter { (key, _) ->
                tag.equals(key, ignoreCase = true)
            }
}
