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

package org.tobi29.scapes.engine.utils.shader.glsl

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.tobi29.scapes.engine.utils.join
import org.tobi29.scapes.engine.utils.shader.*
import org.tobi29.scapes.engine.utils.shader.Function
import java.math.BigInteger
import java.util.*

class GLSLGenerator(private val version: GLSLGenerator.Version) {
    private val output = StringBuilder(1024)
    private val identifiers = HashMap<Identifier, Expression>()
    private var properties: Map<String, String>? = null
    private var functionsSignatures = ArrayList<Pair<FunctionExportedSignature, String>>()

    private fun staticFunction(expression: FunctionExpression): String {
        val name = expression.name
        val args = Array(expression.args.size) {
            expression(expression.args[it])
        }
        for (function in functionsSignatures) {
            if (function.first.name != name) {
                continue
            }
            if (function.first.parameters.size != args.size) {
                continue
            }
            return "${function.second}(${join(*args)})"
        }
        when (args.size) {
            0 -> staticFunction(name)?.let { return it }
            1 -> staticFunction(name, args[0])?.let { return it }
        }
        throw ShaderGenerateException(
                "No functions for given arguments: $name", expression)
    }

    private fun staticFunction(name: String): String? {
        when (name) {
            "discard" -> return "discard"
        }
        return null
    }

    private fun staticFunction(name: String,
                               argument0: String): String? {
        when (name) {
            "return" -> return "return " + argument0
        }
        return null
    }

    private fun variable(identifier: Identifier): String? {
        val expression = identifiers[identifier] ?: return null
        return pack(expression)
    }

    private fun member(field: String): String {
        return field
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
        } else if (expression is IdentifierExpression) {
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
        throw IllegalArgumentException(
                "Unknown expression: ${expression.javaClass}")
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
            else -> throw IllegalArgumentException(
                    "Unexpected expression type: $type")
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
            else -> throw IllegalArgumentException(
                    "Unexpected expression type: $type")
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
        if (expression is IntegerLiteralExpression) {
            return expression.value.toString()
        } else if (expression is IntegerPropertyExpression) {
            return property(expression.key) ?: throw ShaderGenerateException(
                    "Unknown property: ${expression.key}", expression)
        }
        throw IllegalArgumentException(
                "Unknown integer: ${expression.javaClass}")
    }

    private fun floatingExpression(expression: FloatingExpression): String {
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

    private fun propertyExpression(expression: PropertyExpression): String {
        return property(expression.key) ?: throw ShaderGenerateException(
                "Unknown property: $expression.key", expression)
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
        val start = integer(expression(expression.start))
        val end = integer(expression(expression.end))
        for (i in start..end - 1) {
            identifiers[expression.index] = IntegerLiteralExpression(
                    BigInteger(i.toString()))
            statement(expression.statement, level)
        }
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
            identifiers[declaration.identifier] = GLSLExpression(
                    declaration.identifier.name)
            str.append(declaration(statement.type, declaration))
        }
        return str.toString()
    }

    private fun declaration(type: Type,
                            declaration: Declaration): String {
        val str = identifier(type,
                declaration.identifier) ?: throw ShaderGenerateException(
                "Unknown identifier: ${declaration.identifier.name}",
                declaration)
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
            identifiers[declaration.identifier] = GLSLExpression(
                    declaration.identifier.name)
            str.append(arrayDeclaration(statement.type, declaration))
        }
        return str.toString()
    }

    private fun arrayDeclaration(type: Type,
                                 declaration: ArrayDeclaration): String {
        return identifier(type,
                declaration.identifier) ?: throw ShaderGenerateException(
                "Unknown identifier: ${declaration.identifier.name}",
                declaration)
    }

    private fun arrayUnsizedDeclarationStatement(
            statement: ArrayUnsizedDeclarationStatement,
            level: Int) {
        val initializer: ArrayExpression.Literal
        if (statement.initializer is ArrayExpression.Property) {
            initializer = property(statement.initializer)
        } else if (statement.initializer is ArrayExpression.Literal) {
            initializer = statement.initializer
        } else {
            throw IllegalArgumentException(
                    "Unknown array initializer: ${statement.javaClass}")
        }
        identifiers[statement.identifier] = GLSLExpression(
                statement.identifier.name)
        println(level, type(statement.type, statement.identifier) + '[' +
                initializer.content.size + "] = " +
                type(statement.type.type) + "[]" +
                arrayExpression(initializer) + ';')
    }

    private fun arrayExpression(initializer: ArrayExpression.Literal): String {
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

    private fun property(expression: ArrayExpression.Property): ArrayExpression.Literal {
        val source = property(expression.key) ?: throw ShaderGenerateException(
                "Unknown property: ${expression.key}", expression)
        try {
            val parser = ShaderCompiler.parser(source)
            val expression2 = ShaderCompiler.initializer(
                    parser.initializerArrayList(), Scope())
            if (expression2 is ArrayExpression.Literal) {
                return expression2
            } else {
                throw ShaderGenerateException(
                        "Property has to be a static array expression",
                        expression)
            }
        } catch (e: ParseCancellationException) {
            throw ShaderGenerateException(e)
        } catch (e: ShaderCompileException) {
            throw ShaderGenerateException(e)
        }

    }

    private fun property(name: String): String? {
        val properties = properties ?: throw IllegalStateException(
                "No shader program")
        return properties[name]
    }

    private fun integer(value: String): Int {
        try {
            return value.toInt()
        } catch (e: NumberFormatException) {
            throw ShaderGenerateException(e)
        }
    }

    private inline fun initBuiltIn(identifier: Identifier?,
                                   init: (Identifier) -> Expression) {
        identifier?.let { identifiers[it] = init(it) }
    }

    private fun init(scope: Scope) {
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "texture",
                        Types.Vector4,
                        TypeExported(
                                Types.Int),
                        TypeExported(
                                Types.Vector2)),
                        "texture"))
        for ((type, typeBoolean) in arrayOf(
                Pair(Types.Float, Types.Boolean),
                Pair(Types.Vector2, Types.Vector2),
                Pair(Types.Vector3, Types.Vector3),
                Pair(Types.Vector4, Types.Vector4))) {
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "length", type,
                            TypeExported(
                                    type)), "length"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "abs", type,
                            TypeExported(
                                    type)), "abs"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "floor", type,
                            TypeExported(
                                    type)), "floor"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "sin", type,
                            TypeExported(
                                    type)), "sin"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "cos", type,
                            TypeExported(
                                    type)), "cos"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "min", type,
                            TypeExported(
                                    type),
                            TypeExported(
                                    type)), "min"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "max", type,
                            TypeExported(
                                    type),
                            TypeExported(
                                    type)), "max"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "clamp", type,
                            TypeExported(
                                    type),
                            TypeExported(
                                    type),
                            TypeExported(
                                    type)), "clamp"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "mix", type,
                            TypeExported(
                                    type),
                            TypeExported(
                                    type),
                            TypeExported(
                                    type)), "mix"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "dot", type,
                            TypeExported(
                                    type),
                            TypeExported(
                                    type)), "dot"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "mod", type,
                            TypeExported(
                                    type),
                            TypeExported(
                                    type)), "mod"))
        }

        for ((type, typeBoolean) in arrayOf(
                Pair(Types.Vector2, Types.Vector2),
                Pair(Types.Vector3, Types.Vector3),
                Pair(Types.Vector4, Types.Vector4))) {
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "greaterThan", typeBoolean,
                            TypeExported(
                                    type),
                            TypeExported(
                                    type)), "greaterThan"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "greaterThanEqual",
                            typeBoolean,
                            TypeExported(
                                    type),
                            TypeExported(
                                    type)), "greaterThanEqual"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "lessThan", typeBoolean,
                            TypeExported(
                                    type),
                            TypeExported(
                                    type)), "lessThan"))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "lessThanEqual", typeBoolean,
                            TypeExported(
                                    type),
                            TypeExported(
                                    type)), "lessThanEqual"))
        }

        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "float",
                        Types.Float,
                        TypeExported(
                                Types.Float)), "float"))

        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector2",
                        Types.Vector2,
                        TypeExported(
                                Types.Float)), "vec2"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector2",
                        Types.Vector2,
                        TypeExported(
                                Types.Vector2)), "vec2"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector2",
                        Types.Vector2,
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Float)),
                        "vec2"))

        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector3",
                        Types.Vector3,
                        TypeExported(
                                Types.Float)), "vec3"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector3",
                        Types.Vector3,
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Float)), "vec3"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector3",
                        Types.Vector3,
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Vector2)),
                        "vec3"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector3",
                        Types.Vector3,
                        TypeExported(
                                Types.Vector2),
                        TypeExported(
                                Types.Float)),
                        "vec3"))

        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector4",
                        Types.Vector4,
                        TypeExported(
                                Types.Float)), "vec4"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector4",
                        Types.Vector4,
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Float)),
                        "vec4"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector4",
                        Types.Vector4,
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Vector2)), "vec4"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector4",
                        Types.Vector4,
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Vector2),
                        TypeExported(
                                Types.Float)), "vec4"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector4",
                        Types.Vector4,
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Vector3)),
                        "vec4"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector4",
                        Types.Vector4,
                        TypeExported(
                                Types.Vector2),
                        TypeExported(
                                Types.Float),
                        TypeExported(
                                Types.Float)), "vec4"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector4",
                        Types.Vector4,
                        TypeExported(
                                Types.Vector2),
                        TypeExported(
                                Types.Vector2)), "vec4"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector4",
                        Types.Vector4,
                        TypeExported(
                                Types.Vector3),
                        TypeExported(
                                Types.Float)), "vec4"))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "vector4",
                        Types.Vector4,
                        TypeExported(
                                Types.Vector4)), "vec4"))

        initBuiltIn(scope["discard"]) { GLSLExpression("discard") }
        initBuiltIn(scope["return"]) { GLSLExpression("return") }
        initBuiltIn(scope["out_Position"]) { GLSLExpression("gl_Position") }
        initBuiltIn(scope["varying_Fragment"]) {
            GLSLExpression("gl_FragCoord")
        }
    }

    fun generateVertex(scope: Scope,
                       shader: CompiledShader,
                       properties: Map<String, String>): String {
        if (output.isNotEmpty()) {
            output.delete(0, output.length - 1)
        }
        init(scope)
        this.properties = properties
        val shaderVertex = shader.shaderVertex ?: throw IllegalStateException(
                "No vertex shader")
        val shaderFragment = shader.shaderFragment ?: throw IllegalStateException(
                "No fragment shader")
        val uniforms = shader.uniforms()
        signatureIdentifiers(shaderVertex.signature)
        signatureIdentifiers(shaderFragment.signature)
        header(uniforms, shaderVertex.signature)
        println()
        header(shaderFragment.signature)
        println()
        header(shader.declarations)
        println()
        functions(shader.functions)
        println()
        shader(shaderVertex)
        identifiers.clear()
        functionsSignatures.clear()
        return output.toString()
    }

    fun generateFragment(scope: Scope,
                         shader: CompiledShader,
                         properties: Map<String, String>): String {
        if (output.isNotEmpty()) {
            output.delete(0, output.length - 1)
        }
        init(scope)
        this.properties = properties
        val shaderFragment = shader.shaderFragment ?: throw IllegalStateException(
                "No fragment shader")
        val outputs = shader.outputs ?: throw IllegalStateException(
                "No outputs")
        val uniforms = shader.uniforms()
        signatureIdentifiers(outputs)
        header(uniforms, shaderFragment.signature)
        println()
        header(outputs)
        println()
        header(shader.declarations)
        println()
        functions(shader.functions)
        println()
        shader(shaderFragment)
        identifiers.clear()
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
                        uniform.identifier.name)
                println(0, "uniform " + type(uniform.type) + ' ' +
                        identifier(uniform.type, uniform.identifier) +
                        ';')
            }
        }
        println()
        for (parameter in input.parameters) {
            if (!expression(parameter.available).toBoolean()) {
                continue
            }
            identifiers[parameter.identifier] = GLSLExpression(
                    parameter.identifier.name)
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
            if (!expression(parameter.available).toBoolean()) {
                continue
            }
            identifiers[parameter.identifier] = GLSLExpression(
                    parameter.identifier.name)
        }
    }

    private fun header(output: ShaderSignature) {
        for (parameter in output.parameters) {
            if (!expression(parameter.available).toBoolean()) {
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
            functionsSignatures.add(Pair(
                    FunctionExportedSignature(
                            signature),
                    signature.name))
            println(0, signature(signature, signature.name))
            statement(function.compound, 0)
        }
    }

    private fun signature(signature: FunctionSignature,
                          name: String): String {
        val str = StringBuilder(24)
        str.append(precision(signature.returnedPrecision)).append(' ')
        str.append(type(signature.returned)).append(' ')
        str.append(name).append('(')
        if (signature.parameters.isNotEmpty()) {
            run {
                val parameter = signature.parameters[0]
                identifiers[parameter.identifier] = GLSLExpression(
                        parameter.identifier.name)
                str.append(type(parameter.type, parameter.identifier))
            }
            for (i in 1..signature.parameters.lastIndex) {
                val parameter = signature.parameters[1]
                identifiers[parameter.identifier] = GLSLExpression(
                        parameter.identifier.name)
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
            arrayDeclarationStatement(statement,
                    level)
        } else if (statement is ArrayUnsizedDeclarationStatement) {
            arrayUnsizedDeclarationStatement(
                    statement, level)
        } else if (statement is ExpressionStatement) {
            println(level,
                    expression(statement.expression) + ';')
        } else {
            throw IllegalArgumentException(
                    "Unknown statement: ${statement.javaClass}")
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
