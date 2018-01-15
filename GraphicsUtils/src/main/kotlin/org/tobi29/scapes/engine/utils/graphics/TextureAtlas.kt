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

import org.tobi29.scapes.engine.math.nextPowerOfTwo
import org.tobi29.scapes.engine.math.vector.MutableVector2i
import org.tobi29.scapes.engine.math.vector.Vector2i
import org.tobi29.scapes.engine.utils.ArrayDeque
import org.tobi29.scapes.engine.utils.math.ceilToInt
import kotlin.math.sqrt

fun assembleAtlas(textures: Sequence<Pair<Vector2i, MutableVector2i>>,
                  minSize: Int = 1): Vector2i {
    if (minSize <= 0) throw IllegalArgumentException(
            "minSize must be greater than 0, is: $minSize")
    val area = textures.map { it.first.x * it.first.y }.sum()
    val areaSize = sqrt(area.toDouble()).ceilToInt()
    var size = nextPowerOfTwo(areaSize)
    while (true) {
        tryInsert(minSize, size, textures)?.let { return Vector2i(size, size) }
        size = size shl 1
    }
}

private fun tryInsert(minSize: Int,
                      size: Int,
                      textures: Sequence<Pair<Vector2i, MutableVector2i>>): TextureNode? {
    var root: TextureNode
    root = TextureNode.Empty(
            Rectangle(0, 0, size - 1, size - 1), { root = it })
    textures.forEach { texture ->
        if (!insert(root, minSize, texture)) {
            return null
        }
    }
    return root
}

private fun insert(nodeStart: TextureNode,
                   minSize: Int,
                   texture: Pair<Vector2i, MutableVector2i>): Boolean {
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
                    val w = nextValidSize(minSize, texture.first.x)
                    val h = nextValidSize(minSize, texture.first.y)
                    if (w > node.rectangle.width || h > node.rectangle.height) {
                        break@current
                    }
                    val dw = node.rectangle.width - w
                    val dh = node.rectangle.height - h
                    if (dw == 0 && dh == 0) {
                        texture.second.setXY(node.rectangle.left,
                                node.rectangle.top)
                        node.swap(TextureNode.Texture(node.rectangle))
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

private fun nextValidSize(minSize: Int,
                          size: Int): Int {
    return ((size - 1) / minSize + 1) * minSize
}

private sealed class TextureNode(val rectangle: Rectangle) {
    class Empty(rectangle: Rectangle,
                val swap: (TextureNode) -> Unit) : TextureNode(rectangle)

    class Texture(rectangle: Rectangle) : TextureNode(rectangle)

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
