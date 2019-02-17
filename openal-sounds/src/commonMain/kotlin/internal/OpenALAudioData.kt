/*
 * Copyright 2012-2019 Tobi29
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

import net.gitout.ktbindings.al.*
import org.tobi29.arrays.BytesRO
import org.tobi29.codec.AudioBuffer
import org.tobi29.codec.ReadableAudioStream
import org.tobi29.codec.toPCM16
import org.tobi29.io.ByteViewE
import org.tobi29.io.IOException
import org.tobi29.io.MemoryViewStream
import org.tobi29.scapes.engine.allocateMemoryBuffer
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem
import org.tobi29.scapes.engine.backends.openal.openal.asDataBuffer

internal class OpenALAudioData(
    data: BytesRO,
    channels: Int,
    rate: Int,
    al: AL11
) {
    val buffer = al.alCreateBuffer()

    init {
        al.alBufferData(
            buffer,
            if (channels > 1) AL_FORMAT_STEREO16 else AL_FORMAT_MONO16,
            data.asDataBuffer(), rate
        )
    }

    fun dispose(
        soundSystem: OpenALSoundSystem,
        al: AL11
    ) {
        soundSystem.removeBufferFromSources(al, buffer)
        al.alDeleteBuffer(buffer)
    }
}

fun readAudioData(input: ReadableAudioStream): Triple<ByteViewE, Int, Int> {
    val output = MemoryViewStream({
        allocateMemoryBuffer(it)
    }, { (it shl 1).coerceAtLeast(409600) })
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
                    "Number of channels changed in audio file, this is not supported for non-streams"
                )
            }
        }
        if (rate == -1) {
            rate = buffer.rate()
        } else {
            if (rate != buffer.rate()) {
                throw IOException(
                    "Sample rate changed in audio file, this is not supported for non-streams"
                )
            }
        }
        buffer.toPCM16 { output.putShort(it) }
        buffer.clear()
    }
    output.flip()
    return Triple(output.bufferSlice(), channels, rate)
}
