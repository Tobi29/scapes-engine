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
import org.tobi29.io.ReadSource
import org.tobi29.math.vector.Vector3d
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem
import org.tobi29.scapes.engine.sound.AudioController
import org.tobi29.scapes.engine.sound.StaticAudio
import org.tobi29.scapes.engine.sound.VolumeChannelEnvironment
import org.tobi29.stdex.assert
import org.tobi29.utils.tryUnwrap

internal class OpenALStaticAudio(
    private val asset: ReadSource,
    private val channel: String,
    private val controller: OpenALAudioController
) : OpenALAudio,
    StaticAudio,
    AudioController by controller {
    private var buffer = emptyALBuffer
    private var source = emptyALSource
    private var playing = false
    private var dispose = false

    constructor(
        asset: ReadSource,
        channel: String,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ) : this(
        asset, channel,
        OpenALAudioController(
            pitch, gain, referenceDistance,
            rolloffFactor
        )
    )

    override fun dispose() {
        dispose = true
    }

    override fun poll(
        sounds: OpenALSoundSystem,
        al: AL11,
        listenerPosition: Vector3d,
        delta: Double
    ): Boolean {
        if (buffer == emptyALBuffer) {
            val audio = sounds.getAudioData(al, asset)
                .tryUnwrap() ?: return false
            buffer = audio.buffer
        }
        assert { buffer != emptyALBuffer }
        if (gain > 0.001) {
            if (playing) {
                assert { source != emptyALSource }
                controller.configure(al, source, sounds.volume(channel))
            } else {
                if (source == emptyALSource) {
                    source = al.alCreateSource()
                }
                if (source != emptyALSource) {
                    playing = true
                    al.alSourceBuffer(source, AL_BUFFER, buffer)
                    controller.configure(
                        al, source, sounds.volume(channel),
                        true
                    )
                    al.alSourcei(source, AL_LOOPING, AL_TRUE)
                    sounds.position(al, source, Vector3d.ZERO, false)
                    al.alSource3f(source, AL_VELOCITY, 0.0f, 0.0f, 0.0f)
                    al.alSourcef(
                        source, AL_MAX_DISTANCE, Float.POSITIVE_INFINITY
                    )
                    al.alSourcePlay(source)
                }
            }
        } else {
            if (playing) {
                assert { source != emptyALSource }
                playing = false
                stop(sounds, al)
            }
        }
        if (dispose) {
            stop(sounds, al)
            return true
        }
        return false
    }

    override fun isPlaying(channel: String) =
        VolumeChannelEnvironment.run {
            this@OpenALStaticAudio.channel in channel
        }

    override fun stop(
        sounds: OpenALSoundSystem,
        al: AL11
    ) {
        if (source != emptyALSource) {
            al.alDeleteSource(source)
            source = emptyALSource
        }
    }
}
