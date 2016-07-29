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
