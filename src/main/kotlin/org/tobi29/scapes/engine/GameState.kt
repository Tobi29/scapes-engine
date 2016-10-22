/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine

import org.tobi29.scapes.engine.graphics.*
import java.util.concurrent.atomic.AtomicReference

abstract class GameState(val engine: ScapesEngine, protected var scene: Scene) {
    protected val model: Model
    protected val newScene = AtomicReference<Scene>()
    private val shaderTextured: Shader
    private val shaderGui: Shader
    protected var fbos: Array<Framebuffer>? = null

    init {
        newScene.set(scene)
        model = createVTI(engine,
                floatArrayOf(0.0f, 540.0f, 0.0f, 960.0f, 540.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 960.0f, 0.0f, 0.0f),
                floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f),
                intArrayOf(0, 1, 2, 3, 2, 1), RenderType.TRIANGLES)
        val graphics = engine.graphics
        shaderTextured = graphics.createShader("Engine:shader/Textured")
        shaderGui = graphics.createShader("Engine:shader/Gui")
    }

    open val tps: Double = 60.0

    fun engine(): ScapesEngine {
        return engine
    }

    fun disposeState(gl: GL) {
        scene.dispose(gl)
    }

    fun disposeState() {
        scene.dispose()
        dispose()
    }

    open fun dispose() {
    }

    abstract fun init()

    abstract val isMouseGrabbed: Boolean

    fun scene(): Scene {
        return scene
    }

    fun switchScene(scene: Scene) {
        newScene.set(scene)
    }

    abstract fun step(delta: Double)

    fun render(gl: GL,
               delta: Double,
               updateSize: Boolean) {
        val newScene = this.newScene.getAndSet(null)
        if (newScene != null) {
            scene.dispose(gl)
            scene.dispose()
            fbos = null
            newScene.init(gl)
            scene = newScene
        }
        val sceneWidth = scene.width(gl.sceneWidth())
        val sceneHeight = scene.height(gl.sceneHeight())
        if (fbos == null || updateSize) {
            fbos = Array(scene.renderPasses()) {
                val fbo = engine.graphics.createFramebuffer(sceneWidth,
                        sceneHeight, scene.colorAttachments(), true, true,
                        false)
                scene.initFBO(it, fbo)
                fbo
            }
        }
        val fbos = fbos ?: throw IllegalStateException("FBOs not initialized")
        gl.checkError("Initializing-Scene-Rendering")
        fbos[0].activate(gl)
        gl.viewport(0, 0, sceneWidth, sceneHeight)
        gl.clearDepth()
        scene.renderScene(gl)
        fbos[0].deactivate(gl)
        gl.checkError("Scene-Rendering")
        gl.setProjectionOrthogonal(0.0f, 0.0f, 960.0f, 540.0f)
        for (i in 0..fbos.size - 1 - 1) {
            fbos[i + 1].activate(gl)
            //gl.viewport(0, 0, gl.sceneWidth(), gl.sceneHeight());
            renderPostProcess(gl, fbos[i], fbos[i], i)
            fbos[i + 1].deactivate(gl)
        }
        gl.viewport(0, 0, gl.contentWidth(), gl.contentHeight())
        renderPostProcess(gl, fbos[fbos.size - 1], fbos[0],
                fbos.size - 1)
        gl.checkError("Post-Processing")
        gl.setProjectionOrthogonal(0.0f, 0.0f,
                engine.container.containerWidth().toFloat() / engine.container.containerHeight() * 540.0f,
                540.0f)
        engine.guiStack.render(gl, shaderGui, delta)
        gl.checkError("Gui-Rendering")
        scene.postRender(gl, delta)
        gl.checkError("Post-Render")
    }

    fun renderPostProcess(gl: GL,
                          fbo: Framebuffer,
                          depthFBO: Framebuffer,
                          i: Int) {
        gl.setAttribute4f(GL.COLOR_ATTRIBUTE, 1.0f, 1.0f, 1.0f, 1.0f)
        val texturesColor = fbo.texturesColor().iterator()
        val textureColor = texturesColor.next()
        var j = 2
        while (texturesColor.hasNext()) {
            gl.activeTexture(j)
            texturesColor.next().bind(gl)
            j++
        }
        gl.activeTexture(1)
        depthFBO.textureDepth().bind(gl)
        gl.activeTexture(0)
        textureColor.bind(gl)
        var shader: Shader? = scene.postProcessing(gl, i)
        if (shader == null) {
            shader = shaderTextured
        }
        model.render(gl, shader)
    }

    fun fbo(i: Int): Framebuffer {
        val fbos = fbos ?: throw IllegalStateException("FBOs not initialized")
        return fbos[i]
    }
}
