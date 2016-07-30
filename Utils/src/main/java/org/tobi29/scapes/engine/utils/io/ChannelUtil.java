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
