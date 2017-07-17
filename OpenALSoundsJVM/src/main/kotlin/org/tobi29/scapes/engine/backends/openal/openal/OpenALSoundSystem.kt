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

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.backends.openal.openal.internal.*
import org.tobi29.scapes.engine.codec.AudioStream
import org.tobi29.scapes.engine.sound.SoundException
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.sound.StaticAudio
import org.tobi29.scapes.engine.utils.ConcurrentHashSet
import org.tobi29.scapes.engine.utils.ConcurrentLinkedQueue
import org.tobi29.scapes.engine.utils.Sync
import org.tobi29.scapes.engine.utils.assert
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.io.ReadSource
import org.tobi29.scapes.engine.utils.io.use
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.math.threadLocalRandom
import org.tobi29.scapes.engine.utils.math.vector.Vector3d
import org.tobi29.scapes.engine.utils.math.vector.distanceSqr
import org.tobi29.scapes.engine.utils.math.vector.minus
import org.tobi29.scapes.engine.utils.task.Joiner
import org.tobi29.scapes.engine.utils.task.TaskExecutor

class OpenALSoundSystem(override val engine: ScapesEngine,
                        openAL: OpenAL,
                        maxSources: Int,
                        latency: Double) : SoundSystem {
    val speedOfSound = 343.3
    private val cache = HashMap<ReadSource, OpenALAudioData>()
    private val queue = ConcurrentLinkedQueue<(OpenAL) -> Unit>()
    private val audios = ConcurrentHashSet<OpenALAudio>()
    private val sources = IntArray(maxSources)
    private val joiner: Joiner
    private var origin = Vector3d.ZERO
    private var listenerPosition = Vector3d.ZERO
    private var listenerOrientation = Vector3d.ZERO
    private var listenerVelocity = Vector3d.ZERO

    init {
        joiner = engine.taskExecutor.runThread({ joiner ->
            openAL.create(speedOfSound)
            for (i in sources.indices) {
                sources[i] = openAL.createSource()
            }
            openAL.checkError("Initializing")
            val sync = Sync(1000.0 / latency, 0, false, "Sound")
            sync.init()
            while (!joiner.marked) {
                try {
                    val delta = sync.delta()
                    while (!queue.isEmpty()) {
                        queue.poll()?.invoke(openAL)
                    }
                    val iterator = audios.iterator()
                    while (iterator.hasNext()) {
                        val element = iterator.next()
                        if (element.poll(this, openAL, listenerPosition,
                                delta)) {
                            iterator.remove()
                        }
                    }
                    openAL.checkError("Sound-Effects")
                    openAL.setListener(listenerPosition.minus(origin),
                            listenerOrientation, listenerVelocity)
                    val distance = origin.distanceSqr(listenerPosition)
                    if (distance > 1024.0 && (distance > 4096.0 || !isSoundPlaying(
                            openAL))) {
                        origin = listenerPosition
                    }
                    openAL.checkError("Updating-System")
                } catch (e: SoundException) {
                    logger.warn { "Error polling sound-system: $e" }
                }
                sync.cap(joiner)
            }
            try {
                audios.forEach { it.stop(this, openAL) }
                audios.clear()
                for (i in sources.indices) {
                    val source = sources[i]
                    openAL.stop(source)
                    openAL.setBuffer(source, 0)
                    openAL.deleteSource(source)
                    sources[i] = -1
                }
                for (audioData in cache.values) {
                    audioData.dispose(this, openAL)
                }
                openAL.checkError("Disposing")
            } catch (e: SoundException) {
                logger.warn { "Error disposing sound-system: $e" }
            }
            openAL.destroy()
        }, "Sound", TaskExecutor.Priority.HIGH)
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
        val time = System.nanoTime()
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
        val time = System.nanoTime()
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
        joiner.join()
    }

    internal fun volume(channel: String): Double {
        return engine.config.volume(channel)
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

    internal fun takeSource(openAL: OpenAL): Int {
        val source = freeSource(openAL, true, true)
        if (source == -1) {
            return -1
        }
        openAL.setBuffer(source, 0)
        return source
    }

    internal fun releaseSource(openAL: OpenAL,
                               source: Int) {
        openAL.stop(source)
        openAL.setBuffer(source, 0)
        for (i in sources.indices) {
            if (sources[i] == -1) {
                sources[i] = source
                return
            }
        }
        assert { false }
    }

    internal fun removeBufferFromSources(openAL: OpenAL,
                                         buffer: Int) {
        for (source in sources) {
            if (source != -1 && openAL.getBuffer(source) == buffer) {
                openAL.stop(source)
                openAL.setBuffer(source, 0)
            }
        }
    }

    internal fun freeSource(openAL: OpenAL,
                            force: Boolean,
                            take: Boolean): Int {
        val random = threadLocalRandom()
        val offset = random.nextInt(sources.size)
        for (i in sources.indices) {
            val j = (i + offset) % sources.size
            val source = sources[j]
            if (source != -1 && openAL.isStopped(source)) {
                if (take) {
                    sources[j] = -1
                }
                return source
            }
        }
        if (force) {
            for (i in sources.indices) {
                val j = (i + offset) % sources.size
                val source = sources[j]
                if (source != -1) {
                    openAL.stop(source)
                    if (take) {
                        sources[j] = -1
                    }
                    return source
                }
            }
        }
        return -1
    }

    private fun queue(consumer: (OpenAL) -> Unit) {
        queue.add(consumer)
        joiner.wake()
    }

    private fun isSoundPlaying(openAL: OpenAL): Boolean {
        return sources.any { it == -1 || !openAL.isStopped(it) }
    }

    companion object : KLogging()
}
