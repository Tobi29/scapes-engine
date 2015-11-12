package org.tobi29.scapes.engine.openal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.openal.codec.AudioStream;
import org.tobi29.scapes.engine.openal.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.BufferCreatorNative;
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class StreamAudio implements Audio {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(StreamAudio.class);
    private final ReadSource asset;
    private final String channel;
    private final ByteBuffer pcmBuffer;
    private final FloatBuffer readBuffer;
    private final Vector3 pos, velocity;
    private final float pitch, gain;
    private final boolean state, hasPosition;
    private final int[] queuedBuffers = new int[3];
    private int source = -1;
    private ReadableAudioStream stream;
    private float gainAL;

    public StreamAudio(ReadSource asset, String channel, Vector3 pos,
            Vector3 velocity, float pitch, float gain, boolean state,
            boolean hasPosition) {
        this.asset = asset;
        this.channel = channel;
        this.pos = pos;
        this.velocity = velocity;
        this.pitch = pitch;
        this.gain = gain;
        this.state = state;
        this.hasPosition = hasPosition;
        readBuffer = BufferCreator.floats(4096 << 2);
        pcmBuffer = BufferCreatorNative.bytes(readBuffer.capacity() << 1);
    }

    @Override
    public boolean poll(SoundSystem sounds, OpenAL openAL,
            Vector3 listenerPosition, double speedFactor, boolean lagSilence) {
        if (source == -1) {
            source = sounds.takeSource();
            if (source == -1) {
                return true;
            }
            for (int i = 0; i < queuedBuffers.length; i++) {
                queuedBuffers[i] = openAL.createBuffer();
            }
            try {
                stream = AudioStream.create(asset);
                for (int queue : queuedBuffers) {
                    stream(openAL, queue);
                    openAL.queue(source, queue);
                }
                openAL.setPitch(source, pitch);
                gainAL = gain * sounds.volume(channel);
                openAL.setGain(source, gainAL);
                openAL.setLooping(source, false);
                sounds.position(source, pos, hasPosition);
                openAL.setVelocity(source, velocity);
                openAL.play(source);
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
                int finished = openAL.getBuffersProcessed(source);
                while (finished-- > 0) {
                    int unqueued = openAL.unqueue(source);
                    if (!stream(openAL, unqueued)) {
                        stream.close();
                        if (state) {
                            stream = AudioStream.create(asset);
                        } else {
                            stream = null;
                        }
                    }
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
    public void stop(SoundSystem sounds, OpenAL openAL) {
        if (source != -1) {
            sounds.releaseSource(source);
            Arrays.stream(queuedBuffers).forEach(openAL::deleteBuffer);
            source = -1;
        }
        if (stream != null) {
            stream.close();
            stream = null;
        }
    }

    private boolean stream(OpenAL openAL, int buffer) throws IOException {
        boolean valid = true;
        while (readBuffer.hasRemaining() && valid) {
            valid = stream.getSome(readBuffer);
        }
        readBuffer.flip();
        store(openAL, buffer);
        stream.frame();
        return valid;
    }

    private void store(OpenAL openAL, int buffer) {
        pcmBuffer.clear();
        while (readBuffer.hasRemaining()) {
            pcmBuffer.putShort(PCMUtil.toInt32(readBuffer.get()));
        }
        readBuffer.clear();
        pcmBuffer.flip();
        openAL.storeBuffer(buffer,
                stream.channels() > 1 ? AudioFormat.STEREO : AudioFormat.MONO,
                pcmBuffer, stream.rate());
    }
}
