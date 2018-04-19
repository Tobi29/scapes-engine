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

package org.tobi29.scapes.engine.graphics

import org.tobi29.graphics.Bitmap
import org.tobi29.graphics.Cam
import org.tobi29.graphics.Image
import org.tobi29.graphics.toImage
import org.tobi29.io.ByteViewRO
import org.tobi29.io.view
import org.tobi29.math.matrix.Matrix4f
import org.tobi29.stdex.JsName
import org.tobi29.stdex.assert
import kotlin.math.max
import kotlin.math.roundToLong

abstract class GL(
    private val gos: GraphicsObjectSupplier
) : GraphicsObjectSupplier by gos {
    val matrixStack = MatrixStack(64)
    var contentWidth = 1
        protected set
    var contentHeight = 1
        protected set
    var containerWidth = 1
        protected set
    var containerHeight = 1
        protected set
    var timer = 0.0
        private set
    var timestamp = 0L
        private set
    val textureEmpty by lazy {
        createTexture(1, 1, byteArrayOf(-1, -1, -1, -1).view, 0)
    }

    fun reshape(
        contentWidth: Int,
        contentHeight: Int,
        containerWidth: Int,
        containerHeight: Int
    ) {
        this.contentWidth = contentWidth
        this.contentHeight = contentHeight
        this.containerWidth = containerWidth
        this.containerHeight = containerHeight
        shaderTracker.disposeAll(this)
    }

    fun step(delta: Double) {
        timer += delta
        // This is over 4 days, should be fine
        if (timer >= 360000.0) {
            timer -= 360000.0
        }
        timestamp += max((1000000000.0 * delta).roundToLong(), 1L)
    }

    fun isRenderCall() = container.isRenderCall()

    fun check() {
        assert { isRenderCall() }
    }

    abstract fun checkError(message: String)

    abstract fun clear(r: Float, g: Float, b: Float, a: Float)

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

    abstract fun enableScissor(x: Int, y: Int, width: Int, height: Int)

    abstract fun setBlending(mode: BlendingMode)

    abstract fun setViewport(x: Int, y: Int, width: Int, height: Int)

    abstract fun getViewport(output: IntArray)

    abstract fun getFrontBuffer(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): Bitmap<*, *>

    abstract fun getFBOColorBuffer(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        attachment: Int = 0
    ): Bitmap<*, *>

    abstract fun getFBODepthBuffer(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): Bitmap<*, *>

    abstract fun setAttribute1f(
        id: Int, v0: Float
    )

    abstract fun setAttribute2f(
        id: Int, v0: Float, v1: Float
    )

    abstract fun setAttribute3f(
        id: Int, v0: Float, v1: Float, v2: Float
    )

    abstract fun setAttribute4f(
        id: Int, v0: Float, v1: Float, v2: Float, v3: Float
    )

    abstract fun setAttribute1f(
        uniform: Int, values: FloatArray
    )

    abstract fun setAttribute2f(
        uniform: Int, values: FloatArray
    )

    abstract fun setAttribute3f(
        uniform: Int, values: FloatArray
    )

    abstract fun setAttribute4f(
        uniform: Int, values: FloatArray
    )

    abstract fun replaceTexture(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        buffer: ByteViewRO
    )

    abstract fun replaceTextureMipMap(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        vararg buffers: ByteViewRO
    )

    abstract fun activeTexture(i: Int)

    companion object {
        const val VERTEX_ATTRIBUTE = 0
        const val COLOR_ATTRIBUTE = 1
        const val TEXTURE_ATTRIBUTE = 2
        const val NORMAL_ATTRIBUTE = 3
    }

    // TODO: Remove after 0.0.13

    @Deprecated("Use getFrontBuffer")
    fun screenShot(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): Image = getFrontBuffer(x, y, width, height).toImage()

    @Deprecated("Use getFBOColorBuffer")
    fun screenShotFBOColor(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        attachment: Int = 0
    ): Image = getFBOColorBuffer(x, y, width, height, attachment).toImage()

    @Deprecated("Use getFBODepthBuffer")
    fun screenShotFBODepth(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): Image = getFBODepthBuffer(x, y, width, height).toImage()

    @Deprecated("Use property", ReplaceWith("textureEmpty"))
    @JsName("textureEmptyFun")
    fun textureEmpty(): Texture = textureEmpty

    @Deprecated(
        "Use renderInto",
        ReplaceWith(
            "renderInto(this, framebuffer, block)",
            "org.tobi29.scapes.engine.graphics.renderInto"
        )
    )
    fun into(
        framebuffer: Framebuffer,
        block: (Double) -> Unit
    ): (Double) -> Unit = renderInto(this, framebuffer, block)

    @Deprecated(
        "Use extension property",
        ReplaceWith(
            "aspectRatio",
            "org.tobi29.scapes.engine.graphics.aspectRatio"
        )
    )
    fun aspectRatio(): Double = aspectRatio

    @Deprecated(
        "Use extension property",
        ReplaceWith("aspectRatio", "org.tobi29.scapes.engine.graphics.space")
    )
    fun space(): Double = space

    @Deprecated(
        "Use extension property",
        ReplaceWith(
            "aspectRatio",
            "org.tobi29.scapes.engine.graphics.contentSpace"
        )
    )
    fun contentSpace(): Double = contentSpace
}

inline val GL.aspectRatio: Double
    get() = containerWidth.toDouble() / containerHeight

inline val GL.space: Double
    get() = max(containerWidth, containerHeight) / 1920.0

inline val GL.contentSpace: Double
    get() = max(contentWidth, contentHeight) / 1920.0

fun Matrix4f.camera(cam: Cam) {
    rotateAccurate((-cam.tilt).toDouble(), 0.0f, 0.0f, 1.0f)
    rotateAccurate((-cam.pitch - 90.0f).toDouble(), 1.0f, 0.0f, 0.0f)
    rotateAccurate((-cam.yaw + 90.0f).toDouble(), 0.0f, 0.0f, 1.0f)
}
