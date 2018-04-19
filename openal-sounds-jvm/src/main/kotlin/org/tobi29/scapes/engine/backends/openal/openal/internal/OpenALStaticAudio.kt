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

import org.tobi29.scapes.engine.backends.openal.openal.OpenAL
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem
import org.tobi29.math.vector.Vector3d
import org.tobi29.scapes.engine.sound.AudioController
import org.tobi29.scapes.engine.sound.StaticAudio
import org.tobi29.scapes.engine.sound.VolumeChannelEnvironment
import org.tobi29.stdex.assert
import org.tobi29.io.ReadSource
import org.tobi29.utils.tryUnwrap

internal class OpenALStaticAudio(
        private val asset: ReadSource,
        private val channel: String,
        private val controller: OpenALAudioController
) : OpenALAudio,
        StaticAudio,
        AudioController by controller {
    private var buffer = -1
    private var source = -1
    private var playing = false
    private var dispose = false

    constructor(asset: ReadSource,
                channel: String,
                pitch: Double,
                gain: Double,
                referenceDistance: Double,
                rolloffFactor: Double
    ) : this(asset, channel,
            OpenALAudioController(pitch, gain, referenceDistance,
                    rolloffFactor))

    override fun dispose() {
        dispose = true
    }

    override fun poll(sounds: OpenALSoundSystem,
                      openAL: OpenAL,
                      listenerPosition: Vector3d,
                      delta: Double): Boolean {
        if (buffer == -1) {
            val audio = sounds.getAudioData(openAL, asset)
                    .tryUnwrap() ?: return false
            buffer = audio.buffer()
        }
        assert { buffer != -1 }
        if (gain > 0.001) {
            if (playing) {
                assert { source != -1 }
                controller.configure(openAL, source, sounds.volume(channel))
            } else {
                if (source == -1) {
                    source = openAL.createSource()
                }
                if (source != -1) {
                    playing = true
                    openAL.setBuffer(source, buffer)
                    controller.configure(openAL, source, sounds.volume(channel),
                            true)
                    sounds.playSound(openAL, source, Vector3d.ZERO,
                            Vector3d.ZERO, true, false)
                }
            }
        } else {
            if (playing) {
                assert { source != -1 }
                playing = false
                stop(sounds, openAL)
            }
        }
        if (dispose) {
            stop(sounds, openAL)
            return true
        }
        return false
    }

    override fun isPlaying(channel: String) =
            VolumeChannelEnvironment.run {
                this@OpenALStaticAudio.channel in channel
            }

    override fun stop(sounds: OpenALSoundSystem,
                      openAL: OpenAL) {
        if (source != -1) {
            openAL.deleteSource(source)
            source = -1
        }
    }
}
