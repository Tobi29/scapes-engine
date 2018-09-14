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

interface Shader : GraphicsObject {
    fun activate(gl: GL)

    fun updateUniforms(gl: GL)

    fun setUniform1f(gl: GL,
                     uniform: Int,
                     v0: Float)

    fun setUniform2f(gl: GL,
                     uniform: Int,
                     v0: Float,
                     v1: Float)

    fun setUniform3f(gl: GL,
                     uniform: Int,
                     v0: Float,
                     v1: Float,
                     v2: Float)

    fun setUniform4f(gl: GL,
                     uniform: Int,
                     v0: Float,
                     v1: Float,
                     v2: Float,
                     v3: Float)

    fun setUniform1i(gl: GL,
                     uniform: Int,
                     v0: Int)

    fun setUniform2i(gl: GL,
                     uniform: Int,
                     v0: Int,
                     v1: Int)

    fun setUniform3i(gl: GL,
                     uniform: Int,
                     v0: Int,
                     v1: Int,
                     v2: Int)

    fun setUniform4i(gl: GL,
                     uniform: Int,
                     v0: Int,
                     v1: Int,
                     v2: Int,
                     v3: Int)

    fun setUniform1(gl: GL,
                    uniform: Int,
                    values: FloatArray)

    fun setUniform2(gl: GL,
                    uniform: Int,
                    values: FloatArray)

    fun setUniform3(gl: GL,
                    uniform: Int,
                    values: FloatArray)

    fun setUniform4(gl: GL,
                    uniform: Int,
                    values: FloatArray)

    fun setUniform1(gl: GL,
                    uniform: Int,
                    values: IntArray)

    fun setUniform2(gl: GL,
                    uniform: Int,
                    values: IntArray)

    fun setUniform3(gl: GL,
                    uniform: Int,
                    values: IntArray)

    fun setUniform4(gl: GL,
                    uniform: Int,
                    values: IntArray)

    fun setUniformMatrix2(gl: GL,
                          uniform: Int,
                          transpose: Boolean,
                          matrices: FloatArray)

    fun setUniformMatrix3(gl: GL,
                          uniform: Int,
                          transpose: Boolean,
                          matrices: FloatArray)

    fun setUniformMatrix4(gl: GL,
                          uniform: Int,
                          transpose: Boolean,
                          matrices: FloatArray)
}
