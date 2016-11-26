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

import org.antlr.v4.runtime.tree.TerminalNode
import java.util.*

internal object ExpressionCompiler {
    fun expression(
            context: ScapesShaderParser.AssignmentExpressionContext,
            scope: Scope): Expression {
        val condition = context.conditionalExpression()
        if (condition == null) {
            val type: AssignmentType
            when (context.assignmentOperator().text) {
                "=" -> type = AssignmentType.ASSIGN
                "*=" -> type = AssignmentType.ASSIGN_MULTIPLY
                "/=" -> type = AssignmentType.ASSIGN_DIVIDE
                "%=" -> type = AssignmentType.ASSIGN_MODULUS
                "+=" -> type = AssignmentType.ASSIGN_PLUS
                "-=" -> type = AssignmentType.ASSIGN_MINUS
                "<<=" -> type = AssignmentType.ASSIGN_SHIFT_LEFT
                ">>=" -> type = AssignmentType.ASSIGN_SHIFT_RIGHT
                "&=" -> type = AssignmentType.ASSIGN_AND
                "|=" -> type = AssignmentType.ASSIGN_INCLUSIVE_OR
                "^=" -> type = AssignmentType.ASSIGN_EXCLUSIVE_OR
                else -> throw ShaderCompileException(
                        "Invalid assignment operator" + context.assignmentOperator().text,
                        context.assignmentOperator())
            }
            return AssignmentExpression(
                    type,
                    expression(context.unaryExpression(), scope),
                    expression(context.assignmentExpression(), scope))
        }
        return expression(condition, scope)
    }

    fun expression(context: ScapesShaderParser.ConditionalExpressionContext,
                   scope: Scope): Expression {
        val condition = context.conditionalExpression() ?: return expression(
                context.logicalOrExpression(), scope)
        return TernaryExpression(
                expression(context.logicalOrExpression(), scope),
                expression(context.expression(), scope),
                expression(condition, scope))
    }

    fun expression(context: ScapesShaderParser.LogicalOrExpressionContext,
                   scope: Scope): Expression {
        val expression = context.logicalAndExpression()
        val next = context.logicalOrExpression() ?: return expression(
                expression, scope)
        return ConditionExpression(
                ConditionType.CONDITION_LOGICAL_OR,
                expression(next, scope), expression(expression, scope))
    }

    fun expression(context: ScapesShaderParser.LogicalAndExpressionContext,
                   scope: Scope): Expression {
        val expression = context.inclusiveOrExpression()
        val next = context.logicalAndExpression() ?: return expression(
                expression, scope)
        return ConditionExpression(
                ConditionType.CONDITION_LOGICAL_AND,
                expression(next, scope), expression(expression, scope))
    }

    fun expression(context: ScapesShaderParser.InclusiveOrExpressionContext,
                   scope: Scope): Expression {
        val expression = context.exclusiveOrExpression()
        val next = context.inclusiveOrExpression() ?: return expression(
                expression, scope)
        return ConditionExpression(
                ConditionType.CONDITION_INCLUSIVE_OR,
                expression(next, scope), expression(expression, scope))
    }

    fun expression(context: ScapesShaderParser.ExclusiveOrExpressionContext,
                   scope: Scope): Expression {
        val expression = context.andExpression()
        val next = context.exclusiveOrExpression() ?: return expression(
                expression, scope)
        return ConditionExpression(
                ConditionType.CONDITION_EXCLUSIVE_OR,
                expression(next, scope), expression(expression, scope))
    }

    fun expression(context: ScapesShaderParser.AndExpressionContext,
                   scope: Scope): Expression {
        val expression = context.equalityExpression()
        val next = context.andExpression() ?: return expression(expression,
                scope)
        return ConditionExpression(
                ConditionType.CONDITION_AND,
                expression(next, scope), expression(expression, scope))
    }

    fun expression(context: ScapesShaderParser.EqualityExpressionContext,
                   scope: Scope): Expression {
        val expression = context.relationalExpression()
        val next = context.equalityExpression() ?: return expression(
                expression, scope)
        val type: ConditionType
        when (context.children[1].text) {
            "==" -> type = ConditionType.CONDITION_EQUALS
            "!=" -> type = ConditionType.CONDITION_NOT_EQUALS
            else -> throw ShaderCompileException(
                    "Invalid conditional operator: " + context.children[1].text,
                    context)
        }
        return ConditionExpression(
                type, expression(next, scope),
                expression(expression, scope))
    }

    fun expression(context: ScapesShaderParser.RelationalExpressionContext,
                   scope: Scope): Expression {
        val expression = context.shiftExpression()
        val next = context.relationalExpression() ?: return expression(
                expression, scope)
        val type: ConditionType
        when (context.children[1].text) {
            "<" -> type = ConditionType.CONDITION_LESS
            ">" -> type = ConditionType.CONDITION_GREATER
            "<=" -> type = ConditionType.CONDITION_LESS_EQUAL
            ">=" -> type = ConditionType.CONDITION_GREATER_EQUAL
            else -> throw ShaderCompileException(
                    "Invalid conditional operator: " + context.children[1].text,
                    context)
        }
        return ConditionExpression(
                type, expression(next, scope),
                expression(expression, scope))
    }

    fun expression(context: ScapesShaderParser.ShiftExpressionContext,
                   scope: Scope): Expression {
        val expression = context.additiveExpression()
        val next = context.shiftExpression() ?: return expression(
                expression, scope)
        val type: OperationType
        when (context.children[1].text) {
            "<<" -> type = OperationType.SHIFT_LEFT
            ">>" -> type = OperationType.SHIFT_RIGHT
            else -> throw ShaderCompileException(
                    "Invalid operator: " + context.children[1].text,
                    context)
        }
        return OperationExpression(
                type, expression(next, scope),
                expression(expression, scope))
    }

    fun expression(context: ScapesShaderParser.AdditiveExpressionContext,
                   scope: Scope): Expression {
        val expression = context.multiplicativeExpression()
        val next = context.additiveExpression() ?: return expression(
                expression, scope)
        val type: OperationType
        when (context.children[1].text) {
            "+" -> type = OperationType.PLUS
            "-" -> type = OperationType.MINUS
            else -> throw ShaderCompileException(
                    "Invalid operator: " + context.children[1].text,
                    context)
        }
        return OperationExpression(
                type, expression(next, scope),
                expression(expression, scope))
    }

    fun expression(context: ScapesShaderParser.MultiplicativeExpressionContext,
                   scope: Scope): Expression {
        val expression = context.unaryExpression()
        val next = context.multiplicativeExpression() ?: return expression(
                expression, scope)
        val type: OperationType
        when (context.children[1].text) {
            "*" -> type = OperationType.MULTIPLY
            "/" -> type = OperationType.DIVIDE
            "%" -> type = OperationType.MODULUS
            else -> throw ShaderCompileException(
                    "Invalid operator: " + context.children[1].text,
                    context)
        }
        return OperationExpression(
                type, expression(next, scope),
                expression(expression, scope))
    }

    fun expression(context: ScapesShaderParser.UnaryExpressionContext,
                   scope: Scope): Expression {
        val next = context.unaryExpression() ?: return expression(
                context.postfixExpression(), scope)
        val unaryOperator = context.unaryOperator()
        if (unaryOperator != null) {
            val type: UnaryType
            when (unaryOperator.text) {
                "+" -> type = UnaryType.POSITIVE
                "-" -> type = UnaryType.NEGATIVE
                "~" -> type = UnaryType.BIT_NOT
                "!" -> type = UnaryType.NOT
                else -> throw ShaderCompileException(
                        "Invalid operator: " + context.children[0].text,
                        context)
            }
            return UnaryExpression(
                    type, expression(next, scope))
        }
        val type: UnaryType
        when (context.children[0].text) {
            "++" -> type = UnaryType.INCREMENT_GET
            "--" -> type = UnaryType.DECREMENT_GET
            else -> throw ShaderCompileException(
                    "Invalid operator: " + context.children[0].text,
                    context)
        }
        return UnaryExpression(
                type, expression(next, scope))
    }

    fun expression(context: ScapesShaderParser.PostfixExpressionContext,
                   scope: Scope): Expression {
        val function = context.functionExpression()
        if (function != null) {
            return function(function, scope)
        }
        val next = context.postfixExpression() ?: return expression(
                context.primaryExpression(), scope)
        val array = context.expression()
        if (array != null) {
            return array(next, array, scope)
        }
        val field = context.Identifier()
        if (field != null) {
            return field(next, field, scope)
        }
        val type: UnaryType
        when (context.children[1].text) {
            "++" -> type = UnaryType.GET_INCREMENT
            "--" -> type = UnaryType.GET_DECREMENT
            else -> throw ShaderCompileException(
                    "Invalid operator: " + context.children[1].text,
                    context)
        }
        return UnaryExpression(
                type, expression(next, scope))
    }

    fun expression(context: ScapesShaderParser.ExpressionContext,
                   scope: Scope): Expression {
        return expression(context.assignmentExpression(), scope)
    }

    fun expression(context: ScapesShaderParser.IfStatementContext,
                   scope: Scope): Expression {
        val expression = context.expression()
        if (expression != null) {
            return ExpressionCompiler.expression(expression, scope)
        }
        throw ShaderCompileException("No expression found", context)
    }

    fun expression(context: ScapesShaderParser.ExpressionStatementContext,
                   scope: Scope): Expression {
        val expression = context.expression() ?: return VoidExpression()
        return ExpressionCompiler.expression(expression, scope)
    }

    fun array(array: ScapesShaderParser.PostfixExpressionContext,
              index: ScapesShaderParser.ExpressionContext,
              scope: Scope): Expression {
        return ArrayAccessExpression(
                expression(array, scope),
                expression(index, scope))
    }

    fun function(context: ScapesShaderParser.FunctionExpressionContext,
                 scope: Scope): Expression {
        val expressions = ArrayList<Expression>()
        var arguments = context.argumentExpressionList()
        while (arguments != null) {
            expressions.add(ExpressionCompiler.expression(
                    arguments.assignmentExpression(), scope))
            arguments = arguments.argumentExpressionList()
        }
        return FunctionExpression(
                context.Identifier().text,
                expressions).apply {
            attach(context)
        }
    }

    fun field(context: ScapesShaderParser.PostfixExpressionContext,
              name: TerminalNode,
              scope: Scope): Expression {
        return MemberExpression(
                name.text,
                expression(context, scope)).apply {
            attach(context)
        }
    }

    fun expression(context: ScapesShaderParser.PrimaryExpressionContext,
                   scope: Scope): Expression {
        val identifier = context.Identifier()
        if (identifier != null) {
            val name = identifier.text
            val variable = scope[name] ?: throw ShaderCompileException(
                    "Unknown variable: $name", identifier)
            return IdentifierExpression(
                    variable).apply {
                attach(identifier)
            }
        }
        val constant = context.constant()
        if (constant != null) {
            return LiteralCompiler.constant(constant).apply {
                attach(constant)
            }
        }
        val property = context.property()
        if (property != null) {
            return PropertyExpression(
                    property.Identifier().text).apply {
                attach(property)
            }
        }
        return ExpressionCompiler.expression(context.expression(), scope)
    }
}