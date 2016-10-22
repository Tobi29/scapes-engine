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

abstract class GL protected constructor(val engine: ScapesEngine, val container: Container) {
    val textures: TextureManager
    val matrixStack: MatrixStack
    val projectionMatrix = Matrix4f()
    val modelViewProjectionMatrix = Matrix4f()
    protected val vaoTracker: GraphicsObjectTracker<Model>
    protected val textureTracker: GraphicsObjectTracker<Texture>
    protected val fboTracker: GraphicsObjectTracker<Framebuffer>
    protected val shaderTracker: GraphicsObjectTracker<Shader>
    protected var resolutionMultiplier = 1.0
    protected var containerWidth = 1
    protected var containerHeight = 1
    protected var contentWidth = 1
    protected var contentHeight = 1
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

    fun engine(): ScapesEngine {
        return engine
    }

    fun textures(): TextureManager {
        return textures
    }

    fun matrixStack(): MatrixStack {
        return matrixStack
    }

    fun projectionMatrix(): Matrix4f {
        return projectionMatrix
    }

    fun modelViewProjectionMatrix(): Matrix4f {
        projectionMatrix.multiply(matrixStack.current().modelView(),
                modelViewProjectionMatrix)
        return modelViewProjectionMatrix
    }

    fun setProjectionPerspective(width: Float,
                                 height: Float,
                                 cam: Cam) {
        projectionMatrix.identity()
        projectionMatrix.perspective(cam.fov, width / height, cam.near, cam.far)
        val matrix = matrixStack.current()
        matrix.identity()
        val viewMatrix = matrix.modelView()
        viewMatrix.rotateAccurate((-cam.tilt).toDouble(), 0.0f, 0.0f, 1.0f)
        viewMatrix.rotateAccurate((-cam.pitch - 90.0f).toDouble(), 1.0f, 0.0f,
                0.0f)
        viewMatrix.rotateAccurate((-cam.yaw + 90.0f).toDouble(), 0.0f, 0.0f,
                1.0f)
        enableCulling()
        enableDepthTest()
        setBlending(BlendingMode.NORMAL)
    }

    fun setProjectionOrthogonal(x: Float,
                                y: Float,
                                width: Float,
                                height: Float) {
        projectionMatrix.identity()
        projectionMatrix.orthogonal(x, x + width, y + height, y, -1024.0f,
                1024.0f)
        val matrix = matrixStack.current()
        matrix.identity()
        disableCulling()
        disableDepthTest()
        setBlending(BlendingMode.NORMAL)
    }

    fun sceneWidth(): Int {
        return (contentWidth * resolutionMultiplier).toInt()
    }

    fun sceneHeight(): Int {
        return (contentHeight * resolutionMultiplier).toInt()
    }

    fun sceneSpace(): Double {
        return max(sceneWidth(), sceneHeight()) / 1920.0
    }

    fun contentWidth(): Int {
        return contentWidth
    }

    fun contentHeight(): Int {
        return contentHeight
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
                                   alpha: Boolean): Framebuffer

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

    abstract fun viewport(x: Int,
                          y: Int,
                          width: Int,
                          height: Int)

    abstract fun screenShot(x: Int,
                            y: Int,
                            width: Int,
                            height: Int): Image

    abstract fun screenShotFBO(fbo: Framebuffer): Image

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
