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

package org.tobi29.scapes.engine.backends.openal.openal

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.LinkedListChannel
import org.tobi29.codec.AudioStream
import org.tobi29.contentinfo.mimeType
import org.tobi29.coroutines.ThreadJob
import org.tobi29.coroutines.Timer
import org.tobi29.coroutines.launchThread
import org.tobi29.io.ByteViewERO
import org.tobi29.io.ReadSource
import org.tobi29.io.use
import org.tobi29.logging.KLogging
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
import org.tobi29.utils.*
import java.util.concurrent.locks.LockSupport
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.math.roundToLong

private typealias AudioData = Triple<ByteViewERO, Int, Int>

class OpenALSoundSystem(
    override val engine: ScapesEngine,
    openAL: OpenAL,
    maxSources: Int,
    latency: Double
) : CoroutineDispatcher(),
    SoundSystem {
    val speedOfSound = 343.3
    private val cache =
        HashMap<ReadSource, Either<Deferred<AudioData?>, OpenALAudioData>?>()
    private val queue = LinkedListChannel<(OpenAL) -> Unit>()
    private val audios = ConcurrentHashSet<OpenALAudio>()
    private val sources = IntArray(maxSources)
    private var updateJob: ThreadJob? = null
    private var enabled = false
    private var origin = Vector3d.ZERO
    private var listenerPosition = Vector3d.ZERO
    private var listenerOrientation = Vector3d.ZERO
    private var listenerVelocity = Vector3d.ZERO

    init {
        updateJob = launchThread("Engine-Sounds") {
            try {
                start(openAL)
                try {
                    val timer = Timer()
                    val maxDiff = (latency * 1000000.0).roundToLong()
                    timer.init()
                    var active = false
                    while (true) {
                        val tickDiff =
                            if (active) {
                                timer.cap(maxDiff,
                                    { LockSupport.parkNanos(it) })
                            } else {
                                LockSupport.park()
                                0L
                            }
                        yield() // Allow cancel
                        active = tick(openAL, tickDiff)
                    }
                } finally {
                    audios.forEach {
                        it.stop(this@OpenALSoundSystem, openAL)
                    }
                    audios.clear()
                    cache.values.asSequence()
                        .filterIsInstance<EitherRight<OpenALAudioData>>()
                        .forEach {
                            it.value.dispose(this@OpenALSoundSystem, openAL)
                        }
                    for (i in sources.indices) {
                        val source = sources[i]
                        openAL.stop(source)
                        openAL.setBuffer(source, 0)
                        openAL.deleteSource(source)
                        sources[i] = -1
                    }
                    openAL.checkError("Disposing")
                }
            } catch (e: SoundException) {
                logger.warn(e) { "Fatal error in sound system" }
            } finally {
                openAL.destroy()
            }
        }
    }

    private fun start(openAL: OpenAL) {
        openAL.create(speedOfSound)
        for (i in sources.indices) {
            sources[i] = openAL.createSource()
        }
        openAL.pause()
        openAL.checkError("Initializing")
    }

    private fun enable(openAL: OpenAL) {
        if (!enabled) {
            enabled = true
            openAL.resume()
        }
    }

    private fun disable(openAL: OpenAL) {
        if (enabled) {
            enabled = false
            openAL.pause()
        }
    }

    private fun tick(
        openAL: OpenAL,
        tickDiff: Long
    ): Boolean {
        try {
            val delta = Timer.toDelta(tickDiff).coerceIn(0.0001, 0.1)
            var active = false
            if (!queue.isEmpty) {
                while (true) {
                    (queue.poll() ?: break).invoke(openAL)
                }
                active = true
            }
            val iterator = audios.iterator()
            if (iterator.hasNext()) active = true
            while (iterator.hasNext()) {
                val element = iterator.next()
                if (element.poll(
                        this@OpenALSoundSystem, openAL,
                        listenerPosition, delta
                    )) {
                    iterator.remove()
                }
            }
            openAL.checkError("Sound-Effects")
            openAL.setListener(
                listenerPosition.minus(origin),
                listenerOrientation, listenerVelocity
            )
            val distance = origin.distanceSqr(listenerPosition)
            if (distance > 1024.0
                && (distance > 4096.0 || !isSoundPlaying(openAL))) {
                origin = listenerPosition
            }
            openAL.checkError("Updating-System")
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
        queue {
            audios.add(
                OpenALStreamAudio(
                    engine, asset, channel, Vector3d.ZERO,
                    Vector3d.ZERO, state, false, pitch, gain, referenceDistance,
                    rolloffFactor
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
        queue {
            audios.add(
                OpenALStreamAudio(
                    engine, asset, channel, position,
                    velocity, state, true, pitch, gain, referenceDistance,
                    rolloffFactor
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
        queue {
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
        queue {
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
        queue { audios.add(staticAudio) }
        return staticAudio
    }

    override fun stop(channel: VolumeChannel) {
        queue { openAL ->
            val stopped = audios.filter { it.isPlaying(channel) }
            stopped.forEach { it.stop(this, openAL) }
            audios.removeAll(stopped)
        }
    }

    override fun enable() {
        queue(::enable)
    }

    override fun disable() {
        queue(::disable)
    }

    override fun clearCache() {
        queue { cache.clear() }
    }

    override fun dispose() {
        updateJob?.let { job ->
            job.cancel()
            LockSupport.unpark(job.thread)
            runBlocking { job.join() }
        }
    }

    internal fun volume(channel: VolumeChannel): Double {
        return engine[ScapesEngineConfig.COMPONENT].volume(channel)
    }

    internal fun playSound(
        openAL: OpenAL,
        source: Int,
        position: Vector3d,
        velocity: Vector3d,
        state: Boolean,
        hasPosition: Boolean
    ) {
        openAL.setLooping(source, state)
        position(openAL, source, position, hasPosition)
        openAL.setVelocity(source, velocity)
        openAL.setMaxDistance(source, Double.POSITIVE_INFINITY)
        openAL.play(source)
    }

    internal fun position(
        openAL: OpenAL,
        source: Int,
        position: Vector3d,
        hasPosition: Boolean
    ) {
        openAL.setRelative(source, !hasPosition)
        if (hasPosition) {
            openAL.setPosition(source, position.minus(origin))
        } else {
            openAL.setPosition(source, position)
        }
    }

    internal fun getAudioData(
        openAL: OpenAL,
        asset: ReadSource
    ): Option<OpenALAudioData?> {
        val entry = cache[asset]
        return if (entry == null) {
            cache[asset] = EitherLeft(async(engine.taskExecutor) {
                try {
                    asset.channel().use { channel ->
                        AudioStream.create(
                            channel,
                            asset.mimeType()
                        ).use { stream ->
                            OpenALAudioData.read(engine, stream)
                        }
                    }
                } catch (e: Exception) {
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
                                openAL
                            )
                        }
                    cache[asset] = completed?.let { EitherRight(completed) }
                    OptionSome(completed)
                } else nil
            is EitherRight<OpenALAudioData> -> OptionSome(entry.value)
        }
    }

    internal fun removeBufferFromSources(
        openAL: OpenAL,
        buffer: Int
    ) {
        for (source in sources) {
            if (openAL.getBuffer(source) == buffer) {
                openAL.stop(source)
                openAL.setBuffer(source, 0)
            }
        }
    }

    internal fun freeSource(openAL: OpenAL): Int {
        val random = threadLocalRandom()
        val offset = random.nextInt(sources.size)
        for (i in sources.indices) {
            val j = (i + offset) % sources.size
            val source = sources[j]
            if (openAL.isStopped(source)) {
                return source
            }
        }
        return -1
    }

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable
    ) {
        queue { block.run() }
    }

    private fun queue(consumer: (OpenAL) -> Unit) {
        if (!queue.offer(consumer)) throw IllegalStateException("Queue full")
        updateJob?.let { job ->
            LockSupport.unpark(job.thread)
        }
    }

    private fun isSoundPlaying(openAL: OpenAL): Boolean {
        return sources.any { !openAL.isStopped(it) }
    }

    companion object : KLogging()
}
