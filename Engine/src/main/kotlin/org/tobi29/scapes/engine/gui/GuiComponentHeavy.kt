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
import org.tobi29.scapes.engine.graphics.*
import org.tobi29.scapes.engine.utils.AtomicBoolean
import org.tobi29.scapes.engine.utils.ThreadLocal
import org.tobi29.scapes.engine.utils.math.vector.Vector2d

abstract class GuiComponentHeavy : GuiComponent {
    private val dirty = AtomicBoolean(true)
    protected var meshes: List<Pair<Model, Texture>>? = null
    private var lastSize = Vector2d.ZERO
    private var lastPixelSize = Vector2d.ZERO
    private var hasHeavyChild = false

    constructor(
            parent: GuiLayoutData
    ) : super(parent)

    internal constructor(
            engine: ScapesEngine,
            parent: GuiLayoutData
    ) : super(engine, parent)

    override fun render(gl: GL,
                        shader: Shader,
                        size: Vector2d,
                        pixelSize: Vector2d,
                        delta: Double) {
        if (visible) {
            gl.matrixStack.push { matrix ->
                transform(matrix, size)
                if (dirty.getAndSet(false) || lastSize != size ||
                        lastPixelSize != pixelSize) {
                    lastPixelSize = pixelSize
                    val renderer = RENDERER.get()
                    renderer.pixelSize = pixelSize
                    hasHeavyChild = render(renderer, size)
                    meshes = renderer.finish()
                    lastSize = size
                }
                meshes?.forEach {
                    it.second.bind(gl)
                    it.first.render(gl, shader)
                }
                renderComponent(gl, shader, size, pixelSize, delta)
                if (hasHeavyChild) {
                    val layout = layoutManager(size)
                    for ((component, position, childSize) in layout.layout()) {
                        val pos = applyTransform(-position.x, -position.y, size)
                        if (-pos.x >= -childSize.x && -pos.y >= -childSize.y
                                && -pos.x <= size.x && -pos.y <= size.y) {
                            gl.matrixStack.push { childMatrix ->
                                childMatrix.translate(position.floatX(),
                                        position.floatY(), 0.0f)
                                component.render(gl, shader, childSize,
                                        pixelSize, delta)
                            }
                        }
                    }
                }
            }
        }
    }

    public override fun renderOverlays(gl: GL,
                                       shader: Shader,
                                       pixelSize: Vector2d) {
        super.renderOverlays(gl, shader, pixelSize)
        if (visible) {
            renderOverlay(gl, shader, pixelSize)
        }
    }

    override fun renderLightweight(renderer: GuiRenderer,
                                   size: Vector2d): Boolean {
        return true
    }

    override fun render(renderer: GuiRenderer,
                        size: Vector2d): Boolean {
        var hasHeavy = false
        val matrixStack = renderer.matrixStack()
        updateMesh(renderer, size)
        val layout = layoutManager(size)
        for ((component, position, childSize) in layout.layout()) {
            matrixStack.push { childMatrix ->
                childMatrix.translate(position.floatX(), position.floatY(),
                        0.0f)
                hasHeavy = hasHeavy or component.renderLightweight(renderer,
                        childSize)
            }
        }
        return hasHeavy
    }

    override fun update(delta: Double) {
        super.update(delta)
        if (visible) {
            updateComponent(delta)
        }
        parent.parent?.activeUpdate()
    }

    override fun dirty() {
        dirty.set(true)
    }

    protected open fun updateComponent(delta: Double) {
    }

    protected open fun renderComponent(gl: GL,
                                       shader: Shader,
                                       size: Vector2d,
                                       pixelSize: Vector2d,
                                       delta: Double) {
    }

    protected open fun renderOverlay(gl: GL,
                                     shader: Shader,
                                     pixelSize: Vector2d) {
    }

    companion object {
        private val RENDERER = ThreadLocal { GuiRenderer() }
    }
}
