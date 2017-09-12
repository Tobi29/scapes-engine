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

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.backends.openal.openal.OpenAL
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem
import org.tobi29.scapes.engine.codec.AudioBuffer
import org.tobi29.scapes.engine.codec.AudioStream
import org.tobi29.scapes.engine.codec.ReadableAudioStream
import org.tobi29.scapes.engine.codec.toPCM16
import org.tobi29.scapes.engine.sound.AudioController
import org.tobi29.scapes.engine.sound.AudioFormat
import org.tobi29.scapes.engine.utils.assert
import org.tobi29.scapes.engine.utils.io.ByteBufferStream
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.io.ReadSource
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.math.vector.Vector3d

internal class OpenALStreamAudio(
        engine: ScapesEngine,
        private val asset: ReadSource,
        private val channel: String,
        private val pos: Vector3d,
        private val velocity: Vector3d,
        private val state: Boolean,
        private val hasPosition: Boolean,
        private val controller: OpenALAudioController
) : OpenALAudio, AudioController by controller {
    private val streamBuffer = ByteBufferStream(engine)
    private val readBuffer = AudioBuffer(4096)
    private var source = -1
    private var queued = 0
    private var stream: ReadableAudioStream? = null

    constructor(engine: ScapesEngine,
                asset: ReadSource,
                channel: String,
                pos: Vector3d,
                velocity: Vector3d,
                state: Boolean,
                hasPosition: Boolean,
                pitch: Double,
                gain: Double,
                referenceDistance: Double,
                rolloffFactor: Double
    ) : this(engine, asset, channel, pos, velocity, state, hasPosition,
            OpenALAudioController(pitch, gain, referenceDistance,
                    rolloffFactor))

    override fun poll(sounds: OpenALSoundSystem,
                      openAL: OpenAL,
                      listenerPosition: Vector3d,
                      delta: Double): Boolean {
        if (source == -1) {
            source = openAL.createSource()
            if (source == -1) {
                return true
            }
            controller.configure(openAL, source, sounds.volume(channel), true)
            openAL.setLooping(source, false)
            sounds.position(openAL, source, pos, hasPosition)
            openAL.setVelocity(source, velocity)
            openAL.setReferenceDistance(source, 1.0)
            openAL.setMaxDistance(source, Double.POSITIVE_INFINITY)
            try {
                stream = AudioStream.create(asset)
            } catch (e: IOException) {
                logger.warn { "Failed to play music: $e" }
                stop(sounds, openAL)
                return true
            }
        }
        if (stream != null) {
            try {
                controller.configure(openAL, source, sounds.volume(channel))
                while (queued < 3) {
                    if (!stream()) break
                    val buffer = openAL.createBuffer()
                    store(openAL, buffer)
                    openAL.queue(source, buffer)
                    queued++
                }
                var finished = openAL.getBuffersProcessed(source)
                while (finished > 0) {
                    if (!stream()) break
                    val unqueued = openAL.unqueue(source)
                    store(openAL, unqueued)
                    openAL.queue(source, unqueued)
                    finished--
                }
                if (queued > 0 && !openAL.isPlaying(source)) {
                    openAL.play(source)
                }
            } catch (e: IOException) {
                logger.warn { "Failed to stream music: $e" }
                stop(sounds, openAL)
                return true
            }

        } else if (!openAL.isPlaying(source)) {
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
        if (source != -1) {
            openAL.stop(source)
            var queued = openAL.getBuffersQueued(source)
            while (queued-- > 0) {
                openAL.deleteBuffer(openAL.unqueue(source))
                this.queued--
            }
            openAL.deleteSource(source)
            source = -1
            assert { this.queued == 0 }
        }
        stream?.let {
            try {
                it.close()
            } catch (e: IOException) {
                logger.warn { "Failed to stop music stream: $e" }
            }
        }
        stream = null
    }

    private fun stream(): Boolean {
        while (!readBuffer.isDone) {
            val stream = stream ?: return false
            when (stream.get(readBuffer)) {
                ReadableAudioStream.Result.YIELD -> return false
                ReadableAudioStream.Result.EOS -> {
                    stream.close()
                    if (state) {
                        this.stream = AudioStream.create(asset)
                    } else {
                        this.stream = null
                    }
                }
            }
        }
        return true
    }

    private fun store(openAL: OpenAL,
                      buffer: Int) {
        readBuffer.toPCM16 { streamBuffer.putShort(it) }
        streamBuffer.buffer().flip()
        openAL.storeBuffer(buffer,
                if (readBuffer.channels() > 1) AudioFormat.STEREO
                else AudioFormat.MONO, streamBuffer.buffer(), readBuffer.rate())
        streamBuffer.buffer().clear()
        readBuffer.clear()
    }

    companion object : KLogging()
}
