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

import org.tobi29.io.ByteView
import org.tobi29.io.ByteViewRO
import org.tobi29.io.view

// TODO: Remove after 0.0.13

/**
 * Copies from the receiver at the given coordinates into a new image
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param width The width of the new image
 * @param height The height of the new image
 */
@Deprecated("Use Bitmap")
inline fun Image.get(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) = Image(bitmap.get(x, y, width, height))

/**
 * Copies from the receiver at the given coordinates into a new image
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param width The width of the new image
 * @param height The height of the new image
 * @param bufferProvider Provider for buffer allocations
 */
@Deprecated("Use Bitmap")
inline fun Image.get(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    bufferProvider: (Int) -> ByteView
): Image = Image(bitmap.get(x, y, width, height, bufferProvider))

/**
 * Copies from the receiver at the given coordinates into the image
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param image The image to copy into
 */
@Deprecated("Use Bitmap")
inline fun Image.get(
    x: Int,
    y: Int,
    image: MutableImage
) = bitmap.get(x, y, image.bitmap)

/**
 * Copies from the receiver at the given coordinates into the buffer
 * @receiver The image to copy from
 * @param x x-Coordinate in the source image
 * @param y y-Coordinate in the source image
 * @param width The width of the buffer
 * @param height The height of the buffer
 * @param buffer The buffer to copy into
 */
@Deprecated("Use Bitmap")
inline fun Image.get(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    buffer: ByteView
) = bitmap.get(x, y, width, height, buffer)

/**
 * Copies from the given image into the receiver at the given coordinates
 * @receiver The image to copy into
 * @param x x-Coordinate in the destination image
 * @param y y-Coordinate in the destination image
 */
@Deprecated("Use Bitmap")
inline fun MutableImage.set(
    x: Int,
    y: Int,
    image: Image
) = bitmap.set(x, y, image.bitmap)

/**
 * Copies from the given image into the receiver at the given coordinates
 * @receiver The image to copy into
 * @param x x-Coordinate in the destination image
 * @param y y-Coordinate in the destination image
 * @param width The width of the buffer
 * @param height The height of the buffer
 * @param buffer The buffer to copy from
 */
@Deprecated("Use Bitmap")
inline fun MutableImage.set(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    buffer: ByteViewRO
) = bitmap.set(x, y, width, height, buffer)

@Deprecated("Use Bitmap")
inline fun MutableImage.flipVertical() = bitmap.flipVertical()

/**
 * Draws [image] on top of the given mutable image, comparable to a layer
 * merge in an image editors
 * @param image The image to merge on top
 * @receiver The image to merge onto
 */
@Deprecated("Use Bitmap")
fun MutableImage.mergeBelow(image: Image) = bitmap.mergeBelow(image.bitmap)

/**
 * Creates a new image from a mutable image
 *
 * **Note:** Changes to the old mutable image will change this new instance
 * @param image The image to take size and buffer from
 */
@Deprecated("Use Bitmap")
inline fun Image(image: MutableImage) =
    Image(image.width, image.height, image.view)

/**
 * Creates a new image from a mutable image
 *
 * **Note:** Changes to the old mutable image will change this new instance
 * @receiver The image to take size and buffer from
 */
@Deprecated("Use Bitmap")
inline fun MutableImage.toImage() = Image(this)

/**
 * Creates a new mutable image from an image copying its buffer
 * @param image The image to take size and buffer from
 */
@Deprecated("Use Bitmap")
inline fun MutableImage(image: Image) = MutableImage(image.width, image.height,
    image.view.run {
        ByteArray(size).view.also { getBytes(0, it) }
    })

/**
 * Creates a new mutable image from an image copying its buffer
 * @receiver The image to take size and buffer from
 */
@Deprecated("Use Bitmap")
inline fun Image.toMutableImage() = MutableImage(this)
