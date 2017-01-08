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

import mu.KLogging
import org.tobi29.scapes.engine.utils.math.ceil
import org.tobi29.scapes.engine.utils.math.nextPowerOfTwo
import org.tobi29.scapes.engine.utils.math.sqrt
import java.util.concurrent.ConcurrentHashMap

abstract class TextureAtlas<T : TextureAtlasEntry>(protected val minSize: Int = 1) {
    protected val textures = ConcurrentHashMap<String, T>()
    protected val sources = ConcurrentHashMap<String, Image>()
    private var imageMut: Image? = null
    val image: Image
        get() = imageMut ?: throw IllegalStateException(
                "Atlas not finished yet")

    init {
        if (minSize <= 0) {
            throw IllegalArgumentException(
                    "minSize must be greater than 0, is: $minSize")
        }
    }

    fun init(): Int {
        val sequence = textures.values.asSequence()
                .sortedByDescending { it.width * it.height }
        val tree = insert(sequence)
        val image = MutableImage(tree.rectangle.width, tree.rectangle.height)
        tree.paint(image)
        imageMut = image.toImage()
        sources.clear()
        return textures.size
    }

    private fun insert(textures: Sequence<T>): TextureNode {
        val area = textures.map { it.width * it.height }.sum()
        val areaSize = ceil(sqrt(area.toDouble()))
        var size = nextPowerOfTwo(areaSize)
        while (true) {
            tryInsert(size, textures)?.let { return it }
            size = size shl 1
        }
    }

    private fun tryInsert(size: Int,
                          textures: Sequence<T>): TextureNode? {
        var root: TextureNode = TextureNode.Empty(
                Rectangle(0, 0, size - 1, size - 1))
        textures.forEach {
            if (!root.insert(it) { root = it }) {
                return null
            }
        }
        return root
    }

    private fun TextureNode.insert(texture: TextureAtlasEntry,
                                   swap: (TextureNode) -> Unit): Boolean {
        when (this) {
            is TextureNode.Texture -> return false
            is TextureNode.Branch -> {
                if (left.insert(texture) { left = it }) {
                    return true
                }
                if (right.insert(texture) { right = it }) {
                    return true
                }
                return false
            }
            is TextureNode.Empty -> {
                val w = nextValidSize(texture.width)
                val h = nextValidSize(texture.height)
                if (w > rectangle.width || h > rectangle.height) {
                    return false
                }
                val dw = rectangle.width - w
                val dh = rectangle.height - h
                if (dw == 0 && dh == 0) {
                    swap(TextureNode.Texture(rectangle, texture))
                    return true
                }
                val node = if (dw > dh) {
                    TextureNode.Branch(rectangle, TextureNode.Empty(
                            Rectangle(rectangle.left, rectangle.top,
                                    rectangle.left + w - 1, rectangle.bottom)),
                            TextureNode.Empty(
                                    Rectangle(rectangle.left + w, rectangle.top,
                                            rectangle.right, rectangle.bottom)))
                } else {
                    TextureNode.Branch(rectangle, TextureNode.Empty(
                            Rectangle(rectangle.left, rectangle.top,
                                    rectangle.right, rectangle.top + h - 1)),
                            TextureNode.Empty(
                                    Rectangle(rectangle.left, rectangle.top + h,
                                            rectangle.right, rectangle.bottom)))
                }
                swap(node)
                return node.left.insert(texture) { node.left = it }
            }
        }
    }

    private fun TextureNode.paint(image: MutableImage) {
        when (this) {
            is TextureNode.Texture -> {
                texture.textureX = rectangle.left.toDouble() / image.width
                texture.textureY = rectangle.top.toDouble() / image.height
                texture.x = rectangle.left
                texture.y = rectangle.top
                texture.textureWidth = texture.width.toDouble() / image.width
                texture.textureHeight = texture.height.toDouble() / image.height
                texture.buffer?.let {
                    image.set(rectangle.left, rectangle.top, texture.width,
                            texture.height, it)
                }
                texture.buffer = null
            }
            is TextureNode.Branch -> {
                left.paint(image)
                right.paint(image)
            }
        }
    }

    private fun nextValidSize(size: Int): Int {
        return ((size - 1) / minSize + 1) * minSize
    }

    companion object : KLogging()

    private sealed class TextureNode(val rectangle: Rectangle) {
        class Empty(rectangle: Rectangle) : TextureNode(rectangle)

        class Texture(rectangle: Rectangle,
                      val texture: TextureAtlasEntry) : TextureNode(rectangle)

        class Branch(rectangle: Rectangle,
                     var left: TextureNode,
                     var right: TextureNode) : TextureNode(rectangle)
    }

    private class Rectangle(val left: Int,
                            val top: Int,
                            val right: Int,
                            val bottom: Int) {
        val width: Int
            get() = right - left + 1
        val height: Int
            get() = bottom - top + 1
    }
}
