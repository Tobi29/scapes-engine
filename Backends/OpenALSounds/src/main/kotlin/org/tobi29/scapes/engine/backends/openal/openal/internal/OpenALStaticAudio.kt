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
import org.tobi29.scapes.engine.sound.StaticAudio
import org.tobi29.scapes.engine.utils.math.abs
import org.tobi29.scapes.engine.utils.math.vector.Vector3d

internal class OpenALStaticAudio(private val asset: String,
                        private val channel: String,
                        private var pitch: Float,
                        private var gain: Float) : OpenALAudio, StaticAudio {
    private var buffer = -1
    private var source = -1
    private var pitchAL = 0.0f
    private var gainAL = 0.0f
    private var playing = false
    private var dispose = false

    override fun setPitch(pitch: Float) {
        this.pitch = pitch
    }

    override fun setGain(gain: Float) {
        this.gain = gain
    }

    override fun dispose() {
        dispose = true
    }

    override fun poll(sounds: OpenALSoundSystem,
                      openAL: OpenAL,
                      listenerPosition: Vector3d,
                      delta: Double): Boolean {
        if (buffer == -1) {
            val audio = sounds[openAL, asset]
            if (audio != null) {
                buffer = audio.buffer()
            }
        }
        if (source == -1) {
            source = sounds.takeSource(openAL)
        }
        if (gain > 0.001f) {
            val gainAL = gain * sounds.volume(channel)
            val pitchAL = pitch
            if (playing) {
                if (abs(gainAL - this.gainAL) > 0.001f) {
                    openAL.setGain(source, gainAL)
                    this.gainAL = gainAL
                }
                if (abs(pitchAL - this.pitchAL) > 0.001f) {
                    openAL.setPitch(source, pitchAL)
                    this.pitchAL = pitchAL
                }
            } else {
                playing = true
                if (buffer != -1) {
                    sounds.playSound(openAL, buffer, source, pitchAL, gainAL,
                            Vector3d.ZERO, Vector3d.ZERO, true, false)
                }
                this.gainAL = gainAL
                this.pitchAL = pitchAL
            }
        } else {
            if (playing) {
                playing = false
                openAL.stop(source)
            }
        }
        if (dispose) {
            stop(sounds, openAL)
            return true
        }
        return false
    }

    override fun isPlaying(channel: String): Boolean {
        return this.channel.startsWith(channel)
    }

    override fun stop(sounds: OpenALSoundSystem,
                      openAL: OpenAL) {
        sounds.releaseSource(openAL, source)
    }
}
