/*
 * Copyright 2012-2016 Tobi29
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
import org.tobi29.scapes.engine.utils.math.vector.Vector3d
import org.tobi29.scapes.engine.utils.math.vector.distance

internal class OpenALEffectAudio(private val asset: String,
                                 private val channel: String,
                                 private val pos: Vector3d,
                                 private val velocity: Vector3d,
                                 private val pitch: Float,
                                 private val gain: Float,
                                 private val hasPosition: Boolean,
                                 private val time: Long) : OpenALAudio {

    override fun poll(sounds: OpenALSoundSystem,
                      openAL: OpenAL,
                      listenerPosition: Vector3d,
                      delta: Double): Boolean {
        val flag: Boolean
        if (hasPosition) {
            val diff = (System.nanoTime() - time) / 1000000000.0
            val delay = listenerPosition.distance(pos) / 343.3 - delta * 0.5
            flag = diff >= delay
        } else {
            flag = true
        }
        if (flag) {
            val audio = sounds[openAL, asset]
            if (audio != null) {
                val gain = this.gain * sounds.volume(channel)
                sounds.playSound(openAL, audio.buffer(), pitch, gain, pos,
                        velocity, false, hasPosition)
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
