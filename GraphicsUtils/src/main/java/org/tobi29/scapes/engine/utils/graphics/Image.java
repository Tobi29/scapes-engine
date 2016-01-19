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

package org.tobi29.scapes.engine.utils.graphics;

import org.tobi29.scapes.engine.utils.BufferCreator;

import java.nio.ByteBuffer;

public class Image {
    private final int width, height;
    private final ByteBuffer buffer;

    public Image() {
        this(1, 1);
    }

    public Image(int width, int height) {
        this(width, height, BufferCreator.bytes(width * height << 2));
    }

    public Image(int width, int height, ByteBuffer buffer) {
        this.width = width;
        this.height = height;
        this.buffer = buffer;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public ByteBuffer buffer() {
        return buffer.asReadOnlyBuffer();
    }
}
