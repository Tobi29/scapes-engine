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

package org.tobi29.scapes.engine.shader.frontend.clike

import org.tobi29.scapes.engine.shader.*

internal fun ScapesShaderParser.DeclaratorContext.ast(scope: Scope): Type {
    declaratorField()?.let { return it.ast() }
    declaratorArray()?.let { return it.ast(scope) }
    throw IllegalStateException("Invalid parse tree node")
}

internal fun ScapesShaderParser.DeclaratorFieldContext.ast(): Type {
    var constant = false
    children.forEach { child ->
        when (child.text) {
            "const" -> constant = true
        }
    }
    val precision = precisionSpecifier()?.ast() ?: Precision.mediump
    return Type(typeSpecifier().ast(), constant, precision)
}

internal fun ScapesShaderParser.DeclaratorArrayContext.ast(scope: Scope): Type {
    var constant = false
    children.forEach { child ->
        when (child.text) {
            "const" -> constant = true
        }
    }
    val precision = precisionSpecifier()?.ast() ?: Precision.mediump
    return Type(
        typeSpecifier().ast(), expression().ast(scope), constant, precision
    )
}

internal fun ScapesShaderParser.TypeContext.ast(): Type {
    val array = childCount > 1
    if (array) TODO()
    return Type(typeSpecifier().ast())
}

internal fun ScapesShaderParser.TypeSpecifierContext.ast(): Types {
    return try {
        Types.valueOf(text)
    } catch (e: IllegalArgumentException) {
        throw ShaderCompileException("Invalid type: $text", this)
    }
}

internal fun ScapesShaderParser.PrecisionSpecifierContext.ast(): Precision {
    return try {
        Precision.valueOf(text)
    } catch (e: IllegalArgumentException) {
        throw ShaderCompileException("Invalid precision: $text", this)
    }
}
