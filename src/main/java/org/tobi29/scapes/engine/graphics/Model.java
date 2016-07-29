package org.tobi29.scapes.engine.graphics;

public interface Model extends GraphicsObject {
    void markAsDisposed();

    boolean render(GL gl, Shader shader);

    boolean render(GL gl, Shader shader, int length);

    boolean renderInstanced(GL gl, Shader shader, int count);

    boolean renderInstanced(GL gl, Shader shader, int length, int count);

    void setWeak(boolean value);
}
