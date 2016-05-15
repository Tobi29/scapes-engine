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

import java8.util.Optional;
import org.tobi29.scapes.engine.opengl.shader.Shader;

import java.nio.ByteBuffer;

public class VAOStatic extends VAO {
    private final RenderType renderType;
    private final int length;
    private final VBO vbo;
    protected boolean weak;
    private Optional<ByteBuffer> data;
    private int indexID, arrayID;

    public VAOStatic(VBO vbo, int[] index, RenderType renderType) {
        this(vbo, index, index.length, renderType);
    }

    public VAOStatic(VBO vbo, int[] index, int length, RenderType renderType) {
        this.vbo = vbo;
        if (renderType == RenderType.TRIANGLES && length % 3 != 0) {
            throw new IllegalArgumentException("Length not multiply of 3");
        } else if (renderType == RenderType.LINES && length % 2 != 0) {
            throw new IllegalArgumentException("Length not multiply of 2");
        }
        this.renderType = renderType;
        this.length = length;
        ByteBuffer indexBuffer = vbo.engine.allocate(length << 1);
        for (int i = 0; i < length; i++) {
            indexBuffer.putShort((short) index[i]);
        }
        data = Optional.of(indexBuffer);
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
        gl.drawElements(length, 0, renderType);
        return true;
    }

    @Override
    public boolean renderInstanced(GL gl, Shader shader, int count) {
        throw new UnsupportedOperationException(
                "Cannot render indexed VAO without length parameter");
    }

    @Override
    public boolean renderInstanced(GL gl, Shader shader, int length,
            int count) {
        throw new UnsupportedOperationException(
                "Cannot render indexed VAO without length parameter");
    }

    @Override
    protected boolean store(GL gl) {
        assert !stored;
        if (!data.isPresent()) {
            return false;
        }
        if (!vbo.canStore()) {
            return false;
        }
        arrayID = gl.createVAO();
        gl.bindVAO(arrayID);
        vbo.store(gl, weak);
        ByteBuffer data = this.data.get();
        data.rewind();
        indexID = gl.createVBO();
        gl.bindVBOElement(indexID);
        gl.bufferVBODataElement(data);
        VAOS.add(this);
        stored = true;
        if (weak) {
            this.data = Optional.empty();
        }
        return true;
    }

    @Override
    protected void dispose(GL gl) {
        assert stored;
        vbo.dispose(gl);
        gl.deleteVBO(indexID);
        gl.deleteVAO(arrayID);
        reset();
    }

    @Override
    protected void reset() {
        super.reset();
        vbo.reset();
    }

    public void setWeak(boolean value) {
        weak = value;
    }
}
