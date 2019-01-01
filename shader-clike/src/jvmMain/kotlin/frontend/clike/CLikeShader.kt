/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.scapes.engine.shader.frontend.clike

import org.tobi29.profiler.profilerSection
import org.tobi29.scapes.engine.shader.*

actual object CLikeShader {
    actual fun compile(source: String): CompiledShader {
        val parser = profilerSection("Parse source") {
            parser(source)
        }
        val scope = Scope()
        scope.add("out_Position", Type(Types.Vector4))
        scope.add("varying_Fragment", Type(Types.Vector4))
        val program = profilerSection("Parse program") {
            externalDeclaration(
                parser.compilationUnit().translationUnit(), scope
            )
        }
        val shaderFragment = program.shaders["fragment"]?.let { shader ->
            Pair(shader.first, shader.second(scope))
        }
        val shaderVertex = program.shaders["vertex"]?.let { shader ->
            if (shaderFragment == null) {
                throw ShaderCompileException(
                    "Vertex shader requires fragment shader!"
                )
            }
            Pair(shader.first, shader.second(shaderFragment.first))
        }
        return CompiledShader(
            program.declarations, program.functions,
            shaderVertex?.second, shaderFragment?.second, program.outputs,
            program.uniforms.toTypedArray(), program.properties
        )
    }
}
