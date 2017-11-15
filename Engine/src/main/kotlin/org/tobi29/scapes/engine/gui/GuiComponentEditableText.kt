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

import org.tobi29.scapes.engine.graphics.*
import org.tobi29.scapes.engine.math.vector.Vector2d
import org.tobi29.scapes.engine.utils.math.clamp
import kotlin.math.min

class GuiComponentEditableText(
        parent: GuiLayoutData,
        text: String,
        private val maxLength: Int,
        private val active: () -> Boolean,
        private val r: Float = 1.0f,
        private val g: Float = 1.0f,
        private val b: Float = 1.0f,
        private val a: Float = 1.0f
) : GuiComponentHeavy(parent) {
    private val data = GuiController.TextFieldData()
    private var vaoCursor: List<Pair<Model, Texture>>? = null
    private var vaoSelection: List<Pair<Model, Texture>>? = null
    private var focused = false
    var textFilter: (String) -> String = { it }
        set(value) {
            field = value
            dirty()
        }

    constructor(parent: GuiLayoutData,
                text: String,
                active: () -> Boolean,
                r: Float = 1.0f,
                g: Float = 1.0f,
                b: Float = 1.0f,
                a: Float = 1.0f
    ) : this(parent, text, Int.MAX_VALUE, active, r, g, b, a)

    init {
        data.text.append(text)
        data.cursor = data.text.length
        dirty()
    }

    val isActive: Boolean get() = active()

    var text: String
        get() = synchronized(data) { data.text.toString() }
        set(value) = synchronized(data) {
            if (data.text.toString() != text) {
                data.text.clear()
                data.text.append(text)
                dirty()
            }
        }

    override fun updateMesh(renderer: GuiRenderer,
                            size: Vector2d) {
        var text = ""
        var selectionStart = 0
        var selectionEnd = 0
        synchronized(data) {
            text = data.text.toString()
            selectionStart = clamp(data.selectionStart, -1, text.length)
            selectionEnd = clamp(data.selectionEnd, 0, text.length)
        }
        val font = gui.style.font
        font.render(FontRenderer.to(renderer, r, g, b, a),
                textFilter(text), size.floatY(), size.floatX())
        val batch = GuiRenderBatch(renderer.pixelSize)
        val cursor = clamp(data.cursor, 0, text.length)
        font.render(FontRenderer.to(batch, -size.floatY() * 0.1f,
                -size.floatY() * 0.2f, 1.0f, 1.0f, 1.0f, 1.0f),
                text.substring(0, cursor) + '|', size.floatY(),
                size.floatY() * 1.2f, size.floatY(), Float.MAX_VALUE,
                cursor,
                cursor + 1)
        vaoCursor = batch.finish()
        if (selectionStart >= 0) {
            font.render(
                    FontRenderer.to(batch, 0.0f, 0.0f, true, 1.0f, 1.0f, 1.0f,
                            1.0f), text,
                    size.floatY(), size.floatY(), size.floatY(), size.floatX(),
                    selectionStart, selectionEnd)
            vaoSelection = batch.finish()
        } else {
            vaoSelection = null
        }
    }

    override fun updateComponent(delta: Double) {
        if (isActive) {
            if (!focused) {
                engine.guiController.focusTextField({ isActive }, data, false)
                focused = true
            }
        } else if (focused) {
            data.selectionStart = -1
            data.cursor = data.text.length
            focused = false
        }
        if (data.dirty.getAndSet(false)) {
            size()?.let { size ->
                synchronized(data) {
                    if (data.text.length > maxLength) {
                        data.text.delete(maxLength, data.text.length)
                        data.cursor = min(data.cursor, maxLength)
                    }
                    val font = gui.style.font
                    val textInfo = font.render(FontRenderer.to(),
                            textFilter(data.text.toString()),
                            size.floatY(), size.floatX())
                    val maxLengthFont = textInfo.length
                    if (data.text.length > maxLengthFont) {
                        data.text.delete(maxLengthFont, data.text.length)
                        data.cursor = min(data.cursor, maxLengthFont)
                    }
                }
            }
            dirty()
        }
    }

    public override fun renderComponent(gl: GL,
                                        shader: Shader,
                                        size: Vector2d,
                                        pixelSize: Vector2d,
                                        delta: Double) {
        super.renderComponent(gl, shader, size, pixelSize, delta)
        if (isActive) {
            if (gl.timestamp / 600000000L % 2L == 0L) {
                vaoCursor?.forEach {
                    it.second.bind(gl)
                    it.first.render(gl, shader)
                }
            }
        }
        gl.textureEmpty().bind(gl)
        gl.setBlending(BlendingMode.INVERT)
        vaoSelection?.forEach { it.first.render(gl, shader) }
        gl.setBlending(BlendingMode.NORMAL)
    }
}
