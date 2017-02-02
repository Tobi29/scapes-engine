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
package org.tobi29.scapes.engine.backends.lwjgl3.opengles

import mu.KLogging
import org.lwjgl.opengles.GLES20
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.Shader
import org.tobi29.scapes.engine.graphics.ShaderCompileInformation
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*

internal class ShaderGL(private val shader: CompiledShader,
                        private val information: ShaderCompileInformation) : Shader {
    private val uniforms = ArrayDeque<() -> Unit>()
    override var isStored = false
    private var valid = false
    private var markAsDisposed = false
    private var uniformLocations: IntArray? = null
    private var program = 0
    private var used: Long = 0
    private var detach: Function0<Unit>? = null

    override fun ensureStored(gl: GL): Boolean {
        if (!isStored) {
            store(gl)
        }
        used = System.currentTimeMillis()
        return valid
    }

    override fun ensureDisposed(gl: GL) {
        if (isStored) {
            dispose(gl)
            reset()
        }
    }

    override fun isUsed(time: Long): Boolean {
        return time - used < 1000 && !markAsDisposed
    }

    override fun dispose(gl: GL) {
        gl.check()
        GLES20.glDeleteProgram(program)
    }

    override fun reset() {
        assert(isStored)
        isStored = false
        detach?.invoke()
        detach = null
        valid = false
        markAsDisposed = false
    }

    override fun activate(gl: GL) {
        if (!ensureStored(gl)) {
            return
        }
        gl.check()
        GLES20.glUseProgram(program)
    }

    override fun updateUniforms(gl: GL) {
        gl.check()
        while (!uniforms.isEmpty()) {
            uniforms.poll()()
        }
    }

    override fun uniformLocation(uniform: Int): Int {
        uniformLocations?.let { return it[uniform] }
        throw IllegalStateException("Shader not stored")
    }

    override fun setUniform1f(uniform: Int,
                              v0: Float) {
        uniforms.add { GLES20.glUniform1f(uniformLocation(uniform), v0) }
    }

    override fun setUniform2f(uniform: Int,
                              v0: Float,
                              v1: Float) {
        uniforms.add { GLES20.glUniform2f(uniformLocation(uniform), v0, v1) }
    }

    override fun setUniform3f(uniform: Int,
                              v0: Float,
                              v1: Float,
                              v2: Float) {
        uniforms.add { GLES20.glUniform3f(uniformLocation(uniform), v0, v1, v2) }
    }

    override fun setUniform4f(uniform: Int,
                              v0: Float,
                              v1: Float,
                              v2: Float,
                              v3: Float) {
        uniforms.add {
            GLES20.glUniform4f(uniformLocation(uniform), v0, v1, v2, v3)
        }
    }

    override fun setUniform1i(uniform: Int,
                              v0: Int) {
        uniforms.add { GLES20.glUniform1i(uniformLocation(uniform), v0) }
    }

    override fun setUniform2i(uniform: Int,
                              v0: Int,
                              v1: Int) {
        uniforms.add { GLES20.glUniform2i(uniformLocation(uniform), v0, v1) }
    }

    override fun setUniform3i(uniform: Int,
                              v0: Int,
                              v1: Int,
                              v2: Int) {
        uniforms.add { GLES20.glUniform3i(uniformLocation(uniform), v0, v1, v2) }
    }

    override fun setUniform4i(uniform: Int,
                              v0: Int,
                              v1: Int,
                              v2: Int,
                              v3: Int) {
        uniforms.add {
            GLES20.glUniform4i(uniformLocation(uniform), v0, v1, v2, v3)
        }
    }

    override fun setUniform1(uniform: Int,
                             values: FloatBuffer) {
        uniforms.add { GLES20.glUniform1fv(uniformLocation(uniform), values) }
    }

    override fun setUniform2(uniform: Int,
                             values: FloatBuffer) {
        uniforms.add { GLES20.glUniform2fv(uniformLocation(uniform), values) }
    }

    override fun setUniform3(uniform: Int,
                             values: FloatBuffer) {
        uniforms.add { GLES20.glUniform3fv(uniformLocation(uniform), values) }
    }

    override fun setUniform4(uniform: Int,
                             values: FloatBuffer) {
        uniforms.add { GLES20.glUniform4fv(uniformLocation(uniform), values) }
    }

    override fun setUniform1(uniform: Int,
                             values: IntBuffer) {
        uniforms.add { GLES20.glUniform1iv(uniformLocation(uniform), values) }
    }

    override fun setUniform2(uniform: Int,
                             values: IntBuffer) {
        uniforms.add { GLES20.glUniform2iv(uniformLocation(uniform), values) }
    }

    override fun setUniform3(uniform: Int,
                             values: IntBuffer) {
        uniforms.add { GLES20.glUniform3iv(uniformLocation(uniform), values) }
    }

    override fun setUniform4(uniform: Int,
                             values: IntBuffer) {
        uniforms.add { GLES20.glUniform4iv(uniformLocation(uniform), values) }
    }

    override fun setUniformMatrix2(uniform: Int,
                                   transpose: Boolean,
                                   matrices: FloatArray) {
        uniforms.add {
            GLES20.glUniformMatrix2fv(uniformLocation(uniform), transpose,
                    matrices)
        }
    }

    override fun setUniformMatrix3(uniform: Int,
                                   transpose: Boolean,
                                   matrices: FloatArray) {
        uniforms.add {
            GLES20.glUniformMatrix3fv(uniformLocation(uniform), transpose,
                    matrices)
        }
    }

    override fun setUniformMatrix4(uniform: Int,
                                   transpose: Boolean,
                                   matrices: FloatArray) {
        uniforms.add {
            GLES20.glUniformMatrix4fv(uniformLocation(uniform), transpose,
                    matrices)
        }
    }

    private fun store(gl: GL) {
        assert(!isStored)
        isStored = true
        gl.check()
        val processor = information.preCompile(gl)
        try {
            val program = GLUtils.createProgram(shader, processor.properties())
            this.program = program.first
            uniformLocations = program.second
        } catch (e: IOException) {
            logger.error(e) { "Failed to generate shader" }
        }

        information.postCompile(gl, this)
        valid = true
        detach = gl.shaderTracker().attach(this)
    }

    companion object : KLogging()
}
