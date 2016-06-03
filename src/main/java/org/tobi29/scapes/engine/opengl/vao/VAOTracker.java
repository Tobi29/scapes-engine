package org.tobi29.scapes.engine.opengl.vao;

import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.OpenGLFunction;

import java.util.ArrayList;
import java.util.List;

public class VAOTracker {
    private final List<VAO> vaos = new ArrayList<>();
    private int disposeOffset;

    @OpenGLFunction
    public void disposeUnused(GL gl) {
        for (int i = disposeOffset; i < vaos.size(); i += 16) {
            VAO vao = vaos.get(i);
            assert vao.stored;
            if (vao.markAsDisposed || !vao.used) {
                vao.dispose(gl);
            }
            vao.used = false;
        }
        disposeOffset++;
        disposeOffset &= 15;
    }

    @OpenGLFunction
    public void disposeAll(GL gl) {
        while (!vaos.isEmpty()) {
            vaos.get(0).dispose(gl);
        }
    }

    public void resetAll() {
        while (!vaos.isEmpty()) {
            vaos.get(0).reset();
        }
    }

    public int vaoCount() {
        return vaos.size();
    }

    public Runnable attach(VAO vao) {
        vaos.add(vao);
        return () -> vaos.remove(vao);
    }
}
