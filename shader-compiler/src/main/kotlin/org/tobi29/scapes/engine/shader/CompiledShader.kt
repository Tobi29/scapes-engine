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

package org.tobi29.scapes.engine.shader

import org.tobi29.io.tag.*
import org.tobi29.stdex.readOnly

class CompiledShader(
    val declarations: List<DeclarationStatement>,
    val functions: List<CallFunction>,
    val shaderVertex: ShaderFunction?,
    val shaderFragment: ShaderFunction?,
    val outputs: ShaderSignature?,
    private val uniforms: Array<Uniform?>,
    val properties: List<Property>
) : TagMapWrite {
    val functionMap =
        HashMap<FunctionParameterSignature, FunctionExportedSignature>()
            .also { functionMap ->
                functions.asSequence().map {
                    Pair(
                        it.signature.exported.call,
                        it.signature.exported
                    )
                }.toMap(functionMap)
                STDLib.functions.keys.forEach {
                    functionMap[it.call] = it
                }
            }.readOnly()

    fun uniforms(): Array<Uniform?> {
        return uniforms.copyOf()
    }

    override fun write(map: ReadWriteTagMap) {
        map["Declarations"] =
                declarations.asSequence().map { it.toTag() }.toTag()
        map["Functions"] = functions.asSequence().map { it.toTag() }.toTag()
        shaderVertex?.let { map["ShaderVertex"] = it.toTag() }
        shaderFragment?.let { map["ShaderFragment"] = it.toTag() }
        outputs?.let { map["Outputs"] = it.toTag() }
        map["Uniforms"] = TagMap {
            uniforms.withIndex().asSequence()
                .forEach { (i, uniform) ->
                    if (uniform == null) return@forEach
                    put(i.toString(), uniform.toTag())
                }
        }
        map["Properties"] = properties.asSequence().map { it.toTag() }.toTag()
    }
}

fun MutableTag.toCompiledShader(): CompiledShader? {
    val map = toMap() ?: return null
    val declarations = map["Declarations"]?.toList()?.map {
        it.toDeclarationStatement() ?: return null
    } ?: return null
    val functions = map["Functions"]?.toList()?.map {
        it.toCallFunction() ?: return null
    } ?: return null
    val shaderVertex = map["ShaderVertex"]?.toShaderFunction()
    val shaderFragment = map["ShaderFragment"]?.toShaderFunction()
    val outputs = map["Outputs"]?.toShaderSignature()
    val uniforms = map["Uniforms"]?.toMap()?.map { (id, uniform) ->
        (id.toIntOrNull() ?: return null) to (uniform.toUniform()
                ?: return null)
    }?.let { uniforms ->
        val array = arrayOfNulls<Uniform>(
            uniforms.maxBy { it.first }?.first?.let { it + 1 } ?: 0)
        uniforms.forEach { array[it.first] = it.second }
        array
    } ?: return null
    val properties = map["Properties"]?.toList()?.map {
        it.toProperty() ?: return null
    } ?: return null
    return CompiledShader(
        declarations, functions, shaderVertex, shaderFragment,
        outputs, uniforms, properties
    )
}

class ShaderContext(
    functions: Map<FunctionParameterSignature, FunctionExportedSignature>,
    functionSimplifications: Map<FunctionExportedSignature, (List<Expression>) -> Expression>,
    properties: Map<String, Expression>
) {
    val functions = functions.readOnly()
    val functionSimplifications = functionSimplifications.readOnly()
    val properties = properties.readOnly()
}
