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

package org.tobi29.scapes.engine.shader.backend.glsl

import org.tobi29.scapes.engine.shader.FunctionExportedSignature
import org.tobi29.scapes.engine.shader.STDLib
import org.tobi29.scapes.engine.shader.Type
import org.tobi29.scapes.engine.shader.Types

internal object GLSLSTDLib {
    fun functions(
        functionsSignatures: MutableList<Pair<FunctionExportedSignature, (Array<String>) -> String>>
    ) {
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "texture",
                Type(Types.Vector4),
                Type(Types.Texture2),
                Type(Types.Vector2)
            ), { a -> glslFunction("texture", *a) })
        )

        mathFunctions(
            functionsSignatures, Type(Types.Float),
            Type(Types.Boolean), Type(Types.Float)
        )
        mathFunctions(
            functionsSignatures, Type(Types.Vector2),
            Type(Types.Vector2b), Type(Types.Float)
        )
        mathFunctions(
            functionsSignatures, Type(Types.Vector3),
            Type(Types.Vector3b), Type(Types.Float)
        )
        mathFunctions(
            functionsSignatures, Type(Types.Vector4),
            Type(Types.Vector4b), Type(Types.Float)
        )

        mathFunctions(
            functionsSignatures, Type(Types.Int),
            Type(Types.Boolean), Type(Types.Int)
        )
        mathFunctions(
            functionsSignatures, Type(Types.Vector2i),
            Type(Types.Vector2b), Type(Types.Int)
        )
        mathFunctions(
            functionsSignatures, Type(Types.Vector3i),
            Type(Types.Vector3b), Type(Types.Int)
        )
        mathFunctions(
            functionsSignatures, Type(Types.Vector4i),
            Type(Types.Vector4b), Type(Types.Int)
        )

        logicFunctions(
            functionsSignatures, Type(Types.Boolean),
            Type(Types.Boolean)
        )
        logicFunctions(
            functionsSignatures, Type(Types.Vector2b),
            Type(Types.Vector2b)
        )
        logicFunctions(
            functionsSignatures, Type(Types.Vector3b),
            Type(Types.Vector3b)
        )
        logicFunctions(
            functionsSignatures, Type(Types.Vector4b),
            Type(Types.Vector4b)
        )

        vectorFunctions(
            functionsSignatures, Type(Types.Vector2),
            Type(Types.Vector2b), Type(Types.Float)
        )
        vectorFunctions(
            functionsSignatures, Type(Types.Vector3),
            Type(Types.Vector3b), Type(Types.Float)
        )
        vectorFunctions(
            functionsSignatures, Type(Types.Vector4),
            Type(Types.Vector4b), Type(Types.Float)
        )

        matrixFunctions(
            functionsSignatures, Type(Types.Matrix2),
            Type(Types.Vector2)
        )
        matrixFunctions(
            functionsSignatures, Type(Types.Matrix3),
            Type(Types.Vector3)
        )
        matrixFunctions(
            functionsSignatures, Type(Types.Matrix4),
            Type(Types.Vector4)
        )

        STDLib.constructScalar.forEach {
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "float",
                    Type(Types.Float), it
                ),
                    { a -> glslFunction("float", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "int",
                    Type(Types.Int), it
                ),
                    { a -> glslFunction("int", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "boolean",
                    Type(Types.Boolean), it
                ),
                    { a -> glslFunction("bool", *a) })
            )
        }
        STDLib.constructVector2.forEach {
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "vector2",
                    Type(Types.Vector2), it
                ),
                    { a -> glslFunction("vec2", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "vector2i",
                    Type(Types.Vector2i), it
                ),
                    { a -> glslFunction("vec2i", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "vector2b",
                    Type(Types.Vector2b), it
                ),
                    { a -> glslFunction("vec2b", *a) })
            )
        }
        STDLib.constructVector3.forEach {
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "vector3",
                    Type(Types.Vector3), it
                ),
                    { a -> glslFunction("vec3", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "vector3i",
                    Type(Types.Vector3i), it
                ),
                    { a -> glslFunction("vec3i", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "vector3b",
                    Type(Types.Vector3b), it
                ),
                    { a -> glslFunction("vec3b", *a) })
            )
        }
        STDLib.constructVector4.forEach {
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "vector4",
                    Type(Types.Vector4), it
                ),
                    { a -> glslFunction("vec4", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "vector4i",
                    Type(Types.Vector4i), it
                ),
                    { a -> glslFunction("vec4i", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "vector4b",
                    Type(Types.Vector4b), it
                ),
                    { a -> glslFunction("vec4b", *a) })
            )
        }
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "discard",
                Type(Types.Unit)
            ), { _ -> "discard" })
        )
    }

    private fun mathFunctions(
        functionsSignatures: MutableList<Pair<FunctionExportedSignature, (Array<String>) -> String>>,
        type: Type,
        typeBoolean: Type,
        typeScalar: Type
    ) {
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "plus", type,
                type,
                type
            ),
                { a -> glslOperation("+", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "minus", type,
                type,
                type
            ),
                { a -> glslOperation("-", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "times", type,
                type,
                type
            ),
                { a -> glslOperation("*", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "div", type,
                type,
                type
            ),
                { a -> glslOperation("/", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "rem", type,
                type,
                type
            ),
                { a -> glslFunction("mod", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "length", typeScalar,
                type
            ),
                { a -> glslFunction("length", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "abs", type,
                type
            ),
                { a -> glslFunction("abs", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "floor", type,
                type
            ),
                { a -> glslFunction("floor", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "sin", type,
                type
            ),
                { a -> glslFunction("sin", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "cos", type,
                type
            ),
                { a -> glslFunction("cos", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "min", type,
                type,
                type
            ),
                { a -> glslFunction("min", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "max", type,
                type,
                type
            ),
                { a -> glslFunction("max", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "clamp", type,
                type,
                type,
                type
            ),
                { a -> glslFunction("clamp", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "mix", type,
                type,
                type,
                type
            ),
                { a -> glslFunction("mix", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "mix", type,
                type,
                type,
                typeScalar
            ),
                { a -> glslFunction("mix", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "dot", typeScalar,
                type,
                type
            ),
                { a -> glslFunction("dot", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "mod", type,
                type,
                type
            ),
                { a -> glslFunction("mod", *a) })
        )
        if (type == Type(Types.Int)) {
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "shl", type,
                    type,
                    type
                ),
                    { a -> glslOperation("<<", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "shr", type,
                    type,
                    type
                ),
                    { a -> glslOperation(">>", *a) })
            )
        }
        if (type == typeScalar) {
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "greaterThan", typeBoolean, type, type
                ),
                    { a -> glslOperation(">", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "greaterThanEqual", typeBoolean, type, type
                ),
                    { a -> glslOperation(">=", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "lessThan", typeBoolean, type, type
                ),
                    { a -> glslOperation("<", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "lessThanEqual", typeBoolean, type, type
                ),
                    { a -> glslOperation("<=", *a) })
            )
        } else {
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "greaterThan", typeBoolean, type, type
                ),
                    { a -> glslFunction("greaterThan", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "greaterThanEqual", typeBoolean, type, type
                ),
                    { a -> glslFunction("greaterThanEqual", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "lessThan", typeBoolean, type, type
                ),
                    { a -> glslFunction("lessThan", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "lessThanEqual", typeBoolean, type, type
                ),
                    { a -> glslFunction("lessThanEqual", *a) })
            )
        }
        logicFunctions(functionsSignatures, type, typeBoolean)
    }

    private fun logicFunctions(
        functionsSignatures: MutableList<Pair<FunctionExportedSignature, (Array<String>) -> String>>,
        type: Type,
        typeBoolean: Type
    ) {
        if (type == Type(Types.Int) || type == Type(Types.Boolean)) {
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "and", type, type, type
                ),
                    { a -> glslOperation("&", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "or", type, type, type
                ),
                    { a -> glslOperation("|", *a) })
            )
            functionsSignatures.add(
                Pair(FunctionExportedSignature(
                    "xor", type, type, type
                ),
                    { a -> glslOperation("^", *a) })
            )
        }
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "equals", typeBoolean, type, type
            ),
                { a -> glslOperation("==", *a) })
        )
    }

    private fun vectorFunctions(
        functionsSignatures: MutableList<Pair<FunctionExportedSignature, (Array<String>) -> String>>,
        type: Type,
        typeBoolean: Type,
        typeScalar: Type
    ) {
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "plus", type,
                type,
                typeScalar
            ),
                { a -> glslOperation("+", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "plus", type,
                typeScalar,
                type
            ),
                { a -> glslOperation("+", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "minus", type,
                type,
                typeScalar
            ),
                { a -> glslOperation("-", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "minus", type,
                typeScalar,
                type
            ),
                { a -> glslOperation("-", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "times", type,
                type,
                typeScalar
            ),
                { a -> glslOperation("*", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "times", type,
                typeScalar,
                type
            ),
                { a -> glslOperation("*", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "div", type,
                type,
                typeScalar
            ),
                { a -> glslOperation("/", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "div", type,
                typeScalar,
                type
            ),
                { a -> glslOperation("/", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "rem", type,
                type,
                typeScalar
            ),
                { a -> glslFunction("mod", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "rem", type,
                typeScalar,
                type
            ),
                { a -> glslFunction("mod", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "min", type,
                type,
                typeScalar
            ),
                { a -> glslFunction("min", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "min", type,
                typeScalar,
                type
            ),
                { a -> glslFunction("min", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "max", type,
                type,
                typeScalar
            ),
                { a -> glslFunction("max", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "max", type,
                typeScalar,
                type
            ),
                { a -> glslFunction("max", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "clamp", type,
                type,
                type,
                typeScalar
            ),
                { a -> glslFunction("clamp", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "clamp", type,
                type,
                typeScalar,
                type
            ),
                { a -> glslFunction("clamp", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "clamp", type,
                typeScalar,
                type,
                typeScalar
            ),
                { a -> glslFunction("clamp", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "clamp", type,
                typeScalar,
                typeScalar,
                type
            ),
                { a -> glslFunction("clamp", *a) })
        )
    }

    private fun matrixFunctions(
        functionsSignatures: MutableList<Pair<FunctionExportedSignature, (Array<String>) -> String>>,
        type: Type,
        typeVector: Type
    ) {
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "plus", type,
                type,
                type
            ),
                { a -> glslOperation("+", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "minus", type,
                type,
                type
            ),
                { a -> glslOperation("-", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "times", type,
                type,
                type
            ),
                { a -> glslOperation("*", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "div", type,
                type,
                type
            ),
                { a -> glslOperation("/", *a) })
        )
        functionsSignatures.add(
            Pair(FunctionExportedSignature(
                "times", typeVector,
                type,
                typeVector
            ),
                { a -> glslOperation("*", *a) })
        )
    }

    private fun glslFunction(
        name: String,
        vararg args: String
    ) =
        "$name(${args.joinToString()})"

    private fun glslOperation(
        name: String,
        vararg args: String
    ) =
        args.asSequence().map { "($it)" }.joinToString(
            separator = " $name "
        )
}
