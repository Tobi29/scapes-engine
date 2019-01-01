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

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.tobi29.profiler.profilerSection
import org.tobi29.scapes.engine.shader.*

internal fun externalDeclaration(
    context: ScapesShaderParser.TranslationUnitContext,
    scope: Scope
): ShaderProgram {
    val declarations = ArrayList<DeclarationStatement>()
    val functions = ArrayList<CallFunction>()
    val shaders = HashMap<String, Pair<Scope, (Scope) -> ShaderFunction>>()
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
                val parameterScope = ShaderParameterScope(
                    inputScope,
                    parameters
                )
                parameterScope.parameters(signature.shaderParameterList())
                val shaderSignature = ShaderSignature(name, parameters)
                shaders[name] = Pair(inputScope, { shaderScope ->
                    val compound = Scope(inputScope, shaderScope).compound {
                        block(shader.compoundStatement().blockItemList())
                    }
                    ShaderFunction(shaderSignature, compound)
                })
            }
        }
        declContext.outputsDefinition()?.let { outputs ->
            profilerSection("Parse outputs") {
                if (outputSignature != null) {
                    throw ShaderCompileException(
                        "Multiple output declarations", outputs
                    )
                }
                val parameters = ArrayList<ShaderParameter>()
                val parameterScope = ShaderParameterScope(scope, parameters)
                parameterScope.parameters(outputs.shaderParameterList())
                outputSignature = ShaderSignature("outputs", parameters)
            }
        }
        declContext.functionDefinition()?.let { function ->
            profilerSection("Parse function") {
                val signature = function.functionSignature()
                val name = signature.Identifier().text
                val parameters = ArrayList<Parameter>()
                val functionScope = Scope(scope)
                val parameterScope = ParameterScope(functionScope, parameters)
                parameterScope.parameters(signature.parameterList())
                val returned = signature.type().ast()
                val precisionSpecifier = signature.precisionSpecifier()
                val returnedPrecision: Precision
                returnedPrecision = if (precisionSpecifier == null) {
                    Precision.mediump
                } else {
                    precisionSpecifier.ast()
                }
                val functionSignature = FunctionSignature(
                    name, returned,
                    returnedPrecision,
                    *parameters.toTypedArray()
                )
                val compound = Scope(functionScope).compound {
                    block(function.compoundStatement().blockItemList())
                }
                functions.add(CallFunction(functionSignature, compound))
            }
        }
        current = current.translationUnit()
    }
    return profilerSection("Pack program data") {
        ShaderProgram(
            declarations, functions, shaders,
            outputSignature, uniforms, properties
        )
    }
}

internal fun parser(source: String): ScapesShaderParser {
    try {
        val streamIn = CharStreams.fromString(source)
        val lexer = ScapesShaderLexer(streamIn)
        val parser = ScapesShaderParser(CommonTokenStream(lexer))
        parser.removeErrorListeners()
        parser.addErrorListener(object : BaseErrorListener() {
            override fun syntaxError(
                recognizer: Recognizer<*, *>?,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String?,
                e: RecognitionException?
            ) {
                throw ParseCancellationException(
                    "line $line:$charPositionInLine $msg"
                )
            }
        })
        return parser
    } catch (e: ParseCancellationException) {
        throw ShaderGenerateException(e)
    }
}

internal fun uniform(
    context: ScapesShaderParser.UniformDeclarationContext,
    uniforms: MutableList<Uniform?>,
    scope: Scope
) {
    val uniform = context.ast(scope)
    while (uniforms.size <= uniform.id) {
        uniforms.add(null)
    }
    uniforms[uniform.id] = uniform
}

internal fun ScapesShaderParser.UniformDeclarationContext.ast(scope: Scope): Uniform {
    val id = IntegerLiteral().text.toInt()
    val name = Identifier().text
    val declarator = declarator()
    val field = declarator.declaratorField()
    if (field != null) {
        val type = field.ast()
        val variable = scope.add(
            name, type
        ) ?: throw ShaderCompileException(
            "Redeclaring variable: $name", Identifier()
        )
        return Uniform(type, id, variable)
    }
    val array = declarator.declaratorArray()
    if (array != null) {
        val type = array.ast(scope)
        val variable = scope.add(
            name, type
        ) ?: throw ShaderCompileException(
            "Redeclaring variable: $name", Identifier()
        )
        return Uniform(type, id, variable)
    }
    throw IllegalStateException("Invalid parse tree")
}

internal fun ScapesShaderParser.PropertyDeclarationContext.ast(scope: Scope): Property {
    val name = Identifier().text
    val declarator = declarator()
    val field = declarator.declaratorField()
    if (field != null) {
        val type = field.ast()
        val variable = scope.add(
            name, type
        ) ?: throw ShaderCompileException(
            "Redeclaring variable: $name", Identifier()
        )
        return Property(type, variable)
    }
    val array = declarator.declaratorArray()
    if (array != null) {
        val type = array.ast(scope)
        val variable = scope.add(
            name, type
        ) ?: throw ShaderCompileException(
            "Redeclaring variable: $name", Identifier()
        )
        return Property(type, variable)
    }
    throw IllegalStateException("Invalid parse tree")
}

internal fun statement(
    context: ScapesShaderParser.StatementContext,
    scope: Scope
): Statement {
    val expression = context.expressionStatement()
    if (expression != null) {
        return expression.ast(scope) as? Statement
                ?: throw ShaderCompileException(
                    "Expression cannot be used as statement",
                    context.expressionStatement()
                )
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
            statement(selection.statement(), scope)
        )
        return IfStatement(
            ifStatement.ast(scope),
            statement(selection.statement(), scope),
            statement(elseStatement.statement(), scope)
        )
    }
    val rangeLoop = context.rangeLoopStatement()
    if (rangeLoop != null) {
        val name = rangeLoop.Identifier().text
        val start = rangeLoop.expression(0).ast(scope)
        val end = rangeLoop.expression(1).ast(scope)
        val loopScope = Scope(scope)
        val variable = loopScope.add(
            name, Type(Types.Int)
        ) ?: throw ShaderCompileException(
            "Redeclaring variable: $name", rangeLoop.Identifier()
        )
        val statement = statement(rangeLoop.statement(), loopScope)
        return LoopFixedStatement(
            variable, start, end, statement
        )
    }
    return scope.compound { block(context.compoundStatement().blockItemList()) }
}

internal fun ScapesShaderParser.DeclarationContext.ast(scope: Scope): DeclarationStatement {
    declarationField()?.let { return declaration(it, scope) }
    declarationArray()?.let { return declaration(it, scope) }
    throw IllegalStateException("Invalid parse tree")
}

internal fun declaration(
    context: ScapesShaderParser.DeclarationFieldContext,
    scope: Scope
): FieldDeclarationStatement {
    val declarator = context.declaratorField()
    val type = declarator.ast()
    val initializer = context.expression()
    val name = context.Identifier().text
    val init = initializer?.ast(scope)
    return scope.declaration(type, name, init)
}

internal fun declaration(
    context: ScapesShaderParser.DeclarationArrayContext,
    scope: Scope
): ArrayDeclarationStatement {
    val declarator = context.declaratorArray()
    val type = declarator.ast(scope)
    val initializer = context.initializerArray()
    val name = context.Identifier().text
    val init = initializer(initializer, scope)
    return scope.arrayDeclaration(type, type.array!!, name, init)
}

internal fun initializer(
    context: ScapesShaderParser.InitializerArrayContext,
    scope: Scope
): Expression {
    val list = context.expressionList()
    if (list != null) {
        return ArrayExpression(list.ast(scope))
    }
    return context.expression().ast(scope)
}

private tailrec fun StatementScope.block(context: ScapesShaderParser.BlockItemListContext?) {
    context ?: return
    add(statement(context.statement(), scope))
    block(context.blockItemList())
}
