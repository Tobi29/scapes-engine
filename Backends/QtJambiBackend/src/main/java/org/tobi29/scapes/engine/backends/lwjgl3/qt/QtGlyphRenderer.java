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
package org.tobi29.scapes.engine.backends.lwjgl3.qt;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.*;
import org.tobi29.scapes.engine.gui.GlyphRenderer;
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.nio.ByteBuffer;

public class QtGlyphRenderer implements GlyphRenderer {
    private static final byte[] WHITE = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    private final String fontName;
    private final int tiles, pageTiles, pageTileBits, pageTileMask, size,
            glyphSize, imageSize, renderX, renderY;
    private final float tileSize;

    public QtGlyphRenderer(String fontName, int size) {
        this.fontName = fontName;
        this.size = size;
        int tileBits = 3;
        tiles = 1 << tileBits;
        pageTileBits = tileBits << 1;
        pageTileMask = (1 << pageTileBits) - 1;
        pageTiles = 1 << pageTileBits;
        tileSize = 1.0f / tiles;
        glyphSize = size << 1;
        imageSize = glyphSize << tileBits;
        renderX = FastMath.round(size * 0.25);
        renderY = FastMath.round(size * 1.5);
    }

    @Override
    public GlyphPage page(int id) {
        float[] width = new float[pageTiles];
        QImage image = new QImage(new QSize(imageSize, imageSize),
                QImage.Format.Format_ARGB32);
        image.fill(0);
        QFont font = new QFont(fontName, size);
        QFontMetricsF fontMetrics = new QFontMetricsF(font);
        QPainter p = new QPainter(image);
        p.setRenderHint(QPainter.RenderHint.TextAntialiasing, true);
        p.setPen(new QPen(QColor.fromRgb(0xFFFFFF)));
        p.setFont(font);
        int i = 0;
        int offset = id << pageTileBits;
        for (int y = 0; y < tiles; y++) {
            int yy = y * glyphSize + renderY;
            for (int x = 0; x < tiles; x++) {
                int xx = x * glyphSize + renderX;
                char c = (char) (i + offset);
                String str = new String(new char[]{c});
                p.drawText(xx, yy, str);
                width[i++] = (float) (fontMetrics.width(c) * 0.75 / size);
            }
        }
        p.end();
        ByteBuffer buffer = BufferCreator.bytes(imageSize * imageSize << 2);
        QImage alpha = image.alphaChannel();
        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                buffer.put(WHITE);
                buffer.put((byte) alpha.pixel(x, y));
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
}
