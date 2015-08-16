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
import java.nio.channels.WritableByteChannel;

public class BufferedWriteChannelStream implements WritableByteStream {
    private final WritableByteChannel channel;
    private final ByteBuffer buffer = BufferCreator.bytes(8192);

    public BufferedWriteChannelStream(WritableByteChannel channel) {
        this.channel = channel;
    }

    @Override
    public WritableByteStream put(ByteBuffer buffer, int len)
            throws IOException {
        int limit = buffer.limit();
        buffer.limit(buffer.position() + len);
        if (ensure(len)) {
            this.buffer.put(buffer);
        } else {
            flush();
            write(buffer);
        }
        buffer.limit(limit);
        return this;
    }

    @Override
    public WritableByteStream put(int b) throws IOException {
        ensure(1);
        buffer.put((byte) b);
        return this;
    }

    @Override
    public WritableByteStream putShort(int value) throws IOException {
        ensure(2);
        buffer.putShort((short) value);
        return this;
    }

    @Override
    public WritableByteStream putInt(int value) throws IOException {
        ensure(4);
        buffer.putInt(value);
        return this;
    }

    @Override
    public WritableByteStream putLong(long value) throws IOException {
        ensure(8);
        buffer.putLong(value);
        return this;
    }

    @Override
    public WritableByteStream putFloat(float value) throws IOException {
        ensure(4);
        buffer.putFloat(value);
        return this;
    }

    @Override
    public WritableByteStream putDouble(double value) throws IOException {
        ensure(8);
        buffer.putDouble(value);
        return this;
    }

    public void flush() throws IOException {
        buffer.flip();
        write(buffer);
        buffer.clear();
    }

    private boolean ensure(int len) throws IOException {
        if (len > buffer.capacity()) {
            return false;
        }
        if (buffer.remaining() < len) {
            flush();
        }
        return true;
    }

    private void write(ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            int length = channel.write(buffer);
            if (length == -1) {
                throw new IOException("End of stream");
            }
        }
    }
}
