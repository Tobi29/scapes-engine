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

import org.tobi29.scapes.engine.opengl.texture.TextureFBOColor;
import org.tobi29.scapes.engine.opengl.texture.TextureFBODepth;
import org.tobi29.scapes.engine.opengl.texture.TextureFilter;
import org.tobi29.scapes.engine.opengl.texture.TextureWrap;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.util.ArrayList;
import java.util.List;

public class FBO {
    private static final List<FBO> FBOS = new ArrayList<>();
    private static int currentBO;
    private final TextureFBOColor[] texturesColor;
    private final boolean depth, hdr, alpha;
    private int framebufferID;
    private int width;
    private int height;
    private int lastFBO;
    private TextureFBODepth textureDepth;

    public FBO(int width, int height, int colorAttachments, boolean depth,
            boolean hdr, boolean alpha, GL gl) {
        this.width = FastMath.max(width, 1);
        this.height = FastMath.max(height, 1);
        texturesColor = new TextureFBOColor[colorAttachments];
        this.depth = depth;
        this.hdr = hdr;
        this.alpha = alpha;
        init(gl);
    }

    @OpenGLFunction
    public static void disposeAll(GL gl) {
        while (!FBOS.isEmpty()) {
            FBOS.get(0).dispose(gl);
        }
    }

    @OpenGLFunction
    private void init(GL gl) {
        init(gl, alpha, hdr);
        framebufferID = gl.createFBO();
        activate(gl);
        attach(gl);
        if (gl.checkFBO() != FBOStatus.COMPLETE) {
            for (TextureFBOColor textureColor : texturesColor) {
                textureColor.dispose(gl);
            }
            if (depth) {
                textureDepth.dispose(gl);
            }
            init(gl, alpha, false);
            attach(gl);
        }
        gl.clear(0.0f, 0.0f, 0.0f, 0.0f);
        deactivate(gl);
        FBOS.add(this);
    }

    @OpenGLFunction
    private void init(GL gl, boolean alpha, boolean hdr) {
        for (int i = 0; i < texturesColor.length; i++) {
            texturesColor[i] =
                    new TextureFBOColor(width, height, TextureFilter.LINEAR,
                            TextureFilter.LINEAR, TextureWrap.CLAMP,
                            TextureWrap.CLAMP, alpha, hdr);
            texturesColor[i].bind(gl);
        }
        if (depth) {
            textureDepth =
                    new TextureFBODepth(width, height, TextureFilter.LINEAR,
                            TextureFilter.LINEAR, TextureWrap.CLAMP,
                            TextureWrap.CLAMP);
            textureDepth.bind(gl);
        }
    }

    @OpenGLFunction
    private void attach(GL gl) {
        if (depth) {
            gl.attachDepth(textureDepth.textureID());
        }
        for (int i = 0; i < texturesColor.length; i++) {
            gl.attachColor(texturesColor[i].textureID(), i);
        }
    }

    @OpenGLFunction
    public void deactivate(GL gl) {
        gl.bindFBO(lastFBO);
        currentBO = lastFBO;
        lastFBO = 0;
    }

    @OpenGLFunction
    public void activate(GL gl) {
        if (framebufferID == -1) {
            init(gl);
        }
        lastFBO = currentBO;
        currentBO = framebufferID;
        gl.bindFBO(framebufferID);
        gl.drawbuffersFBO(texturesColor.length);
    }

    @OpenGLFunction
    public void setSize(int width, int height, GL gl) {
        dispose(gl);
        this.width = FastMath.max(width, 1);
        this.height = FastMath.max(height, 1);
        init(gl);
    }

    @OpenGLFunction
    public void dispose(GL gl) {
        if (framebufferID != -1) {
            gl.deleteFBO(framebufferID);
            for (TextureFBOColor textureColor : texturesColor) {
                textureColor.dispose(gl);
            }
            if (depth) {
                textureDepth.dispose(gl);
            }
            framebufferID = -1;
        }
        FBOS.remove(this);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public TextureFBOColor[] texturesColor() {
        return texturesColor;
    }

    public TextureFBODepth textureDepth() {
        if (!depth) {
            throw new IllegalStateException("FBO has no depth buffer");
        }
        return textureDepth;
    }
}
