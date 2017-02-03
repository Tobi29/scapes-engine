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

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.util.*

class ShaderCompiler {
    private val declarations = ArrayList<Statement>()
    private val functions = ArrayList<Function>()
    private val shaders = HashMap <String, Pair<Scope, (Scope) -> ShaderFunction>>()
    private var outputs: ShaderSignature? = null
    private var uniforms = arrayOfNulls<Uniform>(0)

    fun compile(source: String): CompiledShader {
        declarations.clear()
        functions.clear()
        uniforms = arrayOfNulls<Uniform>(0)
        val scope = Scope()
        scope.add("out_Position")
        scope.add("varying_Fragment")
        try {
            val parser = parser(source)
            var program: ScapesShaderParser.TranslationUnitContext? = parser.compilationUnit().translationUnit()
            while (program != null) {
                parser.tokenStream[program.sourceInterval.a]
                externalDeclaration(program.externalDeclaration(), shaders,
                        scope)
                program = program.translationUnit()
            }
        } catch (e: ParseCancellationException) {
            throw ShaderCompileException(e)
        }
        val declarations = ArrayList<Statement>(this.declarations.size)
        declarations.addAll(this.declarations)
        val functions = ArrayList<Function>(this.functions.size)
        functions.addAll(this.functions)
        val shaderFragment = shaders["fragment"]?.let { shader ->
            Pair(shader.first, shader.second(scope))
        }
        val shaderVertex = shaders["vertex"]?.let { shader ->
            if (shaderFragment == null) {
                throw ShaderCompileException(
                        "Vertex shader requires fragment shader!")
            }
            Pair(shader.first, shader.second(shaderFragment.first))
        }
        return CompiledShader(declarations, functions, shaderVertex?.second,
                shaderFragment?.second, outputs, scope, uniforms)
    }

    private fun externalDeclaration(
            context: ScapesShaderParser.ExternalDeclarationContext,
            shaders: MutableMap<String, Pair<Scope, (Scope) -> ShaderFunction>>,
            scope: Scope) {
        val uniform = context.uniformDeclaration()
        if (uniform != null) {
            val declarator = uniform.declarator()
            val field = declarator.declaratorField()
            if (field != null) {
                val id = uniform.IntegerLiteral().text.toInt()
                val name = uniform.Identifier().text
                if (uniforms.size <= id) {
                    val newUniforms = arrayOfNulls<Uniform>(id + 1)
                    System.arraycopy(uniforms, 0, newUniforms, 0,
                            uniforms.size)
                    uniforms = newUniforms
                }
                val variable = scope.add(name) ?: throw ShaderCompileException(
                        "Redeclaring variable: $name", uniform.Identifier())
                uniforms[id] = Uniform(
                        TypeCompiler.type(field), id, variable)
            }
            val array = declarator.declaratorArray()
            if (array != null) {
                throw UnsupportedOperationException("NYI")
            }
            return
        }
        val declaration = context.declaration()
        if (declaration != null) {
            declarations.add(declaration(declaration, scope))
        }
        val shader = context.shaderDefinition()
        if (shader != null) {
            val signature = shader.shaderSignature()
            val name = signature.Identifier().text
            val parameters = ArrayList<ShaderParameter>()
            val inputScope = Scope(scope)
            ParameterCompiler.parameters(signature.shaderParameterList(),
                    parameters, inputScope)
            val shaderSignature = ShaderSignature(
                    name,
                    *parameters.toTypedArray())
            shaders[name] = Pair(inputScope, { shaderScope ->
                val compound = compound(
                        shader.compoundStatement().blockItemList(),
                        Scope(inputScope, shaderScope))
                ShaderFunction(
                        shaderSignature, compound)
            })
            return
        }
        val outputs = context.outputsDefinition()
        if (outputs != null) {
            val parameters = ArrayList<ShaderParameter>()
            ParameterCompiler.parameters(outputs.shaderParameterList(),
                    parameters, scope)
            val outputsSignature = ShaderSignature(
                    "outputs",
                    *parameters.toTypedArray())
            this.outputs = outputsSignature
            return
        }
        val function = context.functionDefinition()
        if (function != null) {
            val signature = function.functionSignature()
            val name = signature.Identifier().text
            val parameters = ArrayList<Parameter>()
            val functionScope = Scope(scope)
            ParameterCompiler.parameters(signature.parameterList(), parameters,
                    functionScope)
            val returned = TypeCompiler.type(signature.typeSpecifier())
            val precisionSpecifier = signature.precisionSpecifier()
            val returnedPrecision: Precision
            if (precisionSpecifier == null) {
                returnedPrecision = Precision.mediump
            } else {
                returnedPrecision = TypeCompiler.precision(precisionSpecifier)
            }
            val functionSignature = FunctionSignature(
                    name, returned,
                    returnedPrecision,
                    *parameters.toTypedArray())
            val compound = compound(
                    function.compoundStatement().blockItemList(),
                    Scope(functionScope))
            functions.add(
                    Function(
                            functionSignature, compound))
            return
        }
    }

    companion object {
        fun statement(context: ScapesShaderParser.StatementContext,
                      scope: Scope): Statement {
            val expression = context.expressionStatement()
            if (expression != null) {
                return ExpressionStatement(
                        ExpressionCompiler.expression(expression, scope))
            }
            val declaration = context.declaration()
            if (declaration != null) {
                return declaration(declaration, scope)
            }
            val selection = context.selectionStatement()
            if (selection != null) {
                val ifStatement = selection.ifStatement()
                val elseStatement = selection.elseStatement() ?: return IfStatement(
                        ExpressionCompiler.expression(ifStatement, scope),
                        statement(selection.statement(), scope))
                return IfStatement(
                        ExpressionCompiler.expression(ifStatement, scope),
                        statement(selection.statement(), scope),
                        statement(elseStatement.statement(), scope))
            }
            val rangeLoop = context.rangeLoopStatement()
            if (rangeLoop != null) {
                val name = rangeLoop.Identifier().text
                val start = LiteralCompiler.integer(
                        rangeLoop.integerConstant(0))
                val end = LiteralCompiler.integer(rangeLoop.integerConstant(1))
                val loopScope = Scope(scope)
                val variable = loopScope.add(
                        name) ?: throw ShaderCompileException(
                        "Redeclaring variable: $name", rangeLoop.Identifier())
                val statement = statement(rangeLoop.statement(), loopScope)
                return LoopFixedStatement(
                        variable, start, end, statement)
            }
            return compound(context.compoundStatement().blockItemList(), scope)
        }

        fun declaration(context: ScapesShaderParser.DeclarationContext,
                        scope: Scope): Statement {
            val field = context.declarationField()
            if (field != null) {
                return declaration(field, scope)
            }
            val array = context.declarationArray()
            if (array != null) {
                return declaration(array, scope)
            }
            throw ShaderCompileException("No declaration", context)
        }

        fun declaration(context: ScapesShaderParser.DeclarationFieldContext,
                        scope: Scope): Statement {
            val type = TypeCompiler.type(context.declaratorField())
            return declaration(type, context.initDeclaratorFieldList(), scope)
        }

        fun declaration(type: Type,
                        context: ScapesShaderParser.InitDeclaratorFieldListContext?,
                        scope: Scope): Statement {
            val expressions = ArrayList<Declaration>()
            declaration(context, expressions, scope)
            return DeclarationStatement(type, expressions)
        }

        private tailrec fun declaration(context: ScapesShaderParser.InitDeclaratorFieldListContext?,
                                        expressions: MutableList<Declaration>,
                                        scope: Scope) {
            context ?: return
            val declarator = context.initDeclaratorField()
            val initializer = declarator.initializerField()
            val name = declarator.Identifier().text
            if (initializer == null) {
                val variable = scope.add(name) ?: throw ShaderCompileException(
                        "Redeclaring variable: $name", declarator)
                expressions.add(Declaration(variable))
            } else {
                val init = ExpressionCompiler.expression(
                        initializer.assignmentExpression(), scope)
                val variable = scope.add(name) ?: throw ShaderCompileException(
                        "Redeclaring variable: $name", declarator)
                expressions.add(Declaration(variable, init))
            }
            declaration(context.initDeclaratorFieldList(), expressions, scope)
        }

        fun declaration(context: ScapesShaderParser.DeclarationArrayContext,
                        scope: Scope): Statement {
            val list = context.initDeclaratorArrayList()
            if (list != null) {
                val declarator = context.declaratorArray()
                val type = TypeCompiler.type(declarator)
                val length = LiteralCompiler.integer(
                        declarator.integerConstant())
                return declaration(type, length, list, scope)
            }
            val type = TypeCompiler.type(context.declaratorArrayUnsized())
            val initializer = context.initializerArray()
            val name = context.Identifier().text
            val init = initializer(initializer, scope)
            val variable = scope.add(name) ?: throw ShaderCompileException(
                    "Redeclaring variable: $name", context)
            return ArrayUnsizedDeclarationStatement(
                    type, variable, init)
        }

        fun declaration(type: Type,
                        length: Expression,
                        context: ScapesShaderParser.InitDeclaratorArrayListContext?,
                        scope: Scope): Statement {
            val declarations = ArrayList<ArrayDeclaration>()
            declaration(context, declarations, scope)
            return ArrayDeclarationStatement(type, length, declarations)
        }

        private tailrec fun declaration(context: ScapesShaderParser.InitDeclaratorArrayListContext?,
                                        declarations: MutableList<ArrayDeclaration>,
                                        scope: Scope) {
            context ?: return
            val name = context.Identifier().text
            val variable = scope.add(name) ?: throw ShaderCompileException(
                    "Redeclaring variable: $name", context)
            declarations.add(ArrayDeclaration(variable))
            declaration(context.initDeclaratorArrayList(), declarations, scope)
        }

        fun initializer(context: ScapesShaderParser.InitializerArrayContext,
                        scope: Scope): ArrayExpression {
            val list = context.initializerArrayList()
            if (list != null) {
                return initializer(list, scope)
            }
            return ArrayExpression.Property(
                    context.property().Identifier().text)
        }

        fun initializer(context: ScapesShaderParser.InitializerArrayListContext?,
                        scope: Scope): ArrayExpression {
            val expressions = ArrayList<Expression>()
            initializer(context, expressions, scope)
            return ArrayExpression.Literal(expressions)
        }

        private tailrec fun initializer(context: ScapesShaderParser.InitializerArrayListContext?,
                                        expressions: MutableList<Expression>,
                                        scope: Scope) {
            context ?: return
            expressions.add(ExpressionCompiler.expression(
                    context.assignmentExpression(), scope))
            initializer(context.initializerArrayList(), expressions, scope)
        }

        fun compound(context: ScapesShaderParser.BlockItemListContext,
                     scope: Scope): CompoundStatement {
            return CompoundStatement(
                    block(context, Scope(scope)))
        }

        fun block(context: ScapesShaderParser.BlockItemListContext?,
                  scope: Scope): StatementBlock {
            val expressions = ArrayList<Statement>()
            block(context, expressions, scope)
            return StatementBlock(expressions)
        }

        private tailrec fun block(context: ScapesShaderParser.BlockItemListContext?,
                                  expressions: MutableList<Statement>,
                                  scope: Scope) {
            context ?: return
            expressions.add(statement(context.statement(), scope))
            block(context.blockItemList(), expressions, scope)
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
                            "line $line:$charPositionInLine $msg")
                }
            })
            return parser
        }
    }
}
