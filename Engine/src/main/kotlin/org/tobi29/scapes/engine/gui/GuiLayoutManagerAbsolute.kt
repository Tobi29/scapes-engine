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
import org.tobi29.scapes.engine.utils.math.vector.plus

class GuiLayoutManagerAbsolute(start: Vector2d,
                               maxSize: Vector2d,
                               components: Set<GuiComponent>) : GuiLayoutManager(
        start, maxSize, components) {

    override fun layout(output: MutableList<Triple<GuiComponent, Vector2d, Vector2d>>) {
        val outSize = MutableVector2d()
        for (component in components) {
            val data = component.parent
            if (data is GuiLayoutDataAbsolute) {
                val size = data.calculateSize(maxSize)
                val asize = size(size, maxSize, maxSize)
                setSize(data.pos() + asize, outSize)
                output.add(Triple(component, data.pos(), asize))
            } else {
                throw IllegalStateException(
                        "Invalid layout node: " + data::class.java)
            }
        }
        this.size = outSize.now()
    }
}
