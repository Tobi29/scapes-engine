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

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.graphics.Texture;
import org.tobi29.scapes.engine.graphics.TextureFilter;
import org.tobi29.scapes.engine.graphics.TextureWrap;
import org.tobi29.scapes.engine.utils.graphics.MipMapGenerator;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

class TextureGL implements Texture {
    protected final ScapesEngine engine;
    protected final OpenGLBind openGL;
    protected final int mipmaps;
    protected final AtomicBoolean dirtyFilter = new AtomicBoolean(true);
    protected int textureID, width = -1, height = -1;
    protected TextureFilter minFilter = TextureFilter.NEAREST, magFilter =
            TextureFilter.NEAREST;
    protected TextureWrap wrapS = TextureWrap.REPEAT, wrapT =
            TextureWrap.REPEAT;
    protected boolean stored, markAsDisposed;
    protected long used;
    protected Runnable detach;
    private ByteBuffer[] buffers;

    public TextureGL(ScapesEngine engine, OpenGLBind openGL, int width,
            int height, ByteBuffer buffer, int mipmaps, TextureFilter minFilter,
            TextureFilter magFilter, TextureWrap wrapS, TextureWrap wrapT) {
        this.engine = engine;
        this.openGL = openGL;
        this.width = width;
        this.height = height;
        this.mipmaps = mipmaps;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        this.wrapS = wrapS;
        this.wrapT = wrapT;
        setBuffer(buffer);
    }

    @Override
    public ScapesEngine engine() {
        return engine;
    }

    @Override
    public void bind(GL gl) {
        OpenGL openGL = this.openGL.get(gl);
        ensureStored(gl);
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
        markAsDisposed = true;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void setWrap(TextureWrap wrapS, TextureWrap wrapT) {
        this.wrapS = wrapS;
        this.wrapT = wrapT;
        dirtyFilter.set(true);
    }

    @Override
    public void setFilter(TextureFilter magFilter, TextureFilter minFilter) {
        this.magFilter = magFilter;
        this.minFilter = minFilter;
        dirtyFilter.set(true);
    }

    @Override
    public ByteBuffer buffer(int i) {
        return buffers[i];
    }

    @Override
    public void setBuffer(ByteBuffer buffer) {
        buffers = MipMapGenerator
                .generateMipMaps(buffer, engine::allocate, width, height,
                        mipmaps, minFilter == TextureFilter.LINEAR);
    }

    @Override
    public boolean ensureStored(GL gl) {
        if (!stored) {
            store(gl);
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
    public void dispose(GL gl) {
        assert stored;
        OpenGL openGL = this.openGL.get(gl);
        openGL.deleteTexture(textureID);
    }

    @Override
    public void reset() {
        assert stored;
        stored = false;
        detach.run();
        detach = null;
        markAsDisposed = false;
    }

    protected void store(GL gl) {
        assert !stored;
        stored = true;
        OpenGL openGL = this.openGL.get(gl);
        textureID = openGL.createTexture();
        texture(gl);
        dirtyFilter.set(true);
        detach = gl.textureTracker().attach(this);
    }

    protected void texture(GL gl) {
        assert stored;
        OpenGL openGL = this.openGL.get(gl);
        openGL.bindTexture(textureID);
        if (buffers.length > 1) {
            openGL.bufferTextureMipMap(width, height, buffers);
        } else {
            openGL.bufferTexture(width, height, true, buffers[0]);
        }
    }
}
