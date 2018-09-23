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

import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.Vector3d
import org.tobi29.math.vector.times
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Matrix
import org.tobi29.scapes.engine.graphics.Shader
import org.tobi29.scapes.engine.graphics.push
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.ConcurrentHashSet
import org.tobi29.stdex.Volatile
import org.tobi29.stdex.atomic.AtomicInt
import org.tobi29.stdex.computeAbsent
import org.tobi29.utils.ConcurrentOrderedCollection
import org.tobi29.utils.EventDispatcher
import org.tobi29.utils.ListenerRegistrar
import kotlin.coroutines.experimental.CoroutineContext

abstract class GuiComponent(
    val engine: ScapesEngine,
    val parent: GuiLayoutData,
    listenerParent: EventDispatcher
) : Comparable<GuiComponent> {
    @Volatile
    private var _visible = true
    private val hover = AtomicInt(0)
    private var added = false
    private val children =
        ConcurrentOrderedCollection(naturalOrder<GuiComponent>())
    private val guiEvents =
        ConcurrentHashMap<GuiEvent<*>, MutableSet<(GuiComponentEvent) -> Unit>>()
    protected val taskExecutor: CoroutineContext get() = engine.taskExecutor
    protected val renderExecutor: CoroutineContext get() = engine.graphics
    @Suppress("LeakingThis")
    val gui = gui(parent) ?: this as Gui
    val events by lazy { EventDispatcher(listenerParent) { listeners() } }
    var visible: Boolean
        get() = _visible
        set(value) {
            synchronized(this) {
                if (_visible != value) {
                    _visible = value
                    updateVisiblePropagate()
                    parent.parent?.dirty()
                }
            }
        }
    val isVisible: Boolean
        get() {
            if (!added) return false
            var current = this
            while (true) {
                if (!current.visible) return false
                current = current.parent.parent ?: break
            }
            return true
        }

    constructor(parent: GuiLayoutData) : this(
        parent.parent?.engine ?: throw IllegalStateException(
            "Non root component without parent"
        ), parent,
        parent.parent.events
    )

    internal constructor(
        engine: ScapesEngine,
        parent: GuiLayoutData
    ) : this(engine, parent, engine.events)

    init {
        on(GuiEvent.CLICK_LEFT) { gui.currentSelection = this }
    }

    protected open fun ListenerRegistrar.listeners() {}

    fun <T : GuiComponentEvent> on(
        event: GuiEvent<T>,
        listener: (T) -> Unit
    ) {
        val listeners = guiEvents.computeAbsent(event) { ConcurrentHashSet() }
        @Suppress("UNCHECKED_CAST")
        listeners.add { listener(it as T) }
    }

    fun <T : GuiComponentEvent> fireEvent(
        type: GuiEvent<T>,
        event: T
    ): Boolean {
        val listeners = guiEvents[type]
        if (listeners == null || listeners.isEmpty()) {
            return false
        }
        listeners.forEach { it(event) }
        return true
    }

    fun hoverBegin(event: GuiComponentEvent): Boolean {
        if (hover.getAndIncrement() == 0) {
            return fireEvent(GuiEvent.HOVER_ENTER, event)
        }
        return false
    }

    fun hover(event: GuiComponentEvent): Boolean =
        fireEvent(GuiEvent.HOVER, event)

    fun hoverEnd(event: GuiComponentEvent): Boolean {
        val result = hover.decrementAndGet()
        if (result == 0) {
            return fireEvent(GuiEvent.HOVER_LEAVE, event)
        } else if (result < 0) {
            throw IllegalStateException("Ended more hovers than got started")
        }
        return false
    }

    open fun tooltip(p: GuiContainerRow): (() -> Unit)? = null

    open fun ignoresEvents(): Boolean = false

    open fun render(
        gl: GL,
        shader: Shader,
        size: Vector2d,
        pixelSize: Vector2d,
        delta: Double
    ) {
        if (visible) {
            gl.matrixStack.push { matrix ->
                transform(matrix, size)
                val layout = layoutManager(size)
                for ((component, position, childSize) in layout.layout()) {
                    val pos = applyTransform(-position.x, -position.y, size)
                    if (-pos.x >= -childSize.x && -pos.y >= -childSize.y
                        && -pos.x <= size.x && -pos.y <= size.y) {
                        gl.matrixStack.push { childMatrix ->
                            childMatrix.translate(
                                position.x.toFloat(),
                                position.y.toFloat(), 0.0f
                            )
                            component.render(
                                gl, shader, childSize, pixelSize,
                                delta
                            )
                        }
                    }
                }
            }
        }
    }

    protected open fun renderOverlays(
        gl: GL,
        shader: Shader,
        pixelSize: Vector2d
    ) {
        if (visible) {
            children.forEach {
                it.renderOverlays(gl, shader, pixelSize)
            }
        }
    }

    open fun renderLightweight(renderer: GuiRenderer, size: Vector2d): Boolean =
        render(renderer, size)

    protected open fun render(renderer: GuiRenderer, size: Vector2d): Boolean {
        var hasHeavy = false
        if (visible) {
            val matrixStack = renderer.matrixStack()
            matrixStack.push { matrix ->
                renderer.offset(0x10000)
                transform(matrix, size)
                updateMesh(renderer, size)
                val layout = layoutManager(size)
                for ((component, position, childSize) in layout.layout()) {
                    matrixStack.push { childMatrix ->
                        childMatrix.translate(
                            position.x.toFloat(),
                            position.y.toFloat(), 0.0f
                        )
                        hasHeavy = component.renderLightweight(
                            renderer,
                            childSize
                        ) || hasHeavy
                    }
                }
            }
            renderer.offset(-0x10000)
        }
        return hasHeavy
    }

    protected open fun updateMesh(renderer: GuiRenderer, size: Vector2d) {}

    open fun dirty() {
        parent.parent?.dirty()
    }

    protected fun <T : GuiComponentEvent> fireEvent(
        event: T,
        listener: (GuiComponent, T) -> Boolean
    ): GuiComponent? {
        if (visible) {
            val inside = checkInside(event.x, event.y, event.size)
            if (inside) {
                val layout = layoutManager(event.size)
                layout.layoutReversed().asSequence()
                    .filterNot { it.first.parent.blocksEvents }
                    .mapNotNull {
                        it.first.fireEvent(
                            applyTransform(event, it),
                            listener
                        )
                    }.firstOrNull()?.let { return it }
                if (!ignoresEvents()) {
                    listener.invoke(this, event)
                    return this
                }
            }
        }
        return null
    }

    protected fun <T : GuiComponentEvent> fireRecursiveEvent(
        event: T,
        listener: (GuiComponent, T) -> Boolean
    ): Set<GuiComponent>? {
        if (visible) {
            val inside = checkInside(event.x, event.y, event.size)
            if (inside) {
                var sinks: HashSet<GuiComponent>? = null
                val layout = layoutManager(event.size)
                layout.layoutReversed().asSequence()
                    .filterNot { it.first.parent.blocksEvents }
                    .forEach {
                        it.first.fireRecursiveEvent(
                            applyTransform(event, it),
                            listener
                        )?.let {
                            sinks = sinks ?: HashSet()
                            sinks!!.addAll(it)
                        }
                    }
                if (!ignoresEvents()) {
                    sinks = sinks ?: HashSet()
                    if (listener.invoke(this, event)) {
                        sinks!!.add(this)
                    }
                }
                return sinks
            }
        }
        return null
    }

    fun size(): Vector2d? =
        foldTowards(this, Vector2d.ZERO) { _, _, _, _, size -> size }

    protected fun findSelectable(): GuiComponent? {
        if (parent.selectable) return this
        for (child in children) {
            child.findSelectable()?.let { return it }
        }
        return null
    }

    fun applyTransform(pos: Vector3d, size: Vector2d): Vector3d {
        val matrix = Matrix()
        matrix.identity()
        transform(matrix, size)
        return matrix.modelView().multiply(pos.times(-1.0)).times(-1.0)
    }

    protected open fun transform(matrix: Matrix, size: Vector2d) {}

    override fun compareTo(other: GuiComponent) = when {
        parent.priority > other.parent.priority -> -1
        parent.priority < other.parent.priority -> 1
        else -> 0
    }

    internal fun added() {
        synchronized(this) {
            if (added) return
            added = true
            events.enable()
            init()
        }
        updateVisible()
        children.forEach { it.added() }
        gui.selectDefault()
    }

    fun remove() {
        // TODO: Handle guis
        parent.parent?.remove(this)
    }

    fun remove(component: GuiComponent) {
        children.remove(component)
        dirty()
        component.removed()
    }

    internal fun removed() {
        synchronized(this) {
            if (!added) return
            added = false
            gui.deselect(this)
            events.disable()
            dispose()
        }
        updateVisible()
        children.forEach { it.removed() }
    }

    fun removeAll() {
        children.forEach { remove(it) }
    }

    private fun updateVisiblePropagate() {
        updateVisible()
        for (component in children) {
            component.updateVisiblePropagate()
        }
    }

    fun layoutManager(size: Vector2d): GuiLayoutManager {
        if (children.isEmpty()) {
            return GuiLayoutManagerEmpty
        }
        return newLayoutManager(children, size)
    }

    protected open fun init() {}

    protected open fun updateVisible() {}

    protected open fun dispose() {}

    protected open fun newLayoutManager(
        components: Collection<GuiComponent>,
        size: Vector2d
    ): GuiLayoutManager {
        return GuiLayoutManagerAbsolute(Vector2d.ZERO, size, children)
    }

    protected fun append(component: GuiComponent) {
        children.add(component)
        component.added()
        dirty()
    }

    companion object {
        fun <T : GuiComponentEvent> sink(
            type: GuiEvent<T>
        ): (GuiComponent, T) -> Boolean {
            return { component, event -> component.fireEvent(type, event) }
        }

        fun <T : GuiComponentEvent> sink(
            type: GuiEvent<T>,
            component: GuiComponent
        ): (T) -> Unit {
            return { event -> component.fireEvent(type, event) }
        }
    }
}

fun <T : GuiComponentEvent> GuiComponent.applyTransform(
    event: T,
    component: Triple<GuiComponent, Vector2d, Vector2d>
): T {
    val pos = applyTransform(
        event.x - component.second.x,
        event.y - component.second.y, component.third
    )
    return event.copy(x = pos.x, y = pos.y, size = component.third)
}

fun <T : GuiComponentEvent> GuiComponent.applyTransform(
    event: T,
    childOffset: Vector2d,
    childSize: Vector2d
): T {
    val pos = applyTransform(
        event.x - childOffset.x,
        event.y - childOffset.y, childSize
    )
    return event.copy(x = pos.x, y = pos.y, size = childSize)
}

fun GuiComponent.applyTransform(
    x: Double,
    y: Double,
    size: Vector2d
): Vector3d {
    return applyTransform(Vector3d(x, y, 0.0), size)
}

fun <T : GuiComponentEvent, R> GuiComponent.sendEvent(
    event: T,
    listener: (T) -> R
): R? = foldTowards(this, event) { e, parent, _, offset, size ->
    parent.applyTransform(e, offset, size)
}?.let { listener(it) }

inline fun <T> foldTowards(
    component: GuiComponent,
    initial: T,
    transform: (T, GuiComponent, GuiComponent, Vector2d, Vector2d) -> T
): T? {
    var current = initial
    var size = component.gui.baseSize()
    val path = component.pathTowards()
    // We skip the last element, which is component
    path@ for (i in 0 until path.lastIndex) {
        val element = path[i]
        if (!element.visible) return null
        val layout = element.layoutManager(size)
        val next = path[i + 1]
        for ((child, childOffset, childSize) in layout.layoutReversed()) {
            if (child === next) {
                current = transform(
                    current, element, child, childOffset,
                    childSize
                )
                size = childSize
                continue@path
            }
        }
        return null
    }
    return current
}

fun GuiComponent.pathTowards(): List<GuiComponent> =
    pathOutwards().asReversed()

fun GuiComponent.pathOutwards(): List<GuiComponent> =
    ArrayList<GuiComponent>().apply {
        var current = this@pathOutwards
        while (true) {
            add(current)
            current = current.parent.parent ?: break
        }
    }

private fun checkInside(
    x: Double,
    y: Double,
    size: Vector2d
): Boolean {
    return x >= 0 && y >= 0 && x < size.x && y < size.y
}

private fun gui(parent: GuiLayoutData): Gui? {
    var other = parent.parent ?: return null
    while (true) {
        if (other is Gui) {
            return other
        }
        other = other.parent.parent ?: throw IllegalArgumentException(
            "Non-Gui component has no parent"
        )
    }
}
