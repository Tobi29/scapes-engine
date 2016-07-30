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
// Based on: http://www.labbookpages.co.uk/audio/wavFiles.html
package org.tobi29.scapes.engine.utils.codec.wav;

import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.codec.AudioBuffer;
import org.tobi29.scapes.engine.utils.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.io.ChannelUtil;
import org.tobi29.scapes.engine.utils.io.IOBooleanSupplier;
import org.tobi29.scapes.engine.utils.io.IOSupplier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.ReadableByteChannel;

public class WAVReadStream implements ReadableAudioStream {
    private static final int BUFFER_SIZE = 4096;
    private static final int FMT_CHUNK_ID = 0x20746D66;
    private static final int DATA_CHUNK_ID = 0x61746164;
    private static final int RIFF_CHUNK_ID = 0x46464952;
    private static final int RIFF_TYPE_ID = 0x45564157;
    private final ReadableByteChannel channel;
    private final ByteBuffer buffer =
            BufferCreator.bytes(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
    private int channels, rate, align, bits, bytes;
    private float offset, scale;
    private IOBooleanSupplier state;
    private boolean eos;

    public WAVReadStream(ReadableByteChannel channel) {
        this.channel = channel;
        buffer.clear().limit(12);
        state = this::init1;
    }

    private static Chunk chunk(ByteBuffer buffer) {
        int chunkID = buffer.getInt();
        int chunkSize = buffer.getInt();
        int numChunkBytes = chunkSize % 2 == 0 ? chunkSize : chunkSize + 1;
        return new Chunk(chunkID, chunkSize, numChunkBytes);
    }

    private boolean skip(long skip, IOSupplier<IOBooleanSupplier> next)
            throws IOException {
        long newSkip = ChannelUtil.skip(channel, skip);
        if (newSkip == 0) {
            state = next.get();
            return true;
        }
        state = () -> skip(newSkip, next);
        return false;
    }

    private boolean init1() throws IOException {
        if (channel.read(buffer) == -1) {
            throw new IOException("End of stream during header");
        }
        if (!buffer.hasRemaining()) {
            buffer.flip();
            Chunk header = chunk(buffer);
            long riffTypeID = buffer.getInt();
            if (header.id != RIFF_CHUNK_ID) {
                throw new IOException(
                        "Invalid Wav Header data, incorrect riff chunk ID");
            }
            if (riffTypeID != RIFF_TYPE_ID) {
                throw new IOException(
                        "Invalid Wav Header data, incorrect riff type ID");
            }
            buffer.clear().limit(8);
            state = this::init2;
            return true;
        }
        return false;
    }

    private boolean init2() throws IOException {
        if (channel.read(buffer) == -1) {
            throw new IOException("End of stream during header");
        }
        if (!buffer.hasRemaining()) {
            buffer.flip();
            Chunk chunk = chunk(buffer);
            switch (chunk.id) {
                case FMT_CHUNK_ID:
                    buffer.clear().limit(16);
                    state = () -> init3(chunk);
                    return true;
                default:
                    state = () -> skip(chunk.bytes, () -> {
                        buffer.clear().limit(8);
                        return this::init2;
                    });
                    return true;
            }
        }
        return false;
    }

    private boolean init3(Chunk chunk) throws IOException {
        if (channel.read(buffer) == -1) {
            throw new IOException("End of stream during header");
        }
        if (!buffer.hasRemaining()) {
            buffer.flip();
            int compressionCode = buffer.getShort();
            if (compressionCode != 1) {
                throw new IOException("Compression Code " + compressionCode +
                        " not supported");
            }
            channels = buffer.getShort();
            rate = buffer.getInt();
            buffer.position(12);
            align = buffer.getShort();
            bits = buffer.getShort();
            int bytes = bits + 7 >> 3;
            if (bytes * channels != align) {
                throw new IOException(
                        "Block Align does not agree with bytes required for validBits and number of channels");
            }
            sanityCheck();
            state = () -> skip(chunk.bytes - 16, () -> {
                buffer.clear().limit(8);
                return this::init4;
            });
            return true;
        }
        return false;
    }

    private boolean init4() throws IOException {
        if (channel.read(buffer) == -1) {
            throw new IOException("End of stream during header");
        }
        if (!buffer.hasRemaining()) {
            buffer.flip();
            Chunk chunk = chunk(buffer);
            switch (chunk.id) {
                case DATA_CHUNK_ID:
                    data(chunk);
                    bytes = bits + 7 >> 3;
                    if (bits > 8) {
                        offset = 0.0f;
                        scale = 1 << bits - 1;
                    } else {
                        offset = -1.0f;
                        scale = 0.5f * ((1 << bits) - 1);
                    }
                    state = null;
                    return false;
                default:
                    state = () -> skip(chunk.bytes, () -> {
                        buffer.clear().limit(8);
                        return this::init4;
                    });
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean get(AudioBuffer buffer) throws IOException {
        if (state != null) {
            while (state.get()) {
            }
            if (state != null) {
                return !eos;
            }
        }
        FloatBuffer pcmBuffer = buffer.buffer(channels, rate);
        while (pcmBuffer.hasRemaining() && !eos) {
            long value = 0;
            for (int b = 0; b < bytes; b++) {
                if (!this.buffer.hasRemaining()) {
                    this.buffer.clear();
                    if (channel.read(this.buffer) == -1) {
                        eos = true;
                        break;
                    }
                    this.buffer.flip();
                }
                int v = this.buffer.get();
                if (b < bytes - 1 || bytes == 1) {
                    v &= 0xFF;
                }
                value += v << (b << 3);
            }
            if (!eos) {
                pcmBuffer.put(offset + value / scale);
            }
        }
        buffer.done();
        return !eos;
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
        }
    }

    private void data(Chunk chunk) throws IOException {
        if (chunk.size % align != 0) {
            throw new IOException(
                    "Data Chunk size is not multiple of Block Align");
        }
    }

    private void sanityCheck() throws IOException {
        if (channels <= 0) {
            throw new IOException(
                    "Number of channels specified in header is equal to zero");
        }
        if (align == 0) {
            throw new IOException(
                    "Block Align specified in header is equal to zero");
        }
        if (bits < 2) {
            throw new IOException(
                    "Valid Bits specified in header is less than 2");
        }
        if (bits > 64) {
            throw new IOException(
                    "Valid Bits specified in header is greater than 64, this is greater than a long can hold");
        }
    }

    private static final class Chunk {
        private final int id, size, bytes;

        private Chunk(int id, int size, int bytes) {
            this.id = id;
            this.size = size;
            this.bytes = bytes;
        }
    }
}
