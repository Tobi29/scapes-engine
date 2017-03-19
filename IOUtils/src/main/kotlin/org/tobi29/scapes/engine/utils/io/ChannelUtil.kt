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
package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.ByteBuffer
import org.tobi29.scapes.engine.utils.math.min
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel

object ChannelUtil {

    @Throws(IOException::class)
    fun skip(channel: ReadableByteChannel,
             skip: Long): Long {
        if (channel is FileChannel) {
            channel.position(channel.position() + skip)
            return 0
        } else {
            val buffer = ByteBuffer(min(4096, skip).toInt())
            while (skip > 0) {
                buffer.limit(min(buffer.capacity().toLong(), skip).toInt())
                val read = channel.read(buffer)
                if (read == -1) {
                    throw IOException("End of stream")
                }
                if (read == 0) {
                    return skip
                }
            }
            return skip
        }
    }
}
