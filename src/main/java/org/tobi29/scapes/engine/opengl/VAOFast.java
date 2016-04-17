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

public class VAOFast extends VAO {
    private final RenderType renderType;
    private final int length;
    private final VBO vbo;
    protected boolean weak;
    private int arrayID;

    public VAOFast(VBO vbo, RenderType renderType) {
        this(vbo, vbo.length, renderType);
    }

    public VAOFast(VBO vbo, int length, RenderType renderType) {
        this.vbo = vbo;
        if (renderType == RenderType.TRIANGLES && length % 3 != 0) {
            throw new IllegalArgumentException("Length not multiply of 3");
        } else if (renderType == RenderType.LINES && length % 2 != 0) {
            throw new IllegalArgumentException("Length not multiply of 2");
        }
        this.renderType = renderType;
        this.length = length;
    }

    @Override
    @OpenGLFunction
    public boolean render(GL gl, Shader shader) {
        return render(gl, shader, length);
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
    public boolean renderInstanced(GL gl, Shader shader, int count) {
        return renderInstanced(gl, shader, length, count);
    }

    @Override
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
        if (!vbo.canStore()) {
            return false;
        }
        arrayID = gl.createVAO();
        gl.bindVAO(arrayID);
        vbo.store(gl, weak);
        VAOS.add(this);
        stored = true;
        return true;
    }

    @Override
    protected void dispose(GL gl) {
        assert stored;
        vbo.dispose(gl);
        gl.deleteVAO(arrayID);
        reset();
    }

    public void setWeak(boolean value) {
        weak = value;
    }
}
