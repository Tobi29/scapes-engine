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

package org.tobi29.scapes.engine.utils.io;

import org.tobi29.scapes.engine.utils.BufferCreator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class BufferedReadChannelStream implements ReadableByteStream {
    private final ReadableByteChannel channel;
    private final ByteBuffer buffer = BufferCreator.bytes(8192);

    public BufferedReadChannelStream(ReadableByteChannel channel) {
        this.channel = channel;
        buffer.limit(0);
    }

    @Override
    public int remaining() {
        return buffer.remaining();
    }

    @Override
    public ReadableByteStream get(ByteBuffer buffer, int len)
            throws IOException {
        if (ensure(len)) {
            int limit = this.buffer.limit();
            this.buffer.limit(this.buffer.position() + len);
            buffer.put(this.buffer);
            this.buffer.limit(limit);
        } else {
            int limit = buffer.limit();
            buffer.limit(buffer.position() + len);
            buffer.put(this.buffer);
            if (!read(buffer)) {
                throw new IOException("End of stream");
            }
            buffer.limit(limit);
        }
        return this;
    }

    @Override
    public boolean getSome(ByteBuffer buffer, int len) throws IOException {
        if (this.buffer.remaining() >= len) {
            int limit = this.buffer.limit();
            this.buffer.limit(this.buffer.position() + len);
            buffer.put(this.buffer);
            this.buffer.limit(limit);
            return true;
        } else {
            int limit = buffer.limit();
            buffer.limit(buffer.position() + len);
            buffer.put(this.buffer);
            boolean available = read(buffer);
            buffer.limit(limit);
            return available;
        }
    }

    @Override
    public byte get() throws IOException {
        ensure(1);
        return buffer.get();
    }

    @Override
    public short getShort() throws IOException {
        ensure(2);
        return buffer.getShort();
    }

    @Override
    public int getInt() throws IOException {
        ensure(4);
        return buffer.getInt();
    }

    @Override
    public long getLong() throws IOException {
        ensure(8);
        return buffer.getLong();
    }

    @Override
    public float getFloat() throws IOException {
        ensure(4);
        return buffer.getFloat();
    }

    @Override
    public double getDouble() throws IOException {
        ensure(8);
        return buffer.getDouble();
    }

    private boolean ensure(int len) throws IOException {
        if (len > buffer.capacity()) {
            return false;
        }
        if (buffer.remaining() < len) {
            ByteBuffer oldBuffer = buffer.duplicate();
            buffer.clear();
            buffer.put(oldBuffer);
            if (!read(buffer)) {
                if (buffer.position() < len) {
                    throw new IOException("End of stream");
                }
            }
            buffer.flip();
        }
        return true;
    }

    private boolean read(ByteBuffer buffer) {
        try {
            while (buffer.hasRemaining()) {
                int length = channel.read(buffer);
                if (length == -1) {
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
