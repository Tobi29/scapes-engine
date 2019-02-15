/*
 * Copyright 2012-2019 Tobi29
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

import org.tobi29.io.ReadSource
import org.tobi29.io.ReadableByteStream
import org.tobi29.io.WritableByteStream

@Deprecated(
    "Use new package",
    ReplaceWith(
        "org.tobi29.graphics.png.decodePng(asset)",
        "org.tobi29.graphics.png.decodePng"
    )
)
suspend inline fun decodePng(asset: ReadSource): Bitmap<*, *> =
    org.tobi29.graphics.png.decodePng(asset)

@Deprecated(
    "Use new package",
    ReplaceWith(
        "org.tobi29.graphics.png.decodePng(stream)",
        "org.tobi29.graphics.png.decodePng"
    )
)
suspend inline fun decodePng(stream: ReadableByteStream): Bitmap<*, *> =
    org.tobi29.graphics.png.decodePng(stream)

@Deprecated(
    "Use new package",
    ReplaceWith(
        "org.tobi29.graphics.png.encodePng(image, stream, level, alpha)",
        "org.tobi29.graphics.png.encodePng"
    )
)
inline fun encodePng(
    image: Bitmap<*, *>,
    stream: WritableByteStream,
    level: Int,
    alpha: Boolean
) = org.tobi29.graphics.png.encodePng(image, stream, level, alpha)