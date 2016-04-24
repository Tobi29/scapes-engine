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
package org.tobi29.scapes.engine.opengl.texture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.OpenGLFunction;
import org.tobi29.scapes.engine.utils.graphics.MipMapGenerator;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class Texture {
    protected static final List<Texture> TEXTURES = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(Texture.class);
    private static int disposeOffset;
    protected final ScapesEngine engine;
    protected final int mipmaps;
    protected boolean dirtyFilter = true;
    protected int textureID, width = -1, height = -1;
    protected TextureFilter minFilter = TextureFilter.NEAREST, magFilter =
            TextureFilter.NEAREST;
    protected TextureWrap wrapS = TextureWrap.REPEAT, wrapT =
            TextureWrap.REPEAT;
    protected boolean markAsDisposed;
    protected long used;
    private ByteBuffer[] buffers;

    protected Texture(ScapesEngine engine, int width, int height,
            ByteBuffer buffer, int mipmaps, TextureFilter minFilter,
            TextureFilter magFilter, TextureWrap wrapS, TextureWrap wrapT) {
        this.engine = engine;
        this.width = width;
        this.height = height;
        this.mipmaps = mipmaps;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        this.wrapS = wrapS;
        this.wrapT = wrapT;
        setBuffer(buffer);
    }

    @OpenGLFunction
    public static void disposeUnused(GL gl) {
        long time = System.currentTimeMillis();
        for (int i = disposeOffset; i < TEXTURES.size(); i += 16) {
            Texture texture = TEXTURES.get(i);
            assert texture.textureID != 0;
            if (texture.markAsDisposed || !texture.used(time)) {
                texture.dispose(gl);
            }
        }
        disposeOffset++;
        disposeOffset &= 15;
    }

    @OpenGLFunction
    public static void disposeAll(GL gl) {
        while (!TEXTURES.isEmpty()) {
            TEXTURES.get(0).dispose(gl);
        }
    }

    public static void resetAll() {
        while (!TEXTURES.isEmpty()) {
            TEXTURES.get(0).reset();
        }
    }

    public static int textureCount() {
        return TEXTURES.size();
    }

    @OpenGLFunction
    public void bind(GL gl) {
        ensureStored(gl);
        gl.bindTexture(textureID);
        if (dirtyFilter) {
            gl.minFilter(minFilter, mipmaps > 0);
            gl.magFilter(magFilter);
            gl.wrapS(wrapS);
            gl.wrapT(wrapT);
            dirtyFilter = false;
        }
    }

    @OpenGLFunction
    public void ensureStored(GL gl) {
        if (textureID == 0) {
            store(gl);
        }
        used = System.currentTimeMillis();
    }

    @OpenGLFunction
    public void ensureDisposed(GL gl) {
        if (textureID != 0) {
            dispose(gl);
        }
    }

    protected void store(GL gl) {
        textureID = gl.createTexture();
        gl.bindTexture(textureID);
        if (buffers.length > 1) {
            gl.bufferTextureMipMap(width, height, buffers);
        } else {
            gl.bufferTexture(width, height, true, buffers[0]);
        }
        dirtyFilter = true;
        TEXTURES.add(this);
    }

    protected boolean used(long time) {
        return time - used < 1000;
    }

    @OpenGLFunction
    protected void dispose(GL gl) {
        gl.deleteTexture(textureID);
        reset();
    }

    protected void reset() {
        TEXTURES.remove(this);
        textureID = 0;
        markAsDisposed = false;
    }

    public void markDisposed() {
        markAsDisposed = true;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void setWrap(TextureWrap wrapS, TextureWrap wrapT) {
        this.wrapS = wrapS;
        this.wrapT = wrapT;
        dirtyFilter = true;
    }

    public void setFilter(TextureFilter magFilter, TextureFilter minFilter) {
        this.magFilter = magFilter;
        this.minFilter = minFilter;
        dirtyFilter = true;
    }

    public ByteBuffer buffer(int i) {
        return buffers[i];
    }

    public void setBuffer(ByteBuffer buffer) {
        buffers = MipMapGenerator
                .generateMipMaps(buffer, engine::allocate, width, height,
                        mipmaps, minFilter == TextureFilter.LINEAR);
    }
}
