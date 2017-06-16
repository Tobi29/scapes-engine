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

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode
import org.tobi29.scapes.engine.utils.shader.ShaderCompileException

internal fun ShaderCompileException(message: String,
                                    context: TerminalNode) =
        ShaderCompileException(message(message, context.symbol))

internal fun ShaderCompileException(message: String,
                                    context: ParserRuleContext) =
        ShaderCompileException(message(message, context.start))

internal inline fun <T : TerminalNode, R> T.compileContext(block: T.() -> R) =
        try {
            block()
        } catch (e: ShaderCompileException) {
            throw ShaderCompileException(e.message ?: "", this)
        }

internal inline fun <C : ParserRuleContext, R> C.compileContext(block: C.() -> R) =
        try {
            block()
        } catch (e: ShaderCompileException) {
            throw ShaderCompileException(e.message ?: "", this)
        }

private fun message(message: String,
                    token: Token): String {
    return "${token.line}:${token.charPositionInLine} -> $message"
}
