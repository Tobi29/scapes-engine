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

package org.tobi29.scapes.engine.backends.lwjgl3.opengl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.graphics.TextureFilter;
import org.tobi29.scapes.engine.graphics.TextureWrap;

import java.nio.ByteBuffer;

final class TextureFBOColor extends TextureFBO {
    private final boolean alpha, hdr;

    public TextureFBOColor(ScapesEngine engine, int width, int height,
            TextureFilter minFilter, TextureFilter magFilter, TextureWrap wrapS,
            TextureWrap wrapT, boolean alpha, boolean hdr) {
        super(engine, width, height, null, 0, minFilter, magFilter, wrapS,
                wrapT);
        this.alpha = alpha;
        this.hdr = hdr;
    }

    public void attach(GL gl, int i) {
        if (i < 0 || i > 31) {
            throw new IllegalArgumentException(
                    "Color Attachment must be 0-31, was " + i);
        }
        store(gl);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,
                GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D, textureID,
                0);
    }

    @Override
    protected void texture(GL gl) {
        assert stored;
        gl.check();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        if (hdr) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0,
                    alpha ? GL30.GL_RGBA16F : GL30.GL_RGB16F, width, height, 0,
                    alpha ? GL11.GL_RGBA : GL11.GL_RGB, GL30.GL_HALF_FLOAT,
                    (ByteBuffer) null);
        } else {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0,
                    alpha ? GL11.GL_RGBA : GL11.GL_RGB, width, height, 0,
                    alpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE,
                    (ByteBuffer) null);
        }
    }
}
