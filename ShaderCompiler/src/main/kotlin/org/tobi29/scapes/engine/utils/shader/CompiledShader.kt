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

package org.tobi29.scapes.engine.utils.shader

import org.tobi29.scapes.engine.utils.readOnly


class CompiledShader(declarations: List<Statement>,
                     functions: List<CallFunction>,
                     val shaderVertex: ShaderFunction?,
                     val shaderFragment: ShaderFunction?,
                     val outputs: ShaderSignature?,
                     val scope: Scope,
                     private val uniforms: Array<Uniform?>,
                     properties: List<Property>) {
    val declarations = declarations.readOnly()
    val functions = functions.readOnly()
    val functionMap =
            HashMap<FunctionParameterSignature, FunctionExportedSignature>()
                    .also { functionMap ->
                        functions.asSequence().map {
                            Pair(it.signature.exported.call,
                                    it.signature.exported)
                        }.toMap(functionMap)
                        STDLib.functions.keys.forEach {
                            functionMap[it.call] = it
                        }
                    }.readOnly()
    val properties = properties.readOnly()

    fun uniforms(): Array<Uniform?> {
        return uniforms.copyOf()
    }
}

class ShaderContext(functions: Map<FunctionParameterSignature, FunctionExportedSignature>,
                    functionSimplifications: Map<FunctionExportedSignature, (List<Expression>) -> Expression>,
                    properties: Map<String, Expression>) {
    val functions = functions.readOnly()
    val functionSimplifications = functionSimplifications.readOnly()
    val properties = properties.readOnly()
}
