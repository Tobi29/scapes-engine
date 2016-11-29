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

package org.tobi29.scapes.engine

import org.tobi29.scapes.engine.graphics.Scene
import org.tobi29.scapes.engine.graphics.Texture
import org.tobi29.scapes.engine.gui.GuiComponentImage
import org.tobi29.scapes.engine.gui.GuiState
import org.tobi29.scapes.engine.gui.GuiStyle
import org.tobi29.scapes.engine.resource.Resource
import org.tobi29.scapes.engine.utils.math.PI
import org.tobi29.scapes.engine.utils.math.min
import org.tobi29.scapes.engine.utils.math.sin

class GameStateStartup(private val nextState: GameState, private val image: String, private val scale: Double,
                       scene: Scene, engine: ScapesEngine) : GameState(engine,
        scene) {
    private var icon: GuiComponentImage? = null
    private var time = 0.0
    private var warmUp = 0

    init {
        this.scene = scene
    }

    override fun init() {
        engine.guiStack.addUnfocused("20-Image",
                GuiImage(engine.graphics.textures()[image], engine.guiStyle))
    }

    override val isMouseGrabbed: Boolean
        get() = true

    override fun step(delta: Double) {
        icon?.let { icon ->
            if (warmUp > 20) {
                time += delta / 5.0
                var a = sin(time * PI).toFloat()
                a = min(a * 1.4f, 1.0f)
                icon.setColor(1.0f, 1.0f, 1.0f, a)
                if (time > 1.0) {
                    engine.switchState(nextState)
                }
                Unit
            } else {
                icon.setColor(1.0f, 1.0f, 1.0f, 0.0f)
                warmUp++
            }
        }
    }

    private inner class GuiImage(texture: Resource<Texture>, style: GuiStyle) : GuiState(
            this@GameStateStartup, style) {
        init {
            val tex = texture.get()
            val width = tex.width()
            val height = tex.height()
            val ratio = width.toDouble() / height
            val w = (540.0 * ratio * scale).toInt()
            val h = (540 * scale).toInt()
            spacer()
            icon = addHori(((960 - w) / 2).toDouble(),
                    ((540 - h) / 2).toDouble(), w.toDouble(), h.toDouble()
            ) { GuiComponentImage(it, texture) }
            spacer()
        }
    }
}
