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

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode
import org.tobi29.scapes.engine.utils.math.vector.Vector2i
import org.tobi29.scapes.engine.utils.readOnly
import org.tobi29.scapes.engine.utils.toArray
import java.math.BigDecimal
import java.math.BigInteger

open class Expression {
    var location: Vector2i? = null

    fun attach(context: ParserRuleContext) {
        attach(context.start)
    }

    fun attach(context: TerminalNode) {
        attach(context.symbol)
    }

    fun attach(token: Token) {
        location = Vector2i(
                token.line, token.charPositionInLine)
    }
}

class ShaderProgram(
        declarations: List<Statement>,
        functions: List<Function>,
        shaders: Map <String, Pair<Scope, (Scope) -> ShaderFunction>>,
        val outputs: ShaderSignature?,
        uniforms: List<Uniform?>
) : Expression() {
    val declarations = declarations.readOnly()
    val functions = functions.readOnly()
    val shaders = shaders.readOnly()
    val uniforms = uniforms.readOnly()
}

sealed class ArrayExpression : Expression() {
    class Literal(content: List<Expression>) : ArrayExpression() {
        val content = content.readOnly()
    }

    class Property(val key: String) : ArrayExpression()
}

class ArrayAccessExpression(val name: Expression,
                            val index: Expression) : Expression()

class ArrayDeclaration(val identifier: Identifier) : Expression()

class ArrayDeclarationStatement(val type: Type,
                                val length: Expression,
                                declarations: List<ArrayDeclaration>) : Statement() {
    val declarations = declarations.readOnly()
}

class ArrayUnsizedDeclarationStatement(val type: Type,
                                       val identifier: Identifier,
                                       val initializer: ArrayExpression) : Statement()

class AssignmentExpression(val type: AssignmentType,
                           val left: Expression,
                           val right: Expression) : Expression()

class BooleanExpression(val value: Boolean) : Expression()

class CompoundStatement(val block: StatementBlock) : Statement()

class ConditionExpression(val type: ConditionType,
                          val left: Expression,
                          val right: Expression) : Expression()

class Declaration(val identifier: Identifier,
                  val initializer: Expression? = null) : Expression()

class DeclarationStatement(val type: Type,
                           declarations: List<Declaration>) : Statement() {
    val declarations = declarations.readOnly()
}

class ExpressionStatement(val expression: Expression) : Statement()

class FloatingExpression(val value: BigDecimal) : Expression()

class Function(val signature: FunctionSignature,
               val compound: CompoundStatement)

class FunctionExportedSignature(val name: String,
                                val returned: Types,
                                vararg val parameters: TypeExported) : Expression() {
    constructor(signature: FunctionSignature) : this(signature.name,
            signature.returned, *convertParameters(
            signature.parameters))

    companion object {
        private fun convertParameters(parameters: Array<out Parameter>) = parameters.asSequence().map {
            TypeExported(it.type.type, it.type.array != null)
        }.toArray()
    }
}

class FunctionExpression(val name: String,
                         val args: List<Expression>) : Expression()

class FunctionSignature(val name: String,
                        val returned: Types,
                        val returnedPrecision: Precision,
                        vararg val parameters: Parameter)

class IdentifierExpression(val identifier: Identifier) : Expression()

class IfStatement(val condition: Expression,
                  val statement: Statement,
                  val statementElse: Statement? = null) : Statement()

open class IntegerExpression : Expression()

class IntegerLiteralExpression(val value: BigInteger) : IntegerExpression()

class IntegerPropertyExpression(val key: String) : IntegerExpression()

class LoopFixedStatement(val index: Identifier,
                         val start: IntegerExpression,
                         val end: IntegerExpression,
                         val statement: Statement) : Statement()

class MemberExpression(val name: String,
                       val member: Expression) : Expression()

class OperationExpression(val type: OperationType,
                          val left: Expression,
                          val right: Expression) : Expression()

class Parameter(val type: Type,
                val identifier: Identifier)

class PropertyExpression(val key: String) : Expression()
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

open class Statement : Expression()
class StatementBlock(statements: List<Statement>) : Expression() {
    val statements = statements.readOnly()
}

class TernaryExpression(val condition: Expression,
                        val expression: Expression,
                        val expressionElse: Expression) : Expression()

class Type(val type: Types,
           val array: Expression? = null,
           val constant: Boolean,
           val precision: Precision) {
    constructor(type: Types,
                constant: Boolean,
                precision: Precision) : this(
            type, null, constant, precision)
}

class TypeExported(val type: Types,
                   val array: Boolean = false)

class UnaryExpression(val type: UnaryType,
                      val value: Expression) : Expression()

class Uniform(val type: Type,
              val id: Int,
              val identifier: Identifier)

class VoidExpression : Expression()

enum class AssignmentType {
    ASSIGN,
    ASSIGN_SHIFT_LEFT,
    ASSIGN_SHIFT_RIGHT,
    ASSIGN_PLUS,
    ASSIGN_MINUS,
    ASSIGN_MULTIPLY,
    ASSIGN_DIVIDE,
    ASSIGN_MODULUS,
    ASSIGN_AND,
    ASSIGN_INCLUSIVE_OR,
    ASSIGN_EXCLUSIVE_OR
}

enum class ConditionType {
    CONDITION_LOGICAL_OR,
    CONDITION_LOGICAL_AND,
    CONDITION_INCLUSIVE_OR,
    CONDITION_EXCLUSIVE_OR,
    CONDITION_AND,
    CONDITION_EQUALS,
    CONDITION_NOT_EQUALS,
    CONDITION_LESS,
    CONDITION_GREATER,
    CONDITION_LESS_EQUAL,
    CONDITION_GREATER_EQUAL
}

enum class OperationType {
    SHIFT_LEFT,
    SHIFT_RIGHT,
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    MODULUS
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
    Texture2
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