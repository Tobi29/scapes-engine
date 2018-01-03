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

import org.tobi29.scapes.engine.graphics.BlendingMode
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Shader
import org.tobi29.scapes.engine.graphics.push
import org.tobi29.scapes.engine.math.vector.Vector2d
import org.tobi29.scapes.engine.math.vector.div
import org.tobi29.scapes.engine.utils.ConcurrentSortedMap

class GuiStack {
    private val guis = ConcurrentSortedMap<String, Gui>()
    private val keys = HashMap<Gui, String>()
    var focus: Gui? = null
        private set

    fun add(id: String,
            add: Gui) {
        synchronized(this) {
            addUnfocused(id, add)
            focus = add
        }
    }

    fun addUnfocused(id: String,
                     add: Gui) {
        synchronized(this) {
            val previous = guis.put(id, add)
            if (previous != null) {
                removed(previous)
            }
            keys.put(add, id)
            add.added()
        }
    }

    operator fun get(id: String): Gui? {
        return guis[id]
    }

    fun has(id: String): Boolean {
        return guis.containsKey(id)
    }

    fun remove(id: String): Gui? {
        return synchronized(this) {
            val previous = guis.remove(id) ?: return@synchronized null
            removed(previous)
            previous
        }
    }

    fun remove(previous: Gui): Boolean {
        return synchronized(this) {
            if (!guis.values.remove(previous)) {
                return@synchronized false
            }
            removed(previous)
            return@synchronized true
        }
    }

    fun swap(remove: Gui,
             add: Gui): Boolean {
        return synchronized(this) {
            val id = keys[remove] ?: return@synchronized false
            guis.put(id, add)
            keys.put(add, id)
            if (removed(remove)) {
                focus = add
            }
            add.added()
            return@synchronized true
        }
    }

    private fun removed(gui: Gui): Boolean {
        guis.values.remove(gui)
        keys.remove(gui)
        gui.removed()
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

    fun <T : GuiComponentEvent> fireEvent(
            type: GuiEvent<T>,
            event: T
    ): GuiComponent? {
        return fireEvent(event, GuiComponent.sink(type))
    }

    fun <T : GuiComponentEvent> fireEvent(
            event: T,
            listener: (GuiComponent, T) -> Boolean
    ): GuiComponent? {
        val guis = ArrayList<Gui>(this.guis.size)
        guis.addAll(this.guis.values)
        return guis.indices.reversed().asSequence()
                .map { guis[it].fireNewEvent(event, listener) }
                .firstOrNull { it != null }
    }

    fun <T : GuiComponentEvent> fireRecursiveEvent(
            type: GuiEvent<T>,
            event: T
    ): Set<GuiComponent>? {
        return fireRecursiveEvent(event, GuiComponent.sink(type))
    }

    fun <T : GuiComponentEvent> fireRecursiveEvent(
            event: T,
            listener: (GuiComponent, T) -> Boolean
    ): Set<GuiComponent>? {
        val guis = ArrayList<Gui>(this.guis.size)
        guis.addAll(this.guis.map { it.value })
        return guis.indices.reversed().asSequence()
                .map { guis[it].fireNewRecursiveEvent(event, listener) }
                .firstOrNull { it != null } ?: emptySet()
    }

    fun fireAction(action: GuiAction): Boolean {
        val focus = this.focus
        return focus != null && focus.fireAction(action)
    }

    fun render(gl: GL,
               shader: Shader,
               delta: Double) {
        val framebufferSize = Vector2d(gl.contentWidth.toDouble(),
                gl.contentHeight.toDouble())
        guis.forEach { (_, it) ->
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
        guis.forEach { (_, it) ->
            val size = it.baseSize()
            val pixelSize = size / framebufferSize
            it.renderOverlays(gl, shader, pixelSize)
        }
    }
}
