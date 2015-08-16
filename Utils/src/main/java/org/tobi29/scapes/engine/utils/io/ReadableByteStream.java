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
import java.nio.charset.StandardCharsets;

public interface ReadableByteStream {
    int remaining();

    default boolean hasRemaining() {
        return remaining() > 0;
    }

    default void skip(int len) throws IOException {
        get(BufferCreator.bytes(len));
    }

    default ReadableByteStream get(ByteBuffer buffer) throws IOException {
        return get(buffer, buffer.remaining());
    }

    ReadableByteStream get(ByteBuffer buffer, int len) throws IOException;

    default boolean getSome(ByteBuffer buffer) throws IOException {
        return getSome(buffer, buffer.remaining());
    }

    boolean getSome(ByteBuffer buffer, int len) throws IOException;

    default ReadableByteStream get(byte[] src) throws IOException {
        return get(src, 0, src.length);
    }

    default ReadableByteStream get(byte[] src, int off, int len)
            throws IOException {
        return get(ByteBuffer.wrap(src, off, len));
    }

    default int getSome(byte[] src, int off, int len) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(src, off, len);
        boolean available = getSome(buffer);
        int position = buffer.position();
        if (position == 0 && !available) {
            return -1;
        }
        return position;
    }

    default boolean getBoolean() throws IOException {
        return get() != 0;
    }

    byte get() throws IOException;

    default short getUByte() throws IOException {
        short value = get();
        if (value < 0) {
            value += 0x100;
        }
        return value;
    }

    short getShort() throws IOException;

    default int getUShort() throws IOException {
        int value = getShort();
        if (value < 0) {
            value += 0x10000;
        }
        return value;
    }

    int getInt() throws IOException;

    default long getUInt() throws IOException {
        long value = getInt();
        if (value < 0) {
            value += 0x100000000L;
        }
        return value;
    }

    long getLong() throws IOException;

    float getFloat() throws IOException;

    double getDouble() throws IOException;

    default byte[] getByteArray() throws IOException {
        return getByteArray(Integer.MAX_VALUE);
    }

    default byte[] getByteArray(int limit) throws IOException {
        int len = getUByte();
        if (len == 0xFF) {
            len = getInt();
        }
        if (len < 0 || len > limit) {
            throw new IOException(
                    "Array length outside of 0 to " + limit + ": " + limit);
        }
        byte[] array = new byte[len];
        get(array);
        return array;
    }

    default byte[] getByteArrayLong() throws IOException {
        return getByteArrayLong(Integer.MAX_VALUE);
    }

    default byte[] getByteArrayLong(int limit) throws IOException {
        int len = getUShort();
        if (len == 0xFFFF) {
            len = getInt();
        }
        if (len < 0 || len > limit) {
            throw new IOException(
                    "Array length outside of 0 to " + limit + ": " + limit);
        }
        byte[] array = new byte[len];
        get(array);
        return array;
    }

    default String getString() throws IOException {
        return getString(Integer.MAX_VALUE);
    }

    default String getString(int limit) throws IOException {
        return new String(getByteArray(limit), StandardCharsets.UTF_8);
    }
}
