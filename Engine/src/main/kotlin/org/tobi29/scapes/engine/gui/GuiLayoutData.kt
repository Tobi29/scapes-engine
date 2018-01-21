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

import org.tobi29.math.vector.Vector2d

open class GuiLayoutData(val parent: GuiComponent?,
                         val size: Vector2d,
                         val priority: Long,
                         val blocksEvents: Boolean) {
    var preferredSize: (Vector2d, Vector2d) -> Vector2d = { a, _ -> a }
    var selectable = false

    fun calculateSize(maxSize: Vector2d) = preferredSize(size, maxSize)
}
