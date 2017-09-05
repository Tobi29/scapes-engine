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

package org.tobi29.scapes.engine.graphics

class Pipeline(gl: GL,
               private val builder: (GL) -> suspend () -> (Double) -> Unit) {
    private var finisher: (suspend () -> (Double) -> Unit)? = builder(gl)
    private var steps: ((Double) -> Unit)? = null

    suspend fun finish() {
        finisher?.let { finisher ->
            this.finisher = null
            steps = finisher()
        }
    }

    fun render(delta: Double): Boolean {
        (steps ?: return false)(delta)
        return true
    }

    fun rebuild(gl: GL): Pipeline {
        return Pipeline(gl, builder)
    }
}

fun renderScene(gl: GL,
                scene: Scene): suspend () -> (Double) -> Unit {
    val render = scene.appendToPipeline(gl)
    return {
        val steps = render()
        ;{ delta ->
        steps(delta)
        gl.checkError("Scene-Rendering")
    }
    }
}

fun postProcess(gl: GL,
                shader: Shader,
                framebuffer: Framebuffer,
                config: Shader.() -> Unit): (Double) -> Unit {
    return postProcess(gl, shader, framebuffer, framebuffer, config)
}

fun postProcess(gl: GL,
                shader: Shader,
                framebuffer: Framebuffer,
                depthbuffer: Framebuffer = framebuffer,
                config: Shader.() -> Unit = {}): (Double) -> Unit {
    val model = gl.createVTI(
            floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 1.0f, 0.0f, 0.0f),
            floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f),
            intArrayOf(0, 1, 2, 3, 2, 1), RenderType.TRIANGLES)
    return {
        config(shader)
        gl.clearDepth()
        renderPostProcess(gl, framebuffer, depthbuffer, model, shader)
        gl.checkError("Post-Process")
    }
}

private fun renderPostProcess(gl: GL,
                              framebuffer: Framebuffer,
                              depthbuffer: Framebuffer,
                              model: Model,
                              shader: Shader) {
    gl.disableCulling()
    gl.disableDepthTest()
    gl.setBlending(BlendingMode.NORMAL)
    gl.matrixStack.push { matrix ->
        matrix.identity()
        matrix.modelViewProjection().orthogonal(0.0f, 0.0f, 1.0f, 1.0f)
        gl.setAttribute4f(GL.COLOR_ATTRIBUTE, 1.0f, 1.0f, 1.0f, 1.0f)
        val texturesColor = framebuffer.texturesColor
        val textureColor = texturesColor[0]
        for (j in 1..texturesColor.lastIndex) {
            gl.activeTexture(j + 1)
            texturesColor[j].bind(gl)
        }
        gl.activeTexture(1)
        depthbuffer.textureDepth?.bind(gl)
        gl.activeTexture(0)
        textureColor.bind(gl)
        model.render(gl, shader)
    }
}
