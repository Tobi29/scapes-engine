package org.tobi29.scapes.engine.graphics;

import java8.util.stream.Stream;

public interface Framebuffer extends GraphicsObject {
    void deactivate(GL gl);

    void activate(GL gl);

    int width();

    int height();

    void setSize(int width, int height);

    Stream<Texture> texturesColor();

    Texture textureColor(int i);

    Texture textureDepth();
}
