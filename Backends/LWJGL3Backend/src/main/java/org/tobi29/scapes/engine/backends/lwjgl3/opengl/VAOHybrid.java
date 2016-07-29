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
package org.tobi29.scapes.engine.backends.lwjgl3.opengl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.graphics.ModelHybrid;
import org.tobi29.scapes.engine.graphics.RenderType;
import org.tobi29.scapes.engine.graphics.Shader;

import java.nio.ByteBuffer;

final class VAOHybrid extends VAO implements ModelHybrid {
    private final RenderType renderType;
    private final VBO vbo1, vbo2;
    private int arrayID;

    public VAOHybrid(VBO vbo1, VBO vbo2, RenderType renderType) {
        super(vbo1.engine());
        this.vbo1 = vbo1;
        this.vbo2 = vbo2;
        this.renderType = renderType;
    }

    @Override
    public boolean render(GL gl, Shader shader) {
        throw new UnsupportedOperationException(
                "Cannot render hybrid VAO without length parameter");
    }

    @Override
    public boolean render(GL gl, Shader shader, int length) {
        if (!ensureStored(gl)) {
            return false;
        }
        gl.check();
        GL30.glBindVertexArray(arrayID);
        shader(gl, shader);
        GL11.glDrawArrays(GLUtils.renderType(renderType), 0, length);
        return true;
    }

    @Override
    public boolean renderInstanced(GL gl, Shader shader, int count) {
        throw new UnsupportedOperationException(
                "Cannot render hybrid VAO without length parameter");
    }

    @Override
    public boolean renderInstanced(GL gl, Shader shader, int length,
            int count) {
        if (!ensureStored(gl)) {
            return false;
        }
        gl.check();
        GL30.glBindVertexArray(arrayID);
        shader(gl, shader);
        GL31.glDrawArraysInstanced(GLUtils.renderType(renderType), 0, length,
                count);
        return true;
    }

    @Override
    public void reset() {
        super.reset();
        vbo1.reset();
        vbo2.reset();
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
        stored = true;
        gl.check();
        arrayID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(arrayID);
        vbo1.store(gl, weak);
        vbo2.store(gl, weak);
        detach = gl.vaoTracker().attach(this);
        return true;
    }

    @Override
    public void dispose(GL gl) {
        assert stored;
        gl.check();
        vbo1.dispose(gl);
        vbo2.dispose(gl);
        GL30.glDeleteVertexArrays(arrayID);
    }

    @Override
    public int strideStream() {
        return vbo2.stride();
    }

    @Override
    public void bufferStream(GL gl, ByteBuffer buffer) {
        vbo2.replaceBuffer(gl, buffer);
    }
}
