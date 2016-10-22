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

package org.tobi29.scapes.engine.sound.dummy

import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.sound.StaticAudio
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource
import org.tobi29.scapes.engine.utils.math.vector.Vector3d

class DummySoundSystem : SoundSystem {
    override fun setListener(position: Vector3d,
                             orientation: Vector3d,
                             velocity: Vector3d) {
    }

    override fun isPlaying(channel: String): Boolean {
        return false
    }

    override fun playMusic(asset: String,
                           channel: String,
                           pitch: Float,
                           gain: Float,
                           state: Boolean) {
    }

    override fun playMusic(asset: String,
                           channel: String,
                           pitch: Float,
                           gain: Float,
                           position: Vector3d,
                           velocity: Vector3d,
                           state: Boolean) {
    }

    override fun playMusic(asset: ReadSource,
                           channel: String,
                           pitch: Float,
                           gain: Float,
                           state: Boolean) {
    }

    override fun playMusic(asset: ReadSource,
                           channel: String,
                           pitch: Float,
                           gain: Float,
                           position: Vector3d,
                           velocity: Vector3d,
                           state: Boolean) {
    }

    override fun playSound(asset: String,
                           channel: String,
                           pitch: Float,
                           gain: Float) {
    }

    override fun playSound(asset: String,
                           channel: String,
                           position: Vector3d,
                           velocity: Vector3d,
                           pitch: Float,
                           gain: Float) {
    }

    override fun playStaticAudio(asset: String,
                                 channel: String,
                                 pitch: Float,
                                 gain: Float): StaticAudio {
        return DummyStaticAudio()
    }

    override fun stop(channel: String) {
    }

    override fun dispose() {
    }
}
