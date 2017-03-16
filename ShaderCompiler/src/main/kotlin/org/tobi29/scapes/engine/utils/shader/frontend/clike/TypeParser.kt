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

package org.tobi29.scapes.engine.utils.shader.frontend.clike

import org.antlr.v4.runtime.tree.TerminalNode
import org.tobi29.scapes.engine.utils.shader.*

internal object TypeParser {
    fun type(context: ScapesShaderParser.DeclaratorContext): Type {
        val field = context.declaratorField()
        if (field != null) {
            return type(field)
        }
        return type(context.declaratorArray())
    }

    fun type(context: ScapesShaderParser.DeclaratorFieldContext): Type {
        var constant = false
        context.children.asSequence().filter { it is TerminalNode }.forEach { child ->
            when (child.text) {
                "const" -> constant = true
            }
        }
        val precisionSpecifier = context.precisionSpecifier()
        val precision: Precision
        if (precisionSpecifier == null) {
            precision = Precision.mediump
        } else {
            precision = precision(precisionSpecifier)
        }
        return Type(
                type(context.typeSpecifier()), constant, precision)
    }

    fun type(context: ScapesShaderParser.DeclaratorArrayContext): Type {
        var constant = false
        context.children.asSequence().filter { it is TerminalNode }.forEach { child ->
            when (child.text) {
                "const" -> constant = true
            }
        }
        val precisionSpecifier = context.precisionSpecifier()
        val precision: Precision
        if (precisionSpecifier == null) {
            precision = Precision.mediump
        } else {
            precision = precision(precisionSpecifier)
        }
        return Type(
                type(context.typeSpecifier()),
                LiteralParser.integer(context.integerConstant()), constant,
                precision)
    }

    fun type(context: ScapesShaderParser.DeclaratorArrayUnsizedContext): Type {
        var constant = false
        context.children.asSequence().filter { it is TerminalNode }.forEach { child ->
            when (child.text) {
                "const" -> constant = true
            }
        }
        val precisionSpecifier = context.precisionSpecifier()
        val precision: Precision
        if (precisionSpecifier == null) {
            precision = Precision.mediump
        } else {
            precision = precision(precisionSpecifier)
        }
        return Type(
                type(context.typeSpecifier()), constant, precision)
    }

    fun type(context: ScapesShaderParser.TypeContext): TypeExported {
        val array = context.childCount > 1
        return TypeExported(
                type(context.typeSpecifier()), array)
    }

    fun type(context: ScapesShaderParser.TypeSpecifierContext): Types {
        try {
            return Types.valueOf(context.text)
        } catch (e: IllegalArgumentException) {
            throw ShaderCompileException(e, context)
        }
    }

    fun precision(context: ScapesShaderParser.PrecisionSpecifierContext): Precision {
        try {
            return Precision.valueOf(context.text)
        } catch (e: IllegalArgumentException) {
            throw ShaderCompileException(e, context)
        }
    }
}