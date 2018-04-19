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

import org.tobi29.io.ByteViewRO

interface Model : GraphicsObject {
    fun markAsDisposed()

    fun render(gl: GL,
               shader: Shader): Boolean

    fun render(gl: GL,
               shader: Shader,
               length: Int): Boolean

    fun renderInstanced(gl: GL,
                        shader: Shader,
                        count: Int): Boolean

    fun renderInstanced(gl: GL,
                        shader: Shader,
                        length: Int,
                        count: Int): Boolean

    fun buffer(gl: GL,
               buffer: ByteViewRO)

    val stride: Int

    var weak: Boolean
}

interface ModelIndexed : Model {
    fun bufferIndices(gl: GL,
                      buffer: ByteViewRO)
}
