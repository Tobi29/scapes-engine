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
package org.tobi29.scapes.engine.sound.openal;

import java8.util.Optional;
import java8.util.function.Consumer;
import java8.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.sound.SoundException;
import org.tobi29.scapes.engine.sound.SoundSystem;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.Sync;
import org.tobi29.scapes.engine.utils.codec.AudioStream;
import org.tobi29.scapes.engine.utils.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;
import org.tobi29.scapes.engine.utils.task.Joiner;
import org.tobi29.scapes.engine.utils.task.TaskExecutor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

public class OpenALSoundSystem implements SoundSystem {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(OpenALSoundSystem.class);
    private final Map<String, OpenALAudioData> cache =
            new ConcurrentHashMap<>();
    private final Queue<Consumer<OpenAL>> queue = new ConcurrentLinkedQueue<>();
    private final Set<OpenALAudio> audios =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final int[] sources = new int[64];
    private final ScapesEngine engine;
    private final Joiner joiner;
    private Vector3 origin = Vector3d.ZERO, listenerPosition = Vector3d.ZERO,
            listenerOrientation = Vector3d.ZERO, listenerVelocity =
            Vector3d.ZERO;

    public OpenALSoundSystem(ScapesEngine engine, OpenAL openAL,
            double latency) {
        this.engine = engine;
        joiner = engine.taskExecutor().runTask(joiner -> {
            openAL.create();
            for (int i = 0; i < sources.length; i++) {
                sources[i] = openAL.createSource();
            }
            openAL.checkError("Initializing");
            Sync sync = new Sync(1000.0 / latency, 0, false, "Sound");
            sync.init();
            while (!joiner.marked()) {
                try {
                    double delta = sync.delta();
                    while (!queue.isEmpty()) {
                        queue.poll().accept(openAL);
                    }
                    Streams.of(audios).filter(audio -> audio
                            .poll(this, openAL, listenerPosition, delta))
                            .forEach(audios::remove);
                    openAL.checkError("Sound-Effects");
                    openAL.setListener(listenerPosition.minus(origin),
                            listenerOrientation, listenerVelocity);
                    double distance =
                            FastMath.pointDistanceSqr(origin, listenerPosition);
                    if (distance > 1024.0 &&
                            (distance > 4096.0 || !isSoundPlaying(openAL))) {
                        origin = listenerPosition;
                    }
                    openAL.checkError("Updating-System");
                } catch (SoundException e) {
                    LOGGER.warn("Error polling sound-system: {}", e.toString());
                }
                sync.cap(joiner);
            }
            try {
                Streams.of(audios).forEach(audio -> audio.stop(this, openAL));
                audios.clear();
                for (int i = 0; i < sources.length; i++) {
                    int source = sources[i];
                    openAL.stop(source);
                    openAL.setBuffer(source, 0);
                    openAL.deleteSource(source);
                    sources[i] = -1;
                }
                for (OpenALAudioData audioData : cache.values()) {
                    audioData.dispose(this, openAL);
                }
                openAL.checkError("Disposing");
            } catch (SoundException e) {
                LOGGER.warn("Error disposing sound-system: {}", e.toString());
            }
            openAL.destroy();
        }, "Sound", TaskExecutor.Priority.HIGH);
    }

    @Override
    public void setListener(Vector3 position, Vector3 orientation,
            Vector3 velocity) {
        listenerPosition = position;
        listenerOrientation = orientation;
        listenerVelocity = velocity;
    }

    @Override
    public boolean isPlaying(String channel) {
        return Streams.of(audios).filter(audio -> audio.isPlaying(channel))
                .findAny().isPresent();
    }

    @Override
    public void playMusic(String asset, String channel, float pitch, float gain,
            boolean state) {
        playMusic(engine.files().get(asset), channel, pitch, gain, state);
    }

    @Override
    public void playMusic(String asset, String channel, float pitch, float gain,
            Vector3 position, Vector3 velocity, boolean state) {
        playMusic(engine.files().get(asset), channel, pitch, gain, position,
                velocity, state);
    }

    @Override
    public void playMusic(ReadSource asset, String channel, float pitch,
            float gain, boolean state) {
        queue(openAL -> audios
                .add(new OpenALStreamAudio(asset, channel, Vector3d.ZERO,
                        Vector3d.ZERO, pitch, gain, state, false)));
    }

    @Override
    public void playMusic(ReadSource asset, String channel, float pitch,
            float gain, Vector3 position, Vector3 velocity, boolean state) {
        queue(openAL -> audios
                .add(new OpenALStreamAudio(asset, channel, position, velocity,
                        pitch, gain, state, true)));
    }

    @Override
    public void playSound(String asset, String channel, float pitch,
            float gain) {
        queue(openAL -> audios
                .add(new OpenALEffectAudio(asset, channel, Vector3d.ZERO,
                        Vector3d.ZERO, pitch, gain, false)));
    }

    @Override
    public void playSound(String asset, String channel, Vector3 position,
            Vector3 velocity, float pitch, float gain) {
        queue(openAL -> audios
                .add(new OpenALEffectAudio(asset, channel, position, velocity,
                        pitch, gain, true)));
    }

    @Override
    public OpenALStaticAudio playStaticAudio(String asset, String channel,
            float pitch, float gain) {
        OpenALStaticAudio staticAudio =
                new OpenALStaticAudio(asset, channel, pitch, gain);
        queue(openAL -> audios.add(staticAudio));
        return staticAudio;
    }

    @Override
    public void stop(String channel) {
        queue(openAL -> {
            Collection<OpenALAudio> stopped =
                    Streams.of(audios).filter(audio -> audio.isPlaying(channel))
                            .collect(Collectors.toList());
            Streams.of(stopped).forEach(audio -> audio.stop(this, openAL));
            audios.removeAll(stopped);
        });
    }

    @Override
    public void dispose() {
        joiner.join();
    }

    protected float volume(String channel) {
        return (float) engine.config().volume(channel);
    }

    protected void playSound(OpenAL openAL, int buffer, float pitch, float gain,
            Vector3 position, Vector3 velocity, boolean state,
            boolean hasPosition) {
        int source = freeSource(openAL, false, false);
        if (source == -1) {
            return;
        }
        playSound(openAL, buffer, source, pitch, gain, position, velocity,
                state, hasPosition);
    }

    protected void playSound(OpenAL openAL, int buffer, int source, float pitch,
            float gain, Vector3 position, Vector3 velocity, boolean state,
            boolean hasPosition) {
        openAL.stop(source);
        openAL.setBuffer(source, buffer);
        openAL.setGain(source, gain);
        openAL.setPitch(source, pitch);
        openAL.setLooping(source, state);
        position(openAL, source, position, hasPosition);
        openAL.setVelocity(source, velocity);
        openAL.play(source);
    }

    protected void position(OpenAL openAL, int source, Vector3 position,
            boolean hasPosition) {
        openAL.setRelative(source, !hasPosition);
        if (hasPosition) {
            openAL.setPosition(source, position.minus(origin));
        } else {
            openAL.setPosition(source, position);
        }
    }

    protected Optional<OpenALAudioData> get(OpenAL openAL, String asset) {
        if (!cache.containsKey(asset)) {
            ReadSource resource = engine.files().get(asset);
            if (resource.exists()) {
                try (ReadableAudioStream stream = AudioStream
                        .create(resource)) {
                    cache.put(asset, new OpenALAudioData(stream, openAL));
                } catch (IOException e) {
                    LOGGER.warn("Failed to get audio data", e);
                }
            }
        }
        return Optional.ofNullable(cache.get(asset));
    }

    protected int takeSource(OpenAL openAL) {
        int source = freeSource(openAL, true, true);
        if (source == -1) {
            return -1;
        }
        openAL.setBuffer(source, 0);
        return source;
    }

    protected void releaseSource(OpenAL openAL, int source) {
        openAL.stop(source);
        openAL.setBuffer(source, 0);
        int queued = openAL.getBuffersQueued(source);
        while (queued-- > 0) {
            openAL.unqueue(source);
        }
        for (int i = 0; i < sources.length; i++) {
            if (sources[i] == -1) {
                sources[i] = source;
                return;
            }
        }
        assert false;
    }

    protected void removeBufferFromSources(OpenAL openAL, int buffer) {
        for (int source : sources) {
            if (source != -1 && openAL.getBuffer(source) == buffer) {
                openAL.stop(source);
                openAL.setBuffer(source, 0);
            }
        }
    }

    private void queue(Consumer<OpenAL> consumer) {
        queue.add(consumer);
        joiner.wake();
    }

    private int freeSource(OpenAL openAL, boolean force, boolean take) {
        Random random = ThreadLocalRandom.current();
        int offset = random.nextInt(sources.length);
        for (int i = 0; i < sources.length; i++) {
            int j = (i + offset) % sources.length;
            int source = sources[j];
            if (source != -1 && openAL.isStopped(source)) {
                if (take) {
                    sources[j] = -1;
                }
                return source;
            }
        }
        if (force) {
            for (int i = 0; i < sources.length; i++) {
                int j = (i + offset) % sources.length;
                int source = sources[j];
                if (source != -1) {
                    openAL.stop(source);
                    if (take) {
                        sources[j] = -1;
                    }
                    return source;
                }
            }
        }
        return -1;
    }

    private boolean isSoundPlaying(OpenAL openAL) {
        for (int source : sources) {
            if (source == -1 || !openAL.isStopped(source)) {
                return true;
            }
        }
        return false;
    }
}
