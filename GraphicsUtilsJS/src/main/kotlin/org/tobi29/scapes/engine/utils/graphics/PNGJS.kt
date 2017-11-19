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

import org.khronos.webgl.Int8Array
import org.tobi29.scapes.engine.utils.asArray
import org.tobi29.scapes.engine.utils.io.*
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import kotlin.browser.document
import kotlin.coroutines.experimental.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.experimental.intrinsics.suspendCoroutineOrReturn

// TODO: This only works in the browser, worthwhile to bother things like node.js?
private suspend fun loadImage(uri: Uri): Image = suspendCoroutineOrReturn { cont ->
    val image = document.createElement("img") as HTMLImageElement
    image.src = uri.toString()
    image.onerror = { _, str, _, _, _ -> throw IOException(str) }
    image.onload = {
        val canvas = document.createElement(
                "canvas") as HTMLCanvasElement
        canvas.width = image.width
        canvas.height = image.height
        val context = canvas.getContext(
                "2d") as CanvasRenderingContext2D
        context.drawImage(image, 0.0, 0.0)
        val imageData = context.getImageData(0.0, 0.0,
                canvas.width.toDouble(), canvas.height.toDouble())
        cont.resume(Image(imageData.width, imageData.height,
                Int8Array(imageData.data.buffer,
                        imageData.data.byteOffset,
                        imageData.data.length).asArray().view))
        undefined
    }
    COROUTINE_SUSPENDED
}

actual suspend fun decodePNG(asset: ReadSource): Image =
        asset.useUri { loadImage(it) }

actual suspend fun decodePNG(stream: ReadableByteStream): Image =
        stream.asByteView().useUri { loadImage(it) }
