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

import ar.com.hjg.pngj.*;
import org.tobi29.scapes.engine.utils.io.ByteStreamInputStream;
import org.tobi29.scapes.engine.utils.io.ByteStreamOutputStream;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.function.IntFunction;

public final class PNG {
    private PNG() {
    }

    public static Image decode(ReadableByteStream stream,
            IntFunction<ByteBuffer> supplier) throws IOException {
        return decode(new ByteStreamInputStream(stream), supplier);
    }

    public static Image decode(InputStream streamIn,
            IntFunction<ByteBuffer> supplier) throws IOException {
        try {
            PngReaderByte reader = new PngReaderByte(streamIn);
            int width = reader.imgInfo.cols;
            int height = reader.imgInfo.rows;
            boolean fillAlpha = !reader.imgInfo.alpha;
            ByteBuffer buffer = supplier.apply(width * height << 2);
            if (fillAlpha) {
                for (int i = 0; i < height; i++) {
                    ImageLineByte line = reader.readRowByte();
                    byte[] array = line.getScanlineByte();
                    int j = 0;
                    while (j < array.length) {
                        buffer.put(array[j++]);
                        buffer.put(array[j++]);
                        buffer.put(array[j++]);
                        buffer.put((byte) 0xFF);
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    ImageLineByte line = reader.readRowByte();
                    buffer.put(line.getScanlineByte());
                }
            }
            reader.end();
            buffer.rewind();
            return new Image(width, height, buffer);
        } catch (PngjException e) {
            throw new IOException(e);
        }
    }

    public static void encode(Image image, WritableByteStream stream, int level,
            boolean alpha) throws IOException {
        encode(image, new ByteStreamOutputStream(stream), level, alpha);
    }

    public static void encode(Image image, OutputStream streamOut, int level,
            boolean alpha) throws IOException {
        try {
            int width = image.width();
            int height = image.height();
            ByteBuffer buffer = image.buffer();
            ImageInfo info = new ImageInfo(width, height, 8, alpha);
            PngWriter writer = new PngWriter(streamOut, info);
            writer.setCompLevel(level);
            ImageLineByte line = new ImageLineByte(info);
            byte[] scanline = line.getScanline();
            if (alpha) {
                for (int y = height - 1; y >= 0; y--) {
                    buffer.position(y * width << 2);
                    int x = 0;
                    while (x < scanline.length) {
                        scanline[x++] = buffer.get();
                        scanline[x++] = buffer.get();
                        scanline[x++] = buffer.get();
                        scanline[x++] = buffer.get();
                    }
                    writer.writeRow(line);
                }
            } else {
                for (int y = height - 1; y >= 0; y--) {
                    buffer.position(y * width << 2);
                    int x = 0;
                    while (x < scanline.length) {
                        scanline[x++] = buffer.get();
                        scanline[x++] = buffer.get();
                        scanline[x++] = buffer.get();
                        buffer.get();
                    }
                    writer.writeRow(line);
                }
            }
            writer.end();
        } catch (PngjException e) {
            throw new IOException(e);
        }
    }
}
