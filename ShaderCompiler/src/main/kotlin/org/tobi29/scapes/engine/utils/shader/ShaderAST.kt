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

import org.tobi29.scapes.engine.utils.math.vector.Vector2i
import org.tobi29.scapes.engine.utils.readOnly
import java.math.BigDecimal
import java.math.BigInteger

abstract class Expression {
    var location: Vector2i? = null

    abstract fun type(context: ShaderContext): TypeExported

    open fun simplify(context: ShaderContext,
                      identifiers: Map<Identifier, Expression> = emptyMap()) =
            this
}

class ShaderProgram(
        declarations: List<Statement>,
        functions: List<Function>,
        shaders: Map <String, Pair<Scope, (Scope) -> ShaderFunction>>,
        val outputs: ShaderSignature?,
        uniforms: List<Uniform?>,
        properties: List<Property>) {
    val declarations = declarations.readOnly()
    val functions = functions.readOnly()
    val shaders = shaders.readOnly()
    val uniforms = uniforms.readOnly()
    val properties = properties.readOnly()
}

class ArrayExpression(content: List<Expression>) : Expression() {
    val content = content.readOnly()

    constructor(array: IntArray) : this(array.map { IntegerExpression(it) })
    constructor(array: DoubleArray) : this(array.map { DecimalExpression(it) })

    override fun type(context: ShaderContext) =
            content.asSequence().map {
                // This fails to compile without the cast
                @Suppress("USELESS_CAST")
                it.type(context) as TypeExported?
            }.reduce { a, b -> a common b }?.type?.exportedArray
                    ?: throw ShaderASTException("Array contains multiple types",
                    this)
}

class ArrayAccessExpression(val name: Expression,
                            val index: Expression) : Expression() {
    override fun type(context: ShaderContext) =
            name.type(context).type.exported
}

class AssignmentExpression(val left: Expression,
                           val right: Expression) : Expression() {
    override fun type(context: ShaderContext) = left.type(context)
}

class ConditionExpression(val type: ConditionType,
                          val left: Expression,
                          val right: Expression) : Expression() {
    override fun type(context: ShaderContext) = Types.Boolean.exported
}

class FunctionExpression(val name: String,
                         val args: List<Expression>) : Expression() {
    constructor(signature: FunctionExportedSignature,
                args: List<Expression>) : this(signature.name, args)

    override fun type(context: ShaderContext) =
            context.functions[FunctionParameterSignature(name, args.map {
                it.type(context)
            })]?.returned ?: throw ShaderASTException("Unknown function", this)
}

class IdentifierExpression(val identifier: Identifier) : Expression() {
    override fun type(context: ShaderContext) = identifier.type

    override fun simplify(context: ShaderContext,
                          identifiers: Map<Identifier, Expression>) =
            identifiers[identifier] ?: this
}

class BooleanExpression(val value: Boolean) : Expression() {
    override fun type(context: ShaderContext) = Types.Boolean.exported
}

class IntegerExpression(val value: BigInteger) : Expression() {
    constructor(value: Int) : this(BigInteger(value.toString()))

    override fun type(context: ShaderContext) = Types.Int.exported
}

class DecimalExpression(val value: BigDecimal) : Expression() {
    constructor(value: Double) : this(BigDecimal(value.toString()))

    override fun type(context: ShaderContext) = Types.Float.exported
}

class MemberExpression(val name: String,
                       val member: Expression) : Expression() {
    override fun type(context: ShaderContext) =
            member.type(context).memberType(name) ?: throw ShaderASTException(
                    "Unknown member", this)
}

class UnaryExpression(val type: UnaryType,
                      val value: Expression) : Expression() {
    override fun type(context: ShaderContext) = value.type(context)

    override fun simplify(context: ShaderContext,
                          identifiers: Map<Identifier, Expression>) =
            value.simplify(context, identifiers).also { value ->
                when (type) {
                    UnaryType.NOT -> when (value) {
                        is BooleanExpression -> BooleanExpression(!value.value)
                        else -> UnaryExpression(type, value)
                    }
                    else -> UnaryExpression(type, value)
                }
            }
}

class TernaryExpression(val condition: Expression,
                        val expression: Expression,
                        val expressionElse: Expression) : Expression() {
    override fun type(context: ShaderContext) =
            expression.type(context) common expressionElse.type(context)
                    ?: throw ShaderASTException("Different result types", this)
}

class VoidExpression : Expression() {
    override fun type(context: ShaderContext) = Types.Void.exported
}

class IfStatement(val condition: Expression,
                  val statement: Statement,
                  val statementElse: Statement? = null) : Statement()

class LoopFixedStatement(val index: Identifier,
                         val start: Expression,
                         val end: Expression,
                         val statement: Statement) : Statement()

class Parameter(val type: Type,
                val identifier: Identifier)

class ShaderFunction(val signature: ShaderSignature,
                     val compound: CompoundStatement)

class ShaderParameter(val type: Type,
                      val id: Int,
                      val identifier: Identifier,
                      val available: Expression)

class ShaderSignature(val name: String,
                      parameters: List<ShaderParameter>) {
    val parameters = parameters.readOnly()
}

open class Statement : Expression() {
    override fun type(context: ShaderContext) = Types.Void.exported
}

class StatementBlock(statements: List<Statement>) : Statement() {
    val statements = statements.readOnly()
}

class DeclarationStatement(val type: Type,
                           val identifier: Identifier,
                           val initializer: Expression? = null) : Statement()

class ArrayDeclarationStatement(val type: Type,
                                val length: Expression,
                                val identifier: Identifier,
                                val initializer: Expression? = null) : Statement()

class CompoundStatement(val block: StatementBlock) : Statement()

class ExpressionStatement(val expression: Expression) : Statement()

class Function(val signature: FunctionSignature,
               val compound: CompoundStatement)

class FunctionSignature(val name: String,
                        val returned: TypeExported,
                        val returnedPrecision: Precision,
                        vararg val parameters: Parameter) {
    val exported by lazy {
        FunctionExportedSignature(name, returned,
                exportedParameters(parameters))
    }

    companion object {
        private fun exportedParameters(parameters: Array<out Parameter>) =
                parameters.asSequence().map {
                    it.type.type.exported(it.type.array != null)
                }.toList()
    }
}

data class FunctionExportedSignature(val name: String,
                                     val returned: TypeExported,
                                     val parameters: List<TypeExported>) {
    constructor(name: String,
                returned: TypeExported,
                vararg parameters: TypeExported) : this(name, returned,
            listOf(*parameters))

    val call by lazy { FunctionParameterSignature(name, parameters) }
}

data class FunctionParameterSignature(val name: String,
                                      val parameters: List<TypeExported>)

class Type(val type: Types,
           val array: Expression? = null,
           val constant: Boolean,
           val precision: Precision) {
    constructor(type: Types,
                constant: Boolean,
                precision: Precision) : this(type, null, constant, precision)

    val exported = type.exported(array != null)
}

data class TypeExported(val type: Types,
                        val array: Boolean = false)

class Uniform(val type: Type,
              val id: Int,
              val identifier: Identifier)

class Property(val type: Type,
               val identifier: Identifier)

enum class ConditionType {
    OR,
    AND,
    EQUALS,
    NOT_EQUALS,
    LESS,
    GREATER,
    LESS_EQUAL,
    GREATER_EQUAL
}

enum class Precision {
    lowp,
    mediump,
    highp
}

enum class Types {
    Void,
    Float,
    Boolean,
    Int,
    Vector2,
    Vector2b,
    Vector2i,
    Matrix2,
    Vector3,
    Vector3b,
    Vector3i,
    Matrix3,
    Vector4,
    Vector4b,
    Vector4i,
    Matrix4,
    Texture2;

    val exported = TypeExported(this)
    val exportedArray = TypeExported(this, true)

    fun exported(array: kotlin.Boolean) = if (array) exportedArray else exported
}

tailrec fun Expression.isLValue(): Boolean = when (this) {
    is IdentifierExpression -> true
    is MemberExpression -> member.isLValue()
    else -> false
}

infix fun TypeExported?.common(other: TypeExported?): TypeExported? {
    return if (this == other) this else null
}

enum class UnaryType {
    INCREMENT_GET,
    DECREMENT_GET,
    GET_INCREMENT,
    GET_DECREMENT,
    POSITIVE,
    NEGATIVE,
    BIT_NOT,
    NOT
}