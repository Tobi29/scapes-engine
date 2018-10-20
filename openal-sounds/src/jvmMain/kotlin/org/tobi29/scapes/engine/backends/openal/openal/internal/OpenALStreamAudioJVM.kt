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

package org.tobi29.scapes.engine.backends.openal.openal.internal

import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.yield
import org.tobi29.codec.AudioBuffer
import org.tobi29.codec.AudioStream
import org.tobi29.codec.ReadableAudioStream
import org.tobi29.contentinfo.mimeType
import org.tobi29.io.ReadSource
import org.tobi29.io.use
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem

internal actual fun CoroutineScope.decodeActor(
    asset: ReadSource,
    state: Boolean
): Pair<SendChannel<AudioBuffer>, Channel<AudioBuffer>> {
    val output = Channel<AudioBuffer>()
    val actor = actor<AudioBuffer>(capacity = 1) {
        try {
            do {
                asset.channel().use { dataChannel ->
                    AudioStream.create(
                        dataChannel, asset.mimeType()
                    ).use { stream ->
                        loop@ while (true) {
                            val buffer = channel.receive()
                            while (true) {
                                when (stream.get(buffer)) {
                                    ReadableAudioStream.Result.YIELD -> yield()
                                    ReadableAudioStream.Result.BUFFER -> {
                                        output.send(buffer)
                                        continue@loop
                                    }
                                    ReadableAudioStream.Result.EOS -> {
                                        output.send(buffer)
                                        break@loop
                                    }
                                }
                            }
                        }
                    }
                }
            } while (state)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Catch internal errors from decoding
            // (Old) Android is buggy
            OpenALSoundSystem.logger.error(e) { "Failed decoding audio stream" }
        } finally {
            output.close()
        }
    }
    repeat(1) { actor.offer(AudioBuffer(4096)) }
    return actor to output
}
