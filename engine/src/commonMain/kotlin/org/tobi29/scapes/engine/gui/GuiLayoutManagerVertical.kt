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

package org.tobi29.scapes.engine.gui

import org.tobi29.math.Face
import org.tobi29.math.vector.*
import org.tobi29.utils.forAllObjects

class GuiLayoutManagerVertical(
    start: Vector2d,
    maxSize: Vector2d,
    components: Collection<GuiComponent>
) : GuiLayoutManager(start, maxSize, components) {
    override fun layout(output: MutableList<Triple<GuiComponent, Vector2d, Vector2d>>) {
        var unsized = 0.0
        var usedHeight = 0.0
        val sizeCache = sizeCache.get()
        if (sizeCache.isEmpty()) {
            sizeCache.push()
        }
        val sizes = sizeCache.removeAt(sizeCache.size - 1)
        for (component in components) {
            if (!component.visible) {
                continue
            }
            val data = component.parent
            if (data is GuiLayoutDataFlow) {
                val marginStart = data.marginStart
                val marginEnd = data.marginEnd
                val margin = data.margin
                val size = data.calculateSize(maxSize - margin)
                sizes.push().set(component, size)
                if (size.y < 0.0) {
                    unsized -= size.y
                } else {
                    usedHeight += size.y + marginStart.y + marginEnd.y
                }
            } else {
                val size = data.calculateSize(maxSize)
                sizes.push().set(component, size)
            }
        }
        if (sizes.isEmpty()) {
            sizeMut = Vector2d.ZERO
            return
        }
        val pos = MutableVector2d()
        val posSize = MutableVector2d()
        val offset = MutableVector2d(start)
        val outSize = MutableVector2d()
        val preferredSize = Vector2d(
            maxSize.x,
            (maxSize.y - usedHeight) / unsized
        )
        val mSize = MutableVector2d()
        val mPreferredSize = MutableVector2d()
        for ((component, size) in sizes) {
            if (component == null) {
                continue
            }
            val data = component.parent
            mSize.set(maxSize)
            val asize = if (data is GuiLayoutDataFlow) {
                pos.setXY(offset.x, offset.y)
                val marginStart = data.marginStart
                val marginEnd = data.marginEnd
                val margin = data.margin
                if (size.x >= 0.0) {
                    pos.addX(
                        (maxSize.x - size.x -
                                marginStart.x - marginEnd.x) * 0.5
                    )
                }
                mPreferredSize.set(preferredSize)
                mPreferredSize.subtract(margin)
                mSize.subtract(margin)
                val asize = size(size, mPreferredSize, mSize)
                pos.add(marginStart)
                offset.addY(asize.y + margin.y)
                posSize.setXY(pos.x, pos.y)
                posSize.add(asize)
                posSize.add(marginEnd)
                setSize(posSize, outSize)
                asize
            } else if (data is GuiLayoutDataAbsolute) {
                pos.set(data.pos())
                val asize = size(size, mSize, mSize)
                posSize.setXY(pos.x, pos.y).add(asize)
                setSize(posSize, outSize)
                asize
            } else {
                throw IllegalStateException("Invalid layout node: $data")
            }
            output.add(Triple(component, pos.now(), asize))
        }
        this.sizeMut = outSize.now()
        sizes.reset()
        sizes.forAllObjects { it.component = null }
        sizeCache.give(sizes)
    }

    override fun navigate(
        face: Face,
        component: GuiComponent
    ): GuiComponent? {
        if (face != Face.NORTH && face != Face.SOUTH) return null
        var i = components.indexOf(component)
        if (i < 0) return null
        while (true) {
            when (face) {
                Face.NORTH -> i--
                Face.SOUTH -> i++
            }
            return components.getOrNull(i) ?: return null
        }
    }

    override fun enter(face: Face): GuiComponent? =
        when (face) {
            Face.NORTH -> components.lastOrNull()
            Face.SOUTH -> components.firstOrNull()
        // TODO: Select based on previous location
            else -> components.firstOrNull()
        }
}
