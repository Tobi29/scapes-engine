package org.tobi29.scapes.engine.graphics;

import org.tobi29.scapes.engine.ScapesEngine;

import java.nio.ByteBuffer;

// TODO: Rename
public interface Texture extends GraphicsObject {
    ScapesEngine engine();

    void bind(GL gl);

    void markDisposed();

    int width();

    int height();

    void setWrap(TextureWrap wrapS, TextureWrap wrapT);

    void setFilter(TextureFilter magFilter, TextureFilter minFilter);

    ByteBuffer buffer(int i);

    void setBuffer(ByteBuffer buffer);
}
