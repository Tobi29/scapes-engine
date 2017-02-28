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

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Matrix
import org.tobi29.scapes.engine.graphics.Shader
import org.tobi29.scapes.engine.utils.ListenerOwner
import org.tobi29.scapes.engine.utils.ListenerOwnerHandle
import org.tobi29.scapes.engine.utils.computeAbsent
import org.tobi29.scapes.engine.utils.math.vector.Vector2d
import org.tobi29.scapes.engine.utils.math.vector.Vector3d
import org.tobi29.scapes.engine.utils.math.vector.times
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

abstract class GuiComponent(val parent: GuiLayoutData) : Comparable<GuiComponent>, ListenerOwner {
    @Suppress("LeakingThis")
    val gui = gui(parent) ?: this as Gui
    var visible = true
        set(value) {
            field = value
            parent.parent?.dirty()
        }
    val engine: ScapesEngine
        get() = gui.style.engine
    protected var hover = false
    protected var hovering = false
    protected var removing = false
    internal var removed = false
    protected val components = ConcurrentSkipListSet<GuiComponent>()
    private val events = ConcurrentHashMap<GuiEvent, MutableSet<(GuiComponentEvent) -> Unit>>()
    private val uid = UID_COUNTER.andIncrement
    private val hasActiveChild = AtomicBoolean(true)
    override val listenerOwner = ListenerOwnerHandle { gui.isValid && !removed }

    init {
        on(GuiEvent.CLICK_LEFT, { gui.lastClicked = this })
    }

    fun on(event: GuiEvent,
           listener: (GuiComponentEvent) -> Unit) {
        val listeners = events.computeAbsent(event) {
            Collections.newSetFromMap(ConcurrentHashMap())
        }
        listeners.add(listener)
    }

    fun fireEvent(type: GuiEvent,
                  event: GuiComponentEvent): Boolean {
        val listeners = events[type]
        if (listeners == null || listeners.isEmpty()) {
            return false
        }
        listeners.forEach { it(event) }
        return true
    }

    fun hover(event: GuiComponentEvent): Boolean {
        if (!hovering) {
            fireEvent(GuiEvent.HOVER_ENTER, event)
            hovering = true
        }
        val success = fireEvent(GuiEvent.HOVER, event)
        hover = true
        parent.parent?.activeUpdate()
        return success
    }

    open fun tooltip(p: GuiContainerRow): (() -> Unit)? = null

    protected fun checkInside(x: Double,
                              y: Double,
                              size: Vector2d): Boolean {
        return x >= 0 && y >= 0 && x < size.x && y < size.y
    }

    open fun ignoresEvents(): Boolean {
        return false
    }

    open fun render(gl: GL,
                    shader: Shader,
                    size: Vector2d,
                    pixelSize: Vector2d,
                    delta: Double) {
        if (visible) {
            val matrixStack = gl.matrixStack()
            val matrix = matrixStack.push()
            transform(matrix, size)
            val layout = layoutManager(size)
            for (component in layout.layout()) {
                val pos = applyTransform(-component.second.x,
                        -component.second.y, size)
                if (-pos.x >= -component.third.x &&
                        -pos.y >= -component.third.y &&
                        -pos.x <= size.x &&
                        -pos.y <= size.y) {
                    val childMatrix = matrixStack.push()
                    childMatrix.translate(component.second.floatX(),
                            component.second.floatY(), 0.0f)
                    component.first.render(gl, shader, component.third,
                            pixelSize,
                            delta)
                    matrixStack.pop()
                }
            }
            matrixStack.pop()
        }
    }

    protected open fun renderOverlays(gl: GL,
                                      shader: Shader,
                                      pixelSize: Vector2d) {
        if (visible) {
            components.forEach { it.renderOverlays(gl, shader, pixelSize) }
        }
    }

    open fun renderLightweight(renderer: GuiRenderer,
                               size: Vector2d): Boolean {
        return render(renderer, size)
    }

    protected open fun render(renderer: GuiRenderer,
                              size: Vector2d): Boolean {
        var hasHeavy = false
        if (visible) {
            val matrixStack = renderer.matrixStack()
            val matrix = matrixStack.push()
            renderer.offset(0x10000)
            transform(matrix, size)
            updateMesh(renderer, size)
            val layout = layoutManager(size)
            for (component in layout.layout()) {
                val childMatrix = matrixStack.push()
                childMatrix.translate(component.second.floatX(),
                        component.second.floatY(),
                        0.0f)
                hasHeavy = hasHeavy or component.first.renderLightweight(
                        renderer,
                        component.third)
                matrixStack.pop()
            }
            matrixStack.pop()
            renderer.offset(-0x10000)
        }
        return hasHeavy
    }

    protected open fun update(delta: Double) {
        if (visible) {
            if (hovering && !hover) {
                hovering = false
                size()?.let { size ->
                    fireEvent(GuiEvent.HOVER_LEAVE,
                            GuiComponentEvent(Double.NaN,
                                    Double.NaN, size))
                }
            }
            if (hover) {
                parent.parent?.activeUpdate()
                hover = false
            }
            if (hasActiveChild.getAndSet(false)) {
                components.forEach { component ->
                    if (component.removing) {
                        remove(component)
                    } else {
                        component.update(delta)
                    }
                }
            }
        }
    }

    protected open fun updateMesh(renderer: GuiRenderer,
                                  size: Vector2d) {
    }

    open fun dirty() {
        parent.parent?.dirty()
    }

    internal fun activeUpdate() {
        hasActiveChild.set(true)
        parent.parent?.activeUpdate()
    }

    protected fun fireEvent(event: GuiComponentEvent,
                            listener: (GuiComponent, GuiComponentEvent) -> Boolean): GuiComponent? {
        if (visible) {
            val inside = checkInside(event.x, event.y, event.size)
            if (inside) {
                val layout = layoutManager(event.size)
                for (component in layout.layout()) {
                    if (!component.first.parent.blocksEvents) {
                        val sink = component.first.fireEvent(
                                applyTransform(event, component),
                                listener)
                        if (sink != null) {
                            return sink
                        }
                    }
                }
                if (!ignoresEvents()) {
                    listener.invoke(this, event)
                    return this
                }
            }
        }
        return null
    }

    protected fun fireRecursiveEvent(event: GuiComponentEvent,
                                     listener: (GuiComponent, GuiComponentEvent) -> Boolean): Set<GuiComponent> {
        if (visible) {
            val inside = checkInside(event.x, event.y, event.size)
            if (inside) {
                val sinks = HashSet<GuiComponent>()
                val layout = layoutManager(event.size)
                for (component in layout.layout()) {
                    if (!component.first.parent.blocksEvents) {
                        sinks.addAll(component.first.fireRecursiveEvent(
                                applyTransform(event, component), listener))
                    }
                }
                if (!ignoresEvents()) {
                    if (listener.invoke(this, event)) {
                        sinks.add(this)
                    }
                }
                return sinks
            }
        }
        return emptySet()
    }

    protected fun sendEvent(event: GuiComponentEvent,
                            destination: GuiComponent,
                            listener: (GuiComponentEvent) -> Unit): Boolean {
        if (visible) {
            val layout = layoutManager(event.size)
            for (component in layout.layout()) {
                if (!component.first.parent.blocksEvents) {
                    val success = component.first.sendEvent(
                            applyTransform(event, component),
                            destination, listener)
                    if (success) {
                        return true
                    }
                }
            }
            if (destination === this) {
                listener(event)
                return true
            }
        }
        return false
    }

    protected fun calculateSize(size: Vector2d,
                                destination: GuiComponent): Vector2d? {
        if (visible) {
            val layout = layoutManager(size)
            for (component in layout.layout()) {
                val success = component.first.calculateSize(component.third,
                        destination)
                if (success != null) {
                    return success
                }
            }
            if (destination === this) {
                return size
            }
        }
        return null
    }

    fun size(): Vector2d? {
        return gui.calculateSize(gui.baseSize(), this)
    }

    protected fun applyTransform(event: GuiComponentEvent,
                                 component: Triple<GuiComponent, Vector2d, Vector2d>): GuiComponentEvent {
        val pos = applyTransform(event.x - component.second.x,
                event.y - component.second.y, component.third)
        return GuiComponentEvent(event, pos.x, pos.y,
                component.third)
    }

    protected fun applyTransform(x: Double,
                                 y: Double,
                                 size: Vector2d): Vector3d {
        return applyTransform(Vector3d(x, y, 0.0), size)
    }

    protected fun applyTransform(pos: Vector3d,
                                 size: Vector2d): Vector3d {
        val matrix = Matrix()
        matrix.identity()
        transform(matrix, size)
        return matrix.modelView().multiply(pos.times(-1.0)).times(-1.0)
    }

    protected open fun transform(matrix: Matrix,
                                 size: Vector2d) {
    }

    override fun hashCode(): Int {
        return uid.toInt()
    }

    override fun equals(other: Any?): Boolean {
        return other is GuiComponent && uid == other.uid
    }

    override fun compareTo(other: GuiComponent): Int {
        if (parent.priority > other.parent.priority) {
            return -1
        }
        if (parent.priority < other.parent.priority) {
            return 1
        }
        if (uid > other.uid) {
            return 1
        }
        if (uid < other.uid) {
            return -1
        }
        return 0
    }

    fun remove() {
        removing = true
    }

    fun remove(component: GuiComponent) {
        components.remove(component)
        dirty()
        component.removed()
    }

    private fun removed() {
        removed = true
        components.forEach { it.removed() }
    }

    fun removeAll() {
        components.forEach { remove(it) }
    }

    protected fun layoutManager(size: Vector2d): GuiLayoutManager {
        if (components.isEmpty()) {
            return GuiLayoutManagerEmpty
        }
        return newLayoutManager(size)
    }

    protected open fun newLayoutManager(size: Vector2d): GuiLayoutManager {
        return GuiLayoutManagerAbsolute(Vector2d.ZERO, size, components)
    }

    protected fun append(component: GuiComponent) {
        components.add(component)
        activeUpdate()
        dirty()
    }

    companion object {
        private val UID_COUNTER = AtomicLong(Long.MIN_VALUE)

        fun sink(
                type: GuiEvent): Function2<GuiComponent, GuiComponentEvent, Boolean> {
            return { component, event -> component.fireEvent(type, event) }
        }

        fun sink(type: GuiEvent,
                 component: GuiComponent): (GuiComponentEvent) -> Unit {
            return { event -> component.fireEvent(type, event) }
        }

        fun gui(parent: GuiLayoutData): Gui? {
            var other = parent.parent ?: return null
            while (true) {
                if (other is Gui) {
                    return other
                }
                other = other.parent.parent ?: throw IllegalArgumentException(
                        "Non-Gui component has no parent")
            }
        }
    }
}
