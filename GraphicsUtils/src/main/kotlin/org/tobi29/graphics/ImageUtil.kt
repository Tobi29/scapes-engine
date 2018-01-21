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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.graphics

import org.tobi29.io.ByteView
import org.tobi29.io.ByteViewRO
import org.tobi29.io.DefaultMemoryViewProvider
import org.tobi29.io.view

/**
 * Copies from the receiver at the given coordinates into a new image
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param width The width of the new image
 * @param height The height of the new image
 */
inline fun Image.get(x: Int,
                     y: Int,
                     width: Int,
                     height: Int) =
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
inline fun Image.get(x: Int,
                     y: Int,
                     width: Int,
                     height: Int,
                     bufferProvider: (Int) -> ByteView): Image {
    val buffer = bufferProvider(width * height shl 2)
    get(x, y, width, height, buffer)
    return Image(width, height, buffer)
}

/**
 * Copies from the receiver at the given coordinates into the image
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param image The image to copy into
 */
inline fun Image.get(x: Int,
                     y: Int,
                     image: MutableImage) {
    get(x, y, image.width, image.height, image.view)
}

/**
 * Copies from the receiver at the given coordinates into the buffer
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param width The width of the buffer
 * @param height The height of the buffer
 * @param buffer The buffer to copy into
 */
inline fun Image.get(x: Int,
                     y: Int,
                     width: Int,
                     height: Int,
                     buffer: ByteView) {
    copy(x, y, this.width, this.height, this.view, 0, 0, width, height,
            buffer, width, height)
}

/**
 * Copies from the given image into the receiver at the given coordinates
 * @receiver The image to copy into
 * @param x x-Coordinate in the destination image
 * @param y y-Coordinate in the destination image
 */
inline fun MutableImage.set(x: Int,
                            y: Int,
                            image: Image) {
    set(x, y, image.width, image.height, image.view)
}

/**
 * Copies from the given image into the receiver at the given coordinates
 * @receiver The image to copy into
 * @param x x-Coordinate in the destination image
 * @param y y-Coordinate in the destination image
 * @param width The width of the buffer
 * @param height The height of the buffer
 * @param buffer The buffer to copy from
 */
inline fun MutableImage.set(x: Int,
                            y: Int,
                            width: Int,
                            height: Int,
                            buffer: ByteViewRO) {
    copy(0, 0, width, height, buffer, x, y, this.width, this.height,
            this.view, width, height)
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
fun copy(srcX: Int,
         srcY: Int,
         srcWidth: Int,
         srcHeight: Int,
         srcBuffer: ByteViewRO,
         destX: Int,
         destY: Int,
         destWidth: Int,
         destHeight: Int,
         destBuffer: ByteView,
         width: Int,
         height: Int) {
    if (srcX < 0) {
        throw IllegalArgumentException("srcX is negative: $srcX")
    }
    if (srcY < 0) {
        throw IllegalArgumentException("srcY is negative: $srcY")
    }
    if (destX < 0) {
        throw IllegalArgumentException("destX is negative: $destX")
    }
    if (destY < 0) {
        throw IllegalArgumentException("destY is negative: $destY")
    }
    if (srcX + width > srcWidth) {
        throw IllegalArgumentException(
                "srcX and width are out of bounds : $srcX + $width")
    }
    if (srcY + height > srcHeight) {
        throw IllegalArgumentException(
                "srcX and width are out of bounds : $srcY + $height")
    }
    if (destX + width > destWidth) {
        throw IllegalArgumentException(
                "destX and width are out of bounds : $destX + $width")
    }
    if (destY + height > destHeight) {
        throw IllegalArgumentException(
                "destX and width are out of bounds : $destY + $height")
    }
    if (srcWidth * srcHeight shl 2 != srcBuffer.size) {
        throw IllegalArgumentException("Source buffer not correctly sized")
    }
    if (destWidth * destHeight shl 2 != destBuffer.size) {
        throw IllegalArgumentException("Destination buffer not correctly sized")
    }

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

inline fun MutableImage.flipVertical() {
    flipVertical(width, height, view)
}

fun flipVertical(width: Int,
                 height: Int,
                 buffer: ByteView) {
    val scanline = width shl 2
    val limit = scanline * height
    if (limit != buffer.size) {
        throw IllegalArgumentException("Buffer not correctly sized")
    }

    val swap = ByteArray(scanline).view
    val offsetInv = limit - scanline
    for (yy in 0 until (height shr 1)) {
        val yyy = yy * scanline
        buffer.getBytes(yyy, swap)
        buffer.getBytes(offsetInv - yyy, buffer.slice(yyy, scanline))
        buffer.setBytes(offsetInv - yyy, swap)
    }
}

/**
 * Draws [image] on top of the given mutable image, comparable to a layer
 * merge in an image editors
 * @param image The image to merge on top
 * @receiver The image to merge onto
 */
fun MutableImage.mergeBelow(image: Image) {
    if (width != image.width || height != image.height)
        throw IllegalArgumentException("Image sizes do not match")

    var position = 0
    while (position < image.view.size) {
        val layerR = image.view[position + 0].toInt() and 0xFF
        val layerG = image.view[position + 1].toInt() and 0xFF
        val layerB = image.view[position + 2].toInt() and 0xFF
        val layerA = image.view[position + 3].toInt() and 0xFF
        when {
            layerA == 255 -> {
                view[position + 0] = layerR.toByte()
                view[position + 1] = layerG.toByte()
                view[position + 2] = layerB.toByte()
                view[position + 3] = layerA.toByte()
            }
            layerA != 0 -> {
                val bufferR = view[position + 0].toInt() and 0xFF
                val bufferG = view[position + 1].toInt() and 0xFF
                val bufferB = view[position + 2].toInt() and 0xFF
                val bufferA = view[position + 3].toInt() and 0xFF
                val a = layerA / 255.0
                val oneMinusA = 1.0 - a
                view[position + 0] = (bufferR * oneMinusA + layerR * a).toByte()
                view[position + 1] = (bufferG * oneMinusA + layerG * a).toByte()
                view[position + 2] = (bufferB * oneMinusA + layerB * a).toByte()
                view[position + 3] = 255.coerceAtMost(bufferA + layerA).toByte()
            }
        }
        position += 4
    }
}

/**
 * Creates a new image from a mutable image
 *
 * **Note:** Changes to the old mutable image will change this new instance
 * @param image The image to take size and buffer from
 */
inline fun Image(image: MutableImage) = Image(image.width, image.height,
        image.view)

/**
 * Creates a new image from a mutable image
 *
 * **Note:** Changes to the old mutable image will change this new instance
 * @receiver The image to take size and buffer from
 */
inline fun MutableImage.toImage() = Image(this)

/**
 * Creates a new mutable image from an image copying its buffer
 * @param image The image to take size and buffer from
 */
inline fun MutableImage(image: Image) = MutableImage(image.width, image.height,
        image.view.run {
            ByteArray(size).view.also { getBytes(0, it) }
        })

/**
 * Creates a new mutable image from an image copying its buffer
 * @receiver The image to take size and buffer from
 */
inline fun Image.toMutableImage() = MutableImage(this)
