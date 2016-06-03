package org.tobi29.scapes.engine.opengl.fbo;

import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.OpenGLFunction;

import java.util.ArrayList;
import java.util.List;

public class FBOTracker {
    private final List<FBO> fbos = new ArrayList<>();
    protected int currentFBO;

    @OpenGLFunction
    public void disposeAll(GL gl) {
        while (!fbos.isEmpty()) {
            fbos.get(0).dispose(gl);
        }
        currentFBO = 0;
    }

    public void resetAll() {
        while (!fbos.isEmpty()) {
            fbos.get(0).reset();
        }
        currentFBO = 0;
    }

    protected Runnable attach(FBO fbo) {
        fbos.add(fbo);
        return () -> fbos.remove(fbo);
    }
}
