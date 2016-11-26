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

package org.tobi29.scapes.engine.utils.shader

import org.tobi29.scapes.engine.utils.shader.BooleanExpression
import org.tobi29.scapes.engine.utils.shader.Parameter
import org.tobi29.scapes.engine.utils.shader.PropertyExpression
import org.tobi29.scapes.engine.utils.shader.ShaderParameter

internal object ParameterCompiler {
    tailrec fun parameters(context: ScapesShaderParser.ParameterListContext,
                           parameters: MutableList<Parameter>,
                           scope: Scope) {
        val parameter = parameter(context.parameterDeclaration(), scope)
        parameters.add(parameter)
        val next = context.parameterList()
        if (next != null) {
            parameters(next, parameters, scope)
        }
    }

    fun parameter(context: ScapesShaderParser.ParameterDeclarationContext,
                  scope: Scope): Parameter {
        context.Identifier().symbol
        val type = TypeCompiler.type(context.declarator())
        val name = context.Identifier().text
        val variable = scope.add(name) ?: throw ShaderCompileException(
                "Redeclaring variable: $name", context.Identifier())
        return Parameter(type,
                variable)
    }

    tailrec fun parameters(context: ScapesShaderParser.ShaderParameterListContext,
                           parameters: MutableList<ShaderParameter>,
                           scope: Scope) {
        val parameter = parameter(context.shaderParameterDeclaration(), scope)
        parameters.add(parameter)
        val next = context.shaderParameterList()
        if (next != null) {
            parameters(next, parameters, scope)
        }
    }

    fun parameter(context: ScapesShaderParser.ShaderParameterDeclarationContext,
                  scope: Scope): ShaderParameter {
        val type = TypeCompiler.type(context.declarator())
        val idConstant = context.IntegerLiteral()
        val id: Int
        if (idConstant == null) {
            id = -1
        } else {
            id = idConstant.text.toInt()
        }
        val name = context.Identifier().text
        val variable = scope.add(name) ?: throw ShaderCompileException(
                "Redeclaring variable: $name", context.Identifier())
        val property = context.property() ?: return ShaderParameter(
                type,
                id, variable,
                BooleanExpression(
                        true))
        return ShaderParameter(
                type, id, variable,
                PropertyExpression(
                        property.Identifier().text))
    }
}