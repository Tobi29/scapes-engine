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

package org.tobi29.scapes.engine.codec

import org.tobi29.scapes.engine.codec.spi.ReadableAudioStreamProvider
import org.tobi29.scapes.engine.utils.ConcurrentHashMap
import org.tobi29.scapes.engine.utils.IOException
import org.tobi29.scapes.engine.utils.io.ReadSource
import org.tobi29.scapes.engine.utils.logging.KLogging
import java.util.*

object AudioStream : KLogging() {
    private val CODECS = ConcurrentHashMap<String, ReadableAudioStreamProvider>()

    // TODO: @Throws(IOException::class)
    fun create(resource: ReadSource): ReadableAudioStream {
        val mime = resource.mimeType()
        val codec = AudioStream[mime]
        if (codec != null) {
            return codec[resource.channel()]
        }
        throw IOException("No compatible decoder found for type: " + mime)
    }

    // TODO: @Throws(IOException::class)
    fun playable(resource: ReadSource): Boolean {
        return AudioStream.playable(resource.mimeType())
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

    private fun loadService(
            mime: String): ReadableAudioStreamProvider? {
        for (codec in ServiceLoader.load(
                ReadableAudioStreamProvider::class.java)) {
            try {
                if (codec.accepts(mime)) {
                    logger.debug { "Loaded audio codec ($mime): ${codec::class.java.name}" }
                    return codec
                }
            } catch (e: ServiceConfigurationError) {
                logger.warn { "Unable to load codec provider: $e" }
            }

        }
        return null
    }
}
