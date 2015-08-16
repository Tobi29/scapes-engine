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

package org.tobi29.scapes.engine.gui;

import java.nio.ByteBuffer;

public interface GlyphRenderer {
    GlyphPage page(int id);

    int pageID(char character);

    int pageCode(char character);

    void dispose();

    class GlyphPage {
        private final ByteBuffer buffer;
        private final float[] width;
        private final int size, tiles;
        private final float tileSize;

        public GlyphPage(ByteBuffer buffer, float[] width, int size, int tiles,
                float tileSize) {
            this.buffer = buffer;
            this.width = width;
            this.size = size;
            this.tiles = tiles;
            this.tileSize = tileSize;
        }

        public ByteBuffer buffer() {
            return buffer;
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public float[] width() {
            return width;
        }

        public int size() {
            return size;
        }

        public int tiles() {
            return tiles;
        }

        public float tileSize() {
            return tileSize;
        }
    }
}
