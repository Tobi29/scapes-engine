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

package org.tobi29.scapes.engine.openal.codec.wav;

import org.tobi29.scapes.engine.openal.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.math.FastMath;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class WAVReadStream implements ReadableAudioStream {
    private static final int BUFFER_SIZE = 4096;
    private final Channel channel;
    private final AudioInputStream stream;
    private final int channels, rate, readSize;
    private final byte[] catchBuffer;
    private final ByteBuffer catchWrap;
    private final int bytes;

    public WAVReadStream(ReadableByteChannel channel) throws IOException {
        this.channel = channel;
        try {
            stream = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(Channels.newInputStream(channel)));
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
        AudioFormat audioFormat = stream.getFormat();
        channels = audioFormat.getChannels();
        rate = (int) audioFormat.getSampleRate();
        int bits = audioFormat.getSampleSizeInBits();
        switch (bits) {
            case 8:
                bytes = 1;
                break;
            case 16:
                bytes = 2;
                break;
            case 24:
                bytes = 3;
                break;
            case 32:
                bytes = 4;
                break;
            default:
                throw new IOException("Unsupported sample size: " + bits);
        }
        readSize = BUFFER_SIZE * channels;
        catchBuffer = new byte[readSize * bytes];
        catchWrap = ByteBuffer.wrap(catchBuffer).order(ByteOrder.nativeOrder());
    }

    @Override
    public int channels() {
        return channels;
    }

    @Override
    public int rate() {
        return rate;
    }

    @Override
    public boolean getSome(FloatBuffer buffer, int len) throws IOException {
        int limit = buffer.limit();
        buffer.limit(buffer.position() + len);
        boolean valid = true;
        while (buffer.hasRemaining() && valid) {
            valid = decodeBatch(buffer);
        }
        buffer.limit(limit);
        return valid;
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
        }
    }

    private boolean decodeBatch(FloatBuffer buffer) throws IOException {
        if (!catchWrap.hasRemaining()) {
            catchBatch();
        }
        int len =
                FastMath.min(buffer.remaining() * bytes, catchWrap.remaining());
        int limit = catchWrap.limit();
        catchWrap.limit(catchWrap.position() + len);
        switch (bytes) {
            case 1:
                while (catchWrap.hasRemaining()) {
                    int sample = catchWrap.get();
                    if (sample < 0) {
                        sample += 256;
                    }
                    sample -= 128;
                    buffer.put((float) sample / Byte.MAX_VALUE);
                }
                break;
            case 2:
                while (catchWrap.hasRemaining()) {
                    buffer.put((float) catchWrap.getShort() / Short.MAX_VALUE);
                }
                break;
            case 3:
                if (catchWrap.order() == ByteOrder.BIG_ENDIAN) {
                    while (catchWrap.hasRemaining()) {
                        int sample = catchWrap.get() << 16;
                        int sample1 = catchWrap.get() << 8;
                        if (sample1 < 0) {
                            sample1 += 0x10000;
                        }
                        int sample2 = catchWrap.get();
                        if (sample2 < 0) {
                            sample2 += 0x100;
                        }
                        sample += sample1;
                        sample += sample2;
                        buffer.put((float) sample / Integer.MAX_VALUE);
                    }
                } else {
                    while (catchWrap.hasRemaining()) {
                        int sample1 = catchWrap.get();
                        if (sample1 < 0) {
                            sample1 += 0x100;
                        }
                        int sample2 = catchWrap.get() << 8;
                        if (sample2 < 0) {
                            sample2 += 0x10000;
                        }
                        int sample = catchWrap.get() << 16;
                        sample += sample1;
                        sample += sample2;
                        buffer.put((float) sample / (1 << 23));
                    }
                }
                break;
            case 4:
                while (catchWrap.hasRemaining()) {
                    buffer.put((float) catchWrap.getInt() / Integer.MAX_VALUE);
                }
                break;
        }
        catchWrap.limit(limit);
        return true;
    }

    private boolean catchBatch() throws IOException {
        catchWrap.clear();
        int read = stream.read(catchBuffer, 0, readSize);
        if (read < 0) {
            return false;
        }
        catchWrap.limit(read);
        return true;
    }
}
