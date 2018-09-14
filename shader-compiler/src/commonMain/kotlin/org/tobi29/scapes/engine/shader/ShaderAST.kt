/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.scapes.engine.shader

import org.tobi29.stdex.readOnly
import org.tobi29.io.tag.*

abstract class Expression : TagMapWrite {
    var location: SourceLocation? = null

    abstract val id: String

    abstract fun type(context: ShaderContext): TypeExported

    open fun simplify(context: ShaderContext,
                      identifiers: Map<Identifier, Expression> = emptyMap()) =
            this

    override fun write(map: ReadWriteTagMap) {
        map["ID"] = id.toTag()
        location?.let { map["Location"] = it.toTag() }
    }
}

fun MutableTag.toExpression(): Expression? {
    val map = toMap() ?: return null
    val id = map["ID"]?.toString() ?: return null
    return when (id) {
        "ArrayExpression" -> toArrayExpression()
        "ArrayAccessExpression" -> toArrayAccessExpression()
        "ConditionExpression" -> toConditionExpression()
        "IdentifierExpression" -> toIdentifierExpression()
        "BooleanExpression" -> toBooleanExpression()
        "IntegerExpression" -> toIntegerExpression()
        "DecimalExpression" -> toDecimalExpression()
        "MemberExpression" -> toMemberExpression()
        "TernaryExpression" -> toTernaryExpression()
        else -> toStatement()
    }
}

fun <E : Expression> E.attachLocation(map: TagMap): E = apply {
    map["Location"]?.toSourceLocation()?.let { location = it }
}

class ShaderProgram(
        declarations: List<DeclarationStatement>,
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
    override val id = "ArrayExpression"

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

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Content"] = content.asSequence().map { it.toTag() }.toTag()
    }
}

fun MutableTag.toArrayExpression(): ArrayExpression? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "ArrayExpression") return null
    val content = map["Content"]?.toList()?.map {
        it.toExpression() ?: return null
    } ?: return null
    return ArrayExpression(content).attachLocation(map)
}

data class ArrayAccessExpression(val name: Expression,
                                 val index: Expression) : Expression() {
    override val id = "ArrayAccessExpression"

    override fun type(context: ShaderContext) =
            name.type(context).type.exported

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Name"] = name.toTag()
        map["Index"] = index.toTag()
    }
}

fun MutableTag.toArrayAccessExpression(): ArrayAccessExpression? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "ArrayAccessExpression") return null
    val name = map["Name"]?.toExpression() ?: return null
    val index = map["Index"]?.toExpression() ?: return null
    return ArrayAccessExpression(name, index).attachLocation(map)
}

data class AssignmentStatement(val left: Expression,
                               val right: Expression) : Statement() {
    override val id = "AssignmentStatement"

    override fun type(context: ShaderContext) = left.type(context)

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Left"] = left.toTag()
        map["Right"] = right.toTag()
    }
}

fun MutableTag.toAssignmentStatement(): AssignmentStatement? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "AssignmentStatement") return null
    val left = map["Left"]?.toExpression() ?: return null
    val right = map["Right"]?.toExpression() ?: return null
    return AssignmentStatement(left, right).attachLocation(map)
}

data class ConditionExpression(val type: ConditionType,
                               val left: Expression,
                               val right: Expression) : Expression() {
    override val id = "ConditionExpression"

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

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Type"] = type.toTag()
        map["Left"] = left.toTag()
        map["Right"] = right.toTag()
    }
}

fun MutableTag.toConditionExpression(): ConditionExpression? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "ConditionExpression") return null
    val type = map["Type"]?.toConditionType() ?: return null
    val left = map["Left"]?.toExpression() ?: return null
    val right = map["Right"]?.toExpression() ?: return null
    return ConditionExpression(type, left, right).attachLocation(map)
}

data class FunctionStatement(val name: String,
                             val arguments: List<Expression>) : Statement() {
    override val id = "FunctionStatement"

    constructor(signature: FunctionExportedSignature,
                args: List<Expression>) : this(signature.name, args)

    fun getSignature(context: ShaderContext) =
            FunctionParameterSignature(name, arguments.map {
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
            it(arguments.map { it.simplify(context, identifiers) })
        } ?: FunctionStatement(name,
                arguments.map { it.simplify(context, identifiers) })
    }

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Name"] = name.toTag()
        map["Arguments"] = arguments.asSequence().map { it.toTag() }.toTag()
    }
}

fun MutableTag.toFunctionStatement(): FunctionStatement? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "FunctionStatement") return null
    val name = map["Name"]?.toString() ?: return null
    val arguments = map["Arguments"]?.toList()?.map {
        it.toExpression() ?: return null
    } ?: return null
    return FunctionStatement(name, arguments).attachLocation(map)
}

data class IdentifierExpression(val identifier: Identifier) : Expression() {
    override val id = "IdentifierExpression"

    override fun type(context: ShaderContext) = identifier.type

    override fun simplify(context: ShaderContext,
                          identifiers: Map<Identifier, Expression>) =
            identifiers[identifier] ?: this

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Identifier"] = identifier.toTag()
    }
}

fun MutableTag.toIdentifierExpression(): IdentifierExpression? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "IdentifierExpression") return null
    val identifier = map["Identifier"]?.toIdentifier() ?: return null
    return IdentifierExpression(identifier).attachLocation(map)
}

data class Identifier(val name: String,
                      val type: TypeExported) : TagMapWrite {
    override fun write(map: ReadWriteTagMap) {
        map["Name"] = name.toTag()
        map["Type"] = type.toTag()
    }
}

fun MutableTag.toIdentifier(): Identifier? {
    val map = toMap() ?: return null
    val name = map["Name"]?.toString() ?: return null
    val type = map["Type"]?.toTypeExported() ?: return null
    return Identifier(name, type)
}

data class BooleanExpression(val value: Boolean) : Expression() {
    override val id = "BooleanExpression"

    override fun type(context: ShaderContext) = Types.Boolean.exported

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Value"] = value.toTag()
    }
}

fun MutableTag.toBooleanExpression(): BooleanExpression? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "BooleanExpression") return null
    val value = map["Value"]?.toBoolean() ?: return null
    return BooleanExpression(value).attachLocation(map)
}

data class IntegerExpression(val value: Int) : Expression() {
    override val id = "IntegerExpression"

    override fun type(context: ShaderContext) = Types.Int.exported

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Value"] = value.toTag()
    }
}

fun MutableTag.toIntegerExpression(): IntegerExpression? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "IntegerExpression") return null
    val value = map["Value"]?.toInt() ?: return null
    return IntegerExpression(value).attachLocation(map)
}

data class DecimalExpression(val value: Double) : Expression() {
    override val id = "DecimalExpression"

    override fun type(context: ShaderContext) = Types.Float.exported

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Value"] = value.toTag()
    }
}

fun MutableTag.toDecimalExpression(): DecimalExpression? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "DecimalExpression") return null
    val value = map["Value"]?.toDouble() ?: return null
    return DecimalExpression(value).attachLocation(map)
}

data class MemberExpression(val name: String,
                            val member: Expression) : Expression() {
    override val id = "MemberExpression"

    override fun type(context: ShaderContext) =
            member.type(context).memberType(name) ?: throw ShaderASTException(
                    "Unknown member", this)

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Name"] = name.toTag()
        map["Member"] = member.toTag()
    }
}

fun MutableTag.toMemberExpression(): MemberExpression? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "MemberExpression") return null
    val name = map["Name"]?.toString() ?: return null
    val member = map["Member"]?.toExpression() ?: return null
    return MemberExpression(name, member).attachLocation(map)
}

data class UnaryStatement(val type: UnaryType,
                          val value: Expression) : Statement() {
    override val id = "UnaryStatement"

    override fun type(context: ShaderContext) = value.type(context)

    override fun simplify(context: ShaderContext,
                          identifiers: Map<Identifier, Expression>) =
            value.simplify(context, identifiers).let { value ->
                when (type) {
                    UnaryType.NOT -> !value
                    else -> UnaryStatement(type, value)
                }
            }

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Type"] = type.toTag()
        map["Value"] = value.toTag()
    }
}

fun MutableTag.toUnaryStatement(): UnaryStatement? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "UnaryStatement") return null
    val type = map["Type"]?.toUnaryType() ?: return null
    val value = map["Value"]?.toExpression() ?: return null
    return UnaryStatement(type, value).attachLocation(map)
}

data class TernaryExpression(val condition: Expression,
                             val expression: Expression,
                             val expressionElse: Expression) : Expression() {
    override val id = "TernaryExpression"

    override fun type(context: ShaderContext) =
            expression.type(context) common expressionElse.type(context)
                    ?: throw ShaderASTException("Different result types", this)

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Condition"] = condition.toTag()
        map["Expression"] = expression.toTag()
        map["ExpressionElse"] = expression.toTag()
    }
}

fun MutableTag.toTernaryExpression(): TernaryExpression? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "TernaryExpression") return null
    val condition = map["Condition"]?.toExpression() ?: return null
    val expression = map["Expression"]?.toExpression() ?: return null
    val expressionElse = map["ExpressionElse"]?.toExpression() ?: return null
    return TernaryExpression(condition, expression,
            expressionElse).attachLocation(map)
}

class VoidStatement : Statement() {
    override val id = "VoidStatement"

    override fun type(context: ShaderContext) = Types.Void.exported
}

fun MutableTag.toVoidStatement(): VoidStatement? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "VoidStatement") return null
    return VoidStatement().attachLocation(map)
}

data class IfStatement(val condition: Expression,
                       val statement: Statement,
                       val statementElse: Statement? = null) : Statement() {
    override val id = "IfStatement"

    override fun type(context: ShaderContext) = Types.Void.exported

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Condition"] = condition.toTag()
        map["Statement"] = statement.toTag()
        statementElse?.let { map["StatementElse"] = it.toTag() }
    }
}

fun MutableTag.toIfStatement(): IfStatement? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "IfStatement") return null
    val condition = map["Condition"]?.toExpression() ?: return null
    val statement = map["Statement"]?.toStatement() ?: return null
    val statementElse = map["StatementElse"]?.toStatement()
    return IfStatement(condition, statement, statementElse).attachLocation(map)
}

data class LoopFixedStatement(val index: Identifier,
                              val start: Expression,
                              val end: Expression,
                              val statement: Statement) : Statement() {
    override val id = "LoopFixedStatement"

    override fun type(context: ShaderContext) = Types.Void.exported

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Index"] = index.toTag()
        map["Start"] = start.toTag()
        map["End"] = end.toTag()
        map["Statement"] = statement.toTag()
    }
}

fun MutableTag.toLoopFixedStatement(): LoopFixedStatement? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "LoopFixedStatement") return null
    val index = map["Index"]?.toIdentifier() ?: return null
    val start = map["Start"]?.toExpression() ?: return null
    val end = map["End"]?.toExpression() ?: return null
    val statement = map["Statement"]?.toStatement() ?: return null
    return LoopFixedStatement(index, start, end, statement).attachLocation(map)
}

data class Parameter(val type: Type,
                     val identifier: Identifier) : TagMapWrite {
    override fun write(map: ReadWriteTagMap) {
        map["Type"] = type.toTag()
        map["Identifier"] = identifier.toTag()
    }
}

fun MutableTag.toParameter(): Parameter? {
    val map = toMap() ?: return null
    val type = map["Type"]?.toType() ?: return null
    val identifier = map["Identifier"]?.toIdentifier() ?: return null
    return Parameter(type, identifier)
}

data class ShaderFunction(val signature: ShaderSignature,
                          val compound: CompoundStatement) : TagMapWrite {
    override fun write(map: ReadWriteTagMap) {
        map["Signature"] = signature.toTag()
        map["Compound"] = compound.toTag()
    }
}

fun MutableTag.toShaderFunction(): ShaderFunction? {
    val map = toMap() ?: return null
    val signature = map["Signature"]?.toShaderSignature() ?: return null
    val compound = map["Compound"]?.toCompoundStatement() ?: return null
    return ShaderFunction(signature, compound)
}

data class ShaderParameter(val type: Type,
                           val id: Int,
                           val identifier: Identifier,
                           val available: Expression) : TagMapWrite {
    override fun write(map: ReadWriteTagMap) {
        map["Type"] = type.toTag()
        map["ID"] = id.toTag()
        map["Identifier"] = identifier.toTag()
        map["Available"] = available.toTag()
    }
}

fun MutableTag.toShaderParameter(): ShaderParameter? {
    val map = toMap() ?: return null
    val type = map["Type"]?.toType() ?: return null
    val id = map["ID"]?.toInt() ?: return null
    val identifier = map["Identifier"]?.toIdentifier() ?: return null
    val available = map["Available"]?.toExpression() ?: return null
    return ShaderParameter(type, id, identifier, available)
}

data class ShaderSignature(val name: String,
                           val parameters: List<ShaderParameter>) : TagMapWrite {
    override fun write(map: ReadWriteTagMap) {
        map["Name"] = name.toTag()
        map["Parameters"] = parameters.asSequence().map { it.toTag() }.toTag()
    }
}

fun MutableTag.toShaderSignature(): ShaderSignature? {
    val map = toMap() ?: return null
    val name = map["Name"]?.toString() ?: return null
    val parameters = map["Parameters"]?.toList()?.map {
        it.toShaderParameter() ?: return null
    } ?: return null
    return ShaderSignature(name, parameters)
}

abstract class Statement : Expression()

fun MutableTag.toStatement(): Statement? {
    val map = toMap() ?: return null
    val id = map["ID"]?.toString() ?: return null
    return when (id) {
        "AssignmentStatement" -> toAssignmentStatement()
        "FunctionStatement" -> toFunctionStatement()
        "UnaryStatement" -> toUnaryStatement()
        "VoidStatement" -> toVoidStatement()
        "IfStatement" -> toIfStatement()
        "LoopFixedStatement" -> toLoopFixedStatement()
        "StatementBlock" -> toStatementBlock()
        "CompoundStatement" -> toCompoundStatement()
        else -> toDeclarationStatement()
    }
}

data class StatementBlock(val statements: List<Statement>) : Statement() {
    override val id = "StatementBlock"

    override fun type(context: ShaderContext) = Types.Void.exported

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Statements"] = statements.asSequence().map { it.toTag() }.toTag()
    }
}

fun MutableTag.toStatementBlock(): StatementBlock? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "StatementBlock") return null
    val statements = map["Statements"]?.toList()?.map {
        it.toStatement() ?: return null
    } ?: return null
    return StatementBlock(statements).attachLocation(map)
}

sealed class DeclarationStatement : Statement() {
    abstract val type: Type
    abstract val identifier: Identifier
    abstract val initializer: Expression?
}

fun MutableTag.toDeclarationStatement(): DeclarationStatement? {
    val map = toMap() ?: return null
    val id = map["ID"]?.toString() ?: return null
    return when (id) {
        "FieldDeclarationStatement" -> toFieldDeclarationStatement()
        "ArrayDeclarationStatement" -> toArrayDeclarationStatement()
        else -> null
    }
}

data class FieldDeclarationStatement(
        override val type: Type,
        override val identifier: Identifier,
        override val initializer: Expression? = null
) : DeclarationStatement() {
    override val id = "FieldDeclarationStatement"

    override fun type(context: ShaderContext) = Types.Void.exported

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Type"] = type.toTag()
        map["Identifier"] = identifier.toTag()
        initializer?.let { map["Initializer"] = it.toTag() }
    }
}

fun MutableTag.toFieldDeclarationStatement(): FieldDeclarationStatement? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "FieldDeclarationStatement") return null
    val type = map["Type"]?.toType() ?: return null
    val identifier = map["Identifier"]?.toIdentifier() ?: return null
    val initializer = map["Initializer"]?.toExpression()
    return FieldDeclarationStatement(type, identifier,
            initializer).attachLocation(
            map)
}

data class ArrayDeclarationStatement(
        override val type: Type,
        val length: Expression,
        override val identifier: Identifier,
        override val initializer: Expression? = null
) : DeclarationStatement() {
    override val id = "ArrayDeclarationStatement"

    override fun type(context: ShaderContext) = Types.Void.exported

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Type"] = type.toTag()
        map["Length"] = length.toTag()
        map["Identifier"] = identifier.toTag()
        initializer?.let { map["Initializer"] = it.toTag() }
    }
}

fun MutableTag.toArrayDeclarationStatement(): ArrayDeclarationStatement? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "ArrayDeclarationStatement") return null
    val type = map["Type"]?.toType() ?: return null
    val length = map["Length"]?.toExpression() ?: return null
    val identifier = map["Identifier"]?.toIdentifier() ?: return null
    val initializer = map["Initializer"]?.toExpression()
    return ArrayDeclarationStatement(type, length, identifier,
            initializer).attachLocation(map)
}

data class CompoundStatement(val block: StatementBlock) : Statement() {
    override val id = "CompoundStatement"

    override fun type(context: ShaderContext) = Types.Void.exported

    override fun write(map: ReadWriteTagMap) {
        super.write(map)
        map["Block"] = block.toTag()
    }
}

fun MutableTag.toCompoundStatement(): CompoundStatement? {
    val map = toMap() ?: return null
    if (map["ID"]?.toString() != "CompoundStatement") return null
    val block = map["Block"]?.toStatementBlock() ?: return null
    return CompoundStatement(block).attachLocation(map)
}

data class CallFunction(val signature: FunctionSignature,
                        val compound: CompoundStatement) : TagMapWrite {
    override fun write(map: ReadWriteTagMap) {
        map["Signature"] = signature.toTag()
        map["Compound"] = compound.toTag()
    }
}

fun MutableTag.toCallFunction(): CallFunction? {
    val map = toMap() ?: return null
    val signature = map["Signature"]?.toFunctionSignature() ?: return null
    val compound = map["Compound"]?.toCompoundStatement() ?: return null
    return CallFunction(signature, compound)
}

class FunctionSignature(val name: String,
                        val returned: TypeExported,
                        val returnedPrecision: Precision,
                        val parameters: List<Parameter>) : TagMapWrite {
    val exported by lazy {
        FunctionExportedSignature(name, returned,
                exportedParameters(parameters))
    }

    constructor(name: String,
                returned: TypeExported,
                returnedPrecision: Precision,
                vararg parameters: Parameter
    ) : this(name, returned, returnedPrecision, parameters.toList())

    override fun write(map: ReadWriteTagMap) {
        map["Name"] = name.toTag()
        map["Returned"] = returned.toTag()
        map["ReturnedPrecision"] = returnedPrecision.toTag()
        map["Parameters"] = parameters.asSequence().map { it.toTag() }.toTag()
    }

    companion object {
        private fun exportedParameters(parameters: List<Parameter>) =
                parameters.asSequence().map {
                    it.type.type.exported(it.type.array != null)
                }.toList()
    }
}

fun MutableTag.toFunctionSignature(): FunctionSignature? {
    val map = toMap() ?: return null
    val name = map["Name"]?.toString() ?: return null
    val returned = map["Returned"]?.toTypeExported() ?: return null
    val returnedPrecision = map["ReturnedPrecision"]?.toPrecision() ?: return null
    val parameters = map["Parameters"]?.toList()?.map {
        it.toParameter() ?: return null
    } ?: return null
    return FunctionSignature(name, returned, returnedPrecision, parameters)
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

data class Type(val type: Types,
                val array: Expression? = null,
                val constant: Boolean = false,
                val precision: Precision = Precision.mediump) : TagMapWrite {
    constructor(type: Types,
                constant: Boolean = false,
                precision: Precision = Precision.mediump
    ) : this(type, null, constant, precision)

    constructor(type: Types,
                precision: Precision = Precision.mediump
    ) : this(type, false, precision)

    val exported = type.exported(array != null)

    override fun write(map: ReadWriteTagMap) {
        map["Type"] = type.toTag()
        array?.let { map["Array"] = it.toTag() }
        map["Constant"] = constant.toTag()
        map["Precision"] = precision.toTag()
    }
}

fun MutableTag.toType(): Type? {
    val map = toMap() ?: return null
    val type = map["Type"]?.toTypes() ?: return null
    val array = map["Array"]?.toExpression()
    val constant = map["Constant"]?.toBoolean() ?: return null
    val precision = map["Precision"]?.toPrecision() ?: return null
    return Type(type, array, constant, precision)
}

data class TypeExported(val type: Types,
                        val array: Boolean = false) : TagMapWrite {
    override fun write(map: ReadWriteTagMap) {
        map["Type"] = type.toTag()
        map["Array"] = array.toTag()
    }
}

fun MutableTag.toTypeExported(): TypeExported? {
    val map = toMap() ?: return null
    val type = map["Type"]?.toTypes() ?: return null
    val array = map["Array"]?.toBoolean() ?: return null
    return TypeExported(type, array)
}

data class Uniform(val type: Type,
                   val id: Int,
                   val identifier: Identifier) : TagMapWrite {
    override fun write(map: ReadWriteTagMap) {
        map["Type"] = type.toTag()
        map["ID"] = id.toTag()
        map["Identifier"] = identifier.toTag()
    }
}

fun MutableTag.toUniform(): Uniform? {
    val map = toMap() ?: return null
    val type = map["Type"]?.toType() ?: return null
    val id = map["ID"]?.toInt() ?: return null
    val identifier = map["Identifier"]?.toIdentifier() ?: return null
    return Uniform(type, id, identifier)
}

data class Property(val type: Type,
                    val identifier: Identifier) : TagMapWrite {
    override fun write(map: ReadWriteTagMap) {
        map["Type"] = type.toTag()
        map["Identifier"] = identifier.toTag()
    }
}

fun MutableTag.toProperty(): Property? {
    val map = toMap() ?: return null
    val type = map["Type"]?.toType() ?: return null
    val identifier = map["Identifier"]?.toIdentifier() ?: return null
    return Property(type, identifier)
}

enum class ConditionType : TagWrite {
    OR,
    AND;

    override fun toTag(): Tag = toString().toTag()
}

fun MutableTag.toConditionType(): ConditionType? = try {
    ConditionType.valueOf(toString())
} catch (e: IllegalArgumentException) {
    null
}

enum class Precision : TagWrite {
    lowp,
    mediump,
    highp;

    override fun toTag(): Tag = toString().toTag()
}

fun MutableTag.toPrecision(): Precision? = try {
    Precision.valueOf(toString())
} catch (e: IllegalArgumentException) {
    null
}

enum class Types : TagWrite {
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

    override fun toTag(): Tag = toString().toTag()
}

fun MutableTag.toTypes(): Types? = try {
    Types.valueOf(toString())
} catch (e: IllegalArgumentException) {
    null
}

tailrec fun Expression.isLValue(): Boolean = when (this) {
    is IdentifierExpression -> true
    is MemberExpression -> member.isLValue()
    else -> false
}

infix fun TypeExported?.common(other: TypeExported?): TypeExported? {
    return if (this == other) this else null
}

enum class UnaryType : TagWrite {
    INCREMENT_GET,
    DECREMENT_GET,
    GET_INCREMENT,
    GET_DECREMENT,
    POSITIVE,
    NEGATIVE,
    BIT_NOT,
    NOT;

    override fun toTag(): Tag = toString().toTag()
}

fun MutableTag.toUnaryType(): UnaryType? = try {
    UnaryType.valueOf(toString())
} catch (e: IllegalArgumentException) {
    null
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
            FunctionStatement(functionName, listOf(a, b))
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
            FunctionStatement(functionName, listOf(a, b))
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
            FunctionStatement(functionName, listOf(a, b))
        }

data class SourceLocation(
        val line: Int,
        val column: Int
) : TagMapWrite {
    override fun toString() = "$line:$column"

    override fun write(map: ReadWriteTagMap) {
        map["Line"] = line.toTag()
        map["Column"] = column.toTag()
    }
}

fun MutableTag.toSourceLocation(): SourceLocation? {
    val map = toMap() ?: return null
    val line = map["Line"]?.toInt() ?: return null
    val column = map["Column"]?.toInt() ?: return null
    return SourceLocation(line, column)
}
