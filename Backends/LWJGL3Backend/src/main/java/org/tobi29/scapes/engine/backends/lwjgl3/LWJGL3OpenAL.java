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

package org.tobi29.scapes.engine.backends.lwjgl3;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.openal.AudioFormat;
import org.tobi29.scapes.engine.openal.OpenAL;
import org.tobi29.scapes.engine.openal.SoundException;
import org.tobi29.scapes.engine.utils.BufferCreatorNative;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class LWJGL3OpenAL implements OpenAL {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(LWJGL3OpenAL.class);
    private final FloatBuffer listenerOrientation =
            BufferCreatorNative.floatsD(6);
    private ByteBuffer directBuffer = BufferCreatorNative.bytesD(4 << 10 << 10);
    private ALContext context;

    @Override
    public void checkError(String message) {
        int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            throw new SoundException(AL10.alGetString(error) + " in " +
                    message);
        }
    }

    @Override
    public void create() {
        context = ALContext.create();
        LOGGER.info("OpenAL: {} (Vendor: {}, Renderer: {})",
                AL10.alGetString(AL10.AL_VERSION),
                AL10.alGetString(AL10.AL_VENDOR),
                AL10.alGetString(AL10.AL_RENDERER));
        AL11.alSpeedOfSound(343.3f);
        listenerOrientation.clear();
        listenerOrientation
                .put(new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f});
        listenerOrientation.rewind();
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation);
        AL10.alListener3f(AL10.AL_POSITION, 0.0f, 0.0f, 0.0f);
        AL10.alListener3f(AL10.AL_VELOCITY, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void destroy() {
        AL.destroy(context);
    }

    @Override
    public void setListener(Vector3 position, Vector3 orientation,
            Vector3 velocity) {
        double cos =
                FastMath.cosTable(orientation.doubleX() * FastMath.DEG_2_RAD);
        float lookX = (float) (
                FastMath.cosTable(orientation.doubleZ() * FastMath.DEG_2_RAD) *
                        cos);
        float lookY = (float) (
                FastMath.sinTable(orientation.doubleZ() * FastMath.DEG_2_RAD) *
                        cos);
        float lookZ = (float) FastMath
                .sinTable(orientation.doubleX() * FastMath.DEG_2_RAD);
        listenerOrientation.put(lookX);
        listenerOrientation.put(lookY);
        listenerOrientation.put(lookZ);
        listenerOrientation.put(0);
        listenerOrientation.put(0);
        listenerOrientation.put(1);
        listenerOrientation.rewind();
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation);
        AL10.alListener3f(AL10.AL_POSITION, position.floatX(),
                position.floatY(), position.floatZ());
        AL10.alListener3f(AL10.AL_VELOCITY, velocity.floatX(),
                velocity.floatY(), velocity.floatZ());
    }

    @Override
    public int createSource() {
        return AL10.alGenSources();
    }

    @Override
    public void deleteSource(int id) {
        AL10.alDeleteSources(id);
    }

    @Override
    public void setBuffer(int id, int value) {
        AL10.alSourcei(id, AL10.AL_BUFFER, value);
    }

    @Override
    public void setPitch(int id, float value) {
        AL10.alSourcef(id, AL10.AL_PITCH, value);
    }

    @Override
    public void setGain(int id, float value) {
        AL10.alSourcef(id, AL10.AL_GAIN, value);
    }

    @Override
    public void setLooping(int id, boolean value) {
        AL10.alSourcei(id, AL10.AL_LOOPING,
                value ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    @Override
    public void setRelative(int id, boolean value) {
        AL10.alSourcei(id, AL10.AL_SOURCE_RELATIVE,
                value ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    @Override
    public void setPosition(int id, Vector3 pos) {
        AL10.alSource3f(id, AL10.AL_POSITION, pos.floatX(), pos.floatY(),
                pos.floatZ());
    }

    @Override
    public void setVelocity(int id, Vector3 vel) {
        AL10.alSource3f(id, AL10.AL_VELOCITY, vel.floatX(), vel.floatY(),
                vel.floatZ());
    }

    @Override
    public void play(int id) {
        AL10.alSourcePlay(id);
    }

    @Override
    public void stop(int id) {
        AL10.alSourceStop(id);
    }

    @Override
    public int createBuffer() {
        return AL10.alGenBuffers();
    }

    @Override
    public void deleteBuffer(int id) {
        AL10.alDeleteBuffers(id);
    }

    @Override
    public void storeBuffer(int id, AudioFormat format, ByteBuffer buffer,
            int rate) {
        switch (format) {
            case MONO:
                AL10.alBufferData(id, AL10.AL_FORMAT_MONO16, direct(buffer),
                        rate);
                break;
            case STEREO:
                AL10.alBufferData(id, AL10.AL_FORMAT_STEREO16, direct(buffer),
                        rate);
                break;
        }
    }

    @Override
    public boolean isPlaying(int id) {
        return AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    @Override
    public boolean isStopped(int id) {
        int state = AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE);
        return state != AL10.AL_PLAYING && state != AL10.AL_PAUSED;
    }

    @Override
    public int getBuffersQueued(int id) {
        return AL10.alGetSourcei(id, AL10.AL_BUFFERS_QUEUED);
    }

    @Override
    public int getBuffersPrecessed(int id) {
        return AL10.alGetSourcei(id, AL10.AL_BUFFERS_PROCESSED);
    }

    @Override
    public void queue(int id, int buffer) {
        AL10.alSourceQueueBuffers(id, buffer);
    }

    @Override
    public int unqueue() {
        return AL10.alSourceUnqueueBuffers(1);
    }

    @Override
    public int getBuffer(int id) {
        return AL10.alGetSourcei(id, AL10.AL_BUFFER);
    }

    @SuppressWarnings("ReturnOfNull")
    private ByteBuffer direct(ByteBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        direct(buffer.remaining());
        directBuffer.put(buffer);
        buffer.flip();
        directBuffer.flip();
        return directBuffer;
    }

    private ByteBuffer direct(int size) {
        directBuffer.clear();
        if (directBuffer.remaining() < size) {
            int capacity = (size >> 10) + 1 << 10;
            LOGGER.debug("Resizing direct buffer: {} ({})", capacity, size);
            directBuffer = BufferCreatorNative.bytesD(capacity);
        }
        directBuffer.limit(size);
        return directBuffer;
    }
}
