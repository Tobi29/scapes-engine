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

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.utils.graphics.Image;
import org.tobi29.scapes.engine.utils.graphics.PNG;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;

import java.io.IOException;

public class TextureFile extends Texture {
    public TextureFile(ScapesEngine engine, ReadableByteStream input)
            throws IOException {
        this(engine, input, 4);
    }

    public TextureFile(ScapesEngine engine, ReadableByteStream input,
            int mipmaps) throws IOException {
        this(engine, input, mipmaps, TextureFilter.NEAREST,
                TextureFilter.NEAREST, TextureWrap.REPEAT, TextureWrap.REPEAT);
    }

    public TextureFile(ScapesEngine engine, ReadableByteStream input,
            int mipmaps, TextureFilter minFilter, TextureFilter magFilter,
            TextureWrap wrapS, TextureWrap wrapT) throws IOException {
        this(engine, PNG.decode(input, engine::allocate), mipmaps, minFilter,
                magFilter, wrapS, wrapT);
    }

    public TextureFile(ScapesEngine engine, Image image, int mipmaps,
            TextureFilter minFilter, TextureFilter magFilter, TextureWrap wrapS,
            TextureWrap wrapT) {
        super(engine, image.width(), image.height(), image.buffer(), mipmaps,
                minFilter, magFilter, wrapS, wrapT);
    }
}
