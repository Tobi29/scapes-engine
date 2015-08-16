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

import org.tobi29.scapes.engine.utils.math.FastMath;

import java.io.IOException;
import java.nio.ByteBuffer;

public class LimitedBufferStream implements ReadableByteStream {
    private final ReadableByteStream stream;
    private int remaining;

    public LimitedBufferStream(ReadableByteStream stream, int remaining) {
        this.stream = stream;
        this.remaining = remaining;
    }

    @Override
    public int remaining() {
        return remaining;
    }

    @Override
    public ReadableByteStream get(ByteBuffer buffer, int len)
            throws IOException {
        check(len);
        return stream.get(buffer, len);
    }

    @Override
    public boolean getSome(ByteBuffer buffer, int len) throws IOException {
        len = FastMath.min(len, remaining);
        remaining -= len;
        return stream.getSome(buffer, len) && remaining > 0;
    }

    @Override
    public byte get() throws IOException {
        check(1);
        return stream.get();
    }

    @Override
    public short getShort() throws IOException {
        check(2);
        return stream.getShort();
    }

    @Override
    public int getInt() throws IOException {
        check(4);
        return stream.getInt();
    }

    @Override
    public long getLong() throws IOException {
        check(8);
        return stream.getLong();
    }

    @Override
    public float getFloat() throws IOException {
        check(4);
        return stream.getFloat();
    }

    @Override
    public double getDouble() throws IOException {
        check(8);
        return stream.getDouble();
    }

    private void check(int len) throws IOException {
        if (remaining < len) {
            throw new IOException("End of stream");
        }
        remaining -= len;
    }
}
