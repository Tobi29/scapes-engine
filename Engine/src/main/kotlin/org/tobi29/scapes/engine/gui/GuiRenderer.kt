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

import org.tobi29.scapes.engine.graphics.MatrixStack
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.Vector3d

class GuiRenderer : GuiRenderBatch(Vector2d(1.0, 1.0)) {
    private val matrixStack = MatrixStack(64)

    fun matrixStack(): MatrixStack {
        return matrixStack
    }

    override fun vector(x: Double,
                        y: Double): Vector3d {
        val matrix = matrixStack.current()
        return matrix.modelView().multiply(Vector3d(x, y, 0.0))
    }
}
