/*
 * Copyright 2012-2017 Tobi29
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
package org.tobi29.scapes.engine.utils.graphics

import org.tobi29.scapes.engine.utils.PlatformName
import org.tobi29.scapes.engine.utils.io.ByteBuffer
import org.tobi29.scapes.engine.utils.math.max

/**
 * Creates an array of [ByteBuffer] containing mipmap
 * textures from the given source texture
 * @param buffer   [ByteBuffer] containing texture data in RGBA format
 * @param supplier Supplier for [ByteBuffer] instances
 * @param width    Width of source texture in pixels
 * @param height   Height of source texture in pixels
 * @param mipmaps  Amount of mipmap levels, resulting array will be n + 1 in size
 * @param alpha    Whether or not to allow transparent borders or harsh ones
 * @return An array of [ByteBuffer] containing the mipmap textures
 */
@PlatformName("generateMipMapsNullable")
fun generateMipMaps(buffer: ByteBuffer?,
                    supplier: (Int) -> ByteBuffer,
                    width: Int,
                    height: Int,
                    mipmaps: Int,
                    alpha: Boolean): Array<ByteBuffer?> {
    val buffers = arrayOfNulls<ByteBuffer>(mipmaps + 1)
    if (buffer == null) {
        return buffers
    }
    buffers[mipmaps] = generateMipMap(buffer, supplier, width, height, mipmaps,
            alpha)
    for (i in mipmaps - 1 downTo 0) {
        buffers[i] = generateMipMap(buffer, supplier, width, height, i, alpha,
                buffers[i + 1], 1)
    }
    return buffers
}

/**
 * Creates an array of [ByteBuffer] containing mipmap
 * textures from the given source texture
 * @param buffer   [ByteBuffer] containing texture data in RGBA format
 * @param supplier Supplier for [ByteBuffer] instances
 * @param width    Width of source texture in pixels
 * @param height   Height of source texture in pixels
 * @param mipmaps  Amount of mipmap levels, resulting array will be n + 1 in size
 * @param alpha    Whether or not to allow transparent borders or harsh ones
 * @return An array of [ByteBuffer] containing the mipmap textures
 */
fun generateMipMaps(buffer: ByteBuffer,
                    supplier: (Int) -> ByteBuffer,
                    width: Int,
                    height: Int,
                    mipmaps: Int,
                    alpha: Boolean): Array<ByteBuffer> {
    val buffers = arrayOfNulls<ByteBuffer>(mipmaps + 1)
    buffers[mipmaps] = generateMipMap(buffer, supplier, width, height, mipmaps,
            alpha)
    for (i in mipmaps - 1 downTo 0) {
        buffers[i] = generateMipMap(buffer, supplier, width, height, i, alpha,
                buffers[i + 1], 1)
    }
    @Suppress("UNCHECKED_CAST")
    return buffers as Array<ByteBuffer>
}

/**
 * Creates a mipmap of given level from the given texture
 * @param buffer         [ByteBuffer] containing texture data in RGBA format
 * @param width          Width of source texture in pixels
 * @param height         Height of source texture in pixels
 * @param scaleBits      Scale for the mipmap texture given as bit-shift value
 * @param lower          Optional [ByteBuffer] to fetch data from when the source has invisible pixels
 * @param lowerScaleBits Scale of the lower texture in comparison to the mipmap texture as bit-shift value
 * @param alpha          Whether or not to allow transparent borders or harsh ones
 * @return A [ByteBuffer] containing the mipmap texture
 */
fun generateMipMap(buffer: ByteBuffer,
                   supplier: (Int) -> ByteBuffer,
                   width: Int,
                   height: Int,
                   scaleBits: Int,
                   alpha: Boolean,
                   lower: ByteBuffer? = null,
                   lowerScaleBits: Int = 0): ByteBuffer {
    val offset = buffer.position()
    val offsetLower: Int
    if (lower == null) {
        offsetLower = 0
    } else {
        offsetLower = lower.position()
    }
    val scale = 1 shl scaleBits
    val widthScaled = width shr scaleBits
    val heightScaled = height shr scaleBits
    val mipmap = supplier(max(widthScaled, 1) * max(heightScaled, 1) shl 2)
    val samples = 1 shl (scaleBits shl 1)
    val minVisible = samples shr 1
    val lowerWidth = widthScaled shr lowerScaleBits
    for (y in 0..heightScaled - 1) {
        val yy = y shl scaleBits
        for (x in 0..widthScaled - 1) {
            val xx = x shl scaleBits
            var r = 0
            var g = 0
            var b = 0
            var a = 0
            var visible = 0
            var div = 0
            for (yyy in 0..scale - 1) {
                var i = ((yy + yyy) * width + xx shl 2) + offset
                for (xxx in 0..scale - 1) {
                    val sampleR = buffer.get(i++).toInt() and 0xFF
                    val sampleG = buffer.get(i++).toInt() and 0xFF
                    val sampleB = buffer.get(i++).toInt() and 0xFF
                    val sampleA = buffer.get(i++).toInt() and 0xFF
                    if (sampleA != 0) {
                        r += sampleR
                        g += sampleG
                        b += sampleB
                        if (alpha) {
                            a += sampleA
                        } else {
                            a = max(a, sampleA)
                            visible++
                        }
                        div++
                    }
                }
            }
            if (div > 0) {
                r /= div
                g /= div
                b /= div
                if (alpha) {
                    a /= samples
                } else if (visible < minVisible) {
                    a = 0
                }
            }
            if (a == 0 && lower != null) {
                var i = ((y shr lowerScaleBits) * lowerWidth + (x shr lowerScaleBits) shl 2) + offsetLower
                mipmap.put(lower.get(i++))
                mipmap.put(lower.get(i++))
                mipmap.put(lower.get(i))
                mipmap.put(0.toByte())
            } else {
                mipmap.put(r.toByte())
                mipmap.put(g.toByte())
                mipmap.put(b.toByte())
                mipmap.put(a.toByte())
            }
        }
    }
    mipmap.rewind()
    return mipmap
}
