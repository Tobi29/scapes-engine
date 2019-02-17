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

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import net.gitout.ktbindings.al.*
import org.tobi29.codec.AudioBuffer
import org.tobi29.codec.AudioStream
import org.tobi29.codec.ReadableAudioStream
import org.tobi29.codec.toPCM16
import org.tobi29.contentinfo.mimeType
import org.tobi29.io.*
import org.tobi29.logging.KLogger
import org.tobi29.math.vector.Vector3d
import org.tobi29.scapes.engine.allocateMemoryBuffer
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem
import org.tobi29.scapes.engine.backends.openal.openal.asDataBuffer
import org.tobi29.scapes.engine.sound.AudioController
import org.tobi29.scapes.engine.sound.VolumeChannel
import org.tobi29.scapes.engine.sound.VolumeChannelEnvironment
import org.tobi29.stdex.assert

internal class OpenALStreamAudio(
    private val asset: ReadSource,
    private val channel: String,
    private val pos: Vector3d,
    private val velocity: Vector3d,
    private val state: Boolean,
    private val hasPosition: Boolean,
    private val controller: OpenALAudioController
) : OpenALAudio,
    AudioController by controller {
    private val streamBuffer =
        MemoryViewStream<ByteViewE>({ allocateMemoryBuffer(it) })
    private var source = emptyALSource
    private var queued = 0
    private var decodeActor: Pair<SendChannel<AudioBuffer>, Channel<AudioBuffer>>? =
        null

    constructor(
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
    ) : this(
        asset,
        channel,
        pos,
        velocity,
        state,
        hasPosition,
        OpenALAudioController(pitch, gain, referenceDistance, rolloffFactor)
    )

    override fun poll(
        sounds: OpenALSoundSystem,
        al: AL11,
        listenerPosition: Vector3d,
        delta: Double
    ): Boolean {
        if (source == emptyALSource) {
            source = al.alCreateSource()
            controller.configure(al, source, sounds.volume(channel), true)
            al.alSourcei(source, AL_LOOPING, AL_FALSE)
            sounds.position(al, source, pos, hasPosition)
            al.alSource3f(
                source, AL_VELOCITY, velocity.x.toFloat(), velocity.y.toFloat(),
                velocity.z.toFloat()
            )
            al.alSourcef(source, AL_REFERENCE_DISTANCE, 1.0f)
            al.alSourcef(source, AL_MAX_DISTANCE, Float.POSITIVE_INFINITY)
            val input = Channel<AudioBuffer>(1)
            val output = Channel<AudioBuffer>()
            sounds.launch {
                try {
                    do {
                        asset.channel().use { dataChannel ->
                            AudioStream.create(
                                dataChannel, asset.mimeType()
                            ).use { stream ->
                                loop@ while (true) {
                                    val buffer = input.receive()
                                    while (true) {
                                        when (stream.get(buffer)) {
                                            ReadableAudioStream.Result.YIELD -> yield()
                                            ReadableAudioStream.Result.BUFFER -> {
                                                output.send(buffer)
                                                continue@loop
                                            }
                                            ReadableAudioStream.Result.EOS -> {
                                                output.send(buffer)
                                                break@loop
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } while (state)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Catch internal errors from decoding
                    // (Old) Android is buggy
                    logger.error(e) { "Failed decoding audio stream" }
                } finally {
                    output.close()
                }
            }
            repeat(1) { input.offer(AudioBuffer(4096)) }
            decodeActor = input to output
        }
        val (decode, buffers) = decodeActor ?: return true
        if (buffers.isClosedForReceive) return true
        try {
            controller.configure(al, source, sounds.volume(channel))
            while (queued < 3) {
                val buffer = buffers.poll() ?: break
                val audioBuffer = al.alCreateBuffer()
                store(al, buffer, audioBuffer)
                buffer.clear()
                if (!decode.offer(buffer))
                    throw IllegalStateException("Buffer lost")
                al.alSourceQueueBuffers(source, audioBuffer)
                queued++
            }
            if (queued > 0
                && al.alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING) {
                al.alSourcePlay(source)
            }
            var finished = al.alGetSourcei(source, AL_BUFFERS_PROCESSED)
            while (finished > 0) {
                val unqueued = al.alSourceUnqueueBuffers(source)
                val buffer = buffers.poll()
                if (buffer == null) {
                    al.alDeleteBuffer(unqueued)
                    queued--
                    break
                }
                store(al, buffer, unqueued)
                buffer.clear()
                if (!decode.offer(buffer))
                    throw IllegalStateException("Buffer lost")
                al.alSourceQueueBuffers(source, unqueued)
                finished--
            }
        } catch (e: IOException) {
            logger.warn { "Failed to stream music: $e" }
            stop(sounds, al)
            return true
        }
        return false
    }

    override fun isPlaying(channel: VolumeChannel) =
        VolumeChannelEnvironment.run {
            this@OpenALStreamAudio.channel in channel
        }

    override fun stop(
        sounds: OpenALSoundSystem,
        al: AL11
    ) {
        if (source != emptyALSource) {
            al.alSourceStop(source)
            var queued = al.alGetSourcei(source, AL_BUFFERS_QUEUED)
            while (queued-- > 0) {
                al.alDeleteBuffer(al.alSourceUnqueueBuffers(source))
                this.queued--
            }
            al.alDeleteSource(source)
            source = emptyALSource
            assert { this.queued == 0 }
        }
        try {
            decodeActor?.first?.close()
        } catch (e: Exception) {
            logger.warn { "Failed to close stream: $e" }
        }
        decodeActor = null
    }

    private fun store(
        al: AL11,
        buffer: AudioBuffer,
        audioBuffer: ALBuffer
    ) {
        buffer.toPCM16 { streamBuffer.putShort(it) }
        streamBuffer.flip()
        al.alBufferData(
            audioBuffer,
            if (buffer.channels() > 1) AL_FORMAT_STEREO16
            else AL_FORMAT_MONO16, streamBuffer.bufferSlice().asDataBuffer(),
            buffer.rate()
        )
        streamBuffer.reset()
    }

    companion object {
        private val logger = KLogger<OpenALStreamAudio>()
    }
}
