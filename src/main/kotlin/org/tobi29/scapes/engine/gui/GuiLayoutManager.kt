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

import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.min
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d
import org.tobi29.scapes.engine.utils.math.vector.Vector2d
import org.tobi29.scapes.engine.utils.math.vector.minus
import java.util.*

abstract class GuiLayoutManager(protected val start: Vector2d,
                                protected val maxSize: Vector2d,
                                components: Set<GuiComponent>) {
    protected val components: MutableList<GuiComponent>
    protected var size: Vector2d? = null

    init {
        this.components = ArrayList<GuiComponent>(components.size)
        this.components.addAll(components)
    }

    fun layout(): List<Triple<GuiComponent, Vector2d, Vector2d>> {
        val output = ArrayList<Triple<GuiComponent, Vector2d, Vector2d>>(
                components.size)
        layout(output)
        return output
    }

    protected abstract fun layout(
            output: MutableList<Triple<GuiComponent, Vector2d, Vector2d>>)

    protected fun size(size: MutableVector2d,
                       preferredSize: Vector2d,
                       maxSize: Vector2d) {
        if (size.doubleX() < 0.0) {
            size.setX(preferredSize.x * -size.doubleX())
        }
        size.setX(min(size.doubleX(), maxSize.x))
        if (size.doubleY() < 0.0) {
            size.setY(preferredSize.y * -size.doubleY())
        }
        size.setY(min(size.doubleY(), maxSize.y))
    }

    protected fun setSize(size: Vector2d,
                          outSize: MutableVector2d) {
        outSize.set(max(size, outSize.now()))
    }

    fun size(): Vector2d {
        val size = size ?: throw IllegalStateException(
                "Size unknown until layout is processed")
        return size - start
    }
}
