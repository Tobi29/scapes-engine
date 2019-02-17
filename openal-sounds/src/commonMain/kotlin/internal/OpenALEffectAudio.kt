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
import org.tobi29.math.vector.distance
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem
import org.tobi29.scapes.engine.sound.VolumeChannel
import org.tobi29.utils.steadyClock
import org.tobi29.utils.unwrapOr

internal class OpenALEffectAudio(
    private val asset: ReadSource,
    private val channel: String,
    private val pos: Vector3d,
    private val velocity: Vector3d,
    private val pitch: Double,
    private val gain: Double,
    private val referenceDistance: Double,
    private val rolloffFactor: Double,
    private val hasPosition: Boolean,
    private val time: Long
) : OpenALAudio {

    override fun poll(
        sounds: OpenALSoundSystem,
        al: AL11,
        listenerPosition: Vector3d,
        delta: Double
    ): Boolean {
        if (!hasPosition || run {
                val diff = (steadyClock.timeSteadyNanos() - time) / 1000000000.0
                val delay = listenerPosition.distance(pos) /
                        sounds.speedOfSound - delta * 0.5
                diff >= delay
            }) {
            val audio = sounds.getAudioData(al, asset)
                .unwrapOr { return false }
            if (audio != null) {
                val gain = gain * sounds.volume(channel)
                val source = sounds.freeSource(al)
                if (source != emptyALSource) {
                    al.alSourceBuffer(source, AL_BUFFER, audio.buffer)
                    al.alSourcef(source, AL_GAIN, gain.toFloat())
                    al.alSourcef(source, AL_PITCH, pitch.toFloat())
                    al.alSourcef(
                        source, AL_REFERENCE_DISTANCE,
                        referenceDistance.toFloat()
                    )
                    al.alSourcef(
                        source, AL_ROLLOFF_FACTOR, rolloffFactor.toFloat()
                    )
                    al.alSourcei(source, AL_LOOPING, AL_FALSE)
                    sounds.position(al, source, pos, hasPosition)
                    al.alSource3f(
                        source, AL_VELOCITY, velocity.x.toFloat(),
                        velocity.y.toFloat(), velocity.z.toFloat()
                    )
                    al.alSourcef(
                        source, AL_MAX_DISTANCE, Float.POSITIVE_INFINITY
                    )
                    al.alSourcePlay(source)
                }
            }
            return true
        }
        return false
    }

    override fun isPlaying(channel: VolumeChannel) = false

    override fun stop(
        sounds: OpenALSoundSystem,
        al: AL11
    ) {
    }
}
