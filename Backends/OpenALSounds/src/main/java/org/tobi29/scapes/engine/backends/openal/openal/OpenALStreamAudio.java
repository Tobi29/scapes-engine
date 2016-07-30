package org.tobi29.scapes.engine.backends.openal.openal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.sound.AudioFormat;
import org.tobi29.scapes.engine.utils.codec.AudioBuffer;
import org.tobi29.scapes.engine.utils.codec.AudioStream;
import org.tobi29.scapes.engine.utils.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;

import java.io.IOException;

public class OpenALStreamAudio implements OpenALAudio {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(OpenALStreamAudio.class);
    private final ReadSource asset;
    private final String channel;
    private final AudioBuffer readBuffer;
    private final Vector3 pos, velocity;
    private final float pitch, gain;
    private final boolean state, hasPosition;
    private int source = -1, queued;
    private ReadableAudioStream stream;
    private float gainAL;

    public OpenALStreamAudio(ScapesEngine engine, ReadSource asset,
            String channel, Vector3 pos, Vector3 velocity, float pitch,
            float gain, boolean state, boolean hasPosition) {
        this.asset = asset;
        this.channel = channel;
        this.pos = pos;
        this.velocity = velocity;
        this.pitch = pitch;
        this.gain = gain;
        this.state = state;
        this.hasPosition = hasPosition;
        readBuffer = new AudioBuffer(4096, engine::allocate);
    }

    @Override
    public boolean poll(OpenALSoundSystem sounds, OpenAL openAL,
            Vector3 listenerPosition, double delta) {
        if (source == -1) {
            source = sounds.takeSource(openAL);
            if (source == -1) {
                return true;
            }
            openAL.setPitch(source, pitch);
            gainAL = gain * sounds.volume(channel);
            openAL.setGain(source, gainAL);
            openAL.setLooping(source, false);
            sounds.position(openAL, source, pos, hasPosition);
            openAL.setVelocity(source, velocity);
            try {
                stream = AudioStream.create(asset);
            } catch (IOException e) {
                LOGGER.warn("Failed to play music: {}", e.toString());
                stop(sounds, openAL);
                return true;
            }
        } else if (stream != null) {
            try {
                float gainAL = gain * sounds.volume(channel);
                if (FastMath.abs(gainAL - this.gainAL) > 0.001f) {
                    openAL.setGain(source, gainAL);
                    this.gainAL = gainAL;
                }
                while (queued < 3) {
                    stream();
                    if (!readBuffer.isDone()) {
                        break;
                    }
                    int buffer = openAL.createBuffer();
                    store(openAL, buffer);
                    openAL.queue(source, buffer);
                    queued++;
                }
                int finished = openAL.getBuffersProcessed(source);
                while (finished-- > 0) {
                    stream();
                    if (!readBuffer.isDone()) {
                        break;
                    }
                    int unqueued = openAL.unqueue(source);
                    store(openAL, unqueued);
                    openAL.queue(source, unqueued);
                }
                if (!openAL.isPlaying(source)) {
                    openAL.play(source);
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to stream music: {}", e.toString());
                stop(sounds, openAL);
                return true;
            }
        } else if (!openAL.isPlaying(source)) {
            stop(sounds, openAL);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPlaying(String channel) {
        return this.channel.startsWith(channel);
    }

    @Override
    public void stop(OpenALSoundSystem sounds, OpenAL openAL) {
        if (source != -1) {
            openAL.stop(source);
            int queued = openAL.getBuffersQueued(source);
            while (queued-- > 0) {
                openAL.deleteBuffer(openAL.unqueue(source));
                this.queued--;
            }
            sounds.releaseSource(openAL, source);
            source = -1;
            assert this.queued == 0;
        }
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                LOGGER.warn("Failed to stop music stream: {}", e.toString());
            }
            stream = null;
        }
    }

    private void stream() throws IOException {
        if (stream == null) {
            return;
        }
        if (!stream.get(readBuffer)) {
            stream.close();
            stream = null;
            if (state) {
                stream = AudioStream.create(asset);
            }
        }
    }

    private void store(OpenAL openAL, int buffer) {
        openAL.storeBuffer(buffer,
                readBuffer.channels() > 1 ? AudioFormat.STEREO :
                        AudioFormat.MONO, readBuffer.toPCM16(),
                readBuffer.rate());
        readBuffer.clear();
    }
}
