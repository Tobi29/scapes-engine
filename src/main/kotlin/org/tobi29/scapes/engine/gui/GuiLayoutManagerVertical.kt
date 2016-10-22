/*
 * Copyright 2012-2016 Tobi29
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

class GuiLayoutManagerVertical(start: Vector2d, maxSize: Vector2d,
                               components: Set<GuiComponent>) : GuiLayoutManager(
        start, maxSize, components) {

    override fun layout(output: MutableList<Triple<GuiComponent, Vector2d, Vector2d>>) {
        var unsized = 0.0
        var usedHeight = 0.0
        for (component in components) {
            val data = component.parent
            if (data is GuiLayoutDataVertical) {
                if (data.height() < 0.0) {
                    unsized -= data.height()
                } else {
                    val marginStart = data.marginStart
                    val marginEnd = data.marginEnd
                    usedHeight += data.height() + marginStart.y +
                            marginEnd.y
                }
            }
        }
        val pos = MutableVector2d()
        val size = MutableVector2d()
        val offset = MutableVector2d(start)
        val outSize = MutableVector2d()
        val preferredSize = Vector2d(maxSize.x,
                (maxSize.y - usedHeight) / unsized)
        for (component in components) {
            val data = component.parent
            pos.set(offset.now())
            size.set(data.width(), data.height())
            if (data is GuiLayoutDataVertical) {
                val marginStart = data.marginStart
                val marginEnd = data.marginEnd
                if (size.doubleX() >= 0.0) {
                    pos.plusX((maxSize.x - size.doubleX() -
                            marginStart.x - marginEnd.x) * 0.5)
                }
                size(size, preferredSize.minus(marginStart).minus(marginEnd),
                        maxSize.minus(marginStart).minus(marginEnd))
                pos.plus(marginStart)
                offset.plusY(size.doubleY() + marginStart.y +
                        marginEnd.y)
                setSize(pos.now().plus(size.now()).plus(marginEnd), outSize)
            } else if (data is GuiLayoutDataAbsolute) {
                pos.set(data.pos())
                size(size, preferredSize, maxSize)
                setSize(pos.now().plus(size.now()), outSize)
            } else {
                throw IllegalStateException(
                        "Invalid layout node: " + data.javaClass)
            }
            output.add(Triple(component, pos.now(), size.now()))
        }
        this.size = outSize.now()
    }
}
