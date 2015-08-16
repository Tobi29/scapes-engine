/*
 * Copyright 2012-2015 Tobi29
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

package org.tobi29.scapes.engine.openal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.openal.codec.AudioStream;
import org.tobi29.scapes.engine.openal.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.BufferCreatorNative;
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource;
import org.tobi29.scapes.engine.utils.io.filesystem.Resource;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SoundSystem {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(SoundSystem.class);
    private final Map<String, AudioData> cache = new ConcurrentHashMap<>();
    private final Queue<Audio> queue = new ConcurrentLinkedQueue<>();
    private final List<Audio> audios = new ArrayList<>();
    private final int musicSource;
    private final int[] queuedBuffers = new int[3];
    private final int[] sources;
    private final ByteBuffer streamBuffer;
    private final FloatBuffer streamReadBuffer;
    private final ScapesEngine engine;
    private final OpenAL openAL;
    private Music music;
    private float musicVolume = 1, soundVolume = 1;
    private ReadableAudioStream stream;
    private Vector3 origin = Vector3d.ZERO, listenerPosition = Vector3d.ZERO,
            listenerOrientation = Vector3d.ZERO, listenerVelocity =
            Vector3d.ZERO;

    public SoundSystem(ScapesEngine engine, OpenAL openAL) {
        this.engine = engine;
        this.openAL = openAL;
        openAL.create();
        musicSource = openAL.createSource();
        streamReadBuffer = BufferCreator.floats(4096 << 2);
        streamBuffer =
                BufferCreatorNative.bytes(streamReadBuffer.capacity() << 1);
        for (int i = 0; i < queuedBuffers.length; i++) {
            queuedBuffers[i] = openAL.createBuffer();
        }
        sources = new int[64];
        for (int i = 0; i < sources.length; i++) {
            sources[i] = openAL.createSource();
        }
        openAL.checkError("Initializing");
    }

    public void dispose() {
        try {
            openAL.stop(musicSource);
            int queued = openAL.getBuffersQueued(musicSource);
            while (queued-- > 0) {
                openAL.unqueue();
            }
            audios.forEach(audio -> audio
                    .poll(this, openAL, listenerPosition, 1.0, false));
            for (AudioData audioData : cache.values()) {
                audioData.dispose(this, openAL);
            }
            openAL.deleteSource(musicSource);
            for (int queuedBuffer : queuedBuffers) {
                openAL.deleteBuffer(queuedBuffer);
            }
            for (int source : sources) {
                openAL.stop(source);
                openAL.setBuffer(source, 0);
                openAL.deleteSource(source);
            }
            openAL.checkError("Disposing");
        } catch (SoundException e) {
            LOGGER.warn("Error disposing sound-system: {}", e.toString());
        }
        openAL.destroy();
    }

    public void setListener(Vector3 position, Vector3 orientation,
            Vector3 velocity) {
        listenerPosition = position;
        listenerOrientation = orientation;
        listenerVelocity = velocity;
    }

    public boolean isMusicPlaying() {
        return music != null;
    }

    public void playMusic(String asset, float pitch, float gain) {
        playMusic(engine.files().get(asset), pitch, gain);
    }

    public void playMusic(String asset, float pitch, float gain,
            Vector3 position, Vector3 velocity) {
        playMusic(engine.files().get(asset), pitch, gain, position, velocity);
    }

    public void playMusic(ReadSource asset, float pitch, float gain) {
        music = new Music(asset, Vector3d.ZERO, Vector3d.ZERO, pitch, gain,
                false);
    }

    public void playMusic(ReadSource asset, float pitch, float gain,
            Vector3 position, Vector3 velocity) {
        music = new Music(asset, position, velocity, pitch, gain, true);
    }

    public void stopMusic() {
        music = new Music(null, Vector3d.ZERO, Vector3d.ZERO, 1.0f, 1.0f,
                false);
    }

    public float musicVolume() {
        return musicVolume;
    }

    public void playSound(String asset, float pitch, float gain) {
        playSound(asset, pitch, gain, 16.0f);
    }

    public void playSound(String asset, float pitch, float gain, float range) {
        queue.add(new EffectAudio(asset, Vector3d.ZERO, Vector3d.ZERO, pitch,
                gain, range, false));
    }

    public void playSound(String asset, Vector3 position, Vector3 velocity,
            float pitch, float gain) {
        playSound(asset, position, velocity, pitch, gain, 16.0f);
    }

    public void playSound(String asset, Vector3 position, Vector3 velocity,
            float pitch, float gain, float range) {
        queue.add(new EffectAudio(asset, position, velocity, pitch, gain, range,
                true));
    }

    public StaticAudio playStaticAudio(String asset, float pitch, float gain) {
        StaticAudio staticAudio = new StaticAudio(asset, pitch, gain);
        queue.add(staticAudio);
        return staticAudio;
    }

    public float soundVolume() {
        return soundVolume;
    }

    public void poll(double delta) {
        try {
            Music music = this.music;
            if (music != null) {
                if (!music.playing) {
                    if (stream != null) {
                        stream.close();
                        stream = null;
                    }
                    openAL.stop(musicSource);
                    int queued = openAL.getBuffersQueued(musicSource);
                    while (queued-- > 0) {
                        openAL.unqueue();
                    }
                    if (music.asset == null) {
                        this.music = null;
                    } else {
                        try {
                            ReadSource asset = music.asset;
                            stream = AudioStream.create(asset);
                            try {
                                for (int queue : queuedBuffers) {
                                    stream(queue);
                                    openAL.queue(musicSource, queue);
                                }
                            } catch (IOException e) {
                                engine.crash(e);
                            }
                            openAL.setPitch(musicSource, music.pitch);
                            openAL.setGain(musicSource,
                                    music.gain * musicVolume);
                            openAL.setLooping(musicSource, false);
                            if (music.hasPosition) {
                                openAL.setRelative(musicSource, false);
                                openAL.setPosition(musicSource,
                                        music.pos.minus(origin));
                            } else {
                                openAL.setRelative(musicSource, true);
                                openAL.setPosition(musicSource, music.pos);
                            }
                            openAL.setVelocity(musicSource, music.velocity);
                            openAL.play(musicSource);
                        } catch (IOException e) {
                            LOGGER.warn("Failed to play music: {}",
                                    e.toString());
                        }
                        music.playing = true;
                    }
                }
                if (stream != null) {
                    try {
                        int finished = openAL.getBuffersPrecessed(musicSource);
                        while (finished-- > 0) {
                            int unqueued = openAL.unqueue();
                            if (stream(unqueued)) {
                                openAL.queue(musicSource, unqueued);
                            }
                        }
                        if (!openAL.isPlaying(musicSource)) {
                            openAL.play(musicSource);
                        }
                    } catch (IOException e) {
                        LOGGER.warn("Error during stream: {}", e.toString());
                        stream = null;
                    }
                } else {
                    this.music = null;
                }
            }
            openAL.checkError("Music-Streaming");
            while (!queue.isEmpty()) {
                audios.add(queue.poll());
            }
            boolean lagSilence =
                    delta > 1.0; // Prevent accumulating sounds on lag spikes
            audios.removeAll(audios.stream().filter(audio -> audio
                    .poll(this, openAL, listenerPosition, delta, lagSilence))
                    .collect(Collectors.toList()));
            openAL.checkError("Sound-Effects");
            float musicVolume = (float) engine.config().musicVolume();
            if (music != null && musicVolume != this.musicVolume) {
                openAL.setGain(musicSource, music.gain * musicVolume);
            }
            this.musicVolume = musicVolume;
            soundVolume = (float) engine.config().soundVolume();
            openAL.setListener(listenerPosition.minus(origin),
                    listenerOrientation, listenerVelocity);
            if (!isSoundPlaying()) {
                origin = listenerPosition;
                if (music != null && music.hasPosition) {
                    openAL.setPosition(musicSource, music.pos.minus(origin));
                }
            }
            openAL.checkError("Updating-System");
        } catch (SoundException e) {
            LOGGER.warn("Error polling sound-system: {}", e.toString());
        }
    }

    protected void playSound(int buffer, float pitch, float gain, float range,
            Vector3 position, Vector3 velocity, boolean state,
            boolean hasPosition) {
        if (hasPosition) {
            if (FastMath.pointDistanceSqr(listenerPosition, position) >
                    range * range) {
                return;
            }
        }
        int source = freeSource();
        if (source == -1) {
            return;
        }
        playSound(buffer, source, pitch, gain, range, position, velocity, state,
                hasPosition);
    }

    protected void playSound(int buffer, int source, float pitch, float gain,
            float range, Vector3 position, Vector3 velocity, boolean state,
            boolean hasPosition) {
        gain *= soundVolume;
        openAL.stop(source);
        openAL.setBuffer(source, buffer);
        openAL.setGain(source, gain * range / 16.0f);
        openAL.setPitch(source, pitch);
        openAL.setLooping(source, state);
        openAL.setRelative(source, !hasPosition);
        if (hasPosition) {
            openAL.setPosition(source, position.minus(origin));
        } else {
            openAL.setPosition(source, position);
        }
        openAL.setVelocity(source, velocity);
        openAL.play(source);
    }

    protected Optional<AudioData> get(String asset) {
        if (!cache.containsKey(asset)) {
            Resource resource = engine.files().get(asset);
            if (resource.exists()) {
                try (ReadableAudioStream stream = AudioStream
                        .create(resource)) {
                    cache.put(asset, new AudioData(stream, openAL));
                } catch (IOException e) {
                    LOGGER.warn("Failed to get audio data", e);
                }
            }
        }
        return Optional.ofNullable(cache.get(asset));
    }

    protected void removeBufferFromSources(int buffer) {
        Arrays.stream(sources)
                .filter(source -> openAL.getBuffer(source) == buffer)
                .forEach(source -> {
                    openAL.stop(source);
                    openAL.setBuffer(source, 0);
                });
    }

    private int freeSource() {
        Random random = ThreadLocalRandom.current();
        int offset = random.nextInt(sources.length);
        for (int i = 0; i < sources.length; i++) {
            int source = sources[(i + offset) % sources.length];
            if (openAL.isStopped(source)) {
                return source;
            }
        }
        return -1;
    }

    private boolean isSoundPlaying() {
        for (int source : sources) {
            if (!openAL.isStopped(source)) {
                return true;
            }
        }
        return false;
    }

    private boolean stream(int buffer) throws IOException {
        if (stream == null) {
            return true;
        }
        boolean valid = true;
        while (streamReadBuffer.hasRemaining() && valid) {
            valid = stream.getSome(streamReadBuffer);
        }
        streamReadBuffer.flip();
        streamBuffer.clear();
        while (streamReadBuffer.hasRemaining()) {
            streamBuffer.putShort(PCMUtil.toInt32(streamReadBuffer.get()));
        }
        streamReadBuffer.clear();
        streamBuffer.flip();
        openAL.storeBuffer(buffer,
                stream.channels() > 1 ? AudioFormat.STEREO : AudioFormat.MONO,
                streamBuffer, stream.rate());
        return valid;
    }

    private static class Music {
        private final ReadSource asset;
        private final Vector3 pos, velocity;
        private final float pitch, gain;
        private final boolean hasPosition;
        private boolean playing;

        private Music(ReadSource asset, Vector3 pos, Vector3 velocity,
                float pitch, float gain, boolean hasPosition) {
            this.asset = asset;
            this.pos = pos;
            this.velocity = velocity;
            this.pitch = pitch;
            this.gain = gain;
            this.hasPosition = hasPosition;
        }
    }
}
