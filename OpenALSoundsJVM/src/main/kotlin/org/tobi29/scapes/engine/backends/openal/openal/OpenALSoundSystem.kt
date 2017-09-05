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

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.runBlocking
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.ScapesEngineConfig
import org.tobi29.scapes.engine.backends.openal.openal.internal.*
import org.tobi29.scapes.engine.codec.AudioStream
import org.tobi29.scapes.engine.sound.SoundException
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.sound.StaticAudio
import org.tobi29.scapes.engine.utils.AtomicBoolean
import org.tobi29.scapes.engine.utils.ConcurrentHashSet
import org.tobi29.scapes.engine.utils.ConcurrentLinkedQueue
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.io.ReadSource
import org.tobi29.scapes.engine.utils.io.use
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.math.roundL
import org.tobi29.scapes.engine.utils.math.threadLocalRandom
import org.tobi29.scapes.engine.utils.math.vector.Vector3d
import org.tobi29.scapes.engine.utils.math.vector.distanceSqr
import org.tobi29.scapes.engine.utils.math.vector.minus
import org.tobi29.scapes.engine.utils.steadyClock
import org.tobi29.scapes.engine.utils.task.ThreadJob
import org.tobi29.scapes.engine.utils.task.Timer
import org.tobi29.scapes.engine.utils.task.launchThread
import org.tobi29.scapes.engine.volume
import java.util.concurrent.locks.LockSupport

class OpenALSoundSystem(override val engine: ScapesEngine,
                        openAL: OpenAL,
                        maxSources: Int,
                        latency: Double) : SoundSystem {
    val speedOfSound = 343.3
    private val cache = HashMap<ReadSource, OpenALAudioData>()
    private val queue = ConcurrentLinkedQueue<(OpenAL) -> Unit>()
    private val audios = ConcurrentHashSet<OpenALAudio>()
    private val sources = IntArray(maxSources)
    private var updateJob: Pair<ThreadJob, AtomicBoolean>? = null
    private var origin = Vector3d.ZERO
    private var listenerPosition = Vector3d.ZERO
    private var listenerOrientation = Vector3d.ZERO
    private var listenerVelocity = Vector3d.ZERO

    init {
        val stop = AtomicBoolean(false)
        updateJob = launchThread("Engine-Sounds", engine.taskExecutor[Job]) {
            openAL.create(speedOfSound)
            for (i in sources.indices) {
                sources[i] = openAL.createSource()
            }
            openAL.checkError("Initializing")
            val timer = Timer()
            val maxDiff = roundL(latency * 1000000.0)
            timer.init()
            var active = true
            while (!stop.get()) {
                val tickDiff =
                        if (active) {
                            timer.cap(maxDiff, { LockSupport.parkNanos(it) })
                        } else {
                            LockSupport.park()
                            0L
                        }
                try {
                    val delta = Timer.toDelta(tickDiff).coerceIn(0.0001, 0.1)
                    active = false
                    if (queue.isNotEmpty()) {
                        while (true) {
                            (queue.poll() ?: break).invoke(openAL)
                        }
                        active = true
                    }
                    val iterator = audios.iterator()
                    if (iterator.hasNext()) active = true
                    while (iterator.hasNext()) {
                        val element = iterator.next()
                        if (element.poll(this@OpenALSoundSystem, openAL,
                                listenerPosition, delta)) {
                            iterator.remove()
                        }
                    }
                    openAL.checkError("Sound-Effects")
                    openAL.setListener(listenerPosition.minus(origin),
                            listenerOrientation, listenerVelocity)
                    val distance = origin.distanceSqr(listenerPosition)
                    if (distance > 1024.0
                            && (distance > 4096.0 || !isSoundPlaying(openAL))) {
                        origin = listenerPosition
                    }
                    openAL.checkError("Updating-System")
                    if (!active) active = isSoundPlaying(openAL)
                } catch (e: SoundException) {
                    logger.warn { "Error polling sound-system: $e" }
                }
            }
            try {
                audios.forEach { it.stop(this@OpenALSoundSystem, openAL) }
                audios.clear()
                for (audioData in cache.values) {
                    audioData.dispose(this@OpenALSoundSystem, openAL)
                }
                for (i in sources.indices) {
                    val source = sources[i]
                    openAL.stop(source)
                    openAL.setBuffer(source, 0)
                    openAL.deleteSource(source)
                    sources[i] = -1
                }
                openAL.checkError("Disposing")
            } catch (e: SoundException) {
                logger.warn { "Error disposing sound-system: $e" }
            }
            openAL.destroy()
        } to stop
    }

    override fun setListener(position: Vector3d,
                             orientation: Vector3d,
                             velocity: Vector3d) {
        listenerPosition = position
        listenerOrientation = orientation
        listenerVelocity = velocity
    }

    override fun isPlaying(channel: String): Boolean {
        return audios.asSequence().filter { audio ->
            audio.isPlaying(channel)
        }.any()
    }

    override fun playMusic(asset: ReadSource,
                           channel: String,
                           state: Boolean,
                           pitch: Double,
                           gain: Double,
                           referenceDistance: Double,
                           rolloffFactor: Double) {
        queue {
            audios.add(OpenALStreamAudio(engine, asset, channel, Vector3d.ZERO,
                    Vector3d.ZERO, state, false, pitch, gain, referenceDistance,
                    rolloffFactor))
        }
    }

    override fun playMusic(asset: ReadSource,
                           channel: String,
                           position: Vector3d,
                           velocity: Vector3d,
                           state: Boolean,
                           pitch: Double,
                           gain: Double,
                           referenceDistance: Double,
                           rolloffFactor: Double) {
        queue {
            audios.add(OpenALStreamAudio(engine, asset, channel, position,
                    velocity, state, true, pitch, gain, referenceDistance,
                    rolloffFactor))
        }
    }

    override fun playSound(asset: ReadSource,
                           channel: String,
                           pitch: Double,
                           gain: Double,
                           referenceDistance: Double,
                           rolloffFactor: Double) {
        val time = steadyClock.timeSteadyNanos()
        queue {
            audios.add(OpenALEffectAudio(asset, channel, Vector3d.ZERO,
                    Vector3d.ZERO, pitch, gain, referenceDistance,
                    rolloffFactor, false, time))
        }
    }

    override fun playSound(asset: ReadSource,
                           channel: String,
                           position: Vector3d,
                           velocity: Vector3d,
                           pitch: Double,
                           gain: Double,
                           referenceDistance: Double,
                           rolloffFactor: Double) {
        val time = steadyClock.timeSteadyNanos()
        queue {
            audios.add(OpenALEffectAudio(asset, channel, position, velocity,
                    pitch, gain, referenceDistance, rolloffFactor, true, time))
        }
    }

    override fun playStaticAudio(asset: ReadSource,
                                 channel: String,
                                 pitch: Double,
                                 gain: Double,
                                 referenceDistance: Double,
                                 rolloffFactor: Double): StaticAudio {
        val staticAudio = OpenALStaticAudio(asset, channel, pitch, gain,
                referenceDistance, rolloffFactor)
        queue { audios.add(staticAudio) }
        return staticAudio
    }

    override fun stop(channel: String) {
        queue { openAL ->
            val stopped = audios.filter { it.isPlaying(channel) }
            stopped.forEach { it.stop(this, openAL) }
            audios.removeAll(stopped)
        }
    }

    override fun clearCache() {
        queue { cache.clear() }
    }

    override fun dispose() {
        updateJob?.let { (job, stop) ->
            stop.set(true)
            LockSupport.unpark(job.thread)
            runBlocking { job.join() }
        }
    }

    internal fun volume(channel: String): Double {
        return engine[ScapesEngineConfig.COMPONENT].volume(channel)
    }

    internal fun playSound(openAL: OpenAL,
                           source: Int,
                           position: Vector3d,
                           velocity: Vector3d,
                           state: Boolean,
                           hasPosition: Boolean) {
        openAL.setLooping(source, state)
        position(openAL, source, position, hasPosition)
        openAL.setVelocity(source, velocity)
        openAL.setMaxDistance(source, Double.POSITIVE_INFINITY)
        openAL.play(source)
    }

    internal fun position(openAL: OpenAL,
                          source: Int,
                          position: Vector3d,
                          hasPosition: Boolean) {
        openAL.setRelative(source, !hasPosition)
        if (hasPosition) {
            openAL.setPosition(source, position.minus(origin))
        } else {
            openAL.setPosition(source, position)
        }
    }

    internal fun getAudioData(openAL: OpenAL,
                              asset: ReadSource): OpenALAudioData? {
        if (!cache.containsKey(asset)) {
            if (asset.exists()) {
                try {
                    AudioStream.create(asset).use {
                        cache.put(asset,
                                OpenALAudioData.read(engine, it, openAL))
                    }
                } catch (e: IOException) {
                    logger.error(e) { "Failed to get audio data" }
                }
            }
        }
        return cache[asset]
    }

    internal fun removeBufferFromSources(openAL: OpenAL,
                                         buffer: Int) {
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

    private fun queue(consumer: (OpenAL) -> Unit) {
        queue.add(consumer)
        updateJob?.let { (job, _) ->
            LockSupport.unpark(job.thread)
        }
    }

    private fun isSoundPlaying(openAL: OpenAL): Boolean {
        return sources.any { !openAL.isStopped(it) }
    }

    companion object : KLogging()
}
