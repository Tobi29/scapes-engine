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

import kotlinx.coroutines.experimental.Deferred
import org.tobi29.coroutines.tryGet
import org.tobi29.math.vector.Vector2d
import org.tobi29.scapes.engine.graphics.Texture

class GuiComponentImage(
    parent: GuiLayoutData,
    texture: Deferred<Texture>? = null
) : GuiComponent(parent) {
    private var r = 1.0
    private var g = 1.0
    private var b = 1.0
    private var a = 1.0
    var texture: Deferred<Texture>? = texture
        set(value) {
            field = value
            value?.invokeOnCompletion { dirty() }
        }

    init {
        texture?.invokeOnCompletion { dirty() }
    }

    fun setColor(
        r: Double,
        g: Double,
        b: Double,
        a: Double
    ) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
        dirty()
    }

    override fun updateMesh(renderer: GuiRenderer, size: Vector2d) {
        gui.style.border(renderer, size)
        texture?.tryGet()?.let { texture ->
            renderer.texture(texture, 0)
            GuiUtils.rectangle(renderer, 0.0, 0.0, size.x, size.y, r, g, b, a)
        }
    }
}
