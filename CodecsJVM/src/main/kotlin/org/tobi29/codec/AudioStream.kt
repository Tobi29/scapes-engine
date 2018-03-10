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

package org.tobi29.codec

import org.tobi29.codec.spi.ReadableAudioStreamProvider
import org.tobi29.io.IOException
import org.tobi29.io.ReadableByteChannel
import org.tobi29.logging.KLogging
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.utils.spiLoad
import org.tobi29.utils.spiLoadFirst

object AudioStream : KLogging() {
    private val CODECS = ConcurrentHashMap<String, ReadableAudioStreamProvider>()

    // TODO: @Throws(IOException::class)
    fun create(channel: ReadableByteChannel,
               mime: String): ReadableAudioStream {
        val codec = AudioStream[mime]
        if (codec != null) return codec[channel]
        throw IOException("No compatible decoder found for type: $mime")
    }

    fun playable(mime: String): Boolean {
        return AudioStream[mime] != null
    }

    private operator fun get(mime: String): ReadableAudioStreamProvider? {
        var codec = AudioStream.CODECS[mime]
        if (codec == null) {
            codec = AudioStream.loadService(mime)
            if (codec != null) {
                AudioStream.CODECS.put(mime, codec)
            }
        }
        return codec
    }

    private fun loadService(mime: String): ReadableAudioStreamProvider? =
            spiLoadFirst(
                    spiLoad<ReadableAudioStreamProvider>(
                            AudioStream::class.java.classLoader), { e ->
                logger.warn(e) { "Service configuration error" }
            }, { it.accepts(mime) })
}
