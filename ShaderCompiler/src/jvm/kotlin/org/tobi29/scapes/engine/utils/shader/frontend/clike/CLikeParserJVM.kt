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

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.tobi29.scapes.engine.utils.profiler.profilerSection
import org.tobi29.scapes.engine.utils.shader.*
import org.tobi29.scapes.engine.utils.shader.Function

object CLikeParser {
    fun externalDeclaration(
            context: ScapesShaderParser.TranslationUnitContext,
            scope: Scope): ShaderProgram {
        val declarations = ArrayList<Statement>()
        val functions = ArrayList<Function>()
        val shaders = HashMap <String, Pair<Scope, (Scope) -> ShaderFunction>>()
        var outputSignature: ShaderSignature? = null
        val uniforms = ArrayList<Uniform?>()
        val properties = ArrayList<Property>()

        var current: ScapesShaderParser.TranslationUnitContext? = context
        while (current != null) {
            val declContext = current.externalDeclaration()
            declContext.uniformDeclaration()?.let {
                profilerSection("Parse uniform") {
                    uniform(it, uniforms, scope)
                }
            }
            declContext.propertyDeclaration()?.let {
                profilerSection("Parse property") {
                    properties.add(it.ast(scope))
                }
            }
            declContext.declaration()?.let {
                profilerSection("Parse declaration") {
                    declarations.add(it.ast(scope))
                }
            }
            declContext.shaderDefinition()?.let { shader ->
                profilerSection("Parse shader") {
                    val signature = shader.shaderSignature()
                    val name = signature.Identifier().text
                    val parameters = ArrayList<ShaderParameter>()
                    val inputScope = Scope(scope)
                    parameters(signature.shaderParameterList(), parameters,
                            inputScope)
                    val shaderSignature = ShaderSignature(name, parameters)
                    shaders[name] = Pair(inputScope, { shaderScope ->
                        val compound = CLikeParser.compound(
                                shader.compoundStatement().blockItemList(),
                                Scope(inputScope, shaderScope))
                        ShaderFunction(shaderSignature, compound)
                    })
                }
            }
            declContext.outputsDefinition()?.let { outputs ->
                profilerSection("Parse outputs") {
                    if (outputSignature != null) {
                        throw ShaderCompileException(
                                "Multiple output declarations", outputs)
                    }
                    val parameters = ArrayList<ShaderParameter>()
                    parameters(outputs.shaderParameterList(), parameters, scope)
                    outputSignature = ShaderSignature("outputs", parameters)
                }
            }
            declContext.functionDefinition()?.let { function ->
                profilerSection("Parse function") {
                    val signature = function.functionSignature()
                    val name = signature.Identifier().text
                    val parameters = ArrayList<Parameter>()
                    val functionScope = Scope(scope)
                    parameters(signature.parameterList(), parameters,
                            functionScope)
                    val returned = signature.type().ast()
                    val precisionSpecifier = signature.precisionSpecifier()
                    val returnedPrecision: Precision
                    if (precisionSpecifier == null) {
                        returnedPrecision = Precision.mediump
                    } else {
                        returnedPrecision = precisionSpecifier.ast()
                    }
                    val functionSignature = FunctionSignature(
                            name, returned,
                            returnedPrecision,
                            *parameters.toTypedArray())
                    val compound = CLikeParser.compound(
                            function.compoundStatement().blockItemList(),
                            Scope(functionScope))
                    functions.add(Function(functionSignature, compound))
                }
            }
            current = current.translationUnit()
        }
        profilerSection("Pack program data") {
            return ShaderProgram(declarations, functions, shaders,
                    outputSignature, uniforms, properties)
        }
    }

    fun uniform(context: ScapesShaderParser.UniformDeclarationContext,
                uniforms: MutableList<Uniform?>,
                scope: Scope) {
        val uniform = context.ast(scope)
        while (uniforms.size <= uniform.id) {
            uniforms.add(null)
        }
        uniforms[uniform.id] = uniform
    }

    fun ScapesShaderParser.UniformDeclarationContext.ast(scope: Scope): Uniform {
        val id = IntegerLiteral().text.toInt()
        val name = Identifier().text
        val declarator = declarator()
        val field = declarator.declaratorField()
        if (field != null) {
            val type = field.ast()
            val variable = scope.add(name,
                    type.exported) ?: throw ShaderCompileException(
                    "Redeclaring variable: $name", Identifier())
            return Uniform(type, id, variable)
        }
        val array = declarator.declaratorArray()
        if (array != null) {
            val type = array.ast(scope)
            val variable = scope.add(name,
                    type.exported) ?: throw ShaderCompileException(
                    "Redeclaring variable: $name", Identifier())
            return Uniform(type, id, variable)
        }
        throw IllegalStateException("Invalid parse tree")
    }

    fun ScapesShaderParser.PropertyDeclarationContext.ast(scope: Scope): Property {
        val name = Identifier().text
        val declarator = declarator()
        val field = declarator.declaratorField()
        if (field != null) {
            val type = field.ast()
            val variable = scope.add(name,
                    type.exported) ?: throw ShaderCompileException(
                    "Redeclaring variable: $name", Identifier())
            return Property(type, variable)
        }
        val array = declarator.declaratorArray()
        if (array != null) {
            val type = array.ast(scope)
            val variable = scope.add(name,
                    type.exported) ?: throw ShaderCompileException(
                    "Redeclaring variable: $name", Identifier())
            return Property(type, variable)
        }
        throw IllegalStateException("Invalid parse tree")
    }

    fun statement(context: ScapesShaderParser.StatementContext,
                  scope: Scope): Statement {
        val expression = context.expressionStatement()
        if (expression != null) {
            return ExpressionStatement(expression.ast(scope))
        }
        val declaration = context.declaration()
        if (declaration != null) {
            return declaration.ast(scope)
        }
        val selection = context.selectionStatement()
        if (selection != null) {
            val ifStatement = selection.ifStatement()
            val elseStatement = selection.elseStatement() ?: return IfStatement(
                    ifStatement.ast(scope),
                    statement(selection.statement(), scope))
            return IfStatement(
                    ifStatement.ast(scope),
                    statement(selection.statement(), scope),
                    statement(elseStatement.statement(), scope))
        }
        val rangeLoop = context.rangeLoopStatement()
        if (rangeLoop != null) {
            val name = rangeLoop.Identifier().text
            val start = rangeLoop.expression(0).ast(scope)
            val end = rangeLoop.expression(1).ast(scope)
            val loopScope = Scope(scope)
            val variable = loopScope.add(name,
                    Types.Int.exported) ?: throw ShaderCompileException(
                    "Redeclaring variable: $name", rangeLoop.Identifier())
            val statement = statement(rangeLoop.statement(), loopScope)
            return LoopFixedStatement(
                    variable, start, end, statement)
        }
        return compound(context.compoundStatement().blockItemList(),
                scope)
    }

    fun ScapesShaderParser.DeclarationContext.ast(scope: Scope): Statement {
        declarationField()?.let { return declaration(it, scope) }
        declarationArray()?.let { return declaration(it, scope) }
        throw IllegalStateException("Invalid parse tree")
    }

    fun declaration(context: ScapesShaderParser.DeclarationFieldContext,
                    scope: Scope): Statement {
        val declarator = context.declaratorField()
        val type = declarator.ast()
        val initializer = context.expression()
        val name = context.Identifier().text
        val init = initializer?.ast(scope)
        val variable = scope.add(name,
                type.exported) ?: throw ShaderCompileException(
                "Redeclaring variable: $name", context)
        return DeclarationStatement(type, variable, init)
    }

    fun declaration(context: ScapesShaderParser.DeclarationArrayContext,
                    scope: Scope): Statement {
        val declarator = context.declaratorArray()
        val type = declarator.ast(scope)
        val initializer = context.initializerArray()
        val name = context.Identifier().text
        val init = initializer(initializer, scope)
        val variable = scope.add(name,
                type.exported) ?: throw ShaderCompileException(
                "Redeclaring variable: $name", context)
        return ArrayDeclarationStatement(type, type.array!!, variable, init)
    }

    fun initializer(context: ScapesShaderParser.InitializerArrayContext,
                    scope: Scope): Expression {
        val list = context.expressionList()
        if (list != null) {
            return ArrayExpression(list.ast(scope))
        }
        return context.expression().ast(scope)
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
        try {
            val streamIn = CharStreams.fromString(source)
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
        } catch (e: ParseCancellationException) {
            throw ShaderGenerateException(e)
        }
    }
}
