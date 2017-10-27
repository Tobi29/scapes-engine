package org.tobi29.scapes.engine.utils.shader.frontend.clike

import org.tobi29.scapes.engine.utils.profiler.profilerSection
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import org.tobi29.scapes.engine.utils.shader.Scope
import org.tobi29.scapes.engine.utils.shader.ShaderCompileException
import org.tobi29.scapes.engine.utils.shader.Types

actual object CLikeShader {
    actual fun compile(source: String): CompiledShader {
        val parser = profilerSection("Parse source") {
            parser(source)
        }
        val scope = Scope()
        scope.add("out_Position", Types.Vector4.exported)
        scope.add("varying_Fragment", Types.Vector4.exported)
        val program = profilerSection("Parse program") {
            externalDeclaration(parser.compilationUnit().translationUnit(),
                    scope)
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
                program.uniforms.toTypedArray(), program.properties)
    }
}
