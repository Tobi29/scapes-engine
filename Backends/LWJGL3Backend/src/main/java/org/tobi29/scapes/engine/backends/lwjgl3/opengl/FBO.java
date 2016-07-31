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

import java8.util.stream.Stream;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.*;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.FastMath;

final class FBO implements Framebuffer {
    private final TextureFBOColor[] texturesColor;
    private final TextureFBODepth textureDepth;
    private Runnable detach;
    private int framebufferID;
    private int width, currentWidth;
    private int height, currentHeight;
    private int lastFBO;
    private long used;
    private boolean stored, markAsDisposed;

    public FBO(ScapesEngine engine, int width, int height, int colorAttachments,
            boolean depth, boolean hdr, boolean alpha) {
        this.width = FastMath.max(width, 1);
        this.height = FastMath.max(height, 1);
        texturesColor = new TextureFBOColor[colorAttachments];
        for (int i = 0; i < texturesColor.length; i++) {
            texturesColor[i] = new TextureFBOColor(engine, width, height,
                    TextureFilter.LINEAR, TextureFilter.LINEAR,
                    TextureWrap.CLAMP, TextureWrap.CLAMP, alpha, hdr);
        }
        if (depth) {
            textureDepth = new TextureFBODepth(engine, width, height,
                    TextureFilter.LINEAR, TextureFilter.LINEAR,
                    TextureWrap.CLAMP, TextureWrap.CLAMP);
        } else {
            textureDepth = null;
        }
    }

    @Override
    public void deactivate(GL gl) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFBO);
        gl.setCurrentFBO(lastFBO);
        lastFBO = 0;
    }

    @Override
    public void activate(GL gl) {
        ensureStored(gl);
        bind(gl);
    }

    @Override
    public int width() {
        return currentWidth;
    }

    @Override
    public int height() {
        return currentHeight;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Stream<Texture> texturesColor() {
        return Streams.of(texturesColor);
    }

    @Override
    public Texture textureColor(int i) {
        return texturesColor[i];
    }

    @Override
    public Texture textureDepth() {
        if (textureDepth == null) {
            throw new IllegalStateException("FBO has no depth buffer");
        }
        return textureDepth;
    }

    @Override
    public boolean ensureStored(GL gl) {
        if (!stored) {
            store(gl);
        }
        int width = this.width;
        int height = this.height;
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width;
            currentHeight = height;
            if (textureDepth != null) {
                textureDepth.resize(width, height, gl);
            }
            for (TextureFBOColor textureColor : texturesColor) {
                textureColor.resize(width, height, gl);
            }
            bind(gl);
            gl.clear(0.0f, 0.0f, 0.0f, 0.0f);
            deactivate(gl);
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
        gl.check();
        GL30.glDeleteFramebuffers(framebufferID);
        for (TextureFBOColor textureColor : texturesColor) {
            textureColor.dispose(gl);
        }
        if (textureDepth != null) {
            textureDepth.dispose(gl);
        }
    }

    @Override
    public void reset() {
        assert stored;
        stored = false;
        detach.run();
        detach = null;
        framebufferID = 0;
        lastFBO = 0;
        markAsDisposed = false;
        for (TextureFBOColor textureColor : texturesColor) {
            textureColor.reset();
        }
        if (textureDepth != null) {
            textureDepth.reset();
        }
    }

    private void store(GL gl) {
        assert !stored;
        stored = true;
        gl.check();
        framebufferID = GL30.glGenFramebuffers();
        bind(gl);
        if (textureDepth != null) {
            textureDepth.attach(gl);
        }
        for (int i = 0; i < texturesColor.length; i++) {
            texturesColor[i].attach(gl, i);
        }
        GLUtils.drawbuffers(texturesColor.length);
        FramebufferStatus status = GLUtils.status();
        if (status != FramebufferStatus.COMPLETE) {
            // TODO: Add error handling
            System.out.println(status);
        }
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        deactivate(gl);
        detach = gl.fboTracker().attach(this);
    }

    private void bind(GL gl) {
        assert stored;
        gl.check();
        lastFBO = gl.currentFBO();
        gl.setCurrentFBO(framebufferID);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferID);
    }
}