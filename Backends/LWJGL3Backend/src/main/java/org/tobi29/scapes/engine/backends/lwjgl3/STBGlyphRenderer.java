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

package org.tobi29.scapes.engine.backends.lwjgl3;

import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.tobi29.scapes.engine.gui.GlyphRenderer;
import org.tobi29.scapes.engine.utils.BufferCreatorNative;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.io.ProcessStream;
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class STBGlyphRenderer implements GlyphRenderer {
    private static final Map<String, Pair<ByteBuffer, ByteBuffer>> FONTS =
            new ConcurrentHashMap<>();
    private final ByteBuffer info;
    private final int tiles, pageTiles, pageTileBits, pageTileMask, glyphSize,
            imageSize;
    private final float tileSize, size, scale;
    private final ByteBuffer glyphBuffer;
    private final IntBuffer intBuffer1 = BufferCreatorNative.intsD(1),
            intBuffer2 = BufferCreatorNative.intsD(1), intBuffer3 =
            BufferCreatorNative.intsD(1), intBuffer4 =
            BufferCreatorNative.intsD(1);

    public STBGlyphRenderer(ByteBuffer info, int size) {
        this.info = info;
        this.size = size;
        int tileBits = 3;
        tiles = 1 << tileBits;
        pageTileBits = tileBits << 1;
        pageTileMask = (1 << pageTileBits) - 1;
        pageTiles = 1 << pageTileBits;
        tileSize = 1.0f / tiles;
        glyphSize = size << 1;
        imageSize = glyphSize << tileBits;
        scale = STBTruetype.stbtt_ScaleForMappingEmToPixels(info, size * 1.38f);
        glyphBuffer = BufferCreatorNative.bytesD(glyphSize * glyphSize);
    }

    public static boolean loadFont(ReadSource font) {
        try {
            ByteBuffer buffer =
                    ProcessStream.processSource(font, ProcessStream.asBuffer());
            ByteBuffer fontBuffer =
                    BufferCreatorNative.bytesD(buffer.remaining());
            fontBuffer.put(buffer);
            fontBuffer.flip();
            ByteBuffer infoBuffer = STBTTFontinfo.malloc();
            if (STBTruetype.stbtt_InitFont(infoBuffer, fontBuffer) != 0) {
                // TODO: Seems to work fine for ASCII characters, needs proper testing
                // TODO: This probably leaks the string
                ByteBuffer nameBuffer = STBTruetype
                        .stbtt_GetFontNameString(infoBuffer,
                                STBTruetype.STBTT_PLATFORM_ID_MAC,
                                STBTruetype.STBTT_MAC_EID_ROMAN,
                                STBTruetype.STBTT_MAC_LANG_ENGLISH, 1);
                byte[] nameArray = new byte[nameBuffer.remaining()];
                nameBuffer.get(nameArray);
                String name = new String(nameArray);
                FONTS.put(name, new Pair<>(fontBuffer, infoBuffer));
                return true;
            }
        } catch (IOException e) {
        }
        return false;
    }

    public static GlyphRenderer fromFont(String name, int size) {
        Pair<ByteBuffer, ByteBuffer> font = FONTS.get(name);
        if (font == null) {
            throw new IllegalArgumentException("Unknown font: " + name);
        }
        return new STBGlyphRenderer(font.b, size);
    }

    @Override
    public synchronized GlyphPage page(int id) {
        ByteBuffer buffer =
                BufferCreatorNative.bytesD(imageSize * imageSize << 2);
        float[] width = new float[pageTiles];
        int i = 0;
        int offset = id << pageTileBits;
        for (int y = 0; y < tiles; y++) {
            int yy = y * glyphSize;
            for (int x = 0; x < tiles; x++) {
                int xx = x * glyphSize;
                char c = (char) (i + offset);
                STBTruetype.stbtt_GetCodepointHMetrics(info, c, intBuffer1,
                        intBuffer2);
                float widthX = intBuffer1.get(0) * scale;
                if (!Character.isISOControl(c)) {
                    STBTruetype.stbtt_GetCodepointBox(info, c, intBuffer1,
                            intBuffer2, intBuffer3, intBuffer4);
                    float offsetX = FastMath.max(
                            size * 0.25f + intBuffer1.get(0) * scale, 0.0f);
                    float offsetY = FastMath.max(
                            size * 1.5f - intBuffer4.get(0) * scale, 0.0f);
                    int renderX = FastMath.max(FastMath.round(offsetX), 0);
                    int renderY = FastMath.max(FastMath.round(offsetY), 0);
                    int sizeX = glyphSize - renderX - 1;
                    int sizeY = glyphSize - renderY - 1;
                    renderX += xx;
                    renderY += yy;
                    STBTruetype.stbtt_MakeCodepointBitmap(info, glyphBuffer,
                            glyphSize, glyphSize, glyphSize, scale, scale, c);
                    for (int yyy = 0; yyy < sizeY; yyy++) {
                        buffer.position(
                                (renderY + yyy) * imageSize + renderX << 2);
                        glyphBuffer.position(yyy * glyphSize);
                        for (int xxx = 0; xxx < sizeX; xxx++) {
                            byte value = glyphBuffer.get();
                            buffer.put((byte) 0xFF);
                            buffer.put((byte) 0xFF);
                            buffer.put((byte) 0xFF);
                            buffer.put(value);
                        }
                    }
                    glyphBuffer.rewind();
                }
                width[i++] = widthX / size / 1.3f;
            }
        }
        buffer.rewind();
        return new GlyphPage(buffer, width, imageSize, tiles, tileSize);
    }

    @Override
    public int pageID(char character) {
        return character >> pageTileBits;
    }

    @Override
    public int pageCode(char character) {
        return character & pageTileMask;
    }

    @Override
    public void dispose() {
    }
}
