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

import org.tobi29.scapes.engine.opengl.shader.Shader;

public class VAOHybrid extends VAO {
    private final RenderType renderType;
    private final VBO vbo1, vbo2;
    protected boolean weak;
    private int arrayID;

    public VAOHybrid(VBO vbo1, VBO vbo2, RenderType renderType) {
        this.vbo1 = vbo1;
        this.vbo2 = vbo2;
        this.renderType = renderType;
    }

    @Override
    @OpenGLFunction
    public boolean render(GL gl, Shader shader) {
        throw new UnsupportedOperationException(
                "Cannot render hybrid VAO without length parameter");
    }

    @Override
    @OpenGLFunction
    public boolean render(GL gl, Shader shader, int length) {
        if (!ensureStored(gl)) {
            return false;
        }
        gl.bindVAO(arrayID);
        shader(gl, shader);
        gl.drawArray(length, renderType);
        return true;
    }

    @Override
    @OpenGLFunction
    public boolean renderInstanced(GL gl, Shader shader, int count) {
        throw new UnsupportedOperationException(
                "Cannot render hybrid VAO without length parameter");
    }

    @Override
    @OpenGLFunction
    public boolean renderInstanced(GL gl, Shader shader, int length,
            int count) {
        if (!ensureStored(gl)) {
            return false;
        }
        gl.bindVAO(arrayID);
        shader(gl, shader);
        gl.drawArrayInstanced(length, count, renderType);
        return true;
    }

    @Override
    protected boolean store(GL gl) {
        assert !stored;
        if (!vbo1.canStore()) {
            return false;
        }
        if (!vbo2.canStore()) {
            return false;
        }
        arrayID = gl.createVAO();
        gl.bindVAO(arrayID);
        vbo1.store(gl, weak);
        vbo2.store(gl, weak);
        VAOS.add(this);
        stored = true;
        return true;
    }

    @Override
    protected void dispose(GL gl) {
        assert stored;
        vbo1.dispose(gl);
        vbo2.dispose(gl);
        gl.deleteVAO(arrayID);
        reset();
    }

    @Override
    protected void reset() {
        super.reset();
        vbo1.reset();
        vbo2.reset();
    }

    public void setWeak(boolean value) {
        weak = value;
    }
}
