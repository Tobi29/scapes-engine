/*
 * Copyright 2012-2015 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tobi29.scapes.engine.opengl.vao;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.OpenGLFunction;
import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.shader.Shader;

public abstract class VAO {
    protected final ScapesEngine engine;
    protected boolean stored, used, markAsDisposed;
    protected Runnable detach;

    protected VAO(ScapesEngine engine) {
        this.engine = engine;
    }

    public void markAsDisposed() {
        markAsDisposed = true;
    }

    @OpenGLFunction
    public boolean ensureStored(GL gl) {
        if (!stored) {
            boolean success = store(gl);
            used = true;
            return success;
        }
        used = true;
        return true;
    }

    @OpenGLFunction
    public void ensureDisposed(GL gl) {
        if (stored) {
            dispose(gl);
        }
    }

    @OpenGLFunction
    public abstract boolean render(GL gl, Shader shader);

    @OpenGLFunction
    public abstract boolean render(GL gl, Shader shader, int length);

    @OpenGLFunction
    public abstract boolean renderInstanced(GL gl, Shader shader, int count);

    @OpenGLFunction
    public abstract boolean renderInstanced(GL gl, Shader shader, int length,
            int count);

    protected abstract boolean store(GL gl);

    protected abstract void dispose(GL gl);

    protected void shader(GL gl, Shader shader) {
        Matrix matrix = gl.matrixStack().current();
        gl.activateShader(shader.programID());
        shader.updateUniforms(gl);
        int uniformLocation = shader.uniformLocation(0);
        if (uniformLocation != -1) {
            gl.setUniformMatrix4(uniformLocation, false,
                    matrix.modelView().values());
        }
        uniformLocation = shader.uniformLocation(1);
        if (uniformLocation != -1) {
            gl.setUniformMatrix4(uniformLocation, false,
                    gl.modelViewProjectionMatrix().values());
        }
        uniformLocation = shader.uniformLocation(2);
        if (uniformLocation != -1) {
            gl.setUniformMatrix3(uniformLocation, false,
                    matrix.normal().values());
        }
    }

    protected void reset() {
        assert detach != null;
        detach.run();
        detach = null;
        stored = false;
        markAsDisposed = false;
    }
}
