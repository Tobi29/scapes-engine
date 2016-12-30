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

import java.util.*

class ShaderCompileInformation {
    private val preCompileListeners = ArrayList<(ShaderPreprocessor, GL) -> Unit>()
    private val postCompileListeners = ArrayList<(GL, Shader) -> Unit>()

    fun supplyPreCompile(listener: ShaderPreprocessor.(GL) -> Unit) {
        preCompileListeners.add(listener)
    }

    fun supplyPostCompile(listener: (Shader) -> Unit) {
        supplyPostCompile({ gl, shader -> listener(shader) })
    }

    fun supplyPostCompile(listener: (GL, Shader) -> Unit) {
        postCompileListeners.add(listener)
    }

    fun preCompile(gl: GL): ShaderPreprocessor {
        val processor = ShaderPreprocessor()
        processor.supplyProperty("SCENE_WIDTH", gl.sceneWidth())
        processor.supplyProperty("SCENE_HEIGHT", gl.sceneHeight())
        processor.supplyProperty("CONTAINER_WIDTH", gl.containerWidth())
        processor.supplyProperty("CONTAINER_HEIGHT", gl.containerHeight())
        processor.supplyProperty("CONTENT_WIDTH", gl.contentWidth())
        processor.supplyProperty("CONTENT_HEIGHT", gl.contentHeight())
        preCompileListeners.forEach { it(processor, gl) }
        return processor
    }

    fun postCompile(gl: GL,
                    shader: Shader) {
        postCompileListeners.forEach { it(gl, shader) }
    }
}
