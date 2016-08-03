/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.backends.opengl;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.graphics.Matrix;
import org.tobi29.scapes.engine.graphics.Model;
import org.tobi29.scapes.engine.graphics.Shader;

abstract class VAO implements Model {
    protected final OpenGLBind openGL;
    protected final ScapesEngine engine;
    protected long used;
    protected boolean stored, markAsDisposed, weak;
    protected Runnable detach;

    protected VAO(ScapesEngine engine, OpenGLBind openGL) {
        this.engine = engine;
        this.openGL = openGL;
    }

    @Override
    public void markAsDisposed() {
        markAsDisposed = true;
    }

    @Override
    public abstract boolean render(GL gl, Shader shader);

    @Override
    public abstract boolean render(GL gl, Shader shader, int length);

    @Override
    public abstract boolean renderInstanced(GL gl, Shader shader, int count);

    @Override
    public abstract boolean renderInstanced(GL gl, Shader shader, int length,
            int count);

    @Override
    public void setWeak(boolean value) {
        weak = value;
    }

    @Override
    public boolean ensureStored(GL gl) {
        if (!stored) {
            boolean success = store(gl);
            used = System.currentTimeMillis();
            return success;
        }
        used = System.currentTimeMillis();
        return true;
    }

    @Override
    public void ensureDisposed(GL gl) {
        if (stored) {
            dispose(gl);
            reset();
        }
    }

    @Override
    public boolean isStored() {
        return stored;
    }

    @Override
    public boolean isUsed(long time) {
        return time - used < 1000 && !markAsDisposed;
    }

    @Override
    public void reset() {
        assert stored;
        stored = false;
        detach.run();
        detach = null;
        markAsDisposed = false;
    }

    protected abstract boolean store(GL gl);

    protected void shader(GL gl, Shader shader) {
        OpenGL openGL = this.openGL.get(gl);
        Matrix matrix = gl.matrixStack().current();
        shader.activate(gl);
        shader.updateUniforms(gl);
        int uniformLocation = shader.uniformLocation(0);
        if (uniformLocation != -1) {
            openGL.setUniformMatrix4(uniformLocation, false,
                    matrix.modelView().values());
        }
        uniformLocation = shader.uniformLocation(1);
        if (uniformLocation != -1) {
            openGL.setUniformMatrix4(uniformLocation, false,
                    gl.modelViewProjectionMatrix().values());
        }
        uniformLocation = shader.uniformLocation(2);
        if (uniformLocation != -1) {
            openGL.setUniformMatrix3(uniformLocation, false,
                    matrix.normal().values());
        }
    }
}
