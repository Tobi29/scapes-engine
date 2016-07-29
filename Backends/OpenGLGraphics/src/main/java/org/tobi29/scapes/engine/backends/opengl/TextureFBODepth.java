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
import org.tobi29.scapes.engine.graphics.TextureFilter;
import org.tobi29.scapes.engine.graphics.TextureWrap;

final class TextureFBODepth extends TextureFBO {
    public TextureFBODepth(ScapesEngine engine, OpenGLBind openGL, int width,
            int height, TextureFilter minFilter, TextureFilter magFilter,
            TextureWrap wrapS, TextureWrap wrapT) {
        super(engine, openGL, width, height, null, 0, minFilter, magFilter,
                wrapS, wrapT);
    }

    public void attach(GL gl) {
        store(gl);
        OpenGL openGL = this.openGL.get(gl);
        openGL.attachDepth(textureID);
    }

    @Override
    protected void texture(GL gl) {
        assert stored;
        OpenGL openGL = this.openGL.get(gl);
        openGL.bindTexture(textureID);
        openGL.bufferTextureDepth(width, height, null);
    }
}
