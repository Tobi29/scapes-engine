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

package org.tobi29.scapes.engine.utils.io;

import org.tobi29.scapes.engine.utils.BufferCreator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteStreamInputStream extends InputStream {
    private final ReadableByteStream stream;
    private final ByteBuffer single = BufferCreator.bytes(1);

    public ByteStreamInputStream(ReadableByteStream stream) {
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        single.clear();
        stream.getSome(single);
        single.flip();
        if (!single.hasRemaining()) {
            return -1;
        }
        return single.get();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return stream.getSome(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        int len = (int) n;
        stream.skip(len);
        return len;
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }
}
