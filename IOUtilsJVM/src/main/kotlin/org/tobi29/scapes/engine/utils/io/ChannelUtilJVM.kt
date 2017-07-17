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

import java.nio.channels.FileChannel

// TODO: @Throws(IOException::class)
impl fun ReadableByteChannel.skip(skip: Long): Long {
    if (this is FileChannel) {
        position(position() + skip)
        return 0
    } else {
        val buffer = ByteBuffer((skip.coerceAtMost(4096)).toInt())
        while (skip > 0) {
            buffer.limit(skip.coerceAtMost(buffer.capacity().toLong()).toInt())
            val read = read(buffer)
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
