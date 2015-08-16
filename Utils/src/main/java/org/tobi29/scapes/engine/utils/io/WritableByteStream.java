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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public interface WritableByteStream {
    default WritableByteStream put(ByteBuffer buffer) throws IOException {
        return put(buffer, buffer.remaining());
    }

    WritableByteStream put(ByteBuffer buffer, int len) throws IOException;

    default WritableByteStream put(byte[] src) throws IOException {
        return put(src, 0, src.length);
    }

    default WritableByteStream put(byte[] src, int off, int len)
            throws IOException {
        return put(ByteBuffer.wrap(src, off, len));
    }

    default WritableByteStream putBoolean(boolean value) throws IOException {
        return put(value ? 1 : 0);
    }

    WritableByteStream put(int b) throws IOException;

    WritableByteStream putShort(int value) throws IOException;

    WritableByteStream putInt(int value) throws IOException;

    WritableByteStream putLong(long value) throws IOException;

    WritableByteStream putFloat(float value) throws IOException;

    WritableByteStream putDouble(double value) throws IOException;

    default WritableByteStream putByteArray(byte[] value) throws IOException {
        if (value.length < 0xFF) {
            put(value.length);
        } else {
            put(0xFF);
            putInt(value.length);
        }
        return put(value);
    }

    default WritableByteStream putByteArrayLong(byte[] value)
            throws IOException {
        if (value.length < 0xFFFF) {
            putShort(value.length);
        } else {
            putShort(0xFFFF);
            putInt(value.length);
        }
        return put(value);
    }

    default WritableByteStream putString(String value) throws IOException {
        return putByteArray(value.getBytes(StandardCharsets.UTF_8));
    }
}
