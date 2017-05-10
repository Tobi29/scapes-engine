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

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

sealed class ShaderException(message: String) : Exception(message)

class ShaderCompileException : ShaderException {
    constructor(message: String,
                context: TerminalNode) : super(
            message(message, context.symbol))

    constructor(message: String,
                context: ParserRuleContext) : super(
            message(message, context.start))

    constructor(message: String) : super(message)
}

class ShaderASTException(message: String,
                         expression: Expression
) : ShaderException(message(message, expression))

class ShaderGenerateException : Exception {
    constructor(message: String) : super(message)

    constructor(message: String,
                expression: Expression) : super(
            message(message, expression))

    constructor(e: Exception) : super(e.toString())
}

private fun message(message: String,
                    token: Token): String {
    return "${token.line}:${token.charPositionInLine} -> $message"
}

private fun message(message: String,
                    context: ParseTree,
                    tokens: TokenStream): String {
    val token = tokens[context.sourceInterval.a]
    return message(message, token)
}

private fun message(message: String,
                    expression: Expression): String {
    val location = expression.location ?: return message
    return "${location.x}:${location.y} -> $message"
}
