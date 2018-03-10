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
package org.tobi29.scapes.engine.backends.lwjgl3.openal

import org.lwjgl.openal.*
import org.tobi29.io.ByteViewRO
import org.tobi29.io._rewind
import org.tobi29.io.readAsNativeByteBuffer
import org.tobi29.logging.KLogging
import org.tobi29.math.vector.Vector3d
import org.tobi29.scapes.engine.backends.lwjgl3.stackFrame
import org.tobi29.scapes.engine.backends.openal.openal.OpenAL
import org.tobi29.scapes.engine.sound.AudioFormat
import org.tobi29.scapes.engine.sound.SoundException
import org.tobi29.stdex.math.toRad
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlin.math.cos
import kotlin.math.sin

class LWJGL3OpenAL : OpenAL {
    private var device = 0L
    private var context = 0L

    override fun checkError(message: String) {
        val error = AL10.alGetError()
        if (error != AL10.AL_NO_ERROR) {
            throw SoundException(
                AL10.alGetString(error) + " in " + message
            )
        }
    }

    override fun create(speedOfSound: Double) {
        device = ALC10.alcOpenDevice(null as ByteBuffer?)
        if (device == 0L) {
            throw IllegalStateException(
                "Failed to open the default device."
            )
        }
        val deviceCaps = ALC.createCapabilities(device)
        context = ALC10.alcCreateContext(device, null as IntBuffer?)
        if (context == 0L) {
            throw IllegalStateException(
                "Failed to create an OpenAL context."
            )
        }
        ALC10.alcMakeContextCurrent(context)
        AL.createCapabilities(deviceCaps)
        logger.info {
            "OpenAL: ${AL10.alGetString(
                AL10.AL_VERSION
            )} (Vendor: ${AL10.alGetString(
                AL10.AL_VENDOR
            )}, Renderer: ${AL10.alGetString(
                AL10.AL_RENDERER
            )})"
        }
        AL11.alSpeedOfSound(speedOfSound.toFloat())
        AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED)
        stackFrame { stack ->
            val listenerOrientation = stack.mallocFloat(6)
            listenerOrientation.put(0.0f)
            listenerOrientation.put(-1.0f)
            listenerOrientation.put(0.0f)
            listenerOrientation.put(0.0f)
            listenerOrientation.put(0.0f)
            listenerOrientation.put(1.0f)
            listenerOrientation._rewind()
            AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation)
            AL10.alListener3f(AL10.AL_POSITION, 0.0f, 0.0f, 0.0f)
            AL10.alListener3f(AL10.AL_VELOCITY, 0.0f, 0.0f, 0.0f)
        }
    }

    override fun resume() {
        if (device != 0L) SOFTPauseDevice.alcDeviceResumeSOFT(device)
    }

    override fun pause() {
        if (device != 0L) SOFTPauseDevice.alcDevicePauseSOFT(device)
    }

    override fun destroy() {
        if (context != 0L) {
            ALC10.alcDestroyContext(context)
            context = 0
        }
        if (device != 0L) {
            ALC10.alcCloseDevice(device)
            device = 0
        }
    }

    override fun setListener(
        position: Vector3d,
        orientation: Vector3d,
        velocity: Vector3d
    ) {
        val cos = cos(orientation.x.toFloat().toRad())
        val lookX = cos(orientation.z.toFloat().toRad()) * cos
        val lookY = sin(orientation.z.toFloat().toRad()) * cos
        val lookZ = sin(orientation.x.toFloat().toRad())
        stackFrame { stack ->
            val listenerOrientation = stack.mallocFloat(6)
            listenerOrientation.put(lookX)
            listenerOrientation.put(lookY)
            listenerOrientation.put(lookZ)
            listenerOrientation.put(0.0f)
            listenerOrientation.put(0.0f)
            listenerOrientation.put(1.0f)
            listenerOrientation._rewind()
            AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation)
        }
        AL10.alListener3f(
            AL10.AL_POSITION, position.x.toFloat(),
            position.y.toFloat(), position.z.toFloat()
        )
        AL10.alListener3f(
            AL10.AL_VELOCITY, velocity.x.toFloat(),
            velocity.y.toFloat(), velocity.z.toFloat()
        )
    }

    override fun createSource(): Int {
        return AL10.alGenSources()
    }

    override fun deleteSource(id: Int) {
        AL10.alDeleteSources(id)
    }

    override fun setBuffer(
        id: Int,
        value: Int
    ) {
        AL10.alSourcei(id, AL10.AL_BUFFER, value)
    }

    override fun setPitch(
        id: Int,
        value: Double
    ) {
        AL10.alSourcef(id, AL10.AL_PITCH, value.toFloat())
    }

    override fun setGain(
        id: Int,
        value: Double
    ) {
        AL10.alSourcef(id, AL10.AL_GAIN, value.toFloat())
    }

    override fun setLooping(
        id: Int,
        value: Boolean
    ) {
        AL10.alSourcei(
            id, AL10.AL_LOOPING,
            if (value) AL10.AL_TRUE else AL10.AL_FALSE
        )
    }

    override fun setRelative(
        id: Int,
        value: Boolean
    ) {
        AL10.alSourcei(
            id, AL10.AL_SOURCE_RELATIVE,
            if (value) AL10.AL_TRUE else AL10.AL_FALSE
        )
    }

    override fun setPosition(
        id: Int,
        pos: Vector3d
    ) {
        AL10.alSource3f(
            id, AL10.AL_POSITION, pos.x.toFloat(), pos.y.toFloat(),
            pos.z.toFloat()
        )
    }

    override fun setVelocity(
        id: Int,
        vel: Vector3d
    ) {
        AL10.alSource3f(
            id, AL10.AL_VELOCITY, vel.x.toFloat(), vel.y.toFloat(),
            vel.z.toFloat()
        )
    }

    override fun setReferenceDistance(
        id: Int,
        value: Double
    ) {
        AL10.alSourcef(id, AL10.AL_REFERENCE_DISTANCE, value.toFloat())
    }

    override fun setRolloffFactor(
        id: Int,
        value: Double
    ) {
        AL10.alSourcef(id, AL10.AL_ROLLOFF_FACTOR, value.toFloat())
    }

    override fun setMaxDistance(
        id: Int,
        value: Double
    ) {
        AL10.alSourcef(id, AL10.AL_MAX_DISTANCE, value.toFloat())
    }

    override fun play(id: Int) {
        AL10.alSourcePlay(id)
    }

    override fun stop(id: Int) {
        AL10.alSourceStop(id)
    }

    override fun createBuffer(): Int {
        return AL10.alGenBuffers()
    }

    override fun deleteBuffer(id: Int) {
        AL10.alDeleteBuffers(id)
    }

    override fun storeBuffer(
        id: Int,
        format: AudioFormat,
        buffer: ByteViewRO,
        rate: Int
    ) {
        AL10.alBufferData(
            id,
            when (format) {
                AudioFormat.MONO -> AL10.AL_FORMAT_MONO16
                AudioFormat.STEREO -> AL10.AL_FORMAT_STEREO16
            },
            buffer.readAsNativeByteBuffer(),
            rate
        )
    }

    override fun isPlaying(id: Int): Boolean {
        return AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING
    }

    override fun isStopped(id: Int): Boolean {
        val state = AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE)
        return state != AL10.AL_PLAYING && state != AL10.AL_PAUSED
    }

    override fun getBuffersQueued(id: Int): Int {
        return AL10.alGetSourcei(id, AL10.AL_BUFFERS_QUEUED)
    }

    override fun getBuffersProcessed(id: Int): Int {
        return AL10.alGetSourcei(id, AL10.AL_BUFFERS_PROCESSED)
    }

    override fun queue(
        id: Int,
        buffer: Int
    ) {
        AL10.alSourceQueueBuffers(id, buffer)
    }

    override fun unqueue(id: Int): Int {
        return AL10.alSourceUnqueueBuffers(id)
    }

    override fun getBuffer(id: Int): Int {
        return AL10.alGetSourcei(id, AL10.AL_BUFFER)
    }

    companion object : KLogging()
}
