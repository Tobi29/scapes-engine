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

import org.tobi29.scapes.engine.graphics.Texture
import org.tobi29.scapes.engine.resource.Resource
import org.tobi29.scapes.engine.utils.math.vector.Vector2d

class GuiComponentIcon(parent: GuiLayoutData,
                       texture: Resource<Texture>? = null) : GuiComponent(
        parent) {
    var texture: Resource<Texture>? = texture
        set(value) {
            field = value
            value?.onLoaded { dirty() }
        }
    private var r = 1.0f
    private var g = 1.0f
    private var b = 1.0f
    private var a = 1.0f

    init {
        texture?.onLoaded { dirty() }
    }

    fun setColor(r: Float,
                 g: Float,
                 b: Float,
                 a: Float) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
        dirty()
    }

    override fun updateMesh(renderer: GuiRenderer,
                            size: Vector2d) {
        texture?.tryGet()?.let { texture ->
            renderer.texture(texture, 0)
            GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(),
                    size.floatY(), r, g, b, a)
        }
    }
}
