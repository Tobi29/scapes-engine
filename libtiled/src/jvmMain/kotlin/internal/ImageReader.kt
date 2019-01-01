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

package org.tobi29.tilemaps.tiled.internal

import org.tobi29.graphics.Bitmap
import org.tobi29.graphics.png.decodePng
import org.tobi29.graphics.size
import org.tobi29.io.Path
import org.tobi29.math.vector.Vector2i
import org.tobi29.tilemaps.makeTransparent
import org.w3c.dom.Node

// TODO: Support embedded images
suspend fun Node.readImage(
    path: Path
): Pair<Bitmap<*, *>, Vector2i> {
    val imgSource = requireAttributeValue("source")
    val transStr = getAttributeValue("trans")
    val size = getAttributeVector2i("width", "height")

    val image = decodePng(path[imgSource])
    if (transStr != null) {
        return image.makeTransparent(transStr) to (size ?: image.size)
    }
    return image to (size ?: image.size)
}
