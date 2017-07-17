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

package org.tobi29.scapes.engine.gui

import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d
import org.tobi29.scapes.engine.utils.math.vector.Vector2d
import org.tobi29.scapes.engine.utils.math.vector.minus

class GuiLayoutManagerHorizontal(start: Vector2d,
                                 maxSize: Vector2d,
                                 components: Collection<GuiComponent>) : GuiLayoutManager(
        start, maxSize, components) {

    override fun layout(output: MutableList<Triple<GuiComponent, Vector2d, Vector2d>>) {
        var unsized = 0.0
        var usedWidth = 0.0
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
                if (size.x < 0.0) {
                    unsized -= size.x
                } else {
                    usedWidth += size.x + marginStart.x + marginEnd.x
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
        val preferredSize = Vector2d((maxSize.x - usedWidth) / unsized,
                maxSize.y)
        val mSize = MutableVector2d()
        val mPreferredSize = MutableVector2d()
        for ((component, size) in sizes) {
            if (component == null) {
                continue
            }
            val data = component.parent
            mSize.set(maxSize)
            val asize = if (data is GuiLayoutDataFlow) {
                pos.set(offset.x, offset.y)
                val marginStart = data.marginStart
                val marginEnd = data.marginEnd
                val margin = data.margin
                if (size.y >= 0.0) {
                    pos.plusY((maxSize.y - size.y -
                            marginStart.y - marginEnd.y) * 0.5)
                }
                mPreferredSize.set(preferredSize).minus(margin)
                mSize.minus(margin)
                val asize = size(size, mPreferredSize, mSize)
                pos.plus(marginStart)
                offset.plusX(asize.x + margin.x)
                posSize.set(pos.x, pos.y).plus(asize).plus(marginEnd)
                setSize(posSize, outSize)
                asize
            } else if (data is GuiLayoutDataAbsolute) {
                pos.set(data.pos())
                val asize = size(size, mSize, mSize)
                posSize.set(pos.x, pos.y).plus(asize)
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
}
