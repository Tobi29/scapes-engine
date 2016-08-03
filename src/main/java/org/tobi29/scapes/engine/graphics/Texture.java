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

package org.tobi29.scapes.engine.graphics;

import org.tobi29.scapes.engine.ScapesEngine;

import java.nio.ByteBuffer;

// TODO: Rename
public interface Texture extends GraphicsObject {
    ScapesEngine engine();

    void bind(GL gl);

    void markDisposed();

    int width();

    int height();

    void setWrap(TextureWrap wrapS, TextureWrap wrapT);

    void setFilter(TextureFilter magFilter, TextureFilter minFilter);

    ByteBuffer buffer(int i);

    void setBuffer(ByteBuffer buffer);
}
