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
import org.lwjgl.opengl.GL12;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.graphics.Texture;
import org.tobi29.scapes.engine.graphics.TextureFilter;
import org.tobi29.scapes.engine.graphics.TextureWrap;
import org.tobi29.scapes.engine.utils.graphics.MipMapGenerator;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

class TextureGL implements Texture {
    protected final ScapesEngine engine;
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
    protected ByteBuffer[] buffers;

    public TextureGL(ScapesEngine engine, int width, int height,
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

    @Override
    public ScapesEngine engine() {
        return engine;
    }

    @Override
    public void bind(GL gl) {
        ensureStored(gl);
        gl.check();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        if (dirtyFilter.getAndSet(false)) {
            if (mipmaps > 0) {
                switch (minFilter) {
                    case NEAREST:
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                                GL11.GL_TEXTURE_MIN_FILTER,
                                GL11.GL_NEAREST_MIPMAP_LINEAR);
                        break;
                    case LINEAR:
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                                GL11.GL_TEXTURE_MIN_FILTER,
                                GL11.GL_LINEAR_MIPMAP_LINEAR);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Illegal texture-filter!");
                }
            } else {
                switch (minFilter) {
                    case NEAREST:
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                                GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                        break;
                    case LINEAR:
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                                GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Illegal texture-filter!");
                }
            }
            switch (magFilter) {
                case NEAREST:
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                    break;
                case LINEAR:
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Illegal texture-filter!");
            }
            switch (wrapS) {
                case REPEAT:
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                    break;
                case CLAMP:
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal texture-wrap!");
            }
            switch (wrapT) {
                case REPEAT:
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
                    break;
                case CLAMP:
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal texture-wrap!");
            }
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
        gl.check();
        GL11.glDeleteTextures(textureID);
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
        gl.check();
        textureID = GL11.glGenTextures();
        texture(gl);
        dirtyFilter.set(true);
        detach = gl.textureTracker().attach(this);
    }

    protected void texture(GL gl) {
        assert stored;
        gl.check();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        if (buffers.length > 1) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL,
                    buffers.length - 1);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width,
                    height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffers[0]);
            for (int i = 1; i < buffers.length; i++) {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, GL11.GL_RGBA,
                        FastMath.max(width >> i, 1),
                        FastMath.max(height >> i, 1), 0, GL11.GL_RGBA,
                        GL11.GL_UNSIGNED_BYTE, buffers[i]);
            }
        } else {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width,
                    height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffers[0]);
        }
    }
}
