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
import java.nio.channels.ByteChannel;

public class ByteBufferChannel implements ByteChannel {
    private final ByteBuffer buffer;

    public ByteBufferChannel(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public ByteBuffer buffer() {
        return buffer;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int len = dst.remaining();
        int limit = buffer.limit();
        buffer.limit(buffer.position() + len);
        dst.put(buffer);
        buffer.limit(limit);
        return len;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int len = src.remaining();
        buffer.put(src);
        return len;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
    }
}
