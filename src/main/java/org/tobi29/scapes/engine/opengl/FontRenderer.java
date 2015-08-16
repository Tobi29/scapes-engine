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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.gui.GlyphRenderer;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.opengl.texture.TextureCustom;
import org.tobi29.scapes.engine.opengl.texture.TextureFilter;
import org.tobi29.scapes.engine.opengl.texture.TextureWrap;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class FontRenderer {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(FontRenderer.class);
    private static final GlyphPage[] EMPTY_GLYPH_PAGE = new GlyphPage[0];
    private static final TextVAO[] EMPTY_TEXT_VAO = new TextVAO[0];
    private final GlyphRenderer glyphRenderer;
    private GlyphPage[] pages = EMPTY_GLYPH_PAGE;

    public FontRenderer(GlyphRenderer glyphRenderer) {
        this.glyphRenderer = glyphRenderer;
        for (int i = 0; i < 4; i++) {
            initPage(i);
        }
    }

    private void initPage(int id) {
        long timestamp = System.currentTimeMillis();
        GlyphRenderer.GlyphPage page = glyphRenderer.page(id);
        int imageSize = page.size();
        Texture texture =
                new TextureCustom(imageSize, imageSize, page.buffer(), 2,
                        TextureFilter.LINEAR, TextureFilter.LINEAR,
                        TextureWrap.CLAMP, TextureWrap.CLAMP);
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

    public Text render(String text, float x, float y, float size, float r,
            float g, float b, float a) {
        if (text == null) {
            return new Text();
        }
        return render(text, x, y, size, size, size, Float.MAX_VALUE, r, g, b, a,
                0, text.length(), false);
    }

    public Text render(String text, float x, float y, float size, float limit,
            float r, float g, float b, float a) {
        if (text == null) {
            return new Text();
        }
        return render(text, x, y, size, size, size, limit, r, g, b, a, 0,
                text.length(), false);
    }

    public synchronized Text render(String text, float x, float y, float width,
            float height, float line, float limit, float r, float g, float b,
            float a, int start, int end, boolean cropped) {
        if (text == null || start == -1) {
            return new Text();
        }
        Map<Integer, Mesh> meshes = new ConcurrentHashMap<>();
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
                    Mesh mesh = meshes.computeIfAbsent(id, key -> new Mesh());
                    float xxx, yyy, w, h, tx, ty, tw, th;
                    if (cropped) {
                        xxx = xx + x;
                        yyy = yy + y;
                        w = width * letterWidth;
                        h = height;
                        tx = (pageLetter % page.tiles + 0.125f) * page.tileSize;
                        ty = (FastMath.floor((float) pageLetter / page.tiles) +
                                0.125f) * page.tileSize;
                        tw = page.tileSize * letterWidth * 0.75f;
                        th = page.tileSize * 0.75f;
                    } else {
                        xxx = xx + x - width * 0.25f;
                        yyy = yy + y - height * 0.25f;
                        w = width * 1.5f;
                        h = height * 1.5f;
                        tx = (pageLetter % page.tiles) * page.tileSize;
                        ty = FastMath.floor((float) pageLetter / page.tiles) *
                                page.tileSize;
                        tw = page.tileSize;
                        th = page.tileSize;
                    }
                    mesh.color(r, g, b, a);
                    mesh.texture(tx, ty);
                    mesh.vertex(xxx, yyy, 0.0f);
                    mesh.texture(tx, ty + th);
                    mesh.vertex(xxx, yyy + h, 0.0f);
                    mesh.texture(tx + tw, ty + th);
                    mesh.vertex(xxx + w, yyy + h, 0.0f);
                    mesh.texture(tx + tw, ty);
                    mesh.vertex(xxx + w, yyy, 0.0f);
                }
                xx += actualWidth;
                length++;
            }
        }
        TextVAO[] vaos = new TextVAO[meshes.size()];
        int i = 0;
        for (Map.Entry<Integer, Mesh> entry : meshes.entrySet()) {
            vaos[i++] = new TextVAO(entry.getValue().finish(),
                    pages[entry.getKey()].texture);
        }
        return new Text(vaos, length);
    }

    @OpenGLFunction
    public void dispose(GL gl) {
        Arrays.stream(pages).filter(Objects::nonNull)
                .forEach(page -> page.texture.dispose(gl));
        glyphRenderer.dispose();
    }

    public static class Text {
        private final TextVAO[] vaos;
        private final int length;

        private Text() {
            this(EMPTY_TEXT_VAO, 0);
        }

        private Text(TextVAO[] vaos, int length) {
            this.vaos = vaos;
            this.length = length;
        }

        @OpenGLFunction
        public void render(GL gl, Shader shader) {
            render(gl, shader, true);
        }

        @OpenGLFunction
        public void render(GL gl, Shader shader, boolean textured) {
            Arrays.stream(vaos).forEach(vao -> {
                if (textured) {
                    vao.texture.bind(gl);
                }
                vao.vao.render(gl, shader);
            });
        }

        public int length() {
            return length;
        }
    }

    private static class TextVAO {
        private final VAO vao;
        private final Texture texture;

        private TextVAO(VAO vao, Texture texture) {
            this.vao = vao;
            this.texture = texture;
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
