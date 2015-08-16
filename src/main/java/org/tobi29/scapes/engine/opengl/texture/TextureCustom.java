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

import org.tobi29.scapes.engine.utils.BufferCreatorNative;

import java.nio.ByteBuffer;

public class TextureCustom extends Texture {
    public TextureCustom(int width, int height) {
        this(width, height, BufferCreatorNative.bytes(width * height * 4), 0);
    }

    public TextureCustom(int width, int height, ByteBuffer buffer,
            int mipmaps) {
        this(width, height, buffer, mipmaps, TextureFilter.NEAREST,
                TextureFilter.NEAREST, TextureWrap.REPEAT, TextureWrap.REPEAT);
    }

    public TextureCustom(int width, int height, ByteBuffer buffer, int mipmaps,
            TextureFilter minFilter, TextureFilter magFilter, TextureWrap wrapS,
            TextureWrap wrapT) {
        super(width, height, buffer, mipmaps, minFilter, magFilter, wrapS,
                wrapT);
    }

    public TextureCustom(int width, int height, int mipmaps) {
        this(width, height, BufferCreatorNative.bytes(width * height * 4),
                mipmaps);
    }

    public TextureCustom(int width, int height, int mipmaps,
            TextureFilter minFilter, TextureFilter magFilter, TextureWrap wrapS,
            TextureWrap wrapT) {
        this(width, height, BufferCreatorNative.bytes(width * height * 4),
                mipmaps, minFilter, magFilter, wrapS, wrapT);
    }

    public TextureCustom(int width, int height, ByteBuffer buffer) {
        this(width, height, buffer, 4);
    }
}
