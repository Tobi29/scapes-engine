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

import org.tobi29.scapes.engine.utils.math.FastMath;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class ThrottledBufferStream implements ReadableByteChannel {
    private final ReadableByteChannel channel;
    private final long speed, time;
    private long used;
    private boolean open = true;

    public ThrottledBufferStream(ReadableByteChannel channel, long speed) {
        this.channel = channel;
        this.speed = speed;
        time = System.nanoTime();
    }

    private int check() {
        long allowed = (System.nanoTime() - time) / speed;
        return (int) FastMath.min(allowed - used, Integer.MAX_VALUE);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int len = FastMath.min(check(), dst.remaining());
        if (len <= 0) {
            return 0;
        }
        int limit = dst.limit();
        dst.limit(dst.position() + len);
        int read = channel.read(dst);
        dst.limit(limit);
        if (read > 0) {
            used += read;
        }
        return read;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        open = false;
    }
}
