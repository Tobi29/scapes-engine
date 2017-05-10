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

import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode
import org.tobi29.scapes.engine.utils.shader.*

internal fun ScapesShaderParser.ExpressionContext.ast(scope: Scope): Expression {
    primaryExpression()?.ast(scope)?.let { return it }
    return when (childCount) {
        2 -> {
            val left = children[0]
            val right = children[1]
            if (left is ScapesShaderParser.ExpressionContext
                    && right is TerminalNode) {
                parseExpression(left.ast(scope), right.symbol)
            } else if (left is TerminalNode
                    && right is ScapesShaderParser.ExpressionContext) {
                parseExpression(left.symbol, right.ast(scope))
            } else {
                throw IllegalStateException("Invalid parse tree: $this")
            }
        }
        3 -> {
            val left = children[0]
            val operator = children[1]
            val right = children[2]
            if (left is ScapesShaderParser.ExpressionContext
                    && right is ScapesShaderParser.ExpressionContext
                    && operator is TerminalNode) {
                parseExpression(left.ast(scope), operator.symbol,
                        right.ast(scope))
            } else if (left is ScapesShaderParser.ExpressionContext
                    && right is TerminalNode
                    && operator is TerminalNode) {
                parseExpression(left.ast(scope), operator.symbol, right.symbol)
            } else if (left is TerminalNode
                    && right is TerminalNode
                    && operator is TerminalNode) {
                parseExpression(left.symbol, operator.symbol, right.symbol)
            } else {
                throw IllegalStateException("Invalid parse tree: $this")
            }
        }
        4 -> {
            val left = children[0]
            val operatorLeft = children[1]
            val right = children[2]
            val operatorRight = children[3]
            if (left is ScapesShaderParser.ExpressionContext
                    && operatorLeft is TerminalNode
                    && right is ScapesShaderParser.ExpressionContext
                    && operatorRight is TerminalNode) {
                parseExpression(left.ast(scope), operatorLeft.symbol,
                        right.ast(scope), operatorRight.symbol)
            } else if (left is TerminalNode
                    && operatorLeft is TerminalNode
                    && right is ScapesShaderParser.ExpressionListContext
                    && operatorRight is TerminalNode) {
                parseExpression(left.symbol, operatorLeft.symbol,
                        right.ast(scope), operatorRight.symbol)
            } else {
                throw IllegalStateException("Invalid parse tree: $this")
            }
        }
        5 -> {
            val left = children[0]
            val operatorLeft = children[1]
            val middle = children[2]
            val operatorRight = children[3]
            val right = children[4]
            if (left is ScapesShaderParser.ExpressionContext
                    && operatorLeft is TerminalNode
                    && middle is ScapesShaderParser.ExpressionContext
                    && operatorRight is TerminalNode
                    && right is ScapesShaderParser.ExpressionContext) {
                parseExpression(left.ast(scope), operatorLeft.symbol,
                        middle.ast(scope), operatorRight.symbol,
                        right.ast(scope))
            } else {
                throw IllegalStateException("Invalid parse tree: $this")
            }
        }
        else -> throw IllegalStateException("Invalid context: $this")
    }
}

internal fun ScapesShaderParser.ExpressionListContext?.ast(scope: Scope): ArrayList<Expression> {
    val expressions = ArrayList<Expression>()
    expression(this, expressions, scope)
    return expressions
}

internal fun ScapesShaderParser.IfStatementContext.ast(scope: Scope): Expression {
    val expression = expression()
    if (expression != null) {
        return expression.ast(scope)
    }
    throw ShaderCompileException("No expression found", this)
}

internal fun ScapesShaderParser.ExpressionStatementContext.ast(scope: Scope): Expression {
    val expression = expression() ?: return VoidExpression()
    return expression.ast(scope)
}

internal fun ScapesShaderParser.PrimaryExpressionContext.ast(scope: Scope): Expression {
    val identifier = Identifier()
    if (identifier != null) {
        val name = identifier.text
        val variable = scope[name] ?: throw ShaderCompileException(
                "Unknown variable: $name", identifier)
        return IdentifierExpression(variable).apply { attach(identifier) }
    }
    literal()?.ast()?.let { return it }
    return expression().ast(scope)
}

private fun parseExpression(left: Expression,
                            operator: Token) = when (operator.text) {
    "++" -> UnaryExpression(UnaryType.GET_INCREMENT, left)
    "--" -> UnaryExpression(UnaryType.GET_DECREMENT, left)
    else -> throw IllegalStateException("Invalid token: $operator")
}

private fun parseExpression(operator: Token,
                            right: Expression) = when (operator.text) {
    "++" -> UnaryExpression(UnaryType.INCREMENT_GET, right)
    "--" -> UnaryExpression(UnaryType.DECREMENT_GET, right)
    "+" -> UnaryExpression(UnaryType.POSITIVE, right)
    "-" -> UnaryExpression(UnaryType.NEGATIVE, right)
    "~" -> UnaryExpression(UnaryType.BIT_NOT, right)
    "!" -> UnaryExpression(UnaryType.NOT, right)
    else -> throw IllegalStateException("Invalid token: $operator")
}

private fun parseExpression(left: Expression,
                            operator: Token,
                            right: Expression) = when (operator.text) {
    "+" -> FunctionExpression("plus", listOf(left, right))
    "-" -> FunctionExpression("minus", listOf(left, right))
    "*" -> FunctionExpression("times", listOf(left, right))
    "/" -> FunctionExpression("div", listOf(left, right))
    "%" -> FunctionExpression("rem", listOf(left, right))
    "&" -> FunctionExpression("and", listOf(left, right))
    "|" -> FunctionExpression("or", listOf(left, right))
    "^" -> FunctionExpression("xor", listOf(left, right))
    "<<" -> FunctionExpression("shl", listOf(left, right))
    ">>" -> FunctionExpression("shr", listOf(left, right))
    "&&" -> ConditionExpression(ConditionType.AND, left, right)
    "||" -> ConditionExpression(ConditionType.OR, left, right)
    "==" -> ConditionExpression(ConditionType.EQUALS, left, right)
    "!=" -> ConditionExpression(ConditionType.NOT_EQUALS, left, right)
    "<" -> ConditionExpression(ConditionType.LESS, left, right)
    ">" -> ConditionExpression(ConditionType.GREATER, left, right)
    "<=" -> ConditionExpression(ConditionType.LESS_EQUAL, left, right)
    ">=" -> ConditionExpression(ConditionType.GREATER_EQUAL, left, right)
    "=" -> AssignmentExpression(left, right)
    "+=" -> AssignmentExpression(left,
            FunctionExpression("plus", listOf(left, right)))
    "-=" -> AssignmentExpression(left,
            FunctionExpression("minus", listOf(left, right)))
    "*=" -> AssignmentExpression(left,
            FunctionExpression("times", listOf(left, right)))
    "/=" -> AssignmentExpression(left,
            FunctionExpression("div", listOf(left, right)))
    "%=" -> AssignmentExpression(left,
            FunctionExpression("rem", listOf(left, right)))
    "&=" -> AssignmentExpression(left,
            FunctionExpression("and", listOf(left, right)))
    "|=" -> AssignmentExpression(left,
            FunctionExpression("or", listOf(left, right)))
    "^=" -> AssignmentExpression(left,
            FunctionExpression("xor", listOf(left, right)))
    "<<=" -> AssignmentExpression(left,
            FunctionExpression("shl", listOf(left, right)))
    ">>=" -> AssignmentExpression(left,
            FunctionExpression("shr", listOf(left, right)))
    else -> throw IllegalStateException("Invalid token: $operator")
}

private fun parseExpression(left: Expression,
                            operator: Token,
                            identifier: Token) = when (operator.text) {
    "." -> MemberExpression(identifier.text, left)
    else -> throw IllegalStateException("Invalid token: $operator")
}

private fun parseExpression(identifier: Token,
                            operatorLeft: Token,
                            operatorRight: Token) = when (operatorLeft.text) {
    "(" -> when (operatorRight.text) {
        ")" -> FunctionExpression(identifier.text, emptyList())
        else -> throw IllegalStateException("Invalid token: $operatorRight")
    }
    else -> throw IllegalStateException("Invalid token: $operatorLeft")
}

private fun parseExpression(left: Expression,
                            operatorLeft: Token,
                            right: Expression,
                            operatorRight: Token) = when (operatorLeft.text) {
    "[" -> when (operatorRight.text) {
        "]" -> ArrayAccessExpression(left, right)
        else -> throw IllegalStateException("Invalid token: $operatorRight")
    }
    else -> throw IllegalStateException("Invalid token: $operatorLeft")
}

private fun parseExpression(identifier: Token,
                            operatorLeft: Token,
                            expressions: List<Expression>,
                            operatorRight: Token) = when (operatorLeft.text) {
    "(" -> when (operatorRight.text) {
        ")" -> FunctionExpression(identifier.text, expressions)
        else -> throw IllegalStateException("Invalid token: $operatorRight")
    }
    else -> throw IllegalStateException("Invalid token: $operatorLeft")
}

private fun parseExpression(left: Expression,
                            operatorLeft: Token,
                            middle: Expression,
                            operatorRight: Token,
                            right: Expression) = when (operatorLeft.text) {
    "?" -> when (operatorRight.text) {
        ":" -> TernaryExpression(left, middle, right)
        else -> throw IllegalStateException("Invalid token: $operatorRight")
    }
    else -> throw IllegalStateException("Invalid token: $operatorLeft")
}

private tailrec fun expression(context: ScapesShaderParser.ExpressionListContext?,
                               expressions: MutableList<Expression>,
                               scope: Scope) {
    context ?: return
    expressions.add(context.expression().ast(scope))
    expression(context.expressionList(), expressions, scope)
}
