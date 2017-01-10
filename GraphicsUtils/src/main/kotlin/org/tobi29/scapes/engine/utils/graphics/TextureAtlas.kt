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
import java.util.*
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
        paint(tree, image)
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
        var root: TextureNode
        root = TextureNode.Empty(
                Rectangle(0, 0, size - 1, size - 1), { root = it })
        textures.forEach { texture ->
            if (!insert(root, texture)) {
                return null
            }
        }
        return root
    }

    private fun insert(nodeStart: TextureNode,
                       texture: TextureAtlasEntry): Boolean {
        val deque = ArrayDeque<TextureNode>()
        deque.add(nodeStart)
        while (deque.isNotEmpty()) {
            var node = deque.pollLast()
            current@ while (true) {
                when (node) {
                    is TextureNode.Branch -> {
                        val branch = node
                        node = branch.left
                        deque.addLast(branch.right)
                    }
                    is TextureNode.Empty -> {
                        val w = nextValidSize(texture.width)
                        val h = nextValidSize(texture.height)
                        if (w > node.rectangle.width || h > node.rectangle.height) {
                            break@current
                        }
                        val dw = node.rectangle.width - w
                        val dh = node.rectangle.height - h
                        if (dw == 0 && dh == 0) {
                            node.swap(TextureNode.Texture(node.rectangle,
                                    texture))
                            return true
                        }
                        val split = if (dw > dh) {
                            TextureNode.Branch(node.rectangle,
                                    Rectangle(node.rectangle.left,
                                            node.rectangle.top,
                                            node.rectangle.left + w - 1,
                                            node.rectangle.bottom),
                                    Rectangle(node.rectangle.left + w,
                                            node.rectangle.top,
                                            node.rectangle.right,
                                            node.rectangle.bottom))
                        } else {
                            TextureNode.Branch(node.rectangle,
                                    Rectangle(node.rectangle.left,
                                            node.rectangle.top,
                                            node.rectangle.right,
                                            node.rectangle.top + h - 1),
                                    Rectangle(node.rectangle.left,
                                            node.rectangle.top + h,
                                            node.rectangle.right,
                                            node.rectangle.bottom))
                        }
                        node.swap(split)
                        node = split.left
                    }
                    else -> break@current
                }
            }
        }
        return false
    }

    private fun paint(nodeStart: TextureNode,
                      image: MutableImage) {
        val deque = ArrayDeque<TextureNode>()
        deque.add(nodeStart)
        while (deque.isNotEmpty()) {
            var current = deque.pollLast()
            current@ while (true) {
                when (current) {
                    is TextureNode.Texture -> {
                        current.run {
                            texture.textureX = rectangle.left.toDouble() / image.width
                            texture.textureY = rectangle.top.toDouble() / image.height
                            texture.x = rectangle.left
                            texture.y = rectangle.top
                            texture.textureWidth = texture.width.toDouble() / image.width
                            texture.textureHeight = texture.height.toDouble() / image.height
                            texture.buffer?.let {
                                image.set(rectangle.left, rectangle.top,
                                        texture.width, texture.height, it)
                            }
                            texture.buffer = null
                        }
                        break@current
                    }
                    is TextureNode.Branch -> current.run {
                        current = left
                        deque.addLast(right)
                    }
                    else -> break@current
                }
            }
        }
    }

    private fun nextValidSize(size: Int): Int {
        return ((size - 1) / minSize + 1) * minSize
    }

    companion object : KLogging()

    private sealed class TextureNode(val rectangle: Rectangle) {
        class Empty(rectangle: Rectangle,
                    val swap: (TextureNode) -> Unit) : TextureNode(rectangle)

        class Texture(rectangle: Rectangle,
                      val texture: TextureAtlasEntry) : TextureNode(
                rectangle)

        class Branch(rectangle: Rectangle,
                     left: Rectangle,
                     right: Rectangle) : TextureNode(rectangle) {
            var left: TextureNode
            var right: TextureNode

            init {
                this.left = TextureNode.Empty(left) { this.left = it }
                this.right = TextureNode.Empty(right) { this.right = it }
            }
        }
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
