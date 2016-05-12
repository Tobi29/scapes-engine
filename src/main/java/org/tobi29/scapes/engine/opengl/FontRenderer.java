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

import java8.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.gui.GlyphRenderer;
import org.tobi29.scapes.engine.gui.GuiRenderBatch;
import org.tobi29.scapes.engine.gui.GuiUtils;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.opengl.texture.TextureCustomUnmanaged;
import org.tobi29.scapes.engine.opengl.texture.TextureFilter;
import org.tobi29.scapes.engine.opengl.texture.TextureWrap;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

public class FontRenderer {
    public static final TextInfo EMPTY_TEXT_INFO =
            new TextInfo("", Vector2d.ZERO, 0);
    private static final Logger LOGGER =
            LoggerFactory.getLogger(FontRenderer.class);
    private static final GlyphPage[] EMPTY_GLYPH_PAGE = new GlyphPage[0];
    private final ScapesEngine engine;
    private final GlyphRenderer glyphRenderer;
    private GlyphPage[] pages = EMPTY_GLYPH_PAGE;

    public FontRenderer(ScapesEngine engine, GlyphRenderer glyphRenderer) {
        this.engine = engine;
        this.glyphRenderer = glyphRenderer;
        for (int i = 0; i < 4; i++) {
            initPage(i);
        }
    }

    public static MeshOutput to(GuiRenderBatch renderer, float r, float g,
            float b, float a) {
        return to(renderer, 0.0f, 0.0f, r, g, b, a);
    }

    public static MeshOutput to(GuiRenderBatch renderer, float x, float y,
            float r, float g, float b, float a) {
        return to(renderer, x, y, false, r, g, b, a);
    }

    public static MeshOutput to(GuiRenderBatch renderer, float x, float y,
            boolean cropped, float r, float g, float b, float a) {
        if (cropped) {
            return (xx, yy, width, height, letterWidth, page, pageLetter) -> {
                float xxx = x + xx;
                float yyy = y + yy;
                float w = width * letterWidth;
                float tx = (pageLetter % page.tiles + 0.125f) * page.tileSize;
                float ty = (FastMath.floor((float) pageLetter / page.tiles) +
                        0.125f) * page.tileSize;
                float tw = page.tileSize * letterWidth * 0.75f;
                float th = page.tileSize * 0.75f;
                renderer.texture(page.texture);
                GuiUtils.rectangle(renderer, xxx, yyy, xxx + w, yyy + height,
                        tx, ty, tx + tw, ty + th, r, g, b, a);
            };
        } else {
            return (xx, yy, width, height, letterWidth, page, pageLetter) -> {
                float xxx = x + xx - width * 0.25f;
                float yyy = y + yy - height * 0.25f;
                float w = width * 1.5f;
                float h = height * 1.5f;
                float tx = (pageLetter % page.tiles) * page.tileSize;
                float ty = FastMath.floor((float) pageLetter / page.tiles) *
                        page.tileSize;
                float tw = page.tileSize;
                float th = page.tileSize;
                renderer.texture(page.texture);
                GuiUtils.rectangle(renderer, xxx, yyy, xxx + w, yyy + h, tx, ty,
                        tx + tw, ty + th, r, g, b, a);
            };
        }
    }

    public static MeshOutput to() {
        return (xx, yy, width, height, letterWidth, page, pageLetter) -> {
        };
    }

    private void initPage(int id) {
        long timestamp = System.currentTimeMillis();
        GlyphRenderer.GlyphPage page = glyphRenderer.page(id);
        int imageSize = page.size();
        Texture texture =
                new TextureCustomUnmanaged(engine, imageSize, imageSize,
                        page.buffer(), 2, TextureFilter.LINEAR,
                        TextureFilter.LINEAR, TextureWrap.CLAMP,
                        TextureWrap.CLAMP);
        timestamp = System.currentTimeMillis() - timestamp;
        LOGGER.debug("Rendered font page in {} ms", timestamp);
        if (pages.length <= id) {
            GlyphPage[] newPages = new GlyphPage[id + 1];
            System.arraycopy(pages, 0, newPages, 0, pages.length);
            pages = newPages;
        }
        pages[id] = new GlyphPage(texture, page.width(), page.tiles(),
                page.tileSize());
    }

    public TextInfo render(MeshOutput output, String text, float size) {
        if (text == null) {
            return EMPTY_TEXT_INFO;
        }
        return render(output, text, size, 0, text.length());
    }

    public TextInfo render(MeshOutput output, String text, float size,
            int start, int end) {
        if (text == null) {
            return EMPTY_TEXT_INFO;
        }
        return render(output, text, size, Float.MAX_VALUE, start, end);
    }

    public TextInfo render(MeshOutput output, String text, float size,
            float limit) {
        if (text == null) {
            return EMPTY_TEXT_INFO;
        }
        return render(output, text, size, limit, 0, text.length());
    }

    public TextInfo render(MeshOutput output, String text, float size,
            float limit, int start, int end) {
        if (text == null || start == -1) {
            return EMPTY_TEXT_INFO;
        }
        return render(output, text, size, size, limit, start, end);
    }

    public TextInfo render(MeshOutput output, String text, float width,
            float height, float limit) {
        if (text == null) {
            return EMPTY_TEXT_INFO;
        }
        return render(output, text, width, height, limit, 0, text.length());
    }

    public TextInfo render(MeshOutput output, String text, float width,
            float height, float limit, int start, int end) {
        if (text == null || start == -1) {
            return EMPTY_TEXT_INFO;
        }
        return render(output, text, width, height, height, limit, start, end);
    }

    public TextInfo render(MeshOutput output, String text, float width,
            float height, float line, float limit) {
        if (text == null) {
            return EMPTY_TEXT_INFO;
        }
        return render(output, text, width, height, line, limit, 0,
                text.length());
    }

    @SuppressWarnings("AccessToStaticFieldLockedOnInstance")
    public synchronized TextInfo render(MeshOutput output, String text,
            float width, float height, float line, float limit, int start,
            int end) {
        if (text == null || start == -1) {
            return EMPTY_TEXT_INFO;
        }
        float textWidth = 0.0f;
        int length = 0;
        float xx = 0.0f, yy = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            if (letter == '\n') {
                xx = 0;
                yy += line;
                length++;
            } else {
                int id = glyphRenderer.pageID(letter);
                int pageLetter = glyphRenderer.pageCode(letter);
                if (id >= pages.length || pages[id] == null) {
                    initPage(id);
                }
                GlyphPage page = pages[id];
                float letterWidth = page.width[pageLetter];
                float actualWidth = letterWidth * width;
                if (xx + actualWidth > limit) {
                    break;
                }
                if (i >= start && i < end) {
                    output.rectangle(xx, yy, width, height, letterWidth, page,
                            pageLetter);
                }
                xx += actualWidth;
                textWidth = FastMath.max(textWidth, xx);
                length++;
            }
        }
        return new TextInfo(text, new Vector2d(textWidth, yy + height), length);
    }

    public void dispose() {
        Streams.of(pages).filter(Objects::nonNull)
                .forEach(page -> page.texture.markDisposed());
    }

    public interface MeshOutput {
        void rectangle(float xx, float yy, float width, float height,
                float letterWidth, GlyphPage page, int pageLetter);
    }

    public static final class TextInfo {
        private final String text;
        private final double width;
        private final Vector2 size;
        private final int length;

        private TextInfo(String text, Vector2 size, int length) {
            this.text = text;
            this.size = size;
            width = size.doubleX();
            this.length = length;
        }

        public String text() {
            return text;
        }

        public double width() {
            return width;
        }

        public Vector2 size() {
            return size;
        }

        public int length() {
            return length;
        }
    }

    private static class GlyphPage {
        private final Texture texture;
        private final float[] width;
        private final int tiles;
        private final float tileSize;

        public GlyphPage(Texture texture, float[] width, int tiles,
                float tileSize) {
            this.texture = texture;
            this.width = width;
            this.tiles = tiles;
            this.tileSize = tileSize;
        }
    }
}
