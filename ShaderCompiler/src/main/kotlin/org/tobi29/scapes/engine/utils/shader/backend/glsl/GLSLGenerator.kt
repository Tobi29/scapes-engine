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

package org.tobi29.scapes.engine.utils.shader.backend.glsl

import org.tobi29.scapes.engine.utils.ConcurrentHashMap
import org.tobi29.scapes.engine.utils.ThreadLocal
import org.tobi29.scapes.engine.utils.computeAbsent
import org.tobi29.scapes.engine.utils.readOnly
import org.tobi29.scapes.engine.utils.shader.*
import org.tobi29.scapes.engine.utils.shader.Function
import java.math.BigInteger

class GLSLGenerator(private val version: GLSLGenerator.Version) {
    private var output = StringBuilder(1024)
    private val identifiers = HashMap<Identifier, Expression>()
    private lateinit var context: ShaderContext
    private var functionImplementations = HashMap<FunctionExportedSignature, (Array<String>) -> String>()

    private fun staticFunction(expression: FunctionExpression): String {
        val name = expression.name
        val args = Array(expression.args.size) {
            expression(expression.args[it])
        }
        val signature = FunctionParameterSignature(name,
                expression.args.map { it.type(context) })
        val newFunction = context.functions[signature]
        if (newFunction != null) {
            functionImplementations[newFunction]?.let { return it(args) }
            return glslFunction(newFunction.name, *args)
        }
        throw ShaderGenerateException(
                "No functions for given arguments: $signature", expression)
    }

    private fun variable(identifier: Identifier): String? {
        return identifiers[identifier]?.let { expression(it) } ?: return null
    }

    private fun member(field: String): String {
        return field
    }

    private fun expression(expression: Expression): String {
        if (expression is GLSLExpression) {
            return glslExpression(expression)
        } else if (expression is AssignmentExpression) {
            return assignmentExpression(expression)
        } else if (expression is ConditionExpression) {
            return conditionExpression(expression)
        } else if (expression is UnaryExpression) {
            return unaryExpression(expression)
        } else if (expression is TernaryExpression) {
            return ternaryExpression(expression)
        } else if (expression is FunctionExpression) {
            return functionExpression(expression)
        } else if (expression is ArrayAccessExpression) {
            return arrayAccessExpression(expression)
        } else if (expression is BooleanExpression) {
            return booleanExpression(expression)
        } else if (expression is IntegerExpression) {
            return integerExpression(expression)
        } else if (expression is DecimalExpression) {
            return decimalExpression(expression)
        } else if (expression is ArrayExpression) {
            return arrayExpression(expression)
        } else if (expression is IdentifierExpression) {
            return variableExpression(expression)
        } else if (expression is MemberExpression) {
            return memberExpression(expression)
        } else if (expression is VoidExpression) {
            return ""
        }
        throw IllegalArgumentException(
                "Unknown expression: ${expression::class}")
    }

    private fun glslExpression(expression: GLSLExpression): String {
        return expression.code
    }

    private fun assignmentExpression(expression: AssignmentExpression): String {
        return combineNotPacked(expression.left, expression.right, "=")
    }

    private fun conditionExpression(expression: ConditionExpression): String {
        return combine(expression.left, expression.right,
                conditionOperator(expression.type))
    }

    private fun conditionOperator(type: ConditionType): String {
        when (type) {
            ConditionType.OR -> return "||"
            ConditionType.AND -> return "&&"
            ConditionType.EQUALS -> return "=="
            ConditionType.NOT_EQUALS -> return "!="
            ConditionType.LESS -> return "<"
            ConditionType.GREATER -> return ">"
            ConditionType.LESS_EQUAL -> return "<="
            ConditionType.GREATER_EQUAL -> return ">="
            else -> throw IllegalArgumentException(
                    "Unexpected expression type: $type")
        }
    }

    private fun unaryExpression(expression: UnaryExpression): String {
        val str = pack(expression.value)
        when (expression.type) {
            UnaryType.INCREMENT_GET -> return "++" + str
            UnaryType.DECREMENT_GET -> return "--" + str
            UnaryType.GET_INCREMENT -> return str + "++"
            UnaryType.GET_DECREMENT -> return str + "--"
            UnaryType.POSITIVE -> return '+' + str
            UnaryType.NEGATIVE -> return '-' + str
            UnaryType.BIT_NOT -> return '~' + str
            UnaryType.NOT -> return '!' + str
            else -> throw IllegalArgumentException(
                    "Unexpected expression type: ${expression.type}")
        }
    }

    private fun ternaryExpression(expression: TernaryExpression): String {
        return pack(expression.condition) + " ? " +
                pack(expression.expression) + " : " +
                pack(expression.expressionElse)
    }

    private fun functionExpression(expression: FunctionExpression): String {
        return staticFunction(expression)
    }

    private fun arrayAccessExpression(expression: ArrayAccessExpression): String {
        return pack(expression.name) + '[' +
                expression(expression.index) + ']'
    }

    private fun booleanExpression(expression: BooleanExpression): String {
        return expression.value.toString()
    }

    private fun integerExpression(expression: IntegerExpression): String {
        return expression.value.toString()
    }

    private fun decimalExpression(expression: DecimalExpression): String {
        val str = expression.value.toString()
        if (str.indexOf('.') == -1) {
            return str + ".0"
        }
        return str
    }

    private fun variableExpression(expression: IdentifierExpression): String {
        return variable(expression.identifier) ?: throw ShaderGenerateException(
                "Unknown identifier: ${expression.identifier.name}", expression)
    }

    private fun memberExpression(expression: MemberExpression): String {
        return pack(expression.member) + '.' + member(expression.name)
    }

    private fun ifStatement(expression: IfStatement,
                            level: Int) {
        val condition = expression.condition.simplify(context, identifiers)
        if (condition is BooleanExpression) {
            if (condition.value) {
                statement(expression.statement, level)
            } else if (expression.statementElse != null) {
                statement(expression.statementElse, level)
            }
        } else {
            println(level, "if(${expression(condition)})")
            statement(expression.statement, level)
            if (expression.statementElse != null) {
                println(level, "else")
                statement(expression.statementElse, level)
            }
        }
    }

    private fun loopFixedStatement(expression: LoopFixedStatement,
                                   level: Int) {
        val startInt = integer(
                expression.start.simplify(context, identifiers)).toInt()
        val endInt = integer(
                expression.end.simplify(context, identifiers)).toInt()
        for (i in startInt..endInt - 1) {
            identifiers[expression.index] = IntegerExpression(
                    BigInteger(i.toString()))
            statement(expression.statement, level)
        }
    }

    private fun declarationStatement(
            statement: DeclarationStatement,
            level: Int) {
        identifiers[statement.identifier] = GLSLExpression(
                statement.identifier.type, statement.identifier.name)
        val declaration = type(statement.type, statement.identifier)
        if (statement.initializer == null) {
            println(level, declaration + ';')
        } else {
            println(level, declaration + " = " +
                    expression(statement.initializer) + ';')
        }
    }

    private fun arrayDeclarationStatement(
            statement: ArrayDeclarationStatement,
            level: Int) {
        identifiers[statement.identifier] = GLSLExpression(
                statement.identifier.type, statement.identifier.name)
        val declaration = type(statement.type, statement.identifier)
        if (statement.initializer == null) {
            println(level, declaration + ';')
        } else {
            println(level, declaration + " = " +
                    type(statement.type.type) + "[]" +
                    expression(statement.initializer) + ';')
        }
    }

    private fun arrayExpression(initializer: ArrayExpression) =
            initializer.content.asSequence().map {
                expression(it)
            }.joinToString(prefix = "(", postfix = ")")

    private fun combine(a: Expression,
                        b: Expression,
                        operator: String): String {
        return pack(a) + ' ' + operator + ' ' + pack(b)
    }

    private fun combineNotPacked(a: Expression,
                                 b: Expression,
                                 operator: String): String {
        return expression(a) + ' ' + operator + ' ' + pack(b)
    }

    private fun pack(expression: Expression): String {
        return "(${expression(expression)})"
    }

    private fun type(type: Type,
                     identifier: Identifier): String? {
        val expression = identifiers[identifier] ?: return null
        return type(type, expression(expression))
    }

    private fun type(type: Type,
                     identifier: String): String {
        val str = StringBuilder(24)
        if (type.constant) {
            str.append("const ")
        }
        when (version) {
            GLSLGenerator.Version.GLES_300 -> str.append(
                    precision(type.precision)).append(' ')
            else -> Unit
        }
        str.append(type(type.type))
        str.append(' ')
        str.append(identifier)
        if (type.array != null) {
            str.append('[')
            str.append(expression(type.array))
            str.append(']')
        }
        return str.toString()
    }

    private fun type(type: Type): String {
        val qualifiers = StringBuilder(24)
        if (type.constant) {
            qualifiers.append("const ")
        }
        when (version) {
            GLSLGenerator.Version.GLES_300 -> qualifiers.append(
                    precision(type.precision)).append(' ')
            else -> Unit
        }
        return qualifiers.toString() + type(type.type)
    }

    private fun identifier(type: Type,
                           identifier: Identifier): String? {
        val expression = identifiers[identifier] ?: return null
        return identifier(type, expression(expression))
    }

    private fun identifier(type: Type,
                           identifier: String): String {
        if (type.array != null) {
            return "$identifier[${expression(type.array)}]"
        }
        return identifier
    }

    private fun type(type: Types): String {
        when (type) {
            Types.Void -> return "void"
            Types.Float -> return "float"
            Types.Boolean -> return "bool"
            Types.Int -> return "int"
            Types.Vector2 -> return "vec2"
            Types.Vector2b -> return "bvec2"
            Types.Vector2i -> return "ivec2"
            Types.Matrix2 -> return "mat2"
            Types.Vector3 -> return "vec3"
            Types.Vector3b -> return "bvec3"
            Types.Vector3i -> return "ivec3"
            Types.Matrix3 -> return "mat3"
            Types.Vector4 -> return "vec4"
            Types.Vector4b -> return "bvec4"
            Types.Vector4i -> return "ivec4"
            Types.Matrix4 -> return "mat4"
            Types.Texture2 -> return "sampler2D"
            else -> throw IllegalArgumentException("Unexpected type: $type")
        }
    }

    private fun precision(precision: Precision): String {
        when (precision) {
            Precision.lowp -> return "lowp"
            Precision.mediump -> return "mediump"
            Precision.highp -> return "highp"
            else -> throw IllegalArgumentException(
                    "Unexpected precision: $precision")
        }
    }

    private fun integer(expression: Expression): BigInteger {
        val integer = expression.simplify(context,
                identifiers) as? IntegerExpression
                ?: throw ShaderGenerateException(
                "Expression has to be integer on compile time", expression)
        return integer.value
    }

    private inline fun initBuiltIn(identifier: Identifier?,
                                   init: (Identifier) -> Expression) {
        identifier?.let { identifiers[it] = init(it) }
    }

    private fun init(scope: Scope) {
        stdFunctionSignatures.forEach {
            functionImplementations[it.first] = it.second
        }
        initBuiltIn(scope["out_Position"]) {
            GLSLExpression(Types.Vector4.exported, "gl_Position")
        }
        initBuiltIn(scope["varying_Fragment"]) {
            GLSLExpression(Types.Vector4.exported, "gl_FragCoord")
        }
    }

    fun generateVertex(scope: Scope,
                       shader: CompiledShader,
                       properties: Map<String, Expression>): String {
        if (output.isNotEmpty()) {
            // output.delete(0, output.length - 1)
            output = StringBuilder(1024)
        }
        init(scope)
        context = ShaderContext(shader.functionMap + stdFunctionSignatures.map {
            Pair(it.first.call, it.first)
        }, properties)
        val shaderVertex = shader.shaderVertex ?: throw IllegalStateException(
                "No vertex shader")
        val shaderFragment = shader.shaderFragment ?: throw IllegalStateException(
                "No fragment shader")
        propertyIdentifiers(shader.properties, properties)
        signatureIdentifiers(shaderVertex.signature)
        signatureIdentifiers(shaderFragment.signature)
        header(shader.uniforms(), shaderVertex.signature)
        println()
        header(shaderFragment.signature)
        println()
        header(shader.declarations)
        println()
        functions(shader.functions)
        println()
        shader(shaderVertex)
        identifiers.clear()
        functionImplementations.clear()
        return output.toString()
    }

    fun generateFragment(scope: Scope,
                         shader: CompiledShader,
                         properties: Map<String, Expression>): String {
        if (output.isNotEmpty()) {
            // output.delete(0, output.length - 1)
            output = StringBuilder(1024)
        }
        init(scope)
        context = ShaderContext(shader.functionMap + stdFunctionSignatures.map {
            Pair(it.first.call, it.first)
        }, properties)
        val shaderFragment = shader.shaderFragment ?: throw IllegalStateException(
                "No fragment shader")
        val outputs = shader.outputs ?: throw IllegalStateException(
                "No outputs")
        propertyIdentifiers(shader.properties, properties)
        signatureIdentifiers(outputs)
        header(shader.uniforms(), shaderFragment.signature)
        println()
        header(outputs)
        println()
        header(shader.declarations)
        println()
        functions(shader.functions)
        println()
        shader(shaderFragment)
        identifiers.clear()
        functionImplementations.clear()
        return output.toString()
    }

    private fun header(uniforms: Array<Uniform?>,
                       input: ShaderSignature) {
        when (version) {
            GLSLGenerator.Version.GL_330 -> println(0, "#version 330")
            GLSLGenerator.Version.GLES_300 -> println(0, "#version 300 es")
        }
        println()
        for (uniform in uniforms) {
            if (uniform != null) {
                identifiers[uniform.identifier] = GLSLExpression(
                        uniform.identifier.type, uniform.identifier.name)
                println(0, "uniform " + type(uniform.type) + ' ' +
                        identifier(uniform.type, uniform.identifier) +
                        ';')
            }
        }
        println()
        for (parameter in input.parameters) {
            val available = expression(parameter.available)
            if (available != "true" && available != "(true)") {
                continue
            }
            identifiers[parameter.identifier] = GLSLExpression(
                    parameter.identifier.type, parameter.identifier.name)
            if (parameter.id == -1) {
                println(0, "in " + type(parameter.type, parameter.identifier) +
                        ';')
            } else {
                println(0, "layout(location = " + parameter.id + ") in " +
                        type(parameter.type, parameter.identifier) + ';')
            }
        }
    }

    private fun signatureIdentifiers(output: ShaderSignature) {
        for (parameter in output.parameters) {
            val available = expression(parameter.available)
            if (available != "true" && available != "(true)") {
                continue
            }
            identifiers[parameter.identifier] = GLSLExpression(
                    parameter.identifier.type, parameter.identifier.name)
        }
    }

    private fun propertyIdentifiers(properties: List<Property>,
                                    propertyValues: Map<String, Expression>) {
        for (property in properties) {
            val value = propertyValues[property.identifier.name]
                    ?: throw ShaderGenerateException(
                    "No value defined for property: ${property.identifier.name}")
            val valueType = value.type(context)
            if (property.type.exported != valueType) {
                throw ShaderGenerateException(
                        "Property declaration for ${property.identifier.name} and value type conflict: ${property.type.exported} <-> $valueType")
            }
            identifiers[property.identifier] = value
        }
    }

    private fun header(output: ShaderSignature) {
        for (parameter in output.parameters) {
            val available = expression(parameter.available)
            if (available != "true" && available != "(true)") {
                continue
            }
            if (parameter.id == -1) {
                println(0, "out " + type(parameter.type, parameter.identifier) +
                        ';')
            } else {
                println(0, "layout(location = " + parameter.id + ") out " +
                        type(parameter.type, parameter.identifier) + ';')
            }
        }
    }

    private fun header(statements: List<Statement>) {
        for (statement in statements) {
            statement(statement, 0)
        }
    }

    private fun functions(functions: List<Function>) {
        for (function in functions) {
            val signature = function.signature
            println(0, signature(signature, signature.name))
            statement(function.compound, 0)
        }
    }

    private fun signature(signature: FunctionSignature,
                          name: String): String {
        val str = StringBuilder(24)
        str.append(precision(signature.returnedPrecision)).append(' ')
        str.append(type(signature.returned.type))
        if (signature.returned.array) {
            str.append("[]")
        }
        str.append(' ')
        str.append(name).append('(')
        if (signature.parameters.isNotEmpty()) {
            run {
                val parameter = signature.parameters[0]
                identifiers[parameter.identifier] = GLSLExpression(
                        parameter.identifier.type, parameter.identifier.name)
                str.append(type(parameter.type, parameter.identifier))
            }
            for (i in 1..signature.parameters.lastIndex) {
                val parameter = signature.parameters[1]
                identifiers[parameter.identifier] = GLSLExpression(
                        parameter.identifier.type, parameter.identifier.name)
                str.append(", ").append(
                        type(parameter.type, parameter.identifier))
            }
        }
        str.append(')')
        return str.toString()
    }

    private fun shader(function: ShaderFunction) {
        println(0, "void main(void)")
        statement(function.compound, 0)
    }

    private fun block(expression: StatementBlock,
                      level: Int) {
        for (statement in expression.statements) {
            statement(statement, level)
        }
    }

    private fun statement(statement: Statement,
                          level: Int) {
        if (statement is CompoundStatement) {
            println(level, "{")
            block(statement.block, level + 1)
            println(level, "}")
        } else if (statement is IfStatement) {
            ifStatement(statement, level)
        } else if (statement is LoopFixedStatement) {
            loopFixedStatement(statement, level)
        } else if (statement is DeclarationStatement) {
            declarationStatement(statement, level)
        } else if (statement is ArrayDeclarationStatement) {
            arrayDeclarationStatement(statement, level)
        } else if (statement is ExpressionStatement) {
            println(level, expression(statement.expression) + ';')
        } else {
            throw IllegalArgumentException(
                    "Unknown statement: ${statement::class}")
        }
    }

    private fun println() {
        output.append('\n')
    }

    private fun println(level: Int,
                        str: String) {
        var indents = level
        while (indents > 0) {
            output.append("    ")
            indents--
        }
        output.append(str).append('\n')
    }

    companion object {
        private val stdFunctionSignatures =
                ArrayList<Pair<FunctionExportedSignature, (Array<String>) -> String>>()
                        .also { GLSLSTDLib.functions(it) }.readOnly()

        private fun glslFunction(name: String,
                                 vararg args: String) =
                "$name(${args.joinToString()})"

        fun generate(version: Version,
                     shader: CompiledShader,
                     properties: Map<String, Expression>): Pair<String, String> {
            val generator = generator.computeAbsent(version) {
                ThreadLocal { GLSLGenerator(version) }
            }.get()
            val vertexSource = generator.generateVertex(shader.scope,
                    shader, properties)
            val fragmentSource = generator.generateFragment(shader.scope,
                    shader, properties)
            return Pair(vertexSource, fragmentSource)
        }

        private val generator = ConcurrentHashMap<Version, ThreadLocal<GLSLGenerator>>()
    }

    enum class Version {
        GL_330,
        GLES_300
    }
}

class GLSLExpression(val type: TypeExported,
                     val code: String) : Expression() {
    override fun type(context: ShaderContext) = type

    override fun simplify(context: ShaderContext,
                          identifiers: Map<Identifier, Expression>) = this
}
