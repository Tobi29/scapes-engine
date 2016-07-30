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
