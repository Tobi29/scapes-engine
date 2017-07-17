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

import org.tobi29.scapes.engine.utils.Pool
import org.tobi29.scapes.engine.utils.ThreadLocal
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.min
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d
import org.tobi29.scapes.engine.utils.math.vector.Vector2d
import org.tobi29.scapes.engine.utils.math.vector.minus

abstract class GuiLayoutManager(protected val start: Vector2d,
                                protected val maxSize: Vector2d,
                                components: Collection<GuiComponent>) {
    protected val components: MutableList<GuiComponent>
    protected var sizeMut: Vector2d? = null

    init {
        this.components = ArrayList(components.size)
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

    protected fun size(size: Vector2d,
                       preferredSize: Vector2d,
                       maxSize: Vector2d): Vector2d {
        var x = size.x
        var y = size.y
        if (x < 0.0) {
            x = preferredSize.x * -x
        }
        x = max(min(x, maxSize.x), 0.0)
        if (y < 0.0) {
            y = preferredSize.y * -y
        }
        y = max(min(y, maxSize.y), 0.0)
        return Vector2d(x, y)
    }

    protected fun size(size: MutableVector2d,
                       preferredSize: MutableVector2d,
                       maxSize: MutableVector2d): Vector2d {
        var x = size.x
        var y = size.y
        if (x < 0.0) {
            x = preferredSize.x * -x
        }
        x = max(min(x, maxSize.x), 0.0)
        if (y < 0.0) {
            y = preferredSize.y * -y
        }
        y = max(min(y, maxSize.y), 0.0)
        return Vector2d(x, y)
    }

    protected fun size(size: Vector2d,
                       preferredSize: MutableVector2d,
                       maxSize: MutableVector2d): Vector2d {
        var x = size.x
        var y = size.y
        if (x < 0.0) {
            x = preferredSize.x * -x
        }
        x = max(min(x, maxSize.x), 0.0)
        if (y < 0.0) {
            y = preferredSize.y * -y
        }
        y = max(min(y, maxSize.y), 0.0)
        return Vector2d(x, y)
    }

    protected fun setSize(size: Vector2d,
                          outSize: MutableVector2d) {
        outSize.x = max(size.x, outSize.x)
        outSize.y = max(size.y, outSize.y)
    }

    protected fun setSize(size: MutableVector2d,
                          outSize: MutableVector2d) {
        outSize.x = max(size.x, outSize.x)
        outSize.y = max(size.y, outSize.y)
    }

    fun size(): Vector2d {
        val size = sizeMut ?: throw IllegalStateException(
                "Size unknown until layout is processed")
        return size - start
    }

    companion object {
        val sizeCache = ThreadLocal { Pool { Pool { SizeCacheEntry() } } }

        class SizeCacheEntry {
            var component: GuiComponent? = null
            var size = Vector2d.ZERO

            operator fun component1() = component

            operator fun component2() = size

            fun set(component: GuiComponent,
                    size: Vector2d): SizeCacheEntry {
                this.component = component
                this.size = size
                return this
            }
        }
    }
}

fun mangleSize(size: Vector2d,
               maxSize: Vector2d): Vector2d {
    val x = size.x
    val y = size.y
    return if (x >= 0.0 && y >= 0.0) {
        size
    } else if (x < 0.0 && y < 0.0) {
        maxSize
    } else {
        Vector2d(if (x >= 0.0) x else maxSize.x, if (y >= 0.0) y else maxSize.y)
    }
}
