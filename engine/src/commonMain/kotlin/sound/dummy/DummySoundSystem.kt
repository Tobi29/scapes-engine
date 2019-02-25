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

package org.tobi29.scapes.engine.sound.dummy

import org.tobi29.io.ReadSource
import org.tobi29.math.vector.Vector3d
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.sound.StaticAudio

class DummySoundSystem(override val engine: ScapesEngine) : SoundSystem {
    override fun setListener(
        position: Vector3d,
        orientation: Vector3d,
        velocity: Vector3d
    ) {
    }

    override fun isPlaying(channel: String): Boolean = false

    override fun playMusic(
        asset: ReadSource,
        channel: String,
        state: Boolean,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ) {
    }

    override fun playMusic(
        asset: ReadSource,
        channel: String,
        position: Vector3d,
        velocity: Vector3d,
        state: Boolean,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ) {
    }

    override fun playSound(
        asset: ReadSource,
        channel: String,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ) {
    }

    override fun playSound(
        asset: ReadSource,
        channel: String,
        position: Vector3d,
        velocity: Vector3d,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ) {
    }

    override fun playStaticAudio(
        asset: ReadSource,
        channel: String,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ): StaticAudio {
        return DummyStaticAudio()
    }

    override fun stop(channel: String) {
    }

    override fun enable() {
    }

    override fun disable() {
    }

    override fun clearCache() {
    }

    override suspend fun dispose() {
    }
}
