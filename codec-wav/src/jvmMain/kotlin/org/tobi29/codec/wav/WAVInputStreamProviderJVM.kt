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

package org.tobi29.codec.wav

import org.tobi29.codec.spi.ReadableAudioStreamProvider
import org.tobi29.io.ReadableByteChannel

class WAVInputStreamProvider : ReadableAudioStreamProvider {
    private val mimeTypes = listOf("audio/x-wav", "audio/vnd.wave")

    override fun accepts(mime: String) = mimeTypes.contains(mime)

    override fun get(channel: ReadableByteChannel) = WAVReadStream(channel)
}
