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

import org.tobi29.scapes.engine.utils.BigDecimal
import org.tobi29.scapes.engine.utils.BigInteger
import org.tobi29.scapes.engine.utils.shader.*

internal object LiteralParser {
    fun integer(context: ScapesShaderParser.IntegerConstantContext): IntegerExpression {
        val literal = context.IntegerLiteral()
        if (literal != null) {
            return IntegerLiteralExpression(
                    BigInteger(literal.text))
        }
        val property = context.property()
        if (property != null) {
            return IntegerPropertyExpression(
                    property.Identifier().text)
        }
        throw ShaderCompileException("No constant found", context)
    }

    fun floating(context: ScapesShaderParser.FloatingConstantContext): Expression {
        val literal = context.FloatingLiteral()
        if (literal != null) {
            return FloatingExpression(
                    BigDecimal(literal.text))
        }
        val property = context.property()
        if (property != null) {
            return PropertyExpression(
                    property.Identifier().text)
        }
        throw ShaderCompileException("No constant found", context)
    }

    fun constant(context: ScapesShaderParser.ConstantContext): Expression {
        val integer = context.integerConstant()
        if (integer != null) {
            return integer(integer)
        }
        val floating = context.floatingConstant()
        if (floating != null) {
            return floating(floating)
        }
        throw ShaderCompileException("No constant found", context)
    }
}