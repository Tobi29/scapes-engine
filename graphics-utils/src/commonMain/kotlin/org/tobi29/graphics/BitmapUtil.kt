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

package org.tobi29.graphics

import org.tobi29.arrays.*
import org.tobi29.io.DefaultMemoryViewProvider
import org.tobi29.io.view
import org.tobi29.stdex.JvmName
import org.tobi29.stdex.combineToInt
import org.tobi29.stdex.splitToBytes

/**
 * Copies from the receiver at the given coordinates into a new image
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param width The width of the new image
 * @param height The height of the new image
 */
inline fun <F : ColorFormat<*>> Bitmap<*, F>.get(
    x: Int,
    y: Int,
    width: Int,
    height: Int
): Bitmap<*, F> =
    get(x, y, width, height, DefaultMemoryViewProvider)

/**
 * Copies from the receiver at the given coordinates into a new image
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param width The width of the new image
 * @param height The height of the new image
 * @param bufferProvider Provider for buffer allocations
 */
@Suppress("UNCHECKED_CAST")
inline fun <F : ColorFormat<*>> Bitmap<*, F>.get(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    bufferProvider: (Int) -> Bytes
): Bitmap<*, F> =
    when (format) {
        is ColorFormatInt -> cast<IntsRO2, ColorFormatInt>()!!
            .get(x, y, width, height, bufferProvider)
        else -> throw IllegalArgumentException("Unsupported format: $format")
    } as Bitmap<*, F>

/**
 * Copies from the receiver at the given coordinates into a new image
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param width The width of the new image
 * @param height The height of the new image
 */
@JvmName("getInt")
inline fun <D : IntsRO2, F : ColorFormatInt> Bitmap<D, F>.get(
    x: Int,
    y: Int,
    width: Int,
    height: Int
): Ints2BytesBitmap<F> =
    get(x, y, width, height, DefaultMemoryViewProvider)

/**
 * Copies from the receiver at the given coordinates into a new image
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param width The width of the new image
 * @param height The height of the new image
 * @param bufferProvider Provider for buffer allocations
 */
@JvmName("getInt")
@Suppress("UNCHECKED_CAST")
inline fun <D : IntsRO2, F : ColorFormatInt> Bitmap<D, F>.get(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    bufferProvider: (Int) -> Bytes
): Ints2BytesBitmap<F> {
    val buffer = bufferProvider(width * height shl 2)
    cast<IntsRO2, ColorFormatInt>()!!.get(x, y, width, height, buffer)
    return Ints2BytesBitmap(buffer, width, height, format)
}

/**
 * Copies from the receiver at the given coordinates into the image
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param image The image to copy into
 */
@JvmName("getInt")
inline fun <D : IntsRO2, F : ColorFormatInt> Bitmap<D, F>.get(
    x: Int,
    y: Int,
    image: Ints2BytesBitmap<F>
) = get(x, y, image.width, image.height, image.data.array.array)

/**
 * Copies from the receiver at the given coordinates into the buffer
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param width The width of the buffer
 * @param height The height of the buffer
 * @param buffer The buffer to copy into
 */
@JvmName("getInt")
fun <D : IntsRO2, F : ColorFormatInt> Bitmap<D, F>.get(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    buffer: Bytes
) {
    val data = data
    when (data) {
        is Int2ByteArrayRO<*> -> {
            copy(
                x, y,
                this.width, this.height,
                data.array,
                0, 0,
                width, height,
                buffer,
                width, height
            )
        }
        else -> {
            var i = 0
            for (yy in y until y + height) {
                for (xx in x until x + width) {
                    this[xx, yy].splitToBytes { b3, b2, b1, b0 ->
                        buffer[i++] = b3
                        buffer[i++] = b2
                        buffer[i++] = b1
                        buffer[i++] = b0
                    }
                }
            }
        }
    }
}

/**
 * Copies from the given image into the receiver at the given coordinates
 * @receiver The image to copy into
 * @param x x-Coordinate in the destination image
 * @param y y-Coordinate in the destination image
 */
inline fun <F : ColorFormat<*>> Bitmap<*, F>.set(
    x: Int,
    y: Int,
    image: Bitmap<*, F>
) = when (format) {
    RGBA -> {
        when (image.format) {
            RGBA -> cast<Ints2, RGBA>(RGBA)!!.set(x, y, image.cast(RGBA)!!)
            else -> throw IllegalArgumentException("Cannot convert from ${image.format} to $format")
        }
    }
    else -> throw IllegalArgumentException("Unsupported format: $format")
}

/**
 * Copies from the given image into the receiver at the given coordinates
 * @receiver The image to copy into
 * @param x x-Coordinate in the destination image
 * @param y y-Coordinate in the destination image
 */
@JvmName("setInt")
inline fun Bitmap<Ints2, RGBA>.set(
    x: Int,
    y: Int,
    image: Bitmap<IntsRO2, RGBA>
) = set(x, y, image.width, image.height, image.data.asBytesRO())

/**
 * Copies from the given image into the receiver at the given coordinates
 * @receiver The image to copy into
 * @param x x-Coordinate in the destination image
 * @param y y-Coordinate in the destination image
 * @param width The width of the buffer
 * @param height The height of the buffer
 * @param buffer The buffer to copy from
 */
fun <D : Ints2, F : ColorFormatInt> Bitmap<D, F>.set(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    buffer: BytesRO
) {
    val data = data
    if (data is Ints2Ints<*>) {
        val array = data.array
        if (array is IntsBytes<*>) {
            copy(
                0, 0,
                width, height,
                buffer,
                x, y,
                this.width, this.height,
                array.array,
                width, height
            )
            return
        }
    }
    if (data is Int2ByteArray<*>) {
        copy(
            0, 0,
            width, height,
            buffer,
            x, y,
            this.width, this.height,
            data.array,
            width, height
        )
        return
    }
    var i = 0
    for (yy in y until y + height) {
        for (xx in x until x + width) {
            this[xx, yy] = combineToInt(
                buffer[i++],
                buffer[i++],
                buffer[i++],
                buffer[i++]
            )
        }
    }
}

inline fun Ints2BytesBitmap<*>.flipVertical() {
    flipVertical(width, height, data.array.array)
}

/**
 * Draws [image] on top of the given mutable image, comparable to a layer
 * merge in an image editors
 * @param image The image to merge on top
 * @receiver The image to merge onto
 */
fun Bitmap<Ints2, RGBA>.mergeBelow(image: Bitmap<IntsRO2, RGBA>) {
    if (width != image.width || height != image.height)
        throw IllegalArgumentException("Bitmap sizes do not match")

    for (y in 0 until height) {
        for (x in 0 until width) {
            image[x, y].splitToBytes { layerR, layerG, layerB, layerA ->
                if (layerA == 0.toByte()) return@splitToBytes
                this[x, y] = when (layerA) {
                    255.toByte() ->
                        combineToInt(layerR, layerG, layerB, layerA)
                    else -> this[x, y].splitToBytes { bufferR, bufferG, bufferB, bufferA ->
                        val a = (layerA.toInt() and 0xFF) / 255.0
                        val oneMinusA = 1.0 - a
                        combineToInt(
                            ((bufferR.toInt() and 0xFF) * oneMinusA + (layerR.toInt() and 0xFF) * a).toByte(),
                            ((bufferG.toInt() and 0xFF) * oneMinusA + (layerG.toInt() and 0xFF) * a).toByte(),
                            ((bufferB.toInt() and 0xFF) * oneMinusA + (layerB.toInt() and 0xFF) * a).toByte(),
                            255.coerceAtMost((bufferA.toInt() and 0xFF) + (layerA.toInt() and 0xFF)).toByte()
                        )
                    }
                }
            }
        }
    }
}

/**
 * General purpose image copy function operating directly on 32-bit RGBA buffers
 *
 * **Note:** Endianness depends on the buffer, copying between little and big
 * endian buffers should work fine
 * @param srcX x-Coordinate in the source image
 * @param srcY y-Coordinate in the source image
 * @param srcWidth The width of the source buffer
 * @param srcHeight The height of the source buffer
 * @param srcBuffer The buffer to copy from
 * @param destX x-Coordinate in the destination image
 * @param destY y-Coordinate in the destination image
 * @param destWidth The width of the destination buffer
 * @param destHeight The height of the destination buffer
 * @param destBuffer The buffer to copy to
 * @param width The width of the copied area
 * @param height The height of the copied area
 */
fun copy(
    srcX: Int, srcY: Int,
    srcWidth: Int, srcHeight: Int,
    srcBuffer: BytesRO,
    destX: Int, destY: Int,
    destWidth: Int, destHeight: Int,
    destBuffer: Bytes,
    width: Int, height: Int
) {
    if (srcX < 0)
        throw IllegalArgumentException("srcX is negative: $srcX")
    if (srcY < 0)
        throw IllegalArgumentException("srcY is negative: $srcY")
    if (destX < 0)
        throw IllegalArgumentException("destX is negative: $destX")
    if (destY < 0)
        throw IllegalArgumentException("destY is negative: $destY")
    if (srcX + width > srcWidth)
        throw IllegalArgumentException(
            "srcX and width are out of bounds : $srcX + $width"
        )
    if (srcY + height > srcHeight)
        throw IllegalArgumentException(
            "srcX and width are out of bounds : $srcY + $height"
        )
    if (destX + width > destWidth)
        throw IllegalArgumentException(
            "destX and width are out of bounds : $destX + $width"
        )
    if (destY + height > destHeight)
        throw IllegalArgumentException(
            "destX and width are out of bounds : $destY + $height"
        )
    if (srcWidth * srcHeight shl 2 != srcBuffer.size)
        throw IllegalArgumentException("Source buffer not correctly sized")
    if (destWidth * destHeight shl 2 != destBuffer.size)
        throw IllegalArgumentException("Destination buffer not correctly sized")

    val srcScanSize = srcWidth shl 2
    val srcScanOffset = srcX shl 2
    val destScanSize = destWidth shl 2
    val destScanOffset = destX shl 2
    val scanSize = width shl 2
    for (row in 0 until height) {
        val srcPos = (srcY + row) * srcScanSize + srcScanOffset
        val destPos = (destY + row) * destScanSize + destScanOffset
        srcBuffer.getBytes(srcPos, destBuffer.slice(destPos, scanSize))
    }
}

fun flipVertical(
    width: Int,
    height: Int,
    buffer: Bytes
) {
    val scanline = width shl 2
    val limit = scanline * height
    if (limit != buffer.size)
        throw IllegalArgumentException("Buffer not correctly sized")

    val swap = ByteArray(scanline).view
    val offsetInv = limit - scanline
    for (yy in 0 until (height shr 1)) {
        val yyy = yy * scanline
        buffer.getBytes(yyy, swap)
        buffer.getBytes(offsetInv - yyy, buffer.slice(yyy, scanline))
        buffer.setBytes(offsetInv - yyy, swap)
    }
}

fun Bitmap<*, *>.asBytesRORGBABitmap(): Ints2BytesROBitmap<RGBA> {
    cast<Ints2BytesRO<BytesRO>, RGBA>()?.let { return it }
    return toByteArrayRGBABitmap()
}

fun Bitmap<*, *>.toByteArrayRGBABitmap(): Ints2ByteArrayBitmap<RGBA> =
    when (format) {
        RGBA -> cast(RGBA)!!.toByteArrayRGBABitmap()
    }

@JvmName("toByteArrayRGBABitmapIntsRO2RGBA")
fun Bitmap<IntsRO2, RGBA>.toByteArrayRGBABitmap(): Ints2ByteArrayBitmap<RGBA> {
    val image = Ints2ByteArrayBitmap(width, height, RGBA)
    for (y in 0 until height) {
        for (x in 0 until width) {
            image[x, y] = this[x, y]
        }
    }
    return image
}

// TODO: Remove after 0.0.14

@Deprecated("Use new array wrappers")
@JvmName("flipVerticalOld")
inline fun MutableIntByteViewBitmap<*>.flipVertical() {
    flipVertical(width, height, data.array)
}

@Deprecated("Use new array wrappers")
@JvmName("getIntOld")
inline fun <D : IntsRO2, F : ColorFormatInt> Bitmap<D, F>.get(
    x: Int,
    y: Int,
    image: MutableIntByteViewBitmap<F>
) = get(x, y, image.width, image.height, image.data.array)

@Deprecated("Use new array wrappers", ReplaceWith("asBytesRORGBABitmap()"))
fun Bitmap<*, *>.asByteViewRGBABitmap(): IntByteViewBitmap<RGBA> {
    cast<Int2ByteArrayRO<BytesRO>, RGBA>()?.let { return it }
    return toByteViewRGBABitmap()
}

@Deprecated("Use new array wrappers", ReplaceWith("toByteArrayRGBABitmap()"))
fun Bitmap<*, *>.toByteViewRGBABitmap(): MutableIntByteViewBitmap<RGBA> =
    when (format) {
        RGBA -> cast(RGBA)!!.toByteViewRGBABitmap()
    }

@Deprecated("Use new array wrappers", ReplaceWith("toByteArrayRGBABitmap()"))
@JvmName("toByteViewRGBABitmapIntsRO2RGBA")
fun Bitmap<IntsRO2, RGBA>.toByteViewRGBABitmap(): MutableIntByteViewBitmap<RGBA> {
    val image = MutableIntByteViewBitmap(width, height, RGBA)
    for (y in 0 until height) {
        for (x in 0 until width) {
            image[x, y] = this[x, y]
        }
    }
    return image
}
