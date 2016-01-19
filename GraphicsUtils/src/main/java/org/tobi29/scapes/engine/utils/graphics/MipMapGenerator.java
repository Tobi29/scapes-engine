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

import org.tobi29.scapes.engine.utils.BufferCreatorNative;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.nio.ByteBuffer;

/**
 * Utility class for creating mipmaps
 */
public final class MipMapGenerator {
    private MipMapGenerator() {
    }

    /**
     * Creates an array of {@link ByteBuffer} containing mipmap
     * textures from the given source texture
     *
     * @param buffer  {@link ByteBuffer} containing texture data in RGBA
     *                format
     * @param width   Width of source texture in pixels
     * @param height  Height of source texture in pixels
     * @param mipmaps Amount of mipmap levels, resulting array will be n + 1 in size
     * @param alpha   Whether or not to allow transparent borders or harsh ones
     * @return An array of {@link ByteBuffer} containing the mipmap
     * textures
     */
    public static ByteBuffer[] generateMipMaps(ByteBuffer buffer, int width,
            int height, int mipmaps, boolean alpha) {
        ByteBuffer[] buffers = new ByteBuffer[mipmaps + 1];
        buffers[mipmaps] =
                generateMipMap(buffer, width, height, mipmaps, alpha);
        for (int i = mipmaps - 1; i >= 0; i--) {
            buffers[i] =
                    generateMipMap(buffer, width, height, i, buffers[i + 1], 1,
                            alpha);
        }
        return buffers;
    }

    /**
     * Creates a mipmap of given level from the given texture
     *
     * @param buffer    {@link ByteBuffer} containing texture data in RGBA
     *                  format
     * @param width     Width of source texture in pixels
     * @param height    Height of source texture in pixels
     * @param scaleBits Scale for the mipmap texture given as bit-shift value
     * @param alpha     Whether or not to allow transparent borders or harsh ones
     * @return A {@link ByteBuffer} containing the mipmap texture
     */
    public static ByteBuffer generateMipMap(ByteBuffer buffer, int width,
            int height, int scaleBits, boolean alpha) {
        return generateMipMap(buffer, width, height, scaleBits, null, 0, alpha);
    }

    /**
     * Creates a mipmap of given level from the given texture
     *
     * @param buffer         {@link ByteBuffer} containing texture data in RGBA
     *                       format
     * @param width          Width of source texture in pixels
     * @param height         Height of source texture in pixels
     * @param scaleBits      Scale for the mipmap texture given as bit-shift value
     * @param lower          Optional {@link ByteBuffer} to fetch data from when the
     *                       source has invisible pixels
     * @param lowerScaleBits Scale of the lower texture in comparison to the mipmap texture as
     *                       bit-shift value
     * @param alpha          Whether or not to allow transparent borders or harsh ones
     * @return A {@link ByteBuffer} containing the mipmap texture
     */
    public static ByteBuffer generateMipMap(ByteBuffer buffer, int width,
            int height, int scaleBits, ByteBuffer lower, int lowerScaleBits,
            boolean alpha) {
        int offset = buffer.position();
        int offsetLower;
        if (lower == null) {
            offsetLower = 0;
        } else {
            offsetLower = lower.position();
        }
        int scale = 1 << scaleBits;
        int widthScaled = width >> scaleBits;
        int heightScaled = height >> scaleBits;
        ByteBuffer mipmap = BufferCreatorNative
                .bytes(FastMath.max(widthScaled, 1) *
                        FastMath.max(heightScaled, 1) << 2);
        int samples = 1 << (scaleBits << 1);
        int minVisible = samples >> 1;
        int lowerWidth = widthScaled >> lowerScaleBits;
        for (int y = 0; y < heightScaled; y++) {
            int yy = y << scaleBits;
            for (int x = 0; x < widthScaled; x++) {
                int xx = x << scaleBits;
                int r = 0, g = 0, b = 0, a = 0, visible = 0, div = 0;
                for (int yyy = 0; yyy < scale; yyy++) {
                    int i = ((yy + yyy) * width + xx << 2) + offset;
                    for (int xxx = 0; xxx < scale; xxx++) {
                        int sampleR = buffer.get(i++) & 0xFF;
                        int sampleG = buffer.get(i++) & 0xFF;
                        int sampleB = buffer.get(i++) & 0xFF;
                        int sampleA = buffer.get(i++) & 0xFF;
                        if (sampleA != 0) {
                            r += sampleR;
                            g += sampleG;
                            b += sampleB;
                            if (alpha) {
                                a += sampleA;
                            } else {
                                a = FastMath.max(a, sampleA);
                                visible++;
                            }
                            div++;
                        }
                    }
                }
                if (div > 0) {
                    r /= div;
                    g /= div;
                    b /= div;
                    if (alpha) {
                        a /= samples;
                    } else if (visible < minVisible) {
                        a = 0;
                    }
                }
                if (a == 0 && lower != null) {
                    int i = ((y >> lowerScaleBits) * lowerWidth +
                            (x >> lowerScaleBits) << 2) + offsetLower;
                    mipmap.put(lower.get(i++));
                    mipmap.put(lower.get(i++));
                    mipmap.put(lower.get(i));
                    mipmap.put((byte) 0);
                } else {
                    mipmap.put((byte) r);
                    mipmap.put((byte) g);
                    mipmap.put((byte) b);
                    mipmap.put((byte) a);
                }
            }
        }
        mipmap.rewind();
        return mipmap;
    }
}
