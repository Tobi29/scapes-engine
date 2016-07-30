package org.tobi29.scapes.engine.utils.codec;

import java.io.IOException;

public interface ReadableAudioStream extends AutoCloseable {
    boolean get(AudioBuffer buffer) throws IOException;

    @Override
    void close() throws IOException;
}
