package org.tobi29.scapes.engine.graphics;

import java.nio.ByteBuffer;

public interface ModelHybrid extends Model {
    int strideStream();

    void bufferStream(GL gl, ByteBuffer buffer);
}
