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

import java.nio.FloatBuffer
import java.nio.IntBuffer

interface Shader : GraphicsObject {
    fun activate(gl: GL)

    fun updateUniforms(gl: GL)

    fun uniformLocation(uniform: Int): Int

    fun setUniform1f(uniform: Int,
                     v0: Float)

    fun setUniform2f(uniform: Int,
                     v0: Float,
                     v1: Float)

    fun setUniform3f(uniform: Int,
                     v0: Float,
                     v1: Float,
                     v2: Float)

    fun setUniform4f(uniform: Int,
                     v0: Float,
                     v1: Float,
                     v2: Float,
                     v3: Float)

    fun setUniform1i(uniform: Int,
                     v0: Int)

    fun setUniform2i(uniform: Int,
                     v0: Int,
                     v1: Int)

    fun setUniform3i(uniform: Int,
                     v0: Int,
                     v1: Int,
                     v2: Int)

    fun setUniform4i(uniform: Int,
                     v0: Int,
                     v1: Int,
                     v2: Int,
                     v3: Int)

    fun setUniform1(uniform: Int,
                    values: FloatBuffer)

    fun setUniform2(uniform: Int,
                    values: FloatBuffer)

    fun setUniform3(uniform: Int,
                    values: FloatBuffer)

    fun setUniform4(uniform: Int,
                    values: FloatBuffer)

    fun setUniform1(uniform: Int,
                    values: IntBuffer)

    fun setUniform2(uniform: Int,
                    values: IntBuffer)

    fun setUniform3(uniform: Int,
                    values: IntBuffer)

    fun setUniform4(uniform: Int,
                    values: IntBuffer)

    fun setUniformMatrix2(uniform: Int,
                          transpose: Boolean,
                          matrices: FloatArray)

    fun setUniformMatrix3(uniform: Int,
                          transpose: Boolean,
                          matrices: FloatArray)

    fun setUniformMatrix4(uniform: Int,
                          transpose: Boolean,
                          matrices: FloatArray)
}
