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

open class GuiLayoutData(val parent: GuiComponent?,
                         size: Vector2d,
                         val priority: Long,
                         val blocksEvents: Boolean) {
    private val size = MutableVector2d()

    init {
        this.size.set(size)
    }

    fun width(): Double {
        return size.doubleX()
    }

    fun height(): Double {
        return size.doubleY()
    }

    fun setWidth(value: Double) {
        size.setX(value)
    }

    fun setHeight(value: Double) {
        size.setY(value)
    }

    fun setSize(size: Vector2d) {
        this.size.set(size)
    }
}
