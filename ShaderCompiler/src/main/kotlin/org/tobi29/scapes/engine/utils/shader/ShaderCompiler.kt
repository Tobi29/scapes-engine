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

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.TerminalNode
import org.tobi29.scapes.engine.utils.shader.expression.*
import org.tobi29.scapes.engine.utils.shader.expression.Function
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

class ShaderCompiler {
    private val declarations = ArrayList<Statement>()
    private val functions = ArrayList<Function>()
    private var shaderVertex: ShaderFunction? = null
    private var shaderFragment: ShaderFunction? = null
    private var outputs: ShaderSignature? = null
    private var uniforms = arrayOfNulls<Uniform>(0)

    @Throws(ShaderCompileException::class)
    fun compile(source: String): CompiledShader {
        declarations.clear()
        functions.clear()
        shaderVertex = null
        shaderFragment = null
        uniforms = arrayOfNulls<Uniform>(0)
        try {
            val parser = parser(source)
            var program: ScapesShaderParser.TranslationUnitContext? = parser.compilationUnit().translationUnit()
            while (program != null) {
                externalDeclaration(program.externalDeclaration())
                program = program.translationUnit()
            }
        } catch (e: ParseCancellationException) {
            throw ShaderCompileException(e)
        }
        val declarations = ArrayList<Statement>(this.declarations.size)
        declarations.addAll(this.declarations)
        val functions = ArrayList<Function>(this.functions.size)
        functions.addAll(this.functions)
        return CompiledShader(declarations, functions, shaderVertex,
                shaderFragment, outputs, uniforms)
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Throws(ShaderCompileException::class)
    private fun externalDeclaration(
            context: ScapesShaderParser.ExternalDeclarationContext) {
        val uniform = context.uniformDeclaration()
        if (uniform != null) {
            val declarator = uniform.declarator()
            val field = declarator.declaratorField()
            if (field != null) {
                val id = uniform.IntegerLiteral().text.toInt()
                if (uniforms.size <= id) {
                    val newUniforms = arrayOfNulls<Uniform>(id + 1)
                    System.arraycopy(uniforms, 0, newUniforms, 0,
                            uniforms.size)
                    uniforms = newUniforms
                }
                uniforms[id] = Uniform(type(field), id,
                        uniform.Identifier().text)
            }
            val array = declarator.declaratorArray()
            if (array != null) {
                throw UnsupportedOperationException("NYI")
            }
            return
        }
        val declaration = context.declaration()
        if (declaration != null) {
            declarations.add(declaration(declaration))
        }
        val shader = context.shaderDefinition()
        if (shader != null) {
            val signature = shader.shaderSignature()
            val name = signature.Identifier().text
            val parameters = ArrayList<ShaderParameter>()
            parameters(signature.shaderParameterList(), parameters)
            val shaderSignature = ShaderSignature(name,
                    *parameters.toTypedArray())
            val compound = compound(shader.compoundStatement().blockItemList())
            val function = ShaderFunction(shaderSignature, compound)
            when (function.signature.name) {
                "vertex" -> shaderVertex = function
                "fragment" -> shaderFragment = function
                else -> throw ShaderCompileException(
                        "Invalid shader name: ${function.signature.name}",
                        shader)
            }
            return
        }
        val outputs = context.outputsDefinition()
        if (outputs != null) {
            val parameters = ArrayList<ShaderParameter>()
            parameters(outputs.shaderParameterList(), parameters)
            val outputsSignature = ShaderSignature("outputs",
                    *parameters.toTypedArray())
            this.outputs = outputsSignature
            return
        }
        val function = context.functionDefinition()
        if (function != null) {
            val signature = function.functionSignature()
            val name = signature.Identifier().text
            val parameters = ArrayList<Parameter>()
            parameters(signature.parameterList(), parameters)
            val returned = type(signature.typeSpecifier())
            val precisionSpecifier = signature.precisionSpecifier()
            val returnedPrecision: Precision
            if (precisionSpecifier == null) {
                returnedPrecision = Precision.mediump
            } else {
                returnedPrecision = precision(precisionSpecifier)
            }
            val functionSignature = FunctionSignature(name, returned,
                    returnedPrecision,
                    *parameters.toTypedArray())
            val compound = compound(
                    function.compoundStatement().blockItemList())
            functions.add(Function(functionSignature, compound))
            return
        }
    }

    companion object {
        @Throws(ShaderCompileException::class)
        private fun parameters(
                context: ScapesShaderParser.ParameterListContext?,
                parameters: MutableList<Parameter>) {
            var context = context
            while (context != null) {
                val parameter = parameter(context.parameterDeclaration())
                parameters.add(parameter)
                context = context.parameterList()
            }
        }

        @Throws(ShaderCompileException::class)
        private fun parameter(
                context: ScapesShaderParser.ParameterDeclarationContext): Parameter {
            val type = type(context.declarator())
            val name = context.Identifier().text
            return Parameter(type, name)
        }

        @Throws(ShaderCompileException::class)
        private fun parameters(
                context: ScapesShaderParser.ShaderParameterListContext?,
                parameters: MutableList<ShaderParameter>) {
            var context = context
            while (context != null) {
                val parameter = parameter(context.shaderParameterDeclaration())
                parameters.add(parameter)
                context = context.shaderParameterList()
            }
        }

        @Throws(ShaderCompileException::class)
        private fun parameter(
                context: ScapesShaderParser.ShaderParameterDeclarationContext): ShaderParameter {
            val type = type(context.declarator())
            val idConstant = context.IntegerLiteral()
            val id: Int
            if (idConstant == null) {
                id = -1
            } else {
                id = idConstant.text.toInt()
            }
            val name = context.Identifier().text
            val property = context.property() ?: return ShaderParameter(type,
                    id, name,
                    BooleanExpression(true))
            return ShaderParameter(type, id, name,
                    PropertyExpression(property.Identifier().text))
        }

        @Throws(ShaderCompileException::class)
        private fun type(context: ScapesShaderParser.DeclaratorContext): Type {
            val field = context.declaratorField()
            if (field != null) {
                return type(field)
            }
            return type(context.declaratorArray())
        }

        @Throws(ShaderCompileException::class)
        private fun type(context: ScapesShaderParser.DeclaratorFieldContext): Type {
            var constant = false
            for (child in context.children) {
                if (child is TerminalNode) {
                    when (child.getText()) {
                        "const" -> constant = true
                    }
                }
            }
            val precisionSpecifier = context.precisionSpecifier()
            val precision: Precision
            if (precisionSpecifier == null) {
                precision = Precision.mediump
            } else {
                precision = precision(precisionSpecifier)
            }
            return Type(type(context.typeSpecifier()), constant, precision)
        }

        @Throws(ShaderCompileException::class)
        private fun type(context: ScapesShaderParser.DeclaratorArrayContext): Type {
            var constant = false
            for (child in context.children) {
                if (child is TerminalNode) {
                    when (child.getText()) {
                        "const" -> constant = true
                    }
                }
            }
            val precisionSpecifier = context.precisionSpecifier()
            val precision: Precision
            if (precisionSpecifier == null) {
                precision = Precision.mediump
            } else {
                precision = precision(precisionSpecifier)
            }
            return Type(type(context.typeSpecifier()),
                    integer(context.integerConstant()), constant, precision)
        }

        @Throws(ShaderCompileException::class)
        private fun type(
                context: ScapesShaderParser.DeclaratorArrayUnsizedContext): Type {
            var constant = false
            for (child in context.children) {
                if (child is TerminalNode) {
                    when (child.getText()) {
                        "const" -> constant = true
                    }
                }
            }
            val precisionSpecifier = context.precisionSpecifier()
            val precision: Precision
            if (precisionSpecifier == null) {
                precision = Precision.mediump
            } else {
                precision = precision(precisionSpecifier)
            }
            return Type(type(context.typeSpecifier()), constant, precision)
        }

        @Throws(ShaderCompileException::class)
        private fun type(context: ScapesShaderParser.TypeSpecifierContext): Types {
            try {
                return Types.valueOf(context.text)
            } catch (e: IllegalArgumentException) {
                throw ShaderCompileException(e, context)
            }

        }

        @Throws(ShaderCompileException::class)
        fun statement(
                context: ScapesShaderParser.StatementContext): Statement {
            val expression = context.expressionStatement()
            if (expression != null) {
                return ExpressionStatement(expression(expression))
            }
            val declaration = context.declaration()
            if (declaration != null) {
                return declaration(declaration)
            }
            val selection = context.selectionStatement()
            if (selection != null) {
                val ifStatement = selection.ifStatement()
                val elseStatement = selection.elseStatement() ?: return IfStatement(
                        expression(ifStatement),
                        statement(selection.statement()))
                return IfStatement(expression(ifStatement),
                        statement(selection.statement()),
                        statement(elseStatement.statement()))
            }
            val rangeLoop = context.rangeLoopStatement()
            if (rangeLoop != null) {
                val name = rangeLoop.Identifier().text
                val start = integer(rangeLoop.integerConstant(0))
                val end = integer(rangeLoop.integerConstant(1))
                val statement = statement(rangeLoop.statement())
                return LoopFixedStatement(name, start, end, statement)
            }
            return compound(context.compoundStatement().blockItemList())
        }

        @Throws(ShaderCompileException::class)
        fun declaration(
                context: ScapesShaderParser.DeclarationContext): Statement {
            val field = context.declarationField()
            if (field != null) {
                return declaration(field)
            }
            val array = context.declarationArray()
            if (array != null) {
                return declaration(array)
            }
            throw ShaderCompileException("No declaration", context)
        }

        @Throws(ShaderCompileException::class)
        fun declaration(
                context: ScapesShaderParser.DeclarationFieldContext): Statement {
            val type = type(context.declaratorField())
            return declaration(type, context.initDeclaratorFieldList())
        }

        @Throws(ShaderCompileException::class)
        fun declaration(type: Type,
                        context: ScapesShaderParser.InitDeclaratorFieldListContext?): Statement {
            var context = context
            val expressions = ArrayList<Declaration>()
            while (context != null) {
                val declarator = context.initDeclaratorField()
                val initializer = declarator.initializerField()
                val name = declarator.Identifier().text
                if (initializer == null) {
                    expressions.add(Declaration(name))
                } else {
                    expressions.add(Declaration(name,
                            expression(initializer.assignmentExpression())))
                }
                context = context.initDeclaratorFieldList()
            }
            return DeclarationStatement(type, expressions)
        }

        @Throws(ShaderCompileException::class)
        fun declaration(
                context: ScapesShaderParser.DeclarationArrayContext): Statement {
            val list = context.initDeclaratorArrayList()
            if (list != null) {
                val declarator = context.declaratorArray()
                val type = type(declarator)
                val length = integer(declarator.integerConstant())
                return declaration(type, length, list)
            }
            val type = type(context.declaratorArrayUnsized())
            val initializer = context.initializerArray()
            return ArrayUnsizedDeclarationStatement(type,
                    context.Identifier().text, initializer(initializer))
        }

        fun declaration(type: Type,
                        length: Expression,
                        context: ScapesShaderParser.InitDeclaratorArrayListContext?): Statement {
            var context = context
            val declarations = ArrayList<ArrayDeclaration>()
            while (context != null) {
                val name = context.Identifier().text
                declarations.add(ArrayDeclaration(name))
                context = context.initDeclaratorArrayList()
            }
            return ArrayDeclarationStatement(type, length, declarations)
        }

        @Throws(ShaderCompileException::class)
        fun initializer(
                context: ScapesShaderParser.InitializerArrayContext): ArrayExpression {
            val list = context.initializerArrayList()
            if (list != null) {
                return initializer(list)
            }
            return PropertyArrayExpression(
                    context.property().Identifier().text)
        }

        @Throws(ShaderCompileException::class)
        fun initializer(
                context: ScapesShaderParser.InitializerArrayListContext?): ArrayExpression {
            var context = context
            val expressions = ArrayList<Expression>()
            while (context != null) {
                expressions.add(expression(context.assignmentExpression()))
                context = context.initializerArrayList()
            }
            return ArrayLiteralExpression(expressions)
        }

        @Throws(ShaderCompileException::class)
        fun precision(
                context: ScapesShaderParser.PrecisionSpecifierContext): Precision {
            try {
                return Precision.valueOf(context.text)
            } catch (e: IllegalArgumentException) {
                throw ShaderCompileException(e, context)
            }

        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.IfStatementContext): Expression {
            val expression = context.expression()
            if (expression != null) {
                return expression(expression)
            }
            throw ShaderCompileException("No expression found", context)
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.ExpressionStatementContext): Expression {
            val expression = context.expression() ?: return VoidExpression()
            return expression(expression)
        }

        @Throws(ShaderCompileException::class)
        fun compound(
                context: ScapesShaderParser.BlockItemListContext): CompoundStatement {
            return CompoundStatement(block(context))
        }

        @Throws(ShaderCompileException::class)
        fun block(
                context: ScapesShaderParser.BlockItemListContext?): StatementBlock {
            var context = context
            val expressions = ArrayList<Statement>()
            while (context != null) {
                expressions.add(statement(context.statement()))
                context = context.blockItemList()
            }
            return StatementBlock(expressions)
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.AssignmentExpressionContext): Expression {
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
                return AssignmentExpression(type,
                        expression(context.unaryExpression()),
                        expression(context.assignmentExpression()))
            }
            return expression(condition)
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.ConditionalExpressionContext): Expression {
            val condition = context.conditionalExpression() ?: return expression(
                    context.logicalOrExpression())
            return TernaryExpression(expression(context.logicalOrExpression()),
                    expression(context.expression()),
                    expression(context.conditionalExpression()))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.LogicalOrExpressionContext): Expression {
            val expression = context.logicalAndExpression()
            val next = context.logicalOrExpression() ?: return expression(
                    expression)
            return ConditionExpression(ConditionType.CONDITION_LOGICAL_OR,
                    expression(next), expression(expression))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.LogicalAndExpressionContext): Expression {
            val expression = context.inclusiveOrExpression()
            val next = context.logicalAndExpression() ?: return expression(
                    expression)
            return ConditionExpression(ConditionType.CONDITION_LOGICAL_AND,
                    expression(next), expression(expression))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.InclusiveOrExpressionContext): Expression {
            val expression = context.exclusiveOrExpression()
            val next = context.inclusiveOrExpression() ?: return expression(
                    expression)
            return ConditionExpression(ConditionType.CONDITION_INCLUSIVE_OR,
                    expression(next), expression(expression))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.ExclusiveOrExpressionContext): Expression {
            val expression = context.andExpression()
            val next = context.exclusiveOrExpression() ?: return expression(
                    expression)
            return ConditionExpression(ConditionType.CONDITION_EXCLUSIVE_OR,
                    expression(next), expression(expression))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.AndExpressionContext): Expression {
            val expression = context.equalityExpression()
            val next = context.andExpression() ?: return expression(expression)
            return ConditionExpression(ConditionType.CONDITION_AND,
                    expression(next), expression(expression))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.EqualityExpressionContext): Expression {
            val expression = context.relationalExpression()
            val next = context.equalityExpression() ?: return expression(
                    expression)
            val type: ConditionType
            when (context.children[1].text) {
                "==" -> type = ConditionType.CONDITION_EQUALS
                "!=" -> type = ConditionType.CONDITION_NOT_EQUALS
                else -> throw ShaderCompileException(
                        "Invalid conditional operator: " + context.children[1].text,
                        context)
            }
            return ConditionExpression(type, expression(next),
                    expression(expression))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.RelationalExpressionContext): Expression {
            val expression = context.shiftExpression()
            val next = context.relationalExpression() ?: return expression(
                    expression)
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
            return ConditionExpression(type, expression(next),
                    expression(expression))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.ShiftExpressionContext): Expression {
            val expression = context.additiveExpression()
            val next = context.shiftExpression() ?: return expression(
                    expression)
            val type: OperationType
            when (context.children[1].text) {
                "<<" -> type = OperationType.SHIFT_LEFT
                ">>" -> type = OperationType.SHIFT_RIGHT
                else -> throw ShaderCompileException(
                        "Invalid operator: " + context.children[1].text,
                        context)
            }
            return OperationExpression(type, expression(next),
                    expression(expression))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.AdditiveExpressionContext): Expression {
            val expression = context.multiplicativeExpression()
            val next = context.additiveExpression() ?: return expression(
                    expression)
            val type: OperationType
            when (context.children[1].text) {
                "+" -> type = OperationType.PLUS
                "-" -> type = OperationType.MINUS
                else -> throw ShaderCompileException(
                        "Invalid operator: " + context.children[1].text,
                        context)
            }
            return OperationExpression(type, expression(next),
                    expression(expression))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.MultiplicativeExpressionContext): Expression {
            val expression = context.unaryExpression()
            val next = context.multiplicativeExpression() ?: return expression(
                    expression)
            val type: OperationType
            when (context.children[1].text) {
                "*" -> type = OperationType.MULTIPLY
                "/" -> type = OperationType.DIVIDE
                "%" -> type = OperationType.MODULUS
                else -> throw ShaderCompileException(
                        "Invalid operator: " + context.children[1].text,
                        context)
            }
            return OperationExpression(type, expression(next),
                    expression(expression))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.UnaryExpressionContext): Expression {
            val next = context.unaryExpression() ?: return expression(
                    context.postfixExpression())
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
                return UnaryExpression(type, expression(next))
            }
            val type: UnaryType
            when (context.children[0].text) {
                "++" -> type = UnaryType.INCREMENT_GET
                "--" -> type = UnaryType.DECREMENT_GET
                else -> throw ShaderCompileException(
                        "Invalid operator: " + context.children[0].text,
                        context)
            }
            return UnaryExpression(type, expression(next))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.PostfixExpressionContext): Expression {
            val next = context.postfixExpression() ?: return expression(
                    context.primaryExpression())
            val array = context.expression()
            if (array != null) {
                return array(next, array)
            }
            val arguments = context.argumentExpressionList()
            if (arguments != null) {
                return function(next, arguments)
            }
            val field = context.Identifier()
            if (field != null) {
                return field(next, field)
            }
            val type: UnaryType
            when (context.children[1].text) {
                "++" -> type = UnaryType.GET_INCREMENT
                "--" -> type = UnaryType.GET_DECREMENT
                "(" -> return function(next, emptyList<Expression>())
                else -> throw ShaderCompileException(
                        "Invalid operator: " + context.children[1].text,
                        context)
            }
            return UnaryExpression(type, expression(next))
        }

        @Throws(ShaderCompileException::class)
        fun array(
                array: ScapesShaderParser.PostfixExpressionContext,
                index: ScapesShaderParser.ExpressionContext): Expression {
            return ArrayAccessExpression(expression(array), expression(index))
        }

        @Throws(ShaderCompileException::class)
        fun function(
                function: ScapesShaderParser.PostfixExpressionContext,
                arguments: ScapesShaderParser.ArgumentExpressionListContext?): Expression {
            var arguments = arguments
            val expressions = ArrayList<Expression>()
            while (arguments != null) {
                expressions.add(expression(arguments.assignmentExpression()))
                arguments = arguments.argumentExpressionList()
            }
            return function(function, expressions)
        }

        @Throws(ShaderCompileException::class)
        fun function(
                function: ScapesShaderParser.PostfixExpressionContext,
                arguments: List<Expression>): Expression {
            val variable = expression(function)
            if (variable !is VariableExpression) {
                throw ShaderCompileException("Function call on member variable",
                        function)
            }
            return FunctionExpression((variable as IdentifierExpression).name,
                    arguments)
        }

        @Throws(ShaderCompileException::class)
        fun field(
                parent: ScapesShaderParser.PostfixExpressionContext,
                name: TerminalNode): Expression {
            return MemberExpression(name.text, expression(parent))
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.PrimaryExpressionContext): Expression {
            val identifier = context.Identifier()
            if (identifier != null) {
                return VariableExpression(identifier.text)
            }
            val constant = context.constant()
            if (constant != null) {
                return constant(constant)
            }
            val property = context.property()
            if (property != null) {
                return PropertyExpression(property.Identifier().text)
            }
            return expression(context.expression())
        }

        @Throws(ShaderCompileException::class)
        fun expression(
                context: ScapesShaderParser.ExpressionContext): Expression {
            return expression(context.assignmentExpression())
        }

        @Throws(ShaderCompileException::class)
        fun integer(
                context: ScapesShaderParser.IntegerConstantContext): IntegerExpression {
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

        @Throws(ShaderCompileException::class)
        fun floating(
                context: ScapesShaderParser.FloatingConstantContext): Expression {
            val literal = context.FloatingLiteral()
            if (literal != null) {
                return FloatingExpression(BigDecimal(literal.text))
            }
            val property = context.property()
            if (property != null) {
                return PropertyExpression(property.Identifier().text)
            }
            throw ShaderCompileException("No constant found", context)
        }

        @Throws(ShaderCompileException::class)
        fun character(
                context: ScapesShaderParser.CharacterConstantContext): Expression {
            // TODO: Implement
            throw ShaderCompileException("NYI", context)
        }

        @Throws(ShaderCompileException::class)
        fun constant(
                context: ScapesShaderParser.ConstantContext): Expression {
            val integer = context.integerConstant()
            if (integer != null) {
                return integer(integer)
            }
            val floating = context.floatingConstant()
            if (floating != null) {
                return floating(floating)
            }
            val character = context.characterConstant()
            if (character != null) {
                return character(character)
            }
            throw ShaderCompileException("No constant found", context)
        }

        fun parser(source: String): ScapesShaderParser {
            val streamIn = ANTLRInputStream(source)
            val lexer = ScapesShaderLexer(streamIn)
            val parser = ScapesShaderParser(CommonTokenStream(lexer))
            parser.removeErrorListeners()
            parser.addErrorListener(object : BaseErrorListener() {
                override fun syntaxError(recognizer: Recognizer<*, *>?,
                                         offendingSymbol: Any?,
                                         line: Int,
                                         charPositionInLine: Int,
                                         msg: String?,
                                         e: RecognitionException?) {
                    throw ParseCancellationException(
                            "line " + line + ':' + charPositionInLine + ' ' +
                                    msg)
                }
            })
            return parser
        }
    }
}
