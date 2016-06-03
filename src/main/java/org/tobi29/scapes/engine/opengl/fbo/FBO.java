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
package org.tobi29.scapes.engine.opengl.fbo;

import java8.util.stream.Stream;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.OpenGLFunction;
import org.tobi29.scapes.engine.opengl.texture.TextureFBOColor;
import org.tobi29.scapes.engine.opengl.texture.TextureFBODepth;
import org.tobi29.scapes.engine.opengl.texture.TextureFilter;
import org.tobi29.scapes.engine.opengl.texture.TextureWrap;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.FastMath;

public class FBO {
    private final TextureFBOColor[] texturesColor;
    private final TextureFBODepth textureDepth;
    private Runnable detach;
    private int framebufferID;
    private int width, currentWidth;
    private int height, currentHeight;
    private int lastFBO;

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

    @OpenGLFunction
    public void deactivate(GL gl) {
        gl.bindFBO(lastFBO);
        gl.fboTracker().currentFBO = lastFBO;
        lastFBO = 0;
    }

    @OpenGLFunction
    public void activate(GL gl) {
        ensureStored(gl);
        bind(gl);
    }

    @OpenGLFunction
    public void ensureStored(GL gl) {
        int width = this.width;
        int height = this.height;
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width;
            currentHeight = height;
            ensureDisposed(gl);
            if (textureDepth != null) {
                textureDepth.resize(width, height, gl);
            }
            for (TextureFBOColor textureColor : texturesColor) {
                textureColor.resize(width, height, gl);
            }
        }
        if (framebufferID == 0) {
            store(gl);
        }
    }

    @OpenGLFunction
    public void ensureDisposed(GL gl) {
        if (framebufferID != 0) {
            dispose(gl);
        }
    }

    @OpenGLFunction
    private void store(GL gl) {
        framebufferID = gl.createFBO();
        bind(gl);
        attach(gl);
        FBOStatus status = gl.checkFBO();
        if (status != FBOStatus.COMPLETE) {
            // TODO: Add error handling
            System.out.println(status);
        }
        gl.clear(0.0f, 0.0f, 0.0f, 0.0f);
        deactivate(gl);
        detach = gl.fboTracker().attach(this);
    }

    @OpenGLFunction
    protected void dispose(GL gl) {
        gl.deleteFBO(framebufferID);
        for (TextureFBOColor textureColor : texturesColor) {
            textureColor.markDisposed();
        }
        if (textureDepth != null) {
            textureDepth.markDisposed();
        }
        reset();
    }

    protected void reset() {
        assert detach != null;
        detach.run();
        detach = null;
        framebufferID = 0;
        lastFBO = 0;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() {
        return currentWidth;
    }

    public int height() {
        return currentHeight;
    }

    public Stream<TextureFBOColor> texturesColor() {
        return Streams.of(texturesColor);
    }

    public TextureFBOColor textureColor(int i) {
        return texturesColor[i];
    }

    public TextureFBODepth textureDepth() {
        if (textureDepth == null) {
            throw new IllegalStateException("FBO has no depth buffer");
        }
        return textureDepth;
    }

    private void bind(GL gl) {
        FBOTracker fboTracker = gl.fboTracker();
        lastFBO = fboTracker.currentFBO;
        fboTracker.currentFBO = framebufferID;
        gl.bindFBO(framebufferID);
    }

    private void attach(GL gl) {
        if (textureDepth != null) {
            textureDepth.attachDepth(gl);
        }
        for (int i = 0; i < texturesColor.length; i++) {
            texturesColor[i].attachColor(gl, i);
        }
        gl.drawbuffersFBO(texturesColor.length);
    }
}
