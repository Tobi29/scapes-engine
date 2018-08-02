/*
 * Copyright 2012-2018 Tobi29
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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.graphics.png

import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.HeapBytes
import org.tobi29.arrays.sliceOver
import org.tobi29.graphics.Bitmap
import org.tobi29.graphics.Ints2ByteArrayBitmap
import org.tobi29.graphics.RGBA
import org.tobi29.io.*
import org.tobi29.io.compression.deflate.InflateHandle
import org.tobi29.io.compression.deflate.filteredBytes
import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.combineToShort
import org.tobi29.stdex.maskAt
import org.tobi29.stdex.splitToBytes

suspend inline fun decodePng(
    asset: ReadSource,
    maxLength: Int = 64 shl 20 // 64 MiB
): Bitmap<*, *> = asset.readAsync { decodePng(it, maxLength) }

fun decodePng(
    stream: ReadableByteStream,
    maxLength: Int = 64 shl 20 // 64 MiB
): Bitmap<*, *> = InflateHandle().use { inflater ->
    decodePng(stream, inflater, maxLength)
}

fun decodePng(
    stream: ReadableByteStream,
    inflate: InflateHandle,
    maxLength: Int = 64 shl 20 // 64 MiB
): Bitmap<*, *> {
    val pngHeader = ByteArray(PNG_HEADER.size)
    stream.get(pngHeader.sliceOver())
    if (!pngHeader.contentEquals(PNG_HEADER))
        throw IOException("Incorrect header")
    var array = ByteArray(0)

    // Set by IHDR
    var header = false
    var width = 0
    var height = 0
    var bitDepth: Byte = 0
    var colorType: Byte = 0
    var compressionMethod: Byte = 0
    var filterMethod: Byte = 0
    var interlaceMethod: Byte = 0

    // Set by PLTE
    var palette: IntArray? = null

    // Set by IDAT
    var bitmap: Bitmap<*, *>? = null
    var decode: (() -> HeapBytes)? = null
    var finish: (() -> Unit)? = null
    var scanline: HeapBytes? = null
    var scanlineIncomplete = false

    // Set by IEND
    var end = false

    while (!end) {
        stream.readChunk(array, maxLength) { type, chunk ->
            array = chunk.array
            when (type) {
                TYPE_IHDR -> {
                    if (header)
                        throw IOException("Second IHDR chunk")
                    if (chunk.size != 13)
                        throw IOException("Invalid IHDR length: ${chunk.size}")
                    header = true
                    chunk.viewBE.apply {
                        width = getInt(0)
                        height = getInt(4)
                        bitDepth = getByte(8)
                        colorType = getByte(9)
                        compressionMethod = getByte(10)
                        filterMethod = getByte(11)
                        interlaceMethod = getByte(12)
                    }
                }
                TYPE_PLTE -> {
                    if (palette != null)
                        throw IOException("Second PLTE chunk")
                    if (!header)
                        throw IOException("PLTE before IHDR chunk")
                    val entryCount = chunk.size / 3
                    if (entryCount * 3 != chunk.size ||
                        entryCount < 0 ||
                        entryCount > valuesForBitDepth(bitDepth))
                        throw IOException("Invalid PLTE length: ${chunk.size}")
                    palette = IntArray(entryCount) { i ->
                        combineToInt(
                            chunk[i * 3 + 0],
                            chunk[i * 3 + 1],
                            chunk[i * 3 + 2],
                            0xFF.toByte()
                        ) // RGBA
                    }
                }
                TYPE_IDAT -> {
                    if (!header)
                        throw IOException("IDAT before IHDR chunk")
                    if (decode == null) {
                        checkConfig(
                            width,
                            height,
                            bitDepth,
                            colorType,
                            compressionMethod,
                            filterMethod,
                            interlaceMethod,
                            palette
                        )
                        val (a, b, c) = initDecode(
                            width,
                            height,
                            bitDepth,
                            colorType,
                            filterMethod,
                            interlaceMethod,
                            palette
                        )
                        bitmap = a
                        decode = b
                        finish = c
                        scanline = decode!!()
                    }
                    inflate.input(chunk)
                    while (true) {
                        val size = inflate.process(scanline!!)
                        if (size == 0) break
                        val bytes = filteredBytes(size)
                        scanline = scanline!!.slice(bytes)
                        if (scanline!!.size == 0) {
                            scanline = decode!!()
                            scanlineIncomplete = false
                        } else if (bytes != 0) {
                            scanlineIncomplete = true
                        }
                        if (size < 0) break
                    }
                }
                TYPE_IEND -> {
                    end = true
                }
                else -> if (type.maskAt(5 + 3 * 8)) {
                    // Ignore
                } else {
                    throw IOException("Unknown required chunk type: $type")
                }
            }
        }
    }
    if (decode == null)
        throw IOException("No IDAT chunk")
    while (true) {
        val size = inflate.processFinish(scanline!!)
        val bytes = filteredBytes(size)
        scanline = scanline!!.slice(bytes)
        if (scanline!!.size == 0) {
            scanline = decode!!()
            scanlineIncomplete = false
        } else if (bytes != 0) {
            scanlineIncomplete = true
        }
        if (size < 0) break
    }
    if (scanlineIncomplete)
        throw IOException("Incomplete scanline")
    finish!!()
    return bitmap!!
}

private fun checkConfig(
    width: Int,
    height: Int,
    bitDepth: Byte,
    colorType: Byte,
    compressionMethod: Byte,
    filterMethod: Byte,
    interlaceMethod: Byte,
    palette: IntArray?
) {
    if (width < 0)
        throw IOException("Negative width: $width")
    if (height < 0)
        throw IOException("Negative width: $width")
    if (colorType != 0.toByte() &&
        colorType != 2.toByte() &&
        colorType != 3.toByte() &&
        colorType != 4.toByte() &&
        colorType != 6.toByte())
        throw IOException("Invalid color-type: $colorType")
    if ((colorType == 0.toByte() &&
                bitDepth != 1.toByte() &&
                bitDepth != 2.toByte() &&
                bitDepth != 4.toByte() &&
                bitDepth != 8.toByte() &&
                bitDepth != 16.toByte()) ||
        (colorType == 2.toByte() &&
                bitDepth != 8.toByte() &&
                bitDepth != 16.toByte()) ||
        (colorType == 3.toByte() &&
                bitDepth != 1.toByte() &&
                bitDepth != 2.toByte() &&
                bitDepth != 4.toByte() &&
                bitDepth != 8.toByte()) ||
        (colorType == 4.toByte() &&
                bitDepth != 8.toByte() &&
                bitDepth != 16.toByte()) ||
        (colorType == 6.toByte() &&
                bitDepth != 8.toByte() &&
                bitDepth != 16.toByte()))
        throw IOException("Invalid bit-depth for color-type $colorType: $bitDepth")
    if (compressionMethod != 0.toByte())
        throw IOException("Invalid compression-method: $compressionMethod")
    if (filterMethod != 0.toByte())
        throw IOException("Invalid filter-method: $filterMethod")
    if (interlaceMethod != 0.toByte() &&
        interlaceMethod != 1.toByte())
        throw IOException("Invalid interlace-method: $interlaceMethod")
    if (colorType == 3.toByte() &&
        palette == null)
        throw IOException("No PLTE for color-type $colorType")
}

private fun initDecode(
    width: Int,
    height: Int,
    bitDepth: Byte,
    colorType: Byte,
    filterMethod: Byte,
    interlaceMethod: Byte,
    palette: IntArray?
): Triple<Bitmap<*, *>, () -> HeapBytes, () -> Unit> {
    // TODO: Instead of converting here, consider exposing formats directly
    val bitmap = Ints2ByteArrayBitmap(width, height, RGBA)
    val channels = channelsForColorType(colorType)
    val distance = distanceForBitDepth(bitDepth, channels)
    return when (interlaceMethod) {
        0.toByte() -> initDecodeInterlaceNone(
            bitmap,
            width,
            height,
            channels,
            distance,
            bitDepth,
            colorType,
            filterMethod,
            palette
        )
        1.toByte() -> initDecodeInterlaceAdam7(
            bitmap,
            width,
            height,
            channels,
            distance,
            bitDepth,
            colorType,
            filterMethod,
            palette
        )
        else -> throw IllegalArgumentException("Invalid interlace method: $interlaceMethod")
    }
}

private fun initDecodeInterlaceNone(
    bitmap: Ints2ByteArrayBitmap<RGBA>,
    width: Int,
    height: Int,
    channels: Int,
    distance: Int,
    bitDepth: Byte,
    colorType: Byte,
    filterMethod: Byte,
    palette: IntArray?
): Triple<Bitmap<*, *>, () -> HeapBytes, () -> Unit> {
    var y = -1
    val scanlineSize = scanlineSize(bitDepth, width, channels)
        .let { if (it > 0) it + 1 else 0 }
    var scanline1 = ByteArray(scanlineSize).sliceOver()
    var scanline2 = ByteArray(scanlineSize).sliceOver()
    return Triple(bitmap, {
        if (y >= height)
            throw IOException("Too many scanlines")
        if (y >= 0 && scanline1.size > 0) {
            val filterType = scanline1[0]
            try {
                pngUnfilter(
                    scanline2.slice(1),
                    scanline1.slice(1),
                    distance,
                    filterMethod,
                    filterType
                )
                parsePixels(
                    scanline1.slice(1), bitDepth, colorType, palette,
                    4,
                    bitmap.data.array.array.slice(
                        y * width * 4,
                        width * 4
                    )
                )
            } catch (e: IllegalArgumentException) {
                throw IOException(e.message ?: "")
            }
            scanline2 = scanline1.also { scanline1 = scanline2 }
        }
        y++
        scanline1
    }, {
        if (y != height)
            throw IOException("Missing scanlines")
    })
}

private fun initDecodeInterlaceAdam7(
    bitmap: Ints2ByteArrayBitmap<RGBA>,
    width: Int,
    height: Int,
    channels: Int,
    distance: Int,
    bitDepth: Byte,
    colorType: Byte,
    filterMethod: Byte,
    palette: IntArray?
): Triple<Bitmap<*, *>, () -> HeapBytes, () -> Unit> {
    var pass = -1
    var y = -1
    var scanlines = 0
    val scanlineSizeMax = scanlineSize(bitDepth, width, channels)
        .let { if (it > 0) it + 1 else 0 }
    val scanlineA = ByteArray(scanlineSizeMax).sliceOver()
    val scanlineB = ByteArray(scanlineSizeMax).sliceOver()
    var scanline1 = scanlineA
    var scanline2 = scanlineB
    return Triple(bitmap, {
        if (y >= 0 && scanline1.size > 0) {
            val filterType = scanline1[0]
            try {
                pngUnfilter(
                    scanline2.slice(1),
                    scanline1.slice(1),
                    distance,
                    filterMethod,
                    filterType
                )
                val step = stepForAdam7Pass(pass) * 4
                val offset = offsetForAdam7Pass(y, width, pass) * 4
                parsePixels(
                    scanline1.slice(1), bitDepth, colorType, palette,
                    step,
                    bitmap.data.array.array.slice(
                        offset,
                        (width * 4).coerceAtMost(
                            bitmap.data.array.array.size - offset
                        )
                    )
                )
            } catch (e: IllegalArgumentException) {
                throw IOException(e.message ?: "")
            }
            scanline2 = scanline1.also { scanline1 = scanline2 }
        }
        if (y >= scanlines - 1) {
            if (pass >= 7)
                throw IOException("Too many scanlines")
            y = 0
            while (true) {
                pass++
                if (pass != 7) {
                    val scanlineWidth =
                        scanlineWidthForAdam7Pass(width, pass)
                    scanlines = scanlinesForAdam7Pass(height, pass)
                    val scanlineSize =
                        scanlineSize(bitDepth, scanlineWidth, channels)
                            .let { if (scanlines > 0 && it > 0) it + 1 else 0 }
                    scanline1 = scanlineA.slice(0, scanlineSize)
                    scanline2 = scanlineB.slice(0, scanlineSize)
                    if (scanlineSize <= 0 && pass != 6) continue
                }
                break
            }
        } else y++
        scanline1
    }, {
        if (pass < 7)
            throw IOException("Missing scanlines")
    })
}

private inline fun valuesForBitDepth(bitDepth: Byte): Int =
    1 shl (bitDepth.toInt() and 0xFF)

private inline fun scanlineSize(
    bitDepth: Byte,
    width: Int,
    channels: Int
): Int = (width * channels).let { elements ->
    when (bitDepth) {
        1.toByte() -> (elements + 7) ushr 3
        2.toByte() -> (elements + 3) ushr 2
        4.toByte() -> (elements + 1) ushr 1
        8.toByte() -> elements
        16.toByte() -> elements shl 1
        else -> throw IllegalArgumentException("Invalid bit-depth: $bitDepth")
    }
}

private inline fun scanlineWidthForAdam7Pass(
    width: Int,
    pass: Int
) = when (pass) {
    0 -> (width + 7) ushr 3
    1 -> (width + 3) ushr 3
    2 -> (width + 3) ushr 2
    3 -> (width + 1) ushr 2
    4 -> (width + 1) ushr 1
    5 -> (width + 0) ushr 1
    6 -> (width + 0) ushr 0
    else -> throw IllegalArgumentException("Invalid pass: $pass")
}

private inline fun scanlinesForAdam7Pass(
    height: Int,
    pass: Int
) = when (pass) {
    0 -> (height + 7) ushr 3
    1 -> (height + 7) ushr 3
    2 -> (height + 3) ushr 3
    3 -> (height + 3) ushr 2
    4 -> (height + 1) ushr 2
    5 -> (height + 1) ushr 1
    6 -> (height + 0) ushr 1
    else -> throw IllegalArgumentException("Invalid pass: $pass")
}

private inline fun stepForAdam7Pass(
    pass: Int
) = when (pass) {
    0 -> 8
    1 -> 8
    2 -> 4
    3 -> 4
    4 -> 2
    5 -> 2
    6 -> 1
    else -> throw IllegalArgumentException("Invalid pass: $pass")
}

private inline fun offsetForAdam7Pass(
    y: Int,
    width: Int,
    pass: Int
) = when (pass) {
    0 -> ((y shl 3) + 0) * width + 0
    1 -> ((y shl 3) + 0) * width + 4
    2 -> ((y shl 3) + 4) * width + 0
    3 -> ((y shl 2) + 0) * width + 2
    4 -> ((y shl 2) + 2) * width + 0
    5 -> ((y shl 1) + 0) * width + 1
    6 -> ((y shl 1) + 1) * width + 0
    else -> throw IllegalArgumentException("Invalid pass: $pass")
}

private inline fun distanceForBitDepth(
    bitDepth: Byte,
    channels: Int
): Int = (when (bitDepth) {
    1.toByte() -> 1
    2.toByte() -> 1
    4.toByte() -> 1
    8.toByte() -> 1
    16.toByte() -> 2
    else -> throw IllegalArgumentException("Invalid bit-depth: $bitDepth")
}) * channels

private inline fun channelsForColorType(
    colorType: Byte
): Int = when (colorType) {
    0.toByte() -> 1
    2.toByte() -> 3
    3.toByte() -> 1
    4.toByte() -> 2
    6.toByte() -> 4
    else -> throw IllegalArgumentException("Invalid color-type: $colorType")
}

private fun parsePixels(
    scanline: BytesRO,
    bitDepth: Byte,
    colorType: Byte,
    palette: IntArray?,
    step: Int,
    output: HeapBytes
) {
    var i = 0
    when (colorType) {
        0.toByte() -> parsePixels1(scanline, bitDepth) { v ->
            val vb = scalePixelTo(bitDepth, 8, v).toByte()
            if (i < output.size) output[i++] = vb
            if (i < output.size) output[i++] = vb
            if (i < output.size) output[i++] = vb
            if (i < output.size) output[i++] = 0xFF.toByte()
            i += step - 4
        }
        2.toByte() -> parsePixels3(scanline, bitDepth) { r, g, b ->
            val rb = scalePixelTo(bitDepth, 8, r).toByte()
            val gb = scalePixelTo(bitDepth, 8, g).toByte()
            val bb = scalePixelTo(bitDepth, 8, b).toByte()
            if (i < output.size) output[i++] = rb
            if (i < output.size) output[i++] = gb
            if (i < output.size) output[i++] = bb
            if (i < output.size) output[i++] = 0xFF.toByte()
            i += step - 4
        }
        3.toByte() -> parsePixels1(scanline, bitDepth) { p ->
            if (p !in palette!!.indices)
                throw IllegalArgumentException("Palette index out of bounds: $p")
            palette[p].splitToBytes { rb, gb, bb, ab ->
                if (i < output.size) output[i++] = rb
                if (i < output.size) output[i++] = gb
                if (i < output.size) output[i++] = bb
                if (i < output.size) output[i++] = ab
                i += step - 4
            }
        }
        4.toByte() -> parsePixels2(scanline, bitDepth) { v, a ->
            val vb = scalePixelTo(bitDepth, 8, v).toByte()
            val ab = scalePixelTo(bitDepth, 8, a).toByte()
            if (i < output.size) output[i++] = vb
            if (i < output.size) output[i++] = vb
            if (i < output.size) output[i++] = vb
            if (i < output.size) output[i++] = ab
            i += step - 4
        }
        6.toByte() -> parsePixels4(scanline, bitDepth) { r, g, b, a ->
            val rb = scalePixelTo(bitDepth, 8, r).toByte()
            val gb = scalePixelTo(bitDepth, 8, g).toByte()
            val bb = scalePixelTo(bitDepth, 8, b).toByte()
            val ab = scalePixelTo(bitDepth, 8, a).toByte()
            if (i < output.size) output[i++] = rb
            if (i < output.size) output[i++] = gb
            if (i < output.size) output[i++] = bb
            if (i < output.size) output[i++] = ab
            i += step - 4
        }
        else -> throw IllegalArgumentException("Invalid color-type: $colorType")
    }
}

private inline fun parsePixels1(
    scanline: BytesRO,
    bitDepth: Byte,
    output: (Int) -> Unit
) = when (bitDepth) {
    1.toByte() -> {
        for (i in 0 until scanline.size) {
            var v = scanline[i].toInt() and 0xFF
            repeat(8) {
                output(
                    (v ushr 0) and 0x1
                )
                v = v ushr 1
            }
        }
    }
    2.toByte() -> {
        for (i in 0 until scanline.size) {
            var v = scanline[i].toInt() and 0xFF
            repeat(4) {
                output(
                    (v ushr 0) and 0x3
                )
                v = v ushr 2
            }
        }
    }
    4.toByte() -> {
        for (i in 0 until scanline.size) {
            var v = scanline[i].toInt() and 0xFF
            repeat(2) {
                output(
                    (v ushr 0) and 0xF
                )
                v = v ushr 4
            }
        }
    }
    8.toByte() -> for (i in 0..(scanline.size - 1) step 1) {
        output(
            scanline[i + 0].toInt() and 0xFF
        )
    }
    16.toByte() -> for (i in 0..(scanline.size - 2) step 2) {
        output(
            combineToShort(
                scanline[i + 0],
                scanline[i + 1]
            ).toInt() and 0xFFFF
        )
    }
    else -> throw IllegalArgumentException("Invalid bit-depth: $bitDepth")
}

private inline fun parsePixels2(
    scanline: BytesRO,
    bitDepth: Byte,
    output: (Int, Int) -> Unit
) = when (bitDepth) {
    1.toByte() -> {
        for (i in 0 until scanline.size) {
            var v = scanline[i].toInt() and 0xFF
            repeat(4) {
                output(
                    (v ushr 1) and 0x1,
                    (v ushr 0) and 0x1
                )
                v = v ushr 2
            }
        }
    }
    2.toByte() -> {
        for (i in 0 until scanline.size) {
            var v = scanline[i].toInt() and 0xFF
            repeat(2) {
                output(
                    (v ushr 2) and 0x3,
                    (v ushr 0) and 0x3
                )
                v = v ushr 4
            }
        }
    }
    4.toByte() -> {
        for (i in 0 until scanline.size) {
            val v = scanline[i].toInt() and 0xFF
            output(
                (v ushr 4) and 0xF,
                (v ushr 0) and 0xF
            )
        }
    }
    8.toByte() -> for (i in 0..(scanline.size - 2) step 2) {
        output(
            scanline[i + 0].toInt() and 0xFF,
            scanline[i + 1].toInt() and 0xFF
        )
    }
    16.toByte() -> for (i in 0..(scanline.size - 4) step 4) {
        output(
            combineToShort(
                scanline[i + 0],
                scanline[i + 1]
            ).toInt() and 0xFFFF,
            combineToShort(
                scanline[i + 2],
                scanline[i + 3]
            ).toInt() and 0xFFFF
        )
    }
    else -> throw IllegalArgumentException("Invalid bit-depth: $bitDepth")
}

private inline fun parsePixels3(
    scanline: BytesRO,
    bitDepth: Byte,
    output: (Int, Int, Int) -> Unit
) = when (bitDepth) {
    8.toByte() -> for (i in 0..(scanline.size - 3) step 3) {
        output(
            scanline[i + 0].toInt() and 0xFF,
            scanline[i + 1].toInt() and 0xFF,
            scanline[i + 2].toInt() and 0xFF
        )
    }
    16.toByte() -> for (i in 0..(scanline.size - 6) step 6) {
        output(
            combineToShort(
                scanline[i + 0],
                scanline[i + 1]
            ).toInt() and 0xFFFF,
            combineToShort(
                scanline[i + 2],
                scanline[i + 3]
            ).toInt() and 0xFFFF,
            combineToShort(
                scanline[i + 4],
                scanline[i + 5]
            ).toInt() and 0xFFFF
        )
    }
    else -> throw IllegalArgumentException("Invalid bit-depth: $bitDepth")
}

private inline fun parsePixels4(
    scanline: BytesRO,
    bitDepth: Byte,
    output: (Int, Int, Int, Int) -> Unit
) = when (bitDepth) {
    8.toByte() -> for (i in 0..(scanline.size - 4) step 4) {
        output(
            scanline[i + 0].toInt() and 0xFF,
            scanline[i + 1].toInt() and 0xFF,
            scanline[i + 2].toInt() and 0xFF,
            scanline[i + 3].toInt() and 0xFF
        )
    }
    16.toByte() -> for (i in 0..(scanline.size - 8) step 8) {
        output(
            combineToShort(
                scanline[i + 0],
                scanline[i + 1]
            ).toInt() and 0xFFFF,
            combineToShort(
                scanline[i + 2],
                scanline[i + 3]
            ).toInt() and 0xFFFF,
            combineToShort(
                scanline[i + 4],
                scanline[i + 5]
            ).toInt() and 0xFFFF,
            combineToShort(
                scanline[i + 6],
                scanline[i + 7]
            ).toInt() and 0xFFFF
        )
    }
    else -> throw IllegalArgumentException("Invalid bit-depth: $bitDepth")
}

private inline fun scalePixelTo(
    bitDepth: Byte,
    destinationBitDepth: Byte,
    v: Int
): Int = when {
    bitDepth < destinationBitDepth -> v shl (destinationBitDepth - bitDepth)
    bitDepth > destinationBitDepth -> v ushr (bitDepth - destinationBitDepth)
    else -> v
}
