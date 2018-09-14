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
import org.tobi29.scapes.engine.graphics.*

class GuiComponentBusy(parent: GuiLayoutData) : GuiComponentHeavy(parent) {
    private var r = 1.0f
    private var g = 1.0f
    private var b = 1.0f
    private var a = 1.0f
    private var model: Model? = null

    fun setColor(
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
        dirty()
    }

    public override fun renderComponent(
        gl: GL,
        shader: Shader,
        size: Vector2d,
        pixelSize: Vector2d,
        delta: Double
    ) {
        gl.textureEmpty.bind(gl)
        gl.matrixStack.push { matrix ->
            matrix.translate(
                size.x.toFloat() * 0.5f, size.y.toFloat() * 0.5f,
                0.0f
            )
            matrix.rotateAccurate((gl.timer * 300.0) % 360.0, 0.0f, 0.0f, 1.0f)
            model?.render(gl, shader)
        }
    }

    override fun updateMesh(renderer: GuiRenderer, size: Vector2d) {
        val mesh = Mesh()
        val pixelSize = renderer.pixelSize
        GuiUtils.busy(
            mesh, size.x, size.y, pixelSize.x, pixelSize.y,
            r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble()
        )
        model = mesh.finish(engine.graphics)
    }
}
