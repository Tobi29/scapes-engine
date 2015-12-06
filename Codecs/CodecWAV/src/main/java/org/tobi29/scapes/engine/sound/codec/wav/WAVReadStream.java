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
package org.tobi29.scapes.engine.sound.codec.wav;

import java8.util.Optional;
import org.tobi29.scapes.engine.sound.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.io.BufferedReadChannelStream;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;

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
    private final ReadableByteStream stream;
    private final int channels, rate, bytes;
    private final float offset, scale;
    private final ByteBuffer buffer =
            BufferCreator.bytes(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN);

    public WAVReadStream(ReadableByteChannel channel) throws IOException {
        this.channel = channel;
        stream = new BufferedReadChannelStream(channel);
        Chunk header = nextChunk();
        buffer(4);
        long riffTypeID = buffer.getInt();
        if (header.id != RIFF_CHUNK_ID) {
            throw new IOException(
                    "Invalid Wav Header data, incorrect riff chunk ID");
        }
        if (riffTypeID != RIFF_TYPE_ID) {
            throw new IOException(
                    "Invalid Wav Header data, incorrect riff type ID");
        }
        Optional<Format> formatChunk = Optional.empty();
        boolean needsData = true;
        while (needsData) {
            Chunk chunk = nextChunk();
            switch (chunk.id) {
                case FMT_CHUNK_ID:
                    Format format = format(chunk);
                    sanityCheck(format);
                    formatChunk = Optional.of(format);
                    break;
                case DATA_CHUNK_ID:
                    data(chunk, formatChunk);
                    needsData = false;
                    break;
                default:
                    stream.skip(chunk.bytes);
                    break;
            }
        }
        Format format = formatChunk.get();
        rate = format.rate;
        channels = format.channels;
        bytes = format.bits + 7 >> 3;
        if (format.bits > 8) {
            offset = 0.0f;
            scale = 1 << format.bits - 1;
        } else {
            offset = -1.0f;
            scale = 0.5f * ((1 << format.bits) - 1);
        }
    }

    private Format format(Chunk chunk) throws IOException {
        buffer(16);
        int compressionCode = buffer.getShort();
        if (compressionCode != 1) {
            throw new IOException("Compression Code " + compressionCode +
                    " not supported");
        }
        int channels = buffer.getShort();
        int rate = buffer.getInt();
        buffer.position(12);
        int align = buffer.getShort();
        int bits = buffer.getShort();
        int bytes = bits + 7 >> 3;
        if (bytes * channels != align) {
            throw new IOException(
                    "Block Align does not agree with bytes required for validBits and number of channels");
        }
        long remaining = chunk.bytes - 16;
        if (remaining > 0) {
            stream.skip(remaining);
        }
        return new Format(channels, rate, bits, align);
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
    public void frame() {
    }

    @Override
    public boolean getSome(FloatBuffer buffer, int len) throws IOException {
        int limit = buffer.limit();
        buffer.limit(buffer.position() + len);
        boolean valid = true;
        while (buffer.hasRemaining() && valid) {
            long value = 0;
            for (int b = 0; b < bytes; b++) {
                if (!this.buffer.hasRemaining()) {
                    this.buffer.clear();
                    if (!stream.getSome(this.buffer)) {
                        valid = false;
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
            if (valid) {
                buffer.put(offset + value / scale);
            }
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

    private Chunk nextChunk() throws IOException {
        buffer(8);
        int chunkID = buffer.getInt();
        int chunkSize = buffer.getInt();
        int numChunkBytes = chunkSize % 2 == 0 ? chunkSize : chunkSize + 1;
        return new Chunk(chunkID, chunkSize, numChunkBytes);
    }

    private void data(Chunk chunk, Optional<Format> formatChunk)
            throws IOException {
        if (!formatChunk.isPresent()) {
            throw new IOException("Data Chunk before Format Chunk");
        }
        Format format = formatChunk.get();
        if (chunk.size % format.align != 0) {
            throw new IOException(
                    "Data Chunk size is not multiple of Block Align");
        }
    }

    private void sanityCheck(Format format) throws IOException {
        if (format.channels <= 0) {
            throw new IOException(
                    "Number of channels specified in header is equal to zero");
        }
        if (format.align == 0) {
            throw new IOException(
                    "Block Align specified in header is equal to zero");
        }
        if (format.bits < 2) {
            throw new IOException(
                    "Valid Bits specified in header is less than 2");
        }
        if (format.bits > 64) {
            throw new IOException(
                    "Valid Bits specified in header is greater than 64, this is greater than a long can hold");
        }
    }

    private void buffer(int length) throws IOException {
        buffer.clear().limit(length);
        stream.get(buffer);
        buffer.flip();
    }

    private static class Chunk {
        private final int id, size, bytes;

        private Chunk(int id, int size, int bytes) {
            this.id = id;
            this.size = size;
            this.bytes = bytes;
        }
    }

    private static class Format {
        private final int channels, rate, bits, align;

        private Format(int channels, int rate, int bits, int align) {
            this.channels = channels;
            this.rate = rate;
            this.bits = bits;
            this.align = align;
        }
    }
}
