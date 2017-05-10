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

import org.tobi29.scapes.engine.utils.shader.*

tailrec fun parameters(context: ScapesShaderParser.ParameterListContext?,
                       parameters: MutableList<Parameter>,
                       scope: Scope) {
    context ?: return
    val parameter = context.parameterDeclaration().ast(scope)
    parameters.add(parameter)
    parameters(context.parameterList(), parameters, scope)
}

fun ScapesShaderParser.ParameterDeclarationContext.ast(scope: Scope): Parameter {
    val type = declarator().ast(scope)
    val name = Identifier().text
    val variable = scope.add(name,
            type.exported) ?: throw ShaderCompileException(
            "Redeclaring variable: $name", Identifier())
    return Parameter(type, variable)
}

tailrec fun parameters(context: ScapesShaderParser.ShaderParameterListContext?,
                       parameters: MutableList<ShaderParameter>,
                       scope: Scope) {
    context ?: return
    val parameter = context.shaderParameterDeclaration().ast(scope)
    parameters.add(parameter)
    parameters(context.shaderParameterList(), parameters, scope)
}

fun ScapesShaderParser.ShaderParameterDeclarationContext.ast(scope: Scope): ShaderParameter {
    val type = declarator().ast(scope)
    val idConstant = IntegerLiteral()
    val id: Int
    if (idConstant == null) {
        id = -1
    } else {
        id = idConstant.text.toInt()
    }
    val name = Identifier().text
    val variable = scope.add(name,
            type.exported) ?: throw ShaderCompileException(
            "Redeclaring variable: $name", Identifier())
    val available = expression() ?: return ShaderParameter(type, id,
            variable, BooleanExpression(true))
    return ShaderParameter(type, id, variable, available.ast(scope))
}
