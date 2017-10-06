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

import org.tobi29.scapes.engine.utils.io.ByteView
import org.tobi29.scapes.engine.utils.io.ByteViewRO
import org.tobi29.scapes.engine.utils.math.max

/**
 * Creates an array of [ByteView]s containing mipmap
 * textures from the given source texture
 * @param buffer View containing texture data in RGBA format
 * @param bufferProvider Provider for buffer allocations
 * @param width Width of source texture in pixels
 * @param height Height of source texture in pixels
 * @param mipmaps Amount of mipmap levels, resulting array will be n + 1 in size
 * @param alpha Whether or not to allow transparent borders or harsh ones
 * @return An array of [ByteView]s containing the mipmap textures
 */
inline fun <reified B : ByteView> generateMipMapsNullable(
        buffer: ByteViewRO?,
        noinline bufferProvider: (Int) -> B,
        width: Int,
        height: Int,
        mipmaps: Int,
        alpha: Boolean
): Array<B?> {
    val buffers = arrayOfNulls<B>(mipmaps + 1)
    if (buffer == null) {
        return buffers
    }
    if (mipmaps == 0) {
        buffers[0] = bufferProvider(buffer.size).apply { setBytes(0, buffer) }
    } else {
        buffers[mipmaps] = generateMipMap(buffer, bufferProvider, width, height,
                mipmaps, alpha, null, 1)
        for (i in mipmaps - 1 downTo 0) {
            buffers[i] = generateMipMap(buffer, bufferProvider, width, height,
                    i, alpha, buffers[i + 1], 1)
        }
    }
    return buffers
}

/**
 * Creates an array of [ByteView] containing mipmap
 * textures from the given source texture
 * @param buffer View containing texture data in RGBA format
 * @param bufferProvider Provider for buffer allocations
 * @param width Width of source texture in pixels
 * @param height Height of source texture in pixels
 * @param mipmaps Amount of mipmap levels, resulting array will be n + 1 in size
 * @param alpha Whether or not to allow transparent borders or harsh ones
 * @return An array of [ByteView] containing the mipmap textures
 */
inline fun <reified B : ByteView> generateMipMaps(
        buffer: ByteViewRO,
        noinline bufferProvider: (Int) -> B,
        width: Int,
        height: Int,
        mipmaps: Int,
        alpha: Boolean
): Array<B> {
    @Suppress("UNCHECKED_CAST")
    return generateMipMapsNullable(buffer, bufferProvider, width, height,
            mipmaps, alpha) as Array<B>
}

/**
 * Creates a mipmap of given level from the given texture
 * @param buffer View containing texture data in RGBA format
 * @param bufferProvider Provider for buffer allocations
 * @param width Width of source texture in pixels
 * @param height Height of source texture in pixels
 * @param scaleBits Scale for the mipmap texture given as bit-shift value
 * @param lower Optional [ByteView] to fetch data from when the source has invisible pixels
 * @param lowerScaleBits Scale of the lower texture in comparison to the mipmap texture as bit-shift value
 * @param alpha Whether or not to allow transparent borders or harsh ones
 * @return A [ByteView] containing the mipmap texture
 */
fun <B : ByteView> generateMipMap(buffer: ByteViewRO,
                                                                    bufferProvider: (Int) -> B,
                                                                    width: Int,
                                                                    height: Int,
                                                                    scaleBits: Int,
                                                                    alpha: Boolean,
                                                                    lower: ByteView? = null,
                                                                    lowerScaleBits: Int = 0): B {
    val scale = 1 shl scaleBits
    val widthScaled = width shr scaleBits
    val heightScaled = height shr scaleBits
    val mipmap = bufferProvider(
            max(widthScaled, 1) * max(heightScaled, 1) shl 2)
    val samples = 1 shl (scaleBits shl 1)
    val minVisible = samples shr 1
    val lowerWidth = widthScaled shr lowerScaleBits
    var positionWrite = 0
    for (y in 0 until heightScaled) {
        val yy = y shl scaleBits
        for (x in 0 until widthScaled) {
            val xx = x shl scaleBits
            var r = 0
            var g = 0
            var b = 0
            var a = 0
            var visible = 0
            var div = 0
            for (yyy in 0 until scale) {
                var i = (yy + yyy) * width + xx shl 2
                for (xxx in 0 until scale) {
                    val sampleR = buffer.getByte(i++).toInt() and 0xFF
                    val sampleG = buffer.getByte(i++).toInt() and 0xFF
                    val sampleB = buffer.getByte(i++).toInt() and 0xFF
                    val sampleA = buffer.getByte(i++).toInt() and 0xFF
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
                var i = (y shr lowerScaleBits) * lowerWidth + (x shr lowerScaleBits) shl 2
                mipmap.setByte(positionWrite++, lower.getByte(i++))
                mipmap.setByte(positionWrite++, lower.getByte(i++))
                mipmap.setByte(positionWrite++, lower.getByte(i))
                mipmap.setByte(positionWrite++, 0)
            } else {
                mipmap.setByte(positionWrite++, r.toByte())
                mipmap.setByte(positionWrite++, g.toByte())
                mipmap.setByte(positionWrite++, b.toByte())
                mipmap.setByte(positionWrite++, a.toByte())
            }
        }
    }
    return mipmap
}
