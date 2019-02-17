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

package org.tobi29.scapes.engine.backends.openal.openal

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.gitout.ktbindings.al.*
import org.tobi29.codec.AudioStream
import org.tobi29.contentinfo.mimeType
import org.tobi29.coroutines.Timer
import org.tobi29.coroutines.delayNanos
import org.tobi29.coroutines.newResponsiveContext
import org.tobi29.io.ByteViewERO
import org.tobi29.io.ReadSource
import org.tobi29.io.use
import org.tobi29.logging.KLogger
import org.tobi29.math.threadLocalRandom
import org.tobi29.math.vector.Vector3d
import org.tobi29.math.vector.distanceSqr
import org.tobi29.math.vector.minus
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.ScapesEngineConfig
import org.tobi29.scapes.engine.backends.openal.openal.internal.*
import org.tobi29.scapes.engine.sound.SoundException
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.sound.StaticAudio
import org.tobi29.scapes.engine.sound.VolumeChannel
import org.tobi29.scapes.engine.volume
import org.tobi29.stdex.ConcurrentHashSet
import org.tobi29.stdex.math.toRad
import org.tobi29.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

private typealias AudioData = Triple<ByteViewERO, Int, Int>

class OpenALSoundSystem(
    override val engine: ScapesEngine,
    maxSources: Int,
    latency: Double
) : CoroutineScope, SoundSystem {
    val speedOfSound = 343.3
    private val cache =
        HashMap<ReadSource, Either<Deferred<AudioData?>, OpenALAudioData>?>()
    private val queue =
        Channel<(ALCDevice, ALCContext, AL11) -> Unit>(Channel.UNLIMITED)
    private val job = Job()
    private val executor: CoroutineContext
    override val coroutineContext: CoroutineContext
        get() = job + executor
    private val audios = ConcurrentHashSet<OpenALAudio>()
    private val sources = Array(maxSources) { emptyALSource }
    private var enabled = false
    private var origin = Vector3d.ZERO
    private var listenerPosition = Vector3d.ZERO
    private var listenerOrientation = Vector3d.ZERO
    private var listenerVelocity = Vector3d.ZERO

    init {
        val (executor, closeExecutor) = CoroutineScope(job)
            .newResponsiveContext(CoroutineName("Engine-Sounds"))
        this.executor = executor
        launch {
            try {
                val (device, context, al) = start()
                try {
                    try {
                        al.checkError("Initializing")
                        val timer = Timer()
                        val maxDiff = (latency * 1000000.0).roundToLong()
                        var active = false
                        while (true) {
                            val tickDiff = if (active) {
                                timer.cap(maxDiff, { delayNanos(it) })
                            } else {
                                queue.receiveOrNull()
                                    ?.invoke(device, context, al)
                                0L
                            }
                            active = tick(device, context, al, tickDiff)
                        }
                    } finally {
                        queue.close()
                        audios.forEach {
                            it.stop(this@OpenALSoundSystem, al)
                        }
                        audios.clear()
                        cache.values.asSequence()
                            .filterIsInstance<EitherRight<OpenALAudioData>>()
                            .forEach {
                                it.value.dispose(this@OpenALSoundSystem, al)
                            }
                        for (i in sources.indices) {
                            val source = sources[i]
                            al.alSourceStop(source)
                            al.alSourceBuffer(source, AL_BUFFER, emptyALBuffer)
                            al.alDeleteSource(source)
                            sources[i] = emptyALSource
                        }
                        al.checkError("Disposing")
                    }
                } catch (e: SoundException) {
                    logger.warn(e) { "Fatal error in sound system" }
                } finally {
                    alcDestroyContext(context)
                    alcCloseDevice(device)
                }
            } finally {
                closeExecutor()
            }
        }
    }

    private fun start(): Triple<ALCDevice, ALCContext, AL11> {
        val device = alcOpenDevice(null)
        if (device == emptyALCDevice) {
            throw IllegalStateException(
                "Failed to open the default device."
            )
        }
        val context = alcCreateContext(device)
        if (context == emptyALCContext) {
            alcCloseDevice(device)
            throw IllegalStateException(
                "Failed to create an OpenAL context."
            )
        }
        alcMakeContextCurrent(context)
        val al = contextAL11()
        logger.info {
            "OpenAL: ${al.alGetString(
                AL_VERSION
            )} (Vendor: ${al.alGetString(
                AL_VENDOR
            )}, Renderer: ${al.alGetString(
                AL_RENDERER
            )})"
        }
        al.alSpeedOfSound(speedOfSound.toFloat())
        al.alDistanceModel(AL_INVERSE_DISTANCE_CLAMPED)
        al.setListener(Vector3d.ZERO, Vector3d.ZERO, Vector3d.ZERO)
        for (i in sources.indices) {
            sources[i] = al.alCreateSource()
        }
        alcDevicePauseSOFT(device)
        return Triple(device, context, al)
    }

    private fun enable(device: ALCDevice) {
        if (!enabled) {
            enabled = true
            alcDeviceResumeSOFT(device)
        }
    }

    private fun disable(device: ALCDevice) {
        if (enabled) {
            enabled = false
            alcDevicePauseSOFT(device)
        }
    }

    private fun tick(
        device: ALCDevice,
        context: ALCContext,
        al: AL11,
        tickDiff: Long
    ): Boolean {
        try {
            val delta = Timer.toDelta(tickDiff).coerceIn(0.0001, 0.1)
            var active = false
            if (!queue.isEmpty) {
                while (true) {
                    (queue.poll() ?: break).invoke(device, context, al)
                }
                active = true
            }
            val iterator = audios.iterator()
            if (iterator.hasNext()) active = true
            while (iterator.hasNext()) {
                val element = iterator.next()
                if (element.poll(
                        this@OpenALSoundSystem, al,
                        listenerPosition, delta
                    )) {
                    iterator.remove()
                }
            }
            al.checkError("Sound-Effects")
            al.setListener(
                listenerPosition.minus(origin),
                listenerOrientation, listenerVelocity
            )
            val distance = origin.distanceSqr(listenerPosition)
            if (distance > 1024.0
                && (distance > 4096.0 || !isSoundPlaying(al))) {
                origin = listenerPosition
            }
            al.checkError("Updating-System")
            return enabled && active
        } catch (e: SoundException) {
            logger.warn { "Error polling sound-system: $e" }
        }
        return false
    }

    override fun setListener(
        position: Vector3d,
        orientation: Vector3d,
        velocity: Vector3d
    ) {
        listenerPosition = position
        listenerOrientation = orientation
        listenerVelocity = velocity
    }

    override fun isPlaying(channel: VolumeChannel): Boolean {
        return audios.asSequence().filter { audio ->
            audio.isPlaying(channel)
        }.any()
    }

    override fun playMusic(
        asset: ReadSource,
        channel: VolumeChannel,
        state: Boolean,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ) {
        queue { _, _, _ ->
            audios.add(
                OpenALStreamAudio(
                    asset, channel, Vector3d.ZERO, Vector3d.ZERO,
                    state, false, pitch, gain, referenceDistance, rolloffFactor
                )
            )
        }
    }

    override fun playMusic(
        asset: ReadSource,
        channel: VolumeChannel,
        position: Vector3d,
        velocity: Vector3d,
        state: Boolean,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ) {
        queue { _, _, _ ->
            audios.add(
                OpenALStreamAudio(
                    asset, channel, position, velocity,
                    state, true, pitch, gain, referenceDistance, rolloffFactor
                )
            )
        }
    }

    override fun playSound(
        asset: ReadSource,
        channel: VolumeChannel,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ) {
        val time = steadyClock.timeSteadyNanos()
        queue { _, _, _ ->
            audios.add(
                OpenALEffectAudio(
                    asset, channel, Vector3d.ZERO,
                    Vector3d.ZERO, pitch, gain, referenceDistance,
                    rolloffFactor, false, time
                )
            )
        }
    }

    override fun playSound(
        asset: ReadSource,
        channel: VolumeChannel,
        position: Vector3d,
        velocity: Vector3d,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ) {
        val time = steadyClock.timeSteadyNanos()
        queue { _, _, _ ->
            audios.add(
                OpenALEffectAudio(
                    asset, channel, position, velocity,
                    pitch, gain, referenceDistance, rolloffFactor, true, time
                )
            )
        }
    }

    override fun playStaticAudio(
        asset: ReadSource,
        channel: VolumeChannel,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ): StaticAudio {
        val staticAudio = OpenALStaticAudio(
            asset, channel, pitch, gain,
            referenceDistance, rolloffFactor
        )
        queue { _, _, _ -> audios.add(staticAudio) }
        return staticAudio
    }

    override fun stop(channel: VolumeChannel) {
        queue { _, _, al ->
            val stopped = audios.filter { it.isPlaying(channel) }
            stopped.forEach { it.stop(this, al) }
            audios.removeAll(stopped)
        }
    }

    override fun enable() {
        queue { device, _, _ -> enable(device) }
    }

    override fun disable() {
        queue { device, _, _ -> disable(device) }
    }

    override fun clearCache() {
        queue { _, _, _ -> cache.clear() }
    }

    override suspend fun dispose() {
        job.cancelAndJoin()
    }

    internal fun volume(channel: VolumeChannel): Double {
        return engine[ScapesEngineConfig.COMPONENT].volume(channel)
    }

    internal fun position(
        al: AL11,
        source: ALSource,
        position: Vector3d,
        hasPosition: Boolean
    ) {
        if (hasPosition) {
            al.alSourcei(source, AL_SOURCE_RELATIVE, AL_FALSE)
            val relativePosition = position.minus(origin)
            al.alSource3f(
                source, AL_POSITION, relativePosition.x.toFloat(),
                relativePosition.y.toFloat(), relativePosition.z.toFloat()
            )
        } else {
            al.alSourcei(source, AL_SOURCE_RELATIVE, AL_TRUE)
            al.alSource3f(
                source, AL_POSITION, position.x.toFloat(), position.y.toFloat(),
                position.z.toFloat()
            )
        }
    }

    internal fun getAudioData(
        al: AL11,
        asset: ReadSource
    ): Option<OpenALAudioData?> {
        val entry = cache[asset]
        return if (entry == null) {
            cache[asset] = EitherLeft(async {
                try {
                    asset.channel().use { channel ->
                        AudioStream.create(channel, asset.mimeType())
                            .use { readAudioData(it) }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Catch internal errors from decoding
                    // (Old) Android is buggy
                    logger.error(e) { "Failed decoding sound effect" }
                    null
                }
            })
            nil
        } else when (entry) {
            is EitherLeft<Deferred<AudioData?>> ->
                if (entry.value.isCompleted) {
                    val completed = entry.value.getCompleted()
                        ?.let {
                            OpenALAudioData(
                                it.first, it.second, it.third,
                                al
                            )
                        }
                    cache[asset] = completed?.let { EitherRight(completed) }
                    EitherLeft(completed) // FIXME: Compiler bug
                } else nil
            is EitherRight<OpenALAudioData> -> EitherLeft(entry.value) // FIXME: Compiler bug
        }
    }

    internal fun removeBufferFromSources(
        al: AL11,
        buffer: ALBuffer
    ) {
        for (source in sources) {
            if (al.alGetSourceBuffer(source, AL_BUFFER) == buffer) {
                al.alSourceStop(source)
                al.alSourceBuffer(source, AL_BUFFER, emptyALBuffer)
            }
        }
    }

    internal fun freeSource(al: AL11): ALSource {
        val random = threadLocalRandom()
        val offset = random.nextInt(sources.size)
        for (i in sources.indices) {
            val j = (i + offset) % sources.size
            val source = sources[j]
            if (al.isStopped(source)) {
                return source
            }
        }
        return emptyALSource
    }

    private fun queue(consumer: (ALCDevice, ALCContext, AL11) -> Unit) {
        if (!queue.offer(consumer)) throw IllegalStateException("Queue full")
    }

    private fun isSoundPlaying(al: AL11): Boolean {
        return sources.any { !al.isStopped(it) }
    }

    companion object {
        internal val logger = KLogger<OpenALSoundSystem>()
    }
}

internal fun AL10.checkError(message: String) {
    val error = alGetError()
    if (error != AL_NO_ERROR) {
        throw SoundException("${alGetString(error)} in $message")
    }
}

internal fun AL10.setListener(
    position: Vector3d,
    orientation: Vector3d,
    velocity: Vector3d
) {
    val cos = cos(orientation.x.toFloat().toRad())
    val lookX = cos(orientation.z.toFloat().toRad()) * cos
    val lookY = sin(orientation.z.toFloat().toRad()) * cos
    val lookZ = sin(orientation.x.toFloat().toRad())
    alListenerfv(
        AL_ORIENTATION, floatArrayOf(
            lookX, lookY, lookZ, 0.0f, 0.0f, 1.0f
        )
    )
    alListener3f(
        AL_POSITION, position.x.toFloat(),
        position.y.toFloat(), position.z.toFloat()
    )
    alListener3f(
        AL_VELOCITY, velocity.x.toFloat(),
        velocity.y.toFloat(), velocity.z.toFloat()
    )
}

internal fun AL10.isStopped(source: ALSource): Boolean {
    val state = alGetSourcei(source, AL_SOURCE_STATE)
    return state != AL_PLAYING && state != AL_PAUSED
}
