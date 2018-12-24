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

package org.tobi29.scapes.engine.backends.js

import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.ScapesEngineBackend
import org.tobi29.scapes.engine.backends.opengles.GLESImpl
import org.tobi29.scapes.engine.graphics.GL
import net.gitout.ktbindings.gles.webgl.WebGL2RenderingContext as WGL2
import org.khronos.webgl.WebGLRenderingContext as WGL1

abstract class ContainerWebGL2(
    wgl: WGL2
) : Container, ScapesEngineBackend by ScapesEngineJS {
    final override val gos =
        GLESImpl({ wgl }, {}, ::isRenderCall).apply { init() }
    protected var isRendering = 0

    protected inline fun <R> renderCall(block: (GL) -> R): R {
        isRendering++
        return try {
            block(gos)
        } finally {
            isRendering--
        }
    }

    override fun isRenderCall() = isRendering > 0
}
