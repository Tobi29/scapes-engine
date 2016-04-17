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
package org.tobi29.scapes.engine.opengl;

import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.shader.Shader;

import java.util.ArrayList;
import java.util.List;

public abstract class VAO {
    protected static final List<VAO> VAOS = new ArrayList<>();
    protected static int disposeOffset;
    protected boolean stored, used, markAsDisposed;

    @OpenGLFunction
    public static void disposeUnused(GL gl) {
        for (int i = disposeOffset; i < VAOS.size(); i += 16) {
            VAO vao = VAOS.get(i);
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
    public static void disposeAll(GL gl) {
        while (!VAOS.isEmpty()) {
            VAOS.get(0).dispose(gl);
        }
    }

    public static void resetAll() {
        while (!VAOS.isEmpty()) {
            VAOS.get(0).reset();
        }
    }

    public static int vaos() {
        return VAOS.size();
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
                    matrix.modelView().getBuffer());
        }
        uniformLocation = shader.uniformLocation(1);
        if (uniformLocation != -1) {
            gl.setUniformMatrix4(uniformLocation, false,
                    gl.modelViewProjectionMatrix().getBuffer());
        }
        uniformLocation = shader.uniformLocation(2);
        if (uniformLocation != -1) {
            gl.setUniformMatrix3(uniformLocation, false,
                    matrix.normal().getBuffer());
        }
    }

    protected void reset() {
        VAOS.remove(this);
        stored = false;
        markAsDisposed = false;
    }
}
