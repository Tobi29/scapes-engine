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

package org.tobi29.scapes.engine.backends.openal.openal

import org.tobi29.scapes.engine.sound.AudioFormat
import org.tobi29.scapes.engine.utils.io.ByteViewRO
import org.tobi29.scapes.engine.utils.math.vector.Vector3d

interface OpenAL {
    fun checkError(message: String)

    fun create(speedOfSound: Double)

    fun destroy()

    fun setListener(position: Vector3d,
                    orientation: Vector3d,
                    velocity: Vector3d)

    fun createSource(): Int

    fun deleteSource(id: Int)

    fun setBuffer(id: Int,
                  value: Int)

    fun setPitch(id: Int,
                 value: Double)

    fun setGain(id: Int,
                value: Double)

    fun setLooping(id: Int,
                   value: Boolean)

    fun setRelative(id: Int,
                    value: Boolean)

    fun setPosition(id: Int,
                    pos: Vector3d)

    fun setVelocity(id: Int,
                    vel: Vector3d)

    fun setReferenceDistance(id: Int,
                             value: Double)

    fun setRolloffFactor(id: Int,
                         value: Double)

    fun setMaxDistance(id: Int,
                       value: Double)

    fun play(id: Int)

    fun stop(id: Int)

    fun createBuffer(): Int

    fun deleteBuffer(id: Int)

    fun storeBuffer(id: Int,
                    format: AudioFormat,
                    buffer: ByteViewRO,
                    rate: Int)

    fun isPlaying(id: Int): Boolean

    fun isStopped(id: Int): Boolean

    fun getBuffersQueued(id: Int): Int

    fun getBuffersProcessed(id: Int): Int

    fun queue(id: Int,
              buffer: Int)

    fun unqueue(id: Int): Int

    fun getBuffer(id: Int): Int
}
