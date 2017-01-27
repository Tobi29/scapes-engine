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

import org.tobi29.scapes.engine.graphics.BlendingMode
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Shader
import org.tobi29.scapes.engine.graphics.push
import org.tobi29.scapes.engine.utils.math.vector.Vector2d
import org.tobi29.scapes.engine.utils.math.vector.div
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap

class GuiStack {
    private val guis = ConcurrentSkipListMap<String, Gui>()
    private val keys = HashMap<Gui, String>()
    private var focus: Gui? = null

    @Synchronized fun add(id: String,
                          add: Gui) {
        val previous = guis.put(id, add)
        if (previous != null) {
            removed(previous)
        }
        keys.put(add, id)
        focus = add
    }

    @Synchronized fun addUnfocused(id: String,
                                   add: Gui) {
        val previous = guis.put(id, add)
        if (previous != null) {
            removed(previous)
        }
        keys.put(add, id)
    }

    operator fun get(id: String): Gui? {
        return guis[id]
    }

    fun has(id: String): Boolean {
        return guis.containsKey(id)
    }

    @Synchronized fun remove(id: String): Gui? {
        val previous = guis.remove(id) ?: return null
        removed(previous)
        return previous
    }

    @Synchronized fun remove(previous: Gui): Boolean {
        if (!guis.values.remove(previous)) {
            return false
        }
        removed(previous)
        return true
    }

    @Synchronized fun swap(remove: Gui,
                           add: Gui): Boolean {
        val id = keys[remove] ?: return false
        guis.put(id, add)
        keys.put(add, id)
        if (removed(remove)) {
            focus = add
        }
        return true
    }

    private fun removed(gui: Gui): Boolean {
        guis.values.remove(gui)
        keys.remove(gui)
        val focus = focus
        if (focus != null && focus == gui) {
            this.focus = null
            return true
        }
        return false
    }

    fun step(delta: Double) {
        guis.values.forEach { gui ->
            if (gui.isValid) {
                gui.update(delta)
            } else {
                remove(gui)
            }
        }
    }

    fun fireEvent(type: GuiEvent,
                  event: GuiComponentEvent): GuiComponent? {
        return fireEvent(event, GuiComponent.sink(type))
    }

    fun fireEvent(event: GuiComponentEvent,
                  listener: (GuiComponent, GuiComponentEvent) -> Boolean): GuiComponent? {
        val guis = ArrayList<Gui>(this.guis.size)
        guis.addAll(this.guis.values)
        for (i in guis.indices.reversed()) {
            val sink = guis[i].fireNewEvent(event, listener)
            if (sink != null) {
                return sink
            }
        }
        return null
    }

    fun fireRecursiveEvent(type: GuiEvent,
                           event: GuiComponentEvent): Set<GuiComponent> {
        return fireRecursiveEvent(event, GuiComponent.sink(type))
    }

    fun fireRecursiveEvent(event: GuiComponentEvent,
                           listener: (GuiComponent, GuiComponentEvent) -> Boolean): Set<GuiComponent> {
        val guis = ArrayList<Gui>(this.guis.size)
        guis.addAll(this.guis.values)
        for (i in guis.indices.reversed()) {
            val sink = guis[i].fireNewRecursiveEvent(event, listener)
            if (!sink.isEmpty()) {
                return sink
            }
        }
        return emptySet()
    }

    fun fireAction(action: GuiAction): Boolean {
        val focus = this.focus
        return focus != null && focus.fireAction(action)
    }

    fun render(gl: GL,
               shader: Shader,
               delta: Double) {
        val container = gl.engine.container
        val framebufferSize = Vector2d(container.contentWidth().toDouble(),
                container.contentHeight().toDouble())
        guis.values.forEach {
            val size = it.baseSize()
            val pixelSize = size / framebufferSize
            gl.disableCulling()
            gl.disableDepthTest()
            gl.setBlending(BlendingMode.NORMAL)
            gl.matrixStack.push { matrix ->
                matrix.identity()
                matrix.modelViewProjection().orthogonal(0.0f, 0.0f,
                        size.floatX(), size.floatY())
                it.render(gl, shader, size, pixelSize, delta)
            }
        }
        guis.values.forEach {
            val size = it.baseSize()
            val pixelSize = size / framebufferSize
            it.renderOverlays(gl, shader, pixelSize)
        }
    }
}
