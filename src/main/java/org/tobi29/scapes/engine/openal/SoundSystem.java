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
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource;
import org.tobi29.scapes.engine.utils.io.filesystem.Resource;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SoundSystem {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(SoundSystem.class);
    private final Map<String, AudioData> cache = new ConcurrentHashMap<>();
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final Set<Audio> audios =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final int[] sources;
    private final ScapesEngine engine;
    private final OpenAL openAL;
    private Vector3 origin = Vector3d.ZERO, listenerPosition = Vector3d.ZERO,
            listenerOrientation = Vector3d.ZERO, listenerVelocity =
            Vector3d.ZERO;

    public SoundSystem(ScapesEngine engine, OpenAL openAL) {
        this.engine = engine;
        this.openAL = openAL;
        openAL.create();
        sources = new int[64];
        for (int i = 0; i < sources.length; i++) {
            sources[i] = openAL.createSource();
        }
        openAL.checkError("Initializing");
    }

    public void dispose() {
        try {
            audios.forEach(audio -> {
                audio.stop(this, openAL);
            });
            audios.clear();
            for (int i = 0; i < sources.length; i++) {
                int source = sources[i];
                openAL.stop(source);
                openAL.setBuffer(source, 0);
                openAL.deleteSource(source);
                sources[i] = -1;
            }
            for (AudioData audioData : cache.values()) {
                audioData.dispose(this, openAL);
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

    public float volume(String channel) {
        return (float) engine.config().volume(channel);
    }

    public boolean isPlaying(String channel) {
        return audios.stream().filter(audio -> audio.isPlaying(channel))
                .findAny().isPresent();
    }

    public void playMusic(String asset, String channel, float pitch, float gain,
            boolean state) {
        playMusic(engine.files().get(asset), channel, pitch, gain, state);
    }

    public void playMusic(String asset, String channel, float pitch, float gain,
            Vector3 position, Vector3 velocity, boolean state) {
        playMusic(engine.files().get(asset), channel, pitch, gain, position,
                velocity, state);
    }

    public void playMusic(ReadSource asset, String channel, float pitch,
            float gain, boolean state) {
        queue.add(() -> audios
                .add(new StreamAudio(asset, channel, Vector3d.ZERO,
                        Vector3d.ZERO, pitch, gain, state, false)));
    }

    public void playMusic(ReadSource asset, String channel, float pitch,
            float gain, Vector3 position, Vector3 velocity, boolean state) {
        queue.add(() -> audios
                .add(new StreamAudio(asset, channel, position, velocity, pitch,
                        gain, state, true)));
    }

    public void stop(String channel) {
        queue.add(() -> {
            Collection<Audio> stopped =
                    audios.stream().filter(audio -> audio.isPlaying(channel))
                            .collect(Collectors.toList());
            stopped.forEach(audio -> audio.stop(this, openAL));
            audios.removeAll(stopped);
        });
    }

    public void playSound(String asset, String channel, float pitch,
            float gain) {
        queue.add(() -> audios
                .add(new EffectAudio(asset, channel, Vector3d.ZERO,
                        Vector3d.ZERO, pitch, gain, false)));
    }

    public void playSound(String asset, String channel, Vector3 position,
            Vector3 velocity, float pitch, float gain) {
        queue.add(() -> audios
                .add(new EffectAudio(asset, channel, position, velocity, pitch,
                        gain, true)));
    }

    public StaticAudio playStaticAudio(String asset, String channel,
            float pitch, float gain) {
        StaticAudio staticAudio = new StaticAudio(asset, channel, pitch, gain);
        queue.add(() -> audios.add(staticAudio));
        return staticAudio;
    }

    public void poll(double delta) {
        try {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
            boolean lagSilence =
                    delta > 1.0; // Prevent accumulating sounds on lag spikes
            audios.removeAll(audios.stream().filter(audio -> audio
                    .poll(this, openAL, listenerPosition, delta, lagSilence))
                    .collect(Collectors.toList()));
            openAL.checkError("Sound-Effects");
            openAL.setListener(listenerPosition.minus(origin),
                    listenerOrientation, listenerVelocity);
            if (!isSoundPlaying()) {
                origin = listenerPosition;
            }
            openAL.checkError("Updating-System");
        } catch (SoundException e) {
            LOGGER.warn("Error polling sound-system: {}", e.toString());
        }
    }

    protected void playSound(int buffer, float pitch, float gain,
            Vector3 position, Vector3 velocity, boolean state,
            boolean hasPosition) {
        int source = freeSource(false, false);
        if (source == -1) {
            return;
        }
        playSound(buffer, source, pitch, gain, position, velocity, state,
                hasPosition);
    }

    protected void playSound(int buffer, int source, float pitch, float gain,
            Vector3 position, Vector3 velocity, boolean state,
            boolean hasPosition) {
        openAL.stop(source);
        openAL.setBuffer(source, buffer);
        openAL.setGain(source, gain);
        openAL.setPitch(source, pitch);
        openAL.setLooping(source, state);
        position(source, position, hasPosition);
        openAL.setVelocity(source, velocity);
        openAL.play(source);
    }

    protected void position(int source, Vector3 position, boolean hasPosition) {
        openAL.setRelative(source, !hasPosition);
        if (hasPosition) {
            openAL.setPosition(source, position.minus(origin));
        } else {
            openAL.setPosition(source, position);
        }
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

    protected int takeSource() {
        int source = freeSource(true, true);
        if (source == -1) {
            return -1;
        }
        openAL.setBuffer(source, 0);
        return source;
    }

    protected void releaseSource(int source) {
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

    protected void removeBufferFromSources(int buffer) {
        Arrays.stream(sources).filter(source -> source != -1 &&
                openAL.getBuffer(source) == buffer).forEach(source -> {
            openAL.stop(source);
            openAL.setBuffer(source, 0);
        });
    }

    private int freeSource(boolean force, boolean take) {
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

    private boolean isSoundPlaying() {
        for (int source : sources) {
            if (source == -1 || !openAL.isStopped(source)) {
                return true;
            }
        }
        return false;
    }
}
