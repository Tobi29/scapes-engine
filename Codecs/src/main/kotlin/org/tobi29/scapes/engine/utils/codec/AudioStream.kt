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

package org.tobi29.scapes.engine.utils.codec

import mu.KLogging
import org.tobi29.scapes.engine.utils.codec.spi.ReadableAudioStreamProvider
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object AudioStream : KLogging() {
    private val CODECS = ConcurrentHashMap<String, ReadableAudioStreamProvider>()

    @Throws(IOException::class)
    fun create(resource: ReadSource): ReadableAudioStream {
        val mime = resource.mimeType()
        val codec = get(mime)
        if (codec != null) {
            return codec[resource.channel()]
        }
        throw IOException("No compatible decoder found for type: " + mime)
    }

    @Throws(IOException::class)
    fun playable(resource: ReadSource): Boolean {
        return playable(resource.mimeType())
    }

    fun playable(mime: String): Boolean {
        return get(mime) != null
    }

    private operator fun get(mime: String): ReadableAudioStreamProvider? {
        var codec = CODECS[mime]
        if (codec == null) {
            codec = loadService(mime)
            if (codec != null) {
                CODECS.put(mime, codec)
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
