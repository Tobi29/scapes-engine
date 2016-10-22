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

import org.tobi29.scapes.engine.ScapesEngine

abstract class Scene(val engine: ScapesEngine) {
    abstract fun init(gl: GL)

    abstract fun renderScene(gl: GL)

    open fun postRender(gl: GL,
                        delta: Double) {
    }

    open fun postProcessing(gl: GL,
                            pass: Int): Shader? {
        return null
    }

    open fun width(width: Int): Int {
        return width
    }

    open fun height(height: Int): Int {
        return height
    }

    open fun renderPasses(): Int {
        return 1
    }

    open fun colorAttachments(): Int {
        return 1
    }

    fun initFBO(i: Int,
                fbo: Framebuffer) {
    }

    open fun dispose(gl: GL) {
    }

    abstract fun dispose()
}
