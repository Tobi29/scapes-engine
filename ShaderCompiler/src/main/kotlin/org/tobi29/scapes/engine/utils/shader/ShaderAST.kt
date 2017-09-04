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

abstract class Expression {
    var location: Vector2i? = null

    abstract fun type(context: ShaderContext): TypeExported

    open fun simplify(context: ShaderContext,
                      identifiers: Map<Identifier, Expression> = emptyMap()) =
            this
}

class ShaderProgram(
        declarations: List<Statement>,
        functions: List<CallFunction>,
        shaders: Map<String, Pair<Scope, (Scope) -> ShaderFunction>>,
        val outputs: ShaderSignature?,
        uniforms: List<Uniform?>,
        properties: List<Property>) {
    val declarations = declarations.readOnly()
    val functions = functions.readOnly()
    val shaders = shaders.readOnly()
    val uniforms = uniforms.readOnly()
    val properties = properties.readOnly()
}

data class ArrayExpression(val content: List<Expression>) : Expression() {
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

data class ArrayAccessExpression(val name: Expression,
                                 val index: Expression) : Expression() {
    override fun type(context: ShaderContext) =
            name.type(context).type.exported
}

data class AssignmentExpression(val left: Expression,
                                val right: Expression) : Statement() {
    override fun type(context: ShaderContext) = left.type(context)
}

data class ConditionExpression(val type: ConditionType,
                               val left: Expression,
                               val right: Expression) : Expression() {
    override fun type(context: ShaderContext) = Types.Boolean.exported

    override fun simplify(context: ShaderContext,
                          identifiers: Map<Identifier, Expression>): Expression {
        val left = left.simplify(context, identifiers)
        val right = right.simplify(context, identifiers)
        if (left is BooleanExpression) {
            if (type == ConditionType.OR && left.value) {
                return BooleanExpression(true)
            }
            if (type == ConditionType.AND && !left.value) {
                return BooleanExpression(false)
            }
            return right
        }
        return ConditionExpression(type, left, right)
    }
}

data class FunctionExpression(val name: String,
                              val args: List<Expression>) : Statement() {
    constructor(signature: FunctionExportedSignature,
                args: List<Expression>) : this(signature.name, args)

    fun getSignature(context: ShaderContext) =
            FunctionParameterSignature(name, args.map {
                it.type(context)
            })

    override fun type(context: ShaderContext) = run {
        val signature = getSignature(context)
        context.functions[signature]?.returned ?: throw ShaderASTException(
                "Unknown function: $signature", this)
    }

    override fun simplify(context: ShaderContext,
                          identifiers: Map<Identifier, Expression>): Expression {
        val signature = getSignature(context)
        val function = context.functions[signature] ?: throw ShaderASTException(
                "Unknown function: $signature", this)
        return context.functionSimplifications[function]?.let {
            it(args.map { it.simplify(context, identifiers) })
        } ?: FunctionExpression(name,
                args.map { it.simplify(context, identifiers) })
    }
}

data class IdentifierExpression(val identifier: Identifier) : Expression() {
    override fun type(context: ShaderContext) = identifier.type

    override fun simplify(context: ShaderContext,
                          identifiers: Map<Identifier, Expression>) =
            identifiers[identifier] ?: this
}

data class BooleanExpression(val value: Boolean) : Expression() {
    override fun type(context: ShaderContext) = Types.Boolean.exported
}

data class IntegerExpression(val value: Int) : Expression() {
    override fun type(context: ShaderContext) = Types.Int.exported
}

data class DecimalExpression(val value: Double) : Expression() {
    override fun type(context: ShaderContext) = Types.Float.exported
}

data class MemberExpression(val name: String,
                            val member: Expression) : Expression() {
    override fun type(context: ShaderContext) =
            member.type(context).memberType(name) ?: throw ShaderASTException(
                    "Unknown member", this)
}

data class UnaryExpression(val type: UnaryType,
                           val value: Expression) : Statement() {
    override fun type(context: ShaderContext) = value.type(context)

    override fun simplify(context: ShaderContext,
                          identifiers: Map<Identifier, Expression>) =
            value.simplify(context, identifiers).let { value ->
                when (type) {
                    UnaryType.NOT -> !value
                    else -> UnaryExpression(type, value)
                }
            }
}

data class TernaryExpression(val condition: Expression,
                             val expression: Expression,
                             val expressionElse: Expression) : Expression() {
    override fun type(context: ShaderContext) =
            expression.type(context) common expressionElse.type(context)
                    ?: throw ShaderASTException("Different result types", this)
}

object VoidStatement : Statement() {
    override fun type(context: ShaderContext) = Types.Void.exported
}

data class IfStatement(val condition: Expression,
                       val statement: Statement,
                       val statementElse: Statement? = null) : Statement() {
    override fun type(context: ShaderContext) = Types.Void.exported
}

data class LoopFixedStatement(val index: Identifier,
                              val start: Expression,
                              val end: Expression,
                              val statement: Statement) : Statement() {
    override fun type(context: ShaderContext) = Types.Void.exported
}

data class Parameter(val type: Type,
                     val identifier: Identifier)

data class ShaderFunction(val signature: ShaderSignature,
                          val compound: CompoundStatement)

class ShaderParameter(val type: Type,
                      val id: Int,
                      val identifier: Identifier,
                      val available: Expression)

class ShaderSignature(val name: String,
                      parameters: List<ShaderParameter>) {
    val parameters = parameters.readOnly()
}

abstract class Statement : Expression()

data class StatementBlock(val statements: List<Statement>) : Statement() {
    override fun type(context: ShaderContext) = Types.Void.exported
}

data class DeclarationStatement(val type: Type,
                                val identifier: Identifier,
                                val initializer: Expression? = null) : Statement() {
    override fun type(context: ShaderContext) = Types.Void.exported
}

data class ArrayDeclarationStatement(val type: Type,
                                     val length: Expression,
                                     val identifier: Identifier,
                                     val initializer: Expression? = null) : Statement() {
    override fun type(context: ShaderContext) = Types.Void.exported
}

data class CompoundStatement(val block: StatementBlock) : Statement() {
    override fun type(context: ShaderContext) = Types.Void.exported
}

class CallFunction(val signature: FunctionSignature,
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

    private val hash = run {
        var result = name.hashCode()
        result = 31 * result + returned.hashCode()
        result = 31 * result + parameters.hashCode()
        result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FunctionExportedSignature) return false

        if (hash != other.hash) return false
        if (name != other.name) return false
        if (returned != other.returned) return false
        if (parameters != other.parameters) return false

        return true
    }

    override fun hashCode(): Int = hash

    val call by lazy { FunctionParameterSignature(name, parameters) }
}

data class FunctionParameterSignature(val name: String,
                                      val parameters: List<TypeExported>) {
    private val hash = run {
        var result = name.hashCode()
        result = 31 * result + parameters.hashCode()
        result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FunctionParameterSignature) return false

        if (hash != other.hash) return false
        if (name != other.name) return false
        if (parameters != other.parameters) return false

        return true
    }

    override fun hashCode(): Int = hash
}

class Type(val type: Types,
           val array: Expression? = null,
           val constant: Boolean = false,
           val precision: Precision = Precision.mediump) {
    constructor(type: Types,
                constant: Boolean = false,
                precision: Precision = Precision.mediump
    ) : this(type, null, constant, precision)

    constructor(type: Types,
                precision: Precision = Precision.mediump
    ) : this(type, false, precision)

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
    AND
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

inline fun arithmeticOperation(
        a: Expression,
        b: Expression,
        functionName: String,
        combineInteger: (Int, Int) -> Int,
        combineDecimal: (Double, Double) -> Double) =
        if (a is IntegerExpression && b is IntegerExpression) {
            IntegerExpression(combineInteger(a.value, b.value))
        } else if (a is DecimalExpression && b is DecimalExpression) {
            DecimalExpression(combineDecimal(a.value, b.value))
        } else {
            FunctionExpression(functionName, listOf(a, b))
        }

inline fun logicOperation(
        a: Expression,
        b: Expression,
        functionName: String,
        combineBoolean: (Boolean, Boolean) -> Boolean,
        combineInteger: (Int, Int) -> Int) =
        if (a is BooleanExpression && b is BooleanExpression) {
            BooleanExpression(combineBoolean(a.value, b.value))
        } else if (a is IntegerExpression && b is IntegerExpression) {
            IntegerExpression(combineInteger(a.value, b.value))
        } else {
            FunctionExpression(functionName, listOf(a, b))
        }

inline fun comparisonOperation(
        a: Expression,
        b: Expression,
        functionName: String,
        combineBoolean: (Boolean, Boolean) -> Boolean,
        combineInteger: (Int, Int) -> Boolean,
        combineDecimal: (Double, Double) -> Boolean) =
        if (a is BooleanExpression && b is BooleanExpression) {
            BooleanExpression(combineBoolean(a.value, b.value))
        } else if (a is IntegerExpression && b is IntegerExpression) {
            BooleanExpression(combineInteger(a.value, b.value))
        } else if (a is DecimalExpression && b is DecimalExpression) {
            BooleanExpression(combineDecimal(a.value, b.value))
        } else {
            FunctionExpression(functionName, listOf(a, b))
        }
