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

operator fun Expression.unaryPlus() = when (this) {
    is IntegerExpression -> this
    is DecimalExpression -> this
    else -> UnaryStatement(UnaryType.POSITIVE, this)
}

operator fun Expression.unaryMinus() = when (this) {
    is IntegerExpression -> IntegerExpression(-value)
    is DecimalExpression -> DecimalExpression(-value)
    else -> UnaryStatement(UnaryType.NEGATIVE, this)
}

operator fun Expression.not() = when (this) {
    is BooleanExpression -> BooleanExpression(!value)
    else -> UnaryStatement(UnaryType.NOT, this)
}

fun Expression.getIncrement() =
        UnaryStatement(UnaryType.GET_INCREMENT, this)

fun Expression.getDecrement() =
        UnaryStatement(UnaryType.GET_DECREMENT, this)

fun Expression.incrementGet() =
        UnaryStatement(UnaryType.INCREMENT_GET, this)

fun Expression.decrementGet() =
        UnaryStatement(UnaryType.DECREMENT_GET, this)

fun Expression.inv() =
        UnaryStatement(UnaryType.BIT_NOT, this)

operator fun Expression.plus(other: Expression) =
        arithmeticOperation(this, other, "plus",
                { a, b -> a + b }, { a, b -> a + b })

operator fun Expression.minus(other: Expression) =
        arithmeticOperation(this, other, "minus",
                { a, b -> a - b }, { a, b -> a - b })

operator fun Expression.times(other: Expression) =
        arithmeticOperation(this, other, "times",
                { a, b -> a * b }, { a, b -> a * b })

operator fun Expression.div(other: Expression) =
        arithmeticOperation(this, other, "div",
                { a, b -> a / b }, { a, b -> a / b })

operator fun Expression.rem(other: Expression) =
        arithmeticOperation(this, other, "rem",
                { a, b -> a % b }, { a, b -> a % b })

infix fun Expression.and(other: Expression) =
        logicOperation(this, other, "and",
                { a, b -> a and b }, { a, b -> a.and(b) })

infix fun Expression.or(other: Expression) =
        logicOperation(this, other, "or",
                { a, b -> a or b }, { a, b -> a.or(b) })

infix fun Expression.xor(other: Expression) =
        logicOperation(this, other, "xor",
                { a, b -> a xor b }, { a, b -> a.xor(b) })

infix fun Expression.shl(other: Expression) =
        FunctionStatement("shl", listOf(this, other))

infix fun Expression.shr(other: Expression) =
        FunctionStatement("shr", listOf(this, other))

infix fun Expression.andAnd(other: Expression) =
        ConditionExpression(ConditionType.AND, this, other)

infix fun Expression.orOr(other: Expression) =
        ConditionExpression(ConditionType.OR, this, other)

infix fun Expression.equals(other: Expression) =
        comparisonOperation(this, other, "equals",
                { a, b -> a == b }, { a, b -> a == b }, { a, b -> a == b })

infix fun Expression.greaterThan(other: Expression) =
        FunctionStatement("greaterThan", listOf(this, other))

infix fun Expression.lessThan(other: Expression) =
        FunctionStatement("lessThan", listOf(this, other))

infix fun Expression.greaterThanEqual(other: Expression) =
        FunctionStatement("greaterThanEqual", listOf(this, other))

infix fun Expression.lessThanEqual(other: Expression) =
        FunctionStatement("lessThanEqual", listOf(this, other))

infix fun Expression.assign(other: Expression) =
        AssignmentStatement(this, other)

fun Expression.member(name: String) =
        MemberExpression(name, this)

fun function(name: String,
             vararg arguments: Expression) =
        function(name, listOf(*arguments))

fun function(name: String,
             arguments: List<Expression>) =
        FunctionStatement(name, arguments)

fun Scope.declareIdentifier(name: String,
                            type: TypeExported) =
        add(name, type)
                ?: throw ShaderCompileException("Redeclaring variable: $name")

fun StatementScope.declaration(
        type: Type,
        name: String,
        initializer: Expression? = null
) = scope.declaration(type, name, initializer)

fun Scope.declaration(
        type: Type,
        name: String,
        initializer: Expression? = null
): FieldDeclarationStatement {
    val identifier = declareIdentifier(name, type.exported)
    return FieldDeclarationStatement(type, identifier, initializer)
}

fun StatementScope.arrayDeclaration(
        type: Type,
        length: Expression,
        name: String,
        initializer: Expression? = null
) = scope.arrayDeclaration(type, length, name, initializer)

fun Scope.arrayDeclaration(
        type: Type,
        length: Expression,
        name: String,
        initializer: Expression? = null
): ArrayDeclarationStatement {
    val identifier = declareIdentifier(name, type.exported)
    return ArrayDeclarationStatement(type, length, identifier, initializer)
}

fun ParameterScope.identifier(name: String) =
        scope.identifier(name)

fun ShaderParameterScope.identifier(name: String) =
        scope.identifier(name)

fun StatementScope.identifier(name: String) =
        scope.identifier(name)

fun Scope.identifier(name: String) =
        this[name]?.let { IdentifierExpression(it) }
                ?: throw ShaderCompileException("Unknown variable: $name")

inline fun StatementScope.compound(block: StatementScope.() -> Unit) =
        scope.compound(block)

inline fun Scope.compound(block: StatementScope.() -> Unit) =
        ArrayList<Statement>().also {
            block(StatementScope(Scope(this), it))
        }.let { CompoundStatement(StatementBlock(it)) }

inline fun StatementScope.block(block: StatementScope.() -> Unit) =
        scope.block(block)

inline fun Scope.block(block: StatementScope.() -> Unit) =
        ArrayList<Statement>().also {
            block(StatementScope(this, it))
        }.let { StatementBlock(it) }

class StatementScope(val scope: Scope,
                     private val statements: MutableCollection<Statement>) {
    fun add(statement: Statement) = statements.add(statement)
}

class ParameterScope(val scope: Scope,
                     private val parameters: MutableCollection<Parameter>) {
    fun add(type: Type,
            name: String): Identifier {
        val identifier = scope.declareIdentifier(name, type.exported)
        return add(Parameter(type, identifier))
    }

    fun add(parameter: Parameter): Identifier {
        parameters.add(parameter)
        return parameter.identifier
    }
}

class ShaderParameterScope(val scope: Scope,
                           private val parameters: MutableCollection<ShaderParameter>) {
    fun add(type: Type,
            name: String,
            available: Expression = BooleanExpression(true)) =
            add(type, -1, name, available)

    fun add(type: Type,
            id: Int,
            name: String,
            available: Expression = BooleanExpression(true)): Identifier {
        val identifier = scope.declareIdentifier(name, type.exported)
        return add(ShaderParameter(type, id, identifier, available))
    }

    fun add(parameter: ShaderParameter): Identifier {
        parameters.add(parameter)
        return parameter.identifier
    }
}

class ShaderProgramScope(val scope: Scope = Scope()) {
    private val declarations = ArrayList<DeclarationStatement>()
    private val functions = ArrayList<CallFunction>()
    private val shaders = HashMap<String, Pair<Scope, (Scope) -> ShaderFunction>>()
    private var outputs: ShaderSignature? = null
    private val uniforms = ArrayList<Uniform?>()
    private val properties = ArrayList<Property>()

    init {
        scope.add("out_Position", Types.Vector4.exported)
        scope.add("varying_Fragment", Types.Vector4.exported)
    }

    fun declaration(type: Type,
                    name: String,
                    initializer: Scope.() -> Expression? = { null }): Identifier {
        val identifier = scope.declareIdentifier(name, type.exported)
        declarations.add(
                FieldDeclarationStatement(type, identifier, initializer(scope)))
        return identifier
    }

    fun uniform(type: Type,
                id: Int,
                name: String): Identifier {
        val identifier = scope.declareIdentifier(name, type.exported)
        val uniform = Uniform(type, id, identifier)
        while (uniforms.size <= uniform.id) {
            uniforms.add(null)
        }
        uniforms[uniform.id] = uniform
        return identifier
    }

    fun shaderFunction(name: String,
                       parameters: ShaderParameterScope.() -> Unit,
                       block: StatementScope.() -> Unit) {
        val inputScope = Scope(scope)
        val signature = ShaderSignature(name,
                ArrayList<ShaderParameter>().also {
                    parameters(ShaderParameterScope(inputScope, it))
                })
        shaders[name] = Pair(inputScope, { shaderScope ->
            val compound = Scope(inputScope, shaderScope).compound(block)
            ShaderFunction(signature, compound)
        })
    }

    fun outputs(parameters: ShaderParameterScope.() -> Unit) {
        outputs = ShaderSignature("outputs",
                ArrayList<ShaderParameter>().also {
                    parameters(ShaderParameterScope(scope, it))
                })
    }

    fun finish() = ShaderProgram(declarations, functions, shaders, outputs,
            uniforms, properties).finish(scope)
}

fun ShaderProgram.finish(scope: Scope): CompiledShader {
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
    return CompiledShader(declarations, functions,
            shaderVertex?.second, shaderFragment?.second, outputs,
            uniforms.toTypedArray(), properties)
}
