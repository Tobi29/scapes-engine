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

import org.tobi29.scapes.engine.shader.*

internal tailrec fun ParameterScope.parameters(
    context: ScapesShaderParser.ParameterListContext?
) {
    context ?: return
    val parameter = context.parameterDeclaration().ast(scope)
    add(parameter)
    parameters(context.parameterList())
}

internal fun ScapesShaderParser.ParameterDeclarationContext.ast(scope: Scope): Parameter {
    val type = declarator().ast(scope)
    val name = Identifier().text
    val variable = scope.add(
        name, type
    ) ?: throw ShaderCompileException(
        "Redeclaring variable: $name", Identifier()
    )
    return Parameter(type, variable)
}

internal tailrec fun ShaderParameterScope.parameters(
    context: ScapesShaderParser.ShaderParameterListContext?
) {
    context ?: return
    add(context.shaderParameterDeclaration().ast(scope))
    parameters(context.shaderParameterList())
}

internal fun ScapesShaderParser.ShaderParameterDeclarationContext.ast(scope: Scope): ShaderParameter {
    val type = declarator().ast(scope)
    val idConstant = IntegerLiteral()
    val id: Int
    id = if (idConstant == null) {
        -1
    } else {
        idConstant.text.toInt()
    }
    val name = Identifier().text
    val variable = scope.add(
        name, type
    ) ?: throw ShaderCompileException(
        "Redeclaring variable: $name", Identifier()
    )
    val available = expression() ?: return ShaderParameter(
        type, id,
        variable, BooleanExpression(true)
    )
    return ShaderParameter(type, id, variable, available.ast(scope))
}
