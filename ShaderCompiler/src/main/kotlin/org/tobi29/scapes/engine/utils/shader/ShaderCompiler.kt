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

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.tobi29.scapes.engine.utils.shader.frontend.clike.CLikeParser

object ShaderCompiler {
    fun compile(source: String): CompiledShader {
        val scope = Scope()
        scope.add("out_Position")
        scope.add("varying_Fragment")
        val program = try {
            val parser = CLikeParser.parser(source)
            CLikeParser.externalDeclaration(
                    parser.compilationUnit().translationUnit(), scope)
        } catch (e: ParseCancellationException) {
            throw ShaderCompileException(e)
        }
        val shaderFragment = program.shaders["fragment"]?.let { shader ->
            Pair(shader.first, shader.second(scope))
        }
        val shaderVertex = program.shaders["vertex"]?.let { shader ->
            if (shaderFragment == null) {
                throw ShaderCompileException(
                        "Vertex shader requires fragment shader!")
            }
            Pair(shader.first, shader.second(shaderFragment.first))
        }
        return CompiledShader(program.declarations, program.functions,
                shaderVertex?.second, shaderFragment?.second, program.outputs,
                scope, program.uniforms.toTypedArray())
    }
}
