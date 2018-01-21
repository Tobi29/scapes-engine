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

package org.tobi29.scapes.engine.graphics

import org.tobi29.scapes.engine.shader.*

val SHADER_TEXTURED: suspend () -> CompiledShader = { SHADER_TEXTURED_LAZY }
private val SHADER_TEXTURED_LAZY by lazy {
    ShaderProgramScope().apply {
        uniform(Type(Types.Matrix4), 1, "uniform_ModelViewProjectionMatrix")
        uniform(Type(Types.Texture2), 3, "uniform_Texture")

        shaderFunction("vertex", {
            add(Type(Types.Vector4), 0, "attribute_Position")
            add(Type(Types.Vector4), 1, "attribute_Color")
            add(Type(Types.Vector2), 2, "attribute_Texture")
        }) {
            add(identifier("varying_Texture") assign
                    identifier("attribute_Texture"))
            add(identifier("varying_Color") assign
                    identifier("attribute_Color"))
            add(identifier("out_Position") assign
                    (identifier("uniform_ModelViewProjectionMatrix")
                            * identifier("attribute_Position")))
        }

        shaderFunction("fragment", {
            add(Type(Types.Vector4), "varying_Color")
            add(Type(Types.Vector2), "varying_Texture")
        }) {
            add(declaration(Type(Types.Vector4), "color",
                    function("texture", identifier("uniform_Texture"),
                            identifier("varying_Texture"))))
            add(identifier("out_Color").member("a") assign
                    (identifier("color").member("a")
                            * identifier("varying_Color").member("a")))
            add(IfStatement(identifier("out_Color").member("a")
                    lessThan DecimalExpression(0.01), compound {
                add(function("discard"))
            }))
            add(identifier("out_Color").member("rgb") assign
                    (identifier("color").member("rgb")
                            * identifier("varying_Color").member("rgb")))
        }

        outputs {
            add(Type(Types.Vector4), 0, "out_Color")
        }
    }.finish()
}
