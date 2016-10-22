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

package org.tobi29.scapes.engine.utils.shader.glsl

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.tobi29.scapes.engine.utils.join
import org.tobi29.scapes.engine.utils.shader.CompiledShader
import org.tobi29.scapes.engine.utils.shader.ShaderCompileException
import org.tobi29.scapes.engine.utils.shader.ShaderCompiler
import org.tobi29.scapes.engine.utils.shader.ShaderGenerateException
import org.tobi29.scapes.engine.utils.shader.expression.*
import org.tobi29.scapes.engine.utils.shader.expression.Function
import java.util.concurrent.ConcurrentHashMap

class GLSLGenerator(private val version: GLSLGenerator.Version) {
    private val output = StringBuilder(1024)
    private val variables = ConcurrentHashMap<String, Expression>()
    private var properties: Map<String, String>? = null
    private var functions: List<Function>? = null

    init {
        variables.put("out_Position", GLSLExpression("gl_Position"))
        variables.put("varying_Fragment", GLSLExpression("gl_FragCoord"))
    }

    private fun staticFunction(name: String,
                               arguments: Array<String>): String {
        val functions = functions ?: throw IllegalStateException(
                "No shader program")
        for (function in functions) {
            if (function.signature.name != name) {
                continue
            }
            if (function.signature.parameters.size != arguments.size) {
                continue
            }
            return "${function.signature.name}(${join(*arguments)})"
        }
        when (arguments.size) {
            0 -> return staticFunction(name)
            1 -> return staticFunction(name, arguments[0])
            2 -> return staticFunction(name, arguments[0], arguments[1])
            3 -> return staticFunction(name, arguments[0], arguments[1],
                    arguments[2])
            4 -> return staticFunction(name, arguments[0], arguments[1],
                    arguments[2], arguments[3])
        }
        throw ShaderGenerateException(
                "No functions for given arguments: " + name)
    }

    private fun staticFunction(name: String): String {
        when (name) {
            "discard" -> return "discard"
        }
        throw ShaderGenerateException("Unknown function: $name()")
    }

    private fun staticFunction(name: String,
                               argument0: String): String {
        when (name) {
            "return" -> return "return " + argument0
            "length" -> return "length($argument0)"
            "floor" -> return "floor($argument0)"
            "abs" -> return "abs($argument0)"
            "sin" -> return "sin($argument0)"
            "cos" -> return "cos($argument0)"
            "float" -> return "float($argument0)"
            "vector2" -> return "vec2($argument0)"
            "vector3" -> return "vec3($argument0)"
            "vector4" -> return "vec4($argument0)"
        }
        throw ShaderGenerateException("Unknown function: $name(x)")
    }

    private fun staticFunction(name: String,
                               argument0: String,
                               argument1: String): String {
        when (name) {
            "texture" -> return "texture(" + argument0 + ", " +
                    argument1 + ')'
            "min" -> return "min(" + argument0 + ", " +
                    argument1 + ')'
            "max" -> return "max(" + argument0 + ", " +
                    argument1 + ')'
            "dot" -> return "dot(" + argument0 + ", " +
                    argument1 + ')'
            "mod" -> return "mod(" + argument0 + ", " +
                    argument1 + ')'
            "greaterThan" -> return "greaterThan(" + argument0 + ", " +
                    argument1 + ')'
            "greaterThanEqual" -> return "greaterThanEqual(" + argument0 + ", " +
                    argument1 + ')'
            "lessThan" -> return "lessThan(" + argument0 + ", " +
                    argument1 + ')'
            "lessThanEqual" -> return "lessThanEqual(" + argument0 + ", " +
                    argument1 + ')'
            "vector2" -> return "vec2(" + argument0 + ", " +
                    argument1 + ')'
            "vector3" -> return "vec3(" + argument0 + ", " +
                    argument1 + ')'
            "vector4" -> return "vec4(" + argument0 + ", " +
                    argument1 + ')'
        }
        throw ShaderGenerateException(
                "Unknown function: $name(x, y)")
    }

    private fun staticFunction(name: String,
                               argument0: String,
                               argument1: String,
                               argument2: String): String {
        when (name) {
            "mix" -> return "mix(" + argument0 + ", " +
                    argument1 + ", " + argument2 + ')'
            "clamp" -> return "clamp(" + argument0 + ", " +
                    argument1 + ", " + argument2 + ')'
            "vector3" -> return "vec3(" + argument0 + ", " +
                    argument1 + ", " + argument2 + ')'
            "vector4" -> return "vec4(" + argument0 + ", " +
                    argument1 + ", " + argument2 + ')'
        }
        throw ShaderGenerateException(
                "Unknown function: $name(x, y, z)")
    }

    private fun staticFunction(name: String,
                               argument0: String,
                               argument1: String,
                               argument2: String,
                               argument3: String): String {
        when (name) {
            "vector4" -> return "vec4(" + argument0 + ", " +
                    argument1 + ", " + argument2 + ", " + argument3 + ')'
        }
        throw ShaderGenerateException(
                "Unknown function: $name(x, y, z, w)")
    }

    private fun variable(field: String): String {
        val expression = variables[field] ?: return field
        return pack(expression)
    }

    private fun expression(expression: Expression): String {
        if (expression is AssignmentExpression) {
            return assignmentExpression(expression)
        } else if (expression is ConditionExpression) {
            return conditionExpression(expression)
        } else if (expression is OperationExpression) {
            return operationExpression(expression)
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
        } else if (expression is FloatingExpression) {
            return floatingExpression(expression)
        } else if (expression is VariableExpression) {
            return variableExpression(expression)
        } else if (expression is MemberExpression) {
            return memberExpression(expression)
        } else if (expression is PropertyExpression) {
            return propertyExpression(expression)
        } else if (expression is GLSLExpression) {
            return glslExpression(expression)
        } else if (expression is VoidExpression) {
            return ""
        }
        throw ShaderGenerateException(
                "Unknown expression: " + expression.javaClass)
    }

    private fun assignmentExpression(expression: AssignmentExpression): String {
        return combineNotPacked(expression.left, expression.right,
                assignmentOperator(expression.type))
    }

    private fun assignmentOperator(type: AssignmentType): String {
        when (type) {
            AssignmentType.ASSIGN -> return "="
            AssignmentType.ASSIGN_SHIFT_LEFT -> return "<<="
            AssignmentType.ASSIGN_SHIFT_RIGHT -> return ">>="
            AssignmentType.ASSIGN_PLUS -> return "+="
            AssignmentType.ASSIGN_MINUS -> return "-="
            AssignmentType.ASSIGN_MULTIPLY -> return "*="
            AssignmentType.ASSIGN_DIVIDE -> return "/="
            AssignmentType.ASSIGN_MODULUS -> return "%="
            AssignmentType.ASSIGN_AND -> return "&="
            AssignmentType.ASSIGN_INCLUSIVE_OR -> return "|="
            AssignmentType.ASSIGN_EXCLUSIVE_OR -> return "^="
            else -> throw ShaderGenerateException(
                    "Unexpected expression type: " + type)
        }
    }

    private fun conditionExpression(expression: ConditionExpression): String {
        return combine(expression.left, expression.right,
                conditionOperator(expression.type))
    }

    private fun conditionOperator(type: ConditionType): String {
        when (type) {
            ConditionType.CONDITION_LOGICAL_OR -> return "||"
            ConditionType.CONDITION_LOGICAL_AND -> return "&&"
            ConditionType.CONDITION_INCLUSIVE_OR -> return "|"
            ConditionType.CONDITION_EXCLUSIVE_OR -> return "^"
            ConditionType.CONDITION_AND -> return "&"
            ConditionType.CONDITION_EQUALS -> return "=="
            ConditionType.CONDITION_NOT_EQUALS -> return "!="
            ConditionType.CONDITION_LESS -> return "<"
            ConditionType.CONDITION_GREATER -> return ">"
            ConditionType.CONDITION_LESS_EQUAL -> return "<="
            ConditionType.CONDITION_GREATER_EQUAL -> return ">="
            else -> throw ShaderGenerateException(
                    "Unexpected expression type: " + type)
        }
    }

    private fun operationExpression(expression: OperationExpression): String {
        return combine(expression.left, expression.right,
                operationOperator(expression.type))
    }

    private fun operationOperator(type: OperationType): String {
        when (type) {
            OperationType.SHIFT_LEFT -> return "<<"
            OperationType.SHIFT_RIGHT -> return ">>"
            OperationType.PLUS -> return "+"
            OperationType.MINUS -> return "-"
            OperationType.MULTIPLY -> return "*"
            OperationType.DIVIDE -> return "/"
            OperationType.MODULUS -> return "%"
            else -> throw ShaderGenerateException(
                    "Unexpected expression type: " + type)
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
            else -> throw ShaderGenerateException(
                    "Unexpected expression type: " + expression.type)
        }
    }

    private fun ternaryExpression(expression: TernaryExpression): String {
        return pack(expression.condition) + " ? " +
                pack(expression.expression) + " : " +
                pack(expression.expressionElse)
    }

    private fun functionExpression(expression: FunctionExpression): String {
        val args = Array(expression.args.size) {
            expression(expression.args[it])
        }
        return staticFunction(expression.name, args)
    }

    private fun arrayAccessExpression(expression: ArrayAccessExpression): String {
        return pack(expression.name) + '[' +
                expression(expression.index) + ']'
    }

    private fun booleanExpression(expression: BooleanExpression): String {
        return expression.value.toString()
    }

    private fun integerExpression(expression: IntegerExpression): String {
        if (expression is IntegerLiteralExpression) {
            return expression.value.toString()
        } else if (expression is IntegerPropertyExpression) {
            return property(expression.key)
        }
        throw ShaderGenerateException(
                "Unknown integer: " + expression.javaClass)
    }

    private fun floatingExpression(expression: FloatingExpression): String {
        val str = expression.value.toString()
        if (str.indexOf('.') == -1) {
            return str + ".0"
        }
        return str
    }

    private fun variableExpression(expression: VariableExpression): String {
        return variable(expression.name)
    }

    private fun memberExpression(expression: MemberExpression): String {
        return pack(expression.member) + '.' + variable(expression.name)
    }

    private fun propertyExpression(expression: PropertyExpression): String {
        return property(expression.key)
    }

    private fun glslExpression(expression: GLSLExpression): String {
        return expression.code
    }

    private fun ifStatement(expression: IfStatement,
                            level: Int) {
        val condition = expression(expression.condition)
        if ("true" == condition) {
            statement(expression.statement, level)
        } else if ("false" == condition) {
            if (expression.statementElse != null) {
                statement(expression.statementElse, level)
            }
        } else {
            println(level, "if(" + condition +
                    ')')
            statement(expression.statement, level)
            if (expression.statementElse != null) {
                println(level, "else")
                statement(expression.statementElse, level)
            }
        }
    }

    private fun loopFixedStatement(expression: LoopFixedStatement,
                                   level: Int) {
        val name = expression.name.toString()
        val start = integer(expression(expression.start))
        val end = integer(expression(expression.end))
        if (variables.containsKey(name)) {
            throw ShaderGenerateException("Duplicate field: " + name)
        }
        for (i in start..end - 1) {
            variables.put(name, GLSLExpression(i.toString()))
            statement(expression.statement, level)
        }
        variables.remove(name)
    }

    private fun declarationStatement(expression: DeclarationStatement,
                                     level: Int) {
        println(level, declarationStatement(expression) + ';')
    }

    private fun declarationStatement(statement: DeclarationStatement): String {
        val str = StringBuilder(32)
        str.append(type(statement.type))
        str.append(' ')
        var first = true
        for (declaration in statement.declarations) {
            if (first) {
                first = false
            } else {
                str.append(", ")
            }
            str.append(declaration(statement.type, declaration))
        }
        return str.toString()
    }

    private fun declaration(type: Type,
                            declaration: Declaration): String {
        val str = identifier(type, declaration.name)
        if (declaration.initializer != null) {
            return str + " = " + expression(declaration.initializer)
        }
        return str
    }

    private fun arrayDeclarationStatement(statement: ArrayDeclarationStatement,
                                          level: Int) {
        println(level, arrayDeclarationStatement(statement) + ';')
    }

    private fun arrayDeclarationStatement(
            statement: ArrayDeclarationStatement): String {
        val str = StringBuilder(32)
        str.append(type(statement.type))
        str.append(' ')
        var first = true
        for (declaration in statement.declarations) {
            if (first) {
                first = false
            } else {
                str.append(", ")
            }
            str.append(arrayDeclaration(statement.type, declaration))
        }
        return str.toString()
    }

    private fun arrayDeclaration(type: Type,
                                 declaration: ArrayDeclaration): String {
        return identifier(type, declaration.name)
    }

    private fun arrayUnsizedDeclarationStatement(
            statement: ArrayUnsizedDeclarationStatement,
            level: Int) {
        val initializer: ArrayLiteralExpression
        if (statement.initializer is PropertyArrayExpression) {
            initializer = property(statement.initializer)
        } else if (statement.initializer is ArrayLiteralExpression) {
            initializer = statement.initializer
        } else {
            throw ShaderGenerateException(
                    "Unknown array initializer: " + statement.javaClass)
        }
        println(level, type(statement.type, statement.name) + '[' +
                initializer.content.size + "] = " +
                type(statement.type.type) + "[]" +
                arrayExpression(initializer) + ';')
    }

    private fun arrayExpression(initializer: ArrayLiteralExpression): String {
        val str = StringBuilder(initializer.content.size * 7)
        str.append('(')
        var first = true
        for (expression in initializer.content) {
            if (first) {
                first = false
            } else {
                str.append(", ")
            }
            str.append(expression(expression))
        }
        str.append(')')
        return str.toString()
    }

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
        return '(' + expression(expression) + ')'
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
        }
        return qualifiers.toString() + type(type.type)
    }

    private fun identifier(type: Type,
                           identifier: String): String {
        if (type.array != null) {
            return identifier + '[' + expression(type.array) + ']'
        }
        return identifier
    }

    private fun type(type: Types): String {
        when (type) {
            Types.Void -> return "void"
            Types.Float -> return "float"
            Types.Int -> return "int"
            Types.Vector2 -> return "vec2"
            Types.Vector2i -> return "ivec2"
            Types.Matrix2 -> return "mat2"
            Types.Vector3 -> return "vec3"
            Types.Vector3i -> return "ivec3"
            Types.Matrix3 -> return "mat3"
            Types.Vector4 -> return "vec4"
            Types.Vector4i -> return "ivec4"
            Types.Matrix4 -> return "mat4"
            Types.Texture2 -> return "sampler2D"
            else -> throw ShaderGenerateException("Unexpected type: " + type)
        }
    }

    private fun precision(precision: Precision): String {
        when (precision) {
            Precision.lowp -> return "lowp"
            Precision.mediump -> return "mediump"
            Precision.highp -> return "highp"
            else -> throw ShaderGenerateException(
                    "Unexpected precision: " + precision)
        }
    }

    private fun property(expression: PropertyArrayExpression): ArrayLiteralExpression {
        val source = property(expression.key)
        try {
            val parser = ShaderCompiler.parser(source)
            val expression2 = ShaderCompiler.initializer(
                    parser.initializerArrayList())
            if (expression2 is ArrayLiteralExpression) {
                return expression2
            } else {
                throw ShaderGenerateException(
                        "Property has to be a static array expression")
            }
        } catch (e: ParseCancellationException) {
            throw ShaderGenerateException(e)
        } catch (e: ShaderCompileException) {
            throw ShaderGenerateException(e)
        }

    }

    private fun property(name: String): String {
        val properties = properties ?: throw IllegalStateException(
                "No shader program")
        val value = properties[name] ?: throw ShaderGenerateException(
                "Unknown property: " + name)
        return value
    }

    private fun integer(value: String): Int {
        try {
            return value.toInt()
        } catch (e: NumberFormatException) {
            throw ShaderGenerateException(e)
        }

    }

    fun generateVertex(shader: CompiledShader,
                       properties: Map<String, String>): String {
        if (output.length > 0) {
            output.delete(0, output.length - 1)
        }
        this.properties = properties
        val functions = shader.functions
        this.functions = functions
        val shaderVertex = shader.shaderVertex ?: throw IllegalStateException(
                "No vertex shader")
        val shaderFragment = shader.shaderFragment ?: throw IllegalStateException(
                "No fragment shader")
        val uniforms = shader.uniforms()
        header(uniforms, shaderVertex.signature)
        println()
        header(shaderFragment.signature)
        println()
        header(shader.declarations)
        println()
        functions(functions)
        println()
        shader(shaderVertex)
        return output.toString()
    }

    fun generateFragment(shader: CompiledShader,
                         properties: Map<String, String>): String {
        if (output.length > 0) {
            output.delete(0, output.length - 1)
        }
        this.properties = properties
        val functions = shader.functions
        this.functions = functions
        val shaderFragment = shader.shaderFragment ?: throw IllegalStateException(
                "No fragment shader")
        val outputs = shader.outputs ?: throw IllegalStateException(
                "No outputs")
        val uniforms = shader.uniforms()
        header(uniforms, shaderFragment.signature)
        println()
        header(outputs)
        println()
        header(shader.declarations)
        println()
        functions(functions)
        println()
        shader(shaderFragment)
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
                println(0, "uniform " + type(uniform.type) + ' ' +
                        identifier(uniform.type, uniform.name) +
                        ';')
            }
        }
        println()
        for (parameter in input.parameters) {
            if (!expression(parameter.available).toBoolean()) {
                continue
            }
            if (parameter.id == -1) {
                println(0, "in " + type(parameter.type, parameter.name) +
                        ';')
            } else {
                println(0, "layout(location = " + parameter.id + ") in " +
                        type(parameter.type, parameter.name) + ';')
            }
        }
    }

    private fun header(output: ShaderSignature) {
        for (parameter in output.parameters) {
            if (!expression(parameter.available).toBoolean()) {
                continue
            }
            if (parameter.id == -1) {
                println(0, "out " + type(parameter.type, parameter.name) +
                        ';')
            } else {
                println(0, "layout(location = " + parameter.id + ") out " +
                        type(parameter.type, parameter.name) + ';')
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
            println(0, signature(signature))
            statement(function.compound, 0)
        }
    }

    private fun signature(signature: FunctionSignature): String {
        val str = StringBuilder(24)
        str.append(precision(signature.returnedPrecision)).append(' ')
        str.append(type(signature.returned)).append(' ')
        str.append(signature.name).append('(')
        if (signature.parameters.size > 0) {
            str.append(type(signature.parameters[0].type,
                    signature.parameters[0].name))
            for (i in 1..signature.parameters.size - 1) {
                str.append(", ").append(type(signature.parameters[i].type,
                        signature.parameters[i].name))
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
            arrayDeclarationStatement(statement,
                    level)
        } else if (statement is ArrayUnsizedDeclarationStatement) {
            arrayUnsizedDeclarationStatement(
                    statement, level)
        } else if (statement is ExpressionStatement) {
            println(level,
                    expression(statement.expression) + ';')
        } else {
            throw ShaderGenerateException(
                    "Unknown statement: " + statement.javaClass)
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

    enum class Version {
        GL_330,
        GLES_300
    }
}
