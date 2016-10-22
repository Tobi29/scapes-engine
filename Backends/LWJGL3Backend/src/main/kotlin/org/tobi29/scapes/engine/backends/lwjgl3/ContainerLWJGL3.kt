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
package org.tobi29.scapes.engine.backends.lwjgl3

import mu.KLogging
import org.lwjgl.Version
import org.lwjgl.opengl.GL11
import org.lwjgl.opengles.GLES
import org.lwjgl.opengles.GLES20
import org.lwjgl.system.Platform
import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.backends.lwjgl3.openal.LWJGL3OpenAL
import org.tobi29.scapes.engine.backends.lwjgl3.opengl.GLLWJGL3GL
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem
import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.input.ControllerDefault
import org.tobi29.scapes.engine.input.ControllerKey
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.utils.task.Joiner
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

abstract class ContainerLWJGL3(protected val engine: ScapesEngine, protected val useGLES: Boolean = false) : ControllerDefault(), Container {
    protected val tasks = ConcurrentLinkedQueue<() -> Unit>()
    protected val mainThread: Thread
    protected val gl: GL
    protected val soundSystem: SoundSystem
    protected val superModifier: Boolean
    protected val joysticksChanged = AtomicBoolean(false)
    protected var focus = true
    protected var valid = false
    protected var visible = false
    protected var containerResized = true
    protected var containerWidth = 0
    protected var containerHeight = 0
    protected var contentWidth = 0
    protected var contentHeight = 0
    protected var mouseX = 0.0
    protected var mouseY = 0.0

    init {
        mainThread = Thread.currentThread()
        logger.info { "LWJGL version: ${Version.getVersion()}" }
        if (useGLES) {
            throw UnsupportedOperationException(
                    "OpenGLES Support not implemented")
            //gl = new GLLWJGL3GLES(engine, this);
        } else {
            gl = GLLWJGL3GL(engine, this)
        }
        soundSystem = OpenALSoundSystem(engine, LWJGL3OpenAL(), 64, 5.0)
        superModifier = Platform.get() === Platform.MACOSX
    }

    fun checkContext(): String? {
        if (useGLES) {
            return checkContextGLES()
        } else {
            return checkContextGL()
        }
    }

    override fun containerWidth(): Int {
        return containerWidth
    }

    override fun containerHeight(): Int {
        return containerHeight
    }

    override fun contentWidth(): Int {
        return contentWidth
    }

    override fun contentHeight(): Int {
        return contentHeight
    }

    override fun contentResized(): Boolean {
        return containerResized
    }

    override fun updateContainer() {
        valid = false
    }

    override fun gl(): GL {
        return gl
    }

    override fun sound(): SoundSystem {
        return soundSystem
    }

    override fun controller(): ControllerDefault? {
        return this
    }

    override fun joysticksChanged(): Boolean {
        return joysticksChanged.get()
    }

    override fun loadFont(asset: String): Font? {
        return STBFont.fromFont(this, engine.files[asset + ".ttf"])
    }

    override fun allocate(capacity: Int): ByteBuffer {
        // TODO: Do more testing if the direct buffer leak is actually gone
        return ByteBuffer.allocateDirect(capacity).order(
                ByteOrder.nativeOrder())
        // Late 2015 OpenJDK 8 (did not test this on other JVMs) deleted direct
        // buffers would not get freed properly causing massive leaks pushing
        // up memory usage to 5+ GB, backend currently can transparently take
        // heap buffers for LWJGL calls (by copying into a shared direct one)
        // return ByteBuffer.allocate(capacity).order(ByteOrder.nativeOrder());
    }

    override val isModifierDown: Boolean
        get() {
            if (superModifier) {
                return isDown(ControllerKey.KEY_LEFT_SUPER) || isDown(
                        ControllerKey.KEY_RIGHT_SUPER)
            } else {
                return isDown(ControllerKey.KEY_LEFT_CONTROL) || isDown(
                        ControllerKey.KEY_RIGHT_CONTROL)
            }
        }

    protected fun <R> exec(runnable: () -> R): R {
        val thread = Thread.currentThread()
        if (thread === mainThread) {
            return runnable()
        }
        val joinable = Joiner.BasicJoinable()
        var output: R? = null
        var exception: Throwable? = null
        tasks.add({
            try {
                output = runnable()
            } catch (e: Throwable) {
                exception = e
            }

            joinable.join()
        })
        joinable.joiner.join()
        if (exception != null) {
            throw IOException(exception)
        }
        return output ?: throw IllegalStateException("Output not passed")
    }

    companion object : KLogging() {
        fun checkContextGL(): String? {
            logger.info {
                "OpenGL: ${GL11.glGetString(
                        GL11.GL_VERSION)} (Vendor: ${GL11.glGetString(
                        GL11.GL_VENDOR)}, Renderer: ${GL11.glGetString(
                        GL11.GL_RENDERER)})"
            }
            val capabilities = org.lwjgl.opengl.GL.getCapabilities()
            if (!capabilities.OpenGL11) {
                return "Your graphics card has no OpenGL 1.1 support!"
            }
            if (!capabilities.OpenGL12) {
                return "Your graphics card has no OpenGL 1.2 support!"
            }
            if (!capabilities.OpenGL13) {
                return "Your graphics card has no OpenGL 1.3 support!"
            }
            if (!capabilities.OpenGL14) {
                return "Your graphics card has no OpenGL 1.4 support!"
            }
            if (!capabilities.OpenGL15) {
                return "Your graphics card has no OpenGL 1.5 support!"
            }
            if (!capabilities.OpenGL20) {
                return "Your graphics card has no OpenGL 2.0 support!"
            }
            if (!capabilities.OpenGL21) {
                return "Your graphics card has no OpenGL 2.1 support!"
            }
            if (!capabilities.OpenGL30) {
                return "Your graphics card has no OpenGL 3.0 support!"
            }
            if (!capabilities.OpenGL31) {
                return "Your graphics card has no OpenGL 3.1 support!"
            }
            if (!capabilities.OpenGL32) {
                return "Your graphics card has no OpenGL 3.2 support!"
            }
            if (!capabilities.OpenGL33) {
                return "Your graphics card has no OpenGL 3.3 support!"
            }
            return null
        }

        fun checkContextGLES(): String? {
            logger.info {
                "OpenGL ES: ${GLES20.glGetString(
                        GLES20.GL_VERSION)} (Vendor: ${GLES20.glGetString(
                        GLES20.GL_VENDOR)}, Renderer: ${GLES20.glGetString(
                        GLES20.GL_RENDERER)})"
            }
            val capabilities = GLES.getCapabilities()
            if (!capabilities.GLES20) {
                return "Your graphics card has no OpenGL ES 2.0 support!"
            }
            if (!capabilities.GLES30) {
                return "Your graphics card has no OpenGL ES 3.0 support!"
            }
            return null
        }
    }
}
