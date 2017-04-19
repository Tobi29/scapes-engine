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

package org.tobi29.scapes.engine.utils.graphics

import org.tobi29.scapes.engine.utils.io.ByteBuffer

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
                     height: Int) = get(x, y, width, height, ::ByteBuffer)

/**
 * Copies from the receiver at the given coordinates into a new image
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param width The width of the new image
 * @param height The height of the new image
 * @param bufferSupplier Supplier for the new buffer
 */
inline fun Image.get(x: Int,
                     y: Int,
                     width: Int,
                     height: Int,
                     bufferSupplier: (Int) -> ByteBuffer): Image {
    val buffer = bufferSupplier(width * height shl 2)
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
    get(x, y, image.width, image.height, image.buffer)
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
                     buffer: ByteBuffer) {
    copy(x, y, this.width, this.height, this.buffer, 0, 0, width, height,
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
    set(x, y, image.width, image.height, image.buffer)
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
                            buffer: ByteBuffer) {
    copy(0, 0, width, height, buffer, x, y, this.width, this.height,
            this.buffer, width, height)
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
         srcBuffer: ByteBuffer,
         destX: Int,
         destY: Int,
         destWidth: Int,
         destHeight: Int,
         destBuffer: ByteBuffer,
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
    if (srcWidth * srcHeight shl 2 != srcBuffer.remaining()) {
        throw IllegalArgumentException("Source buffer not correctly sized")
    }
    if (destWidth * destHeight shl 2 != destBuffer.remaining()) {
        throw IllegalArgumentException("Destination buffer not correctly sized")
    }

    val oldSrcLimit = srcBuffer.limit()
    val oldSrcPosition = srcBuffer.position()
    val oldDestPosition = destBuffer.position()

    val srcScanSize = srcWidth shl 2
    val srcScanOffset = (srcX shl 2) + oldSrcPosition
    val destScanSize = destWidth shl 2
    val destScanOffset = (destX shl 2) + oldDestPosition
    val scanSize = width shl 2
    for (row in 0..height - 1) {
        val srcPos = (srcY + row) * srcScanSize + srcScanOffset
        srcBuffer.limit(srcPos + scanSize)
        srcBuffer.position(srcPos)
        destBuffer.position((destY + row) * destScanSize + destScanOffset)
        destBuffer.put(srcBuffer)
    }

    srcBuffer.limit(oldSrcLimit)
    srcBuffer.position(oldSrcPosition)
    destBuffer.position(oldDestPosition)
}

inline fun MutableImage.flipVertical() {
    flipVertical(width, height, buffer)
}

fun flipVertical(width: Int,
                 height: Int,
                 buffer: ByteBuffer) {
    val scanline = width shl 2
    val limit = scanline * height
    if (limit != buffer.remaining()) {
        throw IllegalArgumentException("Buffer not correctly sized")
    }

    val oldLimit = buffer.limit()
    val oldPosition = buffer.position()

    val copy = buffer.asReadOnlyBuffer()
    val swap = ByteBuffer(scanline)
    val offset = oldPosition
    val offsetInv = offset + limit - scanline
    for (yy in 0..(height shr 1) - 1) {
        buffer.limit((yy + 1) * scanline)
        buffer.position(offset + yy * scanline)
        swap.put(buffer)
        buffer.position(offset + yy * scanline)
        swap.rewind()
        copy.limit(offsetInv - (yy - 1) * scanline)
        copy.position(offsetInv - yy * scanline)
        buffer.put(copy)
        buffer.limit(limit)
        buffer.position(offsetInv - yy * scanline)
        buffer.put(swap)
        swap.rewind()
    }

    buffer.limit(oldLimit)
    buffer.position(oldPosition)
}

/**
 * Creates a new image from a mutable image
 *
 * **Note:** Changes to the old mutable image will change this new instance
 * @param image The image to take size and buffer from
 */
inline fun Image(image: MutableImage) = Image(image.width, image.height,
        image.buffer)

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
        image.buffer)

/**
 * Creates a new mutable image from an image copying its buffer
 * @receiver The image to take size and buffer from
 */
inline fun Image.toMutableImage() = MutableImage(this)
