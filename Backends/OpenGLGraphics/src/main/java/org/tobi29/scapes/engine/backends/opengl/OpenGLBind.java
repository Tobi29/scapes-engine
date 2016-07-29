package org.tobi29.scapes.engine.backends.opengl;

import org.tobi29.scapes.engine.graphics.GL;

final class OpenGLBind {
    private final GL gl;
    private final OpenGL openGL;

    public OpenGLBind(GL gl, OpenGL openGL) {
        this.gl = gl;
        this.openGL = openGL;
    }

    public OpenGL get(GL gl) {
        assert this.gl == gl;
        return openGL;
    }
}
