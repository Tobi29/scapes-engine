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

import org.tobi29.scapes.engine.backends.openal.openal.OpenAL
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem
import org.tobi29.scapes.engine.utils.io.ReadSource
import org.tobi29.scapes.engine.utils.math.vector.Vector3d
import org.tobi29.scapes.engine.utils.math.vector.distance

internal class OpenALEffectAudio(private val asset: ReadSource,
                                 private val channel: String,
                                 private val pos: Vector3d,
                                 private val velocity: Vector3d,
                                 private val pitch: Double,
                                 private val gain: Double,
                                 private val referenceDistance: Double,
                                 private val rolloffFactor: Double,
                                 private val hasPosition: Boolean,
                                 private val time: Long) : OpenALAudio {

    override fun poll(sounds: OpenALSoundSystem,
                      openAL: OpenAL,
                      listenerPosition: Vector3d,
                      delta: Double): Boolean {
        if (!hasPosition || run {
            val diff = (System.nanoTime() - time) / 1000000000.0
            val delay = listenerPosition.distance(
                    pos) / sounds.speedOfSound - delta * 0.5
            diff >= delay
        }) {
            val audio = sounds.getAudioData(openAL, asset)
            if (audio != null) {
                val gain = gain * sounds.volume(channel)
                val source = sounds.freeSource(openAL, false, false)
                if (source != -1) {
                    openAL.setBuffer(source, audio.buffer())
                    openAL.setGain(source, gain)
                    openAL.setPitch(source, pitch)
                    openAL.setReferenceDistance(source, referenceDistance)
                    openAL.setRolloffFactor(source, rolloffFactor)
                    sounds.playSound(openAL, source, pos, velocity, false,
                            hasPosition)
                }
            }
            return true
        }
        return false
    }

    override fun isPlaying(channel: String): Boolean {
        return false
    }

    override fun stop(sounds: OpenALSoundSystem,
                      openAL: OpenAL) {
    }
}
