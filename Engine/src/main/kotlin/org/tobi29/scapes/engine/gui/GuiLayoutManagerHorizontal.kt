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
import org.tobi29.scapes.engine.utils.math.vector.plus

class GuiLayoutManagerHorizontal(start: Vector2d,
                                 maxSize: Vector2d,
                                 components: Set<GuiComponent>) : GuiLayoutManager(
        start, maxSize, components) {

    override fun layout(output: MutableList<Triple<GuiComponent, Vector2d, Vector2d>>) {
        var unsized = 0.0
        var usedWidth = 0.0
        val sizes = HashMap<GuiComponent, Vector2d>()
        for (component in components) {
            if (!component.visible) {
                continue
            }
            val data = component.parent
            if (data is GuiLayoutDataFlow) {
                val marginStart = data.marginStart
                val marginEnd = data.marginEnd
                val size = data.calculateSize(maxSize - marginStart - marginEnd)
                sizes[component] = size
                if (size.x < 0.0) {
                    unsized -= size.x
                } else {
                    usedWidth += size.x + marginStart.x + marginEnd.x
                }
            } else {
                val size = data.calculateSize(maxSize)
                sizes[component] = size
            }
        }
        val pos = MutableVector2d()
        val offset = MutableVector2d(start)
        val outSize = MutableVector2d()
        val preferredSize = Vector2d((maxSize.x - usedWidth) / unsized,
                maxSize.y)
        for (component in components) {
            if (!component.visible) {
                continue
            }
            val data = component.parent
            val size = sizes[component]!!
            pos.set(offset.now())
            val asize = if (data is GuiLayoutDataFlow) {
                val marginStart = data.marginStart
                val marginEnd = data.marginEnd
                if (size.y >= 0.0) {
                    pos.plusY((maxSize.y - size.y -
                            marginStart.y - marginEnd.y) * 0.5)
                }
                val asize = size(size,
                        preferredSize.minus(marginStart).minus(marginEnd),
                        maxSize.minus(marginStart).minus(marginEnd))
                pos.plus(marginStart)
                offset.plusX(asize.x + marginStart.x + marginEnd.x)
                setSize(pos.now().plus(asize).plus(marginEnd), outSize)
                asize
            } else if (data is GuiLayoutDataAbsolute) {
                pos.set(data.pos())
                val asize = size(size, maxSize, maxSize)
                setSize(pos.now().plus(asize), outSize)
                asize
            } else {
                throw IllegalStateException(
                        "Invalid layout node: " + data::class.java)
            }
            output.add(Triple(component, pos.now(), asize))
        }
        this.size = outSize.now()
    }
}
