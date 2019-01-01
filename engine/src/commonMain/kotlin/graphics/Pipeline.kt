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

class Pipeline(
    gl: GL,
    private val builder: (GL) -> suspend () -> (Double) -> Unit
) {
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

fun renderScene(
    gl: GL,
    scene: Scene
): suspend () -> (Double) -> Unit {
    val render = scene.appendToPipeline(gl)
    return {
        val steps = render()
        ;{ delta ->
        steps(delta)
        gl.checkError("Scene-Rendering")
    }
    }
}

inline fun renderInto(
    gl: GL,
    framebuffer: Framebuffer,
    crossinline block: (Double) -> Unit
): (Double) -> Unit {
    val viewport = IntArray(4)
    return { delta ->
        gl.getViewport(viewport)
        framebuffer.activate(gl)
        gl.setViewport(0, 0, framebuffer.width(), framebuffer.height())
        try {
            block(delta)
        } finally {
            framebuffer.deactivate(gl)
            gl.setViewport(viewport[0], viewport[1], viewport[2], viewport[3])
        }
    }
}

inline fun postProcess(
    gl: GL,
    shader: Shader,
    framebuffer: Framebuffer,
    depthbuffer: Framebuffer = framebuffer,
    crossinline config: Shader.() -> Unit = {}
): (Double) -> Unit {
    val model = postProcessModel(gl)
    return {
        config(shader)
        gl.clearDepth()
        renderPostProcess(gl, framebuffer, depthbuffer, model, shader)
        gl.checkError("Post-Process")
    }
}

@PublishedApi
internal fun postProcessModel(gl: GL) = gl.createVTI(
    floatArrayOf(
        0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f
    ),
    floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f),
    intArrayOf(0, 1, 2, 3, 2, 1), RenderType.TRIANGLES
)

@PublishedApi
internal fun renderPostProcess(
    gl: GL,
    framebuffer: Framebuffer,
    depthbuffer: Framebuffer,
    model: Model,
    shader: Shader
) {
    gl.disableCulling()
    gl.disableDepthTest()
    gl.setBlending(BlendingMode.NORMAL)
    gl.matrixStack.push { matrix ->
        matrix.identity()
        matrix.modelViewProjectionMatrix.orthogonal(0.0f, 0.0f, 1.0f, 1.0f)
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
