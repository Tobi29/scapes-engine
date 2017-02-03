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

class GuiCursor {
    private val pos = MutableVector2d()
    private val guiPos = MutableVector2d()

    fun set(pos: Vector2d,
            guiPos: Vector2d) {
        this.pos.set(pos)
        this.guiPos.set(guiPos)
    }

    fun currentPos(): Vector2d {
        return pos.now()
    }

    fun currentGuiPos(): Vector2d {
        return guiPos.now()
    }
}
