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
import org.tobi29.scapes.engine.graphics.TextureFilter;
import org.tobi29.scapes.engine.graphics.TextureWrap;

import java.nio.ByteBuffer;

abstract class TextureFBO extends TextureGL {
    protected TextureFBO(ScapesEngine engine, OpenGLBind openGL, int width,
            int height, ByteBuffer buffer, int mipmaps, TextureFilter minFilter,
            TextureFilter magFilter, TextureWrap wrapS, TextureWrap wrapT) {
        super(engine, openGL, width, height, buffer, mipmaps, minFilter,
                magFilter, wrapS, wrapT);
    }

    public void resize(int width, int height, GL gl) {
        this.width = width;
        this.height = height;
        texture(gl);
    }

    @Override
    public void bind(GL gl) {
        if (!stored) {
            return;
        }
        OpenGL openGL = this.openGL.get(gl);
        openGL.bindTexture(textureID);
        if (dirtyFilter.getAndSet(false)) {
            openGL.minFilter(minFilter, mipmaps > 0);
            openGL.magFilter(magFilter);
            openGL.wrapS(wrapS);
            openGL.wrapT(wrapT);
        }
    }

    @Override
    public void markDisposed() {
        throw new UnsupportedOperationException(
                "FBO texture should not be disposed");
    }

    @Override
    public boolean ensureStored(GL gl) {
        throw new UnsupportedOperationException(
                "FBO texture can only be managed by framebuffer");
    }

    @Override
    public void ensureDisposed(GL gl) {
        throw new UnsupportedOperationException(
                "FBO texture can only be managed by framebuffer");
    }

    @Override
    public boolean isUsed(long time) {
        return stored;
    }

    @Override
    public void dispose(GL gl) {
        assert stored;
        OpenGL openGL = this.openGL.get(gl);
        openGL.deleteTexture(textureID);
    }

    @Override
    public void reset() {
        assert stored;
        stored = false;
        markAsDisposed = false;
    }

    @Override
    protected void store(GL gl) {
        assert !stored;
        stored = true;
        OpenGL openGL = this.openGL.get(gl);
        textureID = openGL.createTexture();
        texture(gl);
        dirtyFilter.set(true);
    }
}
