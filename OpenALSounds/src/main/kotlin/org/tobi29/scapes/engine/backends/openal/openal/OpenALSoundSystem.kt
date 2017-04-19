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
import org.tobi29.scapes.engine.utils.*
import org.tobi29.scapes.engine.utils.io.ReadSource
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.math.vector.Vector3d
import org.tobi29.scapes.engine.utils.math.vector.distanceSqr
import org.tobi29.scapes.engine.utils.math.vector.minus
import org.tobi29.scapes.engine.utils.task.Joiner
import org.tobi29.scapes.engine.utils.task.TaskExecutor

class OpenALSoundSystem(override val engine: ScapesEngine,
                        openAL: OpenAL,
                        maxSources: Int,
                        latency: Double) : SoundSystem {
    private val cache = ConcurrentHashMap<ReadSource, OpenALAudioData>()
    private val queue = ConcurrentLinkedQueue<(OpenAL) -> Unit>()
    private val audios = ConcurrentHashSet<OpenALAudio>()
    private val sources: IntArray
    private val joiner: Joiner
    private var origin = Vector3d.ZERO
    private var listenerPosition = Vector3d.ZERO
    private var listenerOrientation = Vector3d.ZERO
    private var listenerVelocity = Vector3d.ZERO

    init {
        sources = IntArray(maxSources)
        joiner = engine.taskExecutor.runThread({ joiner ->
            openAL.create()
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
                        queue.poll()(openAL)
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
            Unit
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
                           pitch: Float,
                           gain: Float,
                           state: Boolean) {
        queue({ openAL ->
            audios.add(OpenALStreamAudio(engine, asset, channel,
                    Vector3d.ZERO, Vector3d.ZERO, pitch, gain, state, false))
        })
    }

    override fun playMusic(asset: ReadSource,
                           channel: String,
                           pitch: Float,
                           gain: Float,
                           position: Vector3d,
                           velocity: Vector3d,
                           state: Boolean) {
        queue({ openAL ->
            audios.add(OpenALStreamAudio(engine, asset, channel, position,
                    velocity, pitch, gain, state, true))
        })
    }

    override fun playSound(asset: ReadSource,
                           channel: String,
                           pitch: Float,
                           gain: Float) {
        val time = System.nanoTime()
        queue({ openAL ->
            audios.add(OpenALEffectAudio(asset, channel, Vector3d.ZERO,
                    Vector3d.ZERO, pitch, gain, false, time))
        })
    }

    override fun playSound(asset: ReadSource,
                           channel: String,
                           position: Vector3d,
                           velocity: Vector3d,
                           pitch: Float,
                           gain: Float) {
        val time = System.nanoTime()
        queue({ openAL ->
            audios.add(OpenALEffectAudio(asset, channel, position, velocity,
                    pitch, gain, true, time))
        })
    }

    override fun playStaticAudio(asset: ReadSource,
                                 channel: String,
                                 pitch: Float,
                                 gain: Float): StaticAudio {
        val staticAudio = OpenALStaticAudio(asset, channel, pitch, gain)
        queue({ openAL -> audios.add(staticAudio) })
        return staticAudio
    }

    override fun stop(channel: String) {
        queue({ openAL ->
            val stopped = audios.filter { it.isPlaying(channel) }
            stopped.forEach { it.stop(this, openAL) }
            audios.removeAll(stopped)
        })
    }

    override fun dispose() {
        joiner.join()
    }

    fun volume(channel: String): Float {
        return engine.config.volume(channel).toFloat()
    }

    fun playSound(openAL: OpenAL,
                  buffer: Int,
                  pitch: Float,
                  gain: Float,
                  position: Vector3d,
                  velocity: Vector3d,
                  state: Boolean,
                  hasPosition: Boolean) {
        val source = freeSource(openAL, false, false)
        if (source == -1) {
            return
        }
        playSound(openAL, buffer, source, pitch, gain, position, velocity,
                state, hasPosition)
    }

    fun playSound(openAL: OpenAL,
                  buffer: Int,
                  source: Int,
                  pitch: Float,
                  gain: Float,
                  position: Vector3d,
                  velocity: Vector3d,
                  state: Boolean,
                  hasPosition: Boolean) {
        openAL.stop(source)
        openAL.setBuffer(source, buffer)
        openAL.setGain(source, gain)
        openAL.setPitch(source, pitch)
        openAL.setLooping(source, state)
        position(openAL, source, position, hasPosition)
        openAL.setVelocity(source, velocity)
        openAL.play(source)
    }

    fun position(openAL: OpenAL,
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

    internal operator fun get(openAL: OpenAL,
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

    private fun queue(consumer: (OpenAL) -> Unit) {
        queue.add(consumer)
        joiner.wake()
    }

    private fun freeSource(openAL: OpenAL,
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

    private fun isSoundPlaying(openAL: OpenAL): Boolean {
        return sources.any { it == -1 || !openAL.isStopped(it) }
    }

    companion object : KLogging()
}
