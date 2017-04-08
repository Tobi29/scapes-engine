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

package org.tobi29.scapes.engine.backends.openal.openal.internal

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.backends.openal.openal.OpenAL
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem
import org.tobi29.scapes.engine.codec.AudioBuffer
import org.tobi29.scapes.engine.codec.ReadableAudioStream
import org.tobi29.scapes.engine.codec.toPCM16
import org.tobi29.scapes.engine.sound.AudioFormat
import org.tobi29.scapes.engine.utils.io.ByteBufferStream
import java.io.IOException
import java.nio.ByteBuffer

internal class OpenALAudioData(data: ByteBuffer,
                               channels: Int,
                               rate: Int,
                               openAL: OpenAL) {
    private val buffer: Int

    init {
        buffer = openAL.createBuffer()
        openAL.storeBuffer(buffer,
                if (channels > 1) AudioFormat.STEREO else AudioFormat.MONO,
                data, rate)
    }

    fun dispose(soundSystem: OpenALSoundSystem,
                openAL: OpenAL) {
        soundSystem.removeBufferFromSources(openAL, buffer)
        openAL.deleteBuffer(buffer)
    }

    fun buffer(): Int {
        return buffer
    }

    companion object {
        fun read(engine: ScapesEngine,
                 input: ReadableAudioStream,
                 openAL: OpenAL): OpenALAudioData {
            val output = ByteBufferStream({ engine.allocate(it) },
                    { it + 409600 })
            val buffer = AudioBuffer(4096)
            var channels = -1
            var rate = -1
            var valid = true
            while (valid) {
                while (!buffer.isDone) {
                    if (input.get(buffer) == ReadableAudioStream.Result.EOS) {
                        valid = false
                        break
                    }
                }
                if (!buffer.isDone) {
                    break
                }
                if (channels == -1) {
                    channels = buffer.channels()
                } else {
                    if (channels != buffer.channels()) {
                        throw IOException(
                                "Number of channels changed in audio file, this is not supported for non-streams")
                    }
                }
                if (rate == -1) {
                    rate = buffer.rate()
                } else {
                    if (rate != buffer.rate()) {
                        throw IOException(
                                "Sample rate changed in audio file, this is not supported for non-streams")
                    }
                }
                buffer.toPCM16 { output.putShort(it) }
                buffer.clear()
            }
            output.buffer().flip()
            return OpenALAudioData(output.buffer(), channels, rate, openAL)
        }
    }
}
