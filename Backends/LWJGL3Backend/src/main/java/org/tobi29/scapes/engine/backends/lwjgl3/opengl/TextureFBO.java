package org.tobi29.scapes.engine.backends.lwjgl3.opengl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.graphics.TextureFilter;
import org.tobi29.scapes.engine.graphics.TextureWrap;

import java.nio.ByteBuffer;

abstract class TextureFBO extends TextureGL {
    protected TextureFBO(ScapesEngine engine, int width, int height,
            ByteBuffer buffer, int mipmaps, TextureFilter minFilter,
            TextureFilter magFilter, TextureWrap wrapS, TextureWrap wrapT) {
        super(engine, width, height, buffer, mipmaps, minFilter, magFilter,
                wrapS, wrapT);
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
    public void reset() {
        assert stored;
        stored = false;
        markAsDisposed = false;
    }

    @Override
    protected void store(GL gl) {
        assert !stored;
        stored = true;
        gl.check();
        textureID = GL11.glGenTextures();
        texture(gl);
        dirtyFilter.set(true);
    }
}
