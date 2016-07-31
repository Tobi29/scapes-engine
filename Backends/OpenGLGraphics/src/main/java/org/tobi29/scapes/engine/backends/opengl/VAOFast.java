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
package org.tobi29.scapes.engine.backends.opengl;

import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.graphics.RenderType;
import org.tobi29.scapes.engine.graphics.Shader;

final class VAOFast extends VAO {
    private final RenderType renderType;
    private final int length;
    private final VBO vbo;
    private int arrayID;

    public VAOFast(VBO vbo, int length, RenderType renderType) {
        super(vbo.engine(), vbo.openGL());
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
    public boolean render(GL gl, Shader shader) {
        return render(gl, shader, length);
    }

    @Override
    public boolean render(GL gl, Shader shader, int length) {
        if (!ensureStored(gl)) {
            return false;
        }
        OpenGL openGL = this.openGL.get(gl);
        openGL.bindVAO(arrayID);
        shader(gl, shader);
        openGL.drawArray(length, renderType);
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
        OpenGL openGL = this.openGL.get(gl);
        openGL.bindVAO(arrayID);
        shader(gl, shader);
        openGL.drawArrayInstanced(length, count, renderType);
        return true;
    }

    @Override
    public void reset() {
        super.reset();
        vbo.reset();
    }

    @Override
    protected boolean store(GL gl) {
        assert !stored;
        if (!vbo.canStore()) {
            return false;
        }
        stored = true;
        OpenGL openGL = this.openGL.get(gl);
        arrayID = openGL.createVAO();
        openGL.bindVAO(arrayID);
        vbo.store(gl, weak);
        detach = gl.vaoTracker().attach(this);
        return true;
    }

    @Override
    public void dispose(GL gl) {
        assert stored;
        OpenGL openGL = this.openGL.get(gl);
        vbo.dispose(gl);
        openGL.deleteVAO(arrayID);
    }
}