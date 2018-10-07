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

package org.tobi29.scapes.engine.backends.js.audio

import kotlinx.coroutines.experimental.*
import org.tobi29.coroutines.Timer
import org.tobi29.coroutines.awaitEvent
import org.tobi29.coroutines.loopUntilCancel
import org.tobi29.io.ReadSource
import org.tobi29.io.readAsInt8Array
import org.tobi29.io.useUri
import org.tobi29.math.vector.Vector3d
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.ScapesEngineConfig
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.sound.StaticAudio
import org.tobi29.scapes.engine.sound.VolumeChannel
import org.tobi29.scapes.engine.sound.dummy.DummyStaticAudio
import org.tobi29.scapes.engine.volume
import org.tobi29.stdex.ConcurrentHashSet
import org.tobi29.stdex.computeAbsent
import org.w3c.dom.events.Event
import org.w3c.webaudio.Audio
import org.w3c.webaudio.AudioBuffer
import org.w3c.webaudio.AudioNode
import org.w3c.webaudio.createAudioContext

class WebAudioSoundSystem(override val engine: ScapesEngine) : SoundSystem {
    private val channels = HashMap<String, ChannelGainNode>()
    private val cache = HashMap<ReadSource, Deferred<AudioBuffer>>()
    private val context = createAudioContext()
    private val config = engine[ScapesEngineConfig.COMPONENT]
    private val updateJob = engine.launch {
        while (true) {
            Timer().apply { init() }.loopUntilCancel(
                Timer.toDiff(10.0)
            ) { delta ->
                channels.values.forEach { it.poll(delta) }
            }
        }
    }

    init {
        context.suspend()
    }

    override fun setListener(
        position: Vector3d,
        orientation: Vector3d,
        velocity: Vector3d
    ) {
    }

    override fun isPlaying(channel: VolumeChannel): Boolean =
        channels.asSequence()
            .any { (key, _) -> key in channel }

    override fun playMusic(
        asset: ReadSource,
        channel: VolumeChannel,
        state: Boolean,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ) {
        engine.launch(engine.taskExecutor) {
            asset.useUri { uri ->
                val element = Audio(uri.toString())
                element.volume = gain
                element.playbackRate = pitch
                element.loop = state

                val node = context.createMediaElementSource(element)
                val channelNode = channelNode(channel)
                var stopped = false
                val audioElement = object :
                    AudioElement {
                    override fun stop() {
                        element.pause()
                        stopped = true
                    }
                }

                channelNode.elements.add(audioElement)
                node.connect(channelNode.node)
                element.play()

                // TODO: Avoid busy wait
                while (!stopped && (state || !element.ended)) {
                    delay(250)
                }

                node.disconnect()
                channelNode.elements.remove(audioElement)
                disconnectIfEmpty(channelNode)
            }
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
        // TODO: 3D audio
        playMusic(
            asset, channel, state, pitch, gain, referenceDistance,
            rolloffFactor
        )
    }

    override fun playSound(
        asset: ReadSource,
        channel: VolumeChannel,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ) {
        val buffer = cachedDecode(asset)
        engine.launch(engine.taskExecutor) {
            val node = context.createBufferSource()
            node.buffer = buffer.await()
            node.playbackRate.value = pitch
            val channelNode = channelNode(channel)
            val audioElement = object :
                AudioElement {
                override fun stop() {}
            }

            channelNode.elements.add(audioElement)
            node.connect(channelNode.node)

            node.start()

            node.awaitEvent<Event>("ended")

            node.disconnect()
            channelNode.elements.remove(audioElement)
            disconnectIfEmpty(channelNode)
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
        // TODO: 3D audio
        playSound(asset, channel, pitch, gain, referenceDistance, rolloffFactor)
    }

    override fun playStaticAudio(
        asset: ReadSource,
        channel: VolumeChannel,
        pitch: Double,
        gain: Double,
        referenceDistance: Double,
        rolloffFactor: Double
    ): StaticAudio {
        return DummyStaticAudio()
    }

    override fun stop(channel: VolumeChannel) {
        channels.asSequence()
            .filter { (key, _) -> key in channel }
            .forEach { (_, node) -> node.elements.forEach { it.stop() } }
    }

    override fun enable() {
        context.resume()
    }

    override fun disable() {
        context.suspend()
    }

    override fun clearCache() {}

    override suspend fun dispose() {
        updateJob.cancelAndJoin()
        context.close()
    }

    private fun channelNode(channel: VolumeChannel): ChannelGainNode =
        channels.computeAbsent(channel) {
            ChannelGainNode(
                context.destination,
                it,
                config
            )
        }

    private fun disconnectIfEmpty(node: ChannelGainNode) {
        if (node.elements.isEmpty()) channels.values.remove(node)
    }

    private fun cachedDecode(asset: ReadSource): Deferred<AudioBuffer> =
        cache.computeAbsent(asset) {
            engine.async(engine.taskExecutor) {
                // We need to copy the buffer either way to avoid it getting
                // detached
                val data = asset.data().readAsInt8Array()
                val buffer = context.decodeAudioData(
                    data.buffer.slice(
                        data.byteOffset,
                        data.byteOffset + data.byteLength
                    )
                )
                buffer.await()
            }
        }
}

private class ChannelGainNode(
    output: AudioNode,
    private val channel: VolumeChannel,
    private val config: ScapesEngineConfig
) {
    private var currentVolume = config.volume(channel)
    val node = output.context.createGain()
    val elements = ConcurrentHashSet<AudioElement>()

    init {
        node.gain.setValueAtTime(currentVolume, node.context.currentTime)
        node.connect(output)
    }

    fun poll(delta: Double) {
        val volume = config.volume(channel)
        if (volume != currentVolume) {
            currentVolume = volume
            node.gain.setTargetAtTime(
                currentVolume,
                node.context.currentTime, delta
            )
        }
    }

    fun dispose() {
        node.disconnect()
    }
}

interface AudioElement {
    fun stop()
}
