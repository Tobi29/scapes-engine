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

package org.tobi29.scapes.engine.sound

import org.tobi29.io.ReadSource
import org.tobi29.math.vector.Vector3d
import org.tobi29.scapes.engine.ScapesEngine

interface SoundSystem {
    val engine: ScapesEngine

    fun setListener(
        position: Vector3d,
        orientation: Vector3d,
        velocity: Vector3d
    )

    fun isPlaying(channel: VolumeChannel): Boolean

    fun playMusic(
        asset: String,
        channel: VolumeChannel,
        state: Boolean,
        pitch: Double = 1.0,
        gain: Double = 1.0,
        referenceDistance: Double = 1.0,
        rolloffFactor: Double = 1.0
    ) = playMusic(
        engine.files[asset], channel, state, pitch, gain,
        referenceDistance, rolloffFactor
    )

    fun playMusic(
        asset: String,
        channel: VolumeChannel,
        position: Vector3d,
        velocity: Vector3d,
        state: Boolean,
        pitch: Double = 1.0,
        gain: Double = 1.0,
        referenceDistance: Double = 1.0,
        rolloffFactor: Double = 1.0
    ) = playMusic(
        engine.files[asset], channel, position, velocity,
        state, pitch, gain, referenceDistance, rolloffFactor
    )

    fun playMusic(
        asset: ReadSource,
        channel: VolumeChannel,
        state: Boolean,
        pitch: Double = 1.0,
        gain: Double = 1.0,
        referenceDistance: Double = 1.0,
        rolloffFactor: Double = 1.0
    )

    fun playMusic(
        asset: ReadSource,
        channel: VolumeChannel,
        position: Vector3d,
        velocity: Vector3d,
        state: Boolean,
        pitch: Double = 1.0,
        gain: Double = 1.0,
        referenceDistance: Double = 1.0,
        rolloffFactor: Double = 1.0
    )

    fun playSound(
        asset: String,
        channel: VolumeChannel,
        pitch: Double = 1.0,
        gain: Double = 1.0,
        referenceDistance: Double = 1.0,
        rolloffFactor: Double = 1.0
    ) = playSound(
        engine.files[asset], channel, pitch, gain,
        referenceDistance, rolloffFactor
    )

    fun playSound(
        asset: String,
        channel: VolumeChannel,
        position: Vector3d,
        velocity: Vector3d,
        pitch: Double = 1.0,
        gain: Double = 1.0,
        referenceDistance: Double = 1.0,
        rolloffFactor: Double = 1.0
    ) = playSound(
        engine.files[asset], channel, position, velocity, pitch,
        gain, referenceDistance, rolloffFactor
    )

    fun playSound(
        asset: ReadSource,
        channel: VolumeChannel,
        pitch: Double = 1.0,
        gain: Double = 1.0,
        referenceDistance: Double = 1.0,
        rolloffFactor: Double = 1.0
    )

    fun playSound(
        asset: ReadSource,
        channel: VolumeChannel,
        position: Vector3d,
        velocity: Vector3d,
        pitch: Double = 1.0,
        gain: Double = 1.0,
        referenceDistance: Double = 1.0,
        rolloffFactor: Double = 1.0
    )

    fun playStaticAudio(
        asset: String,
        channel: VolumeChannel,
        pitch: Double = 1.0,
        gain: Double = 1.0,
        referenceDistance: Double = 1.0,
        rolloffFactor: Double = 1.0
    ) = playStaticAudio(
        engine.files[asset], channel, pitch, gain,
        referenceDistance, rolloffFactor
    )

    fun playStaticAudio(
        asset: ReadSource,
        channel: VolumeChannel,
        pitch: Double = 1.0,
        gain: Double = 1.0,
        referenceDistance: Double = 1.0,
        rolloffFactor: Double = 1.0
    ): StaticAudio

    fun stop(channel: VolumeChannel)

    fun enable()

    fun disable()

    fun clearCache()

    suspend fun dispose()
}
