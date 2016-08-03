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
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public final class ChannelUtil {
    private ChannelUtil() {
    }

    public static long skip(ReadableByteChannel channel, long skip)
            throws IOException {
        if (channel instanceof FileChannel) {
            FileChannel fileChannel = (FileChannel) channel;
            fileChannel.position(fileChannel.position() + skip);
            return 0;
        } else {
            ByteBuffer buffer =
                    BufferCreator.bytes((int) FastMath.min(4096, skip));
            while (skip > 0) {
                buffer.limit((int) FastMath.min(buffer.capacity(), skip));
                int read = channel.read(buffer);
                if (read == -1) {
                    throw new IOException("End of stream");
                }
                if (read == 0) {
                    return skip;
                }
            }
            return skip;
        }
    }
}
