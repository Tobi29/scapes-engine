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

import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.utils.graphics.Cam
import org.tobi29.scapes.engine.utils.graphics.Image
import org.tobi29.scapes.engine.utils.math.matrix.Matrix4f
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import java.nio.ByteBuffer
import java.nio.FloatBuffer

abstract class GL protected constructor(val engine: ScapesEngine,
                                        val container: Container) {
    val textures: TextureManager
    val matrixStack: MatrixStack
    protected val vaoTracker: GraphicsObjectTracker<Model>
    protected val textureTracker: GraphicsObjectTracker<Texture>
    protected val fboTracker: GraphicsObjectTracker<Framebuffer>
    protected val shaderTracker: GraphicsObjectTracker<Shader>
    protected var resolutionMultiplier = 1.0
    protected var containerWidth = 1
    protected var containerHeight = 1
    protected var contentWidth = 1
    protected var contentHeight = 1
    var timer = 0.0
        private set
    var currentFBO = 0
    private var mainThread: Thread? = null

    init {
        matrixStack = MatrixStack(64)
        textures = TextureManager(engine)
        vaoTracker = GraphicsObjectTracker<Model>()
        textureTracker = GraphicsObjectTracker<Texture>()
        fboTracker = GraphicsObjectTracker<Framebuffer>()
        shaderTracker = GraphicsObjectTracker<Shader>()
        resolutionMultiplier = engine.config.resolutionMultiplier
        container.loadFont("Engine:font/QuicksandPro-Regular")
    }

    fun init() {
        mainThread = Thread.currentThread()
    }

    fun reshape(contentWidth: Int,
                contentHeight: Int,
                containerWidth: Int,
                containerHeight: Int,
                resolutionMultiplier: Double) {
        this.contentWidth = contentWidth
        this.contentHeight = contentHeight
        this.containerWidth = containerWidth
        this.containerHeight = containerHeight
        this.resolutionMultiplier = resolutionMultiplier
        shaderTracker.disposeAll(this)
    }

    fun step(delta: Double) {
        timer += delta
        // This is over 4 days, should be fine
        if (timer >= 360000.0) {
            timer -= 360000.0
        }
    }

    fun engine(): ScapesEngine {
        return engine
    }

    fun textures(): TextureManager {
        return textures
    }

    fun matrixStack(): MatrixStack {
        return matrixStack
    }

    fun sceneWidth(): Int {
        return (contentWidth * resolutionMultiplier).toInt()
    }

    fun sceneHeight(): Int {
        return (contentHeight * resolutionMultiplier).toInt()
    }

    fun sceneSpace(): Double {
        return max(contentWidth, contentHeight) * resolutionMultiplier / 1920.0
    }

    fun contentWidth(): Int {
        return contentWidth
    }

    fun contentHeight(): Int {
        return contentHeight
    }

    fun contentSpace(): Double {
        return max(contentWidth, contentHeight) / 1920.0
    }

    fun containerWidth(): Int {
        return containerWidth
    }

    fun containerHeight(): Int {
        return containerHeight
    }

    fun vaoTracker(): GraphicsObjectTracker<Model> {
        return vaoTracker
    }

    fun textureTracker(): GraphicsObjectTracker<Texture> {
        return textureTracker
    }

    fun fboTracker(): GraphicsObjectTracker<Framebuffer> {
        return fboTracker
    }

    fun shaderTracker(): GraphicsObjectTracker<Shader> {
        return shaderTracker
    }

    fun currentFBO(): Int {
        return currentFBO
    }

    fun clear() {
        vaoTracker.disposeAll(this)
        textureTracker.disposeAll(this)
        fboTracker.disposeAll(this)
        shaderTracker.disposeAll(this)
    }

    fun reset() {
        vaoTracker.resetAll()
        textureTracker.resetAll()
        fboTracker.resetAll()
        shaderTracker.resetAll()
    }

    fun check() {
        assert(Thread.currentThread() === mainThread)
    }

    abstract fun createTexture(width: Int,
                               height: Int,
                               buffer: ByteBuffer,
                               mipmaps: Int,
                               minFilter: TextureFilter,
                               magFilter: TextureFilter,
                               wrapS: TextureWrap,
                               wrapT: TextureWrap): Texture

    abstract fun createFramebuffer(width: Int,
                                   height: Int,
                                   colorAttachments: Int,
                                   depth: Boolean,
                                   hdr: Boolean,
                                   alpha: Boolean,
                                   minFilter: TextureFilter = TextureFilter.NEAREST,
                                   magFilter: TextureFilter = minFilter): Framebuffer

    abstract fun createModelFast(attributes: List<ModelAttribute>,
                                 length: Int,
                                 renderType: RenderType): Model

    abstract fun createModelStatic(attributes: List<ModelAttribute>,
                                   length: Int,
                                   index: IntArray,
                                   indexLength: Int,
                                   renderType: RenderType): Model

    abstract fun createModelHybrid(
            attributes: List<ModelAttribute>,
            length: Int,
            attributesStream: List<ModelAttribute>,
            lengthStream: Int,
            renderType: RenderType): ModelHybrid

    abstract fun createShader(shader: CompiledShader,
                              information: ShaderCompileInformation): Shader

    abstract fun checkError(message: String)

    abstract fun clear(r: Float,
                       g: Float,
                       b: Float,
                       a: Float)

    abstract fun clearDepth()

    abstract fun disableCulling()

    abstract fun disableDepthTest()

    abstract fun disableDepthMask()

    abstract fun disableWireframe()

    abstract fun disableScissor()

    abstract fun enableCulling()

    abstract fun enableDepthTest()

    abstract fun enableDepthMask()

    abstract fun enableWireframe()

    abstract fun enableScissor(x: Int,
                               y: Int,
                               width: Int,
                               height: Int)

    abstract fun setBlending(mode: BlendingMode)

    abstract fun setViewport(x: Int,
                             y: Int,
                             width: Int,
                             height: Int)

    abstract fun getViewport(output: IntArray)

    abstract fun screenShot(x: Int,
                            y: Int,
                            width: Int,
                            height: Int): Image

    abstract fun screenShotFBO(fbo: Framebuffer): Image

    fun into(framebuffer: Framebuffer,
             block: () -> Unit): () -> Unit {
        val viewport = IntArray(4)
        return {
            getViewport(viewport)
            framebuffer.activate(this)
            setViewport(0, 0, framebuffer.width(), framebuffer.height())
            block()
            framebuffer.deactivate(this)
            setViewport(viewport[0], viewport[1], viewport[2], viewport[3])
        }
    }

    abstract fun setAttribute1f(id: Int,
                                v0: Float)

    abstract fun setAttribute2f(id: Int,
                                v0: Float,
                                v1: Float)

    abstract fun setAttribute3f(id: Int,
                                v0: Float,
                                v1: Float,
                                v2: Float)

    abstract fun setAttribute4f(id: Int,
                                v0: Float,
                                v1: Float,
                                v2: Float,
                                v3: Float)

    abstract fun setAttribute2f(uniform: Int,
                                values: FloatBuffer)

    abstract fun setAttribute3f(uniform: Int,
                                values: FloatBuffer)

    abstract fun setAttribute4f(uniform: Int,
                                values: FloatBuffer)

    abstract fun replaceTexture(x: Int,
                                y: Int,
                                width: Int,
                                height: Int,
                                buffer: ByteBuffer)

    abstract fun replaceTextureMipMap(x: Int,
                                      y: Int,
                                      width: Int,
                                      height: Int,
                                      vararg buffers: ByteBuffer?)

    abstract fun activeTexture(i: Int)

    companion object {
        val VERTEX_ATTRIBUTE = 0
        val COLOR_ATTRIBUTE = 1
        val TEXTURE_ATTRIBUTE = 2
        val NORMAL_ATTRIBUTE = 3
    }
}

fun Matrix4f.camera(cam: Cam) {
    rotateAccurate((-cam.tilt).toDouble(), 0.0f, 0.0f, 1.0f)
    rotateAccurate((-cam.pitch - 90.0f).toDouble(), 1.0f, 0.0f, 0.0f)
    rotateAccurate((-cam.yaw + 90.0f).toDouble(), 0.0f, 0.0f, 1.0f)
}