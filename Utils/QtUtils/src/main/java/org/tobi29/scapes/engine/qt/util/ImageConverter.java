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
package org.tobi29.scapes.engine.qt.util;

import com.trolltech.qt.gui.QImage;
import org.tobi29.scapes.engine.utils.graphics.Image;

import java.nio.ByteBuffer;

public class ImageConverter {
    public static QImage image(Image image) {
        int width = image.width();
        int height = image.height();
        ByteBuffer buffer = image.buffer();
        // Cannot give array directly, because Qt Jambi doesn't make a deep copy of the array
        QImage qImage = new QImage(width, height, QImage.Format.Format_ARGB32);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = buffer.get();
                if (r < 0) {
                    r += 0x100;
                }
                int g = buffer.get();
                if (g < 0) {
                    g += 0x100;
                }
                int b = buffer.get();
                if (b < 0) {
                    b += 0x100;
                }
                int a = buffer.get();
                if (a < 0) {
                    a += 0x100;
                }
                qImage.setPixel(x, y, a << 24 | r << 16 | g << 8 | b);
            }
        }
        return qImage;
    }
}
