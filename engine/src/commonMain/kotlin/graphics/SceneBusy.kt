/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.scapes.engine.graphics

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.gui.GuiUtils

class SceneBusy(engine: ScapesEngine) : Scene(engine) {
    override fun appendToPipeline(gl: GL): suspend () -> (Double) -> Unit {
        val busy = busyPipeline(gl)
        return {
            val busyRender = busy()
            ;{ _ ->
            gl.clear(0.0f, 0.0f, 0.0f, 1.0f)
            busyRender()
        }
        }
    }
}

fun busyPipeline(gl: GL): suspend () -> () -> Unit {
    val mesh = Mesh()
    GuiUtils.busy(mesh, 64.0, 64.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0)
    val busy = mesh.finish(gl)
    return {
        val shader = gl.createShader(SHADER_TEXTURED(), emptyMap())
        ;{
        gl.disableCulling()
        gl.disableDepthTest()
        gl.setBlending(BlendingMode.NORMAL)
        gl.matrixStack.push { matrix ->
            val width = gl.contentWidth.toFloat()
            val height = gl.contentHeight.toFloat()
            matrix.identity()
            matrix.modelViewProjectionMatrix.orthogonal(
                -width * 0.5f, -height * 0.5f, width, height
            )
            gl.textureEmpty.bind(gl)
            matrix.rotateAccurate((gl.timer * 300.0) % 360.0, 0.0f, 0.0f, 1.0f)
            busy.render(gl, shader)
        }
    }
    }
}
