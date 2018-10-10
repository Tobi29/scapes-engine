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

package org.tobi29.scapes.engine.backends.opengles

import org.lwjgl.opengles.GLES
import org.lwjgl.opengles.GLES20
import org.tobi29.logging.KLogger
import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.GLESBackend
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.GraphicsCheckException
import org.tobi29.scapes.engine.graphics.GraphicsObjectSupplier

object GLESBackendLWJGL : GLESBackend {
    private val logger = KLogger<GLESBackendLWJGL>()

    override fun createGL(container: Container): Pair<GraphicsObjectSupplier, GL> =
        GLESHandle(container).let { it to GLESImpl(it) }

    override fun initContext() {
        org.lwjgl.opengles.GLES.createCapabilities()
        checkContextGLES()?.let { throw GraphicsCheckException(it) }
    }

    private fun checkContextGLES(): String? {
        logger.info {
            "OpenGL ES: ${GLES20.glGetString(
                GLES20.GL_VERSION
            )} (Vendor: ${GLES20.glGetString(
                GLES20.GL_VENDOR
            )}, Renderer: ${GLES20.glGetString(
                GLES20.GL_RENDERER
            )})"
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
