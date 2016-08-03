/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils.codec;

import java8.util.function.IntFunction;
import org.tobi29.scapes.engine.utils.BufferCreator;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class AudioBuffer {
    private final int size;
    private final IntFunction<ByteBuffer> pcmBufferSupplier;
    private FloatBuffer buffer;
    private ByteBuffer pcmBuffer;
    private int channels, rate;
    private boolean empty = true, done;

    public AudioBuffer(int size, IntFunction<ByteBuffer> pcmBufferSupplier) {
        this.size = size;
        this.pcmBufferSupplier = pcmBufferSupplier;
    }

    public FloatBuffer buffer(int channels, int rate) {
        if (empty) {
            empty = false;
            int capacity = size * channels;
            if (buffer == null || buffer.capacity() != capacity) {
                buffer = BufferCreator.floats(capacity);
            }
            this.channels = channels;
            this.rate = rate;
        } else {
            assert channels == this.channels && rate == this.rate;
        }
        return buffer;
    }

    public void done() {
        assert buffer.position() % channels == 0;
        buffer.flip();
        done = true;
    }

    public boolean isDone() {
        return done;
    }

    public int channels() {
        return channels;
    }

    public int rate() {
        return rate;
    }

    public void clear() {
        assert done;
        buffer.clear();
        done = false;
    }

    public ByteBuffer toPCM16() {
        int capacity = buffer.remaining() << 1;
        if (pcmBuffer == null || pcmBuffer.capacity() != capacity) {
            pcmBuffer = pcmBufferSupplier.apply(capacity);
        } else {
            pcmBuffer.clear();
        }
        while (buffer.hasRemaining()) {
            pcmBuffer.putShort(PCMUtil.toInt16(buffer.get()));
        }
        pcmBuffer.flip();
        return pcmBuffer;
    }

    public void toPCM16(PCM16Consumer consumer) {
        while (buffer.hasRemaining()) {
            consumer.append(PCMUtil.toInt16(buffer.get()));
        }
    }

    public interface PCM16Consumer {
        void append(short value);
    }
}
