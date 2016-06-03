package org.tobi29.scapes.engine.opengl.texture;

import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.OpenGLFunction;

import java.util.ArrayList;
import java.util.List;

public class TextureTracker {
    private final List<Texture> textures = new ArrayList<>();
    private int disposeOffset;

    @OpenGLFunction
    public void disposeUnused(GL gl) {
        long time = System.currentTimeMillis();
        for (int i = disposeOffset; i < textures.size(); i += 16) {
            Texture texture = textures.get(i);
            assert texture.stored;
            if (texture.markAsDisposed || !texture.used(time)) {
                texture.dispose(gl);
            }
        }
        disposeOffset++;
        disposeOffset &= 15;
    }

    @OpenGLFunction
    public void disposeAll(GL gl) {
        while (!textures.isEmpty()) {
            textures.get(0).dispose(gl);
        }
    }

    public void resetAll() {
        while (!textures.isEmpty()) {
            textures.get(0).reset();
        }
    }

    public int textureCount() {
        return textures.size();
    }

    public Runnable attach(Texture texture) {
        textures.add(texture);
        return () -> textures.remove(texture);
    }
}
