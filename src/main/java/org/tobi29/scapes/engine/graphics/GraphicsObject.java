package org.tobi29.scapes.engine.graphics;

public interface GraphicsObject {
    boolean ensureStored(GL gl);

    void ensureDisposed(GL gl);

    boolean isStored();

    boolean isUsed(long time);

    void dispose(GL gl);

    void reset();
}
