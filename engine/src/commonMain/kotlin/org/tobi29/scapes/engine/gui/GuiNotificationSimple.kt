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
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.Vector3d
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Matrix
import org.tobi29.scapes.engine.graphics.Shader
import org.tobi29.scapes.engine.graphics.Texture
import kotlin.math.PI
import kotlin.math.sin

class GuiNotificationSimple(
    parent: GuiLayoutData,
    icon: Deferred<Texture>,
    text: String,
    time: Double = 3.0
) : GuiComponentVisibleSlabHeavy(parent) {
    private val speed: Double
    private var progress = 0.0

    init {
        addHori(10.0, 10.0, 40.0, 40.0) { GuiComponentIcon(it, icon) }
        addHori(10.0, 23.0, -1.0, -1.0) { GuiComponentText(it, text) }
        speed = 1.0 / time
    }

    public override fun renderComponent(
        gl: GL,
        shader: Shader,
        size: Vector2d,
        pixelSize: Vector2d,
        delta: Double
    ) {
        progress += speed * delta
        if (progress > 1.1) {
            progress = 1.1
            remove()
        }
    }

    override fun transform(
        matrix: Matrix,
        size: Vector2d
    ) {
        val sin = sin(progress * PI).toFloat() - 1.0f
        var sqr = sin * sin
        sqr *= sqr
        val start = matrix.modelViewMatrix.multiply(Vector3d.ZERO)
        matrix.translate(0.0f, (sqr * sin * (start.y + size.y)).toFloat(), 0.0f)
    }
}
